/*
 * Copyright (c) 2010-2020 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
import {Form, scout} from '../../index';
import $ from 'jquery';

export default class FormSpecHelper {
  constructor(session) {
    this.session = session;
  }

  createViewWithOneField(model) {
    let form = this.createFormWithOneField(model);
    form.displayHint = Form.DisplayHint.VIEW;
    return form;
  }

  createFormWithOneField(model) {
    let defaults = {
      parent: this.session.desktop
    };
    model = $.extend({}, defaults, model);
    let form = scout.create('Form', model);
    let rootGroupBox = this.createGroupBoxWithFields(form, 1);
    form._setRootGroupBox(rootGroupBox);
    return form;
  }

  createGroupBoxWithOneField(parent, numFields) {
    return this.createGroupBoxWithFields(parent, 1);
  }

  createGroupBoxWithFields(parent, numFields) {
    parent = scout.nvl(parent, this.session.desktop);
    numFields = scout.nvl(numFields, 1);
    let
      fields = [],
      groupBox = scout.create('GroupBox', {
        parent: parent
      });
    for (let i = 0; i < numFields; i++) {
      fields.push(scout.create('StringField', {
        parent: groupBox
      }));
    }
    groupBox.setProperty('fields', fields);
    return groupBox;
  }

  createRadioButtonGroup(parent, numRadioButtons) {
    parent = scout.nvl(parent, this.session.desktop);
    numRadioButtons = scout.nvl(numRadioButtons, 2);
    let fields = [];
    for (let i = 0; i < numRadioButtons; i++) {
      fields.push({
        objectType: 'RadioButton'
      });
    }
    return scout.create('RadioButtonGroup', {
      parent: parent,
      fields: fields
    });
  }

  createFormWithFields(parent, isModal, numFields) {
    parent = scout.nvl(parent, this.session.desktop);
    let form = scout.create('Form', {
      parent: parent,
      displayHint: isModal ? 'dialog' : 'view'
    });
    let rootGroupBox = this.createGroupBoxWithFields(form, numFields);
    form._setRootGroupBox(rootGroupBox);
    return form;
  }

  createFieldModel(objectType, parent, modelProperties) {
    parent = scout.nvl(parent, this.session.desktop);
    let model = createSimpleModel(objectType || 'StringField', this.session);
    model.parent = parent;
    if (modelProperties) {
      $.extend(model, modelProperties);
    }
    return model;
  }

  createField(objectType, parent, modelProperties) {
    parent = parent || this.session.desktop;
    return scout.create(objectType, this.createFieldModel(objectType, parent, modelProperties));
  }

  createModeSelector(parent, numModes) {
    parent = scout.nvl(parent, this.session.desktop);
    numModes = scout.nvl(numModes, 2);
    let modes = [];
    for (let i = 0; i < numModes; i++) {
      modes.push({
        objectType: 'Mode'
      });
    }
    return scout.create('ModeSelector', {
      parent: parent,
      modes: modes
    });
  }

}
