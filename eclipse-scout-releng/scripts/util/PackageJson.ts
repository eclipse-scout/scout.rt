/*
 * Copyright (c) 2010, 2026 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */

/**
 * Dependency declarations of a `package.json` (alias -> version specifier).
 */
export type Dependencies = Record<string, string>;

/**
 * Content of a `package.json`. Only the fields used by the release engineering scripts are typed.
 */
export interface PackageJson {
  name?: string;
  version?: string;
  dependencies?: Dependencies;
  devDependencies?: Dependencies;
  peerDependencies?: Dependencies;
  optionalDependencies?: Dependencies;
  peerDependenciesMeta?: Record<string, { optional?: boolean }>;

  [field: string]: unknown;
}

/**
 * Snapshot suffix of a version: `-snapshot`, optionally followed by a timestamp (e.g. `1.2.3-snapshot` or `1.2.3-snapshot.19700101000000`).
 */
const SNAPSHOT_SUFFIX = '-snapshot(?:\\.\\d{14})?';
const SNAPSHOT_VERSION_REGEX = new RegExp(`${SNAPSHOT_SUFFIX}$`, 'i');
/**
 * Range with a snapshot version as lower bound, e.g. `>=1.2.3-snapshot <1.2.3`.
 */
const SNAPSHOT_RANGE_REGEX = new RegExp(`${SNAPSHOT_SUFFIX}\\s*<`, 'i');

/**
 * @returns `true` if the given version ends with a snapshot suffix, e.g. `1.2.3-snapshot`, `1.2.3-snapshot.19700101000000` or `npm:foo@1.2.3-snapshot`.
 */
export function isSnapshotVersion(version: string): boolean {
  return SNAPSHOT_VERSION_REGEX.test(version);
}

/**
 * @returns `true` if the given version range has a snapshot version as lower bound, e.g. `>=1.2.3-snapshot <1.2.3`.
 */
export function isSnapshotRange(range: string): boolean {
  return SNAPSHOT_RANGE_REGEX.test(range);
}

/**
 * @returns the given version without snapshot suffix, e.g. `1.2.3` for `1.2.3-snapshot.19700101000000`.
 */
export function removeSnapshotSuffix(version: string): string {
  return version.replace(SNAPSHOT_VERSION_REGEX, '');
}
