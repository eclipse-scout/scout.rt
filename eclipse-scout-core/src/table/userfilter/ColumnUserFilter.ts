/*
 * Copyright (c) 2010-2022 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
import {ColumnUserFilterEventMap, comparators, Event, FilterFieldsGroupBox, strings, TableMatrix, TableRow, TableUserFilter} from '../../index';
import {TableMatrixDateGroup, TableMatrixKeyAxis, TableMatrixNumberGroup} from '../TableMatrix';
import {TableUserFilterAddedEventData, TableUserFilterRemovedEventData} from './TableUserFilter';
import {ColumnComparator} from '../columns/comparators';
import {EventMapOf, EventModel} from '../../events/EventEmitter';

export default class ColumnUserFilter extends TableUserFilter {
  declare eventMap: ColumnUserFilterEventMap;

  /**
   * This property is used to check early whether or not this filter can produce filter-fields.
   * Set this property to true in your sub-class, if it creates filter fields.
   */
  hasFilterFields: boolean;

  /**
   * array of (normalized) key, text composite
   */
  availableValues: ColumnUserFilterValues[];

  /**
   * array of (normalized) keys
   */
  selectedValues: (string | number)[];
  matrix: TableMatrix;
  xAxis: TableMatrixKeyAxis;

  constructor() {
    super();
    this.filterType = ColumnUserFilter.TYPE;
    this.hasFilterFields = false;
    this.availableValues = [];
    this.selectedValues = [];
  }

  static TYPE = 'column';

  axisGroup(): TableMatrixNumberGroup | TableMatrixDateGroup {
    return TableMatrix.NumberGroup.COUNT;
  }

  calculate() {
    let containsSelectedValue, reorderAxis;

    this.matrix = new TableMatrix(this.table, this.session);
    this.matrix.addData(this.column, TableMatrix.NumberGroup.COUNT);
    this.xAxis = this.matrix.addAxis(this.column, this.axisGroup());
    let cube = this.matrix.calculate();

    this.selectedValues.forEach(selectedValue => {
      containsSelectedValue = false;
      this.xAxis.some(key => {
        if (this.xAxis.keyToDeterministicKey(key) === selectedValue) {
          containsSelectedValue = true;
          return true;
        }
        return false;
      });

      if (!containsSelectedValue) {
        this.xAxis.push(this.xAxis.deterministicKeyToKey(selectedValue));
        reorderAxis = true;
      }
    });

    if (reorderAxis) {
      this.xAxis.reorder();
    }

    this.availableValues = [];
    this.xAxis.forEach(key => {
      let deterministicKey = this.xAxis.keyToDeterministicKey(key);
      let text = this.xAxis.format(key);
      let iconId: string = null;
      if (key !== null && this.xAxis.textIsIcon) {
        // Only display icon if textIsIcon (still display empty text if key is null)
        iconId = text;
        text = null;
      }
      let cubeValue = cube.getValue([key]);
      this.availableValues.push({
        key: deterministicKey,
        text: text,
        iconId: iconId,
        htmlEnabled: false,
        cssClass: null,
        count: cubeValue ? cubeValue[0] : 0
      });
    });
  }

  override createFilterAddedEventData(): TableUserFilterAddedEventData {
    let data = super.createFilterAddedEventData();
    data.columnId = this.column.id;
    data.selectedValues = this.selectedValues;
    return data;
  }

  override createFilterRemovedEventData(): TableUserFilterRemovedEventData {
    let data = super.createFilterRemovedEventData();
    data.columnId = this.column.id;
    return data;
  }

  createLabel(): string {
    if (this.column.headerHtmlEnabled) {
      let plainText = strings.plainText(this.column.text);
      return plainText.replace(/\n/g, ' ');
    }
    return this.column.text || '';
  }

  override createKey(): string {
    return this.column.id;
  }

  accept(row: TableRow): boolean {
    if (!this.xAxis) {
      // Lazy calculation. It is not possible on init, because the table is not rendered yet.
      this.calculate();
    }
    let
      acceptByTable = true,
      acceptByFields = true,
      value = this.column.cellValueOrTextForCalculation(row),
      deterministicKey = this.xAxis.normDeterministic(value);

    if (this.tableFilterActive()) {
      acceptByTable = this.selectedValues.indexOf(deterministicKey) > -1;
    }
    if (this.fieldsFilterActive()) {
      acceptByFields = this.acceptByFields(value, deterministicKey, row);
    }

    return acceptByTable && acceptByFields;
  }

  filterActive(): boolean {
    return this.tableFilterActive() || this.fieldsFilterActive();
  }

  tableFilterActive(): boolean {
    return this.selectedValues.length > 0;
  }

  triggerFilterFieldsChanged() {
    this.trigger('filterFieldsChanged');
  }

  override trigger<K extends string & keyof EventMapOf<ColumnUserFilter>>(type: K, eventOrModel?: Event | EventModel<EventMapOf<ColumnUserFilter>[K]>): EventMapOf<ColumnUserFilter>[K] {
    return super.trigger(type, eventOrModel);
  }

  /**
   * Returns whether or not the given key is accepted by the filter-fields in their current state.
   * The default impl. returns true.
   */
  acceptByFields(key: any, normKey: number | string, row: TableRow): boolean {
    return true;
  }

  /**
   * Returns whether or not filter-fields have an effect on the column-filter in their current state.
   * The default impl. returns false.
   */
  fieldsFilterActive(): boolean {
    return false;
  }

  /**
   * Adds filter fields for this type of column filter.
   * The default impl. adds no fields.
   */
  addFilterFields(groupBox: FilterFieldsGroupBox) {
    // NOP
  }

  /**
   * Called after filter group-box has been rendered.
   * Gives the filter impl. a chance to modify the rendered fields.
   * The default impl. does nothing.
   */
  modifyFilterFields() {
    // NOP
  }

  /**
   * Returns the title displayed above the filter fields.
   * The default impl. returns a null value, which means the title is not displayed.
   */
  filterFieldsTitle(): string {
    return null;
  }

  createComparator(): ColumnComparator {
    return comparators.NUMERIC;
  }
}

export type ColumnUserFilterValues = {
  key: string | number;
  text: string;
  iconId: string;
  htmlEnabled: boolean;
  cssClass: string;
  count: number;
};
