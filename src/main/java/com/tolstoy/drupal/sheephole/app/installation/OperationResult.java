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
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.commons.lang3.StringUtils;

import com.tolstoy.drupal.sheephole.api.installation.IOperationResult;
import com.tolstoy.drupal.sheephole.api.installation.OperationResultType;

public class OperationResult implements IOperationResult {
	private static final Logger logger = LogManager.getLogger( OperationResult.class );

	private OperationResultType type;
	private Object data;
	private final List<String> messages;

	public OperationResult() {
		this.type = OperationResultType.UNKNOWN;
		this.messages = new ArrayList<String>();
		this.data = null;
	}

	public OperationResult( OperationResultType type ) {
		this.type = type;
		this.messages = new ArrayList<String>();
		this.data = null;
	}

	public OperationResult( OperationResultType type, Object data ) {
		this.type = type;
		this.messages = new ArrayList<String>();
		this.data = ( data instanceof List ) ? new ArrayList<>( (List) data ) : data;
	}

	public OperationResult( OperationResultType type, List<String>messages ) {
		this.type = type;
		this.messages = messages;
	}

	public OperationResult( OperationResultType type, String message ) {
		this.type = type;
		this.messages = new ArrayList();
		this.messages.add( message );
	}

	public OperationResult( OperationResultType type, Object data, List<String>messages ) {
		this.type = type;
		this.data = ( data instanceof List ) ? new ArrayList<>( (List) data ) : data;
		this.messages = messages;
	}

	public OperationResultType getType() {
		return type;
	}

	public void setType( OperationResultType type ) {
		this.type = type;
	}

	public Object getData() {
		return data;
	}

	public void setData( Object data ) {
		this.data = data;
	}

	public List<String> getMessages() {
		return messages;
	}

	public void addMessage( String msg ) {
		messages.add( msg );
	}

	@Override
	public String toString() {
		String msgs = getMessages().size() > 0 ? " " + StringUtils.join( getMessages(), "; " ) : "";

		return "" + type + msgs;
	}
}
