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

// Makes the script crash on unhandled rejections instead of silently
// ignoring them. In the future, promise rejections that are not handled will
// terminate the Node.js process with a non-zero exit code.
process.on('unhandledRejection', err => {
  throw err;
});

const yargs = require('yargs');

const updateVersionSnapshotDependencies = args => {
  const updateVersion = require('../scripts/update-version');
  updateVersion.snapshotDependencies({verbose: args.verbose, dryrun: args.dryrun, workspaceRoot: args.workspaceRoot})
    .then(() => console.log('version:snapshot:dependencies done'))
    .catch(e => {
      console.error('version:snapshot:dependencies failed');
      console.error(e);
      process.exitCode = 1;
    });
};

const updateVersionSnapshot = args => {
  const updateVersion = require('../scripts/update-version');
  updateVersion.snapshot({verbose: args.verbose, dryrun: args.dryrun, workspaceRoot: args.workspaceRoot})
    .then(() => console.log('version:snapshot done'))
    .catch(e => {
      console.error('version:snapshot failed');
      console.error(e);
      process.exitCode = 1;
    });
};

const updateVersionReleaseDependencies = args => {
  const updateVersion = require('../scripts/update-version');
  updateVersion.releaseDependencies({mapping: args.mapping, verbose: args.verbose, dryrun: args.dryrun, workspaceRoot: args.workspaceRoot})
    .then(() => console.log('version:release:dependencies done'))
    .catch(e => {
      console.error('version:release:dependencies failed');
      console.error(e);
      process.exitCode = 1;
    });
};

const updateVersionRelease = args => {
  if (!args.newVersion && !args.mapping) {
    throw new Error('Please provide arguments for --newVersion or --mapping');
  }
  const updateVersion = require('../scripts/update-version');
  updateVersion.release({mapping: args.mapping, newVersion: args.newVersion, useRegexMap: args.useRegexMap, verbose: args.verbose, dryrun: args.dryrun, workspaceRoot: args.workspaceRoot})
    .then(() => console.log('version:release done'))
    .catch(e => {
      console.error('version:release failed');
      console.error(e);
      process.exitCode = 1;
    });
};

const cleanupSnapshots = args => {
  const snapshotCleanup = require('../scripts/snapshot-cleanup');
  snapshotCleanup.doCleanup({url: args.url, apikey: args.apikey, user: args.user, pwd: args.pwd, reponame: args.reponame, keep: args.keep, dryrun: args.dryrun, verbose: args.verbose})
    .then(() => console.log('cleanup:snapshots done'))
    .catch(e => {
      console.error('cleanup:snapshots failed');
      console.error(e);
      process.exitCode = 1;
    });
};

yargs
  .command('$0', 'default', () => {
  }, argv => {
    throw new Error(`Unknown script ${argv._[0]}`);
  })
  .command('version:snapshot:dependencies', 'Updates snapshot-dependencies that are not part of the pnpm-workspace to a snapshot-range.',
    yargs => yargs.option('workspaceRoot', {description: 'Path to the pnpm workspace root directory (optional).', type: 'string', default: null}),
    updateVersionSnapshotDependencies
  )
  .command('version:snapshot', 'Adds the timestamp to the current snapshot-version and updates the pnpm-workspace dependencies to a snapshot-range.',
    yargs => yargs.option('workspaceRoot', {description: 'Path to the pnpm workspace root directory (optional).', type: 'string', default: null}),
    updateVersionSnapshot
  )
  .command('version:release:dependencies', 'Updates snapshot-dependencies that are not part of the pnpm-workspace to the release versions of the given mapping.',
    yargs => yargs
      .option('mapping', {
        description: '1 or more mappings with a regex and a version to specify which dependencies should be updated by what version. E.g.: --mapping.0.regex @your-dep --mapping.0.version 1.2.3 --mapping.1.regex @your-dep2 --mapping.1.version 4.5.6',
        type: 'string'
      })
      .option('workspaceRoot', {description: 'Path to the pnpm workspace root directory (optional).', type: 'string', default: null}),
    updateVersionReleaseDependencies
  )
  .command('version:release', 'Updates the snapshot-versions of the pnpm-workspace modules with the new version provided. Also updates the dependencies to these pnpm-workspace modules with the new version.',
    yargs => yargs
      .option('newVersion', {description: 'New version of the npm module', type: 'string'})
      .option('useRegexMap', {description: 'true if the modules in the workspace have different versions. the regex-version mapping is used to set the version', type: 'boolean', default: false})
      .option('workspaceRoot', {description: 'Path to the pnpm workspace root directory (optional).', type: 'string', default: null}),
    updateVersionRelease
  )
  .command('cleanup:snapshots', 'Cleans up old modules on the Artifactory repository.',
    yargs => yargs
      .option('apikey', {description: 'API Key for authentication', type: 'string'})
      .option('url', {description: 'URL of the artifactory', type: 'string'})
      .option('user', {description: 'Username', type: 'string'})
      .option('pwd', {description: 'Password', type: 'string'})
      .option('reponame', {description: 'Name of the repository', type: 'string'})
      .option('keep', {description: 'Number of Artifacts to keep', type: 'number'}),
    cleanupSnapshots
  )
  .option('dryrun', {description: 'If true, simulation of the command', type: 'boolean', default: false})
  .option('verbose', {description: 'More Logging', type: 'boolean', default: false}
  ).argv;
