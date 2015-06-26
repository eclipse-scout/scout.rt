scout.SmartFieldPopup = function(session, options) {
  options = options || {};
  options.installFocusContext = false;
  scout.SmartFieldPopup.parent.call(this, session, options);
  this.smartField = options.smartField;
};
scout.inherits(scout.SmartFieldPopup, scout.Popup);

scout.SmartFieldPopup.prototype._render = function($parent) {
  var htmlPopup, popupLayout, fieldBounds, initialPopupSize;
  if (!$parent) {
    $parent = this.session.$entryPoint;
  }

  this.$container = $.makeDiv('smart-field-popup')
    .on('mousedown', this._onMousedown.bind(this))
    .appendTo($parent);

  htmlPopup = new scout.HtmlComponent(this.$container, this.session);
  popupLayout = new scout.SmartFieldPopupLayout(htmlPopup);
  fieldBounds = this.smartField._fieldBounds();
  initialPopupSize = new scout.Dimension(0, scout.HtmlEnvironment.formRowHeight);
  htmlPopup.validateRoot = true;
  popupLayout.adjustAutoSize = function(prefSize) {
    // must re-evaluate _fieldBounds() for each call, since smart-field is not laid out at this point.
    return this._popupSize(this.smartField._fieldBounds(), prefSize);
  }.bind(this);
  htmlPopup.setLayout(popupLayout);
  popupLayout.autoSize = false;
  htmlPopup.setSize(this._popupSize(fieldBounds, initialPopupSize));
  popupLayout.autoSize = true;
};

scout.SmartFieldPopup.prototype._popupSize = function(fieldBounds, prefSize) {
  return new scout.Dimension(
    Math.max(fieldBounds.width, prefSize.width),
    Math.min(350, prefSize.height));
};

scout.SmartFieldPopup.prototype.resize = function() {
  var htmlPopup = scout.HtmlComponent.get(this.$container),
    popupLayout = htmlPopup.layoutManager,
    prefSize = htmlPopup.getPreferredSize(),
    size = this._popupSize(this.smartField._fieldBounds(), prefSize);
  $.log.debug('SmartFieldPopup resize size=' + size + ' prefSize=' + prefSize);
  // Invalidate is required, when the popup is already opened and the proposal chooser is rendered later
  // when the popup size is the same as before, the proposal chooser would not layout its children (like
  // the table) without invalidate.
  htmlPopup.invalidateLayout();
  htmlPopup.setSize(size);
};

scout.SmartFieldPopup.prototype._onMousedown = function(event) {
  // Make sure field blur won't be triggered (using preventDefault).
  // Also makes sure event does not get propagated (and handled by another mouse down handler, e.g. the one from CellEditorPopup.js)
  return false;
};
