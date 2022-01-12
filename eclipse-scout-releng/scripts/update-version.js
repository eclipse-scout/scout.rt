/*
 * Copyright (c) 2010-2021 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */

/* eslint-disable max-len */

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

const writePackageJson = async (fileName, file, verbose) => {
  const newContentCompact = JSON.stringify(file);
  const currentContent = await fsp.readFile(fileName, 'utf-8');
  const currentContentCompact = JSON.stringify(JSON.parse(currentContent));
  if (currentContentCompact === newContentCompact) {
    // only change in formatting: skip write
    return;
  }

  let newContent = JSON.stringify(file, null, 2);
  if (!newContent.endsWith('\n')) {
    // ensure file ends with a new line
    newContent += '\n';
  }
  if (verbose) {
    console.log(`file: ${fileName}; new content:\n${newContent}`);
  }

  await fsp.writeFile(fileName, newContent, 'utf-8');
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

const updateDependencyConstraints = ({dependencies, workspaceModuleNames = [], updateWorkspaceDependencies, isSnapshot, mapping, verbose, newModuleVersion, useRegexMap}) => {
  if (!dependencies || isSnapshot) {
    return;
  }

  const regex = new RegExp('-snapshot\\s*<');
  for (const [moduleName, version] of Object.entries(dependencies)) {
    if (regex.test(version)) {
      if ((updateWorkspaceDependencies && workspaceModuleNames.includes(moduleName)) || (!updateWorkspaceDependencies && !workspaceModuleNames.includes(moduleName))) {
        const versionConstraint = createReleaseVersionConstraint({
          moduleName,
          mapping,
          newModuleVersion: newModuleVersion,
          useRegexMap,
          updateWorkspaceDependencies,
          verbose
        });
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
 * Gets the directory closest to the file-system root that contains a 'pnpm-workspace.yaml' file. The search starts at the given start dir stepping up the parent directories.
 * @param dir where to start searching.
 * @returns {Promise<*>}
 */
const findWorkspaceFileDir = async (dir) => {
  let pnpmWorkspace = null;
  let parentDir = dir;
  let currentDir;
  do {
    currentDir = parentDir;
    parentDir = path.join(currentDir, '../');
    let candidate = path.join(currentDir, 'pnpm-workspace.yaml');
    if (fs.existsSync(candidate)) {
      pnpmWorkspace = currentDir;
    }
  } while (currentDir !== parentDir);
  return pnpmWorkspace;
};

const collectModulesInWorkspace = async (startDir, verbose, workspaceRoot) => {
  if (workspaceRoot) {
    console.log(`use given workspace root: ${workspaceRoot}`);
  } else {
    workspaceRoot = await findWorkspaceFileDir(startDir);
    if (workspaceRoot) {
      console.log(`use workspace root found at: ${workspaceRoot}`);
    } else {
      workspaceRoot = path.join(startDir, '../'); // parent folder as default if no workspace file could be found
      console.log(`unable to find workspace file. Use parent directory as workspace root: ${workspaceRoot}`);
    }
  }
  return findWorkspacePackages.default(workspaceRoot);
};

const updateAllPackageJsons = async ({
                                       isSnapshot = true,
                                       updateWorkspaceDependencies = false,
                                       releaseDependencyMapping = {},
                                       newVersion,
                                       useRegexMap = false,
                                       verbose = false,
                                       dryrun = false,
                                       workspaceRoot = null
                                     }) => {
  const filename = './package.json';
  const filePath = path.resolve(filename);
  const dir = path.dirname(filePath);
  const workspaceModules = await collectModulesInWorkspace(dir, verbose, workspaceRoot);
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
    updateDependencyConstraints({
      dependencies: packageJson.dependencies,
      workspaceModuleNames,
      updateWorkspaceDependencies,
      isSnapshot,
      mapping: releaseDependencyMapping,
      newModuleVersion: newVersion,
      useRegexMap,
      verbose
    });
    updateDependencyConstraints({
      dependencies: packageJson.devDependencies,
      workspaceModuleNames,
      updateWorkspaceDependencies,
      isSnapshot,
      mapping: releaseDependencyMapping,
      newModuleVersion: newVersion,
      useRegexMap,
      verbose
    });
    updateDependencyConstraints({
      dependencies: packageJson.peerDependencies,
      workspaceModuleNames,
      updateWorkspaceDependencies,
      isSnapshot,
      mapping: releaseDependencyMapping,
      newModuleVersion: newVersion,
      useRegexMap,
      verbose
    });
    updateDependencyConstraints({
      dependencies: packageJson.bundledDependencies,
      workspaceModuleNames,
      updateWorkspaceDependencies,
      isSnapshot,
      mapping: releaseDependencyMapping,
      newModuleVersion: newVersion,
      useRegexMap,
      verbose
    });
    updateDependencyConstraints({
      dependencies: packageJson.optionalDependencies,
      workspaceModuleNames,
      updateWorkspaceDependencies,
      isSnapshot,
      mapping: releaseDependencyMapping,
      newModuleVersion: newVersion,
      useRegexMap,
      verbose
    });

    if (!dryrun) {
      await writePackageJson(path.join(module.dir, 'package.json'), packageJson, verbose);
    } else {
      console.log(JSON.stringify(packageJson, null, 2));
    }
  }
};

const snapshotDependencies = async ({verbose, dryrun, workspaceRoot = null}) => {
  await updateAllPackageJsons({isSnapshot: true, updateWorkspaceDependencies: false, verbose, dryrun, workspaceRoot});
};

const snapshot = async ({verbose, dryrun, workspaceRoot = null}) => {
  const timeStamp = generateTimeStamp();
  await updateAllPackageJsons({isSnapshot: true, updateWorkspaceDependencies: true, newVersion: timeStamp, verbose, dryrun, workspaceRoot});
};

const releaseDependencies = async ({mapping, verbose, dryrun, workspaceRoot = null}) => {
  await updateAllPackageJsons({isSnapshot: false, updateWorkspaceDependencies: false, releaseDependencyMapping: mapping, verbose, dryrun, workspaceRoot});
};

const release = async ({mapping, newVersion, verbose, dryrun, useRegexMap, workspaceRoot = null}) => {
  await updateAllPackageJsons({isSnapshot: false, updateWorkspaceDependencies: true, releaseDependencyMapping: mapping, newVersion, useRegexMap, verbose, dryrun, workspaceRoot});
};

module.exports = {
  snapshotDependencies,
  snapshot,
  releaseDependencies,
  release
};
