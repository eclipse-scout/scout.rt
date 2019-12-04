#!/usr/bin/env node
/*
 * Copyright (c) BSI Business Systems Integration AG. All rights reserved.
 * http://www.bsiag.com/
 */

// Makes the script crash on unhandled rejections instead of silently
// ignoring them. In the future, promise rejections that are not handled will
// terminate the Node.js process with a non-zero exit code.
process.on('unhandledRejection', err => {
  throw err;
});

const yargs = require('yargs');

const generateSnapshot = args => {
  const script = require('../scripts/updateVersion');
  script.updateSnapshotVersion(args.verbose)
    .then(() => console.log('snapshot version done'))
    .catch(e => {
      console.error('snapshot version failed');
      console.error(e);
      process.exitCode = 1;
    });
};

const setInstallSnapshotDependencies = args => {
  const script = require('../scripts/updateVersion');
  script.setPreInstallSnapshotDependencies({verbose: args.verbose, dryrun: args.dryrun, excludeFolderOverride: args.excludeFolderOverride})
    .then(() => console.log('setPreInstallSnapshotDependencies version done'))
    .catch(e => {
      console.error('setPreInstallSnapshotDependencies version failed');
      console.error(e);
      process.exitCode = 1;
    });
};

const setPublishSnapshotDependencies = args => {
  const script = require('../scripts/updateVersion');
  script.setPrePublishSnapshotDependencies({verbose: args.verbose, dryrun: args.dryrun, excludeFolderOverride: args.excludeFolderOverride})
    .then(() => console.log('setPrePublishSnapshotDependencies version done'))
    .catch(e => {
      console.error('setPrePublishSnapshotDependencies version failed');
      console.error(e);
      process.exitCode = 1;
    });
};

const setInstallReleaseDependencies = args => {
  const script = require('../scripts/updateVersion');
  script.setPreInstallReleaseDependencies({mapping: args.mapping, verbose: args.verbose, dryrun: args.dryrun, excludeFolderOverride: args.excludeFolderOverride})
    .then(() => console.log('setPreInstallReleaseDependencies version done'))
    .catch(e => {
      console.error('setPreInstallReleaseDependencies version failed');
      console.error(e);
      process.exitCode = 1;
    });
};

const setPublishReleaseDependencies = args => {
  if (!args.newVersion && !args.mapping) {
    throw new Error('Please provide arguments for --newVersion or --mapping');
  }
  const script = require('../scripts/updateVersion');
  script.setPrePublishReleaseDependencies({mapping: args.mapping, newVersion: args.newVersion, useRegexMap: args.useRegexMap, verbose: args.verbose, dryrun: args.dryrun, excludeFolderOverride: args.excludeFolderOverride})
    .then(() => console.log('setPrePublishReleaseDependencies version done'))
    .catch(e => {
      console.error('setPrePublishReleaseDependencies version failed');
      console.error(e);
      process.exitCode = 1;
    });
};

const cleanupArtifactory = args => {
  const script = require('../scripts/snapshot-cleanup');
  script.doCleanup({url: args.url, apikey: args.apikey, user: args.user, pwd: args.pwd, reponame: args.reponame, keep: args.keep, dryrun: args.dryrun, verbose: args.verbose})
    .then(() => console.log('Repository cleanup done'))
    .catch(e => {
      console.error('Repository cleanup failed');
      console.error(e);
      process.exitCode = 1;
    });
};

yargs
  .command('$0', 'default', () => {
  }, argv => {
    console.log(`Unknown script ${argv._[0]}`);
  })
  .command('snapshot-version', 'generate a new snapshot version for the module',
    () => {
    },
    generateSnapshot
  )
  .command('snapshot-install-dependency', 'updates dependencies for ci',
    yargs => {
      return yargs
        .option('excludeFolderOverride', {
          description: 'override the default exclusion',
          type: 'array'
        });
    },
    setInstallSnapshotDependencies
  )
  .command('snapshot-publish-dependency', 'updates dependencies for ci',
    yargs => {
      return yargs
        .option('excludeFolderOverride', {
          description: 'override the default exclusion',
          type: 'array'
        });
    },
    setPublishSnapshotDependencies
  )
  .command('release-install-dependency', 'updates dependencies for ci',
    yargs => {
      return yargs
        .option('excludeFolderOverride', {
          description: 'override the default exclusion',
          type: 'array'
        });
    },
    setInstallReleaseDependencies
  )
  .command('release-publish-dependency', 'updates dependencies for ci',
    yargs => {
      return yargs
        .option('newVersion', {
          description: 'new version of the npm module',
          type: 'string'
        })
        .option('useRegexMap', {
          description: 'true if the modules in the workspace have different versions. the regex-version mapping is used to set the version',
          type: 'boolean',
          default: false
        })
        .option('excludeFolderOverride', {
          description: 'override the default exclusion',
          type: 'array'
        });
    },
    setPublishReleaseDependencies
  )
  .command('snapshot-cleanup', 'cleanup old modules on the artifactory',
    yargs => {
      return yargs
        .option('apikey', {
          description: 'API Key for authentication',
          type: 'string'
        })
        .option('url', {
          description: 'URL of the artifactory',
          type: 'string'
        })
        .option('user', {
          description: 'username',
          type: 'string'
        })
        .option('pwd', {
          description: 'password',
          type: 'string'
        })
        .option('reponame', {
          description: 'name of the repository',
          type: 'string'
        })
        .option('keep', {
          description: 'Number of Artifacts to keep',
          type: 'number'
        });
    },
    cleanupArtifactory
  )
  .option('dryrun', {
    description: 'If true, simulation of the command',
    type: 'boolean',
    default: false
  })
  .option(
    'verbose',
    {
      description: 'More Logging',
      type: 'boolean',
      default: false
    }
  ).argv;
