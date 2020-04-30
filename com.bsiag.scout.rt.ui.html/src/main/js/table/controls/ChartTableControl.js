/*
 * Copyright (c) 2014-2020 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the BSI CRM Software License v1.0
 * which accompanies this distribution as bsi-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
import {DateColumn, icons, NumberColumn, objects, scout, scrollbars, strings, TableControl, TableMatrix, tooltips} from '@eclipse-scout/core';
import {Chart, ChartTableControlLayout, ChartTableUserFilter} from '../../index';
import $ from 'jquery';

export default class ChartTableControl extends TableControl {

  constructor() {
    super();
    this.cssClass = 'chart';
    this.iconId = icons.CHART;
    this.tooltipText = '${textKey:ui.Chart}';
    this.chartAggregation = {
      modifier: TableMatrix.NumberGroup.COUNT
    };
    this.chartType = Chart.Type.BAR_VERTICAL_OLD;
    // TODO [15.4] cgu: make use of XyChartRenderer.js and remove duplicate code

    // chart config selection
    this.$chartSelect;
    this.$xAxisSelect;
    this.$yAxisSelect;
    // main chart svg element
    this.$chartMain;

    this.xAxis;
    this.yAxis;

    // remove chart function
    this.removeChart = null;

    this.dateGroup;

    this._tableUpdatedHandler = this._onTableUpdated.bind(this);
  }

  static DATE_GROUP_FLAG = 0x100;
  static MAX_AXIS_COUNT = 100;

  _init(model) {
    super._init(model);
    this.setEnabled(this._hasColumns());
  }

  _destroy() {
    super._destroy();
  }

  _createLayout() {
    return new ChartTableControlLayout(this);
  }

  _renderChartType() {
    this._selectChartType();
    this.$yAxisSelect.animateAVCSD('width', this.chartType === Chart.Type.SCATTER ? 175 : 0);
    if (this.contentRendered) {
      this._drawChart();
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
    let groupName = 'chartGroup' + groupId;
    let map = '_' + groupName + 'Map';
    let chartGroup = this[groupName];
    if (chartGroup) {
      let $element = this[map][chartGroup.id];
      $element.siblings().animateAVCSD('height', 30);
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

  _renderChartSelect(cssClass, chartType, renderSvgIcon) {
    let $svg = this.$chartSelect
      .appendSVG('svg', cssClass + ' select-chart')
      .toggleClass('disabled', !this.enabledComputed)
      .data('chartType', chartType);

    if (this.enabledComputed) {
      $svg.on('click', this._onClickChartType.bind(this));
    }

    this._chartTypeMap[chartType] = $svg;

    renderSvgIcon($svg);
  }

  /**
   * Appends a chart selection divs to this.$contentContainer and sets the this.$chartSelect property.
   * */
  _renderChartSelectContainer() {
    // create container
    this.$chartSelect = this.$contentContainer.appendDiv('chart-select');

    // create chart types for selection
    this._chartTypeMap = {};

    this._renderChartSelect('chart-bar', Chart.Type.BAR_VERTICAL_OLD, renderSvgIconBar);
    this._renderChartSelect('chart-stacked', Chart.Type.BAR_HORIZONTAL_OLD, renderSvgIconStacked);
    this._renderChartSelect('chart-line', Chart.Type.LINE_OLD, renderSvgIconLine);
    this._renderChartSelect('chart-pie', Chart.Type.PIE_OLD, renderSvgIconPie.bind(this));
    this._renderChartSelect('chart-scatter', Chart.Type.SCATTER, renderSvgIconScatter);

    function renderSvgIconBar($svg) {
      let show = [2, 4, 3, 3.5, 5];

      for (let s = 0; s < show.length; s++) {
        $svg.appendSVG('rect', 'select-fill')
          .attr('x', s * 14)
          .attr('y', 50 - show[s] * 9)
          .attr('width', 12)
          .attr('height', show[s] * 9);
      }
    }

    function renderSvgIconStacked($svg) {
      let show = [2, 4, 3.5, 5];

      for (let s = 0; s < show.length; s++) {
        $svg.appendSVG('rect', 'select-fill')
          .attr('x', 0)
          .attr('y', 16 + s * 9)
          .attr('width', show[s] * 14)
          .attr('height', 7);
      }
    }

    function renderSvgIconLine($svg) {
      let show = [0, 1.7, 1, 2, 1.5, 3],
        pathPoints = [];

      for (let s = 0; s < show.length; s++) {
        pathPoints.push(2 + (s * 14) + ',' + (45 - show[s] * 11));
      }

      $svg
        .appendSVG('path', 'select-fill-line')
        .attr('d', 'M' + pathPoints.join('L'));
    }

    function renderSvgIconPie($svg) {
      let show = [
        [0, 0.1],
        [0.1, 0.25],
        [0.25, 1]
      ];

      for (let s = 0; s < show.length; s++) {
        $svg
          .appendSVG('path', 'select-fill-pie')
          .attr('d', this._pathSegment(37, 30, 24, show[s][0], show[s][1]));
      }
    }

    function renderSvgIconScatter($svg) {
      $svg.appendSVG('line', 'select-fill-line')
        .attr('x1', 3).attr('y1', 53)
        .attr('x2', 70).attr('y2', 53);

      $svg.appendSVG('line', 'select-fill-line')
        .attr('x1', 8).attr('y1', 12)
        .attr('x2', 8).attr('y2', 58);

      $svg.appendSVG('circle', 'select-fill')
        .attr('cx', 22).attr('cy', 40)
        .attr('r', 5);

      $svg.appendSVG('circle', 'select-fill')
        .attr('cx', 50).attr('cy', 26)
        .attr('r', 11);
    }
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
    let $dataSelect,
      that = this;

    this.dateGroup = [
      [TableMatrix.DateGroup.YEAR, this.session.text('ui.groupedByYear')],
      [TableMatrix.DateGroup.MONTH, this.session.text('ui.groupedByMonth')],
      [TableMatrix.DateGroup.WEEKDAY, this.session.text('ui.groupedByWeekday')],
      [TableMatrix.DateGroup.DATE, this.session.text('ui.groupedByDate')]
    ];

    // listeners
    this._filterResetListener = this.table.on('filterReset', event => {
      $('.main-chart.selected', that.$contentContainer).removeClass('selected');
    });
    this.table.on('columnStructureChanged', this._tableUpdatedHandler);
    this.table.on('rowsInserted', this._tableUpdatedHandler);
    this.table.on('rowsDeleted', this._tableUpdatedHandler);
    this.table.on('allRowsDeleted', this._tableUpdatedHandler);

    // add addition rectangle for hover and event handling
    $('.select-chart', this.$contentContainer)
      .appendSVG('rect', 'select-events')
      .attr('width', 65)
      .attr('height', 60)
      .attr('fill', 'none')
      .attr('pointer-events', 'all');

    // create container for x/y-axis
    this.$xAxisSelect = this.$contentContainer
      .appendDiv('xaxis-select')
      .data('groupId', 1);
    this.$yAxisSelect = this.$contentContainer
      .appendDiv('yaxis-select')
      .data('groupId', 2);

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

      content = that._axisContentForColumn(column1);

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

    if (numberOfAxisItems < 2) {
      let $scatterSelect = this.$contentContainer.find('.chart-scatter.select-chart');
      if ($scatterSelect) {
        $scatterSelect.remove();
      }
    }

    // map for selection (column id, $element)
    this._aggregationMap = {};

    if (this._hasColumns()) {
      // create container for data
      $dataSelect = this.$contentContainer.appendDiv('data-select');

      // add data-count for no column restriction (all columns)
      let countDesc = this.session.text('ui.Count');
      this._aggregationMap.all = $dataSelect
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

          this._aggregationMap[column2.id] = $dataSelect
            .appendDiv('select-data data-sum', columnText)
            .data('column', column2)
            .data('modifier', TableMatrix.NumberGroup.SUM);
        }
      }

      // click handling for data
      $('.select-data', this.$contentContainer)
        .on('click', this._onClickAggregation.bind(this));
    }

    // draw first chart
    this.$chartMain = this.$contentContainer.appendSVG('svg', 'chart-main');

    this._initializeSelection(columnCount);
    this._renderChartType();
    this._renderChartAggregation();
    this._renderChartGroup1();
    this._renderChartGroup2();
    this._drawChart();
  }

  _initializeSelection(columnCount) {
    let $axisColumns;

    if (!this.chartType) {
      this.setChartType(Chart.Type.BAR_VERTICAL_OLD);
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
   * @param {matrix} columnCount Colum-count matrix as returned by TableMatrix#columnCount(). Holds possible grouping columns.
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

  _existsInAxisColumns($candidates, columToSearch) {
    for (let i = 0; i < $candidates.length; i++) {
      if ($candidates.eq(i).data('column') === columToSearch) {
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

  _drawStacked(xAxis, dataAxis, cube) {
    // dimension functions
    let maxHeight = 0,
      maxWidth = Math.max(280, this.$contentContainer.width() - 700),
      height = Math.max(10, Math.min(240 / xAxis.length, 30)),
      x = i => 100 + i / dataAxis.max * maxWidth,
      y = i => {
        i = i === null ? xAxis.length : i;
        return 50 + i * height;
      };

    // draw data-axis
    let labels = [0, dataAxis.max / 4, dataAxis.max / 2, dataAxis.max / 4 * 3, dataAxis.max];
    for (let l = 0; l < labels.length; l++) {
      let label = labels[l];

      this._drawAxisLine(x(label), y(0) - 10, x(label), y(xAxis.length) + 7);
      this._drawAxisText(x(label), y(0) - 20, 'x', dataAxis, label);
    }

    // draw x-axis and values
    for (let a = 0; a < xAxis.length; a++) {
      let key = xAxis[a],
        value = cube.getValue([key])[0];

      // bbox is expensive, test only if there is a chance so draw label
      let $text = this._drawAxisText(x(0) - 8, y(a) + height / 2, 'y', xAxis, key),
        h = xAxis.length > 50 ? height * 10 : $text[0].getBBox().height;

      maxHeight = (h > maxHeight) ? h : maxHeight;

      this.$chartMain.appendSVG('rect', 'main-chart')
        .attr('x', x(0)).attr('y', y(a))
        .attr('width', 0).attr('height', height - 3)
        .delay(200)
        .animateSVG('width', x(value) - 100)
        .attr('data-xAxis', key)
        .data('data-text', $text)
        .mouseenter(this._chartMouseenter)
        .mouseleave(this._chartMouseleave)
        .click(this._chartClick.bind(this));
    }

    // in case of to many elements, hide label
    if (maxHeight > height) {
      // IE doesn't support finding SVG elements by class. See comment in _drawBar()
      this.$chartMain.find('[class=main-axis-y]').attr('fill-opacity', 0);
    }

    // function for later remove
    this.removeChart = function() {
      this.$chartMain
        .children('.main-chart')
        .animateSVG('width', 0, 200, $.removeThis);
    };
  }

  _drawChart() {
    let group, group2;

    // remove axis and chart
    if (this.removeChart) {
      this.removeChart();
    }
    this.$chartMain.children('.main-axis, .main-axis-x, .main-axis-y')
      .animateSVG('opacity', 0, 250, $.removeThis);

    // build matrix
    let matrix = new TableMatrix(this.table, this.session);

    // aggregation (data axis)
    let data = this.chartAggregation.id ? this._aggregationMap[this.chartAggregation.id].data('column') : -1;
    let dataAxis = matrix.addData(data, this.chartAggregation.modifier);

    // find xAxis
    if (this.chartGroup1) {
      let axis = this._chartGroup1Map[this.chartGroup1.id].data('column');
      this.xAxis = matrix.addAxis(axis, this.chartGroup1.modifier);
    }

    // find yAxis
    // in case of scatter
    if (this.chartType === Chart.Type.SCATTER && this.chartGroup2) {
      let axis2 = this._chartGroup2Map[this.chartGroup2.id].data('column');
      this.yAxis = matrix.addAxis(axis2, this.chartGroup2.modifier);
    }

    // return not possible to draw chart
    if (matrix.isEmpty() || !matrix.isMatrixValid()) {
      return false;
    }

    // calculate matrix
    let cube = matrix.calculate();

    // set max width
    this.$chartMain.css('width', '3000px');
    this.$chartMain.css('height', '1500px');
    this.sizeDirty = true;

    // based on chart type: set class and draw chart
    if (this.chartGroup1) {
      this._updateAxisClass();
      if (this.chartType === Chart.Type.BAR_VERTICAL_OLD) {
        this._drawBar(this.xAxis, dataAxis, cube);
      } else if (this.chartType === Chart.Type.BAR_HORIZONTAL_OLD) {
        this._drawStacked(this.xAxis, dataAxis, cube);
      } else if (this.chartType === Chart.Type.LINE_OLD) {
        this._drawLine(this.xAxis, dataAxis, cube);
      } else if (this.chartType === Chart.Type.PIE_OLD) {
        this._drawPie(this.xAxis, dataAxis, cube);
      } else if (this.chartType === Chart.Type.SCATTER && this.chartGroup2) {
        this._drawScatter(this.xAxis, this.yAxis, dataAxis, cube, group2, group);
      }
    }

    // update
    this.$chartMain.children().promise().done(() => {
      // adapt size of svg
      try {
        // Firefox throws error when node is not in dom(already removed by navigating away). all other browser returns a boundingbox with 0
        let box = this.$chartMain[0].getBBox();
        this.$chartMain.css('width', box.x + box.width);
        this.$chartMain.css('height', box.y + box.height);

        // update scrollbar
        scrollbars.update(this.$contentContainer);
        this.sizeDirty = false;
      } catch (e) {
        // nop
      }
    });

    return false;
  }

  /**
   * Update the class on the selected axes depending on the chart type
   * */
  _updateAxisClass() {
    let $selectXAxis = this._chartGroup1Map[this.chartGroup1.id];
    if (this.chartType === Chart.Type.BAR_VERTICAL_OLD || this.chartType === Chart.Type.LINE_OLD || this.chartType === Chart.Type.SCATTER) {
      $selectXAxis.removeClass('axis-ver axis-around').addClass('axis-hor');
    } else if (this.chartType === Chart.Type.BAR_HORIZONTAL_OLD) {
      $selectXAxis.removeClass('axis-hor axis-around').addClass('axis-ver');
    } else if (this.chartType === Chart.Type.PIE_OLD) {
      $selectXAxis.removeClass('axis-ver axis-hor').addClass('axis-around');
    }

    if (this.chartType === Chart.Type.SCATTER) {
      this._chartGroup2Map[this.chartGroup2.id].addClass('axis-up');
    }

  }

  _drawBar(xAxis, dataAxis, cube) {
    // dimension functions
    let maxWidth = 0,
      width = Math.max(12, Math.min(800 / xAxis.length, 70)),
      x = i => {
        i = i === null ? xAxis.length : i;
        return 100 + i * width;
      },
      y = i => 280 - i / (dataAxis.max - 0) * 240;

    // draw data-axis
    let labels = [0, dataAxis.max / 4, dataAxis.max / 2, dataAxis.max / 4 * 3, dataAxis.max];
    for (let l = 0; l < labels.length; l++) {
      let label = labels[l];

      this._drawAxisLine(x(0) - 10, y(label), x(xAxis.length) + 7, y(label));
      this._drawAxisText(x(0) - 20, y(label), 'y', dataAxis, label);
    }

    // draw x-axis and values
    for (let a = 0; a < xAxis.length; a++) {
      let key = xAxis[a],
        value = cube.getValue([key])[0];

      // bbox is expensive, test only if there is a chance so draw label
      let $text = this._drawAxisText(x(a) + width / 2 - 1.5, y(0) + 14, 'x', xAxis, key),
        w = xAxis.length > 50 ? width * 10 : $text[0].getBBox().width;

      maxWidth = (w > maxWidth) ? w : maxWidth;

      this.$chartMain.appendSVG('rect', 'main-chart')
        .attr('x', x(a)).attr('y', y(0))
        .attr('width', width - 3).attr('height', 0)
        .delay(200)
        .animateSVG('height', 280 - y(value), 600)
        .animateSVG('y', y(value), 600)
        .attr('data-xAxis', key)
        .data('data-text', $text)
        .mouseenter(this._chartMouseenter)
        .mouseleave(this._chartMouseleave)
        .click(this._chartClick.bind(this));
    }

    // in case of to many elements, hide or rotate label
    // Note: do not use CSS style JQuery selector '.main-axis-x' here, since in IE the SVGElement doesn't have
    // the method getElementsByCssClass(). Instead we must use an attribute selector for the class attribute.
    // see: http://stackoverflow.com/questions/30885991/using-jquery-find-to-target-svg-elements-fails-in-ie-only
    if (maxWidth > (width - 3) * 3) {
      $('[class=main-axis-x]', this.$chartMain).attr('fill-opacity', 0);
    } else if (maxWidth > width) {
      $('[class=main-axis-x]', this.$chartMain).each(function() {
        $(this)
          .css('text-anchor', 'end')
          .attr('y', parseFloat($(this).attr('y')) - 4)
          .attr('x', parseFloat($(this).attr('x')) + 4)
          .attr('transform', 'rotate(-25 ' + $(this).attr('x') + ', ' + $(this).attr('y') + ')');
      });
    }

    // function for later remove
    this.removeChart = function() {
      this.$chartMain.children('.main-chart')
        .animateSVG('height', 0, 200)
        .animateSVG('y', y(0), 200, $.removeThis);
    };

  }

  _drawLine(xAxis, dataAxis, cube) {
    // chart only possible with 2 or more values
    if (xAxis.length <= 1) {
      return false;
    }

    // dimension functions
    let maxWidth = Math.max(280, this.$contentContainer.width() - 700),
      x = i => {
        i = i === null ? xAxis.max : i;
        return 100 + (i - xAxis.min) / (xAxis.max - xAxis.min) * maxWidth;
      },
      y = i => 280 - i / (dataAxis.max - 0) * 240;

    // draw data-axis
    let labels = [0, dataAxis.max / 4, dataAxis.max / 2, dataAxis.max / 4 * 3, dataAxis.max];
    for (let l = 0; l < labels.length; l++) {
      let label = labels[l];

      this._drawAxisLine(x(xAxis.min) - 10, y(label), x(xAxis.max) + 10, y(label));
      this._drawAxisText(x(xAxis.min) - 20, y(label), 'y', dataAxis, label);
    }

    // draw x-axis
    let labelsX;
    if (xAxis.length > 14) {
      let delta = xAxis.max - xAxis.min;
      labelsX = [xAxis.min,
        xAxis.min + delta / 4,
        xAxis.min + delta / 2,
        xAxis.min + delta / 4 * 3,
        xAxis.max
      ];
    } else {
      labelsX = xAxis;
    }

    // Given an index into the array of labels, get a position on the x-axis corresponding to the label index, not its
    // value. E.g. If the labels are [2, 3, 5], this returns equally spaced x positions instead of double spacing the
    // gap between 3 and 5.
    let xPos = labelIndex => xAxis.min + (xAxis.max - xAxis.min) * (labelIndex / (labelsX.length - 1));

    for (let k = 0; k < labelsX.length; k++) {
      let labelX = labelsX[k];

      this._drawAxisLine(x(xPos(k)), y(0) - 3, x(xPos(k)), y(0) + 3);
      this._drawAxisText(x(xPos(k)), y(0) + 14, 'x', xAxis, labelX);
    }

    // draw values
    let valueForNullLabel = cube.getValue([null]) ? cube.getValue([null])[0] : 0;
    for (let i = 1; i < labelsX.length; ++i) {
      let fromLabel = labelsX[i - 1];
      let toLabel = labelsX[i];

      // The label with a text like '-empty-' will have toLabel === null.
      let fromValue = xAxis.indexOf(fromLabel) === -1 ? valueForNullLabel : cube.getValue([fromLabel])[0];
      let toValue = xAxis.indexOf(toLabel) === -1 ? valueForNullLabel : cube.getValue([toLabel])[0];

      let fromXPos = xPos(i - 1);
      let toXPos = xPos(i);

      this.$chartMain.appendSVG('line', 'main-chart')
        .attr('x1', x(fromXPos)).attr('y1', y(0))
        .attr('x2', x(toXPos)).attr('y2', y(0))
        .delay(200)
        .animateSVG('y1', y(fromValue), 600)
        .animateSVG('y2', y(toValue), 600);
    }

    // function for later remove
    this.removeChart = function() {
      this.$chartMain.children('.main-chart')
        .animateSVG('y1', y(0), 200)
        .animateSVG('y2', y(0), 200, $.removeThis);
    };
  }

  _drawPie(xAxis, dataAxis, cube) {
    // circle for surrounding text, hehe: svg ;)
    this.$chartMain.appendSVG('path', 'main-axis')
      .attr('id', 'ArcAxis')
      .attr('fill', 'none')
      .attr('d', 'M 210 160 m 0, -110 a 110,110 0 1, 1 0,220 a 110,110 0 1, 1 0,-220');

    this.$chartMain.appendSVG('path', 'main-axis')
      .attr('id', 'ArcAxisWide')
      .attr('fill', 'none')
      .attr('d', 'M 210 160 m 0, -122 a 122,122 0 1, 1 0,244 a 122,122 0 1, 1 0,-244');

    let startAngle = 0,
      endAngle;

    let me = this;
    let tweenIn = function(now, fx) {
      let start = this.getAttribute('data-start'),
        end = this.getAttribute('data-end');
      this.setAttribute('d', me._pathSegment(210, 160, 105, start * fx.pos, end * fx.pos));
    };

    let tweenOut = function(now, fx) {
      let start = this.getAttribute('data-start'),
        end = this.getAttribute('data-end');
      this.setAttribute('d', me._pathSegment(210, 160, 105, start * (1 - fx.pos), end * (1 - fx.pos)));
    };

    // find data
    let segments = [];
    for (let a = 0; a < xAxis.length; a++) {
      let k = xAxis[a],
        m = xAxis.format(k),
        v = cube.getValue([k])[0];

      segments.push([k, m, v]);
    }

    // order segments
    segments.sort((a, b) => {
      return (b[2] - a[2]);
    });

    // collect small segments
    let TRESHOLD = 5;
    if (segments.length > TRESHOLD) {
      for (let s = segments.length - 1; s >= TRESHOLD; s--) {
        if (typeof segments[TRESHOLD - 1][0] === 'number') {
          segments[TRESHOLD - 1][0] = [segments[TRESHOLD - 1][0], segments[s][0]];
        } else { // i guess we assume that this else branch covers the 'is array' case (?)
          if (!segments[TRESHOLD - 1][0]) {
            segments[TRESHOLD - 1][0] = [segments[s][0]];
          } else {
            segments[TRESHOLD - 1][0].push(segments[s][0]);
          }
        }
        segments[TRESHOLD - 1][2] += segments[s][2];
        segments.pop();
      }
      segments[TRESHOLD - 1][1] = this.session.text('ui.OtherValues');
    }

    let roundingError = 0;

    for (let t = 0; t < segments.length; t++) {
      let icon,
        key = segments[t][0],
        mark = segments[t][1],
        value = segments[t][2];

      if (dataAxis.total > 0) {
        endAngle = startAngle + value / dataAxis.total;
      } else {
        endAngle = 1;
      }

      // -0.001, else: only 1 arc is not drawn, svg...
      if (endAngle === 1) {
        endAngle = endAngle - 0.001;
      }

      // arc segment
      let $arc = this.$chartMain.appendSVG('path', 'main-chart')
        .attr('data-start', startAngle)
        .attr('data-end', endAngle - 0.001)
        .delay(200)
        .animate({
          tabIndex: 0
        }, {
          step: tweenIn,
          duration: 600
        })
        .attr('data-xAxis', JSON.stringify(key))
        .click(this._chartClick.bind(this));

      // labels
      let $label1 = this.$chartMain.appendSVG('text', 'main-axis-x')
        .appendSVG('textPath')
        .attr('startOffset', (startAngle + endAngle) / 2 * 100 + '%')
        .attrXLINK('href', '#ArcAxis')
        .attr('opacity', 0)
        .delay(400).animateSVG('opacity', 1, 400);

      if (mark !== this.session.text('ui.OtherValues') && xAxis.textIsIcon) {
        icon = icons.parseIconId(mark);
      }
      if (icon && icon.isFontIcon()) {
        $label1.addClass(icon.appendCssClass('font-icon'));
        $label1.text(icon.iconCharacter);
      } else {
        $label1.text(mark);
      }

      // data inside the arc
      let midPoint = (startAngle + (endAngle - startAngle) / 2) * 2 * Math.PI;
      let roundedResult;
      if (dataAxis.total === 0) {
        roundedResult = 100;
      } else {
        // take into account the rounding error of the previous rounding
        // this guarantees that all rounded values add up to 100%
        let result = value / dataAxis.total * 100 - roundingError;
        roundedResult = Math.round(result);
        roundingError = roundedResult - result;
      }
      let percentage = roundedResult + '%';
      let $label2 = this.$chartMain.appendSVG('text', 'main-axis')
        .attr('x', 210 + 70 * Math.sin(midPoint))
        .attr('y', 160 - 70 * Math.cos(midPoint))
        .text(percentage)
        .attr('opacity', 0)
        .delay(600).animateSVG('opacity', 1, 300);

      // handling of small arcs
      $arc
        .data('data-text', $label1)
        .mouseenter(this._chartMouseenter)
        .mouseleave(this._chartMouseleave);

      if (endAngle - startAngle < 0.05) {
        $label1
          .attr('fill-opacity', 0)
          .attrXLINK('href', '#ArcAxisWide');
        $label2
          .attr('fill-opacity', 0);
      }

      startAngle = endAngle;
    }

    // function for later remove
    this.removeChart = function() {
      this.$chartMain.children('.main-chart')
        .animate({
          tabIndex: 0
        }, {
          step: tweenOut,
          complete: $.removeThis,
          duration: 200
        });
    };
  }

  _drawScatter(xAxis, yAxis, dataAxis, cube, yGroup, xGroup) {
    // dimension functions
    let maxWidth = Math.max(280, this.$contentContainer.width() - 860),
      yAxisSpecialDateTreating = yAxis.column instanceof DateColumn && yGroup === TableMatrix.DateGroup.YEAR && yAxis.indexOf(null) > -1,
      xAxisSpecialDateTreating = xAxis.column instanceof DateColumn && xGroup === TableMatrix.DateGroup.YEAR && xAxis.indexOf(null) > -1;

    // init drawY axis
    let deltaY = yAxis.max - yAxis.min,
      labelsY;

    if (yAxis.length > 14) {
      deltaY = Math.ceil(deltaY / 4) * 4;
      yAxis.max = yAxis.min + deltaY;
      if (yAxisSpecialDateTreating) {
        // if key value with undefined or null
        let clone2 = yAxis.slice(0, yAxis.length - 1);
        yAxis.min = Math.min.apply(null, clone2);
        deltaY = yAxis.max - yAxis.min;
        labelsY = [null, yAxis.min, yAxis.min + deltaY / 4, yAxis.min + deltaY / 2, yAxis.min + deltaY / 4 * 3, yAxis.max];
      } else {
        labelsY = [yAxis.min, yAxis.min + deltaY / 4, yAxis.min + deltaY / 2, yAxis.min + deltaY / 4 * 3, yAxis.max];
      }
    } else {
      if (yAxisSpecialDateTreating) {
        let clone4 = yAxis.slice(0, yAxis.length - 1);
        yAxis.min = Math.min.apply(null, clone4);
        deltaY = yAxis.max - yAxis.min;
      }
      labelsY = yAxis;
    }

    let y = i => {
      if (yAxis.length <= 1) {
        return 280;
      }
      if (yAxisSpecialDateTreating) {
        if (i === null) {
          return 40;
        }
        return 280 - (i - yAxis.min) / (yAxis.max - yAxis.min - 1) * (240 - (240 / labelsY.length));
      }
      i = i === null ? yAxis.max : i;
      return 280 - (i - yAxis.min) / (yAxis.max - yAxis.min) * 240;
    };

    // draw x-axis
    let deltaX = xAxis.max - xAxis.min,
      labelsX;

    if (xAxis.length > 14) {
      deltaX = Math.ceil(deltaX / 4) * 4;
      xAxis.max = xAxis.min + deltaX;
      if (xAxisSpecialDateTreating) {
        let clone = xAxis.slice(0, xAxis.length - 1);
        xAxis.min = Math.min.apply(null, clone);
        deltaX = xAxis.max - xAxis.min;
        labelsX = [null, xAxis.min, xAxis.min + deltaX / 4, xAxis.min + deltaX / 2, xAxis.min + deltaX / 4 * 3, xAxis.max];
      } else {
        labelsX = [xAxis.min, xAxis.min + deltaX / 4, xAxis.min + deltaX / 2, xAxis.min + deltaX / 4 * 3, xAxis.max];
      }
    } else {
      if (xAxisSpecialDateTreating) {
        let clone3 = xAxis.slice(0, xAxis.length - 1);
        xAxis.min = Math.min.apply(null, clone3);
      }
      labelsX = xAxis;
    }

    let x = i => {

      if (xAxis.length <= 1) {
        return 100;
      }
      if (xAxisSpecialDateTreating) {
        let offset = maxWidth / labelsX.length;
        if (i === null) {
          return 100;
        }
        return 100 + offset + (i - xAxis.min) / (xAxis.max - xAxis.min) * (maxWidth - offset);

      }
      i = i === null ? xAxis.max : i;
      return 100 + (i - xAxis.min) / (xAxis.max - xAxis.min) * maxWidth;
    };

    for (let k = 0; k < labelsX.length; k++) {
      let labelX = labelsX[k];

      this._drawAxisLine(x(labelX), y(yAxisSpecialDateTreating ? null : yAxis.max) - 7, x(labelX), y(yAxis.min) + 3);
      this._drawAxisText(x(labelX), y(yAxis.min) + 14, 'x', xAxis, labelX);
    }

    // draw y-axis

    for (let l = 0; l < labelsY.length; l++) {
      let labelY = labelsY[l];

      this._drawAxisLine(x(xAxisSpecialDateTreating ? null : xAxis.min) - 10, y(labelY), x(xAxis.max), y(labelY));
      this._drawAxisText(x(xAxisSpecialDateTreating ? null : xAxis.min) - 20, y(labelY), 'y', yAxis, labelY);
    }
    // draw values
    for (let a1 = 0; a1 < xAxis.length; a1++) {
      for (let a2 = 0; a2 < yAxis.length; a2++) {
        let key1 = xAxis[a1],
          key2 = yAxis[a2],
          testValue = cube.getValue([key1, key2]);

        if (testValue) {
          let value = testValue[0],
            r;

          if (value === null) {
            continue;
          } else if (dataAxis.max === dataAxis.min) {
            r = 40;
          } else {
            r = Math.max(Math.sqrt((value - dataAxis.min) / (dataAxis.max - dataAxis.min)) * 40, 10);
          }

          this.$chartMain.appendSVG('circle', 'main-chart')
            .attr('cx', x(key1))
            .attr('cy', y(key2))
            .attr('r', 0)
            .delay(200)
            .animateSVG('r', r, 600)
            .attr('data-xAxis', key1)
            .attr('data-yAxis', key2)
            .click(this._chartClick.bind(this));
        }
      }
    }

    // function for later remove
    this.removeChart = function() {
      this.$chartMain.children('.main-chart')
        .animateSVG('r', 0, 200, $.removeThis);
    };
  }

  _chartClick(event) {
    let $clicked = $(event.target);

    // change state
    if (event.ctrlKey) {
      if ($clicked.hasClass('selected')) {
        $clicked.removeClass('selected');
      } else {
        $clicked.addClass('selected');
      }
    } else {
      $clicked.addClass('selected');
      $clicked.siblings('.main-chart').removeClass('selected');
    }

    //  prepare filter
    let filters = [],
      oneDim = !$('.selected', this.$chartSelect).hasClass('chart-scatter');

    // find all filter
    // different data may be stored: undefined, arrays (of keys )and single numbers (keys)
    let readData = (object, attribute) => {
      let a = object.attr(attribute);
      if (a === undefined) {
        return [null];
      }
      let n = parseFloat(a);
      if (isNaN(n)) {
        return JSON.parse(a);
      }
      return [n];
    };

    $('.main-chart.selected', this.$contentContainer).each(function() {
      let dX, dY;

      if (oneDim) {
        dX = readData($(this), 'data-xAxis');
        filters = filters.concat(dX);
      } else {
        dX = readData($(this), 'data-xAxis');
        dY = readData($(this), 'data-yAxis');
        filters.push(JSON.stringify([dX[0], dY[0]]));
      }
    });

    //  filter function
    if (filters.length) {
      let filter = scout.create('ChartTableUserFilter', {
        session: this.session,
        table: this.table,
        text: this.tooltipText,
        xAxis: this.xAxis,
        yAxis: this.yAxis,
        filters: filters
      });

      this.table.addFilter(filter);
    } else {
      this.table.removeFilterByKey(ChartTableUserFilter.TYPE);
    }

    this.table.filter();
  }

  _chartMouseleave(event) {
    let $element = $(this),
      $text = $element.data('data-text');

    $text.attr('fill-opacity', $element.data('data-store-opacity'));
  }

  _chartMouseenter(event) {
    let $element = $(this),
      $text = $element.data('data-text');

    $element.data('data-store-opacity', $text.attr('fill-opacity'));
    $text.attr('fill-opacity', '1');
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
    this.$xAxisSelect.each((index, element) => {
      tooltips.uninstall($(element));
    });
    this.$yAxisSelect.each((index, element) => {
      tooltips.uninstall($(element));
    });
    this._uninstallScrollbars();
    this.$contentContainer.remove();
    this.table.events.removeListener(this._filterResetListener);
    this.table.off('columnStructureChanged', this._tableUpdatedHandler);
    this.table.off('rowsInserted', this._tableUpdatedHandler);
    this.table.off('rowsDeleted', this._tableUpdatedHandler);
    this.table.off('allRowsDeleted', this._tableUpdatedHandler);
  }

  _drawAxisText(x, y, c, axis, key) {
    let icon,
      text = axis.format(key),
      $text = this.$chartMain.appendSVG('text', 'main-axis-' + c)
        .attr('x', x).attr('y', y)
        .attr('opacity', 0);

    if (axis.textIsIcon) {
      icon = icons.parseIconId(text);
    }
    if (icon && icon.isFontIcon()) {
      $text.addClass(icon.appendCssClass('font-icon'));
      $text.text(icon.iconCharacter);
    } else {
      $text.text(text);
    }
    $text.delay(200).animateSVG('opacity', 1, 600);

    return $text;
  }

  _drawAxisLine(x1, y1, x2, y2) {
    this.$chartMain.appendSVG('line', 'main-axis')
      .attr('x1', x1).attr('y1', y1)
      .attr('x2', x2).attr('y2', y2)
      .attr('opacity', 0)
      .delay(200).animateSVG('opacity', 1, 600);
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

      this.setEnabled(this._hasColumns());
      if (!this.rendered) {
        return;
      }

      this._setChartGroup1(null);
      this._setChartGroup2(null);
      this.removeContent();
      this.renderContent();
    });
  }
}
