/*
 * Copyright (c) 2010, 2026 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {type PnpmWorkspaceYaml} from '../../util/PnpmWorkspaceYaml.ts';
import {type PnpmDependencyMap} from '../../util/pnpm.ts';
import {isSnapshotVersion} from '../../util/PackageJson.ts';
import {DependencyMetaData} from './DependencyMetaData.ts';
import {addVersion, type PnpmDependencyGraph, PROJECT_DEPENDENCY_FIELDS} from './PnpmDependencyGraph.ts';

/**
 * Regex to decide whether a declared version specifier is a fix version (e.g. `1.2.3` or `=1.2.3-beta`) and not a range.
 */
const FIX_VERSION_REGEX = /^=?\d+\.\d+\.\d+(-.*)?$/;
const NPM_ALIAS_PREFIX = 'npm:';

/**
 * This class may be used to compute overrides for a given `pnpm-workspace.yaml`.
 */
export class OverridesComputer {

  /**
   * Parsed `pnpm-workspace.yaml`.
   */
  pnpmWorkspaceYaml: PnpmWorkspaceYaml;
  /**
   * Dependency graph of all projects of the pnpm workspace (may contain more projects than {@link pnpmWorkspaceYaml}).
   */
  dependencyGraph: PnpmDependencyGraph;
  /**
   * `true` to collect the versions of all packages of {@link dependencyGraph} in {@link _versionsByName}, not only the ones of the visited packages, e.g. the
   * dependencies of a peer dependency installed by pnpm itself (see {@link _visitDependencies}). For the `pnpm-workspace.yaml` installing the whole graph, an
   * override with a parent applies to these packages as well, so the parent needs a version if one of them is another version of the parent.
   */
  countVersionsOfAllPackages: boolean;

  protected _dependencyDeclarationsByVersion = new Map<string /* dependency alias */, Map<string /* dependency version */, Map<string /* parent DependencyMetaData.id() */, DependencyDeclaration>>>();
  /**
   * Aliases of dependencies declared as peer dependency by at least one package (see {@link _registerDependencyDeclaration}).
   */
  protected _peerDependencyAliases = new Set<string>();
  /**
   * Versions of all visited packages and projects by package name (not alias), or of all packages (see {@link countVersionsOfAllPackages}). Links are not included.
   */
  protected _versionsByName = new Map<string /* package name */, Set<string>>();

  constructor(pnpmWorkspaceYaml: PnpmWorkspaceYaml, dependencyGraph: PnpmDependencyGraph, countVersionsOfAllPackages = false) {
    this.pnpmWorkspaceYaml = pnpmWorkspaceYaml;
    this.dependencyGraph = dependencyGraph;
    this.countVersionsOfAllPackages = countVersionsOfAllPackages;
  }

  /**
   * Aliases of the dependencies declared as peer dependency by at least one installed package. Available after {@link compute}.
   */
  get peerDependencyAliases(): ReadonlySet<string> {
    return this._peerDependencyAliases;
  }

  /**
   * Computes overrides from all packages in {@link pnpmWorkspaceYaml}.
   */
  async compute(): Promise<Record<string, string>> {
    // visit all workspace packages and collect all non workspace dependencies
    await this._visitProjects(new Set(await this.pnpmWorkspaceYaml.getPackages()));
    if (this.countVersionsOfAllPackages) {
      for (const [name, versions] of this.dependencyGraph.getVersionsByName()) {
        versions.forEach(version => addVersion(this._versionsByName, name, version));
      }
    }

    // build overrides
    return this._buildOverrides();
  }

  /**
   * Visits the dependencies of all projects in {@link dependencyGraph} that are part of the given workspacePackages.
   * @param workspacePackages the absolute paths of the packages of {@link pnpmWorkspaceYaml}.
   */
  protected async _visitProjects(workspacePackages: Set<string>): Promise<void> {
    const visitedPaths = new Set<string>();
    for (const project of this.dependencyGraph.projects) {
      if (!workspacePackages.has(project.path)) {
        // pnpmList contains all dependencies of all modules. In case pnpm-workspace.yaml files are nested, only projects part of this workspace should be visited
        continue;
      }
      const parent = new DependencyMetaData({name: project.name, alias: project.name, version: project.version, path: project.path});
      addVersion(this._versionsByName, parent.name, parent.version);
      for (const field of PROJECT_DEPENDENCY_FIELDS) {
        await this._visitDependencies(workspacePackages, parent, project[field], visitedPaths);
      }
    }
  }

  /**
   * Registers the given dependencies of the given parent (see {@link _registerDependencyDeclaration}) and visits their dependencies recursively.
   * Linked projects that are part of the given workspacePackages are skipped, as they are visited as project. The dependencies of each package are visited only once.
   *
   * The dependencies of a peer dependency are not visited: they are visited from the package providing it. If no package of this workspace provides it, pnpm took
   * it from another project (e.g. an optional peer dependency) or installed it itself. Then its dependencies may not be installed when installing this workspace
   * on its own (e.g. a nested `pnpm-workspace.yaml`), so no overrides are created for them.
   */
  protected async _visitDependencies(workspacePackages: Set<string>, parent: DependencyMetaData, dependencies: PnpmDependencyMap, visitedPaths: Set<string>): Promise<void> {
    for (const [alias, pnpmDependency] of Object.entries(dependencies ?? {})) {
      const dependency = new DependencyMetaData({name: pnpmDependency.from, alias, version: pnpmDependency.version, path: pnpmDependency.path});
      if (dependency.version.startsWith('link:') && workspacePackages.has(dependency.path)) {
        continue;
      }
      addVersion(this._versionsByName, dependency.name, dependency.version);
      const isPeerDependency = await this._registerDependencyDeclaration(parent, dependency);
      if (isPeerDependency || visitedPaths.has(dependency.path)) {
        continue;
      }
      visitedPaths.add(dependency.path);
      await this._visitDependencies(workspacePackages, dependency, this._getDependenciesToVisit(dependency), visitedPaths);
    }
  }

  /**
   * @returns `true` if the given dependency is declared as peer dependency (only) by the given parent. The dependencies of a project are never peer dependencies:
   *   they are installed as regular dependencies (including devDependencies also declared as peer).
   */
  protected async _isPeerDependency(parent: DependencyMetaData, dependency: DependencyMetaData): Promise<boolean> {
    if (this.dependencyGraph.isProject(parent.path)) {
      return false;
    }
    return !!(await this.dependencyGraph.getDeclaredDependencies(parent.path))?.peerOnly.has(dependency.alias);
  }

  /**
   * @returns the dependencies of the given dependency that are visited.
   */
  protected _getDependenciesToVisit(dependency: DependencyMetaData): PnpmDependencyMap {
    // A dependency to a project of the pnpm workspace that is not part of the processed pnpm-workspace.yaml (e.g. a project of another repository) is installed
    // as package when only the processed pnpm-workspace.yaml is installed. pnpm never installs the devDependencies of a package, but `pnpm list` expands such a
    // linked project including its devDependencies. Therefore, only its dependencies and optionalDependencies are visited, as otherwise overrides would be created
    // for packages not installed and the version counts of the installed ones would be distorted.
    if (this.dependencyGraph.isProject(dependency.path)) {
      return this.dependencyGraph.getProjectRuntimeDependencies(dependency.path);
    }
    return this.dependencyGraph.getDependencies(dependency.path);
  }

  /**
   * Registers a dependency declaration for the given parent.
   * @returns `true` if the given dependency is a peer dependency of the given parent (no declaration is registered, see {@link _isPeerDependency}).
   */
  protected async _registerDependencyDeclaration(parent: DependencyMetaData, dependency: DependencyMetaData): Promise<boolean> {
    // A peer dependency is not resolved by the package declaring it but provided by the package using it, so the same package version may get different peer versions
    // in different places (pnpm then installs it multiple times). An override always applies to all of them, as pnpm also applies overrides to the peerDependencies:
    // it would replace the peer range with the overridden version and cause unmet peer dependencies wherever another version is provided.
    // Therefore, no declaration is registered for a peer dependency. Its version is pinned by the regular dependency of the package providing it.
    // Projects are excluded: their dependencies (including devDependencies also declared as peer) are installed as regular dependencies.
    if (await this._isPeerDependency(parent, dependency)) {
      this._peerDependencyAliases.add(dependency.alias);
      return true;
    }
    const declaredDependencies = await this.dependencyGraph.getDeclaredDependencies(parent.path);

    // ensure version declarations for dependency
    let declarationsByVersion = this._dependencyDeclarationsByVersion.get(dependency.alias);
    if (!declarationsByVersion) {
      declarationsByVersion = new Map();
      this._dependencyDeclarationsByVersion.set(dependency.alias, declarationsByVersion);
    }

    // ensure declarations for resolved version
    const version = this._computeOverrideVersion(dependency);
    let declarations = declarationsByVersion.get(version);
    if (!declarations) {
      declarations = new Map();
      declarationsByVersion.set(version, declarations);
    }

    // register parent as declaration
    declarations.set(parent.id, {parent, fix: this._isFixVersion(declaredDependencies?.specifiers[dependency.alias])});
    return false;
  }

  /**
   * @returns the version to use in the overrides for the given dependency. For a dependency declared using a npm alias (e.g. `"foo": "npm:bar@^1.0.0"`) this is a npm alias with the
   *   resolved version (e.g. `npm:bar@1.2.3`).
   */
  protected _computeOverrideVersion(dependency: DependencyMetaData): string {
    if (dependency.name === dependency.alias || dependency.version.startsWith('link:')) {
      return dependency.version;
    }
    return `${NPM_ALIAS_PREFIX}${dependency.name}@${dependency.version}`;
  }

  /**
   * @param specifier the declared version specifier of a dependency (e.g. `1.2.3`, `^1.2.3` or `npm:bar@1.2.3`).
   * @returns `true` if the given specifier is a fix version and not a range.
   */
  protected _isFixVersion(specifier: string): boolean {
    if (specifier?.startsWith(NPM_ALIAS_PREFIX)) {
      specifier = specifier.substring(specifier.lastIndexOf('@') + 1);
    }
    return FIX_VERSION_REGEX.test(specifier);
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

      // An override without parent also replaces the range of every peer dependency on this dependency (pnpm applies overrides to the peerDependencies too).
      // The version of the override would contradict the peer version provided in places using another version, causing unmet peer dependencies. Therefore, a dependency
      // used as peer dependency gets overrides with parent only, for all of its versions. This also applies to a single version: otherwise, the overrides would switch
      // to overrides with parent as soon as a second version is installed, and a peer dependency resolved against the replaced range would change on the next install.
      // Example: acorn-jsx declares the peer dependency 'acorn: ^6.0.0 || ^7.0.0 || ^8.0.0' and is used by
      // - espree@7.3.1 (providing acorn@7.4.1) and
      // - espree@10.4.0 (providing acorn@8.19.0).
      // The override 'acorn@8.19.0: 8.19.0' would change the peer range of acorn-jsx to '8.19.0', which is unmet below espree@7.3.1.
      // Instead, the overrides 'espree@7.3.1>acorn@7.4.1: 7.4.1' and 'espree@10.4.0>acorn@8.19.0: 8.19.0' are created.
      const isPeerDependency = this._peerDependencyAliases.has(depAlias);

      // most used version of a dependency -> use override without parent
      const [mostUsedVersion, mostUsedDeclarations] = versionsSorted[0];
      let allowSkipFixed = true;
      // add version without parent if possible
      if (!isPeerDependency && this._isOverrideVersionAllowed(mostUsedVersion) && !allFixed(mostUsedDeclarations)) {
        result.set(this._computeOverrideTarget(depAlias, mostUsedVersion), mostUsedVersion);
        // an override without version selector (npm alias) also applies to the fixed declarations of other versions, so they need to be added to overrides as well
        allowSkipFixed = !mostUsedVersion.startsWith(NPM_ALIAS_PREFIX);
      }

      // less used versions (and all versions of a peer dependency) -> use override with parent
      for (let i = isPeerDependency ? 0 : 1; i < versionsSorted.length; i++) {
        const [version, declarations] = versionsSorted[i];
        // add overrides for all versions and parents
        if (this._isOverrideVersionAllowed(version)) {
          for (const declaration of declarations.values()) {
            this._addOverride(result, depAlias, version, declaration, allowSkipFixed);
          }
        }
      }
    }

    // sort alphabetically by alias, independent of the locale of the system
    return Object.fromEntries([...result].sort(([alias1], [alias2]) => alias1.localeCompare(alias2, 'en')));
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
      return version1.localeCompare(version2, 'en');
    };
    return [...versions.entries()].sort(versionDeclarationComparator);
  }

  /**
   * Checks whether an override is allowed for the given version.
   * It is allowed if the version is not a link (i.e. starts with 'link:') and not a snapshot version.
   */
  protected _isOverrideVersionAllowed(version: string): boolean {
    return !version.startsWith('link:') && !isSnapshotVersion(version);
  }

  /**
   * Adds an override for the given alias and version to the given {@link Map}.
   * Skips fix versions if skip is allowed.
   */
  protected _addOverride(overrides: Map<string, string>, depAlias: string, depVersion: string, declaration: DependencyDeclaration, allowSkipFixed: boolean) {
    // nothing to fix and skip allowed, or a project without name that cannot be selected as parent
    if ((declaration.fix && allowSkipFixed) || !declaration.parent.name) {
      return;
    }
    overrides.set(`${this._toParentSelector(declaration.parent)}>${this._computeOverrideTarget(depAlias, depVersion)}`, depVersion);
  }

  /**
   * @returns the parent selector of an override for the given parent: its name, including its version if the package occurs in multiple versions, as pnpm matches
   *   the parent by its package name (not alias).
   */
  protected _toParentSelector(parent: DependencyMetaData): string {
    const addParentVersion = this._versionsByName.get(parent.name)?.size > 1 && !!parent.version && this._isOverrideVersionAllowed(parent.version);
    return parent.name + (addParentVersion ? `@${parent.version}` : '');
  }

  /**
   * @returns the target selector of the override for the given dependency and version: `<dependency>@<version>`, e.g. `lodash@4.17.21`.
   *
   * pnpm applies an override with a version selector only to dependencies whose declared range includes this version. An override pins the version where it is
   * compatible, but does not force it on packages requiring another version. E.g. when a dependency is updated to a version requiring `ms: ^2.1.1`, the override
   * `ms@2.0.0: 2.0.0` does not apply to it, while the override `ms: 2.0.0` would force the incompatible version 2.0.0. The same applies to dependencies whose
   * range was changed in a `package.json`: they are resolved anew if their range no longer includes the pinned version.
   *
   * Overrides for npm aliases (the version is `npm:<name>@<version>`) have no version selector, as pnpm does not match a version selector with a dependency declared as npm alias.
   */
  protected _computeOverrideTarget(depAlias: string, version: string): string {
    return version.startsWith(NPM_ALIAS_PREFIX) ? depAlias : `${depAlias}@${version}`;
  }

  /**
   * Logs non unique packages (see {@link DependencyConvergenceLogLevel}). Available after {@link compute}.
   */
  logNonUniqueDependencyVersions(convergenceLogLevel?: DependencyConvergenceLogLevel) {
    // nothing to log
    if (convergenceLogLevel === dependencyConvergenceLogLevel.NONE) {
      return;
    }

    // check if at least one declaration comes from an external package
    const isOutsideWorkspace = (declarations: Map<string, DependencyDeclaration>) => [...declarations.values()]
      .some(declarations => declarations.parent.path.includes('.pnpm'));

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

export type DependencyDeclaration = { parent: DependencyMetaData; fix: boolean };


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
   * Like {@link OWN}, and additionally dependencies for which a single pnpm-workspace-external dependency exists. This may be handy to eliminate duplicate dependencies by
   * adapting the workspace to the single external one.
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
