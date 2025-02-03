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

import org.semver4j.Semver;
import com.tolstoy.drupal.sheephole.api.installation.IInstallationInstruction;
import com.tolstoy.drupal.sheephole.api.installation.InstallationInstructionType;

public class InstallationInstruction implements IInstallationInstruction {
	private final InstallationInstructionType type;
	private final String cmd;

	public InstallationInstruction( InstallationInstructionType type, String cmd ) {
		this.type = type;
		this.cmd = cmd;
	}

	@Override
	public InstallationInstructionType getType() {
		return type;
	}

	@Override
	public String getCommand() {
		return cmd;
	}

	@Override
	public String toString() {
		return "type=" + type + ", cmd=" + cmd;
	}
}
