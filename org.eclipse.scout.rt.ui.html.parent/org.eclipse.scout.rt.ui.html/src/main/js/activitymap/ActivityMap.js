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
  this.$container.addClass('not-implemented'); // TODO Remove once implemented
  var layout = new scout.ActivityMapLayout(this);
  this.htmlComp = new scout.HtmlComponent(this.$container, this.session);
  this.htmlComp.setLayout(layout);
  this.htmlComp.pixelBasedSizing = false;

  this.$data = this.$container.appendDiv('activity-map-data');
  this._$scrollable = scout.scrollbars.install(this.$data);

  this.drawData();
};

scout.ActivityMap.prototype.drawData = function() {
  this.$data.text('ACTIVITY MAP');
};

scout.ActivityMap.prototype._renderDays = function() {
};

scout.ActivityMap.prototype._renderWorkDayCount = function() {
};

scout.ActivityMap.prototype._renderWorkDaysOnly = function() {
};

scout.ActivityMap.prototype._renderFirstHourOfDay = function() {
};

scout.ActivityMap.prototype._renderLastHourOfDay = function() {
};

scout.ActivityMap.prototype._renderIntradayInterval = function() {
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
