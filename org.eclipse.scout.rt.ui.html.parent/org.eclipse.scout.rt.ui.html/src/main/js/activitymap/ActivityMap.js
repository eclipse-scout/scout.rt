// SCOUT GUI
// (c) Copyright 2013-2014, BSI Business Systems Integration AG

scout.ActivityMap = function() {
  scout.ActivityMap.parent.call(this);

  this.$container;
  this.$data;
  this._$scrollable;
};
scout.inherits(scout.ActivityMap, scout.ModelAdapter);

scout.ActivityMap.prototype.init = function(model, session) {
  scout.ActivityMap.parent.prototype.init.call(this, model, session);
};

// TODO ActivityMap | Implement all _render* methods
scout.ActivityMap.prototype._render = function($parent) {
  this._$parent = $parent;
  this.$container = this._$parent.appendDiv('activity-map').attr('id', this._generateId('activity-map'));
  var layout = new scout.ActivityMapLayout(this);
  this.htmlComp = new scout.HtmlComponent(this.$container, this.session);
  this.htmlComp.setLayout(layout);
  this.htmlComp.pixelBasedSizing = false;

  this.$data = this.$container.appendDiv('activity-map-data');
  this._$scrollable = scout.scrollbars.install(this.$data);

  this.drawData();
};

scout.ActivityMap.prototype.drawData = function() {
  this.$data.text('ACTIVITY MAP').addClass('not-implemented');
};

scout.ActivityMap.prototype._renderComponents = function() {
};

scout.ActivityMap.prototype._renderSelectedComponent = function() {
};

scout.ActivityMap.prototype._renderDisplayMode = function() {
};

scout.ActivityMap.prototype._renderDisplayCondensed = function() {
};

scout.ActivityMap.prototype._renderTitle = function() {
};

scout.ActivityMap.prototype._renderViewRange = function() {
};

scout.ActivityMap.prototype._renderSelectedDate = function() {
};

scout.ActivityMap.prototype._renderLoadInProgress = function() {
};

scout.ActivityMap.prototype._renderStartHour = function() {
};

scout.ActivityMap.prototype._renderEndHour = function() {
};

scout.ActivityMap.prototype._renderUseOverflowCells = function() {
};

scout.ActivityMap.prototype._renderShowDisplayModeSelection = function() {
};

scout.ActivityMap.prototype._renderMarkNoonHour = function() {
};

scout.ActivityMap.prototype._renderMarkOutOfMonthDays = function() {
};

scout.ActivityMap.prototype.onModelAction = function(event) {
//  if (event.type === 'calendarChanged') {
//    this._onCalendarChanged(event);
//  } else if (event.type === 'calendarChangedBatch') {
//    this._onCalendarChangedBatch(event.batch);
//  } else {
//    $.log.warn('Model event not handled. Widget: scout.Calendar. Event: ' + event.type + '.');
//  }
};

//scout.ActivityMap.prototype._onActivityMapChanged = function(ActivityMapEvent) {
//  // TODO ActivityMap | Implement --> see JsonActivityMapEvent
//};
//
//scout.ActivityMap.prototype._onActivityMapChangedBatch = function(ActivityMapEventBatch) {
//  // TODO Calendar | Implement --> see JsonCalendarEvent (calendarEventBatch is an array of CalendarEvent)
//};
