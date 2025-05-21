/*
 * Copyright (c) 2010, 2025 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {
  BaseDoEntity, BooleanColumn, Column, Desktop, DesktopModel, Form, FormModel, GroupBox, NumberColumn, ObjectOrModel, Outline, OutlineViewButton, Page, PageParamDo, PageWithNodes, PageWithTable, ResetMenu, scout, SearchFormTableControl,
  SearchMenu, StringField, strings, Table, TableRow, typeName
} from '../../src';

// ---------------------------------------------------------------
//
//   SpecOutline1
//   +- SpecNodePage1 [leaf, SpecDetailForm]
//   +- SpecTablePage1 [leaf] (Letters)
//   +- SpecTablePage3 [leaf, SpecSearchForm] (Colors)
//
//   SpecOutline2
//   +- SpecNodePage2 [leaf, SpecDetailForm]
//   +- SpecNodePage3
//      +- SpecNodePage1 [leaf, SpecDetailForm]
//      +- SpecTablePage2 [SpecSearchForm] (Fruits)
//      |  +- SpecNodePage4
//      |     +- SpecNodePage2 [leaf, SpecDetailForm]
//      |     +- (rec:SpecTablePage2)
//      +- SpecNodePage2 [leaf, SpecDetailForm]
//
// ---------------------------------------------------------------

export function getOutline(desktop: Desktop, outlineId: string): Outline {
  return scout.assertValue(desktop.getOutlines().find(outline => outline.id === outlineId), `Outline not found: ${outlineId}`);
}

export function goToOutline(desktop: Desktop, outlineId: string): Outline {
  let outline = getOutline(desktop, outlineId);
  desktop.setOutline(outline);
  desktop.bringOutlineToFront();
  return outline;
}

export function specDesktopModel(): DesktopModel {
  return {
    navigationVisible: true,
    headerVisible: true,
    benchVisible: true,
    viewButtons: [
      {
        id: 'SpecOutline1ViewButton',
        objectType: OutlineViewButton,
        outline: {
          id: SPEC_OUTLINE_1_ID,
          uuid: SPEC_OUTLINE_1_UUID,
          objectType: Outline,
          title: 'Outline 1',
          nodes: [
            {objectType: SpecNodePage1},
            {objectType: SpecTablePage1},
            {objectType: SpecTablePage3}
          ]
        },
        selected: true,
        displayStyle: 'MENU',
        text: 'Outline Button 1'
      },
      {
        id: 'SpecOutline2ViewButton',
        objectType: OutlineViewButton,
        outline: {
          id: SPEC_OUTLINE_2_ID,
          uuid: SPEC_OUTLINE_2_UUID,
          objectType: Outline,
          title: 'Outline 2',
          nodes: [
            {objectType: SpecNodePage2},
            {objectType: SpecNodePage3}
          ]
        },
        displayStyle: 'MENU',
        text: 'Outline Button 2'
      }
    ],
    outline: SPEC_OUTLINE_1_ID
  };
}

// ---------------------------------------------------------------

export const SPEC_OUTLINE_1_ID = 'SpecOutline1';
export const SPEC_OUTLINE_2_ID = 'SpecOutline2';
export const SPEC_OUTLINE_1_UUID = '8841b967-4801-47bb-87a9-a5f6d54b4014';
export const SPEC_OUTLINE_2_UUID = 'a6379a66-c844-4ec7-8e7e-1f854dc7e81e';
export const SPEC_NODE_PAGE_1_UUID = '9e4a69e7-73a5-44fd-8d68-ebb6a50f07ba';
export const SPEC_NODE_PAGE_2_UUID = 'c7f9ad97-d80a-429b-8701-0378cad9307f';
export const SPEC_NODE_PAGE_3_UUID = '80e022bf-5b00-491d-818e-3c4054d7fcc3';
export const SPEC_NODE_PAGE_4_UUID = 'df79375c-047b-47cf-8323-360652ee97ae';
export const SPEC_TABLE_PAGE_1_UUID = 'e9320869-aead-46a5-a67e-25491f8823de';
export const SPEC_TABLE_PAGE_2_UUID = '56c699e5-5692-4a21-9595-e7dac5ee568e';
export const SPEC_TABLE_PAGE_3_UUID = 'daf22921-71eb-4382-b500-854225e71622';
export const FRUIT_1_KEY = '1'; // Apple
export const FRUIT_2_KEY = '2'; // Banana
export const FRUIT_3_KEY = '3'; // Pineapple
export const FRUIT_4_KEY = '4'; // Lemon
export const FRUIT_5_KEY = '5'; // Kiwi

@typeName('SpecPageParam')
export class SpecPageParamDo extends PageParamDo {
  fooId: string;
}

@typeName('SpecSearch')
export class SpecSearchDo extends BaseDoEntity {
  text: string;
}

export class SpecNodePage1 extends PageWithNodes {

  constructor() {
    super();
    this.uuid = SPEC_NODE_PAGE_1_UUID;
    this.text = 'Node Page 1';
    this.leaf = true;
  }

  protected override _createDetailForm(): Form {
    return scout.create(SpecDetailForm, {
      parent: this.outline
    });
  }
}

export class SpecNodePage2 extends PageWithNodes {

  constructor() {
    super();
    this.uuid = SPEC_NODE_PAGE_2_UUID;
    this.text = 'Node Page 3';
    this.leaf = true;
  }

  protected override _createDetailForm(): Form {
    return scout.create(SpecDetailForm, {
      parent: this.outline
    });
  }
}

export class SpecNodePage3 extends PageWithNodes {

  constructor() {
    super();
    this.uuid = SPEC_NODE_PAGE_3_UUID;
    this.text = 'Node Page 3';
  }

  protected override _createChildPages(): JQuery.Promise<Page[]> {
    return $.resolvedPromise([
      scout.create(SpecNodePage1, {parent: this.outline}),
      scout.create(SpecTablePage2, {parent: this.outline}),
      scout.create(SpecNodePage2, {parent: this.outline})
    ]);
  }
}

export class SpecNodePage4 extends PageWithNodes {

  constructor() {
    super();
    this.uuid = SPEC_NODE_PAGE_4_UUID;
    this.text = 'Node Page 4';
  }

  protected override _createChildPages(): JQuery.Promise<Page[]> {
    return $.resolvedPromise([
      scout.create(SpecNodePage2, {parent: this.outline}),
      scout.create(SpecTablePage2, {parent: this.outline})
    ]);
  }
}

export class SpecTablePage1 extends PageWithTable {

  constructor() {
    super();
    this.uuid = SPEC_TABLE_PAGE_1_UUID;
    this.text = 'Table Page 1';
    this.leaf = true;
  }

  protected override _createDetailTable(): Table {
    return scout.create(Table, {
      parent: this.outline,
      columns: [{
        id: 'LetterColumn',
        objectType: Column,
        text: 'Letter'
      }]
    });
  }

  protected override _loadTableData(searchFilter: any): JQuery.Promise<any> {
    let data = ['A', 'B', 'C'];
    return $.resolvedPromise(data);
  }

  protected override _transformTableDataToTableRows(tableData: any): ObjectOrModel<TableRow>[] {
    return tableData.map(rowData => {
      return scout.create(TableRow, {
        parent: this.detailTable,
        cells: [rowData]
      });
    });
  }
}

export class SpecTablePage2 extends PageWithTable {

  constructor() {
    super();
    this.uuid = SPEC_TABLE_PAGE_2_UUID;
    this.text = 'Table Page 2';
  }

  protected override _createDetailTable(): Table {
    return scout.create(Table, {
      parent: this.outline,
      columns: [{
        id: 'KeyColumn',
        objectType: Column,
        primaryKey: true,
        displayable: false
      }, {
        id: 'NameColumn',
        objectType: Column,
        text: 'Name',
        summary: true
      }, {
        id: 'AmountColumn',
        objectType: NumberColumn,
        text: 'Amount'
      }],
      tableControls: [{
        id: 'SearchFormTableControl',
        objectType: SearchFormTableControl,
        form: {
          id: 'SearchForm',
          objectType: SpecSearchForm
        }
      }]
    });
  }

  protected override _initDetailTable(table: Table) {
    super._initDetailTable(table);
    this.getSearchForm().on('search reset', event => table.reload(Table.ReloadReason.SEARCH));
  }

  protected override _loadTableData(searchFilter: any): JQuery.Promise<any> {
    let data = [
      {key: FRUIT_1_KEY, name: 'Apple', amount: 42},
      {key: FRUIT_2_KEY, name: 'Banana', amount: 37},
      {key: FRUIT_3_KEY, name: 'Pineapple', amount: 29},
      {key: FRUIT_4_KEY, name: 'Lemon', amount: 58},
      {key: FRUIT_5_KEY, name: 'Kiwi', amount: 33}
    ];
    if (searchFilter instanceof SpecSearchDo && searchFilter.text) {
      data = data.filter(d => new RegExp(strings.quote(searchFilter.text), 'i').test(d.name));
    }
    return $.resolvedPromise(data);
  }

  protected override _transformTableDataToTableRows(tableData: any): ObjectOrModel<TableRow>[] {
    return tableData.map(rowData => {
      return scout.create(TableRow, {
        parent: this.detailTable,
        cells: [rowData.key, rowData.name, rowData.amount]
      });
    });
  }

  protected override _createChildPage(row: TableRow): Page {
    let pageParam = scout.create(SpecPageParamDo, {
      fooId: this.detailTable.columnById('KeyColumn').cellValue(row)
    });
    return scout.create(SpecNodePage4, {
      parent: this.outline,
      pageParam: pageParam
    });
  }
}

export class SpecTablePage3 extends PageWithTable {

  constructor() {
    super();
    this.uuid = SPEC_TABLE_PAGE_3_UUID;
    this.text = 'Table Page 3';
    this.leaf = true;
  }

  protected override _createDetailTable(): Table {
    return scout.create(Table, {
      parent: this.outline,
      columns: [{
        id: 'KeyColumn',
        objectType: Column,
        primaryKey: true,
        displayable: false
      }, {
        id: 'ColorColumn',
        objectType: Column,
        text: 'Color',
        summary: true
      }, {
        id: 'PrimaryColumn',
        objectType: BooleanColumn,
        text: 'Primary color'
      }],
      tableControls: [{
        id: 'SearchFormTableControl',
        objectType: SearchFormTableControl,
        form: {
          id: 'SearchForm',
          objectType: SpecSearchForm
        }
      }]
    });
  }

  protected override _initDetailTable(table: Table) {
    super._initDetailTable(table);
    this.getSearchForm().on('search reset', event => table.reload(Table.ReloadReason.SEARCH));
  }

  protected override _loadTableData(searchFilter: any): JQuery.Promise<any> {
    let data = [
      {key: '#000000', label: 'Black', primary: false},
      {key: '#ff0000', label: 'Red', primary: true},
      {key: '#00ff00', label: 'Green', primary: true},
      {key: '#0000ff', label: 'Blue', primary: true},
      {key: '#ffff00', label: 'Yellow', primary: false},
      {key: '#ff00ff', label: 'Magenta', primary: false},
      {key: '#00ffff', label: 'Cyan', primary: false},
      {key: '#ffffff', label: 'White', primary: false}
    ];
    if (searchFilter instanceof SpecSearchDo && searchFilter.text) {
      data = data.filter(d => new RegExp(strings.quote(searchFilter.text), 'i').test(d.label));
    }
    return $.resolvedPromise(data);
  }

  protected override _transformTableDataToTableRows(tableData: any): ObjectOrModel<TableRow>[] {
    return tableData.map(rowData => {
      return scout.create(TableRow, {
        parent: this.detailTable,
        cells: [rowData.key, rowData.label, rowData.primary]
      });
    });
  }
}

export class SpecSearchForm extends Form {
  declare data: SpecSearchDo;
  declare widgetMap: {
    'TextField': StringField;
    'SearchMenu': SearchMenu;
    'ResetMenu': ResetMenu;
  };

  protected override _jsonModel(): FormModel {
    return {
      rootGroupBox: {
        id: 'MainBox',
        objectType: GroupBox,
        fields: [{
          id: 'TextField',
          objectType: StringField,
          label: 'Text'
        }],
        menus: [{
          id: 'SearchMenu',
          objectType: SearchMenu
        }, {
          id: 'ResetMenu',
          objectType: ResetMenu
        }]
      }
    };
  }

  override importData() {
    if (!this.data) {
      return;
    }
    this.widget('TextField').setValue(this.data.text);
  }

  override exportData(): any {
    return scout.create(SpecSearchDo, {
      text: this.widget('TextField').value
    });
  }
}

export class SpecDetailForm extends Form {
}
