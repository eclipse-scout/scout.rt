/*
 * Copyright (c) 2017 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Distribution License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/org/documents/edl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
import {Status} from '../../index';
import {Form} from '../../index';
import {scout} from '../../index';
import {arrays} from '../../index';

export default class UnsavedFormChangesForm extends Form {

constructor() {
  super();

  this.unsavedForms = [];
}


_jsonModel() {
  return {
    id: 'scout.UnsavedFormChangesForm',
    objectType: 'Form',
    type: 'model',
    title: '${textKey:SaveChangesOfSelectedItems}',
    askIfNeedSave: false,
    rootGroupBox: {
      id: 'MainBox',
      objectType: 'GroupBox',
      menus: [{
        id: 'OkMenu',
        objectType: 'OkMenu'
      }, {
        id: 'CancelMenu',
        objectType: 'CancelMenu'
      }],
      fields: [{
        id: 'UnsavedChangesBox',
        objectType: 'GroupBox',
        labelVisible: false,
        gridColumnCount: 1,
        fields: [{
          id: 'OpenFormsField',
          objectType: 'ListBox',
          gridDataHints: {
            h: 5
          },
          labelVisible: false
        }]
      }]
    }
  };
}

_init(model) {
  super._init( model);

  this.openFormsField = this.widget('OpenFormsField');
  this.openFormsField.setLookupCall(scout.create('scout.UnsavedFormsLookupCall', {
    session: this.session,
    unsavedForms: this.unsavedForms
  }));

  this.openFormsField.table.one('rowsInserted', function(event) {
    event.source.checkAll(true);
  }.bind(this));

  var checkAllMenu = scout.create('Menu', {
    parent: this.openFormsField.table,
    id: 'CheckAllMenu',
    menuTypes: ['Table.EmptySpace'],
    text: '${textKey:CheckAll}'
  });

  checkAllMenu.on('action', function(event) {
    this.openFormsField.table.checkAll(true);
  }.bind(this));

  var uncheckAllMenu = scout.create('Menu', {
    parent: this.openFormsField.table,
    id: 'UncheckAllMenu',
    menuTypes: ['Table.EmptySpace'],
    text: '${textKey:UncheckAll}'
  });

  uncheckAllMenu.on('action', function(event) {
    this.openFormsField.table.uncheckAll();
  }.bind(this));

  this.openFormsField.table.setMenus([checkAllMenu, uncheckAllMenu]);

  this.on('postLoad', function(event) {
    this.touch();
  }.bind(this));
}

_validate(data) {
  var invalidForms = this.getInvalidForms();
  if (invalidForms.length > 0) {
    var msg = [];
    msg.push('<p><b>', this.session.text('NotAllCheckedFormsCanBeSaved'), '</b></p>');
    msg.push(this.session.text('FormsCannotBeSaved'), '<br><br>');
    invalidForms.forEach(function(form) {
      msg.push('- ', UnsavedFormChangesForm.getFormDisplayName(form), '<br>');
    }, this);
    return Status.error({
      message: msg.join('')
    });
  }
  return Status.ok();
}

getInvalidForms() {
  var invalidForms = [];
  this.openFormsField.value.forEach(function(form) {
    var missingElements = form.lifecycle._invalidElements().missingElements.slice();
    var invalidElements = form.lifecycle._invalidElements().invalidElements.slice();
    form.visitDisplayChildren(function(dialog) {
      var diagElem = dialog.lifecycle._invalidElements();
      arrays.pushAll(missingElements, diagElem.missingElements);
      arrays.pushAll(invalidElements, diagElem.invalidElements);
    }, function(dialog) {
      // forms are the only display children with a lifecycle, only visit those.
      return dialog instanceof Form;
    });
    if (missingElements.length > 0 || invalidElements.length > 0) {
      invalidForms.push(form);
    }
  });
  return invalidForms;
}

static getFormDisplayName(form) {
  return [form.title, form.name, form.subTitle].filter(Boolean).join(' - ');
}
}
