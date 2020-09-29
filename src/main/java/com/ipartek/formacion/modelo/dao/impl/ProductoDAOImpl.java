package com.ipartek.formacion.modelo.dao.impl;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import org.apache.log4j.Logger;

import com.ipartek.formacion.modelo.ConnectionManager;
import com.ipartek.formacion.modelo.dao.ProductoDAO;
import com.ipartek.formacion.modelo.dao.SeguridadException;
import com.ipartek.formacion.modelo.pojo.Categoria;
import com.ipartek.formacion.modelo.pojo.Producto;
import com.ipartek.formacion.modelo.pojo.ResumenUsuario;
import com.ipartek.formacion.modelo.pojo.Usuario;

public class ProductoDAOImpl implements ProductoDAO {

	private final static Logger LOG = Logger.getLogger(ProductoDAOImpl.class);
	private static ProductoDAOImpl INSTANCE = null;

	private ProductoDAOImpl() {
		super();
	}

	public static synchronized ProductoDAOImpl getInstance() {

		if (INSTANCE == null) {
			INSTANCE = new ProductoDAOImpl();
		}

		return INSTANCE;
	}

	private final String SELECT_CAMPOS = "SELECT u.id 'usuario_id', u.nombre 'usuario_nombre', p.id  'producto_id', p.nombre 'producto_nombre', precio, imagen, c.id 'categoria_id', c.nombre 'categoria_nombre' ";
	private final String FROM_INNER_JOIN = " FROM producto p , categoria c, usuario u WHERE p.id_categoria  = c.id AND p.id_usuario = u.id  ";
	
	// excuteQuery => ResultSet
	private final String SQL_GET_ALL = SELECT_CAMPOS + FROM_INNER_JOIN + " AND fecha_validado IS NOT NULL " + " ORDER BY p.id DESC LIMIT 500; ";

	private final String SQL_GET_LAST = SELECT_CAMPOS + FROM_INNER_JOIN + " AND fecha_validado IS NOT NULL " + " ORDER BY p.id DESC LIMIT ? ; ";

	private final String SQL_PA_GET_BY_CATEGORIA = "{CALL pa_producto_por_categoria(?,?)}";

	private final String SQL_GET_BY_USUARIO_PRODUCTO_VALIDADO = SELECT_CAMPOS + FROM_INNER_JOIN + " AND fecha_validado IS NOT NULL AND p.id_usuario = ? \n"
			+ "ORDER BY p.id DESC LIMIT 500; ";

	private final String SQL_GET_BY_USUARIO_PRODUCTO_SIN_VALIDAR = SELECT_CAMPOS + FROM_INNER_JOIN + " AND fecha_validado IS NULL AND p.id_usuario = ? \n"
			+ "ORDER BY p.id DESC LIMIT 500; ";

	private final String SQL_GET_BY_ID = SELECT_CAMPOS + FROM_INNER_JOIN + " AND p.id = ? ; ";

	private final String SQL_GET_BY_ID_AND_USER = SELECT_CAMPOS + FROM_INNER_JOIN + " AND p.id = ? AND p.id_usuario = ? ; ";

	//view
	private final String SQL_VIEW_RESUMEN_USUARIO = " SELECT id_usuario, total, aprobado, pendiente FROM v_usuario_productos WHERE id_usuario = ?; ";
	
	// excuteUpdate => int numero de filas afectadas
	private final String SQL_INSERT = " INSERT INTO producto (nombre, imagen, precio , id_usuario, id_categoria ) VALUES ( ? , ?, ? , ?,  ? ) ; ";
	private final String SQL_UPDATE = " UPDATE producto SET nombre = ?, imagen = ?, precio = ?, id_categoria = ? WHERE id = ?; ";
	private final String SQL_UPDATE_BY_USER = " UPDATE producto SET nombre = ?, imagen = ?, precio = ?, id_categoria = ? , fecha_validado = NULL WHERE id = ?; ";

	private final String SQL_DELETE = " DELETE FROM producto WHERE id = ? ; ";
	private final String SQL_DELETE_BY_USER = " DELETE FROM producto WHERE id = ? AND id_usuario = ? ; ";

	@Override
	public void validar(int id) {
		
		//TODO UPDATE producto SET fecha_validado = NOW() WHERE id = 15;
	}

	public ArrayList<Producto> getAllByNombre(String nombre) {
		return null;
	}

	@Override
	public ArrayList<Producto> getAll() {

		ArrayList<Producto> registros = new ArrayList<Producto>();

		try (Connection conexion = ConnectionManager.getConnection();
				PreparedStatement pst = conexion.prepareStatement(SQL_GET_ALL);
				ResultSet rs = pst.executeQuery();

		) {

			LOG.debug(pst);
			while (rs.next()) {

				registros.add(mapper(rs));

			} // while

		} catch (Exception e) {
			LOG.error(e);
		}

		return registros;
	}

	@Override
	public ArrayList<Producto> getAllByUser(int idUsuario, boolean isValidado) {
		ArrayList<Producto> registros = new ArrayList<Producto>();

		String sql = (isValidado) ? SQL_GET_BY_USUARIO_PRODUCTO_VALIDADO : SQL_GET_BY_USUARIO_PRODUCTO_SIN_VALIDAR;

		try (Connection conexion = ConnectionManager.getConnection();
				PreparedStatement pst = conexion.prepareStatement(sql);) {
		
			pst.setNull(1, java.sql.Types.NULL);
			pst.setInt(1, idUsuario);

			LOG.debug(pst);

			try (ResultSet rs = pst.executeQuery()) {
				while (rs.next()) {
					registros.add(mapper(rs));
				}
			}

		} catch (Exception e) {
			LOG.error(e);
		}

		return registros;
	}

	@Override
	public ArrayList<Producto> getLast(int numReg) {

		ArrayList<Producto> registros = new ArrayList<Producto>();
		try (Connection conexion = ConnectionManager.getConnection();
				PreparedStatement pst = conexion.prepareStatement(SQL_GET_LAST);) {
			pst.setInt(1, numReg);
			LOG.debug(pst);
			try (ResultSet rs = pst.executeQuery()) {
				while (rs.next()) {
					registros.add(mapper(rs));
				}
			}

		} catch (Exception e) {
			LOG.error(e);
		}
		return registros;
	}

	@Override
	public ArrayList<Producto> getAllByCategoria(int idCategoria, int numReg) {
		ArrayList<Producto> registros = new ArrayList<Producto>();
		try (Connection conexion = ConnectionManager.getConnection();
			CallableStatement cs= conexion.prepareCall(SQL_PA_GET_BY_CATEGORIA);) {
			cs.setInt(1, idCategoria);
			cs.setInt(2, numReg);
			LOG.debug(cs);
			try (ResultSet rs = cs.executeQuery()) {
				while (rs.next()) {
					registros.add(mapper(rs));
				}
			}

		} catch (Exception e) {
			LOG.error(e);
		}
		return registros;
	}

	@Override
	public Producto getById(int id) throws Exception {
		Producto registro = new Producto();

		try (Connection conexion = ConnectionManager.getConnection();
				PreparedStatement pst = conexion.prepareStatement(SQL_GET_BY_ID);) {

			pst.setInt(1, id);
			LOG.debug(pst);
			ResultSet rs = pst.executeQuery();

			if (rs.next()) {

				registro = mapper(rs);

			} else {
				throw new Exception("No se puede encontrar registro con id=" + id);
			}

		}

		return registro;
	}

	@Override
	public Producto checkSeguridad(int idProducto, int idUsuario) throws Exception, SeguridadException {

		Producto registro = new Producto();

		try (Connection conexion = ConnectionManager.getConnection();
				PreparedStatement pst = conexion.prepareStatement(SQL_GET_BY_ID_AND_USER);) {

			pst.setInt(1, idProducto);
			pst.setInt(2, idUsuario);
			LOG.debug(pst);
			ResultSet rs = pst.executeQuery();

			if (rs.next()) {
				registro = mapper(rs);
			} else {
				throw new SeguridadException();
			}

		}

		return registro;
	}

	@Override
	public Producto delete(int id) throws Exception {

		// conseguir el producto antes de Eliminar
		Producto registro = getById(id);

		try (Connection conexion = ConnectionManager.getConnection();
				PreparedStatement pst = conexion.prepareStatement(SQL_DELETE);

		) {

			pst.setInt(1, id);
			LOG.debug(pst);
			int affectedRows = pst.executeUpdate();

			if (affectedRows != 1) {
				throw new Exception("No se puedo eliminar el registro id = " + id);
			}

		} // try

		return registro;
	}

	@Override
	public Producto delete(int idProducto, int idUsuario) throws Exception, SeguridadException {

		
		Producto registro = checkSeguridad(idProducto, idUsuario);

		try (Connection conexion = ConnectionManager.getConnection();
				PreparedStatement pst = conexion.prepareStatement(SQL_DELETE_BY_USER);) {

			pst.setInt(1, idProducto);
			pst.setInt(2, idUsuario);
			LOG.debug(pst);

			pst.executeUpdate();

		}

		return registro;
	}

	@Override
	public Producto insert(Producto pojo) throws Exception {

		try (Connection conexion = ConnectionManager.getConnection();
				PreparedStatement pst = conexion.prepareStatement(SQL_INSERT, PreparedStatement.RETURN_GENERATED_KEYS);

		) {

			pst.setString(1, pojo.getNombre());
			pst.setString(2, pojo.getImagen());
			pst.setFloat(3, pojo.getPrecio());
			pst.setInt(4, pojo.getUsuario().getId());
			pst.setInt(5, pojo.getCategoria().getId());
			LOG.debug(pst);
			int affectedRows = pst.executeUpdate();

			if (affectedRows == 1) {

				// conseguir el ID

				try (ResultSet rsKeys = pst.getGeneratedKeys()) {

					if (rsKeys.next()) {
						int id = rsKeys.getInt(1);
						pojo.setId(id);
					}

				}

			} else {
				throw new Exception("No se ha podido guardar el registro " + pojo);
			}

		}

		return pojo;
	}

	@Override
	public Producto update(Producto pojo) throws Exception {

		try (Connection conexion = ConnectionManager.getConnection();
				PreparedStatement pst = conexion.prepareStatement(SQL_UPDATE);

		) {

			// TODO antes de modificar comprobar el ROL del usuario
			// si es ADMIN hacer la update que tenemos abajo
			// si es USER comprobar que le pertenezca ??

			// throw new SeguridadException( SeguridadException.MENSAJE_1 );
			// throw new SeguridadException();

			pst.setString(1, pojo.getNombre());
			pst.setString(2, pojo.getImagen());
			pst.setFloat(3, pojo.getPrecio());
			pst.setInt(4, pojo.getCategoria().getId());
			pst.setInt(5, pojo.getId());
			LOG.debug(pst);
			int affectedRows = pst.executeUpdate();
			if (affectedRows != 1) {
				throw new Exception("No se puede podificar el registro con id=" + pojo.getId());
			}

		}

		return pojo;
	}
	
	
	@Override
	public Producto updateByUser(Producto pojo) throws Exception, SeguridadException {
		
		try (Connection conexion = ConnectionManager.getConnection();
				PreparedStatement pst = conexion.prepareStatement(SQL_UPDATE_BY_USER);

		) {
			int idProducto = pojo.getId();
			int idUsuario = pojo.getUsuario().getId();
			
			checkSeguridad(idProducto, idUsuario); 
			
			pst.setString(1, pojo.getNombre());
			pst.setString(2, pojo.getImagen());
			pst.setFloat(3, pojo.getPrecio());
			pst.setInt(4, pojo.getCategoria().getId());
			pst.setInt(5, pojo.getId());
			LOG.debug(pst);
			
			int affectedRows = pst.executeUpdate();
			if (affectedRows != 1) {
				throw new Exception("No se puede podificar el registro con id=" + pojo.getId());
			}

		}

		return pojo;
	}
	
	

	@Override
	public ArrayList<Producto> getAllRangoPrecio(int precioMinimo, int precioMaximo) throws Exception {
		throw new Exception("Sin implemntar");
	}

	@Override
	public ResumenUsuario getResumenByUsuario(int idUsuario) {
		ResumenUsuario resul = new ResumenUsuario();
		try (Connection conexion = ConnectionManager.getConnection();
				PreparedStatement pst = conexion.prepareStatement(SQL_VIEW_RESUMEN_USUARIO);) {

			pst.setInt(1, idUsuario);
			LOG.debug(pst);

			try (ResultSet rs = pst.executeQuery()) {
				if (rs.next()) {
					// mapper de RS al POJO
					resul.setIdUsuario(idUsuario);
					resul.setProductosTotal(rs.getInt("total"));
					resul.setProductosAprobados(rs.getInt("aprobado"));
					resul.setProductosPendientes(rs.getInt("pendiente"));
				}
			}

		} catch (Exception e) {
			LOG.error(e);
		}
		return resul;
	}

	private Producto mapper(ResultSet rs) throws SQLException {

		Producto p = new Producto();
		Categoria c = new Categoria();
		Usuario u = new Usuario();

		p.setId(rs.getInt("producto_id"));
		p.setNombre(rs.getString("producto_nombre"));
		p.setImagen(rs.getString("imagen"));
		p.setPrecio(rs.getFloat("precio"));

		c.setId(rs.getInt("categoria_id"));
		c.setNombre(rs.getString("categoria_nombre"));
		p.setCategoria(c);
		
		u.setId(rs.getInt("usuario_id"));
		u.setNombre(rs.getString("usuario_nombre"));
		p.setUsuario(u);
		
		return p;
	}

	

}
