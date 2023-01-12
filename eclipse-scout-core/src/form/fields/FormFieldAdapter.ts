/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {App, arrays, Event, FileDropEvent, FormField, FormFieldModel, ModelAdapter, objects} from '../../index';

export class FormFieldAdapter extends ModelAdapter {
  declare widget: FormField;

  /**
   * Set this property to true when the form-field should stay enabled in offline case.
   * By default, the field will be disabled.
   */
  enabledWhenOffline: boolean;
  protected _currentMenuTypes: string[];

  constructor() {
    super();
    this.enabledWhenOffline = false;
    this._currentMenuTypes = [];
  }

  protected override _initProperties(model: FormFieldModel & { currentMenuTypes: string[] }) {
    super._initProperties(model);
    this._currentMenuTypes = arrays.ensure(model.currentMenuTypes);
    delete model.currentMenuTypes;
  }

  protected _syncCurrentMenuTypes(currentMenuTypes: string | string[]) {
    this._currentMenuTypes = arrays.ensure(currentMenuTypes);
    this.widget._updateMenus();
  }

  protected override _goOffline() {
    if (this.enabledWhenOffline) {
      return;
    }
    this._enabledBeforeOffline = this.widget.enabled;
    this.widget.setEnabled(false);
  }

  protected override _goOnline() {
    if (this.enabledWhenOffline) {
      return;
    }
    this.widget.setEnabled(this._enabledBeforeOffline);
  }

  protected override _onWidgetEvent(event: Event<FormField>) {
    if (event.type === 'drop' && this.widget.dragAndDropHandler) {
      this.widget.dragAndDropHandler.uploadFiles(event as Event<FormField> & FileDropEvent);
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

  static getCurrentMenuTypes(): string[] {
    // @ts-expect-error
    let modelAdapter = this.modelAdapter as FormFieldAdapter;
    if (modelAdapter) {
      return modelAdapter._currentMenuTypes;
    }
    // @ts-expect-error
    return this.getCurrentMenuTypesOrig();
  }
}

App.addListener('bootstrap', FormFieldAdapter.modifyFormFieldPrototype);
