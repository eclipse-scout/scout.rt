#!/usr/bin/env node
/*******************************************************************************
 * Copyright (c) 2014-2019 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/

const axios = require('axios');

// Makes the script crash on unhandled rejections instead of silently
// ignoring them. In the future, promise rejections that are not handled will
// terminate the Node.js process with a non-zero exit code.
process.on('unhandledRejection', err => {
  throw err;
});

const constructPath = (artifactoryUrl, item) => {
  if (item.path === '.') {
    return `${artifactoryUrl}/${item.repo}/${item.filename}`;
  }
  return `${artifactoryUrl}/${item.repo}/${item.path}/${item.filename}`;
};

const getScopeName = path => {
  const SCOPE_REGEX = /^@[\w-]+/g;
  return path.match(SCOPE_REGEX);
};

const getPackageName = itemName => {
  const NAME_REGEX = /-snapshot.*/gi;
  return itemName.replace(NAME_REGEX, '');
};

const getSnapshots = async(artifactoryUrl, config) => {
  const AQL_API = 'api/search/aql'; // use aql for search
  const SEARCH_URL = `${artifactoryUrl}${AQL_API}`;
  const QUERY = 'items.find({"repo":"com.bsiag.scout-npm-local","name":{"$match":"*snapshot.*tgz"}})';

  const snapshotMap = new Map();
  try {
    const response = await axios.post(SEARCH_URL, QUERY, config);
    if (response.status !== 200) {
      console.log(`NOK: ${response.statusText}`);
      return snapshotMap;
    }
    const data = response.data;

    console.log('found items:\n' + JSON.stringify(data, null, 2));

    if (data && data.results) {
      for (const item of data.results) {
        // group the snapshots by package and version to ensure that old versions wont be deleted e.g. groupName = @scout/cli-10.0.0
        const groupName = `${getScopeName(item.path)}/${getPackageName(item.name)}`;

        let groupSet = snapshotMap.get(groupName);
        if (!groupSet) {
          groupSet = new Set();
          snapshotMap.set(groupName, groupSet);
        }
        groupSet.add({
          groupName,
          filename: item.name,
          path: item.path,
          created: new Date(item.created),
          repo: item.repo
        });
      }
    }
    return snapshotMap;
  } catch (error) {
    console.error(error);
    return snapshotMap;
  }
};

const calculateItemsToDelete = async(itemMap, noToKeep) => {
  const toDelete = [];
  for (const itemSet of itemMap.values()) {
    // order the snapshots of each package by date and select the oldest items to delete
    const deleteItems = Array.from(itemSet)
      .sort((a, b) => b.created - a.created)
      .slice(noToKeep);
    toDelete.push(...deleteItems);
  }
  return toDelete;
};

const deleteItems = async(artifactoryUrl, items, config, dryrun) => {
  for (const item of items) {
    const itemUrl = constructPath(artifactoryUrl, item);
    console.log(`delete: ${itemUrl}; dryrun=${dryrun}`);
    if (dryrun) {
      continue;
    }
    try {
      const response = await axios.delete(itemUrl, config);
      console.log(response.status);
    } catch (error) {
      console.error(error);
    }
  }
};

const doCleanup = async() => {
  const ARTIFACTORY_URL = 'https://scout.bsiag.com/repository/'; // Artifactory server
  const NO_KEEP = 1;
  const DRY_RUN = true;

  const config = {
    headers: {
      'Content-Type': 'text/plain'
    },
    auth: { // TODO auth anpassen
      username: 'dsh',
      password: 'AKCp5ccv1eWCa8gV62LEbYGAt9CuXpsHiD3mXpCxsRqHDVddN8yfhvsbQpoRRbzxuZXzsNTz8'
    }
  };

  const foundItems = await getSnapshots(ARTIFACTORY_URL, config);
  const itemsToDelete = await calculateItemsToDelete(foundItems, NO_KEEP);
  await deleteItems(ARTIFACTORY_URL, itemsToDelete, config, DRY_RUN);
};

doCleanup()
  .then(() => console.log('Repository cleanup finished'))
  .catch(e => console.error(e));


