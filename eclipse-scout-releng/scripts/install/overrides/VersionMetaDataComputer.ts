/*
 * Copyright (c) 2010, 2026 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {readPackageJsonFromDir} from '@pnpm/read-package-json';
import files from '../../util/files.ts';

const FIX_SEMVER_VERSION = /^=?\d+\.\d+\.\d+(-.*)?$/;
const NPM_ALIAS_PREFIX = 'npm:';
const VERSION_DELIMITER = '@';

/**
 * This class may be used to resolve meta information about dependency declarations, e.g. if a dependency was declared using a npm alias (see {@link compute}).
 */
export class VersionMetaDataComputer {

  protected _cache = new Map<string, Record<string, string>>();

  /**
   * Resolves information about the given dependency and its version declaration in the given `package.json`.
   * @param packageJsonDirectory Absolute path to the directory containing the `package.json` declaring the dependency.
   * @param depAlias Alias (the left-hand-side part of the dependency declaration) of the dependency for which the metadata should be computed.
   * @param depVersion The version currently in use for the dependency.
   */
  async compute(packageJsonDirectory: string, depAlias: string, depVersion: string): Promise<VersionMetaData> {
    // read dependencies of the package.json
    const deps = await this._getDependencies(packageJsonDirectory);

    // get declared version specifier and split it into name and version
    const specifier = deps?.[depAlias];
    const {name, version} = this._splitNpmAliasSpecifier(specifier);

    // check whether the declared version is a fix one or a range
    const fix = FIX_SEMVER_VERSION.test(version);

    if (name) {
      // the dependency was declared as npm alias: also return a npm alias but with the fixed version now.
      return {
        version: NPM_ALIAS_PREFIX + name + VERSION_DELIMITER + depVersion,
        fix
      };
    }

    return {version: depVersion, fix};
  }

  /**
   * Splits a npm alias specifier into the declared name and version of the dependency.
   * If e.g. a dependency is declared as `"foo": "npm:bar@^42.13.7"` the specifier is split into `{name: "bar", version "^42.13.7"}`.
   */
  protected _splitNpmAliasSpecifier(specifier: string): { name?: string; version: string } {
    // not a npm alias specifier -> simply return specifier as version
    if (!specifier?.startsWith(NPM_ALIAS_PREFIX)) {
      return {name: null, version: specifier};
    }

    // cut off npm alias prefix
    specifier = specifier.substring(NPM_ALIAS_PREFIX.length);

    // cut off namespace marker so it does not interfere with the version delimiter
    const namespaceMarker = '@';
    const hasNamespaceMaker = specifier.startsWith(namespaceMarker);
    const withoutNamespaceMarker = hasNamespaceMaker ? specifier.substring(namespaceMarker.length) : specifier;

    // find version delimiter
    const versionDelimPos = withoutNamespaceMarker.lastIndexOf(VERSION_DELIMITER);
    const hasVersion = versionDelimPos > 0;

    // split into name and version
    const name = (hasNamespaceMaker ? namespaceMarker : '') + (hasVersion ? withoutNamespaceMarker.substring(0, versionDelimPos) : withoutNamespaceMarker);
    const version = hasVersion ? withoutNamespaceMarker.substring(versionDelimPos + 1) : null;

    return {name, version};
  }

  /**
   * Gets all dependencies for the `package.json` in the given directory, i.e. dependencies, devDependencies, optionalDependencies and peerDependencies.
   * The returned object contains all of these dependencies and their declared versions.
   */
  protected async _getDependencies(packageJsonDirectory: string): Promise<Record<string, string>> {
    let deps = this._cache.get(packageJsonDirectory);

    // read dependencies from package.json if not in cache already
    if (deps === undefined) {
      deps = await this._readDependencies(packageJsonDirectory);
      this._cache.set(packageJsonDirectory, deps);
    }

    return deps;
  }

  /**
   * Reads all dependencies from the `package.json` in the given directory, i.e. dependencies, devDependencies, optionalDependencies and peerDependencies.
   * The returned object contains all of these dependencies and their declared versions.
   */
  protected async _readDependencies(packageJsonDirectory: string): Promise<Record<string, string>> {
    // check if package.json exists
    if (!packageJsonDirectory || !(await files.exists(packageJsonDirectory))) {
      return null;
    }

    // read package.json and collect dependencies using the same priority as pnpm (peerDependencies < devDependencies < dependencies < optionalDependencies)
    const pckJson = await readPackageJsonFromDir(packageJsonDirectory);
    return {
      ...pckJson.peerDependencies,
      ...pckJson.devDependencies,
      ...pckJson.dependencies,
      ...pckJson.optionalDependencies
    };
  }
}

export type VersionMetaData = {
  /**
   * The version to use in the overrides.
   * If the dependency was declared using a npm alias the version returned contains the complete fixed npm alias.
   */
  version: string;

  /**
   * Specifies whether the dependency was declared using a fix version or a SemVer range.
   */
  fix: boolean;
};
