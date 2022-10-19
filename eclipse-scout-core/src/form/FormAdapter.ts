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
import {Event, Form, FormField, FormModel, ModelAdapter, Widget} from '../index';
import {RefModel, SomeRequired} from '../types';

export default class FormAdapter extends ModelAdapter {
  declare widget: Form;

  constructor() {
    super();
  }

  protected override _initModel(m: RefModel<FormModel>, parent: Widget): SomeRequired<FormModel, 'objectType'> {
    let model = super._initModel(m, parent);
    // Set logical grid to null -> Calculation happens on server side
    model.logicalGrid = null;
    return model;
  }

  protected override _onWidgetEvent(event: Event<Form>) {
    if (event.type === 'abort') {
      this._onWidgetAbort(event);
    } else if (event.type === 'close') {
      this._onWidgetClose(event);
    } else {
      super._onWidgetEvent(event);
    }
  }

  protected _onWidgetAbort(event: Event<Form>) {
    // Do not close the form immediately, server will send the close event
    event.preventDefault();

    this._send('formClosing');
    // Waiting for the current request to complete is necessary to be able to check whether the form is still open after the close request.
    this.session.onRequestsDone(() => {
      if (this.widget) {
        // @ts-ignore
        this.widget._afterAbort();
      }
    });
  }

  protected _onWidgetClose(event: Event<Form>) {
    this._send('close');
  }

  override onModelAction(event: any) {
    if (event.type === 'requestFocus') {
      this._onRequestFocus(event);
    } else if (event.type === 'requestInput') {
      this._onRequestInput(event);
    } else {
      super.onModelAction(event);
    }
  }

  protected _onRequestFocus(event: { formField: string }) {
    this.session.getOrCreateWidget(event.formField, this.widget).focus();
  }

  protected _onRequestInput(event: { formField: string }) {
    (this.session.getOrCreateWidget(event.formField, this.widget) as FormField).requestInput();
  }
}
