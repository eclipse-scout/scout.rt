scout.SmartFieldPopup = function() {
  scout.SmartFieldPopup.parent.call(this);
};
scout.inherits(scout.SmartFieldPopup, scout.Popup);

scout.SmartFieldPopup.prototype._init = function(options) {
  options.installFocusContext = false;
  scout.SmartFieldPopup.parent.prototype._init.call(this, options);

  this.smartField = options.smartField;
};

scout.SmartFieldPopup.prototype._createLayout = function() {
  return new scout.SmartFieldPopupLayout(this);
};

scout.SmartFieldPopup.prototype._render = function($parent) {
  this.$container = $.makeDiv('smart-field-popup')
    .on('mousedown', this._onContainerMouseDown.bind(this))
    .appendTo($parent);

  this.htmlComp = new scout.HtmlComponent(this.$container, this.session);
  this.htmlComp.setLayout(this._createLayout());
  this.htmlComp.validateRoot = true;
};

scout.SmartFieldPopup.prototype.resize = function() {
  // Revalidate is required when the popup is already opened and the proposal chooser is rendered later
  this.revalidateLayout();
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
