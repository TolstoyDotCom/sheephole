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
package com.tolstoy.drupal.sheephole.app.installation;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.LongProperty;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleLongProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.SimpleBooleanProperty;
import org.semver4j.Semver;
import com.tolstoy.drupal.sheephole.api.installation.ISiteProfile;
import com.tolstoy.drupal.sheephole.api.installation.PlatformType;

public class SiteProfile implements ISiteProfile {
	private final LongProperty id;
	private final IntegerProperty uid;
	private final StringProperty title;
	private final StringProperty userName;
	private final StringProperty uri;
	private final StringProperty directory;
	private final BooleanProperty toBeDeleted;
	private final LongProperty created;
	private final LongProperty modified;
	private final PlatformType platformType;
	private Semver platformVersion;
	private String password;

	public SiteProfile( String title, String userName, String uri, String directory ) {
		this.id = new SimpleLongProperty( 0 );
		this.uid = new SimpleIntegerProperty( 1 );
		this.title = new SimpleStringProperty( title );
		this.userName = new SimpleStringProperty( userName );
		this.uri = new SimpleStringProperty( uri );
		this.directory = new SimpleStringProperty( directory );
		this.toBeDeleted = new SimpleBooleanProperty();

		long ts = System.currentTimeMillis() / 1000;
		this.created = new SimpleLongProperty( ts );
		this.modified = new SimpleLongProperty( ts );
		this.platformType = PlatformType.DRUPAL;
		this.platformVersion = new Semver( "11.0.0" );
	}

	public SiteProfile( long id, String title, String userName, String uri, String directory ) {
		this.id = new SimpleLongProperty( id );
		this.uid = new SimpleIntegerProperty( 1 );
		this.title = new SimpleStringProperty( title );
		this.userName = new SimpleStringProperty( userName );
		this.uri = new SimpleStringProperty( uri );
		this.directory = new SimpleStringProperty( directory );
		this.toBeDeleted = new SimpleBooleanProperty();

		long ts = System.currentTimeMillis() / 1000;
		this.created = new SimpleLongProperty( ts );
		this.modified = new SimpleLongProperty( ts );
		this.platformType = PlatformType.DRUPAL;
		this.platformVersion = new Semver( "11.0.0" );
	}

	public SiteProfile( long id, String title, String userName, String uri, String directory, int uid, long created, long modified ) {
		this.id = new SimpleLongProperty( id );
		this.title = new SimpleStringProperty( title );
		this.userName = new SimpleStringProperty( userName );
		this.uri = new SimpleStringProperty( uri );
		this.directory = new SimpleStringProperty( directory );
		this.toBeDeleted = new SimpleBooleanProperty();
		this.uid = new SimpleIntegerProperty( uid );
		this.created = new SimpleLongProperty( created );
		this.modified = new SimpleLongProperty( modified );
		this.platformType = PlatformType.DRUPAL;
		this.platformVersion = new Semver( "11.0.0" );
	}

	public SiteProfile( long id, String title, String userName, String uri, String directory, int uid, long created, long modified, String versionString ) {
		this.id = new SimpleLongProperty( id );
		this.title = new SimpleStringProperty( title );
		this.userName = new SimpleStringProperty( userName );
		this.uri = new SimpleStringProperty( uri );
		this.directory = new SimpleStringProperty( directory );
		this.toBeDeleted = new SimpleBooleanProperty();
		this.uid = new SimpleIntegerProperty( uid );
		this.created = new SimpleLongProperty( created );
		this.modified = new SimpleLongProperty( modified );
		this.platformType = PlatformType.DRUPAL;
		this.platformVersion = new Semver( versionString );
	}

	@Override
	public boolean isMatchFor( String substring ) {
		return getTitle().toLowerCase().contains( substring );
	}

	@Override
	public long getId() {
		return id.get();
	}

	@Override
	public void setId( long id ) {
		this.id.set( id );
	}

	public LongProperty getIdProperty() {
		return id;
	}

	@Override
	public int getUid() {
		return uid.get();
	}

	@Override
	public void setUid( int uid ) {
		this.uid.set( uid );
	}

	public IntegerProperty getUidProperty() {
		return uid;
	}

	@Override
	public String getTitle() {
		return title.get();
	}

	@Override
	public void setTitle( String title ) {
		this.title.set( title );
	}

	public StringProperty getTitleProperty() {
		return title;
	}

	@Override
	public String getUserName() {
		return userName.get();
	}

	@Override
	public void setUserName( String userName ) {
		this.userName.set( userName );
	}

	public StringProperty getUserNameProperty() {
		return userName;
	}

	@Override
	public String getUri() {
		return uri.get();
	}

	@Override
	public void setUri( String uri ) {
		this.uri.set( uri );
	}

	public StringProperty getUriProperty() {
		return uri;
	}

	@Override
	public String getDirectory() {
		return directory.get();
	}

	@Override
	public void setDirectory( String directory ) {
		this.directory.set( directory );
	}

	public StringProperty getDirectoryProperty() {
		return directory;
	}

	@Override
	public Boolean getToBeDeleted() {
		return toBeDeleted.get();
	}

	@Override
	public void setToBeDeleted( Boolean toBeDeleted ) {
		this.toBeDeleted.set( toBeDeleted );
	}

	public BooleanProperty getToBeDeletedProperty() {
		return toBeDeleted;
	}

	@Override
	public long getCreated() {
		return created.get();
	}

	@Override
	public void setCreated( long created ) {
		this.created.set( created );
	}

	public LongProperty getCreatedProperty() {
		return created;
	}

	@Override
	public long getModified() {
		return modified.get();
	}

	@Override
	public void setModified( long modified ) {
		this.modified.set( modified );
	}

	public LongProperty getModifiedProperty() {
		return modified;
	}

	@Override
	public PlatformType getPlatformType() {
		return platformType;
	}

	@Override
	public Semver getVersion() {
		return platformVersion;
	}

	@Override
	public void setVersion( Semver version ) {
		platformVersion = version;
	}

	@Override
	public void setVersion( String versionString ) throws Exception {
		platformVersion = new Semver( versionString );
	}

	@Override
	public String getPassword() {
		return password;
	}

	@Override
	public void setPassword( String password ) {
		this.password = password;
	}

	@Override
	public String toString() {
		return "{ title: " + getTitle() + ", userName: " + getUserName() + ", toBeDeleted: " + getToBeDeleted() + " }";
	}
}
