/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {ChildModelOf, Event, Form, FormAdapter, FullModelOf, JsFormModel, Widget} from '../../index';

export class JsFormAdapter extends FormAdapter {

  protected override _initModel(m: ChildModelOf<Widget>, parent: Widget): FullModelOf<Widget> {
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

  protected override _createWidget(model: FullModelOf<Form>): Form {
    let widget;
    if (model.exclusiveKey) {
      widget = this.session.desktop.createFormExclusive(() => super._createWidget(model) as Form, model.exclusiveKey);
      if (!widget.exclusiveAdapterKey) {
        // Link the form with this adapter. The form may be an existing form created by JS code or created now by Java.
        widget.exclusiveAdapterKey = this.id;
      } else if (widget.exclusiveAdapterKey !== this.id) {
        // If the found form belongs to this adapter, it can be opened exclusively.
        // Otherwise, it needs to be opened separately because the server may send events for that form (formHide etc.).
        // To prevent that, the server needs to ensure exclusiveness as well.
        widget = super._createWidget(model) as Form;
      }
    } else {
      widget = super._createWidget(model) as Form;
    }

    if (!widget.showOnOpen) {
      widget.open();
    } else {
      widget.blockRendering = true;
      widget.open();
      widget.whenPostLoad().then(() => {
        widget.blockRendering = false;
        if (widget.destroyed || !widget.showOnOpen) {
          return;
        }
        widget.show();
      });
    }

    return widget;
  }

  protected override _onWidgetEvent(event: Event<Form>) {
    if (event.type === 'save') {
      this._onWidgetSave(event);
    } else if (event.type === 'search') {
      this._onWidgetSearch(event);
    } else if (event.type === 'reset') {
      this._onWidgetReset(event);
    } else {
      super._onWidgetEvent(event);
    }
  }

  protected _onWidgetSave(event: Event<Form>) {
    this._sendOutputData('save', this.widget.data);
  }

  protected _onWidgetSearch(event: Event<Form>) {
    this._sendOutputData('search', this.widget.exportData());
  }

  protected _onWidgetReset(event: Event<Form>) {
    this._sendOutputData('reset', this.widget.exportData());
  }

  protected _sendOutputData(type: string, data?: any) {
    this._send(type, {
      outputData: data
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
