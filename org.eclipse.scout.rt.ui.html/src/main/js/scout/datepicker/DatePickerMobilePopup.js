// FIXME AWE: (popups) common base clase for mobile popup
// FIXME AWE: (popups) swipe für month shift
scout.DatePickerMobilePopup = function(popup) {
  scout.DatePickerMobilePopup.parent.call(this, popup);
};
scout.inherits(scout.DatePickerMobilePopup, scout.Popup);

scout.DatePickerMobilePopup.TOP_MARGIN = 45;

scout.DatePickerMobilePopup.prototype._init = function(options) {
  options = options || {};
  options.installFocusContext = false;
  scout.DatePickerMobilePopup.parent.prototype._init.call(this, options);
  this._mobileDateField = options.dateField;

  // clone original mobile date-field
  // original and clone both point to the same _popup instance
  this._dateField = this._mobileDateField.cloneAdapter({
    parent: this,
    _datePickerPopup: this,
    labelPosition: scout.FormField.LABEL_POSITION_ON_FIELD,
    statusVisible: false,
    embedded: true,
    mobile: false
  });

  this.picker = scout.create(scout.DatePicker, {
    parent: this,
    dateFoFormat: options.dateFormat
  });
};

scout.DatePickerMobilePopup.prototype._createLayout = function() {
  return new scout.DatePickerMobilePopupLayout(this);
};

scout.DatePickerMobilePopup.prototype._render = function($parent) {
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
scout.DatePickerMobilePopup.prototype.prefLocation = function($container, openingDirectionY) {
  var popupSize = this.htmlComp._layout.preferredLayoutSize($container),
    screenWidth = $(document).width(),
    x = Math.max(0, (screenWidth - popupSize.width) / 2);
  return new scout.Point(x, scout.DatePickerMobilePopup.TOP_MARGIN);
};

scout.DatePickerMobilePopup.prototype.preselectDate = function(date, animated) {
  if (!this.isOpen()) {
    this.render();
  }
  this.picker.preselectDate(date, animated);
};

scout.DatePickerMobilePopup.prototype.selectDate = function(date, animated) {
  if (!this.isOpen()) {
    this.render();
  }
  this.picker.selectDate(date, animated);
};

scout.DatePickerMobilePopup.prototype.shiftViewDate = function(years, months, days) {
  if (!this.isOpen()) {
    this.render();
  }
  this.picker.shiftViewDate(years, months, days);
};

scout.DatePickerMobilePopup.prototype.shiftSelectedDate = function(years, months, days) {
  if (!this.isOpen()) {
    this.render();
  }
  this.picker.shiftSelectedDate(years, months, days);
};

scout.DatePickerMobilePopup.prototype.isOpen = function() {
  return this.rendered;
};

/**
 * @override Popup.js
 */
scout.DatePickerMobilePopup.prototype._onMouseDown = function(event) {
  // when user clicks on DateField input-field, cannot prevent default
  // because text-selection would not work anymore
  if (this.$anchor.isOrHas(event.target)) {
    return;
  }

  // or else: clicked somewhere else on the document -> close
  scout.DatePickerMobilePopup.parent.prototype._onMouseDown.call(this, event);
};

scout.DatePickerMobilePopup.prototype._onAnchorScroll = function(event) {
  this.position();
};
