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

import javafx.application.Platform;
import javafx.scene.control.TextArea;
import org.apache.logging.log4j.core.*;
import org.apache.logging.log4j.core.appender.AbstractAppender;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.config.plugins.PluginAttribute;
import org.apache.logging.log4j.core.config.plugins.PluginElement;
import org.apache.logging.log4j.core.config.plugins.PluginFactory;
import org.apache.logging.log4j.core.layout.PatternLayout;

@Plugin(name = "TextAreaLogAppender", category = Core.CATEGORY_NAME, elementType = Appender.ELEMENT_TYPE)
public class TextAreaLogAppender extends AbstractAppender {
	private static TextArea textArea;

	protected TextAreaLogAppender( String name, Filter filter ) {
		super( name, filter, null );
	}

	public static void setTextArea( TextArea textArea ) {
		TextAreaLogAppender.textArea = textArea;
	}

	@PluginFactory
	public static TextAreaLogAppender createAppender( @PluginAttribute("name") String name, @PluginElement("Filter") Filter filter ) {
		return new TextAreaLogAppender( name, filter );
	}

	@Override
	public void append( LogEvent event ) {
		if ( textArea != null ) {
			Platform.runLater( () -> textArea.appendText( event.getMessage().getFormattedMessage() + "\n" ) );
		}
	}
}
