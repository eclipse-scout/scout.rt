/*
 * Copyright (c) 2014-2018 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
import {comparators, EventSupport, strings, TableMatrix, TableUserFilter} from '../../index';

export default class ColumnUserFilter extends TableUserFilter {

  constructor() {
    super();
    this.filterType = ColumnUserFilter.TYPE;
    this.events = new EventSupport();

    /**
     * This property is used to check early whether or not this filter can produce filter-fields.
     * Set this property to true in your sub-class, if it creates filter fields.
     */
    this.hasFilterFields = false;

    /**
     * array of (normalized) key, text composite
     */
    this.availableValues = [];

    /**
     * array of (normalized) keys
     */
    this.selectedValues = [];
  }

  static TYPE = 'column';

  axisGroup() {
    return TableMatrix.NumberGroup.COUNT;
  }

  calculate() {
    let containsSelectedValue, reorderAxis;

    this.matrix = new TableMatrix(this.table, this.session);
    this.matrix.addData(this.column, TableMatrix.NumberGroup.COUNT);
    this.xAxis = this.matrix.addAxis(this.column, this.axisGroup());
    let cube = this.matrix.calculate();

    this.selectedValues.forEach(function(selectedValue) {
      containsSelectedValue = false;
      this.xAxis.some(key => {
        if (this.xAxis.keyToDeterministicKey(key) === selectedValue) {
          containsSelectedValue = true;
          return true;
        }
        return false;
      }, this);

      if (!containsSelectedValue) {
        this.xAxis.push(this.xAxis.deterministicKeyToKey(selectedValue));
        reorderAxis = true;
      }
    }, this);

    if (reorderAxis) {
      this.xAxis.reorder();
    }

    let text, deterministicKey, cubeValue, iconId;
    this.availableValues = [];
    this.xAxis.forEach(function(key) {
      deterministicKey = this.xAxis.keyToDeterministicKey(key);
      text = this.xAxis.format(key);
      iconId = null;
      if (key !== null && this.xAxis.textIsIcon) {
        // Only display icon if textIsIcon (still display empty text if key is null)
        iconId = text;
        text = null;
      }
      cubeValue = cube.getValue([key]);
      this.availableValues.push({
        key: deterministicKey,
        text: text,
        iconId: iconId,
        htmlEnabled: false,
        cssClass: null,
        count: cubeValue ? cubeValue[0] : 0
      });
    }, this);
  }

  /**
   * @override TableUserFilter.js
   */
  createFilterAddedEventData() {
    let data = super.createFilterAddedEventData();
    data.columnId = this.column.id;
    data.selectedValues = this.selectedValues;
    return data;
  }

  createFilterRemovedEventData() {
    let data = super.createFilterRemovedEventData();
    data.columnId = this.column.id;
    return data;
  }

  createLabel() {
    if (this.column.headerHtmlEnabled) {
      let plainText = strings.plainText(this.column.text);
      return plainText.replace(/\n/g, ' ');
    }
    return this.column.text || '';
  }

  createKey() {
    return this.column.id;
  }

  accept(row) {
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

  filterActive() {
    return this.tableFilterActive() || this.fieldsFilterActive();
  }

  tableFilterActive() {
    return this.selectedValues.length > 0;
  }

  triggerFilterFieldsChanged(event) {
    this.events.trigger('filterFieldsChanged', event);
  }

  on(type, func) {
    this.events.on(type, func);
  }

  off(type, func) {
    this.events.off(type, func);
  }

  /**
   * Returns whether or not the given key is accepted by the filter-fields in their current state.
   * The default impl. returns true.
   */
  acceptByFields(key, normKey, row) {
    return true;
  }

  /**
   * Returns whether or not filter-fields have an effect on the column-filter in their current state.
   * The default impl. returns false.
   */
  fieldsFilterActive() {
    return false;
  }

  /**
   * Adds filter fields for this type of column filter.
   * The default impl. adds no fields.
   *
   * @param groupBox FilterFieldsGroupBox
   */
  addFilterFields(groupBox) {
    // NOP
  }

  /**
   * Called after filter group-box has been rendered. Gives the filter impl. a chance to
   * modify the rendered fields. The default impl. does nothing.
   */
  modifyFilterFields() {
    // NOP
  }

  /**
   * Returns the title displayed above the filter fields.
   * The default impl. returns a null value, which means the title is not displayed.
   */
  filterFieldsTitle() {
    return null;
  }

  createComparator() {
    return comparators.NUMERIC;
  }
}
