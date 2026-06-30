/*
 * Copyright (c) 2010, 2026 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {BookmarkDo, CancelMenu, Form, FormField, FormModel, GroupBox, OkMenu, StringField} from '../index';

export default (): FormModel => ({
  objectType: Form,
  title: '${textKey:Bookmark}',
  saveNeededVisible: false,
  rootGroupBox: {
    id: 'MainBox',
    objectType: GroupBox,
    mainBox: true,
    gridDataHints: {
      widthInPixel: 600
    },
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
          w: FormField.FULL_WIDTH
        },
        fields: [
          {
            id: 'NameField',
            objectType: StringField,
            label: '${textKey:Name}',
            mandatory: true
          }
        ]
      }
    ]
  }
});

export interface BookmarkFormModel extends FormModel {
  /**
   * The bookmark to edit. If this is set, no data is loaded from or saved to the {@link BookmarkStore bookmark store}.
   */
  bookmark?: BookmarkDo;
  /**
   * The ID of the bookmark to edit. If this is set, the bookmark is loaded from and saved to the {@link BookmarkStore bookmark store}.
   */
  bookmarkId?: string;
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
