scout.SmartFieldButtonPopup = function() {
  scout.SmartFieldButtonPopup.parent.call(this);
};
scout.inherits(scout.SmartFieldButtonPopup, scout.Popup);

scout.SmartFieldButtonPopup.prototype._init = function(options) {
  scout.SmartFieldButtonPopup.parent.prototype._init.call(this, options);
  this._smartFieldButton = options.smartFieldButton;

  // clone original smart-field(button)
  var modelClone = $.extend({}, this._smartFieldButton._model);
  modelClone.parent = this;
  modelClone.noPopup = true; // FIXME AWE: (popups) get rid of this flags
  modelClone.buttonOnly = false;
  this._smartField = scout.create(modelClone);
  this._smartField._popup = this; // FIXME AWE: (popups) beautify this, see addSmartFieldPopup() called in render
  // local smart-field should receive all events adressed to button
  this.session.registerAdapterClone(this._smartFieldButton, this._smartField);
};

scout.SmartFieldButtonPopup.prototype._render = function($parent) {
  var popupLayout, fieldBounds, initialPopupSize;
  if (!$parent) {
    $parent = this.session.$entryPoint;
  }

  this.$container = $.makeDiv('smart-field-popup button')
    .on('mousedown', this._onContainerMouseDown.bind(this)) // FIXME AWE: (popups) is this line required?
    .appendTo($parent);

  var $proposalChooserContainer = $.makeDiv('proposal-chooser-container')
    .appendTo(this.$container);
  this._proposalChooserHtmlComp = new scout.HtmlComponent($proposalChooserContainer, this.session);
  this._proposalChooserHtmlComp.setLayout(new scout.SingleLayout());

  this._smartField.render(this.$container);

  this.htmlComp = new scout.HtmlComponent(this.$container, this.session);
  this.htmlComp.setLayout(new scout.SmartFieldButtonPopupLayout(this));
};

/**
 * @override Popup.js
 */
scout.SmartFieldButtonPopup.prototype._onMouseDown = function(event) {
  // when user clicks on SmartField input-field, cannot prevent default
  // because text-selection would not work anymore
  if (this.$anchor.isOrHas(event.target)) {
    return;
  }

  // or else: clicked somewhere else on the document -> close
  scout.SmartFieldButtonPopup.parent.prototype._onMouseDown.call(this, event);
};

/**
 * This event handler is called before the mousedown handler on the _document_ is triggered
 * This allows us to prevent the default, which is important for the CellEditorPopup which
 * should stay open when the SmartField popup is closed. It also prevents the focus blur
 * event on the SmartField input-field.
 */
//TODO CGU/AWE this is not required by the cell editor anymore, but we cannot remove it either because mouse down on a row would immediately close the popup, why?
scout.SmartFieldButtonPopup.prototype._onContainerMouseDown = function(event) {
  // when user clicks on proposal popup with table or tree (prevent default,
  // so input-field does not lose the focus, popup will be closed by the
  // proposal chooser impl.
//  return false;
  // FIXME AWE: (popups) durch das prevent default here, wird verhindert, dass ein text-field im popup den fokus bekommen kann
  // müssen wir für mobile und editierbare tabellen (?) noch lösen
};

scout.SmartFieldButtonPopup.prototype._onAnchorScroll = function(event) {
  this.position();
};


// ------------------------

scout.SmartFieldButtonPopupLayout = function(popup) {
  scout.SmartFieldButtonPopupLayout.parent.call(this);
  this._popup = popup;
};
scout.inherits(scout.SmartFieldButtonPopupLayout, scout.AbstractLayout);

scout.SmartFieldButtonPopupLayout.prototype.layout = function($container) {
  var size = this._popup.htmlComp.getSize(),
    popup = this._popup;
  popup._smartField.htmlComp.setBounds(new scout.Rectangle(10, 10, size.width - 20, 30));
  popup._proposalChooserHtmlComp.setBounds(new scout.Rectangle(10, 50, size.width - 20, size.height - 60));
};

scout.SmartFieldButtonPopupLayout.prototype.preferredLayoutSize = function($container) {
  return new scout.Dimension(400, 300);
};
