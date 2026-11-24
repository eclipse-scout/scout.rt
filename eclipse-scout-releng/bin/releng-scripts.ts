/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */

import {getDefaultValues, getUnknownOptions, type OptionDefinitions, parseArguments} from '../scripts/util/cliArguments.ts';
import {release, releaseDependencies, snapshot, snapshotDependencies} from '../scripts/update-version.ts';
import {doCleanup} from '../scripts/snapshot-cleanup.ts';
import {DEFAULT_CONVERGENCE_LOG_LEVEL} from '../scripts/install/overrides/OverridesComputer.ts';
import {DEFAULT_UPDATE_MODE, scoutInstall} from '../scripts/install/scoutInstall.ts';

const workspaceRootOption: OptionDefinitions = {
  // Path to the pnpm workspace root directory (optional).
  workspaceRoot: {type: 'string', default: null}
};

const dryrunOption: OptionDefinitions = {
  // If true, simulation of the command
  dryrun: {type: 'boolean', default: false}
};

const verboseOption: OptionDefinitions = {
  verbose: {type: 'boolean', default: false}
};

const mappingOption: OptionDefinitions = {
  // e.g. --mapping.0.regex @your-dep --mapping.0.version 1.2.3
  mapping: {type: 'object'}
};

const commands: Record<string, { options: OptionDefinitions; run: (args: Record<string, any>) => Promise<void> }> = {
  // Currently, this operation does nothing as no update is necessary for snapshot dependencies which are not part of the pnpm-workspace.
  'version:snapshot:dependencies': {
    options: {...workspaceRootOption, ...dryrunOption},
    run: args => snapshotDependencies({dryrun: args.dryrun, workspaceRoot: args.workspaceRoot})
  },

  // Changes the version of all packages of the pnpm-workspace to a snapshot-version with timestamp.
  'version:snapshot': {
    options: {...workspaceRootOption, ...dryrunOption, ...verboseOption},
    run: args => snapshot({verbose: args.verbose, dryrun: args.dryrun, workspaceRoot: args.workspaceRoot})
  },

  // Updates snapshot-dependencies that are not part of the pnpm-workspace to the release versions of the given mapping.
  // E.g.: --mapping.0.regex @your-dep --mapping.0.version 1.2.3 --mapping.1.regex @your-dep2 --mapping.1.version 4.5.6
  'version:release:dependencies': {
    options: {...workspaceRootOption, ...dryrunOption, ...verboseOption, ...mappingOption},
    run: args => releaseDependencies({mapping: args.mapping, verbose: args.verbose, dryrun: args.dryrun, workspaceRoot: args.workspaceRoot})
  },

  // Updates the snapshot-versions of the pnpm-workspace modules with the new version provided. Also updates the dependencies to these pnpm-workspace modules with the new version.
  'version:release': {
    options: {
      ...workspaceRootOption,
      ...dryrunOption,
      ...verboseOption,
      ...mappingOption,
      newVersion: {type: 'string'},
      // true if the modules in the workspace have different versions. The regex-version mapping is used to set the version.
      useRegexMap: {type: 'boolean', default: false}
    },
    run: args => {
      if (!args.newVersion && !args.mapping) {
        throw new Error('Please provide arguments for --newVersion or --mapping');
      }
      return release({mapping: args.mapping, newVersion: args.newVersion, useRegexMap: args.useRegexMap, verbose: args.verbose, dryrun: args.dryrun, workspaceRoot: args.workspaceRoot});
    }
  },

  // Cleans up old modules on the Artifactory repository.
  'cleanup:snapshots': {
    options: {
      ...dryrunOption,
      ...verboseOption,
      apikey: {type: 'string'},
      // URL of the artifactory (with or without trailing slash).
      url: {type: 'string'},
      user: {type: 'string'},
      pwd: {type: 'string'},
      reponame: {type: 'string'},
      // Number of artifacts to keep
      keep: {type: 'number'}
    },
    run: args => doCleanup({url: args.url, apikey: args.apikey, user: args.user, pwd: args.pwd, repoName: args.reponame, keep: args.keep, dryrun: args.dryrun, verbose: args.verbose})
  },

  // Installs all dependencies using pnpm and updates the Scout overrides if required.
  install: {
    options: {
      // see updateMode in scoutInstall.ts
      updateMode: {type: 'string', default: DEFAULT_UPDATE_MODE},
      // see dependencyConvergenceLogLevel in OverridesComputer.ts
      convergenceLogLevel: {type: 'string', default: DEFAULT_CONVERGENCE_LOG_LEVEL}
    },
    run: args => scoutInstall(process.cwd(), {updateMode: args.updateMode, convergenceLogLevel: args.convergenceLogLevel})
  }
};

const allOptions: OptionDefinitions = Object.assign({}, ...Object.values(commands).map(command => command.options));
const {positionals, values} = parseArguments(process.argv.slice(2), allOptions);
const script = positionals[0] ?? '';
try {
  const command = commands[script];
  if (command) {
    const unknownOptions = getUnknownOptions(values, command.options);
    if (!unknownOptions.length) {
      await command.run({...getDefaultValues(command.options), ...values});
      console.log(`${script} done.`);
    } else {
      console.error(`Unknown options for script "${script}": ${unknownOptions.map(name => `--${name}`).join(', ')}.`);
      process.exitCode = 3;
    }
  } else {
    console.error(`Unknown script: "${script}".`);
    process.exitCode = 2;
  }
} catch (e) {
  console.error(`${script} failed.`);
  console.error(e);
  process.exitCode = 1;
}
