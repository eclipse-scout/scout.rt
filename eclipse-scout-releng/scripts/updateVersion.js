#!/usr/bin/env node
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
const fsp = fs.promises;

const writeFile = async(fileName, file, verbose) => {
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
 * generates a timestamp with the pattern yyyyMMddHHmmss
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
 * generates a snapshot-version of the current module version and the timestamp
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
  console.log(`New Version: ${newVersion}`);
  return newVersion;
};


const updateSnapshotVersion = async verbose => {
  const filename = './package.json';
  const packageJson = require(path.resolve(filename));
  packageJson.version = generateSnapshotVersion({moduleVersion: packageJson.version, timestamp: generateTimeStamp(), verbose});
  await writeFile(filename, packageJson, verbose);
};

/**
 * searches the matching regex for the module name and returns the corresponding version.
 * @param moduleName
 * @param mapping
 * @param newModuleVersion
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
 * generates the constraint for a snapshot build
 * @param oldConstraint
 * @returns {string}
 */
const createSnapshotVersionConstraint = oldConstraint => {
  const cleanedModuleVersion = oldConstraint.replace(/-snapshot(.)*/i, '');
  const versionConstraint = `>=${cleanedModuleVersion}-snapshot <${cleanedModuleVersion}`; // ">=10.0.0-snapshot <10.0.0"
  return versionConstraint;
};

const updateDependencyConstraints = ({dependencies, modulesInWorkspace = [], updateWorkspaceDependencies, isSnapshot, mapping, verbose, newModuleVersion, useRegexMap}) => {
  if (!dependencies) {
    return;
  }

  const regex = new RegExp('-snapshot$');
  for (const [moduleName, version] of Object.entries(dependencies)) {
    if (regex.test(version)) {
      if ((updateWorkspaceDependencies && modulesInWorkspace.includes(moduleName)) || (!updateWorkspaceDependencies && !modulesInWorkspace.includes(moduleName))) {
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
 * find pnpm-workspace.yaml file by going up the directory tree
 * @param dir where to start searching
 * @param verbose more logging
 * @returns {Promise<*>}
 */
const findWorkspaceFileDir = async(dir, verbose) => {
  const filePath = path.join(dir, 'pnpm-workspace.yaml');
  if (fs.existsSync(filePath)) {
    if (verbose) {
      console.log('workspace dir: ' + dir);
    }
    return dir;
  }

  const parentDir = path.join(dir, '../');
  if (dir === parentDir) {
    throw new Error('there is no workspace file');
  }
  return findWorkspaceFileDir(parentDir, verbose);
};

/**
 * returns the folders which are excluded by default
 * @param excludeFolderOverride
 * @returns {*[]|*}
 */
const getExcludedFolders = excludeFolderOverride => {
  if (!excludeFolderOverride) {
    return ['node_modules', '.git', '.settings', '.idea', 'target', 'src'];
  }
  return excludeFolderOverride;
};
/**
 * searches all npm modules in the workspace.
 * @param dir
 * @param verbose
 * @param result
 * @returns {Promise<Array>} key is the name of the module, value is the path to the package.json file
 */
const findNpmModules = async({dir, verbose, result = [], excludeFolderOverride}) => {
  const excludedFolders = getExcludedFolders(excludeFolderOverride);
  const files = await fsp.readdir(dir);
  for (const file of files) {
    const filePath = path.resolve(dir, file);
    const state = await fsp.stat(filePath);
    if (state.isDirectory() && !excludedFolders.includes(file)) {
      // go deeper
      await findNpmModules({dir: filePath, verbose, result, excludeFolderOverride});
    } else {
      if (file === 'package.json' ) {
        if (verbose) {
          console.log(`found a package.json here: ${filePath}`);
        }

        const packageJson = require(filePath);
        const name = packageJson.name;
        result[name] = filePath;
      }
    }
  }
  return result;
};

const collectModulesInWorkspace = async(startDir, excludeFolderOverride, verbose) => {
  const root = await findWorkspaceFileDir(startDir, verbose);
  console.log('start searching for package.json files...');
  const result = await findNpmModules({dir: root, verbose, excludeFolderOverride});
  return result;
};


const updateAllPackageJsons = async({isSnapshot = true, updateWorkspaceDependencies = false, releaseDependencyMapping = {}, newVersion, useRegexMap = false, verbose = false, dryrun = false, excludeFolderOverride}) => {
  const filename = './package.json';
  const filePath = path.resolve(filename);
  const dir = path.dirname(filePath);
  const foundModulesMap = await collectModulesInWorkspace(dir, excludeFolderOverride, verbose);
  if (!foundModulesMap) {
    console.log('no modules found');
    return;
  }

  const modulesInWorkspace = Object.keys(foundModulesMap);
  for (const modulePath of Object.values(foundModulesMap)) {
    const packageJson = require(modulePath);
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
    updateDependencyConstraints({dependencies: packageJson.dependencies, modulesInWorkspace, updateWorkspaceDependencies, isSnapshot, mapping: releaseDependencyMapping, newModuleVersion: newVersion, useRegexMap, verbose});
    updateDependencyConstraints({dependencies: packageJson.devDependencies, modulesInWorkspace, updateWorkspaceDependencies, isSnapshot, mapping: releaseDependencyMapping, newModuleVersion: newVersion, useRegexMap, verbose});
    updateDependencyConstraints({dependencies: packageJson.peerDependencies, modulesInWorkspace, updateWorkspaceDependencies, isSnapshot, mapping: releaseDependencyMapping, newModuleVersion: newVersion, useRegexMap, verbose});
    updateDependencyConstraints({dependencies: packageJson.bundledDependencies, modulesInWorkspace, updateWorkspaceDependencies, isSnapshot, mapping: releaseDependencyMapping, newModuleVersion: newVersion, useRegexMap, verbose});
    updateDependencyConstraints({dependencies: packageJson.optionalDependencies, modulesInWorkspace, updateWorkspaceDependencies, isSnapshot, mapping: releaseDependencyMapping, newModuleVersion: newVersion, useRegexMap, verbose});

    if (!dryrun) {
      await writeFile(modulePath, packageJson, verbose);
    } else {
      console.log(JSON.stringify(packageJson, null, 2));
    }
  }
};

const setPreInstallSnapshotDependencies = async({verbose, dryrun, excludeFolderOverride}) => {
  await updateAllPackageJsons({isSnapshot: true, updateWorkspaceDependencies: false, verbose, dryrun, excludeFolderOverride});
};

const setPrePublishSnapshotDependencies = async({verbose, dryrun, excludeFolderOverride}) => {
  const timeStamp = generateTimeStamp();
  await updateAllPackageJsons({isSnapshot: true, updateWorkspaceDependencies: true, newVersion: timeStamp, verbose, dryrun, excludeFolderOverride});
};

const setPreInstallReleaseDependencies = async({mapping, verbose, dryrun, excludeFolderOverride}) => {
  await updateAllPackageJsons({isSnapshot: false, updateWorkspaceDependencies: false, releaseDependencyMapping: mapping, verbose, dryrun, excludeFolderOverride});

};

const setPrePublishReleaseDependencies = async({mapping, newVersion, verbose, dryrun, useRegexMap, excludeFolderOverride}) => {
  await updateAllPackageJsons({isSnapshot: false, updateWorkspaceDependencies: true, releaseDependencyMapping: mapping, newVersion, useRegexMap, verbose, dryrun, excludeFolderOverride});
};

module.exports = {
  updateSnapshotVersion,
  setPreInstallSnapshotDependencies,
  setPrePublishSnapshotDependencies,
  setPreInstallReleaseDependencies,
  setPrePublishReleaseDependencies
};
