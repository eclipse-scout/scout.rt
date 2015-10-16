// FIXME AWE: (popups) common base clase for mobile popup
// FIXME AWE: (popups) swipe für month shift
scout.DatePickerTouchPopup = function(popup) {
  scout.DatePickerTouchPopup.parent.call(this, popup);
};
scout.inherits(scout.DatePickerTouchPopup, scout.Popup);

scout.DatePickerTouchPopup.TOP_MARGIN = 45;

scout.DatePickerTouchPopup.prototype._init = function(options) {
  options = options || {};
  options.installFocusContext = false;
  scout.DatePickerTouchPopup.parent.prototype._init.call(this, options);
  this._mobileDateField = options.dateField;

  // clone original mobile date-field
  // original and clone both point to the same _popup instance
  this._dateField = this._mobileDateField.cloneAdapter({
    parent: this,
    _datePickerPopup: this,
    labelPosition: scout.FormField.LABEL_POSITION_ON_FIELD,
    statusVisible: false,
    embedded: true,
    touch: false
  });

  this.picker = scout.create(scout.DatePicker, {
    parent: this,
    dateFoFormat: options.dateFormat
  });
};

scout.DatePickerTouchPopup.prototype._createLayout = function() {
  return new scout.DatePickerTouchPopupLayout(this);
};

scout.DatePickerTouchPopup.prototype._render = function($parent) {
  this.$container = $.makeDiv('smart-field-popup mobile')
    .appendTo($parent);

  this._dateField.render(this.$container);

  this.$datePickerContainer = $.makeDiv('date-picker-container')
    .appendTo(this.$container);
  this.datePickerContainerHtmlComp = new scout.HtmlComponent(this.$datePickerContainer, this.session);
  this.datePickerContainerHtmlComp.setLayout(new scout.SingleLayout());

  this.picker.render(this.$datePickerContainer);

  this.htmlComp = new scout.HtmlComponent(this.$container, this.session);
  this.htmlComp.setLayout(this._createLayout());
};

/**
 * @override Popup.js
 */
scout.DatePickerTouchPopup.prototype.prefLocation = function($container, openingDirectionY) {
  var popupSize = this.htmlComp._layout.preferredLayoutSize($container),
    screenWidth = $(document).width(),
    x = Math.max(0, (screenWidth - popupSize.width) / 2);
  return new scout.Point(x, scout.DatePickerTouchPopup.TOP_MARGIN);
};

scout.DatePickerTouchPopup.prototype.preselectDate = function(date, animated) {
  if (!this.isOpen()) {
    this.render();
  }
  this.picker.preselectDate(date, animated);
};

scout.DatePickerTouchPopup.prototype.selectDate = function(date, animated) {
  if (!this.isOpen()) {
    this.render();
  }
  this.picker.selectDate(date, animated);
};

scout.DatePickerTouchPopup.prototype.shiftViewDate = function(years, months, days) {
  if (!this.isOpen()) {
    this.render();
  }
  this.picker.shiftViewDate(years, months, days);
};

scout.DatePickerTouchPopup.prototype.shiftSelectedDate = function(years, months, days) {
  if (!this.isOpen()) {
    this.render();
  }
  this.picker.shiftSelectedDate(years, months, days);
};

scout.DatePickerTouchPopup.prototype.isOpen = function() {
  return this.rendered;
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

scout.DatePickerTouchPopup.prototype._onAnchorScroll = function(event) {
  this.position();
};
