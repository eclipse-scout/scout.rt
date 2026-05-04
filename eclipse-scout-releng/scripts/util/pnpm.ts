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
import path from 'node:path';
import process from 'node:process';
import files from './files.ts';

/**
 * Forks a pnpm process in the given directory with the given arguments.
 * @returns A promise to wait for the forked process to finish.
 */
async function fork(workingDir: string, ...args: string[]): Promise<void> {
  const pnpm = await findCjs();
  return new Promise((resolve, reject) => {
    // fork a child process in the working directory
    const childProcess = forkNode(pnpm, args, {
      cwd: workingDir,
      stdio: 'inherit'
    });

    // resolve or reject promise depending on the result of the child process
    childProcess.on('error', reject);
    childProcess.on('exit', (code: number, signal: NodeJS.Signals) => {
      if (code === 0) {
        resolve();
      } else {
        reject(new Error(`Child exited with code ${code}${signal ? `, signal ${signal}` : ''}`));
      }
    });
  });
}

/**
 * @returns The location of the `pnpm.cjs`. Throws an error if no `pnpm.cjs` was found.
 */
async function findCjs(): Promise<string> {
  const candidates = [
    '../../lib/node_modules/pnpm/bin/pnpm.cjs', // e.g. Linux
    '../node_modules/pnpm/bin/pnpm.cjs' // e.g. Windows
  ];

  // find first candidate that exists
  for (const candidate of candidates) {
    const absolutePath = path.resolve(process.execPath, candidate);
    if (await files.exists(absolutePath)) {
      return absolutePath;
    }
  }

  // pnpm.cjs not found -> throw error
  throw new Error(`Cannot find 'pnpm.cjs' in node installation '${process.execPath}'.`);
}

export default {fork, findCjs};
