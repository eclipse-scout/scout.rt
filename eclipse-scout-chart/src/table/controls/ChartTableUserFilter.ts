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
import {arrays, TableMatrix, TableUserFilter} from '@eclipse-scout/core';

export default class ChartTableUserFilter extends TableUserFilter {

  constructor() {
    super();
    this.filterType = ChartTableUserFilter.TYPE;
    this.text = null;
    this.xAxis = null;
    this.yAxis = null;
    this.filters = [];
    this.columnIdX = null;
    this.columnIdY = null;
  }

  static TYPE = 'CHART';

  createLabel() {
    return this.text;
  }

  /**
   * @override TableUserFilter.js
   */
  createFilterAddedEventData() {
    let data = super.createFilterAddedEventData();
    data.text = this.text;
    data.filters = this.filters;
    data.columnIdX = (this.xAxis && this.xAxis.column) ? this.xAxis.column.id : null;
    data.columnIdY = (this.yAxis && this.yAxis.column) ? this.yAxis.column.id : null;
    return data;
  }

  calculate() {
    let matrix = new TableMatrix(this.table, this.session);
    let columnX = this.table.columnById(this.columnIdX);
    let axisGroupX = columnX.createFilter().axisGroup();
    this.xAxis = matrix.addAxis(columnX, axisGroupX);
    if (this.columnIdY) {
      let columnY = this.table.columnById(this.columnIdY);
      let axisGroupY = columnY.createFilter().axisGroup();
      this.yAxis = matrix.addAxis(columnY, axisGroupY);
    }
    matrix.calculate();
  }

  accept(row) {
    if (!this.xAxis) {
      // Lazy calculation. It is not possible on init, because the table is not rendered yet.
      this.calculate();
    }
    let value = this.xAxis.column.cellValueOrTextForCalculation(row);
    let deterministicKeyX = this.xAxis.normDeterministic(value);

    if (!this.yAxis) {
      return (this.filters.filter(filter => filter.deterministicKey === deterministicKeyX).length);
    }
    value = this.yAxis.column.cellValueOrTextForCalculation(row);
    let deterministicKeyY = this.yAxis.normDeterministic(value);
    return (this.filters.filter(filter => arrays.equals(filter.deterministicKey, [deterministicKeyX, deterministicKeyY])).length);
  }
}
