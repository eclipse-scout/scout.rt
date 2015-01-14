// SCOUT GUI
// (c) Copyright 2013-2014, BSI Business Systems Integration AG

scout.Calendar = function() {
  scout.Calendar.parent.call(this);

  this.$container;
  this.$data;
  this._$scrollable;
};
scout.inherits(scout.Calendar, scout.ModelAdapter);

scout.Calendar.prototype.init = function(model, session) {
  scout.Calendar.parent.prototype.init.call(this, model, session);

  // Trigger async loading of data TODO BSH Calendar | Check if there is a better solution
  this.session.send(this.id, 'setVisibleRange', {
    dateRange: {
      // TODO Calendar | Get initial dates from date selector
      from: scout.dates.toJsonDate(new Date()),
      to: scout.dates.toJsonDate(new Date())
    }
  });
};

// TODO Calendar | Implement all _render* methods
scout.Calendar.prototype._render = function($parent) {
  this._$parent = $parent;
  this.$container = this._$parent.appendDiv('calendar').attr('id', this._generateId('calendar'));
  var layout = new scout.CalendarLayout(this);
  this.htmlComp = new scout.HtmlComponent(this.$container, this.session);
  this.htmlComp.setLayout(layout);
  this.htmlComp.pixelBasedSizing = false;

  this.$data = this.$container.appendDiv('calendar-data');
  this._$scrollable = scout.scrollbars.install(this.$data);

  this.drawData();
};

scout.Calendar.prototype.drawData = function() {
  this.$data.text('CALENDAR [selectedDate: ' + this.selectedDate + ']').
    addClass('not-implemented').css('background-color', '#FFFFCC');
};

scout.Calendar.prototype._renderComponents = function() {
};

scout.Calendar.prototype._renderSelectedComponent = function() {
};

scout.Calendar.prototype._renderDisplayMode = function() {
};

scout.Calendar.prototype._renderDisplayCondensed = function() {
};

scout.Calendar.prototype._renderTitle = function() {
};

scout.Calendar.prototype._renderViewRange = function() {
};

scout.Calendar.prototype._renderSelectedDate = function() {
};

scout.Calendar.prototype._renderLoadInProgress = function() {
};

scout.Calendar.prototype._renderStartHour = function() {
};

scout.Calendar.prototype._renderEndHour = function() {
};

scout.Calendar.prototype._renderUseOverflowCells = function() {
};

scout.Calendar.prototype._renderShowDisplayModeSelection = function() {
};

scout.Calendar.prototype._renderMarkNoonHour = function() {
};

scout.Calendar.prototype._renderMarkOutOfMonthDays = function() {
};

scout.Calendar.prototype.onModelAction = function(event) {
  if (event.type === 'calendarChanged') {
    this._onCalendarChanged(event);
  } else if (event.type === 'calendarChangedBatch') {
    this._onCalendarChangedBatch(event.batch);
  } else {
    $.log.warn('Model event not handled. Widget: scout.Calendar. Event: ' + event.type + '.');
  }
};

scout.Calendar.prototype._onCalendarChanged = function(calendarEvent) {
  // TODO Calendar | Implement --> see JsonCalendarEvent
};

scout.Calendar.prototype._onCalendarChangedBatch = function(calendarEventBatch) {
  // TODO Calendar | Implement --> see JsonCalendarEvent (calendarEventBatch is an array of CalendarEvent)
};
