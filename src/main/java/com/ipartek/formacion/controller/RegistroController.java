package com.ipartek.formacion.controller;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import com.ipartek.formacion.modelo.dao.UsuarioDAO;
import com.ipartek.formacion.modelo.dao.impl.UsuarioDAOImpl;
import com.ipartek.formacion.modelo.pojo.Rol;
import com.ipartek.formacion.modelo.pojo.Usuario;

/**
 * Servlet implementation class RegistroController
 */
@WebServlet("/views/usuarios/registro")
public class RegistroController extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static final Logger LOG = Logger.getLogger(RegistroController.class);
	private static final UsuarioDAO dao = UsuarioDAOImpl.getInstance();

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		LOG.trace("entra en doPost RegistroController");
		String redireccion = "";

		Usuario usuario = new Usuario();

		Alerta alerta = new Alerta();
		String tipo = "";
		String mensaje = "";

		// recoger parametros de la vista

		String nombre = request.getParameter("nombre");
		String pass = request.getParameter("pass");
		String repass = request.getParameter("repass");
		String fnaci =request.getParameter("fnaci");
		


		usuario.setNombre(nombre);
		usuario.setContrasenia(pass);
		usuario.setRol(new Rol(Rol.USUARIO));

		try {
			SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
			Date date=format.parse(fnaci);
			usuario.setFechaNacimiento(date);
			
			
			if (!pass.equals(repass)) {
				tipo = "warning";
				mensaje = "Contraseñas no coinciden";

				throw new Exception("contraseñas no coinciden");
			}

			mensaje = "Ocurrio un error inesperado";
			tipo = "warning";

			dao.insert(usuario);

			redireccion = "/inicio";

			tipo = "success";
			mensaje = "Datos Guardado con exito";

		} catch (Exception e) {
			LOG.error(e);

			redireccion = "/views/usuarios/registro.jsp";
			request.setAttribute("usuario", usuario);
		} finally {
			alerta.setTipo(tipo);
			alerta.setTexto(mensaje);
			request.setAttribute("alerta", alerta);
			request.getRequestDispatcher(redireccion).forward(request, response);
		}

	}

}
