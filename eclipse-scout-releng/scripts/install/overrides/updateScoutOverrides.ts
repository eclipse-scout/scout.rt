/*
 * Copyright (c) 2010, 2026 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */

import {dependencyConvergenceLogLevel, type DependencyConvergenceLogLevel, OverridesComputer} from './OverridesComputer.ts';
import {PnpmWorkspaceYaml} from '../../util/PnpmWorkspaceYaml.ts';

/**
 * Updates overrides of all `pnpm-workspace.yaml` in the given directory and its subdirectories.
 */
export async function updateAllScoutOverrides(lockfileDir: string, convergenceLogLevel?: DependencyConvergenceLogLevel): Promise<void> {
  console.log(`Update all scout overrides in '${lockfileDir}' (convergenceLogLevel: '${convergenceLogLevel}')`);
  console.time('Update all scout overrides');

  // collect pnpm-workspace.yaml files, root workspace first
  const workspaceFiles = (await PnpmWorkspaceYaml.findPnpmWorkspaceFiles(lockfileDir)).sort(comparatorByLength);

  if (!workspaceFiles?.length) {
    process.exitCode = 2;
    throw new Error(`No pnpm-workspaces found in directory '${lockfileDir}'.`);
  }

  // update overrides of all pnpm-workspace.yaml files
  await Promise.all(workspaceFiles.map(workspaceFile => updateScoutOverrides(workspaceFile, lockfileDir, workspaceFile === workspaceFiles[0] ? convergenceLogLevel : dependencyConvergenceLogLevel.NONE)));

  console.timeEnd('Update all scout overrides');
  console.log();
}

/**
 * sort strings by length, shortest first.
 */
function comparatorByLength(a: string, b: string): number {
  const lengthDif = a.length - b.length;
  if (lengthDif !== 0) {
    return lengthDif;
  }
  return a.localeCompare(b);
}

/**
 * Updates overrides of the given `pnpm-workspace.yaml`.
 */
export async function updateScoutOverrides(workspaceFile: string, lockfileDir: string, convergenceLogLevel?: DependencyConvergenceLogLevel): Promise<void> {
  // parse pnpm-workspace.yaml
  const wsYaml = await PnpmWorkspaceYaml.parse(workspaceFile);

  // compute overrides
  const overridesComputer = new OverridesComputer(lockfileDir, wsYaml);
  const overrides = await overridesComputer.compute(convergenceLogLevel);

  // update overrides and flush pnpm-workspace.yaml
  wsYaml.updateScoutOverrides(overrides);
  await wsYaml.flush();

  console.log(`Updated scout overrides in ${workspaceFile}`);
}
