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
scout.ButtonAdapterMenu = function() {
  scout.ButtonAdapterMenu.parent.call(this);
  this._removeAdapterProperties('childActions'); // managed by button

  this._buttonPropertyChangeHandler = this._onButtonPropertyChange.bind(this);
  this._buttonDestroyHandler = this._onButtonDestroy.bind(this);

  this._addCloneProperties(['button']);
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

scout.ButtonAdapterMenu.prototype._render = function($parent) {
  scout.ButtonAdapterMenu.parent.prototype._render.call(this, $parent);
  // Convenience: Add ID of original button to DOM for debugging purposes
  this.$container.attr('data-buttonadapter', this.button.id);
};

scout.ButtonAdapterMenu.prototype._onButtonPropertyChange = function(event) {
  // Whenever a button property changes, apply the changes to the menu
  var changedProperties = {};
  changedProperties[event.name] = event.newValue;
  changedProperties = scout.ButtonAdapterMenu.adaptButtonProperties(changedProperties);
  for (var prop in changedProperties) { // NOSONAR
    this.callSetter(prop, changedProperties[prop]);
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
  if (actionExecuted && this.isToggleAction() && this.rendered) {
    this.setSelected(!this.selected);
  }
  return actionExecuted;
};

/**
 * @implements FormField.js
 */
scout.ButtonAdapterMenu.prototype.getFocusableElement = function() {
  if (this.rendered) {
    return this.$container[0];
  }
  return null;
};

/**
 * Sets the focus on this field if the field is rendered.
 */
scout.ButtonAdapterMenu.prototype.focus = function() { // XXX 7.0 - mit ASA nochmal anschauen --> focus wird jetzt nicht mehr aufgerufen / requestFocus ist kein guter ersatz
  if (!this.rendered) {
    return;
  }
  this.parent.setTabbableMenu(this);
  this.session.focusManager.requestFocus(this.$container[0]);
};


/* --- STATIC HELPERS ------------------------------------------------------------- */

/**
 * @memberOf scout.ButtonAdapterMenu
 */
scout.ButtonAdapterMenu.adaptButtonProperties = function(buttonProperties, menuProperties) {
  menuProperties = menuProperties || {};

  // Plain properties: simply copy, no translation required
  ['visible', 'selected', 'tooltipText', 'keyStroke', 'keyStrokes', 'modelClass', 'classId', 'iconId'].forEach(function(prop) {
    menuProperties[prop] = buttonProperties[prop];
  });

  // Properties requiring special handling (non-trivial mapping)
  menuProperties.enabled = buttonProperties.enabledComputed;
  menuProperties.text = buttonProperties.label;
  menuProperties.horizontalAlignment = (buttonProperties.gridData ? buttonProperties.gridData.horizontalAlignment : undefined);
  menuProperties.actionStyle = buttonStyleToActionStyle(buttonProperties.displayStyle);
  menuProperties.toggleAction = buttonProperties.displayStyle === scout.Button.DisplayStyle.TOGGLE;
  menuProperties.childActions = buttonProperties.menus;

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
