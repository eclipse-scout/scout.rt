/*
 * Copyright (c) 2010-2020 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
import {TableSpecHelper} from '@eclipse-scout/testing';
import Cell from '../../../src/cell/Cell';

describe('SmartColumn', () => {
  let session, helper;

  beforeEach(() => {
    setFixtures(sandbox());
    session = sandboxSession();
    helper = new TableSpecHelper(session);
    jasmine.clock().install();
  });

  afterEach(() => {
    jasmine.clock().uninstall();
  });

  // SmartColumn must deal with values of type number or string
  it('isContentValid', () => {
    let table = helper.createTable({
      columns: [{
        objectType: 'SmartColumn',
        mandatory: true
      }]
    });
    table.insertRow({
      cells: [null]
    });
    let column = table.columns[0];
    let row = table.rows[0];
    expect(column.isContentValid(row).valid).toBe(false);
    column.setCellValue(row, '');
    expect(column.isContentValid(row).valid).toBe(false);
    column.setCellValue(row, 0);
    expect(column.isContentValid(row).valid).toBe(true);
    column.setCellValue(row, 1);
    expect(column.isContentValid(row).valid).toBe(true);
    column.setCellValue(row, 'foo');
    expect(column.isContentValid(row).valid).toBe(true);
  });

  /**
   * Makes sure no lookup call is executed (this would throw an error, because no lookup call
   * is configured for the column / smart-field.
   */
  it('must NOT execute a lookup by key when the editor is initialized', () => {
    let table = helper.createTable({
      columns: [{
        objectType: 'SmartColumn',
        mandatory: true
      }]
    });
    let cell = new Cell();
    cell.setText('Foo');
    cell.setValue(7);
    cell.setEditable(true);
    table.insertRow({
      cells: [cell]
    });
    let column = table.columns[0];
    let row = table.rows[0];
    let field = null;
    table.on('startCellEdit', event => {
      field = event.field;
    });
    table.render();
    table.focusCell(column, row);
    jasmine.clock().tick();
    expect(field.displayText).toEqual('Foo');
    expect(field.value).toEqual(7);
  });

});
