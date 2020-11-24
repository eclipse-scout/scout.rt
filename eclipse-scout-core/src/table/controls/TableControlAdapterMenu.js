/*
 * Copyright (c) 2014-2017 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
import {FormMenu} from '../../index';

export default class TableControlAdapterMenu extends FormMenu {

  constructor() {
    super();

    this.tableControl = null;
    this._tableControlPropertyChangeHandler = this._onTableControlPropertyChange.bind(this);
    this._tableControlDestroyHandler = this._onTableControlDestroy.bind(this);

    this._addCloneProperties(['tableControl']);
  }

  /**
   * @override Action.js
   */
  _init(model) {
    super._init(model);
    if (!this.tableControl) {
      throw new Error('Cannot adapt to undefined tableControl');
    }
    this._installListeners();
  }

  _destroy() {
    this._uninstallListeners();
    super._destroy();
  }

  _installListeners() {
    this.tableControl.on('propertyChange', this._tableControlPropertyChangeHandler);
    this.tableControl.on('destroy', this._tableControlDestroyHandler);
  }

  _uninstallListeners() {
    this.tableControl.off('propertyChange', this._tableControlPropertyChangeHandler);
    this.tableControl.off('destroy', this._tableControlDestroyHandler);
  }

  _render() {
    super._render();
    // Convenience: Add ID of original tableControl to DOM for debugging purposes
    this.$container.attr('data-tableControlAdapter', this.tableControl.id);
  }

  _onTableControlPropertyChange(event) {
    // Whenever a tableControl property changes, apply the changes to the menu
    let changedProperties = {};
    changedProperties[event.propertyName] = event.newValue;
    changedProperties = TableControlAdapterMenu.adaptTableControlProperties(changedProperties);
    for (let prop in changedProperties) { // NOSONAR
      // Set the property (don't use callSetter because this may delegate to the table control)
      this.setProperty(prop, changedProperties[prop]);
    }
  }

  _onTableControlDestroy(event) {
    this.destroy();
    this._uninstallListeners();
  }

  /**
   * @override Action.js
   */
  doAction() {
    return this.tableControl.doAction();
  }

  /**
   * @override Action.js
   */
  setSelected(selected) {
    this.tableControl.setSelected(selected);
  }

  /* --- STATIC HELPERS ------------------------------------------------------------- */

  /**
   * @memberOf TableControlAdapterMenu
   */
  static adaptTableControlProperties(tableControlProperties, menuProperties) {
    menuProperties = menuProperties || {};

    // Plain properties: simply copy, no translation required
    ['text', 'iconId', 'enabled', 'visible', 'selected', 'tooltipText', 'keyStroke', 'keyStrokes', 'modelClass', 'classId', 'form'].forEach(prop => {
      menuProperties[prop] = tableControlProperties[prop];
    });

    // Cleanup: Remove all properties that have value 'undefined' from the result object,
    // otherwise, they would be applied to the model adapter.
    for (let prop in menuProperties) {
      if (menuProperties[prop] === undefined) {
        delete menuProperties[prop];
      }
    }
    return menuProperties;
  }
}
