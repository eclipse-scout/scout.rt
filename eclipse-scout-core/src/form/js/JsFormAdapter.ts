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
import {Event, Form, FormAdapter, FormModel, JsFormModel, ObjectWithType, Widget} from '../../index';

export default class JsFormAdapter extends FormAdapter {

  constructor() {
    super();
  }

  protected override _initModel(m: Omit<JsFormModel, 'parent'> & ObjectWithType, parent: Widget): FormModel & ObjectWithType {
    let model = super._initModel(m, parent) as JsFormModel;

    if (!model.jsFormObjectType || !model.jsFormObjectType.length) {
      throw new Error('jsFormObjectType not set');
    }

    let jsFormModel = {
      parent: model.parent,
      owner: model.owner,
      objectType: model.jsFormObjectType,
      displayParent: model.displayParent,
      displayHint: model.displayHint,
      data: model.inputData
    };

    if (model.jsFormModel) {
      jsFormModel = $.extend(true, {}, model.jsFormModel, jsFormModel);
    }

    return jsFormModel;
  }

  protected override _createWidget(model: FormModel & ObjectWithType): Form {
    let widget = super._createWidget(model) as Form;

    widget.showOnOpen = false;
    // noinspection JSIgnoredPromiseFromCall
    widget.open();

    return widget;
  }

  protected override _onWidgetEvent(event: Event<Form>) {
    if (event.type === 'save') {
      this._onWidgetSave(event);
    } else {
      super._onWidgetEvent(event);
    }
  }

  protected _onWidgetSave(event: Event<Form>) {
    this._send('save', {
      outputData: this.widget.data
    });
  }

  protected override _onWidgetClose(event: Event<Form>) {
    // marks the end of the js lifecycle
    // prevent remove/destroy of the widget as it will be done by the UI server
    event.preventDefault();
    // fromClosing will trigger a 'formHide' event on the desktop which then removes the widget
    this._send('formClosing');
  }

  protected override _onWidgetAbort(event: Event<Form>) {
    // completely handled by the js lifecycle -> no need to notify the UI server
  }
}
