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
scout.TableControlAdapterMenu = function() {
  scout.TableControlAdapterMenu.parent.call(this);

  this._tableControlPropertyChangeHandler = this._onTableControlPropertyChange.bind(this);
  this._tableControlDestroyHandler = this._onTableControlDestroy.bind(this);

  this._addCloneProperties(['tableControl']);
};
scout.inherits(scout.TableControlAdapterMenu, scout.FormMenu);

/**
 * @override Action.js
 */
scout.TableControlAdapterMenu.prototype._init = function(model) {
  scout.TableControlAdapterMenu.parent.prototype._init.call(this, model);
  if (!this.tableControl) {
    throw new Error('Cannot adapt to undefined tableControl');
  }
  this._installListeners();
};

scout.TableControlAdapterMenu.prototype._installListeners = function() {
  this.tableControl.on('propertyChange', this._tableControlPropertyChangeHandler);
  this.tableControl.on('destroy', this._tableControlDestroyHandler);
};

scout.TableControlAdapterMenu.prototype._uninstallListeners = function() {
  this.tableControl.off('propertyChange', this._tableControlPropertyChangeHandler);
  this.tableControl.off('destroy', this._tableControlDestroyHandler);
};

scout.TableControlAdapterMenu.prototype._render = function($parent) {
  scout.TableControlAdapterMenu.parent.prototype._render.call(this, $parent);
  // Convenience: Add ID of original tableControl to DOM for debugging purposes
  this.$container.attr('data-tableControlAdapter', this.tableControl.id);
};

scout.TableControlAdapterMenu.prototype._onTableControlPropertyChange = function(event) {
  // Whenever a tableControl property changes, apply the changes to the menu
  var changedProperties = {};
  event.changedProperties.forEach(function(prop) {
    changedProperties[prop] = event.newProperties[prop];
  });
  this.onModelPropertyChange({
    properties: scout.TableControlAdapterMenu.adaptTableControlProperties(changedProperties)
  });
};

scout.TableControlAdapterMenu.prototype._onTableControlDestroy = function(event) {
  this.destroy();
  this._uninstallListeners();
};

/**
 * @override Action.js
 */
scout.TableControlAdapterMenu.prototype.doAction = function() {
  return this.tableControl.doAction();
};

scout.TableControlAdapterMenu.prototype._syncSelected = function(selected) {
  // Don't call super, because super prevents rendering and instead delegates to setSelected. But in this case rendering is needed
  this.selected = selected;
};

/**
 * @override Action.js
 */
scout.TableControlAdapterMenu.prototype.setSelected = function(selected, notifyServer) {
  this.tableControl.setSelected(selected, notifyServer);
};

/* --- STATIC HELPERS ------------------------------------------------------------- */

/**
 * @memberOf scout.TableControlAdapterMenu
 */
scout.TableControlAdapterMenu.adaptTableControlProperties = function(tableControlProperties, menuProperties) {
  menuProperties = menuProperties || {};

  // Plain properties: simply copy, no translation required
  ['text', 'iconId', 'enabled', 'visible', 'selected', 'tooltipText', 'keyStroke', 'keyStrokes', 'modelClass', 'classId', 'form'].forEach(function(prop) {
    menuProperties[prop] = tableControlProperties[prop];
  });

  // Cleanup: Remove all properties that have value 'undefined' from the result object,
  // otherwise, they would be applied to the model adapter.
  for (var prop in menuProperties) {
    if (menuProperties[prop] === undefined) {
      delete menuProperties[prop];
    }
  }
  return menuProperties;
};
