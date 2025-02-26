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

import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.io.IOException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import net.schmizz.sshj.SSHClient;
import net.schmizz.sshj.common.IOUtils;
import net.schmizz.sshj.connection.channel.direct.Session;
import net.schmizz.sshj.connection.channel.direct.Session.Command;

import com.tolstoy.drupal.sheephole.api.installation.ISiteProfile;

public class SSHManager implements ISSHManager {
	private static final Logger logger = LogManager.getLogger( SSHManager.class );
	private static final Pattern PATTERN = Pattern.compile( "\\sconst\\sVERSION\\s=\\s(.*);" );

	public SSHManager() {
	}

	@Override
	public void composerInstall( ISiteProfile profile, String password, String composerNamespace ) throws Exception {
		SSHClient ssh = null;
		Session session = null;

		try {
			ssh = new SSHClient();

			ssh.loadKnownHosts();
			ssh.connect( profile.getUri() );
			String drupalPath = null;

			ssh.authPassword( profile.getUserName(), password );
			ssh.setTimeout( 5 );

			session = ssh.startSession();

			if ( !pathExists( ssh, profile.getDirectory() + "/composer.json", true ) ) {
				throw new RuntimeException( "composer.json does not exist in " + profile.getDirectory() );
			}

			String s = "cd " + escape( profile.getDirectory() ) + " && composer require " + escape( composerNamespace ) + " && echo 'flubr'";

			SSHResult res = runCommand( ssh, s );

			logger.info( "ran command=" + s + ", res=" + res );

			if ( !res.getResult().contains( "flubr" ) ) {
				throw new RuntimeException( "Command failed. Cmd=" + s + ", res=" + res );
			}
		}
		finally {
			try {
				if ( ssh != null ) {
					ssh.disconnect();
				}
			}
			catch ( Exception e ) {
			}
		}
	}

	@Override
	public IInstallationInfo getInstallationInfo( String userName, String password, String uri, String directory ) throws Exception {
		SSHClient ssh = null;

		try {
			ssh = new SSHClient();

			ssh.loadKnownHosts();
			ssh.connect( uri );
			String drupalPath = null;

			ssh.authPassword( userName, password );

			if ( !pathExists( ssh, directory, false ) ) {
				throw new RuntimeException( "Root path does not exist: " + directory );
			}

			if ( !pathExists( ssh, directory + "/composer.json", true ) ) {
				throw new RuntimeException( "composer.json does not exist in " + directory );
			}

			drupalPath = directory + "/web/core/lib/Drupal.php";
			if ( !pathExists( ssh, drupalPath, true ) ) {
				drupalPath = directory + "/core/lib/Drupal.php";
				if ( !pathExists( ssh, drupalPath, true ) ) {
					throw new RuntimeException( "Drupal.php does not exist at " + drupalPath );
				}
			}

			String drupalContents = readFile( ssh, drupalPath ).getResult();

			Matcher matcher = PATTERN.matcher( drupalContents );
			if ( !matcher.find() ) {
				throw new RuntimeException( "Cannot get VERSION from Drupal.php at " + drupalPath );
			}

			String versionString = matcher.group( 1 );
			versionString = versionString.replace( "\"", "" ).replace( "'", "" );

			return new InstallationInfo( directory, drupalPath, versionString );
		}
		finally {
			try {
				if ( ssh != null ) {
					ssh.disconnect();
				}
			}
			catch ( Exception e ) {
			}
		}
	}

	protected SSHResult readFile( SSHClient ssh, String path ) throws Exception {
		String escapedPath = escape( path );
		Session session = null;

		try {
			session = ssh.startSession();
			String s = "cat " + escapedPath;
			Command cmd = session.exec( s );
			String result = IOUtils.readFully( cmd.getInputStream() ).toString().trim();
			cmd.join( 5, TimeUnit.SECONDS );
			int status = cmd.getExitStatus();

			return new SSHResult( status, result.trim() );
		}
		finally {
			try {
				if ( session != null ) {
					session.close();
				}
			}
			catch ( IOException e ) {
			}
		}
	}

	protected SSHResult runCommand( SSHClient ssh, String escapedCommand ) throws Exception {
		Session session = null;

		try {
			session = ssh.startSession();
			Command cmd = session.exec( escapedCommand );
			String result = IOUtils.readFully( cmd.getInputStream() ).toString().trim();
			cmd.join( 5, TimeUnit.SECONDS );
			int status = cmd.getExitStatus();

			return new SSHResult( status, result.trim() );
		}
		finally {
			try {
				if ( session != null ) {
					session.close();
				}
			}
			catch ( IOException e ) {
			}
		}
	}

	protected boolean pathExists( SSHClient ssh, String path, boolean isFile ) throws Exception {
		String escapedPath = escape( path );
		Session session = null;

		try {
			session = ssh.startSession();
			String test = isFile ? "-f" : "-d";
			String s = "[ " + test + " " + escapedPath + " ] && " + "echo 'flibbity' || echo 'zurbness'";
			Command cmd = session.exec( s );
			String result = IOUtils.readFully( cmd.getInputStream() ).toString().trim();
			cmd.join( 5, TimeUnit.SECONDS );
			int status = cmd.getExitStatus();
			logger.info( "s=" + s + ", status=" + status + ", result=" + result + "#####" );

			return result.contains( "flibbity" );
		}
		finally {
			try {
				if ( session != null ) {
					session.close();
				}
			}
			catch ( IOException e ) {
			}
		}
	}

	protected String escape( String s ) {
		return "'" + s.replace( "'", "'\\''" ) + "'";
	}

	private static class SSHResult {
		private final String result;
		private final int status;

		SSHResult( int status, String result ) {
			this.status = status;
			this.result = result != null ? result : "";
		}

		int getStatus() {
			return status;
		}

		String getResult() {
			return result;
		}

		@Override
		public String toString() {
			return "status=" + status + ", result=" + result;
		}
	}
}
