// FIXME AWE: (popups) play animation when popup opens?
scout.SmartFieldTouchPopup = function() {
  scout.SmartFieldTouchPopup.parent.call(this);

  this.$proposalChooserContainer;
  this.proposalChooserContainerHtmlComp;
};
scout.inherits(scout.SmartFieldTouchPopup, scout.Popup);

scout.SmartFieldTouchPopup.TOP_MARGIN = 45;

scout.SmartFieldTouchPopup.prototype._init = function(options) {
  scout.SmartFieldTouchPopup.parent.prototype._init.call(this, options);
  this._mobileSmartField = options.smartField;

  // clone original mobile smart-field
  // original and clone both point to the same _popup instance
  this._smartField = this._mobileSmartField.cloneAdapter({
    parent: this,
    _popup: this,
    labelPosition: scout.FormField.LABEL_POSITION_ON_FIELD,
    statusVisible: false,
    embedded: true,
    touch: false
  });
};

/**
 * @override Popup.js
 */
scout.SmartFieldTouchPopup.prototype.prefLocation = function($container, openingDirectionY) {
  var popupSize = this.htmlComp._layout.preferredLayoutSize($container),
    screenWidth = $(document).width(),
    x = Math.max(0, (screenWidth - popupSize.width) / 2);
  return new scout.Point(x, scout.SmartFieldTouchPopup.TOP_MARGIN);
};

scout.SmartFieldTouchPopup.prototype._createLayout = function() {
  return new scout.SmartFieldTouchPopupLayout(this);
};

scout.SmartFieldTouchPopup.prototype._render = function($parent) {
  this.$container = $.makeDiv('smart-field-popup mobile')
    .on('mousedown', this._onContainerMouseDown.bind(this)) // FIXME AWE: (popups) is this line required?
    .appendTo($parent);

  this.$proposalChooserContainer = $.makeDiv('proposal-chooser-container')
    .appendTo(this.$container);
  this.proposalChooserContainerHtmlComp = new scout.HtmlComponent(this.$proposalChooserContainer, this.session);
  this.proposalChooserContainerHtmlComp.setLayout(new scout.SingleLayout());

  this._smartField.render(this.$container);

  this.htmlComp = new scout.HtmlComponent(this.$container, this.session);
  this.htmlComp.setLayout(this._createLayout());
};

scout.SmartFieldTouchPopup.prototype._postRender = function() {
  scout.SmartFieldTouchPopup.parent.prototype._postRender.call(this);
  this._smartField._openProposal(true);
};

scout.SmartFieldTouchPopup.prototype._renderProposalChooser = function(proposalChooser) {
  proposalChooser.render(this.$proposalChooserContainer);
  proposalChooser.setParent(this);
  this.proposalChooserContainerHtmlComp.revalidateLayout();
};

/**
 * @override Popup.js
 */
scout.SmartFieldTouchPopup.prototype._onMouseDown = function(event) {
  // when user clicks on SmartField input-field, cannot prevent default
  // because text-selection would not work anymore
  if (this.$anchor.isOrHas(event.target)) {
    return;
  }

  // or else: clicked somewhere else on the document -> close
  scout.SmartFieldTouchPopup.parent.prototype._onMouseDown.call(this, event);
};

/**
 * This event handler is called before the mousedown handler on the _document_ is triggered
 * This allows us to prevent the default, which is important for the CellEditorPopup which
 * should stay open when the SmartField popup is closed. It also prevents the focus blur
 * event on the SmartField input-field.
 */
//TODO CGU/AWE this is not required by the cell editor anymore, but we cannot remove it either because mouse down on a row would immediately close the popup, why?
scout.SmartFieldTouchPopup.prototype._onContainerMouseDown = function(event) {
  // when user clicks on proposal popup with table or tree (prevent default,
  // so input-field does not lose the focus, popup will be closed by the
  // proposal chooser impl.
//  return false;
  // FIXME AWE: (popups) durch das prevent default here, wird verhindert, dass ein text-field im popup den fokus bekommen kann
  // müssen wir für mobile und editierbare tabellen (?) noch lösen
};

/**
 * @override Popup.js
 */
scout.SmartFieldTouchPopup.prototype.close = function(event) {
  this._smartField._sendCancelProposal();
  scout.SmartFieldTouchPopup.parent.prototype.close.call(this);
};

