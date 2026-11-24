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
import {describe, it} from 'node:test';
import {isSnapshotRange, isSnapshotVersion, removeSnapshotSuffix} from '../../scripts/util/PackageJson.ts';

describe('PackageJson', () => {

  describe('isSnapshotVersion', () => {

    it('returns true for versions ending with a snapshot suffix', () => {
      for (const version of ['1.2.3-snapshot', '1.2.3-SNAPSHOT', '1.2.3-snapshot.19700101000000', 'npm:foo@1.2.3-snapshot', 'npm:@scope/foo@1.2.3-snapshot.20250101000000']) {
        assert.equal(isSnapshotVersion(version), true, version);
      }
    });

    it('returns false for other versions', () => {
      for (const version of ['1.2.3', '1.2.3-beta', '1.2.3-snapshot.1', '1.2.3-snapshot.19700101000000.1', '1.2.3-snapshot-1', '>=1.2.3-snapshot <1.2.3', 'link:../snapshot']) {
        assert.equal(isSnapshotVersion(version), false, version);
      }
    });
  });

  describe('isSnapshotRange', () => {

    it('returns true for ranges with a snapshot version as lower bound', () => {
      for (const range of ['>=1.2.3-snapshot <1.2.3', '>=1.2.3-snapshot<1.2.3', '>=1.2.3-snapshot.19700101000000 <1.2.3']) {
        assert.equal(isSnapshotRange(range), true, range);
      }
    });

    it('returns false for other ranges and versions', () => {
      for (const range of ['1.2.3-snapshot', '^1.2.3', '>=1.2.3 <2.0.0', '>=1.2.3-beta <1.2.3']) {
        assert.equal(isSnapshotRange(range), false, range);
      }
    });
  });

  describe('removeSnapshotSuffix', () => {

    it('removes the snapshot suffix', () => {
      assert.equal(removeSnapshotSuffix('1.2.3-snapshot'), '1.2.3');
      assert.equal(removeSnapshotSuffix('1.2.3-snapshot.19700101000000'), '1.2.3');
      assert.equal(removeSnapshotSuffix('core-23.2.0-SNAPSHOT.20251009202039'), 'core-23.2.0');
    });

    it('returns other versions unchanged', () => {
      assert.equal(removeSnapshotSuffix('1.2.3'), '1.2.3');
      assert.equal(removeSnapshotSuffix('1.2.3-beta'), '1.2.3-beta');
    });
  });
});
