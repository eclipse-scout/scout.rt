/*
 * Copyright (c) 2010, 2025 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */

import {TableSpecHelper} from '../../src/testing';
import {dataObjects, DoEntityWithContributions, LimitedResultInfoContributionDo, MaxRowCountContributionDo, scout, Table, TableMaxResultsHelper} from '../../src';

describe('TableMaxResultsHelper', () => {
  let table: Table;

  beforeEach(() => {
    setFixtures(sandbox());
    const session = sandboxSession();
    const helper = new TableSpecHelper(session);
    table = helper.createTable(helper.createModelFixture(1, 10));
  });

  describe('isLoadMoreDataPossible', () => {

    function isLoadMoreDataPossible(limitedResultInfoDo: LimitedResultInfoContributionDo): boolean {
      table.setResultInfo(limitedResultInfoDo);
      return scout.create(TableMaxResultsHelper).isLoadMoreDataPossible(table);
    }

    it('allows loading more data if result is limited', () => {
      expect(scout.create(TableMaxResultsHelper).isLoadMoreDataPossible(null)).toBeFalse();

      expect(isLoadMoreDataPossible({limitedResult: false})).toBeFalse();
      expect(isLoadMoreDataPossible({limitedResult: true})).toBeTrue();
      expect(isLoadMoreDataPossible({limitedResult: true, estimatedRowCount: 100})).toBeTrue();
      expect(isLoadMoreDataPossible({limitedResult: false, estimatedRowCount: 100})).toBeFalse();
    });

    it('does not allow loading more data if maxRowCount is reached', () => {
      expect(isLoadMoreDataPossible({limitedResult: false, maxRowCount: 10})).toBeFalse();
      expect(isLoadMoreDataPossible({limitedResult: false, maxRowCount: 5})).toBeFalse();
      expect(isLoadMoreDataPossible({limitedResult: false, maxRowCount: 11})).toBeFalse();
      expect(isLoadMoreDataPossible({limitedResult: true, maxRowCount: 10})).toBeFalse();
      expect(isLoadMoreDataPossible({limitedResult: true, maxRowCount: 5})).toBeFalse();
      expect(isLoadMoreDataPossible({limitedResult: true, maxRowCount: 11})).toBeTrue();
      expect(isLoadMoreDataPossible({limitedResult: false, maxRowCount: 10, estimatedRowCount: 100})).toBeFalse();
      expect(isLoadMoreDataPossible({limitedResult: false, maxRowCount: 5, estimatedRowCount: 100})).toBeFalse();
      expect(isLoadMoreDataPossible({limitedResult: false, maxRowCount: 11, estimatedRowCount: 100})).toBeFalse();
      expect(isLoadMoreDataPossible({limitedResult: true, maxRowCount: 10, estimatedRowCount: 100})).toBeFalse();
      expect(isLoadMoreDataPossible({limitedResult: true, maxRowCount: 5, estimatedRowCount: 100})).toBeFalse();
      expect(isLoadMoreDataPossible({limitedResult: true, maxRowCount: 11, estimatedRowCount: 100})).toBeTrue();
    });
  });

  describe('withMaxRowCountContribution', () => {
    it('adds no MaxRowCountContributionDo if maxRowCount is not set', () => {
      const dataObject: DoEntityWithContributions = {};

      scout.create(TableMaxResultsHelper).withMaxRowCountContribution(dataObject, table);
      expect(dataObjects.getContribution(MaxRowCountContributionDo, dataObject)).toBeNull();
    });

    it('adds a MaxRowCountContributionDo if maxRowCount is set', () => {
      const dataObject: DoEntityWithContributions = {};
      table.setMaxRowCount(42);

      scout.create(TableMaxResultsHelper).withMaxRowCountContribution(dataObject, table);
      expect(dataObjects.getContribution(MaxRowCountContributionDo, dataObject)).toEqual(scout.create(MaxRowCountContributionDo, {hint: 42}));
    });

    it('updates the MaxRowCountContributionDo if maxRowCount has changed', () => {
      const dataObject: DoEntityWithContributions = {_contributions: [scout.create(MaxRowCountContributionDo, {hint: 13})]};
      table.setMaxRowCount(42);

      scout.create(TableMaxResultsHelper).withMaxRowCountContribution(dataObject, table);
      expect(dataObjects.getContribution(MaxRowCountContributionDo, dataObject)).toEqual(scout.create(MaxRowCountContributionDo, {hint: 42}));
    });

    it('removes the MaxRowCountContributionDo if maxRowCount is no longer set', () => {
      const dataObject: DoEntityWithContributions = {_contributions: [scout.create(MaxRowCountContributionDo, {hint: 13})]};

      scout.create(TableMaxResultsHelper).withMaxRowCountContribution(dataObject, table);
      expect(dataObjects.getContribution(MaxRowCountContributionDo, dataObject)).toBeNull();
    });
  });
});
