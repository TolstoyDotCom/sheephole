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

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.tolstoy.drupal.sheephole.api.installation.PlatformType;
import com.tolstoy.drupal.sheephole.api.installation.ProjectType;

public class InstallationRequestEvent {
	private static final Logger logger = LogManager.getLogger( InstallationRequestEvent.class );

	private final PlatformType platformType;
	private final ProjectType projectType;
	private final String identifier;

	public InstallationRequestEvent( PlatformType platformType, ProjectType projectType, String identifier ) {
		this.platformType = platformType;
		this.projectType = projectType;
		this.identifier = identifier;
	}

	public PlatformType getPlatformType() {
		return platformType;
	}

	public ProjectType getProjectType() {
		return projectType;
	}

	public String getIdentifier() {
		return identifier;
	}

	@Override
	public String toString() {
		return new ToStringBuilder( this )
		.append( "platformType", platformType )
		.append( "projectType", projectType )
		.append( "identifier", identifier )
		.toString();
	}
}
