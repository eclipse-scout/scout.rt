/*
 * Copyright (c) 2010, 2026 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */

import pnpm from '../util/pnpm.ts';
import {PnpmWorkspaceYaml} from '../util/PnpmWorkspaceYaml.ts';
import {DEFAULT_CONVERGENCE_LOG_LEVEL, dependencyConvergenceLogLevel, type DependencyConvergenceLogLevel} from './overrides/OverridesComputer.ts';
import {updateAllScoutOverrides} from './overrides/updateScoutOverrides.ts';

/**
 * Installs pnpm dependencies for the given directory and updates scout overrides if necessary.
 * The {@link options.updateMode} determines which dependencies are updated.
 */
export async function scoutInstall(dir: string, options: UpdateOptions = {updateMode: DEFAULT_UPDATE_MODE, convergenceLogLevel: DEFAULT_CONVERGENCE_LOG_LEVEL}): Promise<void> {
  options.updateMode = options.updateMode || DEFAULT_UPDATE_MODE;
  options.convergenceLogLevel = options.convergenceLogLevel || DEFAULT_CONVERGENCE_LOG_LEVEL;
  if (!Object.values(updateMode).includes(options.updateMode)) {
    throw new Error(`Unknown updateMode '${options.updateMode}'.`);
  }
  if (!Object.values(dependencyConvergenceLogLevel).includes(options.convergenceLogLevel)) {
    console.warn(`Unknown convergenceLogLevel '${options.convergenceLogLevel}'. Using default level '${DEFAULT_CONVERGENCE_LOG_LEVEL}' instead.`);
    options.convergenceLogLevel = DEFAULT_CONVERGENCE_LOG_LEVEL;
  }

  console.log();
  console.log(`Run scout install in '${dir}' (updateMode: '${options.updateMode}', convergenceLogLevel: '${options.convergenceLogLevel}')\n`);
  console.time('Scout install');

  const commonPnpmArguments = [
    '--recursive',
    '--no-lockfile',
    '--ignore-scripts',
    '--config.link-workspace-packages=true',
    '--config.prefer-workspace-packages=true'
  ];

  // 'pnpm update' updates all non overridden dependencies to the newest version available that fulfills the required version
  // if scout overrides are present, 'pnpm update' will only update dependencies that are not part of the scout overrides, i.e. snapshot dependencies
  // -> disable scout overrides depending on the updateMode
  if (options.updateMode === updateMode.ALL) {
    await disableScoutOverrides(dir);
  }

  try {
    // update
    console.log('Perform \'pnpm update\'\n');
    await pnpm.fork(dir, 'update', '--no-save', ...commonPnpmArguments);
    console.log();
  } finally {
    // enable overrides again
    if (options.updateMode === updateMode.ALL) {
      await enableScoutOverrides(dir);
    }
  }

  // 'pnpm install' updates all non overridden dependencies that do not fulfill the required version
  // -> disable scout overrides and call 'pnpm install' depending on the updateMode
  if (options.updateMode === updateMode.REQUIRED) {
    await disableScoutOverrides(dir);
    try {
      console.log('Perform \'pnpm install\'\n');
      await pnpm.fork(dir, 'install', ...commonPnpmArguments);
      console.log();
    } finally {
      // enable overrides again
      await enableScoutOverrides(dir);
    }
  }

  if (options.updateMode !== updateMode.SNAPSHOTS) {
    // update scout overrides if necessary
    await updateAllScoutOverrides(dir, options.convergenceLogLevel);
  }

  console.timeEnd('Scout install');
}

/**
 * Disables scout overrides of the `pnpm-workspace.yaml` in the given directory (see {@link PnpmWorkspaceYaml.removeScoutOverrides}).
 */
export async function disableScoutOverrides(dir: string): Promise<void> {
  console.log(`Disable scout overrides in '${dir}'\n`);
  const wsYaml = await PnpmWorkspaceYaml.parse(dir);
  wsYaml.removeScoutOverrides();
  await wsYaml.flush();
}

/**
 * Enables scout overrides of the `pnpm-workspace.yaml` in the given directory (see {@link PnpmWorkspaceYaml.addScoutOverrides}).
 */
export async function enableScoutOverrides(dir: string): Promise<void> {
  console.log(`Enable scout overrides in '${dir}'\n`);
  const wsYaml = await PnpmWorkspaceYaml.parse(dir);
  wsYaml.addScoutOverrides();
  await wsYaml.flush();
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
