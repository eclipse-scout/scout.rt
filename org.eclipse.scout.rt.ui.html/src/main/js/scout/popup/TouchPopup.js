scout.TouchPopup = function() {
  scout.TouchPopup.parent.call(this);

  // the original touch field from the form
  this._touchField;
  // the cloned field from the popup
  this._field;
  // the widget placed below the field
  this._widget;
  this._$widgetContainer;
  this._widgetContainerHtmlComp;

};
scout.inherits(scout.TouchPopup, scout.Popup);

scout.TouchPopup.TOP_MARGIN = 45;

scout.TouchPopup.prototype._init = function(options) {
  options = options || {};
  scout.TouchPopup.parent.prototype._init.call(this, options);
  this._touchField = options.field;

  // clone original touch field
  // original and clone both point to the same _popup instance
  this._field = this._touchField.cloneAdapter({
    parent: this,
    _datePickerPopup: this, // FIXME dynamic name for this property! or rename _datePickerPopup in DateField
    _popup: this,
    labelPosition: scout.FormField.LABEL_POSITION_ON_FIELD,
    statusVisible: false,
    embedded: true,
    touch: false
  });

  this._initWidget(options);
};

scout.TouchPopup.prototype._initWidget = function(options) {
  // NOP
};

scout.TouchPopup.prototype._createLayout = function() {
  return new scout.TouchPopupLayout(this);
};

/**
 * @override Popup.js
 */
scout.TouchPopup.prototype.prefLocation = function($container, openingDirectionY) {
  var popupSize = this.htmlComp._layout.preferredLayoutSize($container),
    screenWidth = $(document).width(),
    x = Math.max(0, (screenWidth - popupSize.width) / 2);
  return new scout.Point(x, scout.TouchPopup.TOP_MARGIN);
};

scout.TouchPopup.prototype._render = function($parent) {
  this.$container = $.makeDiv('touch-popup')
//    .on('mousedown', this._onContainerMouseDown.bind(this)) // FIXME AWE: (popups) is this line required?
    .appendTo($parent);

  this._field.render(this.$container);

  this._$widgetContainer = $.makeDiv('widget-container')
    .appendTo(this.$container);
  this._widgetContainerHtmlComp = new scout.HtmlComponent(this._$widgetContainer, this.session);
  this._widgetContainerHtmlComp.setLayout(new scout.SingleLayout());

  if (this._widget) {
    this._widget.render(this._$widgetContainer);
  }

  this.htmlComp = new scout.HtmlComponent(this.$container, this.session);
  this.htmlComp.setLayout(this._createLayout());
};
