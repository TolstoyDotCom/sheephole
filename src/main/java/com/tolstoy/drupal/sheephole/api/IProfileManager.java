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
package com.tolstoy.drupal.sheephole.api;

import java.util.List;

import com.tolstoy.drupal.sheephole.api.installation.ISiteProfile;

public interface IProfileManager {
	ISiteProfile createProfile( String title, String userName, String password, String uri, String directory ) throws Exception;

	ISiteProfile loadProfileById( long id ) throws Exception;

	List<ISiteProfile> getProfiles() throws Exception;

	void saveProfiles( List<ISiteProfile> list ) throws Exception;

	void deleteProfiles( List<ISiteProfile> list ) throws Exception;

	void saveProfile( ISiteProfile profile ) throws Exception;

	void deleteProfile( ISiteProfile profile ) throws Exception;
}
