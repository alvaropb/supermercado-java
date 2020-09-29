<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>    
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>   

<jsp:include page="../../../includes/office-head.jsp" />
<jsp:include page="../../../includes/office-navbar-admin.jsp" />
    

	<a href="views/backoffice/categoria?id=0">Crear categoria</a>
	<table class="tabla table table-striped">
		<thead>
			<tr>
				<td>Id</td>
				<td>Nombre</td>
				<td>Operaciones</td>
			</tr>
		</thead>
		<tbody>
			<c:forEach items="${categorias}" var="c">
				<tr>
					<td>${c.id}</td> <% // no hace falta usar el getter p.id == p.getId() %>
					<td>${c.nombre}</td>
					<td>
						<a href="views/backoffice/categoria?id=${c.id}" class="mr-4"> <i class="far fa-edit fa-2x" title="Editar Categoria"></i></a>
						<a href="views/backoffice/categoria?id=${c.id}&accion=eliminar"
						   onclick="confirmar('${c.nombre}')" 
						   ><i class="fas fa-trash fa-2x" title="Eliminar Categoria"></i></a>
					</td>
				</tr>
			</c:forEach>
		</tbody>
	</table>
  
 <jsp:include page="../../../includes/office-footer.jsp" />    
