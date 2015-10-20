scout.DatePickerPopup = function() {
  scout.DatePickerPopup.parent.call(this);
};
scout.inherits(scout.DatePickerPopup, scout.Popup);

scout.DatePickerPopup.prototype._init = function(options) {
  options.installFocusContext = false;
  scout.DatePickerPopup.parent.prototype._init.call(this, options);

  this.picker = scout.create(scout.DatePicker, {
    parent: this,
    dateFormat: options.dateFormat
  });
};

scout.DatePickerPopup.prototype._createLayout = function() {
  return new scout.NullLayout(this);
};

scout.DatePickerPopup.prototype._render = function($parent) {
  this.picker.render($parent);
  this.$container = this.picker.$container;
  this.$container
    .addClass('date-picker-popup');
  this.htmlComp = new scout.HtmlComponent(this.$container, this.session);
  this.htmlComp.setLayout(this._createLayout());
};

/**
 * @override Popup.js
 */
scout.DatePickerPopup.prototype._onMouseDown = function(event) {
  // when user clicks on DateField input-field, cannot prevent default
  // because text-selection would not work anymore
  if (this.$anchor.isOrHas(event.target)) {
    return;
  }

  // or else: clicked somewhere else on the document -> close
  scout.DatePickerPopup.parent.prototype._onMouseDown.call(this, event);
};

scout.DatePickerPopup.prototype._onAnchorScroll = function(event) {
  this.position();
};

/**
 * @implements DatePickerPopup
 */
scout.DatePickerPopup.prototype.getDatePicker = function() {
  return this.picker;
};
