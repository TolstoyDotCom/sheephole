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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.json.JSONArray;
import org.json.JSONObject;
import org.jsoup.Jsoup;

import com.tolstoy.basic.app.utils.Utils;
import com.tolstoy.drupal.sheephole.api.installation.IInstallable;
import com.tolstoy.drupal.sheephole.api.installation.IInstallableVersion;
import com.tolstoy.drupal.sheephole.api.installation.IInstallationInstruction;
import com.tolstoy.drupal.sheephole.api.installation.InstallationInstructionType;
import com.tolstoy.drupal.sheephole.api.installation.IJsonUtils;
import com.tolstoy.drupal.sheephole.api.installation.PlatformType;

public class Installable implements IInstallable {
	private static final Logger logger = LogManager.getLogger( Installable.class );
	private static final List<String> REQUIRED_JSON_KEYS = Arrays.asList( "body", "title",
																			"drupal_internal__nid", "field_project_machine_name",
																			"field_active_installs_total", "field_security_advisory_coverage" );

	private final String title;
	private final String machineName;
	private final String description;
	private final String link;
	private final PlatformType type;
	private final IInstallableVersion installableVersion;
	private final List<IInstallationInstruction> installationInstructions;
	private final Map<String,String> extraData;

	public Installable( JSONObject obj, PlatformType type, IInstallableVersion installableVersion, IJsonUtils jsonUtils ) throws Exception {
		JSONObject attributes = (JSONObject) obj.getJSONObject( "attributes" );

		for ( String key : REQUIRED_JSON_KEYS ) {
			if ( !attributes.has( key ) ) {
				throw new IllegalArgumentException( "missing key '" + key + "' from " + attributes );
			}
		}

		String tempDesc = "";

		Object tempObj = attributes.opt( "body" );
		if ( tempObj instanceof String ) {
			tempDesc = (String) tempObj;
		}
		else if ( tempObj instanceof JSONObject ) {
			Object tempObj2 = ( (JSONObject) tempObj ).opt( "value" );
			if ( tempObj2 instanceof String ) {
				tempDesc = (String) tempObj2;
			}
		}

		this.title = jsonUtils.getJSONValue( attributes, "title", String.class );
		this.link = "https://www.drupal.org/node/" + jsonUtils.getJSONValue( attributes, "drupal_internal__nid", Integer.class );
		this.machineName = jsonUtils.getJSONValue( attributes, "field_project_machine_name", String.class );
		this.description = tempDesc;
		this.type = type;
		this.installableVersion = installableVersion;
		this.installationInstructions = new ArrayList<IInstallationInstruction>();
		this.installationInstructions.add( new InstallationInstruction( InstallationInstructionType.COMPOSER_NAMESPACE, jsonUtils.getJSONValue( attributes, "field_composer_namespace", String.class ) ) );

		this.extraData = new HashMap<String,String>();
		this.extraData.put( "installs_total", "" + jsonUtils.getJSONValue( attributes, "field_active_installs_total", Integer.class, 0 ) );
		this.extraData.put( "security_coverage", jsonUtils.getJSONValue( attributes, "field_security_advisory_coverage", String.class, "" ) );
	}

	public Installable( String title,
						String link,
						String machineName,
						String description,
						PlatformType type,
						IInstallableVersion installableVersion,
						List<IInstallationInstruction> installationInstructions ) {
		this.title = title;
		this.link = link;
		this.machineName = machineName;
		this.description = description;
		this.type = type;
		this.installableVersion = null;
		this.installationInstructions = installationInstructions;
		this.extraData = new HashMap<String,String>();
	}

	@Override
	public String getTitle() {
		return title;
	}

	@Override
	public String getMachineName() {
		return machineName;
	}

	@Override
	public String getDescription() {
		return description;
	}

	@Override
	public String getPlainDescription( int maxLen ) {
		if ( description == null || description.length() < 2 ) {
			return description;
		}

		String ret = Jsoup.parse( description ).text();
		ret = Utils.replaceAllEmojis( ret );

		return ret.length() > maxLen ? ret.substring( 0, maxLen ) : ret;
	}

	@Override
	public PlatformType getType() {
		return type;
	}

	@Override
	public IInstallableVersion getInstallableVersion() {
		return installableVersion;
	}

	@Override
	public List<IInstallationInstruction> getInstallationInstructions() {
		return installationInstructions;
	}

	@Override
 	public boolean isMatchFor( String substring ) {
		return title.toLowerCase().contains( substring ) || machineName.toLowerCase().contains( substring );
	}

	@Override
	public String getExtraValue( String key ) {
		return extraData.get( key );
	}

	@Override
	public void setExtraData( String key, String value ) {
		extraData.put( key, value );
	}

	@Override
	public String getSummary() {
		return new ToStringBuilder( this )
		.append( "title", title )
		.append( "machineName", machineName )
		.append( "description", getPlainDescription( 40 ) )
		.append( "link", link )
		.append( "type", type )
		.append( "installableVersion", installableVersion )
		.append( "installationInstructions", installationInstructions )
		.append( "extraData", extraData )
		.toString();
	}

	@Override
	public String toString() {
		return title;
	}
}
