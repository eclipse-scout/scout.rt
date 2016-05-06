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
scout.FormMenu = function() {
  scout.FormMenu.parent.call(this);
  this._addAdapterProperties('form');
};
scout.inherits(scout.FormMenu, scout.Menu);

scout.FormMenu.PopupStyle = {
  DEFAULT: 'default',
  MOBILE: 'mobile'
};

scout.FormMenu.prototype._init = function(model) {
  scout.FormMenu.parent.prototype._init.call(this, model);

  if (!this.popupStyle) {
    if (this.session.userAgent.deviceType === scout.Device.Type.MOBILE) {
      this.popupStyle = scout.FormMenu.PopupStyle.MOBILE;
    } else {
      this.popupStyle = scout.FormMenu.PopupStyle.DEFAULT;
    }
  }
};

scout.FormMenu.prototype._renderForm = function() {
  if (!this.rendered) {
    // Don't execute initially since _renderSelected will be executed
    return;
  }
  this._renderSelected();
};

/**
 * @override
 */
scout.FormMenu.prototype._renderText = function() {
  scout.FormMenu.parent.prototype._renderText.call(this);
  if (this.rendered && this.popup && this.popup instanceof scout.FormMenuPopup) {
    this.popup.rerenderHead();
    this.popup.position();
  }
};

/**
 * @override
 */
scout.FormMenu.prototype.cloneAdapter = function(modelOverride) {
  modelOverride = modelOverride || {};
  // If the FormMenu is put into a context menu it will be cloned.
  // Cloning a form is not possible because it may non clonable components (Table, TabBox, etc.) -> exclude
  // Luckily, it is not necessary to clone it since the form is never shown multiple times at once -> Just use the same instance
  modelOverride.form = this.form;
  return scout.FormMenu.parent.prototype.cloneAdapter.call(this, modelOverride);
};

/**
 * @override
 */
scout.FormMenu.prototype._createPopup = function() {
  // Menu bar should always be on the bottom
  this.form.rootGroupBox.menuBar.bottom();

  if (this.popupStyle === scout.FormMenu.PopupStyle.MOBILE) {
    return scout.create('MobilePopup', {
      parent: this.session.desktop, // use desktop to make _handleSelectedInEllipsis work (if parent is this and this were not rendered, popup.entryPoint would not work)
      widget: this.form,
      title: this.form.title
    });
  }

  return scout.create('FormMenuPopup', {
    parent: this,
    formMenu: this,
    openingDirectionX: this.popupOpeningDirectionX,
    openingDirectionY: this.popupOpeningDirectionY
  });
};

/**
 * @override
 */
scout.FormMenu.prototype._doActionTogglesPopup = function() {
  return !!this.form;
};

scout.FormMenu.prototype._handleSelectedInEllipsis = function() {
  if (this.popupStyle !== scout.FormMenu.PopupStyle.MOBILE) {
    scout.FormMenu.parent.prototype._handleSelectedInEllipsis.call(this);
    return;
  }
  if (!this._doActionTogglesPopup()) {
    return;
  }
  // The mobile popup is not atached to a header -> no need to open the parent menu, just show the poupup
  if (this.selected) {
    this._openPopup();
  } else {
    this._closePopup();
  }
};

/**
 * @override
 */
scout.FormMenu.prototype._createActionKeyStroke = function() {
  return new scout.FormMenuActionKeyStroke(this);
};

/**
 * FormMenuActionKeyStroke
 */
scout.FormMenuActionKeyStroke = function(action) {
  scout.FormMenuActionKeyStroke.parent.call(this, action);
};
scout.inherits(scout.FormMenuActionKeyStroke, scout.ActionKeyStroke);

scout.FormMenuActionKeyStroke.prototype.handle = function(event) {
  this.field.toggle();
};

scout.FormMenuActionKeyStroke.prototype._postRenderKeyBox = function($drawingArea) {
  if (this.field.iconId) {
    var wIcon = $drawingArea.find('.icon').width();
    var wKeybox = $drawingArea.find('.key-box').outerWidth();
    var containerPadding = Number($drawingArea.css('padding-left').replace('px', ''));
    var leftKeyBox = wIcon / 2 - wKeybox / 2 + containerPadding;
    $drawingArea.find('.key-box').css('left', leftKeyBox + 'px');
  }
};
