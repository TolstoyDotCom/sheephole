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
package com.tolstoy.drupal.sheephole.api.installation;

import javafx.beans.property.BooleanProperty;

import org.semver4j.Semver;

public interface ISiteProfile {

	boolean isMatchFor( String substring );

	long getId();

	void setId( long id );

	int getUid();

	void setUid( int uid );

	String getTitle();

	void setTitle( String title );

	String getUserName();

	void setUserName( String userName );

	String getUri();

	void setUri( String uri );

	String getDirectory();

	void setDirectory( String directory );

	Boolean getToBeDeleted();

	void setToBeDeleted( Boolean toBeDeleted );

	long getCreated();

	void setCreated( long created );

	long getModified();

	void setModified( long modified );

	PlatformType getPlatformType();

	Semver getVersion();

	void setVersion( Semver version );

	void setVersion( String versionString ) throws Exception;

	String getPassword();

	void setPassword( String password );

	BooleanProperty getToBeDeletedProperty();
}
