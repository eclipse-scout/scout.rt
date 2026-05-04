/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */

import axios, {type AxiosRequestConfig, type AxiosResponse, type RawAxiosRequestHeaders} from 'axios';

// Makes the script crash on unhandled rejections instead of silently
// ignoring them. In the future, promise rejections that are not handled will
// terminate the Node.js process with a non-zero exit code.
process.on('unhandledRejection', err => {
  throw err;
});

function constructPath(artifactoryUrl: string, item: SnapshotItem): string {
  if (item.path === '.') {
    return `${artifactoryUrl}/${item.repo}/${item.filename}`;
  }
  return `${artifactoryUrl}/${item.repo}/${item.path}/${item.filename}`;
}

function getScopeName(path: string): string {
  const SCOPE_REGEX = /^@[\w-]+/g;
  return path.match(SCOPE_REGEX)?.[0];
}

function getPackageName(itemName: string): string {
  const NAME_REGEX = /-snapshot.*/gi;
  return itemName.replace(NAME_REGEX, '');
}

async function getSnapshots(artifactoryUrl: string, repoName: string, config: AxiosRequestConfig<string>, verbose: boolean): Promise<Map<string, Set<SnapshotItem>>> {
  const AQL_API = 'api/search/aql'; // use aql for search
  const searchUrl = `${artifactoryUrl}${AQL_API}`;
  const query = `items.find({"repo":"${repoName}","name":{"$match":"*snapshot.*tgz"}})`;

  const snapshotMap = new Map<string, Set<SnapshotItem>>();
  const response: AxiosResponse<SearchData> = await axios.post(searchUrl, query, config);
  const data = response.data;

  if (verbose) {
    console.log('found items:\n' + JSON.stringify(data, null, 2));
  }

  if (data && data.results) {
    for (const item of data.results) {
      // group the snapshots by package and version to ensure that old versions won't be deleted e.g. groupName = @eclipse-scout/eslint-config-22.0.0
      const groupName = `${getScopeName(item.path)}/${getPackageName(item.name)}`;

      let groupSet = snapshotMap.get(groupName);
      if (!groupSet) {
        groupSet = new Set();
        snapshotMap.set(groupName, groupSet);
      }
      const snapshotItem: SnapshotItem = {
        groupName,
        filename: item.name,
        path: item.path,
        created: new Date(item.created),
        repo: item.repo
      };
      groupSet.add(snapshotItem);
    }
  }
  return snapshotMap;
}

async function calculateItemsToDelete(itemMap: Map<string, Set<SnapshotItem>>, noToKeep: number): Promise<SnapshotItem[]> {
  const toDelete: SnapshotItem[] = [];
  for (const itemSet of itemMap.values()) {
    // order the snapshots of each package by date and select the oldest items to delete
    const deleteItems = Array.from(itemSet)
      .sort((a, b) => b.created.valueOf() - a.created.valueOf())
      .slice(noToKeep);
    toDelete.push(...deleteItems);
  }
  return toDelete;
}

async function deleteItems(artifactoryUrl: string, items: SnapshotItem[], config: AxiosRequestConfig<string>, dryrun: boolean): Promise<void> {
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
      const response: AxiosResponse = await axios.delete(itemUrl, config);
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
  console.log(`Input arguments: url=${url}; repo-name=${repoName}; number of artifacts to keep=${keep}; dry-run=${dryrun}; verbose=${verbose}`);
  if (!repoName || !url) {
    throw new Error('Please provide arguments for --url and --repo-name');
  }

  const headers: RawAxiosRequestHeaders = {
    'Content-Type': 'text/plain'
  };
  if (apikey) {
    headers['X-JFrog-Art-Api'] = apikey;
  }
  const config: AxiosRequestConfig<string> = {
    headers: headers
  };
  if (!apikey && user && pwd) {
    config.auth = {
      username: user,
      password: pwd
    };
  }

  const foundItems = await getSnapshots(url, repoName, config, verbose);
  const itemsToDelete = await calculateItemsToDelete(foundItems, keep);
  await deleteItems(url, itemsToDelete, config, dryrun);
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
type SnapshotItem = { groupName: string; filename: string; path: string; created: Date; repo: string };
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
