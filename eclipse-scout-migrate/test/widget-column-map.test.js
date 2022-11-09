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
import {mockPluginParams} from './test-utils.js';
import {crlfToLf, lfToCrlf} from '../src/common.js';
import widgetColumnMapPlugin from '../src/widgetColumnMapPlugin.js';

describe('widget column map plugin', () => {
  let simpleModel = `export default (): FormModel => ({
  id: 'test.FancyForm',
  rootGroupBox: {
    id: 'MainBox',
    objectType: GroupBox,
    fields: [
      {
        id: 'DetailBox',
        objectType: GroupBox,
        menus: [
          {
            id: 'FancyMenu',
            objectType: Menu
          }
        ],
        fields: [
          {
            id: 'FancyStringField',
            objectType: StringField
          },
          {
            id: 'FancyNumberField',
            objectType: NumberField
          }
        ]
      }
    ]
  }
});`,
    simpleImports = 'import {FormModel, GroupBox, Menu, NumberField, StringField} from \'@eclipse-scout/core\';',
    simpleWidgetMap = `export type FancyFormWidgetMap = {
  'MainBox': GroupBox;
  'DetailBox': GroupBox;
  'FancyMenu': Menu;
  'FancyStringField': StringField;
  'FancyNumberField': NumberField;
};`,
    simpleWidgetMapMissingProperties = `export type FancyFormWidgetMap = {
  'DetailBox': GroupBox;
  'FancyStringField': StringField;
  'FancyNumberField': NumberField;
};`,
    simpleWidgetMapToManyProperties = `export type FancyFormWidgetMap = {
  'MainBox': GroupBox;
  'DetailBox': GroupBox;
  'FancyMenu': Menu;
  'FancyStringField': StringField;
  'FancyStringField2': StringField;
  'FancyNumberField': NumberField;
  'FancyNumberField2': NumberField;
  'BottomBox': GroupBox;
};`,
    withTableModel = `export default (): FormModel => ({
  id: 'test.FancyForm',
  rootGroupBox: {
    id: 'MainBox',
    objectType: GroupBox,
    fields: [
      {
        id: 'DetailBox',
        objectType: GroupBox,
        menus: [
          {
            id: 'FancyMenu',
            objectType: Menu
          }
        ],
        fields: [
          {
            id: 'FancyStringField',
            objectType: StringField
          },
          {
            id: 'FancyNumberField',
            objectType: NumberField
          },
          {
            id: 'FancyTableField',
            objectType: TableField,
            table: {
              id: 'FancyTable',
              objectType: Table,
              columns: [
                {
                  id: 'FancyNumbers',
                  objectType: NumberColumn
                },
                {
                  id: 'FancyStrings',
                  objectType: Column
                },
                {
                  id: 'FancyDates',
                  objectType: DateColumn
                }
              ]
            }
          },
          {
            id: 'LameTableField',
            objectType: TableField,
            table: {
              id: 'Table',
              objectType: Table,
              columns: [
                {
                  id: 'LameNumbers',
                  objectType: NumberColumn
                }
              ]
            }
          }
        ]
      }
    ]
  }
});`,
    withTableImports = 'import {Column, DateColumn, FormModel, GroupBox, Menu, NumberColumn, NumberField, StringField, Table, TableField} from \'@eclipse-scout/core\';',
    withTableWidgetMap = `export type FancyFormWidgetMap = {
  'MainBox': GroupBox;
  'DetailBox': GroupBox;
  'FancyMenu': Menu;
  'FancyStringField': StringField;
  'FancyNumberField': NumberField;
  'FancyTableField': TableField;
  'FancyTable': FancyTable;
  'LameTableField': TableField;
  'Table': LameTableFieldTable;
};`,
    fancyTableColumnMap = `export type FancyTableColumnMap = {
  'FancyNumbers': NumberColumn;
  'FancyStrings': Column;
  'FancyDates': DateColumn;
};`,
    fancyTableColumnMapMissingProperties = `export type FancyTableColumnMap = {
  'FancyNumbers': NumberColumn;
};`,
    fancyTableColumnMapToManyProperties = `export type FancyTableColumnMap = {
  'FancyNumbers': NumberColumn;
  'FancyStrings': Column;
  'FancyStrings2': Column;
  'FancyDates': DateColumn;
  'FancyDates2': DateColumn;
};`,
    fancyTable = `export class FancyTable extends Table {
  declare columnMap: FancyTableColumnMap;
}`,
    lameTableFieldTableColumnMap = `export type LameTableFieldTableColumnMap = {
  'LameNumbers': NumberColumn;
};`,
    lameTableFieldTable = `export class LameTableFieldTable extends Table {
  declare columnMap: LameTableFieldTableColumnMap;
}`;

  async function runPluginAndExpectSimple(text) {
    text = lfToCrlf(text);
    let result = await widgetColumnMapPlugin.run(
      mockPluginParams({text, fileName: 'FancyFormModel.ts', options: {}})
    );
    result = crlfToLf(result);
    expect(result).toBe(`${simpleImports}

${simpleModel}

${simpleWidgetMap}
`);
  }

  async function runPluginAndExpectWithTable(text) {
    text = lfToCrlf(text);
    let result = await widgetColumnMapPlugin.run(
      mockPluginParams({text, fileName: 'FancyFormModel.ts', options: {}})
    );
    result = crlfToLf(result);
    expect(result).toBe(`${withTableImports}

${withTableModel}

${withTableWidgetMap}

${fancyTableColumnMap}

${fancyTable}

${lameTableFieldTableColumnMap}

${lameTableFieldTable}
`);
  }

  it('creates widget map with properties from model', async () => {
    await runPluginAndExpectSimple(`${simpleImports}

${simpleModel}
`);
  });

  it('adds missing widget map properties from model', async () => {
    await runPluginAndExpectSimple(`${simpleImports}

${simpleModel}

${simpleWidgetMapMissingProperties}
`);
  });

  it('removes unnecessary widget map properties that are no longer in the model', async () => {
    await runPluginAndExpectSimple(`${simpleImports}

${simpleModel}

${simpleWidgetMapToManyProperties}
`);
  });

  it('creates column map and table class with properties from model', async () => {
    await runPluginAndExpectWithTable(`${withTableImports}

${withTableModel}
`);
  });

  it('adds missing column map properties from model', async () => {
    await runPluginAndExpectWithTable(`${withTableImports}

${withTableModel}

${withTableWidgetMap}

${fancyTableColumnMapMissingProperties}

${fancyTable}
`);
  });

  it('removes unnecessary column map properties that are no longer in the model', async () => {
    await runPluginAndExpectWithTable(`${withTableImports}

${withTableModel}

${withTableWidgetMap}

${fancyTableColumnMapToManyProperties}

${fancyTable}
`);
  });
});
