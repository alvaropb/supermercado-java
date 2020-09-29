package com.ipartek.formacion.controller;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.util.Set;

import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;
import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;

import org.apache.log4j.Logger;

import com.ipartek.formacion.modelo.dao.impl.CategoriaDAOImpl;
import com.ipartek.formacion.modelo.dao.impl.ProductoDAOImpl;
import com.ipartek.formacion.modelo.pojo.Categoria;
import com.ipartek.formacion.modelo.pojo.Producto;
import com.ipartek.formacion.modelo.pojo.Usuario;

/**
 * Servlet implementation class ProductoCrearController
 */
@WebServlet("/producto")
@MultipartConfig
public class ProductoGuardarController extends HttpServlet {
	
	private static final long serialVersionUID = 1L;
	private final static Logger LOG = Logger.getLogger(ProductoGuardarController.class);
	
	private static ProductoDAOImpl daoProducto = ProductoDAOImpl.getInstance();
	private static CategoriaDAOImpl daoCategoria = CategoriaDAOImpl.getInstance();
	
	private static ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
	private static Validator validator = factory.getValidator();
	private static String RUTA="/home/javaee/eclipse-workspace/supermercado-java/src/main/webapp/imagenes/";
	private static long TAMAGNO_MAXIMO=1024*1024*5;//5 megas
	private static String JPG="jpg";
	private static String PNG="png";
	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		try {
			String parametroId = request.getParameter("id");
			Producto producto = new Producto();
			
			if ( parametroId != null ) {
			
				int id = Integer.parseInt(parametroId);			
				ProductoDAOImpl dao = ProductoDAOImpl.getInstance();		
				producto = dao.getById(id);
			}		
			
			request.setAttribute("producto", producto);
			
			
		} catch (Exception e) {
			
			e.printStackTrace();
			
		}finally {
			
			request.setAttribute("categorias", daoCategoria.getAll());
			// ir a la nueva vista o jsp
			request.getRequestDispatcher("views/productos/formulario.jsp").forward(request, response);	
		}
		
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		Alerta alerta = new Alerta();
		Producto producto = new Producto();		
		
		try {
			
			// recoger los valores del formulario
			String idParametro = request.getParameter("id");
			String nombre = request.getParameter("nombre");
			String precio = request.getParameter("precio");
			String categoriaId = request.getParameter("categoria_id");
			Usuario usuario=(Usuario) request.getSession().getAttribute("usuario_login");
			
			//*********************INICIO fichero*************************
			//TODO validar tamaño y extensiones
			
			
			//TODO preveer si al hacer update se guarda la imagen
			String description = request.getParameter("description"); // Retrieves <input type="text" name="description">
			Part filePart = request.getPart("fichero"); // Retrieves <input type="file" name="file">
			String fileName = Paths.get(filePart.getSubmittedFileName()).getFileName().toString(); // MSIE fix.
           
	


			
			//*********************FIN fichero****************************
			
			int id = Integer.parseInt(idParametro);
			int idCategoria = Integer.parseInt(categoriaId);
			float precioFloat = Float.parseFloat(precio);
			
			
			producto.setId(id);
			producto.setNombre(nombre);
			producto.setImagen("imagenes/"+fileName);
			producto.setPrecio(precioFloat);
			
			Categoria c = new Categoria();
			c.setId(idCategoria);
			producto.setCategoria(c);
			
			// agregar el usuario
			producto.setUsuario(usuario);
			
			
			
			Set<ConstraintViolation<Producto>> violations = validator.validate(producto);
		    long tamagno=filePart.getSize();
		    String extension=fileName.split("\\.")[1];


			
			if ( violations.isEmpty()&&TAMAGNO_MAXIMO >= tamagno&&(PNG.equals(extension)||JPG.equals(extension)) ) {  // sin errores de validacion, podemos guardar en bbd
				
				if ( id == 0 ) {
					daoProducto.insert(producto);
					fileUpload(filePart,fileName);
				}else {
					daoProducto.update(producto);
					if (filePart!=null) {
						fileUpload(filePart,fileName);
					}
				}			
			
				alerta = new Alerta( "success", "Producto guardado con exito");
				
			}else {                        // tenemos errores de validacion
				
				String errores = "";
				for (ConstraintViolation<Producto> v : violations) {					
					errores += "<p><b>" + v.getPropertyPath() + "</b>: "  + v.getMessage() + "</p>";					
				}
				if (TAMAGNO_MAXIMO<tamagno) {
					errores += "<p><b>" + "ERROR" + "</b>: "  + "tamaño de archivo ha de ser menor a 5 megas" + "</p>";
				}
				if (!(PNG.equals(extension)||JPG.equals(extension))) {
					errores += "<p><b>" + "ERROR" + "</b>: "  + "Extension debe ser una imagen png o jpg" +"envio un archivo ."+extension+ "</p>";
				}
				alerta = new Alerta( "danger", errores );
				
			}
		
		} catch ( SQLException e) {	
			
			alerta = new Alerta( "danger", "Lo sentimos pero ya existe ese NOMBRE, escribe otro por favor ");
			e.printStackTrace();
			
		} catch (Exception e) {
			
			alerta = new Alerta( "danger", "Lo sentimos pero hemos tenido un ERROR inxesperado ");
			e.printStackTrace();
			
		}finally {
		

			// enviar datos a la vista
			request.setAttribute("alerta", alerta);
			request.setAttribute("producto", producto);			
			request.setAttribute("categorias", daoCategoria.getAll());

			// ir a la nueva vista o jsp
			request.getRequestDispatcher("views/productos/formulario.jsp").forward(request, response);
			
		}
		
		
		
	}//doPost
	/**
	 * Metodo que sube un fichero a la ruta de imagenes del proyecto
	 * @param filePart
	 * @param fileName
	 * @throws Exception 
	 */
	private void fileUpload(Part filePart,String fileName) throws Exception {
	    
	    
	    
	    InputStream fileContent = filePart.getInputStream();
	    

	    //ruta de destino
	    // /home/javaee/eclipse-workspace/supermercado-java/src/main/webapp/imagenes/
	
        File fileToSave = new File(RUTA+fileName);
        Files.copy(fileContent, fileToSave.toPath());
        LOG.info("Imagen subida "+fileName+" tamaño:"+filePart.getSize());
		
	}

}
