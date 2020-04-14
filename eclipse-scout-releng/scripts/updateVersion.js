#!/usr/bin/env node
/* eslint-disable max-len */
/*
 * Copyright (c) 2010-2019 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */

// Makes the script crash on unhandled rejections instead of silently
// ignoring them. In the future, promise rejections that are not handled will
// terminate the Node.js process with a non-zero exit code.
process.on('unhandledRejection', err => {
  throw err;
});

const path = require('path');
const fs = require('fs');
const findWorkspacePackages = require('@pnpm/find-workspace-packages');
const fsp = fs.promises;

const writeFile = async (fileName, file, verbose) => {
  const stringified = JSON.stringify(file, null, 2);
  if (verbose) {
    console.log(`file: ${fileName}; new content:\n${stringified}`);
  }
  await fsp.writeFile(fileName, stringified);
  if (verbose) {
    console.log(`file ${fileName} saved`);
  }
};

/**
 * Generates a timestamp with the pattern yyyyMMddHHmmss
 * @returns {string}
 */
const generateTimeStamp = () => {
  let timestamp = new Date().toISOString(); // UTC

  // Create a string with the pattern yyyyMMddHHmmss
  timestamp = timestamp.replace(/[-:.TZ]/g, '');
  timestamp = timestamp.substr(0, timestamp.length - 3);
  return timestamp;
};

/**
 * Generates a snapshot-version of the current module version and the timestamp
 * @param moduleVersion
 * @param timestamp
 * @param verbose
 * @returns {string}
 */
const generateSnapshotVersion = ({moduleVersion, timestamp, verbose}) => {
  const cleanedVersion = moduleVersion.replace(/-snapshot(.)*/i, '');
  const newVersion = `${cleanedVersion}-snapshot.${timestamp}`;

  if (verbose) {
    console.log(`old version was: ${moduleVersion}`);
  }
  console.log(`new Version: ${newVersion}`);
  return newVersion;
};

const updateSnapshotVersion = async verbose => {
  const filename = './package.json';
  const packageJson = require(path.resolve(filename));
  packageJson.version = generateSnapshotVersion({moduleVersion: packageJson.version, timestamp: generateTimeStamp(), verbose});
  await writeFile(filename, packageJson, verbose);
};

/**
 * Searches the matching regex for the module name and returns the corresponding version.
 * @param moduleName
 * @param mapping.regex the regex to test against the given moduleName
 * @param mapping.version the new version to return if the regex matches
 * @param newModuleVersion the new version to return if useRegexMap is false and updateWorkspaceDependencies is true
 * @param useRegexMap
 * @param updateWorkspaceDependencies
 * @param verbose
 * @returns {string | *}
 */
const createReleaseVersionConstraint = ({moduleName, mapping, newModuleVersion, useRegexMap = false, updateWorkspaceDependencies, verbose}) => {
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
};

/**
 * Generates the constraint for a snapshot build
 * @param oldConstraint
 * @returns {string}
 */
const createSnapshotVersionConstraint = oldConstraint => {
  const cleanedModuleVersion = oldConstraint.replace(/-snapshot(.)*/i, '');
  return `>=${cleanedModuleVersion}-snapshot <${cleanedModuleVersion}`; // ">=10.0.0-snapshot <10.0.0"
};

const updateDependencyConstraints = ({dependencies, workspaceModuleNames = [], updateWorkspaceDependencies, isSnapshot, mapping, verbose, newModuleVersion, useRegexMap}) => {
  if (!dependencies) {
    return;
  }

  const regex = new RegExp('-snapshot$');
  for (const [moduleName, version] of Object.entries(dependencies)) {
    if (regex.test(version)) {
      if ((updateWorkspaceDependencies && workspaceModuleNames.includes(moduleName)) || (!updateWorkspaceDependencies && !workspaceModuleNames.includes(moduleName))) {
        const versionConstraint = isSnapshot ? createSnapshotVersionConstraint(version) : createReleaseVersionConstraint({moduleName, mapping, newModuleVersion: newModuleVersion, useRegexMap, updateWorkspaceDependencies, verbose});
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
};

/**
 * Finds pnpm-workspace.yaml file by going up the directory tree
 * @param dir where to start searching
 * @param verbose more logging
 * @returns {Promise<*>}
 */
const findWorkspaceFileDir = async (dir, verbose) => {
  const filePath = path.join(dir, 'pnpm-workspace.yaml');
  if (fs.existsSync(filePath)) {
    if (verbose) {
      console.log('workspace dir: ' + dir);
    }
    return dir;
  }

  const parentDir = path.join(dir, '../');
  if (dir === parentDir) {
    return null;
  }
  return findWorkspaceFileDir(parentDir, verbose);
};

const collectModulesInWorkspace = async (startDir, verbose) => {
  let root = await findWorkspaceFileDir(startDir, verbose);
  if (!root) {
    root = path.join(startDir, '../'); // parent folder as default if no workspace file could be found
    console.log(`unable to find workspace file. Use parent directory as workspace root: ${root}`);
  }
  console.log(`start searching for package.json files at ${root}`);
  return findWorkspacePackages.default(root);
};

const updateAllPackageJsons = async ({isSnapshot = true, updateWorkspaceDependencies = false, releaseDependencyMapping = {}, newVersion, useRegexMap = false, verbose = false, dryrun = false}) => {
  const filename = './package.json';
  const filePath = path.resolve(filename);
  const dir = path.dirname(filePath);
  const workspaceModules = await collectModulesInWorkspace(dir, verbose);
  if (!workspaceModules || workspaceModules.length === 0) {
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
        packageJson.version = generateSnapshotVersion({moduleVersion: packageJson.version, timestamp: newVersion, verbose});
      } else {
        if (!useRegexMap) {
          packageJson.version = newVersion;
        } else {
          packageJson.version = createReleaseVersionConstraint({moduleName: packageJson.name, mapping: releaseDependencyMapping, newVersion, useRegexMap, updateWorkspaceDependencies, verbose});
        }
      }
      if (verbose) {
        console.log(`new version for module: ${packageJson.version}`);
      }
    }

    // update dependencies of this module
    updateDependencyConstraints({dependencies: packageJson.dependencies, workspaceModuleNames, updateWorkspaceDependencies, isSnapshot, mapping: releaseDependencyMapping, newModuleVersion: newVersion, useRegexMap, verbose});
    updateDependencyConstraints({dependencies: packageJson.devDependencies, workspaceModuleNames, updateWorkspaceDependencies, isSnapshot, mapping: releaseDependencyMapping, newModuleVersion: newVersion, useRegexMap, verbose});
    updateDependencyConstraints({dependencies: packageJson.peerDependencies, workspaceModuleNames, updateWorkspaceDependencies, isSnapshot, mapping: releaseDependencyMapping, newModuleVersion: newVersion, useRegexMap, verbose});
    updateDependencyConstraints({dependencies: packageJson.bundledDependencies, workspaceModuleNames, updateWorkspaceDependencies, isSnapshot, mapping: releaseDependencyMapping, newModuleVersion: newVersion, useRegexMap, verbose});
    updateDependencyConstraints({dependencies: packageJson.optionalDependencies, workspaceModuleNames, updateWorkspaceDependencies, isSnapshot, mapping: releaseDependencyMapping, newModuleVersion: newVersion, useRegexMap, verbose});

    if (!dryrun) {
      await writeFile(path.join(module.dir, 'package.json'), packageJson, verbose);
    } else {
      console.log(JSON.stringify(packageJson, null, 2));
    }
  }
};

const setPreInstallSnapshotDependencies = async ({verbose, dryrun}) => {
  await updateAllPackageJsons({isSnapshot: true, updateWorkspaceDependencies: false, verbose, dryrun});
};

const setPrePublishSnapshotDependencies = async ({verbose, dryrun}) => {
  const timeStamp = generateTimeStamp();
  await updateAllPackageJsons({isSnapshot: true, updateWorkspaceDependencies: true, newVersion: timeStamp, verbose, dryrun});
};

const setPreInstallReleaseDependencies = async ({mapping, verbose, dryrun}) => {
  await updateAllPackageJsons({isSnapshot: false, updateWorkspaceDependencies: false, releaseDependencyMapping: mapping, verbose, dryrun});
};

const setPrePublishReleaseDependencies = async ({mapping, newVersion, verbose, dryrun, useRegexMap}) => {
  await updateAllPackageJsons({isSnapshot: false, updateWorkspaceDependencies: true, releaseDependencyMapping: mapping, newVersion, useRegexMap, verbose, dryrun});
};

module.exports = {
  updateSnapshotVersion,
  setPreInstallSnapshotDependencies,
  setPrePublishSnapshotDependencies,
  setPreInstallReleaseDependencies,
  setPrePublishReleaseDependencies
};
