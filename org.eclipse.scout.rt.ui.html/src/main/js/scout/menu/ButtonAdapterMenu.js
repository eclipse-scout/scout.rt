/*
 * Copyright (c) 2014-2018 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
scout.ButtonAdapterMenu = function() {
  scout.ButtonAdapterMenu.parent.call(this);
  this._removeWidgetProperties('childActions'); // managed by button

  this._buttonPropertyChangeHandler = this._onButtonPropertyChange.bind(this);
  this._buttonDestroyHandler = this._onButtonDestroy.bind(this);

  this._addCloneProperties(['button']);
  this.button;
  this.menubar;
};
scout.inherits(scout.ButtonAdapterMenu, scout.Menu);

/**
 * @override Action.js
 */
scout.ButtonAdapterMenu.prototype._init = function(model) {
  scout.ButtonAdapterMenu.parent.prototype._init.call(this, model);
  if (!this.button) {
    throw new Error('Cannot adapt to undefined button');
  }
  this.button.adaptedBy = this;
  this._installListeners();
};

scout.ButtonAdapterMenu.prototype._destroy = function() {
  scout.ButtonAdapterMenu.parent.prototype._destroy.call(this);
  delete this.button.adaptedBy;
};

scout.ButtonAdapterMenu.prototype._installListeners = function() {
  this.button.on('propertyChange', this._buttonPropertyChangeHandler);
  this.button.on('destroy', this._buttonDestroyHandler);
};

scout.ButtonAdapterMenu.prototype._uninstallListeners = function() {
  this.button.off('propertyChange', this._buttonPropertyChangeHandler);
  this.button.off('destroy', this._buttonDestroyHandler);
};

scout.ButtonAdapterMenu.prototype._render = function() {
  scout.ButtonAdapterMenu.parent.prototype._render.call(this);
  // Convenience: Add ID of original button to DOM for debugging purposes
  this.$container.attr('data-buttonadapter', this.button.id);
};

scout.ButtonAdapterMenu.prototype._onButtonPropertyChange = function(event) {
  // Whenever a button property changes, apply the changes to the menu
  var changedProperties = {};
  changedProperties[event.propertyName] = event.newValue;
  changedProperties = scout.ButtonAdapterMenu.adaptButtonProperties(changedProperties);
  for (var prop in changedProperties) { // NOSONAR
    // Set the property (don't use callSetter because this may delegate to the button)
    this.setProperty(prop, changedProperties[prop]);
  }
};

scout.ButtonAdapterMenu.prototype._onButtonDestroy = function(event) {
  this.destroy();
  this._uninstallListeners();
};

/**
 * @override Action.js
 */
scout.ButtonAdapterMenu.prototype.doAction = function(srcEvent) {
  if (this.childActions.length > 0) {
    // Popup menu is handled by this menu itself
    return scout.ButtonAdapterMenu.parent.prototype.doAction.call(this, srcEvent);
  }

  // Everything else is delegated to the button
  var actionExecuted = this.button.doAction();
  if (actionExecuted) {
    if (this.isToggleAction()) {
      this.setSelected(!this.selected);
    }
    this._doAction();
  }
  return actionExecuted;
};

/**
 * @override
 */
scout.ButtonAdapterMenu.prototype.focus = function() {
  if (!this.rendered) {
    this.session.layoutValidator.schedulePostValidateFunction(this.focus.bind(this));
    return false;
  }
  this.menubar.setTabbableMenu(this);
  return this.session.focusManager.requestFocus(this.getFocusableElement());
};

/* --- STATIC HELPERS ------------------------------------------------------------- */

/**
 * @memberOf scout.ButtonAdapterMenu
 */
scout.ButtonAdapterMenu.adaptButtonProperties = function(buttonProperties, menuProperties) {
  menuProperties = menuProperties || {};

  // Plain properties: simply copy, no translation required
  ['visible', 'selected', 'tooltipText', 'keyStroke', 'keyStrokes', 'cssClass', 'modelClass', 'classId', 'iconId', 'preventDoubleClick', 'enabled', 'inheritAccessibility', 'stackable', 'shrinkable'].forEach(function(prop) {
    menuProperties[prop] = buttonProperties[prop];
  });

  // Properties requiring special handling (non-trivial mapping)
  menuProperties.text = buttonProperties.label;
  menuProperties.horizontalAlignment = (buttonProperties.gridData ? buttonProperties.gridData.horizontalAlignment : undefined);
  menuProperties.actionStyle = buttonStyleToActionStyle(buttonProperties.displayStyle);
  menuProperties.toggleAction = buttonProperties.displayStyle === scout.Button.DisplayStyle.TOGGLE;
  menuProperties.childActions = buttonProperties.menus;
  if (menuProperties.defaultMenu === undefined) {
    // buttonProperties.defaultButton property is only mapped if it is true, false should not be mapped as the default defaultMenu = null setting
    // would be overridden if this default null setting is overridden scout.MenuBar.prototype.updateDefaultMenu would not consider these entries anymore
    // on actual property changes defaultMenu will always be undefined which always maps the defaultButton property to the defaultMenu property
    menuProperties.defaultMenu = buttonProperties.defaultButton;
  }

  // Cleanup: Remove all properties that have value 'undefined' from the result object,
  // otherwise, they would be applied to the model adapter.
  for (var prop in menuProperties) {
    if (menuProperties[prop] === undefined) {
      delete menuProperties[prop];
    }
  }
  return menuProperties;

  // ----- Helper functions -----

  function buttonStyleToActionStyle(buttonStyle) {
    if (buttonStyle === undefined) {
      return undefined;
    }
    if (buttonStyle === scout.Button.DisplayStyle.LINK) {
      return scout.Action.ActionStyle.DEFAULT;
    } else {
      return scout.Action.ActionStyle.BUTTON;
    }
  }
};
