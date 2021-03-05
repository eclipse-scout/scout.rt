import {icons} from '@eclipse-scout/core';

export default () => ({
  id: '${simpleArtifactName}.PersonTablePage',
  objectType: 'PageWithTable',
  leaf: true,
  text: '${symbol_dollar}{textKey:Persons}',
  detailTable: {
    id: '${simpleArtifactName}.PersonTablePage.Table',
    objectType: 'Table',
    autoResizeColumns: true,
    columns: [
      {
        id: 'PersonIdColumn',
        objectType: 'Column',
        visible: false
      },
      {
        id: 'FirstNameColumn',
        objectType: 'Column',
        text: '${symbol_dollar}{textKey:FirstName}',
        width: 300
      },
      {
        id: 'LastNameColumn',
        objectType: 'Column',
        text: '${symbol_dollar}{textKey:LastName}',
        width: 300
      },
      {
        id: 'SalaryColumn',
        objectType: 'NumberColumn',
        text: '${symbol_dollar}{textKey:Salary}',
        width: 200
      },
      {
        id: 'ExternColumn',
        objectType: 'BooleanColumn',
        text: '${symbol_dollar}{textKey:External}',
        width: 150
      }
    ],
    menus: [
      {
        id: 'EditPersonMenu',
        objectType: 'Menu',
        text: '${symbol_dollar}{textKey:EditPerson}',
        iconId: icons.PENCIL,
        menuTypes: [
          'Table.SingleSelection'
        ]
      },
      {
        id: 'CreatePersonMenu',
        objectType: 'Menu',
        text: '${symbol_dollar}{textKey:CreatePerson}',
        iconId: icons.PLUS,
        menuTypes: [
          'Table.EmptySpace'
        ]
      },
      {
        id: 'DeletePersonMenu',
        objectType: 'Menu',
        text: '${symbol_dollar}{textKey:DeletePerson}',
        iconId: icons.REMOVE,
        menuTypes: [
          'Table.SingleSelection'
        ]
      }
    ],
    tableControls: [
      {
        id: 'SearchFormTableControl',
        objectType: 'FormTableControl',
        iconId: icons.SEARCH,
        form: {
          id: 'SearchForm',
          objectType: '${simpleArtifactName}.PersonSearchForm'
        }
      },
      {
        id: 'AggregateTableControl',
        objectType: 'AggregateTableControl'
      }
    ]
  }
});
