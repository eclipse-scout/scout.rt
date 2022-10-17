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
import {App, arrays, Event, FormField, FormFieldModel, ModelAdapter, objects} from '../../index';
import {FileDropEvent} from '../../util/dragAndDrop';

export default class FormFieldAdapter extends ModelAdapter {
  declare widget: FormField;

  /**
   * Set this property to true when the form-field should stay enabled in offline case.
   * By default the field will be disabled.
   */
  enabledWhenOffline: boolean;
  protected _currentMenuTypes: string[];

  constructor() {
    super();
    this.enabledWhenOffline = false;
    this._currentMenuTypes = [];
  }

  protected override _initProperties(model: FormFieldModel) {
    super._initProperties(model);
    this._currentMenuTypes = arrays.ensure(model.currentMenuTypes);
    delete model.currentMenuTypes;
  }

  protected _syncCurrentMenuTypes(currentMenuTypes: string | string[]) {
    this._currentMenuTypes = arrays.ensure(currentMenuTypes);
    // @ts-ignore
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
    // @ts-ignore
    let modelAdapter = this.modelAdapter as FormFieldAdapter;
    if (modelAdapter) {
      return modelAdapter._currentMenuTypes;
    }
    // @ts-ignore
    return this.getCurrentMenuTypesOrig();
  }
}

App.addListener('bootstrap', FormFieldAdapter.modifyFormFieldPrototype);
