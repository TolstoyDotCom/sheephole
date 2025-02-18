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
import java.util.Set;
import java.util.HashSet;
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
import com.spaceprogram.kittycache.KittyCache;

import com.tolstoy.basic.api.storage.IStorage;
import com.tolstoy.basic.app.utils.Utils;
import com.tolstoy.drupal.sheephole.api.IProfileManager;
import com.tolstoy.drupal.sheephole.api.installation.ISiteProfile;
import com.tolstoy.drupal.sheephole.app.installation.SiteProfile;

public class CachingProfileManager implements IProfileManager {
	private static final Logger logger = LogManager.getLogger( CachingProfileManager.class );

	private static final int REFRESH_SECONDS = 3600;

	private final IProfileManager other;
	private final KittyCache<Long,ISiteProfile> individualCache;
	private final Set<Long> profileIds;

	public CachingProfileManager( IProfileManager other ) throws Exception {
		this.other = other;
		this.individualCache = new KittyCache<Long,ISiteProfile>( 100 );
		this.profileIds = new HashSet<Long>();
	}

	@Override
	public ISiteProfile createProfile( String title, String userName, String password, String uri, String directory ) throws Exception {
		ISiteProfile profile = other.createProfile( title, userName, password, uri, directory );
		long id = profile.getId();
		if ( id > 0 ) {
			individualCache.put( id, profile, REFRESH_SECONDS );
			profileIds.add( id );
		}

		return profile;
	}

	@Override
	public ISiteProfile loadProfileById( long id ) throws Exception {
		ISiteProfile profile;

		try {
			profile = individualCache.get( id );
			if ( profile != null ) {
				return profile;
			}
		}
		catch ( Exception e ) {
			//	it's just not there
		}

		profile = other.loadProfileById( id );
		if ( profile != null ) {
			individualCache.put( id, profile, REFRESH_SECONDS );
		}

		return profile;
	}

	@Override
	public List<ISiteProfile> getProfiles() throws Exception {
		if ( profileIds.isEmpty() ) {
			List<ISiteProfile> tempList = other.getProfiles();
			for ( ISiteProfile profile : tempList ) {
				profileIds.add( profile.getId() );
			}
		}
 
		List<ISiteProfile> ret = new ArrayList<ISiteProfile>();
		for ( Long id : profileIds ) {
			ret.add( loadProfileById( id ) );
		}

		return ret;
	}

	@Override
	public void saveProfiles( List<ISiteProfile> list ) throws Exception {
		for ( ISiteProfile profile : list ) {
			saveProfile( profile );
		}
	}

	@Override
	public void deleteProfiles( List<ISiteProfile> list ) throws Exception {
		for ( ISiteProfile profile : list ) {
			deleteProfile( profile );
		}
	}

	@Override
	public void saveProfile( ISiteProfile profile ) throws Exception {
		other.saveProfile( profile );
		long id = profile.getId();
		if ( id > 0 ) {
			individualCache.put( id, profile, REFRESH_SECONDS );
			profileIds.add( id );
		}
	}

	@Override
	public void deleteProfile( ISiteProfile profile ) throws Exception {
		long id = profile.getId();
		other.deleteProfile( profile );
		if ( id > 0 ) {
			individualCache.remove( id );
			profileIds.remove( id );
		}
	}
}
