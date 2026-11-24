/*
 * Copyright (c) 2010, 2026 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {readFile} from 'node:fs/promises';
import path from 'node:path';
import {type Dependencies, type PackageJson} from '../../util/PackageJson.ts';
import {type PnpmDependencyMap, type PnpmListOutput, type PnpmProject} from '../../util/pnpm.ts';

/**
 * Dependency fields of a {@link PnpmProject} that are visited.
 */
export const PROJECT_DEPENDENCY_FIELDS = ['dependencies', 'devDependencies', 'optionalDependencies'] as const satisfies readonly (keyof PnpmProject)[];

/**
 * Dependencies declared in a `package.json`.
 */
export type DeclaredDependencies = {
  /**
   * Declared version specifier by dependency alias of all dependencies, devDependencies, optionalDependencies and peerDependencies.
   */
  specifiers: Dependencies;
  /**
   * Aliases declared in the peerDependencies or peerDependenciesMeta (optional peer dependency) only, i.e. not in the dependencies or optionalDependencies.
   */
  peerOnly: Set<string>;
};

/**
 * Dependency graph of all projects of a pnpm workspace based on the output of `pnpm list --recursive --json`.
 * Provides the resolved dependencies of every installed package and the dependencies declared in its `package.json`. Both are computed once and may be shared.
 */
export class PnpmDependencyGraph {

  /**
   * Output of `pnpm list --recursive --json`.
   */
  projects: PnpmListOutput;

  protected _dependenciesByPath = new Map<string /* package directory */, PnpmDependencyMap>();
  protected _versionsByName = new Map<string /* package name */, Set<string>>();
  protected _projectsByPath: Map<string /* project directory */, PnpmProject>;
  protected _declaredDependenciesByDir = new Map<string /* package directory */, Promise<DeclaredDependencies>>();

  constructor(projects: PnpmListOutput) {
    this.projects = projects;
    this._index(projects);
    this._projectsByPath = new Map(projects.map(project => [project.path, project]));
  }

  /**
   * @returns `true` if the given directory is a project of the workspace and not an installed package.
   */
  isProject(packageDirectory: string): boolean {
    return this._projectsByPath.has(packageDirectory);
  }

  /**
   * @returns the resolved dependencies and optionalDependencies (without devDependencies) of the project in the given directory.
   */
  getProjectRuntimeDependencies(projectDirectory: string): PnpmDependencyMap {
    const project = this._projectsByPath.get(projectDirectory);
    return {...project?.dependencies, ...project?.optionalDependencies};
  }

  /**
   * @returns the resolved dependencies of the installed package in the given directory.
   */
  getDependencies(packageDirectory: string): PnpmDependencyMap {
    return this._dependenciesByPath.get(packageDirectory);
  }

  /**
   * @returns the versions of all projects and installed packages by package name (not alias). Links are not included.
   */
  getVersionsByName(): ReadonlyMap<string, ReadonlySet<string>> {
    return this._versionsByName;
  }

  /**
   * Gets the dependencies declared in the `package.json` in the given directory (see {@link _readDeclaredDependencies}). The result is cached per directory.
   */
  getDeclaredDependencies(packageJsonDirectory: string): Promise<DeclaredDependencies> {
    let declaredDependencies = this._declaredDependenciesByDir.get(packageJsonDirectory);
    if (!declaredDependencies) {
      declaredDependencies = this._readDeclaredDependencies(packageJsonDirectory);
      this._declaredDependenciesByDir.set(packageJsonDirectory, declaredDependencies);
    }
    return declaredDependencies;
  }

  /**
   * Collects the dependencies of every package in the given projects by the absolute path of the package in {@link _dependenciesByPath}, and the versions of all
   * projects and packages in {@link _versionsByName}.
   * `pnpm list` lists the dependencies of a package only once and marks all other occurrences as deduped. This index resolves the dependencies of deduped packages.
   */
  protected _index(projects: PnpmListOutput) {
    const index = (dependencies: PnpmDependencyMap) => {
      for (const dependency of Object.values(dependencies ?? {})) {
        addVersion(this._versionsByName, dependency.from, dependency.version);
        if (dependency.dependencies) {
          if (!this._dependenciesByPath.has(dependency.path)) {
            this._dependenciesByPath.set(dependency.path, dependency.dependencies);
          }
          index(dependency.dependencies);
        }
      }
    };
    for (const project of projects) {
      addVersion(this._versionsByName, project.name, project.version);
      for (const field of PROJECT_DEPENDENCY_FIELDS) {
        index(project[field]);
      }
    }
  }

  /**
   * Reads the dependencies declared in the `package.json` in the given directory.
   * @returns the declared dependencies or `null` if there is no `package.json`.
   */
  protected async _readDeclaredDependencies(packageJsonDirectory: string): Promise<DeclaredDependencies> {
    let packageJson: PackageJson;
    try {
      packageJson = JSON.parse(await readFile(path.join(packageJsonDirectory, 'package.json'), 'utf8'));
    } catch (error) {
      if (error.code === 'ENOENT') {
        return null;
      }
      throw error;
    }
    const peerOnly = new Set([...Object.keys(packageJson.peerDependencies ?? {}), ...Object.keys(packageJson.peerDependenciesMeta ?? {})]
      .filter(alias => !packageJson.dependencies?.[alias] && !packageJson.optionalDependencies?.[alias]));
    return {
      // same priority as pnpm (peerDependencies < devDependencies < dependencies < optionalDependencies)
      specifiers: {
        ...packageJson.peerDependencies,
        ...packageJson.devDependencies,
        ...packageJson.dependencies,
        ...packageJson.optionalDependencies
      },
      peerOnly
    };
  }
}

/**
 * Adds the given version of the package with the given name to the given versions by name. A missing name or version and a link (`link:...`) are ignored.
 */
export function addVersion(versionsByName: Map<string, Set<string>>, name: string, version: string) {
  if (!name || !version || version.startsWith('link:')) {
    return;
  }
  let versions = versionsByName.get(name);
  if (!versions) {
    versions = new Set();
    versionsByName.set(name, versions);
  }
  versions.add(version);
}
