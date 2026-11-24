/*
 * Copyright (c) 2010, 2026 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */

import {promises as fs} from 'node:fs';

/**
 * Checks whether a file exists for the given path.
 * @returns true if it exists and can be read, false otherwise.
 */
async function exists(path: string): Promise<boolean> {
  return await fs.access(path, fs.constants.R_OK).then(() => true).catch(() => false);
}

export default {exists};
