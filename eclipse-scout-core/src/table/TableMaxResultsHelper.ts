/*
 * Copyright (c) 2010, 2026 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {BaseDoEntity, dataObjects, DoEntity, scout, Table, TableReloadReason, typeName} from '../index';

export class TableMaxResultsHelper {

  /**
   * Checks if more data could be loaded into the table.
   * @param numRows The number of currently loaded rows of the table.
   * @param estRows The number of estimated rows that would be available.
   * @param maxRows The number of maximum rows that are allowed to be loaded.
   * @returns true if a estRows and maxRows are available and the currently loaded number of rows (numRows) is smaller than the two. This means more data could be loaded.
   */
  isLoadMoreDataPossible(numRows: number, estRows: number, maxRows: number): boolean {
    return estRows > 0 && maxRows > 0 && numRows < estRows && numRows < maxRows;
  }

  /**
   * Adds a DataObject contribution of type {@link MaxRowCountContributionDo} to the given dataObject if necessary, removes an existing contribution otherwise.
   * @param dataObject The DataObject to which the contribution should be added.
   * @param table The table for which the {@link MaxRowCountContributionDo} should be added.
   * @param reloadReason The reason of the reload for which the {@link MaxRowCountContributionDo} should be added.
   */
  withMaxRowCountContribution<T extends { _contributions?: DoEntity[] }>(dataObject: T, table: Table, reloadReason?: TableReloadReason): T {
    const maxRowCountContribution = this.buildMaxRowCountContribution(table, reloadReason);
    if (maxRowCountContribution) {
      dataObject = dataObject || {} as T;
      // see ScoutDataObjectModule.DEFAULT_CONTRIBUTIONS_ATTRIBUTE_NAME
      dataObjects.addContribution(maxRowCountContribution, dataObject);
    } else {
      dataObjects.removeContribution(MaxRowCountContributionDo, dataObject);
    }
    return dataObject;
  }

  /**
   * Reads the maximum number of rows for the given table and reason and converts it to a {@link MaxRowCountContributionDo}.
   * @param table The table for which the {@link MaxRowCountContributionDo} should be returned.
   * @param reloadReason The reason of the reload for which the {@link MaxRowCountContributionDo} should be added.
   * @returns the {@link MaxRowCountContributionDo} if there is a valid maxRowCount for the given table or null if no row count constraint is used.
   */
  buildMaxRowCountContribution(table: Table, reloadReason: TableReloadReason): MaxRowCountContributionDo {
    const maxOutlineRowCount = this.getMaxTableRowCount(table, reloadReason);
    if (maxOutlineRowCount > 0) {
      return scout.create(MaxRowCountContributionDo, {hint: maxOutlineRowCount});
    }
    return null;
  }

  /**
   * @param table The table for which the max table row count should be returned.
   * @param reloadReason The reason of the reload for which the max table row count should be returned.
   * @returns the maximum number of rows for the given table and reload reason.
   */
  getMaxTableRowCount(table: Table, reloadReason: TableReloadReason): number {
    return table?.maxRowCount;
  }
}

@typeName('scout.MaxRowCountContribution')
export class MaxRowCountContributionDo extends BaseDoEntity {
  override: number;
  hint: number;
}

export interface LimitedResultInfoContributionDo extends DoEntity {
  limitedResult: boolean;
  maxRowCount?: number;
  estimatedRowCount?: number;
}
