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
package com.tolstoy.drupal.sheephole.app.gui;

import java.io.File;
import java.util.EventObject;

public class RunItineraryEvent extends EventObject {
	/**
	 * 
	 */
	private static final long serialVersionUID = -7248172622748134409L;
	private final File itineraryFile;

	public RunItineraryEvent( final Object source, final File itineraryFile ) {
		super( source );
		this.itineraryFile = itineraryFile;
	}

	public File getItineraryFile() {
		return itineraryFile;
	}
}
