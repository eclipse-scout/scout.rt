scout.DatePickerTouchPopup = function() {
  scout.DatePickerTouchPopup.parent.call(this);
};
scout.inherits(scout.DatePickerTouchPopup, scout.TouchPopup);

/**
 * @override TouchPopup.js
 */
scout.DatePickerTouchPopup.prototype._initWidget = function(options) {
  this._widget = scout.create(scout.DatePicker, {
    parent: this,
    dateFoFormat: options.dateFormat
  });
};

scout.DatePickerTouchPopup.prototype.preselectDate = function(date, animated) {
  if (!this.isOpen()) {
    this.render();
  }
  this._widget.preselectDate(date, animated);
};

scout.DatePickerTouchPopup.prototype.selectDate = function(date, animated) {
  if (!this.isOpen()) {
    this.render();
  }
  this._widget.selectDate(date, animated);
};

scout.DatePickerTouchPopup.prototype.shiftSelectedDate = function(years, months, days) {
  if (!this.isOpen()) {
    this.render();
  }
  this._widget.shiftSelectedDate(years, months, days);
};

scout.DatePickerTouchPopup.prototype._onDateSelect = function(callback) {
 this._widget.on('dateSelect', callback);
};

scout.DatePickerTouchPopup.prototype._dateFormat = function(dateFormat) {
  this._widget.dateFormat = dateFormat;
};

/**
 * @override Popup.js
 */
scout.DatePickerTouchPopup.prototype._onMouseDown = function(event) {
  // when user clicks on DateField input-field, cannot prevent default
  // because text-selection would not work anymore
  if (this.$anchor.isOrHas(event.target)) {
    return;
  }

  // or else: clicked somewhere else on the document -> close
  scout.DatePickerTouchPopup.parent.prototype._onMouseDown.call(this, event);
};
