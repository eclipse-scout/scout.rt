/*
 * Copyright (c) 2010, 2026 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */

import {fork as forkNode} from 'node:child_process';
import {once} from 'node:events';
import path from 'node:path';
import process from 'node:process';
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

export default {fork, findPnpm};
