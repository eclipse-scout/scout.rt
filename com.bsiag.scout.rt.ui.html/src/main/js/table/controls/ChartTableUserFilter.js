/*
 * Copyright (c) 2014-2017 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
import {TableMatrix, TableUserFilter} from '@eclipse-scout/core';

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
    var data = super.createFilterAddedEventData();
    data.text = this.text;
    data.filters = this.filters;
    data.columnIdX = (this.xAxis && this.xAxis.column) ? this.xAxis.column.id : null;
    data.columnIdY = (this.yAxis && this.yAxis.column) ? this.yAxis.column.id : null;
    return data;
  }

  calculate() {
    var matrix = new TableMatrix(this.table, this.session);
    var columnX = this.table.columnById(this.columnIdX);
    var axisGroupX = columnX.createFilter().axisGroup();
    this.xAxis = matrix.addAxis(columnX, axisGroupX);
    if (this.columnIdY) {
      var columnY = this.table.columnById(this.columnIdY);
      var axisGroupY = columnY.createFilter().axisGroup();
      this.yAxis = matrix.addAxis(columnY, axisGroupY);
    }
    matrix.calculate();
  }

  accept(row) {
    if (!this.xAxis) {
      // Lazy calculation. It is not possible on init, because the table is not rendered yet.
      this.calculate();
    }
    var key = this.xAxis.column.cellValueOrTextForCalculation(row);
    var nX = this.xAxis.norm(key);

    if (!this.yAxis) {
      return (this.filters.indexOf(nX) > -1);
    }
    key = this.yAxis.column.cellValueOrTextForCalculation(row);
    var nY = this.yAxis.norm(key);
    return (this.filters.indexOf(JSON.stringify([nX, nY])) > -1);
  }
}
