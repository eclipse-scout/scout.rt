import {CancelMenu, CheckBoxField, FormModel, GroupBox, NumberField, OkMenu, StringField} from '@eclipse-scout/core';

export default (): FormModel => ({
  displayHint: 'view',
  rootGroupBox: {
    id: 'MainBox',
    objectType: GroupBox,
    fields: [
      {
        id: 'DetailBox',
        objectType: GroupBox,
        fields: [
          {
            id: 'FirstNameField',
            objectType: StringField,
            label: '${symbol_dollar}{textKey:FirstName}',
            maxLength: 200
          },
          {
            id: 'LastNameField',
            objectType: StringField,
            label: '${symbol_dollar}{textKey:LastName}',
            maxLength: 200,
            mandatory: true
          },
          {
            id: 'SalaryField',
            objectType: NumberField,
            label: '${symbol_dollar}{textKey:Salary}',
            minValue: 0,
            maxValue: 99999999
          },
          {
            id: 'ExternalField',
            objectType: CheckBoxField,
            label: '${symbol_dollar}{textKey:External}'
          }
        ]
      }
    ],
    menus: [
      {
        id: 'OkMenu',
        objectType: OkMenu,
        tooltipText: '${symbol_dollar}{textKey:OkMenuTooltip}'
      },
      {
        id: 'CancelMenu',
        objectType: CancelMenu,
        tooltipText: '${symbol_dollar}{textKey:CancelMenuTooltip}'
      }
    ]
  }
});

export type PersonFormWidgetMap = {
  'MainBox': GroupBox;
  'DetailBox': GroupBox;
  'FirstNameField': StringField;
  'LastNameField': StringField;
  'SalaryField': NumberField;
  'ExternalField': CheckBoxField;
  'OkMenu': OkMenu;
  'CancelMenu': CancelMenu;
};
