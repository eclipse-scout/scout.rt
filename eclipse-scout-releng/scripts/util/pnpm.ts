/*
 * Copyright (c) 2010, 2026 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */

import {execFile, fork as forkNode} from 'node:child_process';
import {once} from 'node:events';
import path from 'node:path';
import process from 'node:process';
import {promisify} from 'node:util';
import files from './files.ts';

/**
 * Forks a pnpm process in the given directory with the given arguments.
 * @returns A promise to wait for the forked process to finish.
 */
async function fork(workingDir: string, ...args: readonly string[]): Promise<void> {
  const childProcess = forkNode(await findPnpm(), args, {
    cwd: workingDir,
    stdio: 'inherit'
  });
  const [code, signal] = await once(childProcess, 'exit') as [number, NodeJS.Signals];
  if (code !== 0) {
    throw new Error(`Child exited with code ${code}${signal ? `, signal ${signal}` : ''}`);
  }
}

/**
 * Structure of output of `pnpm list --recursive --json`.
 * See https://github.com/pnpm/pnpm/blob/main/pnpm/crates/cli/src/cli_args/list/render/structured.rs
 */
export type PnpmListOutput = PnpmProject[];

/** A workspace project (package.json) of the pnpm workspace. */
export interface PnpmProject {
  /** Absent if the manifest declares no name. */
  name?: string;
  /** Absent if the manifest declares no version. */
  version?: string;
  /** Absolute path of the project directory. */
  path: string;
  private: boolean;
  dependencies?: PnpmDependencyMap;
  devDependencies?: PnpmDependencyMap;
  optionalDependencies?: PnpmDependencyMap;
  /** Packages present in node_modules but not declared in package.json. */
  unsavedDependencies?: PnpmDependencyMap;
}

/** Dependencies keyed by alias (the name under which the dependent declares the package). */
export type PnpmDependencyMap = Record<string, PnpmDependency>;

export interface PnpmDependency {
  /** Real package name. Differs from the map key for `npm:` aliases. */
  from: string;
  /** Semver version (e.g. `26.2.1`) or a link (e.g. `link:scout.rt/eslint-config`). */
  version: string;
  /** Tarball URL. Absent for `link:` versions. */
  resolved?: string;
  /** Absolute path of the installed package. */
  path: string;
  /** Transitive dependencies. Absent if the package has none or is deduped. */
  dependencies?: PnpmDependencyMap;
  /** `true` if the subtree was already listed elsewhere and is omitted here. */
  deduped?: true;
  /** Number of dependencies omitted due to deduplication. Only present if `deduped`. */
  dedupedDependenciesCount?: number;
}

/**
 * Runs `pnpm list --recursive --json --depth=Infinity` in the given directory. The stderr output of pnpm is written to the console.
 * @returns the parsed output.
 */
async function list(workingDir: string): Promise<PnpmListOutput> {
  const {stdout, stderr} = await promisify(execFile)(process.execPath, [await findPnpm(), 'list', '-r', '--json', '--depth=Infinity'], {
    cwd: workingDir,
    maxBuffer: Infinity
  });
  if (stderr) {
    process.stderr.write(stderr);
  }
  return JSON.parse(stdout) as PnpmListOutput;
}

/**
 * @returns The location of the `pnpm.mjs` of the running Node.js installation. Throws an error if no `pnpm.mjs` was found.
 */
async function findPnpm(): Promise<string> {
  const candidates = [
    '../../lib/node_modules/pnpm/bin/pnpm.mjs', // e.g. Linux
    '../node_modules/pnpm/bin/pnpm.mjs' // e.g. Windows
  ];

  // find first candidate that exists
  for (const candidate of candidates) {
    const absolutePath = path.resolve(process.execPath, candidate);
    if (await files.exists(absolutePath)) {
      return absolutePath;
    }
  }

  // pnpm.mjs not found -> throw error
  throw new Error(`Cannot find 'pnpm.mjs' in node installation '${process.execPath}'.`);
}

export default {fork, list, findPnpm};
