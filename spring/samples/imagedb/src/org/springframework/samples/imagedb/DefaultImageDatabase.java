package org.springframework.samples.imagedb;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import javax.sql.DataSource;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.support.AbstractLobCreatingPreparedStatementCallback;
import org.springframework.jdbc.core.support.AbstractLobStreamingResultSetExtractor;
import org.springframework.jdbc.core.support.JdbcDaoSupport;
import org.springframework.jdbc.LobRetrievalFailureException;
import org.springframework.jdbc.object.MappingSqlQuery;
import org.springframework.jdbc.support.lob.LobCreator;
import org.springframework.jdbc.support.lob.LobHandler;
import org.springframework.util.FileCopyUtils;

/**
 * Default implementation of the central business interface.
 * Uses JDBC with a LobHandler to retrieve and store image data.
 * @author Juergen Hoeller
 * @since 07.01.2004
 */
public class DefaultImageDatabase extends JdbcDaoSupport implements ImageDatabase {

	private LobHandler lobHandler;

	private GetImagesQuery getImagesQuery;

	public void setLobHandler(LobHandler lobHandler) {
		this.lobHandler = lobHandler;
	}

	protected void initDao() throws Exception {
		this.getImagesQuery = new GetImagesQuery(getDataSource());
	}


	public List getImages() throws DataAccessException {
		return this.getImagesQuery.execute();
	}

	public void streamImage(final String name, final OutputStream os) throws DataAccessException {
		getJdbcTemplate().query(
				"SELECT content FROM imagedb WHERE image_name=?", new Object[] {name},
				new AbstractLobStreamingResultSetExtractor() {
					protected void handleNoRowFound() throws LobRetrievalFailureException {
						throw new LobRetrievalFailureException("Image with name '" + name + "' not found in database");
					}
					public void streamData(ResultSet rs) throws SQLException, IOException {
						FileCopyUtils.copy(lobHandler.getBlobAsBinaryStream(rs, 1), os);
					}
				}
		);
	}

	public void storeImage(final String name, final InputStream is, final int contentLength,
												 final String description) throws DataAccessException {
		getJdbcTemplate().execute(
				"INSERT INTO imagedb (image_name, content, description) VALUES (?, ?, ?)",
				new AbstractLobCreatingPreparedStatementCallback(this.lobHandler) {
					protected void setValues(PreparedStatement ps, LobCreator lobCreator) throws SQLException {
						ps.setString(1, name);
						lobCreator.setBlobAsBinaryStream(ps, 2, is, contentLength);
						lobCreator.setClobAsString(ps, 3, description);
					}
				}
		);
	}

	public void checkImages() {
		// could implement consistency check here
		logger.info("Checking images: not implemented but invoked by scheduling");
	}

	public void clearDatabase() {
		getJdbcTemplate().update("DELETE FROM imagedb");
	}


	protected class GetImagesQuery extends MappingSqlQuery {

		public GetImagesQuery(DataSource ds) {
			super(ds, "SELECT image_name, description FROM imagedb");
			compile();
		}

		protected Object mapRow(ResultSet rs, int rowNum) throws SQLException {
			String name = rs.getString(1);
			String description = lobHandler.getClobAsString(rs, 2);
			return new ImageDescriptor(name, description);
		}
	}

}
