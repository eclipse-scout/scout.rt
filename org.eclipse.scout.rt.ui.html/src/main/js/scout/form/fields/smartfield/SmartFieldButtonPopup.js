scout.SmartFieldButtonPopup = function() {
  scout.SmartFieldButtonPopup.parent.call(this);
};
scout.inherits(scout.SmartFieldButtonPopup, scout.Popup);

scout.SmartFieldButtonPopup.MARGIN = 45; // min. margin around popup

scout.SmartFieldButtonPopup.prototype._init = function(options) {
  scout.SmartFieldButtonPopup.parent.prototype._init.call(this, options);
  this._smartFieldButton = options.smartFieldButton;

  // clone original smart-field(button)
  var model = this._smartFieldButton.extractModel();
  model.parent = this;
  model.labelPosition = scout.FormField.LABEL_POSITION_TOP;
  model.noPopup = true; // FIXME AWE: (popups) get rid of this flags
  model.buttonOnly = false;
  this._smartField = scout.create(model);
  this._smartField._popup = this; // FIXME AWE: (popups) beautify this, see addSmartFieldPopup() called in render
  // local smart-field should receive all events adressed to button
  this.session.registerAdapterClone(this._smartFieldButton, this._smartField);
};

/**
 * @override Popup.js
 */
scout.SmartFieldButtonPopup.prototype.prefLocation = function($container, openingDirectionY) {
  var popupSize = this.prefSize($container),
    screenWidth = $(document).width(),
    x = (screenWidth - popupSize.width) / 2;
  return new scout.Point(x, scout.SmartFieldButtonPopup.MARGIN);
};

/**
 * @override Popup.js
 */
scout.SmartFieldButtonPopup.prototype.prefSize = function($container) {
  var screenWidth = $(document).width(),
    screenHeight = $(document).height(),
    minPopupWidth = scout.HtmlEnvironment.formColumnWidth / 2,
    maxPopupWidth = scout.HtmlEnvironment.formColumnWidth,
    maxPopupHeight = scout.HtmlEnvironment.formRowHeight * 15,
    popupWidth = screenWidth - 2 * scout.SmartFieldButtonPopup.MARGIN,
    popupHeight = screenHeight / 2 - scout.SmartFieldButtonPopup.MARGIN;

  popupWidth = Math.min(popupWidth, maxPopupWidth);
  popupWidth = Math.max(popupWidth, minPopupWidth);
  popupHeight = Math.min(popupHeight, maxPopupHeight);

  return new scout.Dimension(popupWidth, popupHeight);
};

scout.SmartFieldButtonPopup.prototype._render = function($parent) {
  if (!$parent) { // FIXME AWE: (popups) was für einen grund gibt es für dieses if? (siehe auch SmartFieldPopup)
    // wahrscheinlich haben wir ja dann nie einen $parent gesetzt, wenn man explizit das field als parent setzt
    // kommt es sowieso nicht gut wegen der z-order (popup wird verdeckt von anderen fields).
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

scout.SmartFieldButtonPopup.prototype._postRender = function() {
  scout.SmartFieldButtonPopup.parent.prototype._postRender.call(this);
  this._smartField._onClick(); // open proposal
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


// ---- SmartFieldButtonPopupLayout ---- //

scout.SmartFieldButtonPopupLayout = function(popup) {
  scout.SmartFieldButtonPopupLayout.parent.call(this);
  this._popup = popup;
};
scout.inherits(scout.SmartFieldButtonPopupLayout, scout.AbstractLayout);

scout.SmartFieldButtonPopupLayout.prototype.layout = function($container) {
  var popupSize = this._popup.htmlComp.getSize(),
    popup = this._popup,
    smartFieldHeight = popup._smartField.htmlComp.getPreferredSize().height,
    proposalChooserVOffset = smartFieldHeight + scout.HtmlEnvironment.formRowGap;

  popup._smartField.htmlComp.setBounds(new scout.Rectangle(0, 0, popupSize.width, smartFieldHeight));
  popup._proposalChooserHtmlComp.setBounds(new scout.Rectangle(0, proposalChooserVOffset, popupSize.width, popupSize.height - proposalChooserVOffset));
};

scout.SmartFieldButtonPopupLayout.prototype.preferredLayoutSize = function($container) {
  return new scout.Dimension(400, 300);
};
