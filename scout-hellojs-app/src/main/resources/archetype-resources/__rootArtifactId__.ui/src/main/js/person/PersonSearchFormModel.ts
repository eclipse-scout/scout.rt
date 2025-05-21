import {FormModel, GroupBox, ResetMenu, SearchMenu, StringField} from '@eclipse-scout/core';

export default (): FormModel => ({
  rootGroupBox: {
    id: 'MainBox',
    objectType: GroupBox,
    fields: [
      {
        id: 'DetailBox',
        objectType: GroupBox,
        gridColumnCount: 2,
        fields: [
          {
            id: 'FirstNameField',
            objectType: StringField,
            maxLength: 200,
            label: '${symbol_dollar}{textKey:FirstName}'
          },
          {
            id: 'LastNameField',
            objectType: StringField,
            maxLength: 200,
            label: '${symbol_dollar}{textKey:LastName}'
          }
        ]
      }
    ],
    menus: [
      {
        id: 'SearchMenu',
        objectType: SearchMenu
      },
      {
        id: 'ResetMenu',
        objectType: ResetMenu
      }
    ]
  }
});

/* **************************************************************************
* GENERATED WIDGET MAPS
* **************************************************************************/

export type PersonSearchFormWidgetMap = {
  'MainBox': GroupBox;
  'DetailBox': GroupBox;
  'FirstNameField': StringField;
  'LastNameField': StringField;
  'SearchMenu': SearchMenu;
  'ResetMenu': ResetMenu;
};
