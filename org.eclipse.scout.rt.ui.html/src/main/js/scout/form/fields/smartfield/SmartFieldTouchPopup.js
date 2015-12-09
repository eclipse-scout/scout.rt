/*******************************************************************************
 * Copyright (c) 2014-2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
scout.SmartFieldTouchPopup = function() {
  scout.SmartFieldTouchPopup.parent.call(this);
};
scout.inherits(scout.SmartFieldTouchPopup, scout.TouchPopup);

scout.SmartFieldTouchPopup.prototype._postRender = function() {
  scout.SmartFieldTouchPopup.parent.prototype._postRender.call(this);
  this._field._openProposal(true);
};

scout.SmartFieldTouchPopup.prototype._renderProposalChooser = function(proposalChooser) {
  proposalChooser.render(this._$widgetContainer);
  proposalChooser.setParent(this);
  this._widgetContainerHtmlComp.revalidateLayout();
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
//TODO [5.2] cgu, awe: this is not required by the cell editor anymore, but we cannot remove it either because mouse down on a row would immediately close the popup, why?
scout.SmartFieldTouchPopup.prototype._onContainerMouseDown = function(event) {
  // when user clicks on proposal popup with table or tree (prevent default,
  // so input-field does not lose the focus, popup will be closed by the
  // proposal chooser impl.
  //  return false;
  // FIXME awe: (popups) durch das prevent default here, wird verhindert, dass ein text-field im popup den fokus bekommen kann
  // müssen wir für mobile und editierbare tabellen (?) noch lösen
};

/**
 * @override Popup.js
 */
scout.SmartFieldTouchPopup.prototype.close = function(event) {
  this._field._sendCancelProposal();
  scout.SmartFieldTouchPopup.parent.prototype.close.call(this);
};
