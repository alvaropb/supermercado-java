package com.ipartek.formacion.controller.backoffice;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Set;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;

import org.apache.log4j.Logger;

import com.ipartek.formacion.controller.Alerta;
import com.ipartek.formacion.modelo.dao.impl.CategoriaDAOImpl;
import com.ipartek.formacion.modelo.pojo.Categoria;


/**
 * Servlet implementation class CategoriaBackofficeController
 */
@WebServlet("/views/backoffice/categoria")
public class CategoriaBackofficeController extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private final static Logger LOG = Logger.getLogger(CategoriaBackofficeController.class);
	private final static CategoriaDAOImpl dao = CategoriaDAOImpl.getInstance();
	private static String VIEW = "";
	private static ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
	private static Validator validator = factory.getValidator();

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		LOG.trace("CategoriaBackofficeController listado categorias");

		// recoger parametros de la vista
		String idParam = request.getParameter("id");
		String accion = request.getParameter("accion");
		
		try {
			if (idParam == null) {

				// no hemos pasado id, asi que listamos todo
				request.setAttribute("categorias", dao.getAll());
				VIEW = "categoria/index.jsp";

			} else {

				int id = Integer.parseInt(idParam);

				if (accion == null) {

					// crear o editar, VIEW =formulario
					VIEW = "categoria/formulario.jsp";

					// new categoria
					Categoria categoria = new Categoria();

					if (id > 0) {// estamos editando

						// categoria =getById
						categoria = dao.getById(id);
					}
					// setear categoria
					request.setAttribute("categoria", categoria);

				} else {
					// eliminar
					dao.delete(id);
					ArrayList<Categoria>categorias=dao.getAll();
					request.setAttribute("categorias", categorias);
					
					request.getServletContext().setAttribute("categorias", categorias );
			    	
			    	
			    	
			    		
			    		
				}

			} // fin else id

		} catch (Exception e) {
			LOG.error(e);
			request.setAttribute("alerta", new Alerta("warning", "ocurrio un error"));
		} finally {
			request.getRequestDispatcher(VIEW).forward(request, response);

		}

	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		LOG.trace("CategoriaBackofficeController formulario categoria");
		// recoger parametros de la vista
		String idParam = request.getParameter("id");
		String nombre = request.getParameter("nombre");

		Categoria categoria = new Categoria();
		Alerta alerta=new Alerta();
		VIEW = "categoria/formulario.jsp";
		try {
			if (idParam != null) {
				int id = Integer.parseInt(idParam);
				categoria.setId(id);
				categoria.setNombre(nombre);

				Set<ConstraintViolation<Categoria>> errores = validator.validate(categoria);
				try {
					// validar pojo
					if (errores.isEmpty()) {

						if (id > 0) {// update
							categoria = dao.update(categoria);
							alerta=new Alerta("success", "actualizacion correcta");
						} else {// insert
							categoria = dao.insert(categoria);
							alerta=new Alerta("success", "insercion correcta");
						}
						request.getServletContext().setAttribute("categorias", dao.getAll() );
					} else {
						String listaErrores = listarErrores(errores);
						
						alerta=new Alerta("danger", "Error:" + listaErrores);
						
						
					}

				} catch (Exception e) {
					LOG.error(e);
					alerta=new Alerta("danger", "Error:nombre ya existe" );
					
				}
			}
			request.setAttribute("categoria", categoria);
		} catch (Exception e) {
			LOG.error(e);
			
			alerta=new Alerta("warning", "ocurrio un error");
			
			
		} finally {
			request.setAttribute("alerta", alerta);
			request.setAttribute("categoria", categoria);
			request.getRequestDispatcher(VIEW).forward(request, response);
		}

	}

	private String listarErrores(Set<ConstraintViolation<Categoria>> errores) {
		String erroresCadena="";
		
		
		
		
		for (ConstraintViolation<Categoria> item : errores) {
			//erroresCadena.concat("<b>").concat(item.getMessage()).concat("</b><br>");
			erroresCadena+="<b>"+item.getMessage()+"</b><br>";
		}
		return erroresCadena;
	}



}
