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

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.NameValuePair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.dizitart.jbus.JBus;

import com.tolstoy.drupal.sheephole.api.installation.PlatformType;
import com.tolstoy.drupal.sheephole.api.installation.ProjectType;

public class BasicServer extends Thread {
	private static final Logger logger = LogManager.getLogger( BasicServer.class );

	private final JBus jbus;
	private final int port;

	public BasicServer( int port, JBus jbus ) {
		this.port = port;
		this.jbus = jbus;
	}

	public void run() {
		try {
			HttpServer server = HttpServer.create( new InetSocketAddress( port ), 0 );

			server.createContext( "/", new CommunicationHandler() );

			server.setExecutor( null );
			server.start();
		}
		catch ( Exception e ) {
			logger.catching( e );
		}
	}

	private final class CommunicationHandler implements HttpHandler {
		@Override
		public void handle( HttpExchange exchange ) throws IOException {
			URI uri = exchange.getRequestURI();
			if ( uri == null || uri.getPath().length() < 2 ) {
				send( exchange, "" );
				return;
			}

			if ( !isLocalAddress( exchange.getRemoteAddress().getHostName() ) ) {
				send( exchange, "" );
				return;
			}

			String path = StringUtils.remove( uri.getPath(), "/" );

			if ( "hurdy".equals( path ) ) {
				handleIsOnline( exchange, path, uri );
			}
			else if ( "install-module".equals( path ) ) {
				handleInstallModule( exchange, path, uri );
			}
			else {
				send( exchange, "" );
			}
		}

		private void handleIsOnline( HttpExchange exchange, String path, URI uri ) throws IOException {
			send( exchange, "gurdy" );
		}

		private void handleInstallModule( HttpExchange exchange, String path, URI uri ) throws IOException {
			String machineName = null;

			List<NameValuePair> params = URLEncodedUtils.parse( IOUtils.toString( exchange.getRequestBody(), StandardCharsets.UTF_8 ), StandardCharsets.UTF_8 );
			for ( NameValuePair param : params ) {
				if ( "machine_name".equals( param.getName() ) && param.getValue() != null && param.getValue().length() > 1 ) {
					machineName = param.getValue();
					break;
				}
			}

			if ( machineName == null || machineName.length() < 2 ) {
				send( exchange, "" );
			}

			logger.info( "Server got request to install module: " + machineName );

			send( exchange, "bombarde" );

			jbus.post( new InstallationRequestEvent( PlatformType.DRUPAL, ProjectType.EXTENSION, machineName ) );
		}

		private void send( HttpExchange exchange, String msg ) throws IOException {
			exchange.sendResponseHeaders( 200, msg.length() );
			OutputStream os = exchange.getResponseBody();
			os.write( msg.getBytes() );
			os.close();
		}

		private boolean isLocalAddress( String addr ) {
			return ( "127.0.0.1".equals( addr ) || "localhost".equals( addr ) );
		}
	}
}
