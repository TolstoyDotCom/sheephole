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

import java.util.List;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.tolstoy.drupal.sheephole.api.installation.IInstallable;

public class InstallInstallablesEvent {
	private static final Logger logger = LogManager.getLogger( InstallInstallablesEvent.class );

	private final List<IInstallable> installables;

	public InstallInstallablesEvent( List<IInstallable> installables ) {
		this.installables = installables;
	}

	public List<IInstallable> getInstallables() {
		return installables;
	}

	@Override
	public String toString() {
		return installables.toString();
	}
}
