/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
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

process.on('unhandledRejection', err => {
  throw err;
});

import yargs from 'yargs';
import {hideBin} from 'yargs/helpers';
import {release, releaseDependencies, snapshot, snapshotDependencies} from '../scripts/update-version.ts';
import {doCleanup} from '../scripts/snapshot-cleanup.ts';
import {DEFAULT_CONVERGENCE_LOG_LEVEL, type DependencyConvergenceLogLevel, dependencyConvergenceLogLevel} from '../scripts/install/overrides/OverridesComputer.ts';
import {scoutInstall, type UpdateMode, updateMode, DEFAULT_UPDATE_MODE} from '../scripts/install/scoutInstall.ts';

async function updateVersionSnapshotDependencies(args): Promise<void> {
  try {
    await snapshotDependencies({verbose: args.verbose, dryrun: args.dryrun, workspaceRoot: args.workspaceRoot});
    console.log('version:snapshot:dependencies done');
  } catch (e) {
    console.error('version:snapshot:dependencies failed');
    console.error(e);
    process.exitCode = 1;
  }
}

async function updateVersionSnapshot(args): Promise<void> {
  try {
    await snapshot({verbose: args.verbose, dryrun: args.dryrun, workspaceRoot: args.workspaceRoot});
    console.log('version:snapshot done');
  } catch (e) {
    console.error('version:snapshot failed');
    console.error(e);
    process.exitCode = 1;
  }
}

async function updateVersionReleaseDependencies(args): Promise<void> {
  try {
    await releaseDependencies({mapping: args.mapping, verbose: args.verbose, dryrun: args.dryrun, workspaceRoot: args.workspaceRoot});
    console.log('version:release:dependencies done');
  } catch (e) {
    console.error('version:release:dependencies failed');
    console.error(e);
    process.exitCode = 1;
  }
}

async function updateVersionRelease(args): Promise<void> {
  if (!args.newVersion && !args.mapping) {
    throw new Error('Please provide arguments for --newVersion or --mapping');
  }
  try {
    await release({mapping: args.mapping, newVersion: args.newVersion, useRegexMap: args.useRegexMap, verbose: args.verbose, dryrun: args.dryrun, workspaceRoot: args.workspaceRoot});
    console.log('version:release done');
  } catch (e) {
    console.error('version:release failed');
    console.error(e);
    process.exitCode = 1;
  }
}

async function cleanupSnapshots(args): Promise<void> {
  try {
    await doCleanup({url: args.url, apikey: args.apikey, user: args.user, pwd: args.pwd, repoName: args.reponame, keep: args.keep, dryrun: args.dryrun, verbose: args.verbose});
    console.log('cleanup:snapshots done');
  } catch (e) {
    console.error('cleanup:snapshots failed');
    console.error(e);
    process.exitCode = 1;
  }
}

async function install(args): Promise<void> {
  try {
    const updateMode: UpdateMode = args.updateMode;
    const convergenceLogLevel: DependencyConvergenceLogLevel = args.convergenceLogLevel;
    await scoutInstall(process.cwd(), {updateMode, convergenceLogLevel});
  } catch (e) {
    console.error('install failed');
    console.error(e);
    process.exitCode = 1;
  }
}

export const args = yargs(hideBin(process.argv))
  .command('$0', 'default', () => {
  }, argv => {
    const script = argv._[0] ?? '';
    throw new Error(`Unknown script: "${script}"`);
  })
  .command('version:snapshot:dependencies', 'Currently, this operation does nothing as no update is necessary for snapshot dependencies which are not part of the pnpm-workspace.',
    yargs => yargs.option('workspaceRoot', {description: 'Path to the pnpm workspace root directory (optional).', type: 'string', default: null}),
    updateVersionSnapshotDependencies
  )
  .command('version:snapshot', 'Changes the version of all packages of the pnpm-workspace to a snapshot-version with timestamp.',
    yargs => yargs.option('workspaceRoot', {description: 'Path to the pnpm workspace root directory (optional).', type: 'string', default: null}),
    updateVersionSnapshot
  )
  .command('version:release:dependencies', 'Updates snapshot-dependencies that are not part of the pnpm-workspace to the release versions of the given mapping.',
    yargs => yargs
      .option('mapping', {
        description: '1 or more mappings with a regex and a version to specify which dependencies should be updated by what version. ' +
          'E.g.: --mapping.0.regex @your-dep --mapping.0.version 1.2.3 --mapping.1.regex @your-dep2 --mapping.1.version 4.5.6',
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
      .option('url', {description: 'URL of the artifactory (including trailing slash).', type: 'string'})
      .option('user', {description: 'Username', type: 'string'})
      .option('pwd', {description: 'Password', type: 'string'})
      .option('reponame', {description: 'Name of the repository', type: 'string'})
      .option('keep', {description: 'Number of Artifacts to keep', type: 'number'}),
    cleanupSnapshots
  )
  .command('install', 'Installs all dependencies using pnpm and updates the Scout overrides if required.',
    yargs => yargs
      .option('updateMode', {
        description: 'Specifies which dependencies should be updated. ' +
          `Use "${updateMode.REQUIRED}" to update packages that do not fulfil the specifier in the package.json. ` +
          `Use "${updateMode.ALL}" to update all packages for which a newer version exists. ` +
          `Use "${updateMode.SNAPSHOTS}" to update snapshots only. ` +
          `Default is "${DEFAULT_UPDATE_MODE}".`, type: 'string', default: DEFAULT_UPDATE_MODE
      })
      .option('convergenceLogLevel', {
        description: 'Specifies which non-convergent dependencies should be logged. ' +
          `Use "${dependencyConvergenceLogLevel.ALL}" to log all packages with non-unique versions. ` +
          `Use "${dependencyConvergenceLogLevel.SINGLE_EXTERNAL}" to log packages for which a single pnpm-workspace-external dependency exists. ` +
          'This may be handy to eliminate duplicate dependencies by adapting the workspace to the single external one. ' +
          `Use "${dependencyConvergenceLogLevel.OWN}" to log only mixed dependency versions in the pnpm-workspace. These may be misconfigured packages. ` +
          `Use "${dependencyConvergenceLogLevel.NONE}" to disable convergence logging. ` +
          `Default is ${DEFAULT_CONVERGENCE_LOG_LEVEL}`, type: 'string', default: DEFAULT_CONVERGENCE_LOG_LEVEL
      }),
    install
  )
  .option('dryrun', {description: 'If true, simulation of the command', type: 'boolean', default: false})
  .option('verbose', {description: 'More Logging', type: 'boolean', default: false})
  .argv;
