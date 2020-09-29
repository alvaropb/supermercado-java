<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>    
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>


<jsp:include page="../../includes/cabecera.jsp" >
  <jsp:param name="pagina" value="registro" />
  <jsp:param name="title" value="Registrar Usuario" /> 
</jsp:include>


<h1>Registrar Usuario</h1>

	<form action="views/usuarios/registro" method="post" >
			

		
		<div class="form-group">
			<label for="nombre">nombre:</label>
			<input type="text" name="nombre" id="nombre" value="${usuario.nombre}" class="form-control" placeholder="Nombre de usuario" >
		</div>
		
		
		<div class="form-group">
			<label for="pass">Contraseña:</label>
			<input type="password" name="pass" id="pass" value="${usuario.contrasenia}" class="form-control" >
		</div>
		<div class="form-group">
			<label for="repass">Repita Contraseña:</label>
			<input type="password" name="repass" id="repass" value="" class="form-control" >
		</div>
		<div class="form-group">
			<label for="fnaci">fecha nacimiento</label>
			<input type="date" name="fnaci" id="fnaci" value="" class="form-control" >
		</div>
						
				
	
				
				
		<input type="submit" value="Guardar" class="btn btn-primary btn-block">
	</form>

<%@include file="../../includes/pie.jsp" %>

