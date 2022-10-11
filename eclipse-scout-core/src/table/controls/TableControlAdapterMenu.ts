/*
 * Copyright (c) 2010-2022 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
import {Event, EventHandler, FormMenu, PropertyChangeEvent, TableControl, TableControlAdapterMenuModel, TableControlModel} from '../../index';

export default class TableControlAdapterMenu extends FormMenu implements TableControlAdapterMenuModel {
  declare model: TableControlAdapterMenuModel;

  tableControl: TableControl;
  protected _tableControlPropertyChangeHandler: EventHandler<PropertyChangeEvent<any, TableControl>>;
  protected _tableControlDestroyHandler: EventHandler<Event<TableControl>>;

  constructor() {
    super();

    this.tableControl = null;
    this._tableControlPropertyChangeHandler = this._onTableControlPropertyChange.bind(this);
    this._tableControlDestroyHandler = this._onTableControlDestroy.bind(this);

    this._addCloneProperties(['tableControl']);
  }

  protected override _init(model: TableControlAdapterMenuModel) {
    super._init(model);
    if (!this.tableControl) {
      throw new Error('Cannot adapt to undefined tableControl');
    }
    this._installListeners();
  }

  protected override _destroy() {
    this._uninstallListeners();
    super._destroy();
  }

  protected _installListeners() {
    this.tableControl.on('propertyChange', this._tableControlPropertyChangeHandler);
    this.tableControl.on('destroy', this._tableControlDestroyHandler);
  }

  protected _uninstallListeners() {
    this.tableControl.off('propertyChange', this._tableControlPropertyChangeHandler);
    this.tableControl.off('destroy', this._tableControlDestroyHandler);
  }

  protected override _render() {
    super._render();
    // Convenience: Add ID of original tableControl to DOM for debugging purposes
    this.$container.attr('data-tableControlAdapter', this.tableControl.id);
  }

  protected _onTableControlPropertyChange(event: PropertyChangeEvent<any, TableControl>) {
    // Whenever a tableControl property changes, apply the changes to the menu
    let changedProperties: Omit<TableControlModel, 'parent'> = {};
    changedProperties[event.propertyName] = event.newValue;
    changedProperties = TableControlAdapterMenu.adaptTableControlProperties(changedProperties);
    for (let prop in changedProperties) {
      // Set the property (don't use callSetter because this may delegate to the table control)
      this.setProperty(prop, changedProperties[prop]);
    }
  }

  protected _onTableControlDestroy(event: Event<TableControl>) {
    this.destroy();
    this._uninstallListeners();
  }

  override doAction() {
    return this.tableControl.doAction();
  }

  override setSelected(selected: boolean) {
    this.tableControl.setSelected(selected);
  }

  /* --- STATIC HELPERS ------------------------------------------------------------- */

  static adaptTableControlProperties(tableControlProperties: Omit<TableControlModel, 'parent'>, menuProperties?: Omit<TableControlAdapterMenuModel, 'parent'>): TableControlAdapterMenuModel {
    menuProperties = menuProperties || {};

    // Plain properties: simply copy, no translation required
    ['text', 'iconId', 'enabled', 'visible', 'selected', 'tooltipText', 'keyStroke', 'keyStrokes', 'modelClass', 'classId', 'form'].forEach(prop => {
      menuProperties[prop] = tableControlProperties[prop];
    });

    // Cleanup: Remove all properties that have value 'undefined' from the result object, otherwise, they would be applied to the model adapter.
    for (let prop in menuProperties) {
      if (menuProperties[prop] === undefined) {
        delete menuProperties[prop];
      }
    }
    return menuProperties as TableControlAdapterMenuModel;
  }
}
