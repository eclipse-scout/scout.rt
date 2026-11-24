/*
 * Copyright (c) 2010, 2026 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */

import path from 'node:path';
import files from '../util/files.ts';
import pnpm from '../util/pnpm.ts';
import {PnpmWorkspaceYaml, WORKSPACE_MANIFEST_FILENAME} from '../util/PnpmWorkspaceYaml.ts';
import {DEFAULT_CONVERGENCE_LOG_LEVEL, dependencyConvergenceLogLevel, type DependencyConvergenceLogLevel} from './overrides/OverridesComputer.ts';
import {updateAllScoutOverrides} from './overrides/updateScoutOverrides.ts';

/**
 * Installs pnpm dependencies for the given directory and updates scout overrides if necessary.
 * The {@link UpdateOptions.updateMode} determines which dependencies are updated.
 */
export async function scoutInstall(dir: string, {updateMode: mode = DEFAULT_UPDATE_MODE, convergenceLogLevel = DEFAULT_CONVERGENCE_LOG_LEVEL}: UpdateOptions = {}): Promise<void> {
  if (!Object.values(updateMode).includes(mode)) {
    throw new Error(`Unknown updateMode '${mode}'.`);
  }
  if (!Object.values(dependencyConvergenceLogLevel).includes(convergenceLogLevel)) {
    console.warn(`Unknown convergenceLogLevel '${convergenceLogLevel}'. Using default level '${DEFAULT_CONVERGENCE_LOG_LEVEL}' instead.`);
    convergenceLogLevel = DEFAULT_CONVERGENCE_LOG_LEVEL;
  }

  console.log();
  console.log(`Run scout install in '${dir}' (updateMode: '${mode}', convergenceLogLevel: '${convergenceLogLevel}')\n`);
  console.time('Scout install');

  // 'pnpm update' updates all non overridden dependencies to the newest version available that fulfills the existing specifiers
  // if scout overrides are present, 'pnpm update' will only update dependencies that are not part of the scout overrides, i.e. snapshot dependencies
  // -> disable scout overrides in ALL mode
  // in REQUIRED mode, the scout overrides stay enabled: they only apply to dependencies whose range includes the pinned version, so dependencies whose range
  // was changed in a package.json (and their new transitive dependencies) are resolved anew, while all other dependencies are kept
  const disableOverrides = mode === updateMode.ALL;
  try {
    if (disableOverrides) {
      await setScoutOverridesEnabled(dir, false);
    } else if (mode === updateMode.REQUIRED) {
      // the legacy overrides may force versions outside the declared ranges, the migrated ones apply only where the declared range includes the version
      await migrateLegacyScoutOverrides(dir);
    }

    await pnpmUpdate(dir);

    if (mode !== updateMode.SNAPSHOTS) {
      // update scout overrides (enables the disabled overrides in ALL mode)
      const peerDependencyOverrideRemoved = await updateAllScoutOverrides(dir, convergenceLogLevel);
      if (peerDependencyOverrideRemoved) {
        // a removed override replaced the range of a peer dependency in the update above: install once more to resolve it without (see isPeerDependencyOverrideRemoved)
        console.log('An override of a peer dependency was removed: repeat \'pnpm update\' and the update of the scout overrides\n');
        await pnpmUpdate(dir);
        if (await updateAllScoutOverrides(dir, convergenceLogLevel)) {
          console.warn('An override of a peer dependency was removed again: the next scout install may change the installed dependencies.\n');
        }
      }
    }
  } catch (error) {
    if (disableOverrides) {
      await setScoutOverridesEnabled(dir, true)
        .catch(restoreError => console.error(`Restoring the scout overrides in '${dir}' failed:`, restoreError)); // a failing restore must not hide the original error
    }
    throw error;
  }

  console.timeEnd('Scout install');
}

async function pnpmUpdate(dir: string): Promise<void> {
  console.log('Perform \'pnpm update\'\n');
  await pnpm.fork(dir, 'update', '--no-save', '--recursive', '--no-lockfile', '--ignore-scripts', '--config.link-workspace-packages=true', '--config.prefer-workspace-packages=true');
  console.log();
}

/**
 * Migrates the `pnpm-workspace.yaml` in the given directory if it is in the legacy format (see {@link PnpmWorkspaceYaml.migrateLegacyScoutOverrides}).
 */
async function migrateLegacyScoutOverrides(dir: string): Promise<void> {
  if (await files.exists(path.join(dir, WORKSPACE_MANIFEST_FILENAME)) && await changeWorkspaceYaml(dir, wsYaml => wsYaml.migrateLegacyScoutOverrides())) {
    console.log(`Migrated the legacy scout overrides in '${dir}'\n`);
  }
}

/**
 * Enables or disables the scout overrides of the `pnpm-workspace.yaml` in the given directory (see {@link PnpmWorkspaceYaml.enableScoutOverrides}).
 */
async function setScoutOverridesEnabled(dir: string, enabled: boolean): Promise<void> {
  console.log(`${enabled ? 'Enable' : 'Disable'} scout overrides in '${dir}'\n`);
  await changeWorkspaceYaml(dir, wsYaml => {
    if (enabled) {
      wsYaml.enableScoutOverrides();
    } else {
      wsYaml.disableScoutOverrides();
    }
    return true;
  });
}

/**
 * Applies the given change to the `pnpm-workspace.yaml` in the given directory and writes it if the change returns `true`.
 * @returns the result of the change.
 */
async function changeWorkspaceYaml(dir: string, change: (wsYaml: PnpmWorkspaceYaml) => boolean): Promise<boolean> {
  const wsYaml = await PnpmWorkspaceYaml.parse(dir);
  const changed = change(wsYaml);
  if (changed) {
    await wsYaml.flush();
  }
  return changed;
}

export interface UpdateOptions {
  /**
   * @see UpdateMode
   */
  updateMode?: UpdateMode;
  /**
   * @see DependencyConvergenceLogLevel
   */
  convergenceLogLevel?: DependencyConvergenceLogLevel;
}

/**
 * @see updateMode
 */
export type UpdateMode = typeof updateMode[keyof typeof updateMode];

/**
 * Determines which dependencies are updated. For the default value see {@link DEFAULT_UPDATE_MODE}.
 */
export const updateMode = {
  /**
   * All dependencies that do not fulfill the specifier from the package.json are updated. Scout overrides are updated as necessary.
   */
  REQUIRED: 'required',
  /**
   * All dependencies are updated to the newest possible version available that fulfills the specifier from the package.json. Scout overrides are updated as necessary.
   */
  ALL: 'all',
  /**
   * Only snapshot dependencies are updated to the newest version available. No Scout overrides are updated (as snapshots are never locked using overrides).
   */
  SNAPSHOTS: 'snapshots'
} as const;

/**
 * The default update mode {@link updateMode.REQUIRED}.
 */
export const DEFAULT_UPDATE_MODE = updateMode.REQUIRED;
