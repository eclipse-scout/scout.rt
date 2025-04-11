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
import {dataObjects, DoEntityWithContributions, MaxRowCountContributionDo, scout, Table, TableMaxResultsHelper} from '../../src';

describe('TableMaxResultsHelper', () => {
  let table: Table;

  beforeEach(() => {
    setFixtures(sandbox());
    const session = sandboxSession();
    const helper = new TableSpecHelper(session);
    table = helper.createTable(helper.createModel([], []));
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
