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
import {arrays, CancelMenu, Form, FormModel, GroupBox, ListBox, Menu, OkMenu, scout, Status, UnsavedFormChangesFormModel, UnsavedFormsLookupCall} from '../../index';
import {RefModel} from '../../types';
import {TableRowsInsertedEvent} from '../../table/TableEventMap';

export default class UnsavedFormChangesForm extends Form implements UnsavedFormChangesFormModel {
  declare model: UnsavedFormChangesFormModel;

  unsavedForms: Form[];
  openFormsField: ListBox;

  constructor() {
    super();
    this.unsavedForms = [];
  }

  protected override _jsonModel(): RefModel<FormModel> {
    return {
      id: 'scout.UnsavedFormChangesForm',
      objectType: Form,
      type: 'model',
      title: '${textKey:SaveChangesOfSelectedItems}',
      askIfNeedSave: false,
      rootGroupBox: {
        id: 'MainBox',
        objectType: GroupBox,
        menus: [{
          id: 'OkMenu',
          objectType: OkMenu
        }, {
          id: 'CancelMenu',
          objectType: CancelMenu
        }],
        fields: [{
          id: 'UnsavedChangesBox',
          objectType: GroupBox,
          labelVisible: false,
          gridColumnCount: 1,
          fields: [{
            id: 'OpenFormsField',
            objectType: ListBox,
            gridDataHints: {
              h: 5
            },
            labelVisible: false
          }]
        }]
      }
    };
  }

  protected override _init(model: UnsavedFormChangesFormModel) {
    super._init(model);

    this.openFormsField = this.widget('OpenFormsField', ListBox);
    this.openFormsField.setLookupCall(scout.create(UnsavedFormsLookupCall, {
      session: this.session,
      unsavedForms: this.unsavedForms
    }));

    this.openFormsField.table.one('rowsInserted', (event: TableRowsInsertedEvent) => {
      event.source.checkAll(true);
    });

    let checkAllMenu = scout.create(Menu, {
      parent: this.openFormsField.table,
      id: 'CheckAllMenu',
      menuTypes: ['Table.EmptySpace'],
      text: '${textKey:CheckAll}'
    });

    checkAllMenu.on('action', event => this.openFormsField.table.checkAll(true));

    let uncheckAllMenu = scout.create(Menu, {
      parent: this.openFormsField.table,
      id: 'UncheckAllMenu',
      menuTypes: ['Table.EmptySpace'],
      text: '${textKey:UncheckAll}'
    });

    uncheckAllMenu.on('action', event => this.openFormsField.table.uncheckAll());

    this.openFormsField.table.setMenus([checkAllMenu, uncheckAllMenu]);

    this.on('postLoad', event => this.touch());
  }

  protected override _validate(): Status {
    let invalidForms = this.getInvalidForms();
    if (invalidForms.length > 0) {
      let msg: string[] = [];
      msg.push('<p><b>', this.session.text('NotAllCheckedFormsCanBeSaved'), '</b></p>');
      msg.push(this.session.text('FormsCannotBeSaved'), '<br><br>');
      invalidForms.forEach(form => msg.push('- ', UnsavedFormChangesForm.getFormDisplayName(form), '<br>'));
      return Status.error({
        message: msg.join('')
      });
    }
    return Status.ok();
  }

  getInvalidForms(): Form[] {
    let invalidForms: Form[] = [];
    this.openFormsField.value.forEach((form: Form) => {
      // @ts-ignore
      let diagElem = form.lifecycle._invalidElements();
      let missingElements = diagElem.missingElements.slice();
      let invalidElements = diagElem.invalidElements.slice();
      form.visitDisplayChildren((dialog: Form) => {
        // @ts-ignore
        let diagElem = dialog.lifecycle._invalidElements();
        arrays.pushAll(missingElements, diagElem.missingElements);
        arrays.pushAll(invalidElements, diagElem.invalidElements);
      }, dialog => {
        // forms are the only display children with a lifecycle, only visit those.
        return dialog instanceof Form;
      });
      if (missingElements.length > 0 || invalidElements.length > 0) {
        invalidForms.push(form);
      }
    });
    return invalidForms;
  }

  static getFormDisplayName(form: Form): string {
    // @ts-ignore
    return [form.title, form.name, form.subTitle].filter(Boolean).join(' - ');
  }
}
