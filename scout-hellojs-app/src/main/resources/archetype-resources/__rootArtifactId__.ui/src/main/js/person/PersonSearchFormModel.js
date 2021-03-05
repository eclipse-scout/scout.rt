import {Action} from '@eclipse-scout/core';

export default () => ({
  id: '${simpleArtifactName}.PersonSearchForm',
  rootGroupBox: {
    id: 'MainBox',
    objectType: 'GroupBox',
    fields: [
      {
        id: 'DetailBox',
        objectType: 'GroupBox',
        gridColumnCount: 2,
        fields: [
          {
            id: 'FirstNameField',
            objectType: 'StringField',
            maxLength: 200,
            label: '${symbol_dollar}{textKey:FirstName}'
          },
          {
            id: 'LastNameField',
            objectType: 'StringField',
            maxLength: 200,
            label: '${symbol_dollar}{textKey:LastName}'
          }
        ]
      }
    ],
    menus: [
      {
        id: 'SearchButton',
        objectType: 'Menu',
        actionStyle: Action.ActionStyle.BUTTON,
        text: '${symbol_dollar}{textKey:Search}',
        keyStroke: 'ENTER'
      },
      {
        id: 'ResetMenu',
        objectType: 'ResetMenu'
      }
    ]
  }
});
