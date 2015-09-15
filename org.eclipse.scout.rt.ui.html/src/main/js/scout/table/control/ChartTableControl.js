// SCOUT GUI
// (c) Copyright 2013-2014, BSI Business Systems Integration AG

scout.ChartTableControl = function() {
  scout.ChartTableControl.parent.call(this);
  this.cssClass = 'chart';
};

scout.inherits(scout.ChartTableControl, scout.TableControl);

scout.ChartTableControl.FILTER_KEY = 'CHART';

scout.ChartTableControl.prototype._renderContent = function($parent) {
  this.$contentContainer = $parent.appendDiv('chart-container');

  // scrollbars
  scout.scrollbars.install(this.$contentContainer, this.session);

  // group functions for dates
  var dateGroup = [
      [scout.ChartTableControlMatrix.DateGroup.YEAR, this.session.text('ui.groupedByYear')],
      [scout.ChartTableControlMatrix.DateGroup.MONTH, this.session.text('ui.groupedByMonth')],
      [scout.ChartTableControlMatrix.DateGroup.WEEKDAY, this.session.text('ui.groupedByWeekday')]
    ],
    countDesc = this.session.text('ui.Count'),
    removeChart = null,
    columns = this.table.columns,
    xAxis,
    yAxis,
    that = this;

  this._filterResetListener = this.table.events.on(scout.Table.GUI_EVENT_FILTER_RESETTED, function(event) {
    that.$contentContainer.find('.main-chart.selected').removeClassSVG('selected');
  });

  // create container
  var $chartSelect = this.$contentContainer.appendDiv('chart-select');

  // create chart types for selection
  addSelectBar($chartSelect);
  addSelectStacked($chartSelect);
  addSelectLine($chartSelect);
  addSelectPie($chartSelect);
  addSelectScatter($chartSelect);

  // add addition rectangle for hover and event handling
  $('svg.select-chart')
    .appendSVG('rect', '', 'select-events')
    .attr('width', 60)
    .attr('height', 45)
    .attr('fill', 'none')
    .attr('pointer-events', 'all')
    .click(function() {
      chartSelect($(this).parent());
    })
    .click(drawChart);

  // first chart type is preselected
  $('svg.select-chart').first().addClassSVG('selected');

  // create container for x/y-axis
  var $xAxisSelect = this.$contentContainer.appendDiv('xaxis-select'),
    $yAxisSelect = this.$contentContainer.appendDiv('yaxis-select');

  // all x/y-axis for selection
  for (var c1 = 0; c1 < columns.length; c1++) {
    var column1 = columns[c1];

    if (column1.type === 'key' || column1.type === 'number' ) {
      continue;
    }

    if (column1.text === null || column1.text === undefined ||  column1.text === '') {
      continue;
    }

    var $div = $.makeDiv('select-axis', column1.text)
      .data('column', column1);

    if (column1.type === 'date') {
      $div.data('group', 0)
        .appendDiv('select-axis-group', dateGroup[0][1]);
    }

    $xAxisSelect.append($div);
    $yAxisSelect.append($div.clone(true));
  }

  // click handling for data
  $('.select-axis')
    .click(axisSelect)
    .click(drawChart);

  // find best x and y axis: best is 9 different entries
  var matrix = new scout.ChartTableControlMatrix(this.table, this.session),
    columnCount = matrix.columnCount(),
    comp = function(a, b) {
      return Math.abs(a[1] - 8) - Math.abs(b[1] - 8);
    };

  columnCount.sort(comp);

  $xAxisSelect.children().each(function() {
    if ($(this).data('column') === columnCount[0][0]) {
      $(this).addClass('selected');
    }
  });

  $yAxisSelect.children().each(function() {
    if ($(this).data('column') === columnCount[1][0]) {
      $(this).addClass('selected');
    }
  });

  // create container for data
  var $dataSelect = this.$contentContainer.appendDiv('data-select');
  $dataSelect.appendDiv('select-data data-count', countDesc)
    .data('column', -1);

  // all data for selection
  for (var c2 = 0; c2 < columns.length; c2++) {
    var column2 = columns[c2];

    if ((column2.type === 'number')) {
      $dataSelect.appendDiv('select-data data-sum', column2.text)
        .data('column', column2);
    }
  }

  // click handling for data
  $('.select-data')
    .click(dataSelect)
    .click(drawChart);

  // first data type is preselected
  $('.select-data').first().addClass('selected');

  // draw first chart
  var $chartMain = this.$contentContainer.appendSVG('svg', '', 'chart-main');
  drawChart();

  function addSelectBar($container) {
    var $svg = $container.appendSVG('svg', '', 'chart-bar select-chart');
    var show = [2, 4, 3, 3.5, 5];

    for (var s = 0; s < show.length; s++) {
      $svg.appendSVG('rect', '', 'select-fill')
        .attr('x', s * 14)
        .attr('y', 50 - show[s] * 9)
        .attr('width', 12)
        .attr('height', show[s] * 9);
    }
  }

  function addSelectStacked($container) {
    var $svg = $container.appendSVG('svg', '', 'chart-stacked select-chart'),
      show = [2, 4, 3.5, 5];

    for (var s = 0; s < show.length; s++) {
      $svg.appendSVG('rect', '', 'select-fill')
        .attr('x', 0)
        .attr('y', 16 + s * 9)
        .attr('width', show[s] * 14)
        .attr('height', 7);
    }
  }

  function addSelectLine($container) {
    var $svg = $container.appendSVG('svg', '', 'chart-line select-chart'),
      show = [0, 1.7, 1, 2, 1.5, 3],
      pathPoints = [];

    for (var s = 0; s < show.length; s++) {
      pathPoints.push(2 + (s * 14) + ',' + (45 - show[s] * 11));
    }

    $svg.appendSVG('path', '', 'select-fill-line').
    attr('d', 'M' + pathPoints.join('L'));
  }

  function addSelectPie($container) {
    var $svg = $container.appendSVG('svg', '', 'chart-pie select-chart'),
      show = [
        [0, 0.1],
        [0.1, 0.25],
        [0.25, 1]
      ];

    for (var s = 0; s < show.length; s++) {
      $svg.appendSVG('path', '', 'select-fill-pie').
      attr('d', pathSegment(37, 30, 24, show[s][0], show[s][1]));
    }
  }

  function addSelectScatter($container) {
    var $svg = $container.appendSVG('svg', '', 'chart-scatter select-chart');

    $svg.appendSVG('line', '', 'select-fill-line')
      .attr('x1', 3).attr('y1', 53)
      .attr('x2', 70).attr('y2', 53);

    $svg.appendSVG('line', '', 'select-fill-line')
      .attr('x1', 8).attr('y1', 12)
      .attr('x2', 8).attr('y2', 58);

    $svg.appendSVG('circle', '', 'select-fill')
      .attr('cx', 22).attr('cy', 40)
      .attr('r', 5);

    $svg.appendSVG('circle', '', 'select-fill')
      .attr('cx', 50).attr('cy', 26)
      .attr('r', 11);
  }

  function chartSelect($chart) {
    $chart.siblings().removeClassSVG('selected');
    $chart.addClassSVG('selected');

    if ($chart.hasClassSVG('chart-scatter')) {
      $yAxisSelect.animateAVCSD('width', 175);
    } else {
      $yAxisSelect.animateAVCSD('width', 0);
    }
  }

  function axisSelect() {
    var $axis = $(this),
      group = $axis.data('group');

    $axis.siblings().animateAVCSD('height', 30);

    if (group >= 0) {
      $axis.animateAVCSD('height', 42);

      if ($axis.hasClass('selected')) {
        var newGroup = (group + 1) % dateGroup.length;
        $axis.data('group', newGroup)
          .children('.select-axis-group').text(dateGroup[newGroup][1]);
      }
    }

    $axis.selectOne('selected');

  }

  function dataSelect() {
    var $data = $(this);

    if ($data.hasClass('selected')) {
      if ($data.hasClass('data-sum')) {
        $data.removeClass('data-sum').addClass('data-median');
      } else if ($data.hasClass('data-median')) {
        $data.removeClass('data-median').addClass('data-sum');
      }
    }

    $data.selectOne('selected');
  }

  function drawChart() {
    var $chart = $('.selected', $chartSelect);

    // remove axis and chart
    if (removeChart) {
      removeChart();
    }
    $chartMain.children('.main-axis, .main-axis-x, .main-axis-y')
      .animateSVG('opacity', 0, 250, $.removeThis);

    // find xAxis and dataAxis
    var axis = $('.selected', $xAxisSelect).data('column'),
      axisGroup = $('.selected', $xAxisSelect).data('group');

    var data = $('.selected', $dataSelect).data('column'),
      dataCount = $('.selected', $dataSelect).hasClass('data-count'),
      dataSum = $('.selected', $dataSelect).hasClass('data-sum');

    // build matrix
    var matrix = new scout.ChartTableControlMatrix(that.table, that.session),
      dataAxis = matrix.addData(data, dataCount ? -1 : (dataSum ? 1 : 2));

    var group = (axisGroup >= 0) ? dateGroup[axisGroup][0] : axisGroup;
    xAxis = matrix.addAxis(axis, group);

    // in case of scatter
    if ($chart.hasClassSVG('chart-scatter')) {
      var axis2 = $('.selected', $yAxisSelect).data('column'),
        axis2Group = $('.selected', $yAxisSelect).data('group');

      var group2 = (axis2Group >= 0) ? dateGroup[axis2Group][0] : axis2Group;
      yAxis = matrix.addAxis(axis2, group2);
    }

    // return if empty
    if (matrix.isEmpty()) {
      return false;
    }


    // calculate matrix
    var cube = matrix.calculateCube();

    // set max width
    $chartMain.css('width', '3000px');
    $chartMain.css('height', '1500px');


    // based on chart type: set class and draw chart
    if ($chart.hasClassSVG('chart-bar')) {
      $('.select-axis', $xAxisSelect).removeClass('axis-ver axis-around').addClass('axis-hor');
      drawBar(xAxis, dataAxis, cube);
    } else if ($chart.hasClassSVG('chart-stacked')) {
      $('.select-axis', $xAxisSelect).removeClass('axis-hor axis-around').addClass('axis-ver');
      drawStacked(xAxis, dataAxis, cube);
    } else if ($chart.hasClassSVG('chart-line')) {
      $('.select-axis', $xAxisSelect).removeClass('axis-ver axis-around').addClass('axis-hor');
      drawLine(xAxis, dataAxis, cube);
    } else if ($chart.hasClassSVG('chart-pie')) {
      $('.select-axis', $xAxisSelect).removeClass('axis-ver axis-hor').addClass('axis-around');
      drawPie(xAxis, dataAxis, cube);
    } else if ($chart.hasClassSVG('chart-scatter')) {
      $('.select-axis', $xAxisSelect).removeClass('axis-ver axis-around').addClass('axis-hor');
      $('.select-axis', $yAxisSelect).addClass('axis-up');
      drawScatter(xAxis, yAxis, dataAxis, cube);
    }

    // update
    $chartMain.children().promise().done(updateSize);

    return false;
  }

  function updateSize () {
    // adapt size of svg
    var box = $chartMain[0].getBBox();
    $chartMain.css('width', box.x + box.width);
    $chartMain.css('height', box.y + box.height);

    // update scrollbar
    scout.scrollbars.update(that.$contentContainer);
  }

  function drawBar(xAxis, dataAxis, cube) {
    // dimension functions
    var maxWidth = 0,
      width = Math.max(12, Math.min(800 / xAxis.length, 70)),
      x = function(i) {
        i = i === null ? xAxis.length : i;
        return 100 + i * width;
      },
      y = function(i) {
        return 280 - i / (dataAxis.max - 0) * 240;
      };

    // draw data-axis
    var labels = [0, dataAxis.max / 4, dataAxis.max / 2, dataAxis.max / 4 * 3, dataAxis.max];
    for (var l = 0; l < labels.length; l++) {
      var label = labels[l],
        text = dataAxis.format(label);

      drawAxisLine(x(0) - 10, y(label), x(xAxis.length ) + 7, y(label));
      drawAxisText(x(0) - 20, y(label), 'y', text);
    }

    // draw x-axis and values
    for (var a = 0; a < xAxis.length; a++) {
      var key = xAxis[a],
        mark = xAxis.format(key),
        value = cube.getValue([key])[0];

      // bbox is expensive, test only if there is a chance so draw label
      var $text = drawAxisText(x(a) + width / 2 - 1.5, y(0) + 14, 'x', mark),
        w = xAxis.length > 50 ? width * 10 : $text[0].getBBox().width;

      maxWidth = (w > maxWidth) ? w : maxWidth;

      $chartMain.appendSVG('rect', '', 'main-chart')
        .attr('x', x(a)).attr('y', y(0))
        .attr('width', width - 3).attr('height', 0)
        .delay(200)
        .animateSVG('height', 280 - y(value), 600)
        .animateSVG('y', y(value), 600)
        .attr('data-xAxis', key)
        .data('data-text', $text)
        .mouseenter(chartMouseenter)
        .mouseleave(chartMouseleave)
        .click(chartClick);
    }

    // in case of to many elements, hide or rotate label
    if (maxWidth > (width - 3) * 3) {
      $('.main-axis-x', $chartMain).attr('fill-opacity', 0);
    } else if (maxWidth > width * 1.2) {
      $('.main-axis-x', $chartMain).each(function () {
        $(this)
          .css('text-anchor', 'end')
          .attr('y', parseFloat($(this).attr('y')) - 4)
          .attr('x', parseFloat($(this).attr('x')) + 4)
          .attr('transform', 'rotate(-25 ' + $(this).attr('x') + ', ' + $(this).attr('y') + ')');
      });
    }

    // function for later remove
    removeChart = function() {
      $chartMain.children('.main-chart')
        .animateSVG('height', 0, 200)
        .animateSVG('y', y(0), 200, $.removeThis );
    };
  }

  function drawStacked(xAxis, dataAxis, cube) {
    // dimension functions
    var maxHeight = 0,
      maxWidth = Math.max(280, that.$contentContainer.width() - 700),
      height = Math.max(10, Math.min(240 / xAxis.length, 30)),
      x = function(i) {
        return 100 + i / dataAxis.max * maxWidth;
      },
      y = function(i) {
        i = i === null ? xAxis.length : i;
        return 50 + i * height;
      };

    // draw data-axis
    var labels = [0, dataAxis.max / 4, dataAxis.max / 2, dataAxis.max / 4 * 3, dataAxis.max];
    for (var l = 0; l < labels.length; l++) {
      var label = labels[l],
        text = dataAxis.format(label);

      drawAxisLine(x(label), y(0) - 10, x(label), y(xAxis.length) + 7);
      drawAxisText(x(label), y(0) - 20, 'x', text);
    }

    // draw x-axis and values
    for (var a = 0; a < xAxis.length; a++) {
      var key = xAxis[a],
        mark = xAxis.format(key),
        value = cube.getValue([key])[0];

      // bbox is expensive, test only if there is a chance so draw label
      var $text = drawAxisText(x(0) - 8, y(a) + height / 2, 'y', mark),
        h = xAxis.length > 50 ? height * 10 : $text[0].getBBox().height;

      maxHeight = (h > maxHeight) ? h : maxHeight;

      $chartMain.appendSVG('rect', '', 'main-chart')
        .attr('x', x(0)).attr('y', y(a))
        .attr('width', 0).attr('height', height - 3)
        .delay(200)
        .animateSVG('width', x(value) - 100)
        .attr('data-xAxis', key)
        .data('data-text', $text)
        .mouseenter(chartMouseenter)
        .mouseleave(chartMouseleave)
        .click(chartClick);
    }

    // in case of to many elements, hide label
    if (maxHeight > height) {
      $('.main-axis-y', $chartMain).attr('fill-opacity', 0);
    }

    // function for later remove
    removeChart = function() {
      $chartMain.children('.main-chart')
        .animateSVG('width', 0, 200, $.removeThis);
    };
  }

  function drawLine(xAxis, dataAxis, cube) {
    // chart only possible with 2 values
    if (xAxis.length <= 1) {
      return false;
    }

    // dimension functions
    var maxWidth = Math.max(280, that.$contentContainer.width() - 700),
      x = function(i) {
      i = i === null ? xAxis.max : i;
      return 100 + (i - xAxis.min) / (xAxis.max - xAxis.min) * maxWidth;
    },
      y = function(i) {
        return 280 - i / (dataAxis.max - 0) * 240;
      };

    // draw data-axis
    var labels = [0, dataAxis.max / 4, dataAxis.max / 2, dataAxis.max / 4 * 3, dataAxis.max];
    for (var l = 0; l < labels.length; l++) {
      var label = labels[l],
        text = dataAxis.format(label);

      drawAxisLine(x(xAxis.min) - 10, y(label), x(xAxis.max) + 10, y(label));
      drawAxisText(x(xAxis.min) - 20, y(label), 'y', text);
    }

    // draw x-axis
    var delta = xAxis.max - xAxis.min,
      labelsX;
    if (xAxis.length > 14) {
      labelsX = [xAxis.min, xAxis.min + delta / 4, xAxis.min + delta / 2,
        xAxis.min + delta / 4 * 3, xAxis.max
      ];
    } else {
      labelsX = xAxis;
    }

    for (var k = 0; k < labelsX.length; k++) {
      var labelX = labelsX[k],
        textX = xAxis.format(labelX);

      drawAxisLine(x(labelX), y(0) - 3, x(labelX), y(0) + 3);
      drawAxisText(x(labelX), y(0) + 14, 'x', textX);
    }

    // draw values
    for (var a = xAxis.min + 1; a <= xAxis.max; a++) {
      if (a === 0) {
        continue;
      }

      var key1 = a - 1,
        key2 = a,
        value1 = xAxis.indexOf(key1) == -1 ? 0 : cube.getValue([key1])[0],
        value2 = xAxis.indexOf(key2) == -1 ? 0 : cube.getValue([key2])[0];

      $chartMain.appendSVG('line', '', 'main-chart')
        .attr('x1', x(key1)).attr('y1', y(0))
        .attr('x2', x(key2)).attr('y2', y(0))
        .delay(200)
        .animateSVG('y1', y(value1), 600)
        .animateSVG('y2', y(value2), 600);
    }

    // function for later remove
    removeChart = function() {
      $chartMain.children('.main-chart')
        .animateSVG('y1', y(0), 200)
        .animateSVG('y2', y(0), 200, $.removeThis);
    };
  }

  function drawPie(xAxis, dataAxis, cube) {
    // circle for surrounding text, hehe: svg ;)
    $chartMain.appendSVG('path', 'ArcAxis', 'main-axis')
      .attr('fill', 'none')
      .attr('d', 'M 210 160 m 0, -110 a 110,110 0 1, 1 0,220 a 110,110 0 1, 1 0,-220');

    $chartMain.appendSVG('path', 'ArcAxisWide', 'main-axis')
      .attr('fill', 'none')
      .attr('d', 'M 210 160 m 0, -122 a 122,122 0 1, 1 0,244 a 122,122 0 1, 1 0,-244');


    var startAngle = 0,
      endAngle;

    var tweenIn = function(now, fx) {
      var start = this.getAttribute('data-start'),
        end = this.getAttribute('data-end');
      this.setAttribute('d', pathSegment(210, 160, 105, start * fx.pos, end * fx.pos));
    };

    var tweenOut = function(now, fx) {
      var start = this.getAttribute('data-start'),
        end = this.getAttribute('data-end');
      this.setAttribute('d', pathSegment(210, 160, 105, start * (1 - fx.pos), end * (1 - fx.pos)));
    };

    // find data
    var segments = [];
    for (var a = 0; a < xAxis.length; a++) {
      var k = xAxis[a],
        m = xAxis.format(k),
        v = cube.getValue([k])[0];

      segments.push([k, m, v]);
    }

    // order segments
    segments.sort(function(a, b){ return (b[2] - a[2]); });

    // collect small segmenets
    var TRESHOLD = 5;
    if (segments.length > TRESHOLD) {
      for (var s = segments.length - 1; s >= TRESHOLD; s--) {
        if (typeof segments[TRESHOLD - 1][0] === 'number') {
          segments[TRESHOLD - 1][0] = [segments[TRESHOLD - 1][0], segments[s][0]];
        } else {
          segments[TRESHOLD - 1][0].push(segments[s][0]);
        }

        segments[TRESHOLD - 1][2] += segments[s][2];
        segments.pop();
      }
      segments[TRESHOLD - 1][1] =  that.session.text('ui.otherValues');
    }

    for (var t = 0; t < segments.length; t++) {
      var key = JSON.stringify(segments[t][0]),
        mark = segments[t][1],
        value = segments[t][2];

      endAngle = startAngle + value / dataAxis.total;


      // -0.001, else: only 1 arc is not drawn, svg...
      if (endAngle === 1) {
        endAngle = endAngle - 0.001;
      }

      // arc segement
      var $arc = $chartMain.appendSVG('path', '', 'main-chart')
        .attr('data-start', startAngle)
        .attr('data-end', endAngle - 0.001)
        .delay(200)
        .animate({
          tabIndex: 0
        }, {
          step: tweenIn,
          duration: 600
        })
        .attr('data-xAxis', key)
        .click(chartClick);

      // labels
      var $label1 = $chartMain.appendSVG('text', '', 'main-axis-x')
        .appendSVG('textPath')
        .attrSVG('startOffset', (startAngle + endAngle) / 2 * 100 + '%')
        .attrXLINK('href', '#ArcAxis')
        .text(mark)
        .attr('opacity', 0)
        .delay(400).animateSVG('opacity', 1, 400);

      // data inside the arc
      var midPoint = (startAngle + (endAngle - startAngle) / 2) * 2 * Math.PI;

      var $label2 = $chartMain.appendSVG('text', '', 'main-axis')
        .attr('x', 210 + 70 * Math.sin(midPoint))
        .attr('y', 160 - 70 * Math.cos(midPoint))
        .attr('fill', '#fff')
        .text(Math.round(value / dataAxis.total * 100) + '%')
        .attr('opacity', 0)
        .delay(600).animateSVG('opacity', 1, 300);

      // handling of small arcs
      $arc
        .data('data-text', $label1)
        .mouseenter(chartMouseenter)
        .mouseleave(chartMouseleave);

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
    removeChart = function() {
      $chartMain.children('.main-chart')
        .animate({
          tabIndex: 0
        }, {
          step: tweenOut,
          complete: $.removeThis,
          duration: 200
        });
    };
  }

  function drawScatter(xAxis, yAxis, dataAxis, cube) {
    // dimension functions
    var maxWidth = Math.max(280, that.$contentContainer.width() - 860),
    x = function(i) {
      i = i === null ? xAxis.max : i;
      return 100 + (i - xAxis.min) / (xAxis.max - xAxis.min) * maxWidth;
    },
      y = function(i) {
        i = i === null ? yAxis.max : i;
        return 280 - (i - yAxis.min) / (yAxis.max - yAxis.min) * 240;
      };

    // draw x-axis
    var deltaX = xAxis.max - xAxis.min,
      labelsX;

    if (xAxis.length > 14) {
      labelsX = [xAxis.min, xAxis.min + deltaX / 4, xAxis.min + deltaX / 2, xAxis.min + deltaX / 4 * 3, xAxis.max];
    } else {
      labelsX = xAxis;
    }

    for (var k = 0; k < labelsX.length; k++) {
      var labelX = labelsX[k],
        textX = xAxis.format(labelX);

      drawAxisLine(x(labelX), y(yAxis.max) - 7, x(labelX), y(yAxis.min) + 3);
      drawAxisText(x(labelX), y(yAxis.min) + 14, 'x', textX);
    }

    // draw y-axis
    var deltaY = yAxis.max - yAxis.min,
      labelsY;

    if (yAxis.length > 14) {
      labelsY = [yAxis.min, yAxis.min + deltaY / 4, yAxis.min + deltaY / 2, yAxis.min + deltaY / 4 * 3, yAxis.max];
    } else {
      labelsY = yAxis;
    }

    for (var l = 0; l < labelsY.length; l++) {
      var labelY = labelsY[l],
        textY = yAxis.format(labelY);

      drawAxisLine(x(xAxis.min) - 10, y(labelY), x(xAxis.max), y(labelY));
      drawAxisText(x(xAxis.min) - 20, y(labelY), 'y', textY);
    }
    // draw values
    for (var a1 = 0; a1 < xAxis.length; a1++) {
      for (var a2 = 0; a2 < yAxis.length; a2++) {
        var key1 = xAxis[a1],
          key2 = yAxis[a2],
          testValue = cube.getValue([key1, key2]);

        if (testValue) {
          var value = testValue[0], r;

          if (value === null) {
            continue;
          } else if (dataAxis.max === dataAxis.min) {
            r = 40;
          } else {
            r = Math.max(Math.sqrt((value - dataAxis.min) / (dataAxis.max - dataAxis.min)) * 40, 10);
          }

          $chartMain.appendSVG('circle', '', 'main-chart')
            .attr('cx', x(key1))
            .attr('cy', y(key2))
            .attr('r', 0)
            .delay(200)
            .animateSVG('r', r, 600)
            .attr('data-xAxis', key1)
            .attr('data-yAxis', key2)
            .click(chartClick);
        }
      }
    }

    // function for later remove
    removeChart = function() {
      $chartMain.children('.main-chart')
        .animateSVG('r', 0, 200, $.removeThis);
    };
  }

  function drawAxisLine(x1, y1, x2, y2) {
    $chartMain.appendSVG('line', '', 'main-axis')
      .attr('x1', x1).attr('y1', y1)
      .attr('x2', x2).attr('y2', y2)
      .attr('opacity', 0)
      .delay(200).animateSVG('opacity', 1, 600);
  }

  function drawAxisText(x, y, c, t) {
    var $text = $chartMain.appendSVG('text', '', 'main-axis-' + c)
      .attr('x', x).attr('y', y)
      .text(t)
      .attr('opacity', 0);

    $text.delay(200).animateSVG('opacity', 1, 600);

    return $text;
  }

  function pathSegment(mx, my, r, start, end) {
    var s = start * 2 * Math.PI,
      e = end * 2 * Math.PI,
      pathString = '';

    pathString += 'M' + (mx + r * Math.sin(s)) + ',' + (my - r * Math.cos(s));
    pathString += 'A' + r + ', ' + r;
    pathString += (end - start < 0.5) ? ' 0 0,1 ' : ' 0 1,1 ';
    pathString += (mx + r * Math.sin(e)) + ',' + (my - r * Math.cos(e));
    pathString += 'L' + mx + ',' + my + 'Z';

    return pathString;
  }

  function chartMouseenter(event) {
    var $element = $(this),
      $text = $element.data('data-text');

    $element.data('data-store-opacity', $text.attr('fill-opacity'));
    $text.attr('fill-opacity', '1');
  }

  function chartMouseleave(event) {
    var $element = $(this),
      $text = $element.data('data-text');

    $text.attr('fill-opacity', $element.data('data-store-opacity'));
  }


  function chartClick(event) {
    var $clicked = $(this);

    // change state
    if (event.ctrlKey) {
      if ($clicked.hasClassSVG('selected')) {
        $clicked.removeClassSVG('selected');
      } else {
        $clicked.addClassSVG('selected');
      }
    } else {
      $clicked.addClassSVG('selected');
      $clicked.siblings('.main-chart').removeClassSVG('selected');
    }

    //  prepare filter
    var filters = [],
      oneDim = !$('.selected', $chartSelect).hasClassSVG('chart-scatter'),
      filterFunc;

    // find all filter
    // different data may be stored: undefined, arrays (of keys )and single numbers (keys)
    var readData = function (object, attribute)  {
      var a = object.attr(attribute);
      if (typeof a === undefined ) {
        return [null];
      } else {
        var n = parseFloat(a);
        if (isNaN(n)) {
          return JSON.parse(a);
        } else {
          return [n];
        }
      }
    };

    that.$contentContainer.find('.main-chart.selected').each(function() {
      var dX, dY;

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
      filterFunc = function($row) {
        var row = $row.data('row');
        var key = xAxis.column.cellValueForGrouping(row);
        var nX = xAxis.norm(key);

        if (oneDim) {
          return (filters.indexOf(nX) > -1);
        } else {
          key = yAxis.column.cellValueForGrouping(row);
          var nY = yAxis.norm(key);
          return (filters.indexOf(JSON.stringify([nX, nY])) > -1);
        }
      };

      var filter = that.table.getFilter(scout.ChartTableControl.FILTER_KEY) || {};
      filter.createLabel = function() {
        return that.tooltipText;
      };
      filter.accept = filterFunc;
      filter.createKey = function() {
        return scout.ChartTableControl.FILTER_KEY;
      };
      that.table.addFilter(filter);

    } else {
      that.table.removeFilterByKey(scout.ChartTableControl.FILTER_KEY);
    }

    that.table.filter();
  }
};

scout.ChartTableControl.prototype.onResize = function() {
  // update scrollbar
  if (this.$contentContainer && this.$contentContainer.length) {
    scout.scrollbars.update(this.$contentContainer);
  }
};


scout.ChartTableControl.prototype._removeContent = function() {
  scout.scrollbars.uninstall(this.$contentContainer, this.session);
  this.$contentContainer.remove();
  this.table.events.removeListener(this._filterResetListener);
};

scout.ChartTableControl.prototype.isContentAvailable = function() {
  return true;
};
