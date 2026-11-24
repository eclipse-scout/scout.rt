/*
 * Copyright (c) 2010, 2026 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */

import {PnpmWorkspaceYaml, WORKSPACE_MANIFEST_FILENAME} from './util/PnpmWorkspaceYaml.ts';
import {type Dependencies, isSnapshotRange, type PackageJson, removeSnapshotSuffix} from './util/PackageJson.ts';
import path from 'node:path';
import {promises as fs} from 'node:fs';
import files from './util/files.ts';

const DEPENDENCY_FIELDS = ['dependencies', 'devDependencies', 'peerDependencies', 'optionalDependencies'] as const satisfies readonly (keyof PackageJson)[];

/**
 * Generates a timestamp with the pattern yyyyMMddHHmmss
 */
function generateTimeStamp(): string {
  return new Date().toISOString().replace(/\D/g, '').slice(0, 14); // UTC without milliseconds
}

/**
 * Generates a snapshot-version of the current module version and the timestamp
 */
function generateSnapshotVersion(moduleVersion: string, timestamp: string, verbose: boolean): string {
  const cleanedVersion = removeSnapshotSuffix(moduleVersion);
  const newVersion = `${cleanedVersion}-snapshot.${timestamp}`;

  if (verbose) {
    console.log(`old version was: ${moduleVersion}`);
  }
  console.log(`new Version: ${newVersion}`);
  return newVersion;
}

/**
 * Searches the matching regex for the module name and returns the corresponding version.
 * @param newModuleVersion the new version to return if useRegexMap is false and updateWorkspaceDependencies is true
 */
function createReleaseVersionConstraint(moduleName: string, mapping: Mapping, newModuleVersion: string, useRegexMap: boolean, updateWorkspaceDependencies: boolean, verbose: boolean): string {
  // --mapping.0.regex @eclipse-scout --mapping.0.version 10.0.2 creates an object mapping = {0: {regex: @eclipse-scout; version: 10.0.2}}
  if (updateWorkspaceDependencies && !useRegexMap) {
    // all Modules in the repository/workspace get the same version
    return newModuleVersion;
  }
  if (mapping) {
    for (const entry of Object.values(mapping)) {
      if (!entry.regex || !entry.version) {
        throw new Error('Please provide arguments in the form of --mapping.0.regex and --mapping.0.version');
      }

      const regex = new RegExp(`^${entry.regex}`); // the module name should start with the regex
      if (regex.test(moduleName)) {
        if (verbose) {
          console.log(`new release constraint form: ${moduleName}: ${entry.version}`);
        }
        return entry.version;
      }
    }
  }
  throw new Error(`couldn't find a constraint for ${moduleName}! Please provide a matching regex`);
}

type DependencyUpdateOptions = { updateWorkspaceDependencies: boolean; mapping: Mapping; newVersion: string; useRegexMap: boolean; verbose: boolean };

/**
 * Replaces the snapshot ranges of the given dependencies with release versions: of the workspace modules if updateWorkspaceDependencies is `true`, of all other
 * modules otherwise (see {@link createReleaseVersionConstraint}).
 */
function updateDependencyConstraints(dependencies: Dependencies, workspaceModuleNames: Set<string>, {updateWorkspaceDependencies, mapping, newVersion, useRegexMap, verbose}: DependencyUpdateOptions) {
  for (const [moduleName, version] of Object.entries(dependencies ?? {})) {
    if (isSnapshotRange(version)) {
      if (updateWorkspaceDependencies === workspaceModuleNames.has(moduleName)) {
        const versionConstraint = createReleaseVersionConstraint(moduleName, mapping, newVersion, useRegexMap, updateWorkspaceDependencies, verbose);
        console.log(`dependency ${moduleName} with version ${version} needs to be updated. new constraint: ${versionConstraint}`);
        dependencies[moduleName] = versionConstraint;
      } else {
        if (verbose) {
          console.log(`dependency ${moduleName} with version ${version} is ${updateWorkspaceDependencies ? 'not ' : ''}in the workspace. no update`);
        }
      }
    } else {
      if (verbose) {
        console.log(`dependency ${moduleName} with version ${version} is not a snapshot. no update`);
      }
    }
  }
}

async function updateAllPackageJsons({
  isSnapshot = true, updateWorkspaceDependencies = false, releaseDependencyMapping = {}, timestamp, newVersion, useRegexMap = false, verbose = false, dryrun = false, workspaceRoot = null
}: {
  isSnapshot?: boolean; updateWorkspaceDependencies?: boolean; releaseDependencyMapping?: Mapping; timestamp?: string; newVersion?: string; useRegexMap?: boolean; verbose?: boolean; dryrun?: boolean; workspaceRoot?: string;
}): Promise<void> {
  const workspaceModules = await collectModulesInWorkspace(process.cwd(), workspaceRoot);
  if (!workspaceModules?.length) {
    console.log('no modules found');
    return;
  }

  const workspaceModuleNames = new Set(workspaceModules.map(module => module.manifest.name));
  for (const module of workspaceModules) {
    const packageJson = module.manifest;
    const original = JSON.stringify(packageJson);
    console.log(`updating version/dependency for module: ${packageJson.name}`);
    // update version of this module
    if (isSnapshot ? timestamp : newVersion || useRegexMap) {
      if (isSnapshot) {
        packageJson.version = generateSnapshotVersion(packageJson.version, timestamp, verbose);
      } else if (!useRegexMap) {
        packageJson.version = newVersion;
      } else {
        packageJson.version = createReleaseVersionConstraint(packageJson.name, releaseDependencyMapping, newVersion, useRegexMap, updateWorkspaceDependencies, verbose);
      }
      if (verbose) {
        console.log(`new version for module: ${packageJson.version}`);
      }
    }

    // update dependencies of this module (snapshot ranges are only replaced on release)
    if (!isSnapshot) {
      for (const field of DEPENDENCY_FIELDS) {
        updateDependencyConstraints(packageJson[field], workspaceModuleNames, {updateWorkspaceDependencies, mapping: releaseDependencyMapping, newVersion, useRegexMap, verbose});
      }
    }

    if (JSON.stringify(packageJson) === original) {
      console.log(`no changes for module: ${packageJson.name}`);
    } else if (!dryrun) {
      await fs.writeFile(path.join(module.rootDir, 'package.json'), JSON.stringify(packageJson, null, 2) + '\n', 'utf8');
    } else {
      console.log(JSON.stringify(packageJson, null, 2));
    }
  }
}

/**
 * A module (directory containing a `package.json`) of a workspace.
 */
export type WorkspaceModule = {
  /**
   * Absolute path of the module directory.
   */
  rootDir: string;
  manifest: PackageJson;
};

/**
 * Collects the workspace root module and all modules declared in its `pnpm-workspace.yaml` (if present).
 * The workspace root is determined using {@link ensureWorkspaceRoot}.
 */
export async function collectModulesInWorkspace(dir: string, workspaceRoot?: string): Promise<WorkspaceModule[]> {
  // ensure workspace root
  workspaceRoot = await ensureWorkspaceRoot(dir, workspaceRoot);

  const rootDirs = new Set<string>();
  if (await files.exists(path.resolve(workspaceRoot, 'package.json'))) {
    // real path like the packages, to detect the root also listed as package
    rootDirs.add(await fs.realpath(workspaceRoot));
  }
  if (await files.exists(path.resolve(workspaceRoot, WORKSPACE_MANIFEST_FILENAME))) {
    // add packages as declared in the pnpm-workspace.yaml.
    const workspaceYaml = await PnpmWorkspaceYaml.parse(workspaceRoot);
    for (const packageDir of await workspaceYaml.getPackages()) {
      rootDirs.add(packageDir);
    }
  }

  return await Promise.all([...rootDirs]
    .sort()
    .map(async rootDir => ({
      rootDir,
      manifest: JSON.parse(await fs.readFile(path.join(rootDir, 'package.json'), 'utf8')) as PackageJson
    })));
}

/**
 * Ensures the workspace root for the given directory.
 * If no workspace root is given it is determined from the given directory using {@link findWorkspaceFileDir}.
 */
export async function ensureWorkspaceRoot(dir: string, workspaceRoot?: string): Promise<string> {
  // workspace root given -> simply return
  if (workspaceRoot) {
    console.log(`use given workspace root: ${workspaceRoot}`);
    return workspaceRoot;
  }

  // no workspace root given -> try to find workspace file
  workspaceRoot = await PnpmWorkspaceYaml.findWorkspaceFileDir(dir);
  if (workspaceRoot) {
    console.log(`use workspace root found at: ${workspaceRoot}`);
    return workspaceRoot;
  }

  // parent folder as default if no workspace file could be found
  workspaceRoot = path.join(dir, '..');
  console.log(`unable to find workspace file. Use parent directory as workspace root: ${workspaceRoot}`);

  return workspaceRoot;
}

export async function snapshotDependencies({dryrun, workspaceRoot = null}) {
  await updateAllPackageJsons({isSnapshot: true, updateWorkspaceDependencies: false, dryrun, workspaceRoot});
}

export async function snapshot({verbose, dryrun, workspaceRoot = null}) {
  await updateAllPackageJsons({isSnapshot: true, updateWorkspaceDependencies: true, timestamp: generateTimeStamp(), verbose, dryrun, workspaceRoot});
}

export async function releaseDependencies({mapping, verbose, dryrun, workspaceRoot = null}) {
  await updateAllPackageJsons({isSnapshot: false, updateWorkspaceDependencies: false, releaseDependencyMapping: mapping, verbose, dryrun, workspaceRoot});
}

export async function release({mapping, newVersion, verbose, dryrun, useRegexMap, workspaceRoot = null}) {
  await updateAllPackageJsons({isSnapshot: false, updateWorkspaceDependencies: true, releaseDependencyMapping: mapping, newVersion, useRegexMap, verbose, dryrun, workspaceRoot});
}

export type Mapping = Record<number, {
  /**
   * the regex to test against the given moduleName
   */
  regex: string;

  /**
   * the new version to return if the regex matches
   */
  version: string;
}>;
