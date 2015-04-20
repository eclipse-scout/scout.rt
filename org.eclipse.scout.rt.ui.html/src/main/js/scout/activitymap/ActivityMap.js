// SCOUT GUI
// (c) Copyright 2013-2014, BSI Business Systems Integration AG

scout.ActivityMap = function() {
  scout.ActivityMap.parent.call(this);

  this.$container;
  this.$data;
  this.$data;
};
scout.inherits(scout.ActivityMap, scout.ModelAdapter);

scout.ActivityMap.prototype.init = function(model, session) {
  scout.ActivityMap.parent.prototype.init.call(this, model, session);
};

// TODO ActivityMap | Implement all _render* methods
scout.ActivityMap.prototype._render = function($parent) {
  this._$parent = $parent;
  this.$container = this._$parent.appendDiv('activity-map');
  this.$container.addClass('not-implemented'); // TODO Remove once implemented
  var layout = new scout.ActivityMapLayout(this);
  this.htmlComp = new scout.HtmlComponent(this.$container, this.session);
  this.htmlComp.setLayout(layout);
  this.htmlComp.pixelBasedSizing = false;

  this.$data = this.$container.appendDiv('activity-map-data');
  scout.scrollbars.install(this.$data);
  this.session.detachHelper.pushScrollable(this.$data);

  this._renderActivityRows();
};

scout.ActivityMap.prototype._remove = function() {
  this.session.detachHelper.removeScrollable(this.$data);
  scout.ActivityMap.parent.prototype._remove.call(this);
};

scout.ActivityMap.prototype._renderActivityRows = function() {
  var i, $row;
  for (i = 0; i < this.rows.length; i++) {
    $row = this._build$ActivityRow(this.rows[i], this.$data);
    $row.appendTo(this.$data);
  }
};

scout.ActivityMap.prototype._build$ActivityRow = function(row) {
  var i, $cell,
    $row = $.makeDiv('activity-row');
  $row.appendSpan().text('resourceId: ' + row.resourceId);
  for (i = 0; i < row.cells.length; i++) {
    $cell = this._build$ActivityCell(row.cells[i]);
    $cell.appendTo($row);
  }
  return $row;
};

scout.ActivityMap.prototype._build$ActivityCell = function(cell) {
  var i,
    $cell = $.makeDiv('activity-cell');
  $cell.text('minorValue: ' + cell.minorValue + ', majorValue: ' + cell.majorValue);
  return $cell;
};

scout.ActivityMap.prototype._renderPlanningMode = function() {
};

scout.ActivityMap.prototype._renderResourceIds = function() {
};

scout.ActivityMap.prototype._renderSelectedBeginTime = function() {
};

scout.ActivityMap.prototype._renderSelectedEndTime = function() {
};

scout.ActivityMap.prototype._renderSelectedResourceIds = function() {
};

scout.ActivityMap.prototype._renderSelectedActivityCell = function() {
};

scout.ActivityMap.prototype._renderTimeScale = function() {
};

scout.ActivityMap.prototype._renderDrawSections = function() {
};

scout.ActivityMap.prototype.onModelAction = function(event) {
  if (event.type === 'activityMapChanged') {
    this._onActivityMapChanged(event);
  } else {
    $.log.warn('Model event not handled. Widget: scout.Calendar. Event: ' + event.type + '.');
  }
};

scout.ActivityMap.prototype._onActivityMapChanged = function(event) {
  // TODO ActivityMap | Implement --> see JsonActivityMapEvent
};
