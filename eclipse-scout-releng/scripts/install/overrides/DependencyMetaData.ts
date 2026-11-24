/*
 * Copyright (c) 2010, 2026 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */

/**
 * This class contains information about a dependency.
 * This information may depend on the context.
 * E.g. if the package is used as a dependency which is declared using a npm alias, both alias and name are set.
 **/
export class DependencyMetaData {
  /**
   * Name of the dependency. Absent for a project without name.
   */
  name: string;
  /**
   * Alias the dependency was declared from (left-hand-side of the dependency declaration). The name for a project.
   */
  alias: string;
  /**
   * Version of the dependency. Absent for a project without version.
   */
  version: string;
  /**
   * Absolute path to the directory containing the `package.json` of the dependency.
   */
  path: string;

  constructor(model: { name?: string; alias?: string; version?: string; path: string }) {
    this.name = model.name;
    this.alias = model.alias;
    this.version = model.version;
    this.path = model.path;
  }

  /**
   * {@link name} and {@link version} combined (e.g. `foo@1.2.3`). The {@link path} replaces a missing name, a missing version is omitted.
   */
  get id(): string {
    const name = this.name ?? this.path;
    return this.version ? `${name}@${this.version}` : name;
  }
}
