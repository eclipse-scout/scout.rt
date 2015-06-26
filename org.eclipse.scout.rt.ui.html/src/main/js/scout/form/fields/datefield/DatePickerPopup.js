scout.DatePickerPopup = function(session, options) {
  options = options || {};
  options.installFocusContext = false;
  scout.DatePickerPopup.parent.call(this, session, options);
  this.picker = new scout.DatePicker(options.dateFormat, options.dateField);
  this.addChild(this.picker);
};
scout.inherits(scout.DatePickerPopup, scout.Popup);

scout.DatePickerPopup.prototype._render = function($parent) {
  if (!$parent) {
    $parent = this.session.$entryPoint;
  }
  this.picker.render($parent);
  this.$container = this.picker.$container;
  this.$container.addClass('date-picker-popup');
};

scout.DatePickerPopup.prototype.selectDate = function(date, animated) {
  if (!this.isOpen()) {
    this.render();
  }
  this.picker.selectDate(date, animated);
};

scout.DatePickerPopup.prototype.shiftViewDate = function(years, months, days) {
  if (!this.isOpen()) {
    this.render();
  }
  this.picker.shiftViewDate(years, months, days);
};

scout.DatePickerPopup.prototype.shiftSelectedDate = function(years, months, days) {
  if (!this.isOpen()) {
    this.render();
  }
  this.picker.shiftSelectedDate(years, months, days);
};

scout.DatePickerPopup.prototype.isOpen = function() {
  return this.rendered;
};
