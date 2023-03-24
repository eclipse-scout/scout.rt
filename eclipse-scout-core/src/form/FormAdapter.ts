/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {App, ChildModelOf, Event, Form, FormField, FullModelOf, ModelAdapter, objects, RemoteEvent, Widget} from '../index';

export class FormAdapter extends ModelAdapter {
  declare widget: Form;

  protected override _initModel(m: ChildModelOf<Widget>, parent: Widget): FullModelOf<Widget> {
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
        this.widget._afterAbort();
      }
    });
  }

  protected _onWidgetClose(event: Event<Form>) {
    this._send('close');
  }

  override onModelAction(event: RemoteEvent) {
    if (event.type === 'requestFocus') {
      this._onRequestFocus(event as any);
    } else if (event.type === 'requestInput') {
      this._onRequestInput(event as any);
    } else {
      super.onModelAction(event);
    }
  }

  protected _onRequestFocus(event: RemoteEvent & { formField: string }) {
    this.session.getOrCreateWidget(event.formField, this.widget).focus();
  }

  protected _onRequestInput(event: RemoteEvent & { formField: string }) {
    (this.session.getOrCreateWidget(event.formField, this.widget) as FormField).requestInput();
  }

  static modifyFormPrototype() {
    if (!App.get().remote) {
      return;
    }

    objects.replacePrototypeFunction(Form, Form.prototype.updateSaveNeeded, FormAdapter.updateSaveNeeded, true);
  }

  static updateSaveNeeded(this: Form & { updateSaveNeededOrig: typeof Form.prototype.updateSaveNeeded }) {
    if (!this.modelAdapter) {
      return this.updateSaveNeededOrig();
    }
    // Don't update state on remote forms
  }
}

App.addListener('bootstrap', FormAdapter.modifyFormPrototype);
