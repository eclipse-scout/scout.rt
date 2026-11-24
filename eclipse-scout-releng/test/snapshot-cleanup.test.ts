/*
 * Copyright (c) 2010, 2026 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * AI Disclosure: This file was partially AI-generated.
 * The AI-generated portions are made available under CC0-1.0
 * and not subject to the project's licence.
 *
 * SPDX-License-Identifier: EPL-2.0 and CC0-1.0
 */

import assert from 'node:assert/strict';
import {createServer} from 'node:http';
import {type AddressInfo} from 'node:net';
import {describe, it} from 'node:test';
import {doCleanup} from '../scripts/snapshot-cleanup.ts';

describe('snapshot-cleanup', () => {

  describe('doCleanup', () => {

    const searchResults = [
      {repo: 'npm-local', path: '@eclipse-scout/core/-/@eclipse-scout', name: 'core-1.0.0-snapshot.20250101000000.tgz', created: '2025-01-01T00:00:00Z'},
      {repo: 'npm-local', path: '@eclipse-scout/core/-/@eclipse-scout', name: 'core-1.0.0-snapshot.20250102000000.tgz', created: '2025-01-02T00:00:00Z'},
      {repo: 'npm-local', path: '.', name: 'other-1.0.0-snapshot.20250101000000.tgz', created: '2025-01-01T00:00:00Z'},
      {repo: 'npm-local', path: '.', name: 'other-1.0.0-snapshot.20250102000000.tgz', created: '2025-01-02T00:00:00Z'}
    ];

    const expectedRequests = [
      'POST /api/search/aql',
      'DELETE /npm-local/@eclipse-scout/core/-/@eclipse-scout/core-1.0.0-snapshot.20250101000000.tgz',
      'DELETE /npm-local/other-1.0.0-snapshot.20250101000000.tgz'
    ];

    it('fails for an invalid number of snapshots to keep', async () => {
      for (const keep of [NaN, -1, 1.5]) {
        await assert.rejects(doCleanup({url: 'http://127.0.0.1:1/', apikey: 'key', repoName: 'npm-local', keep}), /--keep must be a non-negative integer/, String(keep));
      }
    });

    it('builds search and delete urls when the url has a trailing slash', async () => {
      await withArtifactory(searchResults, async (baseUrl, requests) => {
        await doCleanup({url: `${baseUrl}/`, apikey: 'key', repoName: 'npm-local', keep: 1});
        assert.deepEqual(requests, expectedRequests);
      });
    });

    it('builds search and delete urls when the url has no trailing slash', async () => {
      await withArtifactory(searchResults, async (baseUrl, requests) => {
        await doCleanup({url: baseUrl, apikey: 'key', repoName: 'npm-local', keep: 1});
        assert.deepEqual(requests, expectedRequests);
      });
    });

    it('searches the snapshots of the given repository, also if its name contains quotes', async () => {
      await withArtifactory([], async (baseUrl, _requests, bodies) => {
        await doCleanup({url: baseUrl, apikey: 'key', repoName: 'npm-"local"', keep: 1});
        assert.equal(bodies[0], 'items.find({"repo":"npm-\\"local\\"","name":{"$match":"*snapshot.*tgz"}})');
      });
    });

    it('builds urls relative to a base path of the url', async () => {
      await withArtifactory(searchResults, async (baseUrl, requests) => {
        await doCleanup({url: `${baseUrl}/artifactory/`, apikey: 'key', repoName: 'npm-local', keep: 1});
        assert.deepEqual(requests, expectedRequests.map(request => request.replace(' /', ' /artifactory/')));
      });
    });
  });
});

/**
 * Starts a local HTTP server that answers the search request with the given results and every delete request with status 204.
 * The callback receives the base url of the server, the received requests as `<method> <path>` and their bodies.
 */
async function withArtifactory(searchResults: object[], callback: (baseUrl: string, requests: string[], bodies: string[]) => Promise<void>): Promise<void> {
  const requests: string[] = [];
  const bodies: string[] = [];
  const server = createServer((request, response) => {
    requests.push(`${request.method} ${request.url}`);
    let body = '';
    request.setEncoding('utf8');
    request.on('data', chunk => {
      body += chunk;
    });
    request.on('end', () => {
      bodies.push(body);
      if (request.method === 'POST') {
        response.setHeader('Content-Type', 'application/json');
        response.end(JSON.stringify({results: searchResults}));
      } else {
        response.statusCode = 204;
        response.end();
      }
    });
  });
  await new Promise<void>(resolve => server.listen(0, '127.0.0.1', resolve));
  const consoleLog = console.log;
  console.log = () => undefined;
  try {
    await callback(`http://127.0.0.1:${(server.address() as AddressInfo).port}`, requests, bodies);
  } finally {
    console.log = consoleLog;
    server.close();
  }
}
