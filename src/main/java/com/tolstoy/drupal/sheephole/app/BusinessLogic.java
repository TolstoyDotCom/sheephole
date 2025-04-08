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

import java.io.File;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Properties;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.dizitart.jbus.JBus;
import org.dizitart.jbus.Subscribe;
import org.semver4j.Semver;
import org.json.JSONArray;
import org.json.JSONObject;

import com.tolstoy.basic.api.storage.IStorage;
import com.tolstoy.basic.api.utils.IResourceBundleWithFormatting;
import com.tolstoy.basic.app.storage.StorageEmbeddedDerby;
import com.tolstoy.basic.app.utils.Utils;
import com.tolstoy.basic.app.utils.ResourceBundleWithFormatting;
import com.tolstoy.drupal.sheephole.api.IProfileManager;
import com.tolstoy.drupal.sheephole.api.installation.IAppDirectories;
import com.tolstoy.drupal.sheephole.api.installation.ISiteProfile;
import com.tolstoy.drupal.sheephole.api.installation.IInstallable;
import com.tolstoy.drupal.sheephole.api.installation.IInstallableVersion;
import com.tolstoy.drupal.sheephole.api.installation.IInstallationInstruction;
import com.tolstoy.drupal.sheephole.api.installation.InstallationInstructionType;
import com.tolstoy.drupal.sheephole.api.installation.IJsonUtils;
import com.tolstoy.drupal.sheephole.api.installation.IOperationResult;
import com.tolstoy.drupal.sheephole.api.installation.OperationResultType;
import com.tolstoy.drupal.sheephole.api.installation.PlatformType;
import com.tolstoy.drupal.sheephole.api.installation.ProjectType;
import com.tolstoy.drupal.sheephole.api.preferences.IPreferences;
import com.tolstoy.drupal.sheephole.api.preferences.IPreferencesFactory;
import com.tolstoy.drupal.sheephole.app.preferences.PreferencesFactory;
import com.tolstoy.drupal.sheephole.app.installation.AppDirectories;
import com.tolstoy.drupal.sheephole.app.installation.BasicInstallableVersion;
import com.tolstoy.drupal.sheephole.app.installation.Installable;
import com.tolstoy.drupal.sheephole.app.installation.InstallationInstruction;
import com.tolstoy.drupal.sheephole.app.installation.JsonUtils;
import com.tolstoy.drupal.sheephole.app.installation.OperationResult;
import com.tolstoy.drupal.sheephole.app.installation.SiteProfile;

public class BusinessLogic {
	private static final Logger logger = LogManager.getLogger( BusinessLogic.class );
	private static final String CACHED_MODULES_D10 = "drupal_modules_d10_feb25.json";
	private static final String CACHED_MODULES_D11 = "drupal_modules_d11_feb25.json";

	private final JBus jbus;
	private final IStorage storage;
	private final IPreferences prefs;
	private final IAppDirectories appDirectories;
	private final IResourceBundleWithFormatting bundle;
	private final IProfileManager profileManager;
	private final ISSHManager sshManager;
	private final List<IInstallable> installables10;
	private final List<IInstallable> installables11;

	private static final String[] TABLE_NAMES = { "preferences" };

	public BusinessLogic( JBus jbus ) throws Exception {
		this.jbus = jbus;
		this.jbus.registerWeak( this );

		this.installables10 = parseCachedModules( CACHED_MODULES_D10, new Semver( "10.0.0" ) );
		this.installables11 = parseCachedModules( CACHED_MODULES_D11, new Semver( "11.0.0" ) );

		Properties props = null;
		Map<String,String> defaultAppPrefs = null;
		IPreferencesFactory prefsFactory = null;
		String databaseConnectionString = null;

		IResourceBundleWithFormatting tempBundle = null;
		IStorage tempStorage = null;
		IPreferences tempPrefs = null;
		IAppDirectories tempAppDirectories = null;
		IProfileManager tempProfileManager = null;
		ISSHManager tempSSHManager = null;

		try {
			props = new Properties();
			props.load( getClass().getClassLoader().getResourceAsStream( "app.properties" ) );
			defaultAppPrefs = new HashMap<String,String>();
			for ( final Object key : props.keySet() ) {
				final String keyString = String.valueOf( key );
				defaultAppPrefs.put( keyString, String.valueOf( props.getProperty( keyString ) ) );
			}

			tempBundle = new ResourceBundleWithFormatting( "GUI" );
			if ( tempBundle == null ) {
				throw new RuntimeException( "cannot load bundle" );
			}
		}
		catch ( final Exception e ) {
			handleError( true, "Could not initialize properties", e );
		}

		this.bundle = tempBundle;

		try {
			tempAppDirectories = new AppDirectories( defaultAppPrefs.get( "storage.derby.dir_name" ),
													defaultAppPrefs.get( "storage.derby.db_name" ),
													defaultAppPrefs.get( "reports.dir_name" ) );

			databaseConnectionString = defaultAppPrefs.get( "storage.derby.connstring.start" ) +
										tempAppDirectories.getDatabaseDirectory() +
										defaultAppPrefs.get( "storage.derby.connstring.end" );
		}
		catch ( final Exception e ) {
			handleError( true, this.bundle.getString( "exc_install_loc" ), e );
		}

		this.appDirectories = tempAppDirectories;

		try {
			tempStorage = new StorageEmbeddedDerby( databaseConnectionString, Arrays.asList( TABLE_NAMES ) );

			tempStorage.connect();
			tempStorage.ensureTables();
		}
		catch ( final Exception e ) {
			handleError( true, this.bundle.getString( "exc_db_init", databaseConnectionString ), e );
		}

		this.storage = tempStorage;

		try {
			tempSSHManager = new SSHManager();

			tempProfileManager = new CachingProfileManager( new ProfileManager( tempStorage, tempSSHManager ) );
		}
		catch ( final Exception e ) {
			handleError( true, this.bundle.getString( "exc_profilemgr_init" ), e );
		}

		this.sshManager = tempSSHManager;
		this.profileManager = tempProfileManager;

		try {
			prefsFactory = new PreferencesFactory( this.storage, defaultAppPrefs );
			tempPrefs = prefsFactory.getAppPreferences();

			logger.info( "prefs before override=" + Utils.prettyPrintMap( "", Utils.sanitizeMap( tempPrefs.getValues() ) ) );
		}
		catch ( final Exception e ) {
			handleError( true, this.bundle.getString( "exc_prefs_init" ), e );
		}

		this.prefs = tempPrefs;
	}

	public IOperationResult createProfile( String title, String userName, String password, String uri, String directory ) {
		ISiteProfile profile = null;

		try {
			profile = profileManager.createProfile( title, userName, password, uri, directory );
		}
		catch ( Exception e ) {
			logger.catching( e );
			return new OperationResult( OperationResultType.FAILURE, e.getMessage() );
		}

		return new OperationResult( OperationResultType.SUCCESS, profile );
	}

	public IOperationResult loadProfileById( long id ) {
		List<ISiteProfile> profiles = null;

		try {
			profiles = profileManager.getProfiles();

			for ( ISiteProfile profile : profiles ) {
				if ( profile.getId() == id ) {
					return new OperationResult( OperationResultType.SUCCESS, profile );
				}
			}
		}
		catch ( Exception e ) {
			logger.catching( e );
			return new OperationResult( OperationResultType.FAILURE, e.getMessage() );
		}

		return new OperationResult( OperationResultType.BAD_ARGUMENTS );
	}

	public IOperationResult getProfiles() {
		List<ISiteProfile> profiles = null;

		try {
			profiles = profileManager.getProfiles();
		}
		catch ( Exception e ) {
			logger.catching( e );
			return new OperationResult( OperationResultType.FAILURE, e.getMessage() );
		}

		return new OperationResult( OperationResultType.SUCCESS, profiles );
	}

	public IOperationResult saveProfiles( List<ISiteProfile> list ) {
		try {
			profileManager.saveProfiles( list );
		}
		catch ( Exception e ) {
			logger.catching( e );
			return new OperationResult( OperationResultType.FAILURE, e.getMessage() );
		}

		return new OperationResult( OperationResultType.SUCCESS, list );
	}

	public IOperationResult deleteProfiles( List<ISiteProfile> list ) {
		try {
			profileManager.deleteProfiles( list );
		}
		catch ( Exception e ) {
			logger.catching( e );
			return new OperationResult( OperationResultType.FAILURE, e.getMessage() );
		}

		return new OperationResult( OperationResultType.SUCCESS );
	}

	public List<IInstallable> getInstallables( PlatformType type, Semver version ) {
		if ( type != PlatformType.DRUPAL ) {
			throw new IllegalArgumentException( "Unknown platform type: " + type );
		}

		if ( version.getMajor() == 10 ) {
			return installables10;
		}

		return installables11;
	}

	public List<IInstallable> getInstallables( PlatformType platformType, ProjectType projectType, String identifier ) {
		List<IInstallable> ret = new ArrayList<IInstallable>();

		for ( IInstallable installable : installables10 ) {
			if ( installable.getMachineName().equals( identifier ) ) {
				ret.add( installable );
			}
		}

		for ( IInstallable installable : installables11 ) {
			if ( installable.getMachineName().equals( identifier ) ) {
				ret.add( installable );
			}
		}

		return ret;
	}

	public IOperationResult installInstallable( IInstallable installable, ISiteProfile profile, String password ) {
		try {
			for ( IInstallationInstruction instruction : installable.getInstallationInstructions() ) {
				if ( instruction.getType() == InstallationInstructionType.COMPOSER_NAMESPACE ) {
					sshManager.composerInstall( profile, password, instruction.getCommand() );
				}
			}
		}
		catch ( Exception e ) {
			logger.catching( e );
			return new OperationResult( OperationResultType.FAILURE, e.getMessage() );
		}

		return new OperationResult( OperationResultType.SUCCESS );
	}

	public IOperationResult composerUpdate( ISiteProfile profile, String password ) {
		try {
			sshManager.composerUpdate( profile, password );
		}
		catch ( Exception e ) {
			logger.catching( e );
			return new OperationResult( OperationResultType.FAILURE, e.getMessage() );
		}

		return new OperationResult( OperationResultType.SUCCESS );
	}

	protected List<IInstallable> parseCachedModules( String resourcePath, Semver semver ) throws Exception {
		List<IInstallable> ret = new ArrayList<IInstallable>( 10000 );

		IJsonUtils jsonUtils = new JsonUtils();
		String json = IOUtils.toString( getClass().getResource( "/" + resourcePath ), StandardCharsets.UTF_8 );
		JSONObject root = new JSONObject( json );
		for ( Object tempObj : root.getJSONArray( "data" ) ) {
			ret.add( new Installable( (JSONObject) tempObj, PlatformType.DRUPAL, new BasicInstallableVersion( semver ), jsonUtils ) );
		}

		return ret;
	}

	protected void handleError( final boolean closeOnExit, final String msg, final Exception e ) throws Exception {
		logger.error( msg, e );
		throw e;
	}

	@Subscribe
	private void listen( InstallationRequestEvent event ) {
		List<IInstallable> installables = getInstallables( event.getPlatformType(), event.getProjectType(), event.getIdentifier() );
		jbus.post( new InstallInstallablesEvent( installables ) );
	}
}
