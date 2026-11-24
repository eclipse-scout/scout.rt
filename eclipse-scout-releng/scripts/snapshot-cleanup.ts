/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */

import {removeSnapshotSuffix} from './util/PackageJson.ts';

/**
 * Sends a request to the given url.
 * @throws Error if the response status is not 2xx.
 */
async function request(method: 'POST' | 'DELETE', url: string, headers: Record<string, string>, body?: string): Promise<Response> {
  const response = await fetch(url, {method, headers, body});
  if (!response.ok) {
    throw new Error(`${method} ${url} failed with status ${response.status}: ${await response.text()}`);
  }
  return response;
}

/**
 * @param artifactoryUrl URL of the artifactory including trailing slash.
 */
function constructPath(artifactoryUrl: string, item: SnapshotItem): string {
  return artifactoryUrl + [item.repo, item.path, item.filename].filter(part => part !== '.').join('/');
}

function getScopeName(path: string): string {
  const SCOPE_REGEX = /^@[\w-]+/;
  return path.match(SCOPE_REGEX)?.[0];
}

/**
 * @param itemName file name of a snapshot tarball, e.g. `core-27.1.0-snapshot.20261117202958.tgz`.
 * @returns the name and version of the package without snapshot suffix, e.g. `core-23.2.0`.
 */
function getPackageName(itemName: string): string {
  return removeSnapshotSuffix(itemName.replace(/\.tgz$/i, ''));
}

async function getSnapshots(artifactoryUrl: string, repoName: string, headers: Record<string, string>, verbose: boolean): Promise<Map<string, SnapshotItem[]>> {
  const AQL_API = 'api/search/aql'; // use aql for search
  const searchUrl = `${artifactoryUrl}${AQL_API}`;
  const query = `items.find(${JSON.stringify({repo: repoName, name: {$match: '*snapshot.*tgz'}})})`;

  const response = await request('POST', searchUrl, headers, query);
  const data = await response.json() as SearchData;

  if (verbose) {
    console.log('found items:\n' + JSON.stringify(data, null, 2));
  }

  const snapshotItems = (data?.results ?? []).map(item => ({filename: item.name, path: item.path, created: new Date(item.created), repo: item.repo}));
  // group the snapshots by package and version to ensure that old versions won't be deleted e.g. groupName = @eclipse-scout/eslint-config-22.0.0
  return Map.groupBy(snapshotItems, item => `${getScopeName(item.path)}/${getPackageName(item.filename)}`);
}

function calculateItemsToDelete(itemMap: Map<string, SnapshotItem[]>, noToKeep: number): SnapshotItem[] {
  // order the snapshots of each package by date and select the oldest items to delete
  return [...itemMap.values()].flatMap(items => items
    .sort((a, b) => b.created.valueOf() - a.created.valueOf())
    .slice(noToKeep));
}

async function deleteItems(artifactoryUrl: string, items: SnapshotItem[], headers: Record<string, string>, dryrun: boolean): Promise<void> {
  let success = true;
  if (!items?.length) {
    console.log('Nothing to cleanup');
    return;
  }
  for (const item of items) {
    const itemUrl = constructPath(artifactoryUrl, item);
    console.log(`delete: ${itemUrl}; ${dryrun ? 'dryrun=true' : ''}`);
    if (dryrun) {
      continue;
    }
    try {
      const response = await request('DELETE', itemUrl, headers);
      console.log(response.status);
    } catch (error) {
      success = false;
      console.error(`couldn't delete item: ${itemUrl}`);
      console.error(error);
    }
  }

  if (!success) {
    throw Error('Not every item could be deleted');
  }
}

export async function doCleanup({url, apikey, user, pwd, repoName, keep = 5, dryrun = false, verbose = false}: CleanupArguments): Promise<void> {
  console.log(`Input arguments: url=${url}; reponame=${repoName}; number of artifacts to keep=${keep}; dry-run=${dryrun}; verbose=${verbose}`);
  if (!repoName || !url) {
    throw new Error('Please provide arguments for --url and --reponame');
  }
  if (!Number.isInteger(keep) || keep < 0) {
    throw new Error(`--keep must be a non-negative integer but is '${keep}'.`);
  }

  const headers: Record<string, string> = {
    'Content-Type': 'text/plain'
  };
  if (apikey) {
    headers['X-JFrog-Art-Api'] = apikey;
  } else if (user && pwd) {
    headers['Authorization'] = `Basic ${Buffer.from(`${user}:${pwd}`).toString('base64')}`;
  }

  const artifactoryUrl = url.endsWith('/') ? url : `${url}/`;
  const foundItems = await getSnapshots(artifactoryUrl, repoName, headers, verbose);
  const itemsToDelete = calculateItemsToDelete(foundItems, keep);
  await deleteItems(artifactoryUrl, itemsToDelete, headers, dryrun);
}

type SearchData = { results: SearchResultItem[] };
type SearchResultItem = {
  /**
   * E.g. `@eclipse-scout/core/-/@eclipse-scout`.
   */
  path: string;
  /**
   * E.g. `core-23.2.0-snapshot.20251009202039.tgz`.
   */
  name: string;
  /**
   * E.g. `2025-10-09T22:23:00.112+02:00`
   */
  created: string;
  /**
   * E.g. `org.eclipse.scout-npm-local`.
   */
  repo: string;
};
type SnapshotItem = { filename: string; path: string; created: Date; repo: string };
export type CleanupArguments = {
  url: string;
  apikey?: string;
  user?: string;
  pwd?: string;
  repoName: string;
  keep?: number;
  dryrun?: boolean;
  verbose?: boolean;
};
