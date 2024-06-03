/*
 * Copyright (c) 2010, 2024 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {BookmarkDo, CancelMenu, Form, FormModel, GroupBox, OkMenu, StringField} from '../index';

export default (): FormModel => ({
  objectType: Form,
  title: 'Bookmark', // FIXME bsh [js-bookmark] NLS
  saveNeededVisible: false,
  rootGroupBox: {
    id: 'MainBox',
    objectType: GroupBox,
    mainBox: true,
    gridColumnCount: 1,
    menus: [
      {
        id: 'OkMenu',
        objectType: OkMenu
      },
      {
        id: 'CancelMenu',
        objectType: CancelMenu
      }
    ],
    fields: [
      {
        id: 'GroupBox',
        objectType: GroupBox,
        statusVisible: false,
        gridDataHints: {
          w: 1
        },
        fields: [
          {
            id: 'NameField',
            objectType: StringField,
            label: 'Name', // FIXME bsh [js-bookmark] NLS
            mandatory: true
          }
        ]
      }
    ]
  }
});

export interface BookmarkFormModel extends FormModel {
  bookmark: BookmarkDo;
}

/* **************************************************************************
* GENERATED WIDGET MAPS
* **************************************************************************/

export type BookmarkFormWidgetMap = {
  'MainBox': GroupBox;
  'OkMenu': OkMenu;
  'CancelMenu': CancelMenu;
  'GroupBox': GroupBox;
  'NameField': StringField;
};
