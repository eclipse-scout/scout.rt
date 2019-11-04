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

const writeFile = async(fileName, file, verbose) => {
  const fsp = require('fs').promises;
  const stringified = JSON.stringify(file, null, 2);
  if (verbose) {
    console.log(`file: ${fileName}; new content:\n${stringified}`);
  }
  await fsp.writeFile(fileName, stringified);
  if (verbose) {
    console.log(`file ${fileName} saved`);
  }
};

const updateDepencies = (dependencies, regex, version, verbose) => {
  for (const moduleName of Object.keys(dependencies)) {
    if (regex.test(moduleName)) {
      if (verbose) {
        console.log(`updating dependency: ${moduleName}: ${version}`);
      }
      dependencies[moduleName] = version;
    }
  }
};

const updateVersion = async() => {
  const yargs = require('yargs');
  const path = require('path');
  const argv = yargs
    .option('newVersion', {
      description: 'new version of the npm module',
      type: 'string'
    })
    .option('verbose', {
      description: 'More Logging',
      type: 'boolean',
      default: false
    })
    .argv;

  console.log(`Input arguments: new version=${argv.newVersion};verbose=${argv.verbose}`);

  if (!argv.newVersion) {
    throw new Error('Please provide arguments for --newVersion');
  }

  const fileName = './package.json';
  const packageJson = require(path.resolve(fileName));

  // update version of npm module
  packageJson.version = argv.newVersion;

  // --mapping.0.regex @eclipse-scout --mapping.0.version 10.0.2 creates an object mapping = {0: {regex: @eclipse-scout; version: 10.0.2}}
  if (argv.mapping) {
    for (const entry of Object.values(argv.mapping)) {
      if (!entry.regex || !entry.version) {
        throw new Error('Please provide arguments in the form of --mapping.0.regex and --mapping.0.version');
      }

      const regex = new RegExp(`^${entry.regex}`); // the module name should start with the regex

      if (argv.verbose) {
        console.log(`searching for dependencies with regex: ${regex} and updating to version=${entry.version}`);
      }
      updateDepencies(packageJson.dependencies, regex, entry.version, argv.verbose);
      updateDepencies(packageJson.devDependencies, regex, entry.version, argv.verbose);
      updateDepencies(packageJson.peerDependencies, regex, entry.version, argv.verbose);
    }
  }

  await writeFile(fileName, packageJson, argv.verbose);
};

updateVersion()
  .then(() => console.log('Update version done'))
  .catch(e => {
    console.error('Update version failed');
    console.error(e);
    process.exit(1);
  });
