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
import {dependencyConvergenceLogLevel, type DependencyConvergenceLogLevel, OverridesComputer} from './OverridesComputer.ts';
import {PnpmWorkspaceYaml, toDependencyOfSelector, WORKSPACE_MANIFEST_FILENAME} from '../../util/PnpmWorkspaceYaml.ts';
import pnpm from '../../util/pnpm.ts';
import {PnpmDependencyGraph} from './PnpmDependencyGraph.ts';

/**
 * Updates overrides of all `pnpm-workspace.yaml` in the given directory and its subdirectories.
 * @returns `true` if an enabled override of a peer dependency was removed from the `pnpm-workspace.yaml` in the given directory (see {@link isPeerDependencyOverrideRemoved}).
 */
export async function updateAllScoutOverrides(lockfileDir: string, convergenceLogLevel?: DependencyConvergenceLogLevel): Promise<boolean> {
  console.log(`Update all scout overrides in '${lockfileDir}' (convergenceLogLevel: '${convergenceLogLevel}')`);
  console.time('Update all scout overrides');

  const workspaceFiles = await PnpmWorkspaceYaml.findPnpmWorkspaceFiles(lockfileDir);
  if (!workspaceFiles.length) {
    throw new Error(`No pnpm-workspaces found in directory '${lockfileDir}'.`);
  }

  // list all dependencies once for all pnpm-workspace.yaml files
  const dependencyGraph = new PnpmDependencyGraph(await pnpm.list(lockfileDir));

  // update overrides of all pnpm-workspace.yaml files, the root workspace first so that its non-convergent dependencies are logged before all other output
  const rootWorkspaceFile = path.resolve(lockfileDir, WORKSPACE_MANIFEST_FILENAME);
  // the overrides of the root workspace apply to all packages of the dependency graph
  const peerDependencyOverrideRemoved = workspaceFiles.includes(rootWorkspaceFile) && await updateScoutOverrides(rootWorkspaceFile, dependencyGraph, convergenceLogLevel, true);
  await Promise.all(workspaceFiles.filter(workspaceFile => workspaceFile !== rootWorkspaceFile).map(workspaceFile => updateScoutOverrides(workspaceFile, dependencyGraph, dependencyConvergenceLogLevel.NONE)));

  console.timeEnd('Update all scout overrides');
  console.log();
  return peerDependencyOverrideRemoved;
}

/**
 * Updates overrides of the given `pnpm-workspace.yaml`.
 * @param dependencyGraph Dependency graph of all projects of the pnpm workspace.
 * @param convergenceLogLevel Which non-convergent dependencies are logged (see {@link OverridesComputer.logNonUniqueDependencyVersions}).
 * @param installsDependencyGraph `true` if the given `pnpm-workspace.yaml` is the one installing the given dependency graph (see {@link OverridesComputer.countVersionsOfAllPackages}).
 * @returns `true` if an enabled override of a peer dependency was removed (see {@link isPeerDependencyOverrideRemoved}).
 */
export async function updateScoutOverrides(workspaceFile: string, dependencyGraph: PnpmDependencyGraph, convergenceLogLevel?: DependencyConvergenceLogLevel, installsDependencyGraph = false): Promise<boolean> {
  // parse pnpm-workspace.yaml
  const wsYaml = await PnpmWorkspaceYaml.parse(workspaceFile);
  const previousOverrides = wsYaml.getEnabledScoutOverrides();

  // compute overrides
  const overridesComputer = new OverridesComputer(wsYaml, dependencyGraph, installsDependencyGraph);
  const overrides = await overridesComputer.compute();

  // log convergence
  overridesComputer.logNonUniqueDependencyVersions(convergenceLogLevel);

  // update overrides and flush pnpm-workspace.yaml
  wsYaml.updateScoutOverrides(overrides);
  await wsYaml.flush();

  console.log(`Updated scout overrides in ${workspaceFile}`);
  return isPeerDependencyOverrideRemoved(previousOverrides, overrides, overridesComputer.peerDependencyAliases);
}

/**
 * An override applies to the peer dependencies too: an override without parent replaces the peer range of every package declaring the dependency as peer, an
 * override with parent the one of this parent. The generated overrides of a peer dependency never replace a peer range, as they only have the packages providing
 * it as parent (see {@link OverridesComputer}). But overrides in the legacy format or generated before the dependency became a peer dependency may.
 * Such an override influenced the resolution of the peer dependency by the last install. Once removed, the next install may resolve it to another version.
 *
 * @returns `true` if one of the given previous overrides is an override of one of the given peer dependencies and not contained in the given new overrides.
 */
export function isPeerDependencyOverrideRemoved(previousOverrides: Record<string, string>, newOverrides: Record<string, string>, peerDependencyAliases: ReadonlySet<string>): boolean {
  return Object.keys(previousOverrides).some(selector => !(selector in newOverrides) && peerDependencyAliases.has(toDependencyOfSelector(selector)));
}
