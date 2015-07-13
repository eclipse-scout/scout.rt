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
  this.$container
    .addClass('date-picker-popup')
    .on('mousedown', this._onContainerMouseDown.bind(this));
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

/**
 * This event handler is called before the mousedown handler on the _document_ is triggered
 * This allows us to prevent the default, which is important for the CellEditorPopup which
 * should stay open when the DatePicker popup is closed. It also prevents the focus blur
 * event on the DatePicker input-field.
 */
scout.DatePickerPopup.prototype._onContainerMouseDown = function(event) {
  return false;
};
