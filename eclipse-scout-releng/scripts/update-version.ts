/*
 * Copyright (c) 2010, 2026 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */

// Makes the script crash on unhandled rejections instead of silently
// ignoring them. In the future, promise rejections that are not handled will
// terminate the Node.js process with a non-zero exit code.
import {PnpmWorkspaceYaml} from './util/PnpmWorkspaceYaml.ts';
import {type Dependencies} from '@pnpm/types';
import {findWorkspacePackagesNoCheck, type Project} from '@pnpm/workspace.find-packages';
import path from 'node:path';
import {promises as fs} from 'node:fs';
import files from './util/files.ts';
import {WORKSPACE_MANIFEST_FILENAME} from '@pnpm/constants';

process.on('unhandledRejection', err => {
  throw err;
});

/**
 * Generates a timestamp with the pattern yyyyMMddHHmmss
 */
function generateTimeStamp(): string {
  let timestamp = new Date().toISOString(); // UTC
  timestamp = timestamp.replace(/[-:.TZ]/g, '');
  return timestamp.substring(0, timestamp.length - 3); // remove milliseconds
}

/**
 * Generates a snapshot-version of the current module version and the timestamp
 */
function generateSnapshotVersion(moduleVersion: string, timestamp: string, verbose: boolean): string {
  const cleanedVersion = moduleVersion.replace(/-snapshot(.)*/i, '');
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
function createReleaseVersionConstraint(moduleName: string, mapping: Mapping, newModuleVersion: string, useRegexMap = false, updateWorkspaceDependencies: boolean, verbose: boolean): string {
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

function updateDependencyConstraints(dependencies: Dependencies, workspaceModuleNames: string[] = [], updateWorkspaceDependencies: boolean, isSnapshot: boolean,
  mapping: Mapping, newModuleVersion: string, useRegexMap: boolean, verbose: boolean) {
  if (!dependencies || isSnapshot) {
    return;
  }

  const regex = new RegExp('-snapshot\\s*<');
  for (const [moduleName, version] of Object.entries(dependencies)) {
    if (regex.test(version)) {
      if ((updateWorkspaceDependencies && workspaceModuleNames.includes(moduleName)) || (!updateWorkspaceDependencies && !workspaceModuleNames.includes(moduleName))) {
        const versionConstraint = createReleaseVersionConstraint(moduleName, mapping, newModuleVersion, useRegexMap, updateWorkspaceDependencies, verbose);
        console.log(`dependency ${moduleName} with version ${version} needs to be updated. new constraint: ${versionConstraint}`);
        dependencies[moduleName] = versionConstraint;
      } else {
        if (verbose) {
          console.log(`dependency ${moduleName} with version ${version} is in the workspace. no update`);
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
  isSnapshot = true, updateWorkspaceDependencies = false, releaseDependencyMapping = {}, newVersion, useRegexMap = false, verbose = false, dryrun = false, workspaceRoot = null
}: {
  isSnapshot?: boolean; updateWorkspaceDependencies?: boolean; releaseDependencyMapping?: Mapping; newVersion?: string; useRegexMap?: boolean; verbose?: boolean; dryrun?: boolean; workspaceRoot?: string;
}): Promise<void> {
  const filename = './package.json';
  const filePath = path.resolve(filename);
  const dir = path.dirname(filePath);
  const workspaceModules = await collectModulesInWorkspace(dir, workspaceRoot);
  if (!workspaceModules?.length) {
    console.log('no modules found');
    return;
  }

  const workspaceModuleNames = workspaceModules.map(module => module.manifest.name);
  for (const module of workspaceModules) {
    const packageJson = module.manifest;
    console.log(`updating version/dependency for module: ${packageJson.name}`);
    // update version of this module
    if (newVersion || useRegexMap) {
      if (isSnapshot) {
        packageJson.version = generateSnapshotVersion(packageJson.version, newVersion, verbose);
      } else {
        if (!useRegexMap) {
          packageJson.version = newVersion;
        } else {
          packageJson.version = createReleaseVersionConstraint(packageJson.name, releaseDependencyMapping, newVersion, useRegexMap, updateWorkspaceDependencies, verbose);
        }
      }
      if (verbose) {
        console.log(`new version for module: ${packageJson.version}`);
      }
    }

    // update dependencies of this module
    updateDependencyConstraints(packageJson.dependencies, workspaceModuleNames, updateWorkspaceDependencies, isSnapshot, releaseDependencyMapping, newVersion, useRegexMap, verbose);
    updateDependencyConstraints(packageJson.devDependencies, workspaceModuleNames, updateWorkspaceDependencies, isSnapshot, releaseDependencyMapping, newVersion, useRegexMap, verbose);
    updateDependencyConstraints(packageJson.peerDependencies, workspaceModuleNames, updateWorkspaceDependencies, isSnapshot, releaseDependencyMapping, newVersion, useRegexMap, verbose);
    updateDependencyConstraints(packageJson.optionalDependencies, workspaceModuleNames, updateWorkspaceDependencies, isSnapshot, releaseDependencyMapping, newVersion, useRegexMap, verbose);

    if (!dryrun) {
      // don't use module.writeProjectManifest as it changes the order of the dependencies in the package.json
      await fs.writeFile(path.resolve(module.rootDirRealPath, filename), JSON.stringify(packageJson, null, 2) + '\n', 'utf8');
    } else {
      console.log(JSON.stringify(packageJson, null, 2));
    }
  }
}

/**
 * Collects all modules in the given directory.
 * The workspace root is given is determined using {@link ensureWorkspaceRoot}.
 */
export async function collectModulesInWorkspace(dir: string, workspaceRoot?: string): Promise<Project[]> {
  // ensure workspace root
  workspaceRoot = await ensureWorkspaceRoot(dir, workspaceRoot);

  let patterns: string[];
  if (await files.exists(path.resolve(workspaceRoot, WORKSPACE_MANIFEST_FILENAME))) {
    // return packages as declared in the pnpm-workspace.yaml.
    const workspaceYaml = await PnpmWorkspaceYaml.parse(workspaceRoot);
    patterns = workspaceYaml.getPackages()
      .map(absolutePath => path.relative(workspaceRoot, absolutePath).replaceAll('\\', '/'));
  } else {
    // return package of current directory only
    patterns = [''];
  }
  return await findWorkspacePackagesNoCheck(workspaceRoot, {patterns});
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

export async function snapshotDependencies({verbose, dryrun, workspaceRoot = null}) {
  await updateAllPackageJsons({isSnapshot: true, updateWorkspaceDependencies: false, verbose, dryrun, workspaceRoot});
}

export async function snapshot({verbose, dryrun, workspaceRoot = null}) {
  const timeStamp = generateTimeStamp();
  await updateAllPackageJsons({isSnapshot: true, updateWorkspaceDependencies: true, newVersion: timeStamp, verbose, dryrun, workspaceRoot});
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
