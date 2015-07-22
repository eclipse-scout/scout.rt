scout.ButtonAdapterMenu = function() {
  scout.ButtonAdapterMenu.parent.call(this);
  this._removeAdapterProperties('childActions'); // managed by button
  this.defaultKeyStroke;
};
scout.inherits(scout.ButtonAdapterMenu, scout.Menu);

/**
 * @override Action.js
 */
scout.ButtonAdapterMenu.prototype._init = function(model, session) {
  scout.ButtonAdapterMenu.parent.prototype._init.call(this, model, session);
  if (!this.button) {
    throw new Error('Cannot adapt to undefined button');
  }
  this._installListeners();
};

scout.ButtonAdapterMenu.prototype._installListeners = function() {
  this.button.on('propertyChange', this._onButtonPropertyChange.bind(this));
  this.button.on('destroy', this._onButtonDestroy.bind(this));
};

scout.ButtonAdapterMenu.prototype._uninstallListeners = function() {
  this.button.off('propertyChange');
  this.button.off('destroy');
};

scout.ButtonAdapterMenu.prototype._onButtonPropertyChange = function(event) {
  // Whenever a button property changes, apply the changes to the menu
  var changedProperties = {};
  event.changedProperties.forEach(function(prop) {
    changedProperties[prop] = event.newProperties[prop];
  });
  this.onModelPropertyChange({
    properties: scout.ButtonAdapterMenu.adaptButtonProperties(changedProperties)
  });
};

scout.ButtonAdapterMenu.prototype._onButtonDestroy = function(event) {
  this.remove();
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
  // Eventhing else is delegated to the button
  return this.button.doAction();
};

/* --- STATIC HELPERS ------------------------------------------------------------- */

/**
 * @memberOf scout.ButtonAdapterMenu
 */
scout.ButtonAdapterMenu.adaptButtonProperties = function(buttonProperties, menuProperties) {
  menuProperties = menuProperties || {};

  // Plain properties: simply copy, no translation required
  ['enabled', 'visible', 'selected', 'tooltipText', 'keyStroke', 'keyStrokes', 'modelClass', 'classId'].forEach(function(prop) {
    menuProperties[prop] = buttonProperties[prop];
  });

  // Properties requiring special handling (non-trivial mapping)
  menuProperties.text = scout.strings.removeAmpersand(buttonProperties.label);
  menuProperties.horizontalAlignment = (buttonProperties.gridData ? buttonProperties.gridData.horizontalAlignment : undefined);
  menuProperties.actionStyle = buttonStyleToActionStyle(buttonProperties.displayStyle);
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
    if (buttonStyle === scout.Button.DisplayStyle.TOGGLE) {
      return scout.Action.ActionStyle.TOGGLE;
    } else if (buttonStyle === scout.Button.DisplayStyle.LINK) {
      return scout.Action.ActionStyle.DEFAULT;
    } else {
      return scout.Action.ActionStyle.BUTTON;
    }
  }
};
