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
import {App, arrays, FormField, ModelAdapter, objects} from '../../index';

/**
 * @typedef FormFieldModel
 * @property {string[]} currentMenuTypes
 */

export default class FormFieldAdapter extends ModelAdapter {

  constructor() {
    super();

    /**
     * Set this property to true when the form-field should stay enabled in offline case.
     * By default the field will be disabled.
     */
    this.enabledWhenOffline = false;
    this._currentMenuTypes = [];
  }

  /**
   * @param {FormFieldModel} model
   */
  _initProperties(model) {
    super._initProperties(model);
    this._currentMenuTypes = arrays.ensure(model.currentMenuTypes);
    delete model.currentMenuTypes;
  }

  _syncCurrentMenuTypes(currentMenuTypes) {
    this._currentMenuTypes = arrays.ensure(currentMenuTypes);
    this.widget._updateMenus();
  }

  _goOffline() {
    if (this.enabledWhenOffline) {
      return;
    }
    this._enabledBeforeOffline = this.widget.enabled;
    this.widget.setEnabled(false);
  }

  _goOnline() {
    if (this.enabledWhenOffline) {
      return;
    }
    this.widget.setEnabled(this._enabledBeforeOffline);
  }

  _onWidgetEvent(event) {
    if (event.type === 'drop' && this.widget.dragAndDropHandler) {
      this.widget.dragAndDropHandler.uploadFiles(event);
    } else {
      super._onWidgetEvent(event);
    }
  }

  /**
   * Static method to modify the prototype of FormField.
   */
  static modifyFormFieldPrototype() {
    if (!App.get().remote) {
      return;
    }

    objects.replacePrototypeFunction(FormField, 'getCurrentMenuTypes', FormFieldAdapter.getCurrentMenuTypes, true);
  }

  static getCurrentMenuTypes() {
    if (this.modelAdapter) {
      return this.modelAdapter._currentMenuTypes;
    }
    return this.getCurrentMenuTypesOrig();
  }
}

App.addListener('bootstrap', FormFieldAdapter.modifyFormFieldPrototype);
