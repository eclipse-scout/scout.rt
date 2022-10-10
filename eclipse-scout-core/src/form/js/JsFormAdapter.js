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
import {FormAdapter} from '../../index';

/**
 * @typedef JsFormModel
 * @property {object} parent
 * @property {object} owner
 * @property {object} displayParent
 * @property {string} displayHint
 * @property {object} inputData
 * @property {string} jsFormObjectType
 * @property {object} jsFormModel
 */

export default class JsFormAdapter extends FormAdapter {

  constructor() {
    super();
  }

  /**
   * @param {JsFormModel} model
   */
  _initModel(model, parent) {
    model = super._initModel(model, parent);

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

  _createWidget(model) {
    let widget = super._createWidget(model);

    widget.showOnOpen = false;
    widget.open();

    return widget;
  }

  _onWidgetEvent(event) {
    if (event.type === 'save') {
      this._onWidgetSave(event);
    } else {
      super._onWidgetEvent(event);
    }
  }

  _onWidgetSave(event) {
    this._send('save', {
      outputData: this.widget.data
    });
  }

  _onWidgetClose(event) {
    // marks the end of the js lifecycle
    // prevent remove/destroy of the widget as it will be done by the UI server
    event.preventDefault();
    // fromClosing will trigger a 'formHide' event on the desktop which then removes the widget
    this._send('formClosing');
  }

  _onWidgetAbort(event) {
    // completely handled by the js lifecycle -> no need to notify the UI server
  }
}
