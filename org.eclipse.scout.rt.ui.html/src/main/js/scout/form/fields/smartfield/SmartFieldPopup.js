scout.SmartFieldPopup = function(session, options) {
  options = options || {};
  options.installFocusContext = false;
  scout.SmartFieldPopup.parent.call(this, session, options);

  this._smartField = options.smartField;
};
scout.inherits(scout.SmartFieldPopup, scout.Popup);

scout.SmartFieldPopup.prototype._render = function($parent) {
  var popupLayout, fieldBounds, initialPopupSize;
  if (!$parent) {
    $parent = this.session.$entryPoint;
  }

  this.$container = $.makeDiv('smart-field-popup')
    .on('mousedown', this._onContainerMouseDown.bind(this))
    .appendTo($parent);

  this.htmlComp = new scout.HtmlComponent(this.$container, this.session);
  popupLayout = new scout.SmartFieldPopupLayout(this);
  fieldBounds = this._smartField._fieldBounds();
  initialPopupSize = new scout.Dimension(0, scout.HtmlEnvironment.formRowHeight);
  this.htmlComp.validateRoot = true;
  popupLayout.adjustAutoSize = function(prefSize) {
    // must re-evaluate _fieldBounds() for each call, since smart-field is not laid out at this point.
    return this._popupSize(this._smartField._fieldBounds(), prefSize);
  }.bind(this);
  this.htmlComp.setLayout(popupLayout);
  popupLayout.autoSize = false;
  this.htmlComp.setSize(this._popupSize(fieldBounds, initialPopupSize));
  popupLayout.autoSize = true;
};

scout.SmartFieldPopup.prototype._popupSize = function(fieldBounds, prefSize) {
  return new scout.Dimension(
    Math.max(fieldBounds.width, prefSize.width),
    Math.min(350, prefSize.height));
};

scout.SmartFieldPopup.prototype.resize = function() {
  var htmlPopup = this.htmlComp,
    popupLayout = htmlPopup.layoutManager,
    prefSize = htmlPopup.getPreferredSize(),
    size = this._popupSize(this._smartField._fieldBounds(), prefSize);
  $.log.debug('SmartFieldPopup resize size=' + size + ' prefSize=' + prefSize);
  // Invalidate is required, when the popup is already opened and the proposal chooser is rendered later
  // when the popup size is the same as before, the proposal chooser would not layout its children (like
  // the table) without invalidate.
  htmlPopup.invalidateLayout();
  htmlPopup.setSize(size);
};

/**
 * @override Popup.js
 */
scout.SmartFieldPopup.prototype._onMouseDown = function(event) {
  // when user clicks on SmartField input-field, cannot prevent default
  // because text-selection would not work anymore
  if (this.$anchor.isOrHas(event.target)) {
    return;
  }

  // or else: clicked somewhere else on the document -> close
  scout.SmartFieldPopup.parent.prototype._onMouseDown.call(this, event);
};

/**
 * This event handler is called before the mousedown handler on the _document_ is triggered
 * This allows us to prevent the default, which is important for the CellEditorPopup which
 * should stay open when the SmartField popup is closed. It also prevents the focus blur
 * event on the SmartField input-field.
 */
//TODO CGU/AWE this is not required by the cell editor anymore, but we cannot remove it either because mouse down on a row would immediately close the popup, why?
scout.SmartFieldPopup.prototype._onContainerMouseDown = function(event) {
  // when user clicks on proposal popup with table or tree (prevent default,
  // so input-field does not lose the focus, popup will be closed by the
  // proposal chooser impl.
  return false;
};

scout.SmartFieldPopup.prototype._onAnchorScroll = function(event) {
  this.position();
};
