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
    dateFormat: options.dateFormat
  });
 
  this._field._attachDatePickerDateSelectedHandler();
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

/**
 * @implements DatePickerPopup
 */
scout.DatePickerTouchPopup.prototype.getDatePicker = function() {
  return this._widget;
};
