// FIXME AWE: (popups) play animation when popup opens?
scout.SmartFieldMobilePopup = function() {
  scout.SmartFieldMobilePopup.parent.call(this);

  this.$proposalChooserContainer;
  this.proposalChooserContainerHtmlComp;
};
scout.inherits(scout.SmartFieldMobilePopup, scout.Popup);

scout.SmartFieldMobilePopup.TOP_MARGIN = 45;

scout.SmartFieldMobilePopup.prototype._init = function(options) {
  scout.SmartFieldMobilePopup.parent.prototype._init.call(this, options);
  this._mobileSmartField = options.smartField;

  // FIXME AWE: (popups) SFMpopup sollte nicht im init vom smartfield erzeugt werden, sondern erst wenn man drauf klickt

  // clone original mobile smart-field
  // original and clone both point to the same _popup instance
  this._smartField = this._mobileSmartField.cloneAdapter({
    parent: this,
    _popup: this,
    labelPosition: scout.FormField.LABEL_POSITION_ON_FIELD,
    embedded: true,
    mobile: false
  });
};

/**
 * @override Popup.js
 */
scout.SmartFieldMobilePopup.prototype.prefLocation = function($container, openingDirectionY) {
  var popupSize = this.prefSize($container),
    screenWidth = $(document).width(),
    x = Math.max(0, (screenWidth - popupSize.width) / 2);
  return new scout.Point(x, scout.SmartFieldMobilePopup.TOP_MARGIN);
};

/**
 * @override Popup.js
 */
scout.SmartFieldMobilePopup.prototype.prefSize = function($container) {
  var screenWidth = $(document).width(),
    screenHeight = $(document).height(),
    minPopupWidth = scout.HtmlEnvironment.formColumnWidth / 2,
    maxPopupHeight = scout.HtmlEnvironment.formRowHeight * 15,
    popupWidth = scout.HtmlEnvironment.formColumnWidth,
    popupHeight = screenHeight / 2 - scout.SmartFieldMobilePopup.TOP_MARGIN;

  popupWidth = Math.max(popupWidth, minPopupWidth);
  popupHeight = Math.min(popupHeight, maxPopupHeight);

  return new scout.Dimension(popupWidth, popupHeight);
};

scout.SmartFieldMobilePopup.prototype._render = function($parent) {
  if (!$parent) { // FIXME AWE: (popups) was für einen grund gibt es für dieses if? (siehe auch SmartFieldPopup)
    // wahrscheinlich haben wir ja dann nie einen $parent gesetzt, wenn man explizit das field als parent setzt
    // kommt es sowieso nicht gut wegen der z-order (popup wird verdeckt von anderen fields).
    $parent = this.session.$entryPoint;
  }

  this.$container = $.makeDiv('smart-field-popup mobile')
    .on('mousedown', this._onContainerMouseDown.bind(this)) // FIXME AWE: (popups) is this line required?
    .appendTo($parent);

  this.$proposalChooserContainer = $.makeDiv('proposal-chooser-container')
    .appendTo(this.$container);
  this.proposalChooserContainerHtmlComp = new scout.HtmlComponent(this.$proposalChooserContainer, this.session);
  this.proposalChooserContainerHtmlComp.setLayout(new scout.SingleLayout());

  this._smartField.render(this.$container);

  this.htmlComp = new scout.HtmlComponent(this.$container, this.session);
  this.htmlComp.setLayout(new scout.SmartFieldMobilePopupLayout(this));
};

scout.SmartFieldMobilePopup.prototype._postRender = function() {
  scout.SmartFieldMobilePopup.parent.prototype._postRender.call(this);
  this._smartField._openProposal(true);
};

scout.SmartFieldMobilePopup.prototype._embedProposalChooser = function(proposalChooser) {
  if (!this.rendered) {
    $.log.warn('Tried to embed proposal chooser, but popup is not rendered');
    return; // FIXME AWE: (popups) schauen ob und wann dieser fall auftritt
  }

  proposalChooser.setParent(this);
  proposalChooser.render(this.$proposalChooserContainer);
  // must use revalidate because size of proposal-chooser _container_ has not changed
  // but the content of the container must be laid out
  this.proposalChooserContainerHtmlComp.revalidateLayout();
};

/**
 * @override Popup.js
 */
scout.SmartFieldMobilePopup.prototype._onMouseDown = function(event) {
  // when user clicks on SmartField input-field, cannot prevent default
  // because text-selection would not work anymore
  if (this.$anchor.isOrHas(event.target)) {
    return;
  }

  // or else: clicked somewhere else on the document -> close
  scout.SmartFieldMobilePopup.parent.prototype._onMouseDown.call(this, event);
};

/**
 * This event handler is called before the mousedown handler on the _document_ is triggered
 * This allows us to prevent the default, which is important for the CellEditorPopup which
 * should stay open when the SmartField popup is closed. It also prevents the focus blur
 * event on the SmartField input-field.
 */
//TODO CGU/AWE this is not required by the cell editor anymore, but we cannot remove it either because mouse down on a row would immediately close the popup, why?
scout.SmartFieldMobilePopup.prototype._onContainerMouseDown = function(event) {
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
scout.SmartFieldMobilePopup.prototype.close = function(event) {
  this._smartField._sendCancelProposal();
  scout.SmartFieldMobilePopup.parent.prototype.close.call(this);
};

