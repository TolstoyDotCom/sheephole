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

public class InstallationInfo implements IInstallationInfo {
	private final String rootDirectory;
	private final String drupalPath;
	private final String versionString;

	public InstallationInfo( String rootDirectory, String drupalPath, String versionString ) {
		this.rootDirectory = rootDirectory;
		this.drupalPath = drupalPath;
		this.versionString = versionString;
	}

	@Override
	public String getRootDirectory() {
		return rootDirectory;
	}

	@Override
	public String getDrupalPath() {
		return drupalPath;
	}

	@Override
	public String getVersionString() {
		return versionString;
	}

	@Override
	public String toString() {
		return "[" + rootDirectory + ", " + versionString + "]";
	}
}
