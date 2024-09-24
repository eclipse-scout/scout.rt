/*
 * Copyright (c) 2010, 2024 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {dataObjects, DoEntity, Table} from '../index';

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
   * Adds a DataObject contribution of type {@link MaxRowCountContributionDo} to the given dataObject.
   * @param dataObject The DataObject to which the contribution should be added.
   * @param table The table to read the maxRowCount property that should be used in the contribution.
   */
  addMaxRowCountContribution<T extends { _contributions?: DoEntity[] }>(dataObject: T, table: Table): T {
    const maxRowCountContribution = this.buildMaxRowCountContribution(table);
    if (maxRowCountContribution) {
      dataObject = dataObject || {} as T;
      // see ScoutDataObjectModule.DEFAULT_CONTRIBUTIONS_ATTRIBUTE_NAME
      dataObjects.addContribution(maxRowCountContribution, dataObject);
    }
    return dataObject;
  }

  /**
   * Reads the maximum number of rows for the given table and converts it to a {@link MaxRowCountContributionDo}.
   * @returns the {@link MaxRowCountContributionDo} if there is a valid maxRowCount for the given table or null if no row count constraint is used.
   */
  buildMaxRowCountContribution(table: Table): MaxRowCountContributionDo {
    const maxOutlineRowCount = this.getMaxTableRowCount(table);
    if (maxOutlineRowCount > 0) {
      return {
        _type: 'scout.MaxRowCountContribution',
        hint: maxOutlineRowCount
      };
    }
    return null;
  }

  /**
   * Gets the maximum number of rows for the given table.
   */
  getMaxTableRowCount(table: Table): number {
    return table.maxRowCount;
  }
}

export interface MaxRowCountContributionDo extends DoEntity {
  override?: number;
  hint?: number;
}

export interface LimitedResultInfoContributionDo extends DoEntity {
  limitedResult: boolean;
  maxRowCount?: number;
  estimatedRowCount?: number;
}
