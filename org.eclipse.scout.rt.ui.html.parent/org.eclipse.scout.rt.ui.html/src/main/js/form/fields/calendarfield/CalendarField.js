// SCOUT GUI
// (c) Copyright 2013-2014, BSI Business Systems Integration AG

scout.CalendarField = function() {
  scout.CalendarField.parent.call(this);
  this._addAdapterProperties(['calendar']);
};
scout.inherits(scout.CalendarField, scout.FormField);

scout.CalendarField.prototype._render = function($parent) {
  this.addContainer($parent, 'calendar-field');
  this.addLabel();
  this.addStatus();
  if (this.calendar) {
    this.calendar.render(this.$container);
    this.addField(this.calendar.$container);
  }
};
