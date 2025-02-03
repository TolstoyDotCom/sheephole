/*
 * Copyright 2025 Chris Kelly
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package com.tolstoy.drupal.sheephole.app;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Properties;
import java.sql.Blob;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.Instant;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.dizitart.jbus.JBus;
import org.semver4j.Semver;

import com.tolstoy.basic.api.storage.IStorage;
import com.tolstoy.basic.app.utils.Utils;
import com.tolstoy.drupal.sheephole.api.installation.ISiteProfile;
import com.tolstoy.drupal.sheephole.app.installation.SiteProfile;

public class ProfileManager {
	private static final Logger logger = LogManager.getLogger( ProfileManager.class );

	private static final String TABLE_NAME = "site_profile";
	private static final int UID = 1;

	private final IStorage storage;
	private final ISSHManager sshManager;

	public ProfileManager( IStorage storage, ISSHManager sshManager ) throws Exception {
		this.storage = storage;
		this.sshManager = sshManager;
		createTableInternalIgnoreIfExists();
	}

	public ISiteProfile createProfile( String title, String userName, String password, String uri, String directory ) throws Exception {
		IInstallationInfo info = sshManager.getInstallationInfo( userName, password, uri, directory );

		long ts = System.currentTimeMillis() / 1000;

		ISiteProfile profile = new SiteProfile( 0, title, userName, uri, directory, UID, ts, ts, info.getVersionString() );

		saveProfiles( Arrays.asList( profile ) );

		profile.setPassword( password );

		return profile;
	}

	public ISiteProfile loadProfileById( long id ) throws Exception {
		Connection connection = null;
		PreparedStatement ps = null;
		ResultSet rs = null;

		try {
			connection = storage.getConnection();
			ps = connection.prepareStatement( "SELECT * FROM " + TABLE_NAME + " WHERE id=?" );
			ps.setLong( 1, id );

			rs = ps.executeQuery();

			while ( rs.next() ) {
				return readProfile( rs );
			}
		}
		finally {
			if ( rs != null ) {
				rs.close();
			}
			if ( ps != null ) {
				ps.close();
			}
			if ( connection != null ) {
				connection.close();
			}
		}

		return null;
	}

	public List<ISiteProfile> getProfiles() throws Exception {
		Connection connection = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		final List<ISiteProfile> ret = new ArrayList<ISiteProfile>();

		try {
			connection = storage.getConnection();
			ps = connection.prepareStatement( "SELECT * FROM " + TABLE_NAME );

			rs = ps.executeQuery();

			while ( rs.next() ) {
				ret.add( readProfile( rs ) );
			}
		}
		finally {
			if ( rs != null ) {
				rs.close();
			}
			if ( ps != null ) {
				ps.close();
			}
			if ( connection != null ) {
				connection.close();
			}
		}

		return ret;
	}

	public void saveProfiles( List<ISiteProfile> list ) throws Exception {
		for ( ISiteProfile profile : list ) {
			saveProfile( profile );
		}
	}

	public void deleteProfiles( List<ISiteProfile> list ) throws Exception {
		for ( ISiteProfile profile : list ) {
			deleteProfile( profile );
		}
	}

	public void saveProfile( ISiteProfile profile ) throws Exception {
		Connection connection = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		String query = null;

		try {
			connection = storage.getConnection();

			if ( profile.getId() == 0 ) {
				query = "INSERT INTO " + TABLE_NAME + "( uid, title, username, uri, directory, platform_type, version_string, created, modified ) VALUES( ?, ?, ?, ?, ?, ?, ?, ?, ? )";
				ps = connection.prepareStatement( query, Statement.RETURN_GENERATED_KEYS );
			}
			else {
				query = "UPDATE " + TABLE_NAME + " SET uid = ?, title = ?, username = ?, uri = ?, directory = ?, platform_type = ?, version_string = ?, created = ?, modified = ? WHERE id = ?";
				ps = connection.prepareStatement( query );
			}

			int ord = 1;
			ps.setInt( ord++, profile.getUid() );
			ps.setString( ord++, profile.getTitle() );
			ps.setString( ord++, profile.getUserName() );
			ps.setString( ord++, profile.getUri() );
			ps.setString( ord++, profile.getDirectory() );
			ps.setString( ord++, "" + profile.getPlatformType() );
			ps.setString( ord++, "" + profile.getVersion() );
			ps.setLong( ord++, profile.getCreated() );
			ps.setLong( ord++, profile.getModified() );

			if ( profile.getId() == 0 ) {
				logger.info( "about to insert to " + TABLE_NAME );

				ps.executeUpdate();

				rs = ps.getGeneratedKeys();

				long id = 0;
				if ( rs.next() ) {
					id = rs.getLong( 1 );
				}

				if ( id != 0 ) {
					profile.setId( id );
				}
				else {
					throw new RuntimeException( "cannot save profile " + profile );
				}
			}
			else {
				ps.setLong( ord++, profile.getId() );

				logger.info( "about to update " + profile.getId() + " in " + TABLE_NAME );

				ps.executeUpdate();
			}
		}
		finally {
			if ( rs != null ) {
				rs.close();
			}
			if ( ps != null ) {
				ps.close();
			}
			if ( connection != null ) {
				connection.close();
			}
		}
	}

	public void deleteProfile( ISiteProfile profile ) throws Exception {
		Connection connection = null;
		PreparedStatement ps = null;

		try {
			connection = storage.getConnection();
			ps = connection.prepareStatement( "DELETE FROM " + TABLE_NAME + " WHERE id=?" );
			ps.setLong( 1, profile.getId() );

			int rowCount = ps.executeUpdate();
		}
		finally {
			if ( ps != null ) {
				ps.close();
			}
			if ( connection != null ) {
				connection.close();
			}
		}
	}

	protected ISiteProfile readProfile( ResultSet rs ) throws Exception {
		return new SiteProfile(
					rs.getLong( "id" ),
					rs.getString( "title" ),
					rs.getString( "username" ),
					rs.getString( "uri" ),
					rs.getString( "directory" ),
					rs.getInt( "uid" ),
					rs.getLong( "created" ),
					rs.getLong( "modified" ),
					rs.getString( "version_string" ) );
	}

	protected void createTableInternalIgnoreIfExists() throws Exception {
		Connection connection = null;
		Statement stmt = null;

		final String definition = "CREATE TABLE " + TABLE_NAME + "( " +
							" id BIGINT NOT NULL GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1)," +
							" uid INT NOT NULL DEFAULT 0," +
							" title VARCHAR(255)," +
							" username VARCHAR(255)," +
							" uri VARCHAR(255)," +
							" directory VARCHAR(255)," +
							" platform_type VARCHAR(255)," +
							" version_string VARCHAR(255)," +
							" created BIGINT," +
							" modified BIGINT," +
							" extra BLOB(16M)," +
							" CONSTRAINT pk" + TABLE_NAME + " PRIMARY KEY (id) )";

		try {
			connection = storage.getConnection();
			stmt = connection.createStatement();
			stmt.executeUpdate( definition );
			logger.info( "created table " + TABLE_NAME );
		}
		catch ( final SQLException e ) {
			final String s = e.toString();
			if ( s.indexOf( "exists" ) < 0 ) {
				logger.error( "while creating table " + TABLE_NAME, e );
				throw e;
			}
		}
		finally {
			if ( stmt != null ) {
				stmt.close();
			}
			if ( connection != null ) {
				connection.close();
			}
		}
	}
}
