/**
 * Layout manager for Scout Html UI forms. Don't forget to dispose the GridLayout when you dispose
 * the layouted container. Otherwise the grid layout still handles resize-events when the widget is
 * already disposed.
 */
scout.GridLayout = function($container) {
  if (!$container.data('columns')) {
    throw 'Missing attribute data-columns, cannot layout container';
  }
  this.$container = $container;
  this.columns = $container.data('columns');
  this.width = $container.parent().width();
  this.columnWidth = 0;
  this.gap = 15;
  // TODO AWE: (layout) gap und rowHeight fix? ist heute in Swing auch so aber via Swing environment überschreibbar.
  // vielleicht können wir das im CSS setzen und dort wieder auslesen? Sonst eine UIDefaults.js Datei
  // machen die pro Projekt überschrieben werden kann.
  this.rowHeight = 28;
};

// TODO AWE: generic toString method, move to *template.js?
scout.GridLayout.prototype.toString = function() {
  return 'GridLayout[columns=' + this.columns +
    ' width=' + this.width +
    ' columnWidth=' + this.columnWidth +
    ' gap=' + this.gap +
    ' rowHeight=' + this.rowHeight + ']';
};

scout.GridLayout.prototype.updateGridDimension = function() {
  // TODO AWE: (layout) padding/margin vom container berücksichtigen
  // TODO AWE: (layout) useUiHeight: was machen wir mit dem?
  var numGaps = this.columns - 1;
  var widthForColumns = this.width - numGaps * this.gap;
  this.columnWidth = Math.floor(widthForColumns / this.columns);
};

/**
 * When dimensions of container have changed, layout must be updated.
 *
 * @param force (optional)
 *   when force is set to true, container is laid out even when size has not changed.
 */
scout.GridLayout.prototype.updateLayout = function(force) {
  if (force === undefined) {
    force = false;
  }
  var oldWidth = this.width;
  var newWidth = this.$container.parent().width();
  console.info('container id=' + this.$container.attr('id') + ' | updateLayout force=' + force + ' oldWidth=' + oldWidth + ' newWidth=' + newWidth);
  if (force || oldWidth != newWidth) {
    console.info('container id=' + this.$container.attr('id') + ' | updateLayout --> call layout()');
    this.width = newWidth;
    this.layout();
  }
};

scout.GridLayout.prototype.layout = function() {
  this.updateGridDimension();
  this.$container.css('width', this.width + 'px');
  this.layoutFields();
  console.info('layout DONE container id=' + this.$container.attr('id'));
};

scout.GridLayout.prototype.layoutFields = function() {
  var that = this;
  this.$container.children('.form-field').each(function() {
    var $formField = $(this);
    var gridData = $formField.data('gridData');
    if (!gridData) {
      console.error('data-gridData is undefined! How is that possible!?');
      return;
    }
    var x = that.calcGridX(gridData.x);
    var y = that.calcGridY(gridData.y);
    var w = that.calcGridW(gridData.w);
    var h = that.calcGridH(gridData.h);
    $formField.
      css('left', x + 'px').
      css('top', y + 'px').
      css('width', w + 'px').
      css('height', h + 'px');

    // check if form-field has own grid-layout (must be updated)
    var gridLayout = $formField.data('gridLayout');
    if (gridLayout) {
      gridLayout.updateLayout();
    }
  });
};

scout.GridLayout.prototype.calcGridX = function(x) {
  return x * (this.columnWidth + this.gap);
};

scout.GridLayout.prototype.calcGridY = function(y) {
  return y * (this.rowHeight + this.gap);
};

scout.GridLayout.prototype.calcGridW = function(columns) {
  return columns * this.columnWidth + (columns - 1) * this.gap;
};

scout.GridLayout.prototype.calcGridH = function(rows) {
  return rows * this.rowHeight + (rows - 1) * this.gap;
};

