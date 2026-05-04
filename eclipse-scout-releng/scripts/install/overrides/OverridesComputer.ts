/*
 * Copyright (c) 2010, 2026 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import path from 'node:path';
import {visitDependenciesForPackages} from '../dependency/visitor/DependencyVisitor.ts';
import {type PnpmWorkspaceYaml} from '../../util/PnpmWorkspaceYaml.ts';
import {type DependencyMetaData} from '../dependency/visitor/DependencyMetaData.ts';
import {VersionMetaDataComputer} from './VersionMetaDataComputer.ts';

/**
 * Regex to decide whether a version is a snapshot version (e.g. `1.2.3-snapshot` or `1.2.3-snapshot.19700101000000`)
 */
const SNAPSHOT_REGEX = /-snapshot|-snapshot\.\d{14}$/i;

/**
 * This class may be used to compute overrides for a given `pnpm-workspace.yaml`.
 */
export class OverridesComputer {

  /**
   * Directory of the `pnpm-lock.yaml`.
   */
  lockfileDir: string;
  /**
   * Parsed `pnpm-workspace.yaml`.
   */
  pnpmWorkspaceYaml: PnpmWorkspaceYaml;

  protected _dependencyDeclarationsByVersion = new Map<string /* dependency alias */, Map<string /* dependency version */, Map<string /* parent DependencyMetaData.id() */, DependencyDeclaration>>>();
  protected _versionMetaDataComputer = new VersionMetaDataComputer();

  constructor(lockfileDir: string, pnpmWorkspaceYaml: PnpmWorkspaceYaml) {
    this.lockfileDir = lockfileDir;
    this.pnpmWorkspaceYaml = pnpmWorkspaceYaml;
  }

  /**
   * Computes overrides from all packages in {@link pnpmWorkspaceYaml}.
   */
  async compute(convergenceLogLevel?: DependencyConvergenceLogLevel): Promise<Record<string, string>> {
    // get all workspace packages
    const workspacePackages = this._computeWorkspacePackages();

    // visit all workspace packages and collect all non workspace dependencies
    await visitDependenciesForPackages(this.lockfileDir, workspacePackages, async (parent: DependencyMetaData, dependency: DependencyMetaData) => this._collectDependencies(workspacePackages, parent, dependency));

    // log non unique dependencies
    this._logNonUniqueDependencyVersions(convergenceLogLevel);

    // build overrides
    return this._buildOverrides();
  }

  /**
   * @returns an array of relative paths from {@link lockfileDir} of all packages in {@link pnpmWorkspaceYaml}.
   */
  protected _computeWorkspacePackages(): string[] {
    return this.pnpmWorkspaceYaml
      .getPackages()
      .map(p => path.relative(this.lockfileDir, p));
  }

  /**
   * Collects all non workspace dependencies (see {@link _registerDependencyDeclaration}) for the given workspacePackages.
   */
  protected async _collectDependencies(workspacePackages: string[], parent: DependencyMetaData, dependency: DependencyMetaData): Promise<boolean> {
    // skip subtree if package is part of pnpm-workspace.yaml as it will be visited anyway later on
    if (dependency.version.startsWith('link:')) {
      const isInOwnWorkspace = workspacePackages.some(wsp => dependency.path.endsWith(wsp));
      if (isInOwnWorkspace) {
        return false;
      }
    }

    // not a linked workspace dependency -> register declaration
    return await this._registerDependencyDeclaration(parent, dependency);
  }

  /**
   * Registers a dependency declaration for the given parent.
   * @returns `true` if the dependency was not registered already (new dependency).
   */
  protected async _registerDependencyDeclaration(parent: DependencyMetaData, dependency: DependencyMetaData): Promise<boolean> {
    // ensure version declarations for dependency
    let declarationsByVersion = this._dependencyDeclarationsByVersion.get(dependency.alias);
    if (!declarationsByVersion) {
      declarationsByVersion = new Map();
      this._dependencyDeclarationsByVersion.set(dependency.alias, declarationsByVersion);
    }

    // resolve version info
    const {version, fix} = await this._versionMetaDataComputer.compute(parent.path, dependency.alias, dependency.version);

    // flag whether the dependency was already registered
    let isNewDependency = false;

    // ensure declarations for resolved version
    let declarations = declarationsByVersion.get(version);
    if (!declarations) {
      declarations = new Map();
      declarationsByVersion.set(version, declarations);
      isNewDependency = true;
    }

    // register parent as declaration
    declarations.set(parent.id, {parent, fix});

    return isNewDependency;
  }

  /**
   * Builds overrides from {@link _dependencyDeclarationsByVersion}.
   */
  protected _buildOverrides(): Record<string, string> {
    const result = new Map<string, string>();

    // checks whether all declarations are fixed declarations
    const allFixed = (declarations: Map<string, DependencyDeclaration>) => [...declarations.values()]
      .every(declarations => declarations.fix);

    for (const [depAlias, versions] of this._dependencyDeclarationsByVersion.entries()) {
      const versionsSorted = this._getDependencyVersionsSorted(versions);

      // most used version of a dependency -> use override without parent
      const [mostUsedVersion, mostUsedDeclarations] = versionsSorted[0];
      let allowSkipFixed = true;
      // add version without parent if possible
      if (this._isOverrideVersionAllowed(mostUsedVersion) && !allFixed(mostUsedDeclarations)) {
        result.set(depAlias, mostUsedVersion);
        // even fixed version need to be added to overrides, as otherwise they are overridden by the recently added override without a parent
        allowSkipFixed = false;
      }

      // less used versions -> use override with parent
      for (let i = 1; i < versionsSorted.length; i++) {
        const [version, declarations] = versionsSorted[i];
        // add overrides for all versions and parents
        if (this._isOverrideVersionAllowed(version)) {
          for (const declaration of declarations.values()) {
            this._addOverride(result, depAlias, version, declaration, allowSkipFixed);
          }
        }
      }
    }

    // sort alphabetically by alias
    return Object.fromEntries([...result].sort(([alias1, version1], [alias2, version2]) => alias1.localeCompare(alias2)));
  }

  /**
   * Sort versions by declaration count (from high to low).
   */
  protected _getDependencyVersionsSorted(versions: Map<string, Map<string, DependencyDeclaration>>): [string, Map<string, DependencyDeclaration>][] {
    const versionDeclarationComparator = ([version1, declarations1], [version2, declarations2]) => {
      // compare declaration count
      const sizeDiff = declarations2.size - declarations1.size;
      if (sizeDiff) {
        return sizeDiff;
      }

      // ensure stable sort in case of same size (prevents flip-flop changes)
      return version1.localeCompare(version2);
    };
    return [...versions.entries()].sort(versionDeclarationComparator);
  }

  /**
   * Checks whether an override is allowed for the given version.
   * It is allowed if the version is not a link (i.e. starts with 'link:') and not a snapshot version.
   */
  protected _isOverrideVersionAllowed(version: string): boolean {
    return !version.startsWith('link:') && !SNAPSHOT_REGEX.test(version);
  }

  /**
   * Adds an override for the given alias and version to the given {@link Map}.
   * Skips fix versions if skip is allowed.
   */
  protected _addOverride(overrides: Map<string, string>, depAlias: string, depVersion: string, declaration: DependencyDeclaration, allowSkipFixed: boolean) {
    // nothing to fix and skip allowed
    if (declaration.fix && allowSkipFixed) {
      return;
    }
    // check whether the parent occurs in multiple versions and its version needs to be included
    const addParentVersion = this._dependencyDeclarationsByVersion.get(declaration.parent.name)?.size > 1 && this._isOverrideVersionAllowed(declaration.parent.version);
    const parentPart = declaration.parent.name + (addParentVersion ? `@${declaration.parent.version}` : '');

    // add override
    overrides.set(`${parentPart}>${depAlias}`, depVersion);
  }

  /**
   * Logs non unique packages (see {@link DependencyConvergenceLogLevel}).
   */
  protected _logNonUniqueDependencyVersions(convergenceLogLevel?: DependencyConvergenceLogLevel) {
    // nothing to log
    if (convergenceLogLevel === dependencyConvergenceLogLevel.NONE) {
      return;
    }
    convergenceLogLevel = convergenceLogLevel || DEFAULT_CONVERGENCE_LOG_LEVEL;

    // check if at least one declaration comes from an external package
    const isOutsideWorkspace = (declarations: Map<string, DependencyDeclaration>) => [...declarations.values()].some(declarations => declarations.parent.path.indexOf('.pnpm') >= 0);

    for (const [depAlias, versionsMap] of this._dependencyDeclarationsByVersion.entries()) {
      // version occurs only once -> nothing to log
      if (versionsMap.size <= 1) {
        continue;
      }

      // count versions from externals
      const versionsFromExternalsCount = [...versionsMap.values()].filter(isOutsideWorkspace).length;

      // log warning depending on requested convergence
      if (convergenceLogLevel === dependencyConvergenceLogLevel.ALL || (convergenceLogLevel === dependencyConvergenceLogLevel.SINGLE_EXTERNAL && versionsFromExternalsCount === 1) || versionsFromExternalsCount === 0) {
        const versionDeclarations = [...versionsMap.entries()]
          .map(([version, declarations]) => `${version}: [\n  ${[...declarations.keys()].sort().join(',\n  ')}\n]`)
          .join('\n');
        console.warn(`Dependency '${depAlias}' does not converge:\n${versionDeclarations}\n`);
      }
    }
  }
}

export type DependencyDeclaration = { parent?: DependencyMetaData; fix: boolean };

/**
 * @see dependencyConvergenceLogLevel
 */
export type DependencyConvergenceLogLevel = typeof dependencyConvergenceLogLevel[keyof typeof dependencyConvergenceLogLevel];

/**
 * Specifies which non-convergent dependencies are logged. For the default value see {@link DEFAULT_CONVERGENCE_LOG_LEVEL}.
 */
export const dependencyConvergenceLogLevel = {
  /**
   * All non-convergent dependencies are logged.
   */
  ALL: 'all',

  /**
   * Only dependencies for which a single pnpm-workspace-external dependency exists. This may be handy to eliminate duplicate dependencies by adapting the workspace to the single external one.
   */
  SINGLE_EXTERNAL: 'single-external',

  /**
   * Only non-convergent dependencies of the pnpm-workspace are logged. These may be misconfigured packages.
   */
  OWN: 'own',

  /**
   * Logging disabled.
   */
  NONE: 'none'
} as const;

/**
 * The default log level {@link dependencyConvergenceLogLevel.OWN}.
 */
export const DEFAULT_CONVERGENCE_LOG_LEVEL = dependencyConvergenceLogLevel.OWN;
