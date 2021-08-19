/*
 * Copyright (c) 2010-2021 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
import {arrays, Form, scout} from '../../index';
import $ from 'jquery';

export default class FormSpecHelper {
  constructor(session) {
    this.session = session;
  }

  closeMessageBoxes() {
    if (!this.session || !this.session.$entryPoint) {
      return;
    }
    let $messageBoxes = this.session.$entryPoint.find('.messagebox .box-button');
    for (let i = 0; i < $messageBoxes.length; i++) {
      scout.widget($messageBoxes[i]).doAction();
    }
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

  createFormWithFieldsAndTabBoxes(model) {
    let fieldModelPart = (id, mandatory) => ({
        id: id,
        objectType: 'StringField',
        label: id,
        mandatory: mandatory
      }),
      tabBoxModelPart = (id, tabItems) => ({
        id: id,
        objectType: 'TabBox',
        tabItems: tabItems
      }),
      tabItemModelPart = (id, fields) => ({
        id: id,
        objectType: 'TabItem',
        label: 'id',
        fields: fields
      }),
      tableFieldModelPart = (id, columns) => ({
        id: id,
        objectType: 'TableField',
        label: id,
        table: {
          id: id + 'Table',
          objectType: 'Table',
          columns: columns
        }
      }),
      columnModelPart = (id, mandatory) => ({
        id: id,
        objectType: 'Column',
        text: id,
        editable: true,
        mandatory: mandatory
      });

    let defaults = {
      parent: this.session.desktop,
      id: 'Form',
      title: 'Form',
      rootGroupBox: {
        id: 'RootGroupBox',
        objectType: 'GroupBox',
        fields: [
          fieldModelPart('Field1', false),
          fieldModelPart('Field2', false),
          fieldModelPart('Field3', true),
          fieldModelPart('Field4', true),
          tabBoxModelPart('TabBox', [
            tabItemModelPart('TabA', [
              fieldModelPart('FieldA1', false),
              fieldModelPart('FieldA2', true),
              tabBoxModelPart('TabBoxA', [
                tabItemModelPart('TabAA', [
                  fieldModelPart('FieldAA1', false),
                  fieldModelPart('FieldAA2', true)
                ]),
                tabItemModelPart('TabAB', [
                  fieldModelPart('FieldAB1', false),
                  fieldModelPart('FieldAB2', true)
                ]),
                tabItemModelPart('TabAC', [
                  fieldModelPart('FieldAC1', false),
                  fieldModelPart('FieldAC2', true)
                ])
              ])
            ]),
            tabItemModelPart('TabB', [
              fieldModelPart('FieldB1', false),
              fieldModelPart('FieldB2', false),
              fieldModelPart('FieldB3', true),
              fieldModelPart('FieldB4', true),
              tableFieldModelPart('TableFieldB5', [
                columnModelPart('ColumnB51', false),
                columnModelPart('ColumnB52', true)
              ])
            ])
          ])
        ]
      }
    };

    model = $.extend({}, defaults, model);
    let form = scout.create('Form', model);
    form.widget('TableFieldB5').table.insertRows([{cells: arrays.init(2)}, {cells: arrays.init(2)}]);
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
