#!/usr/bin/env node
/*
 * Copyright (c) 2014-2019 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */

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

const getSnapshots = async (artifactoryUrl, repoName, config, verbose) => {
  const AQL_API = 'api/search/aql'; // use aql for search
  const searchUrl = `${artifactoryUrl}${AQL_API}`;
  const query = `items.find({"repo":"${repoName}","name":{"$match":"*snapshot.*tgz"}})`;

  const snapshotMap = new Map();
  const response = await axios.post(searchUrl, query, config);
  const data = response.data;

  if (verbose) {
    console.log('found items:\n' + JSON.stringify(data, null, 2));
  }

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
};

const calculateItemsToDelete = async (itemMap, noToKeep) => {
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

const deleteItems = async (artifactoryUrl, items, config, dryrun) => {
  let success = true;
  if (!items || items.length === 0) {
    console.log('Nothing to cleanup');
  }
  for (const item of items) {
    const itemUrl = constructPath(artifactoryUrl, item);
    console.log(`delete: ${itemUrl}; ${dryrun ? 'dryrun=true' : ''}`);
    if (dryrun) {
      continue;
    }
    try {
      const response = await axios.delete(itemUrl, config);
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
};

const doCleanup = async () => {
  const yargs = require('yargs');
  const argv = yargs
    .option('apikey', {
      description: 'API Key for authentication',
      type: 'string'
    })
    .option('url', {
      description: 'URL of the artifactory',
      type: 'string'
    })
    .option('user', {
      description: 'username',
      type: 'string'
    })
    .option('pwd', {
      description: 'password',
      type: 'string'
    })
    .option('reponame', {
      description: 'name of the repository',
      type: 'string'
    })
    .option('dryrun', {
      description: 'If true, the command only indicates which artifacts would have been deleted.',
      type: 'boolean',
      default: false
    })
    .option('keep', {
      description: 'Number of Artifacts to keep',
      type: 'number',
      default: 5
    })
    .option('verbose', {
      description: 'More Logging',
      type: 'boolean',
      default: false
    })
    .argv;

  console.log(`Input arguments: url=${argv.url}; repo-name=${argv.reponame}; number of artifacts to keep=${argv.keep}; dry-run=${argv.dryrun}; verbose=${argv.verbose}`);

  if (!argv.reponame || !argv.url) {
    throw new Error('Please provide arguments for --url and --repo-name');
  }

  const headers = {
    'Content-Type': 'text/plain'
  };

  if (argv.apikey) {
    headers['X-JFrog-Art-Api'] = argv.apikey;
  }

  const config = {
    headers: headers
  };

  if (!argv.apikey && argv.user && argv.pwd) {
    config.auth = {
      username: argv.user,
      password: argv.pwd
    };
  }

  const foundItems = await getSnapshots(argv.url, argv.reponame, config, argv.verbose);
  const itemsToDelete = await calculateItemsToDelete(foundItems, argv.keep);
  await deleteItems(argv.url, itemsToDelete, config, argv.dryrun);
};

doCleanup()
  .then(() => console.log('Repository cleanup done'))
  .catch(e => {
    console.error('Repository cleanup failed');
    console.error(e);
    process.exit(1);
  });


