/*
 * Copyright (c) 2010-2021 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
import {arrays, DateColumn, icons, NumberColumn, objects, scout, scrollbars, strings, styles, TableControl, TableMatrix, tooltips} from '@eclipse-scout/core';
import {Chart, ChartTableControlLayout, ChartTableUserFilter} from '../../index';
import $ from 'jquery';

export default class ChartTableControl extends TableControl {

  constructor() {
    super();
    this.tooltipText = '${textKey:ui.Chart}';
    this.chartAggregation = {
      modifier: TableMatrix.NumberGroup.COUNT
    };
    this.chartType = Chart.Type.BAR;
    this.oldChartType = null;
    this.chartColorScheme = 'chart-table-control';

    // chart config selection
    this.$chartSelect = null;
    this.$xAxisSelect = null;
    this.$yAxisSelect = null;
    this.$dataSelect = null;

    this.xAxis = null;
    this.yAxis = null;

    this.dateGroup = null;

    this._tableUpdatedHandler = this._onTableUpdated.bind(this);
    this._tableColumnStructureChangedHandler = this._onTableColumnStructureChanged.bind(this);
    this._chartValueClickedHandler = this._onChartValueClick.bind(this);
  }

  static DATE_GROUP_FLAG = 0x100;
  static MAX_AXIS_COUNT = 100;

  _init(model) {
    super._init(model);
    this.table.on('columnStructureChanged', this._tableColumnStructureChangedHandler);

    this.chart = scout.create('Chart', {
      parent: this
    });
  }

  _destroy() {
    this.table.off('columnStructureChanged', this._tableColumnStructureChangedHandler);
    super._destroy();
  }

  _computeEnabled(inheritAccessibility, parentEnabled) {
    if (!this._hasColumns() && !this.selected) {
      return false;
    }
    return super._computeEnabled(inheritAccessibility, parentEnabled);
  }

  _renderChart() {
    if (this.chart) {
      this.chart.render(this.$contentContainer);
      this.chart.$container.addClass(this.denseClass);
    }
  }

  _createLayout() {
    return new ChartTableControlLayout(this);
  }

  _renderChartType() {
    this._selectChartType();
    this.$yAxisSelect.toggleClass('hide', this.chartType !== Chart.Type.BUBBLE);
    this.$yAxisSelect.toggleClass('animated', scout.isOneOf(Chart.Type.BUBBLE, this.chartType, this.oldChartType) && !!this.oldChartType);
    this.$yAxisSelect.data('scroll-shadow').setVisible(false);
    this.$yAxisSelect.oneAnimationEnd(() => {
      scrollbars.update(this.$yAxisSelect);
      this.$yAxisSelect.data('scroll-shadow').setVisible(true);
      this.$yAxisSelect.removeClass('animated');
    });

    if (this.contentRendered) {
      this.chart.$container.animateAVCSD('opacity', 0, () => {
        this.chart.$container.css('opacity', 1);
        this._drawChart();
      });
    }
  }

  _selectChartType() {
    objects.values(this._chartTypeMap).forEach($element => {
      $element.removeClass('selected');
    });
    this._chartTypeMap[this.chartType].addClass('selected');
  }

  _renderChartGroup1() {
    this._renderChartGroup(1);
  }

  _renderChartGroup2() {
    this._renderChartGroup(2);
  }

  _renderChartGroup(groupId) {
    if (!this._hasColumns()) {
      return;
    }
    let groupName = 'chartGroup' + groupId;
    let map = '_' + groupName + 'Map';
    let chartGroup = this[groupName];
    if (chartGroup) {
      let $element = this[map][chartGroup.id];
      $element.siblings('.select-axis').animateAVCSD('height', 30);
      $element.selectOne('selected');

      if (chartGroup.modifier > 0) {
        let dateGroupIndex = chartGroup.modifier ^ ChartTableControl.DATE_GROUP_FLAG;
        $element.animateAVCSD('height', 42);
        $element.children('.select-axis-group').text(this.dateGroup[dateGroupIndex][1]);
      }
      if (this.contentRendered) {
        this._drawChart();
      }
    }
  }

  _renderChartAggregation() {
    let $element = this._aggregationMap[this.chartAggregation.id || 'all'];
    if ($element) {
      $element.selectOne('selected');
      $element
        .removeClass('data-sum')
        .removeClass('data-avg');
      $element.addClass(this._getAggregationCssClass());
      if (this.contentRendered) {
        this._drawChart();
      }
    }
  }

  _getAggregationCssClass() {
    switch (this.chartAggregation.modifier) {
      case TableMatrix.NumberGroup.COUNT:
        return 'data-count';
      case TableMatrix.NumberGroup.SUM:
        return 'data-sum';
      case TableMatrix.NumberGroup.AVG:
        return 'data-avg';
      default:
        return null;
    }
  }

  _renderChartSelect(cssClass, chartType, iconId) {
    let icon = scout.create('Icon', {
      parent: this,
      iconDesc: iconId,
      cssClass: cssClass
    });
    icon.render(this.$chartSelect);
    this.$contentContainer.one('remove', () => icon.destroy());
    let $iconContainer = icon.$container;
    $iconContainer
      .toggleClass('disabled', !this.enabledComputed || !this._hasColumns())
      .data('chartType', chartType);

    if (this.enabledComputed && this._hasColumns()) {
      $iconContainer.on('click', this._onClickChartType.bind(this));
    }

    this._chartTypeMap[chartType] = $iconContainer;
  }

  /**
   * Appends a chart selection divs to this.$contentContainer and sets the this.$chartSelect property.
   * */
  _renderChartSelectContainer() {
    // create container
    this.$chartSelect = this.$contentContainer.appendDiv('chart-select');

    // create chart types for selection
    this._chartTypeMap = {};

    let supportedChartTypes = this._getSupportedChartTypes();

    if (scout.isOneOf(Chart.Type.BAR, supportedChartTypes)) {
      this._renderChartSelect('chart-bar', Chart.Type.BAR, icons.DIAGRAM_BARS_VERTICAL);
    }
    if (scout.isOneOf(Chart.Type.BAR_HORIZONTAL, supportedChartTypes)) {
      this._renderChartSelect('chart-stacked', Chart.Type.BAR_HORIZONTAL, icons.DIAGRAM_BARS_HORIZONTAL);
    }
    if (scout.isOneOf(Chart.Type.LINE, supportedChartTypes)) {
      this._renderChartSelect('chart-line', Chart.Type.LINE, icons.DIAGRAM_LINE);
    }
    if (scout.isOneOf(Chart.Type.PIE, supportedChartTypes)) {
      this._renderChartSelect('chart-pie', Chart.Type.PIE, icons.DIAGRAM_PIE);
    }
    if (scout.isOneOf(Chart.Type.BUBBLE, supportedChartTypes)) {
      this._renderChartSelect('chart-bubble', Chart.Type.BUBBLE, icons.DIAGRAM_SCATTER);
    }
  }

  _getSupportedChartTypes() {
    return [
      Chart.Type.BAR,
      Chart.Type.BAR_HORIZONTAL,
      Chart.Type.LINE,
      Chart.Type.PIE,
      Chart.Type.BUBBLE
    ];
  }

  _onClickChartType(event) {
    let $target = $(event.currentTarget),
      chartType = $target.data('chartType');
    this.setChartType(chartType);
  }

  _onClickChartGroup(event) {
    let $target = $(event.currentTarget),
      groupId = $target.parent().data('groupId'),
      column = $target.data('column'),
      origModifier = $target.data('modifier');

    // do nothing when item is disabled
    if (!$target.isEnabled()) {
      return;
    }

    let modifier = $target.isSelected() ? this._nextDateModifier(origModifier) : origModifier;
    $target.data('modifier', modifier);

    let config = {
      id: column ? column.id : null,
      modifier: modifier
    };

    this._setChartGroup(groupId, config);
  }

  _onClickAggregation(event) {
    let $target = $(event.currentTarget);
    // update modifier
    let origModifier = $target.data('modifier');
    let modifier = $target.isSelected() ? this._nextModifier(origModifier) : origModifier;
    $target.data('modifier', modifier);

    let column = $target.data('column');
    let aggregation = {
      id: column ? column.id : null,
      modifier: modifier
    };

    this._setChartAggregation(aggregation);
  }

  _nextDateModifier(modifier) {
    switch (modifier) {
      case TableMatrix.DateGroup.DATE:
        return TableMatrix.DateGroup.MONTH;
      case TableMatrix.DateGroup.MONTH:
        return TableMatrix.DateGroup.WEEKDAY;
      case TableMatrix.DateGroup.WEEKDAY:
        return TableMatrix.DateGroup.YEAR;
      case TableMatrix.DateGroup.YEAR:
        return TableMatrix.DateGroup.DATE;
      default:
        return modifier;
    }
  }

  _nextModifier(modifier) {
    switch (modifier) {
      case TableMatrix.NumberGroup.SUM:
        return TableMatrix.NumberGroup.AVG;
      case TableMatrix.NumberGroup.AVG:
        return TableMatrix.NumberGroup.SUM;
      default:
        return modifier;
    }
  }

  _setChartAggregation(chartAggregation) {
    if (chartAggregation === this.chartAggregation) {
      return;
    }
    this._setProperty('chartAggregation', chartAggregation);
    if (this.contentRendered) {
      this._renderChartAggregation();
    }
  }

  _setChartGroup1(chartGroup) {
    this._setChartGroup(1, chartGroup);
  }

  _setChartGroup2(chartGroup) {
    this._setChartGroup(2, chartGroup);
  }

  _setChartGroup(groupId, chartGroup) {
    let propertyName = 'chartGroup' + groupId;
    this._changeProperty(propertyName, chartGroup);
  }

  _changeProperty(prop, value) {
    if (value === this[prop]) {
      return;
    }
    this._setProperty(prop, value);
    if (this.contentRendered) {
      this['_render' + prop.charAt(0).toUpperCase() + prop.slice(1)]();
    }
  }

  setChartType(chartType) {
    this.oldChartType = this.chartType;
    this.setProperty('chartType', chartType);
  }

  _hasColumns() {
    return this.table.columns.length !== 0;
  }

  _axisCount(columnCount, column) {
    let i, tmpColumn;
    for (i = 0; i < columnCount.length; i++) {
      tmpColumn = columnCount[i][0];
      if (tmpColumn === column) {
        return columnCount[i][1];
      }
    }
    return 0;
  }

  _plainAxisText(column, text) {
    if (column.headerHtmlEnabled) {
      let plainText = strings.plainText(text);
      return plainText.replace(/\n/g, ' ');
    }
    return text;
  }

  _renderContent($parent) {
    this.$contentContainer = $parent.appendDiv('chart-container');

    // scrollbars
    this._installScrollbars();

    this._renderChartSelectContainer();

    // group functions for dates
    this.dateGroup = [
      [TableMatrix.DateGroup.YEAR, this.session.text('ui.groupedByYear')],
      [TableMatrix.DateGroup.MONTH, this.session.text('ui.groupedByMonth')],
      [TableMatrix.DateGroup.WEEKDAY, this.session.text('ui.groupedByWeekday')],
      [TableMatrix.DateGroup.DATE, this.session.text('ui.groupedByDate')]
    ];

    // listeners
    this._filterResetListener = this.table.on('filterReset', event => {
      if (this.chart) {
        this.chart.setCheckedItems([]);
      }
    });

    this._addListeners();

    this._renderAxisSelectorsContainer();
    let columnCount = this._renderAxisSelectors();

    // draw first chart
    this._renderChart();

    this._initializeSelection(columnCount);

    this._renderChartParts();

    this._drawChart();
  }

  _addListeners() {
    this.table.on('rowsInserted', this._tableUpdatedHandler);
    this.table.on('rowsDeleted', this._tableUpdatedHandler);
    this.table.on('allRowsDeleted', this._tableUpdatedHandler);
    this.chart.on('valueClick', this._chartValueClickedHandler);
  }

  _renderAxisSelectorsContainer() {
    this.$axisSelectContainer = this.$contentContainer
      .appendDiv('axis-select-container');
  }

  _renderAxisSelectors() {
    // create container for x/y-axis
    this.$xAxisSelect = this.$axisSelectContainer
      .appendDiv('xaxis-select')
      .data('groupId', 1);
    scrollbars.install(this.$xAxisSelect, {
      parent: this,
      session: this.session,
      axis: 'y'
    });

    this.$yAxisSelect = this.$axisSelectContainer
      .appendDiv('yaxis-select')
      .data('groupId', 2);
    scrollbars.install(this.$yAxisSelect, {
      parent: this,
      session: this.session,
      axis: 'y'
    });

    // map for selection (column id, $element)
    this._chartGroup1Map = {};
    this._chartGroup2Map = {};

    // find best x and y axis: best is 9 different entries
    let matrix = new TableMatrix(this.table, this.session),
      columnCount = matrix.columnCount(false); // filterNumberColumns false: number columns will be filtered below
    columnCount.sort((a, b) => {
      return Math.abs(a[1] - 8) - Math.abs(b[1] - 8);
    });

    let axisCount, enabled,
      numberOfAxisItems = 0,
      columns = matrix.columns(false); // filterNumberColumns false: number columns will be filtered below

    // all x/y-axis for selection
    for (let c1 = 0; c1 < columns.length; c1++) {
      let content, $div, $yDiv,
        column1 = columns[c1];

      // Check if data-spread is too large. This is a problem in large tables where a column has unique values.
      // We cannot create DOM elements for each unique value because this causes all browser to stop script
      // execution. May be in a later release we could implement some sort of data aggregation, but this is not
      // a simple task on the UI layer, because it requires some know-how about the entity represented by the table,
      // which we don't have in the UI. Another possible solution: make the charts scrollable, however this is
      // probably not a good idea, because with a lot of data, the chart fails to provide an oversight over the data
      // when the user must scroll and only sees a small part of the chart.
      if (column1 instanceof DateColumn) {
        // dates are always aggregated and thus we must not check if the chart has "too much data".
        enabled = true;
      } else {
        axisCount = this._axisCount(columnCount, column1);
        enabled = (axisCount <= ChartTableControl.MAX_AXIS_COUNT);
      }

      content = this._axisContentForColumn(column1);

      $div = this.$contentContainer
        .makeDiv('select-axis', this._plainAxisText(column1, content.text))
        .data('column', column1)
        .setEnabled(enabled);

      if (!enabled) {
        if (this.chartGroup1 && this.chartGroup1.id === column1.id) {
          this.chartGroup1 = null;
          this.chartGroup2 = null;
        }
        if (this.chartGroup2 && this.chartGroup2.id === column1.id) {
          this.chartGroup2 = null;
        }
      }

      if (content.icon) {
        $div.addClass(content.icon.appendCssClass('font-icon'));
      }

      if (column1 instanceof DateColumn) {
        $div
          .data('modifier', TableMatrix.DateGroup.YEAR)
          .appendDiv('select-axis-group', this.dateGroup[0][1]);
      }

      // install click handler or tooltip
      if (enabled) {
        $div.on('click', this._onClickChartGroup.bind(this));
        tooltips.installForEllipsis($div, {
          parent: this
        });
      } else {
        tooltips.install($div, {
          parent: this,
          text: this.session.text('ui.TooMuchData')
        });
      }

      numberOfAxisItems++;
      $yDiv = $div.clone(true);
      this._chartGroup1Map[column1.id] = $div;
      this._chartGroup2Map[column1.id] = $yDiv;
      this.$xAxisSelect.append($div);
      this.$yAxisSelect.append($yDiv);
    }

    // map for selection (column id, $element)
    this._aggregationMap = {};

    if (this._hasColumns()) {
      // create container for data
      this.$dataSelect = this.$axisSelectContainer.appendDiv('data-select');
      scrollbars.install(this.$dataSelect, {
        parent: this,
        session: this.session,
        axis: 'y'
      });

      // add data-count for no column restriction (all columns)
      let countDesc = this.session.text('ui.Count');
      this._aggregationMap.all = this.$dataSelect
        .appendDiv('select-data data-count', countDesc)
        .data('column', null)
        .data('modifier', TableMatrix.NumberGroup.COUNT);

      // all data for selection
      for (let c2 = 0; c2 < columns.length; c2++) {
        let column2 = columns[c2];
        let fakeNumberLabelCol2 = c2 + 1;

        if (column2 instanceof NumberColumn) {
          let columnText;
          if (strings.hasText(column2.text)) {
            columnText = this._plainAxisText(column2, column2.text);
          } else if (strings.hasText(column2.headerTooltipText)) {
            columnText = column2.headerTooltipText;
          } else {
            columnText = '[' + fakeNumberLabelCol2 + ']';
          }

          this._aggregationMap[column2.id] = this.$dataSelect
            .appendDiv('select-data data-sum', columnText)
            .data('column', column2)
            .data('modifier', TableMatrix.NumberGroup.SUM);
        }
      }

      // click handling for data
      $('.select-data', this.$contentContainer)
        .on('click', this._onClickAggregation.bind(this));
    }

    return columnCount;
  }

  _initializeSelection(columnCount) {
    let $axisColumns;

    if (!this.chartType) {
      this.setChartType(Chart.Type.BAR);
    }

    // no id selected
    if (!this.chartAggregation || !this._aggregationMap[this.chartAggregation.id]) {
      this._setChartAggregation({
        id: null,
        modifier: TableMatrix.NumberGroup.COUNT
      });
    }

    // apply default selection
    if (!this.chartGroup1 || !this.chartGroup1.id || !this._chartGroup1Map[this.chartGroup1.id]) {
      $axisColumns = this.$xAxisSelect.children(':not(.disabled)');
      this._setDefaultSelectionForGroup(1, columnCount, $axisColumns, 0 /* only use the first column for the first group */);
    }
    if (!this.chartGroup2 || !this.chartGroup2.id || !this._chartGroup2Map[this.chartGroup2.id]) {
      $axisColumns = this.$yAxisSelect.children(':not(.disabled)');
      this._setDefaultSelectionForGroup(2, columnCount, $axisColumns, 1 /* try to use the second column for the second group (if available). Otherwise the first column is used. */);
    }
  }

  /**
   * Applies the default column selection for the specified chartGroup.
   * The implementation only considers columns that are part of the specified columnCount matrix and $candidates array.
   * From all these columns the last match that is lower or equal to the specified maxIndex is set as default chart group.
   *
   * @param {number} chartGroup The number of the chart group (1 or 2) for which the default column should be set.
   * @param {matrix} columnCount Column-count matrix as returned by TableMatrix#columnCount(). Holds possible grouping columns.
   * @param {array} $candidates jQuery array holding all axis columns that could be used as default.
   * @param {number} maxIndex The maximum column index to use as default column for the specified chartGroup.
   */
  _setDefaultSelectionForGroup(chartGroup, columnCount, $candidates, maxIndex) {
    let col = this._getDefaultSelectedColumn(columnCount, $candidates, maxIndex);
    if (col) {
      this._setChartGroup(chartGroup, this._getDefaultChartGroup(col));
    }
  }

  _getDefaultSelectedColumn(columnCount, $candidates, maxIndex) {
    let matchCounter = 0,
      curColumn,
      result;
    for (let j = 0; j < columnCount.length && matchCounter <= maxIndex; j++) {
      curColumn = columnCount[j][0];
      if (this._existsInAxisColumns($candidates, curColumn)) {
        result = curColumn; // remember possible result
        matchCounter++;
      }
    }
    return result;
  }

  _existsInAxisColumns($candidates, columnToSearch) {
    for (let i = 0; i < $candidates.length; i++) {
      if ($candidates.eq(i).data('column') === columnToSearch) {
        return true;
      }
    }
    return false;
  }

  _getDefaultChartGroup(column) {
    let modifier;
    if (column instanceof DateColumn) {
      modifier = 256;
    }
    return {
      id: column.id,
      modifier: modifier
    };
  }

  _renderChartParts() {
    this._renderChartType();
    this._renderChartAggregation();
    this._renderChartGroup1();
    this._renderChartGroup2();
  }

  _drawChart() {
    if (!this._hasColumns()) {
      this._hideChart();
      return;
    }

    let cube = this._calculateValues();

    if (cube.length) {
      this.chart.setVisible(true);
    } else {
      this._hideChart();
      return;
    }

    let config = {
      type: this.chartType,
      options: {
        handleResize: true,
        colorScheme: this.chartColorScheme,
        maxSegments: 5,
        plugins: {
          legend: {
            display: false
          }
        }
      }
    };

    let iconClasses = [];
    config.data = this._computeData(iconClasses, cube);
    this._adjustFont(config, iconClasses);

    this._adjustConfig(config);

    this.chart.setConfig(config);

    let checkedItems = this._computeCheckedItems(config.data.datasets[0].deterministicKeys);
    this.chart.setCheckedItems(checkedItems);
  }

  _hideChart() {
    this.chart.setConfig({
      type: this.chartType
    });
    this.chart.setVisible(false);
  }

  _getDatasetLabel() {
    let elem = this._aggregationMap[this.chartAggregation.id || 'all'];
    return (elem ? elem.text() : null) || this.session.text('ui.Value');
  }

  _calculateValues() {
    // build matrix
    let matrix = new TableMatrix(this.table, this.session);

    // aggregation (data axis)
    let tableData = this.chartAggregation.id ? this._aggregationMap[this.chartAggregation.id].data('column') : -1;
    matrix.addData(tableData, this.chartAggregation.modifier);

    // find xAxis
    if (this.chartGroup1) {
      let axis = this._chartGroup1Map[this.chartGroup1.id].data('column');
      this.xAxis = matrix.addAxis(axis, this.chartGroup1.modifier);
    }

    // find yAxis
    // in case of bubble
    if (this.chartType === Chart.Type.BUBBLE && this.chartGroup2) {
      let axis2 = this._chartGroup2Map[this.chartGroup2.id].data('column');
      this.yAxis = matrix.addAxis(axis2, this.chartGroup2.modifier);
    } else {
      this.yAxis = null;
    }

    // return not possible to draw chart
    if (matrix.isEmpty() || !matrix.isMatrixValid()) {
      return false;
    }

    // calculate matrix
    return matrix.calculate();
  }

  _getXAxis() {
    return this.xAxis;
  }

  _getYAxis() {
    return this.yAxis;
  }

  _computeData(iconClasses, cube) {
    let data = {
      datasets: [{
        label: this._getDatasetLabel()
      }]
    };
    if (!cube) {
      return data;
    }
    iconClasses = iconClasses || [];

    let segments = [];

    if (this.chartType === Chart.Type.BUBBLE) {
      segments = this._computeBubbleData(iconClasses, cube);
    } else {
      let xAxis = this._getXAxis();
      for (let x = 0; x < xAxis.length; x++) {
        let label,
          keyX = xAxis[x];
        if (xAxis.column instanceof NumberColumn) {
          // the axis will format numbers as two digit decimals and null/undefined as the text '-empty-' or something similar
          // only pass null/undefined to the axis as we want to leave the number format to the chart but need the '-empty-' string
          label = objects.isNullOrUndefined(keyX) ? xAxis.format(keyX) : keyX;
        } else {
          label = this._handleIconLabel(xAxis.format(keyX), xAxis, iconClasses);
        }
        segments.push({
          value: cube.getValue([keyX])[0],
          label: label,
          deterministicKey: xAxis.keyToDeterministicKey(keyX)
        });
      }
    }
    let dataset = data.datasets[0],
      labels = [];

    dataset.data = [];
    dataset.deterministicKeys = [];

    segments.forEach(elem => {
      dataset.data.push(elem.value);
      dataset.deterministicKeys.push(elem.deterministicKey);
      if (!objects.isNullOrUndefined(elem.label)) {
        labels.push(elem.label);
      }
    });

    if (labels.length) {
      data.labels = labels;
    }

    return data;
  }

  _computeBubbleData(iconClasses, cube) {
    if (!cube) {
      return [];
    }
    iconClasses = iconClasses || [];

    let xAxis = this._getXAxis(),
      yAxis = this._getYAxis(),
      segments = [];
    for (let x = 0; x < xAxis.length; x++) {
      let keyX = xAxis[x],
        xValue = keyX;
      this._handleIconLabel(xAxis.format(keyX), xAxis, iconClasses);
      if (!(xAxis.column instanceof NumberColumn) && xValue === null) {
        xValue = xAxis.max;
      }
      if (xAxis.column instanceof DateColumn) {
        xValue = xValue - xAxis.min;
      }
      for (let y = 0; y < yAxis.length; y++) {
        let keyY = yAxis[y],
          yValue = keyY,
          cubeValues = cube.getValue([keyX, keyY]);
        this._handleIconLabel(yAxis.format(keyY), yAxis, iconClasses);
        if (cubeValues && cubeValues.length) {
          if (!(yAxis.column instanceof NumberColumn) && yValue === null) {
            yValue = yAxis.max;
          }
          if (yAxis.column instanceof DateColumn) {
            yValue = yValue - yAxis.min;
          }
          segments.push({
            value: {
              x: xValue,
              y: yValue,
              z: cubeValues[0]
            },
            deterministicKey: [xAxis.keyToDeterministicKey(keyX), yAxis.keyToDeterministicKey(keyY)]
          });
        }
      }
    }
    return segments;
  }

  _handleIconLabel(label, axis, iconClasses) {
    if (axis && axis.textIsIcon) {
      let icon = icons.parseIconId(label);
      if (icon && icon.isFontIcon()) {
        iconClasses.push(...icon.appendCssClass('font-icon').split(' '));
        return icon.iconCharacter;
      }
    }
    return label;
  }

  _adjustFont(config, iconClasses) {
    if (!config || !iconClasses) {
      return;
    }

    iconClasses = iconClasses.filter((value, index, self) => {
      return self.indexOf(value) === index;
    });
    if (iconClasses.length) {
      let fontFamily = styles.get(iconClasses, 'font-family').fontFamily;
      if (this.chartType !== Chart.Type.PIE) {
        config.options = $.extend(true, {}, config.options, {
          scales: {
            x: {
              ticks: {
                font: {
                  family: fontFamily
                }
              }
            },
            y: {
              ticks: {
                font: {
                  family: fontFamily
                }
              }
            }
          }
        });
      }
      config.options = $.extend(true, {}, config.options, {
        plugins: {
          tooltip: {
            titleFont: {
              family: fontFamily
            }
          }
        }
      });
      config.options = $.extend(true, {}, config.options, {
        plugins: {
          datalabels: {
            font: {
              family: fontFamily
            }
          }
        }
      });
    }
  }

  _adjustLabels(config) {
    if (!config) {
      return;
    }

    let xAxis = this._getXAxis(),
      yAxis = this._getYAxis();
    if (this.chartType === Chart.Type.BUBBLE) {
      if (!(xAxis.column instanceof NumberColumn)) {
        config.options = $.extend(true, {}, config.options, {
          scales: {
            x: {
              ticks: {
                callback: label => this._formatLabel(label, xAxis)
              }
            }
          }
        });
      }
      if (!(yAxis.column instanceof NumberColumn)) {
        config.options = $.extend(true, {}, config.options, {
          scales: {
            y: {
              ticks: {
                callback: label => this._formatLabel(label, yAxis)
              }
            }
          }
        });
      }
    } else {
      if (xAxis.column instanceof NumberColumn) {
        config.options = $.extend(true, {}, config.options, {
          reformatLabels: true
        });
      }
    }
  }

  _formatLabel(label, axis) {
    if (axis) {
      if (axis.column instanceof DateColumn) {
        label = label + axis.min;
        if (label !== parseInt(label) || (axis.length < 2 && (label < axis.min || label > axis.max))) {
          return null;
        }
      }
      if (axis.indexOf(null) !== -1) {
        if (label === axis.max) {
          label = null;
        } else if (label > axis.max) {
          return null;
        }
      }
      label = axis.format(label);
      if (axis.textIsIcon) {
        let icon = icons.parseIconId(label);
        if (icon && icon.isFontIcon()) {
          label = icon.iconCharacter;
        }
      }
    }
    return label;
  }

  _adjustConfig(config) {
    if (!config) {
      return;
    }

    this._adjustLabels(config);
    this._adjustClickable(config);

    if (this.chartType === Chart.Type.BUBBLE) {
      this._adjustBubble(config);
    } else if (this.chartType === Chart.Type.PIE) {
      this._adjustPie(config);
    } else {
      this._adjustScales(config);
    }
  }

  _adjustClickable(config) {
    if (!config) {
      return;
    }

    if (this._isChartClickable()) {
      config.options = $.extend(true, {}, config.options, {
        clickable: true,
        checkable: true,
        otherSegmentClickable: true
      });
    }
  }

  _isChartClickable() {
    return true;
  }

  _adjustBubble(config) {
    if (!config || this.chartType !== Chart.Type.BUBBLE) {
      return;
    }

    config.options.bubble = $.extend(true, {}, config.options.bubble, {
      sizeOfLargestBubble: 25,
      minBubbleSize: 5
    });
  }

  _adjustPie(config) {
    if (!config || this.chartType !== Chart.Type.PIE) {
      return;
    }

    config.data.datasets[0].datalabels = {
      labels: {
        index: {
          display: 'auto',
          color: styles.get([this.chartColorScheme, this.chartType + '-chart', 'elements', 'label'], 'fill').fill,
          formatter: (value, context) => {
            return context.chart.data.labels[context.dataIndex];
          },
          anchor: 'end',
          align: 'end',
          clamp: true,
          offset: 10,
          padding: 4
        },
        labels: {}
      }
    };

    config.options = $.extend(true, {}, config.options, {
      plugins: {
        datalabels: {
          display: true
        }
      }
    });
    // Compensate the margin of the container so that the chart is always centered vertically
    let margin = this.chart.$container.cssMarginTop() - this.chart.$container.cssMarginBottom();
    config.options = $.extend(true, {}, config.options, {
      layout: {
        padding: {
          top: 30 + (Math.sign(margin) < 0 ? Math.abs(margin) : 0),
          bottom: 30 + (Math.sign(margin) > 0 ? margin : 0)
        }
      }
    });
  }

  _adjustScales(config) {
    if (!config) {
      return;
    }

    config.options = $.extend(true, {}, config.options, {
      scales: {
        x: {
          beginAtZero: true
        },
        y: {
          beginAtZero: true
        }
      }
    });
  }

  _computeCheckedItems(deterministicKeys) {
    if (!deterministicKeys) {
      return [];
    }

    let xAxis = this._getXAxis(),
      yAxis = this._getYAxis(),
      tableFilter = this.table.getFilter(ChartTableUserFilter.TYPE),
      filters = [],
      checkedIndices = [];

    if (tableFilter && (tableFilter.xAxis || {}).column === (xAxis || {}).column && (tableFilter.yAxis || {}).column === (yAxis || {}).column) {
      filters = tableFilter.filters;
    }

    deterministicKeys.forEach((deterministicKey, idx) => {
      if (filters.filter(filter => (Array.isArray(filter.deterministicKey) && Array.isArray(deterministicKey)) ? arrays.equals(filter.deterministicKey, deterministicKey) : filter.deterministicKey === deterministicKey).length) {
        checkedIndices.push(idx);
      }
    });
    let datasetIndex = 0;
    if (this.chartType === Chart.Type.PIE) {
      let maxSegments = this.chart.config.options.maxSegments,
        collapsedIndices = arrays.init(deterministicKeys.length - maxSegments).map((elem, idx) => idx + maxSegments);
      if (!arrays.containsAll(checkedIndices, collapsedIndices)) {
        arrays.remove(checkedIndices, maxSegments - 1);
      }
      arrays.removeAll(checkedIndices, collapsedIndices);

      // first dataset is hidden on pie charts
      datasetIndex = 1;
    }

    let checkedItems = [];
    if (checkedIndices.length) {
      checkedIndices.forEach(index => {
        checkedItems.push({
          datasetIndex: datasetIndex,
          dataIndex: index
        });
      });
    }

    return checkedItems;
  }

  _onChartValueClick() {
    //  prepare filter
    let filters = [];
    if (this.chart && this.chart.config.data) {
      let maxSegments = this.chart.config.options.maxSegments,
        dataset = this.chart.config.data.datasets[0],
        getFilters = index => ({deterministicKey: dataset.deterministicKeys[index]});
      if (this.chartType === Chart.Type.PIE) {
        getFilters = index => {
          index = parseInt(index);
          if (maxSegments && maxSegments === index + 1) {
            return arrays.init(dataset.deterministicKeys.length - index).map((elem, idx) => ({deterministicKey: dataset.deterministicKeys[idx + index]}));
          }
          return {deterministicKey: dataset.deterministicKeys[index]};
        };
      }

      let checkedIndices = this.chart.checkedItems.filter(item => item.datasetIndex === 0)
        .map(item => item.dataIndex);
      checkedIndices.forEach(index => {
        arrays.pushAll(filters, getFilters(index));
      });
    }

    //  filter function
    if (filters.length) {
      let filter = scout.create('ChartTableUserFilter', {
        session: this.session,
        table: this.table,
        text: this.tooltipText,
        xAxis: this._getXAxis(),
        yAxis: this._getYAxis(),
        filters: filters
      });

      this.table.addFilter(filter);
    } else {
      this.table.removeFilterByKey(ChartTableUserFilter.TYPE);
    }
  }

  _axisContentForColumn(column) {
    let icon,
      text = column.text;

    if (strings.hasText(text)) {
      return {
        text: text
      };
    }

    if (column.headerIconId) {
      icon = icons.parseIconId(column.headerIconId);
      if (icon.isFontIcon()) {
        return {
          text: icon.iconCharacter,
          icon: icon
        };
      }
    }

    if (column.headerTooltipText) {
      return {
        text: column.headerTooltipText
      };
    }

    return {
      text: '[' + (this.table.visibleColumns().indexOf(column) + 1) + ']'
    };
  }

  _removeContent() {
    this._removeScrollbars();
    this.$contentContainer.remove();
    this.chart.remove();
    this.table.events.removeListener(this._filterResetListener);
    this._removeListeners();
    this.oldChartType = null;
    this.recomputeEnabled();
  }

  _removeScrollbars() {
    this.$xAxisSelect.each((index, element) => {
      tooltips.uninstall($(element));
    });
    scrollbars.uninstall(this.$xAxisSelect, this.session);
    this.$yAxisSelect.each((index, element) => {
      tooltips.uninstall($(element));
    });
    scrollbars.uninstall(this.$yAxisSelect, this.session);
    scrollbars.uninstall(this.$dataSelect, this.session);
    this._uninstallScrollbars();
  }

  _removeListeners() {
    this.table.off('rowsInserted', this._tableUpdatedHandler);
    this.table.off('rowsDeleted', this._tableUpdatedHandler);
    this.table.off('allRowsDeleted', this._tableUpdatedHandler);
    this.chart.off('valueClick', this._chartValueClickedHandler);
  }

  _pathSegment(mx, my, r, start, end) {
    let s = start * 2 * Math.PI,
      e = end * 2 * Math.PI,
      pathString = '';

    pathString += 'M' + (mx + r * Math.sin(s)) + ',' + (my - r * Math.cos(s));
    pathString += 'A' + r + ', ' + r;
    pathString += (end - start < 0.5) ? ' 0 0,1 ' : ' 0 1,1 ';
    pathString += (mx + r * Math.sin(e)) + ',' + (my - r * Math.cos(e));
    pathString += 'L' + mx + ',' + my + 'Z';

    return pathString;
  }

  _onTableUpdated(event) {
    if (this._tableUpdatedTimeOutId) {
      return;
    }

    this._tableUpdatedTimeOutId = setTimeout(() => {
      this._tableUpdatedTimeOutId = null;

      if (!this.rendered) {
        return;
      }

      this._setChartGroup1(null);
      this._setChartGroup2(null);
      this.removeContent();
      this.renderContent();
    });
  }

  _onTableColumnStructureChanged() {
    this.recomputeEnabled();
    if (this.contentRendered && this.selected) {
      this._onTableUpdated();
    }
  }
}
