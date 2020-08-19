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
import arrays from '../../../src/util/arrays';

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

  it('must use batch lookup calls when enabled', () => {
    const table = helper.createTable({
      columns: [{
        objectType: 'SmartColumn'
      }]
    });

    const lookupCall = scout.create('LookupCall', {session: session, batch: true});
    table.columns[0].setLookupCall(lookupCall);

    const valueMap = {key1: 'Value 1', key2: 'Value 2', key3: 'Value 3'};
    spyOn(lookupCall, 'textsByKeys').and.returnValue($.resolvedPromise(valueMap));
    spyOn(lookupCall, 'textByKey').and.callFake(key => $.resolvedPromise(valueMap[key]));

    const getRow = key => ({cells: [key]});

    // insert 6 rows
    table.insertRows(Object.keys(valueMap).concat(Object.keys(valueMap)).map(getRow));
    table.render();
    jasmine.clock().tick(500);

    // text should get resolved with a single batch lookup call
    expect(lookupCall.textsByKeys).toHaveBeenCalledTimes(1);

    const arrayEqualsIgnoreOrder = arr => {
      return {
        asymmetricMatch: compareTo => arrays.equalsIgnoreOrder(arr, compareTo),
        jasmineToString: () => '<arrayWithEqualElements: [' + arr.toString() + ']>'
      };
    };
    // textsByKeys should be called with unique keys
    expect(lookupCall.textsByKeys).toHaveBeenCalledWith(arrayEqualsIgnoreOrder(Object.keys(valueMap)));

    table.insertRow(getRow('key1'));
    jasmine.clock().tick(500);
    expect(lookupCall.textsByKeys).toHaveBeenCalledTimes(2);

    table.insertRow(getRow('key2'));
    jasmine.clock().tick(500);
    expect(lookupCall.textsByKeys).toHaveBeenCalledTimes(3);

    // textByKey should never be called in batch mode
    expect(lookupCall.textByKey).not.toHaveBeenCalled();

    // disable batch mode, now textByKey should be called instead
    lookupCall.setBatch(false);

    table.insertRows(Object.keys(valueMap).map(getRow));
    jasmine.clock().tick(500);

    expect(lookupCall.textsByKeys).toHaveBeenCalledTimes(3);
    expect(lookupCall.textByKey).toHaveBeenCalledTimes(3);

    // rows have texts returned by lookup call
    table.rows.forEach(row => expect(row.cells[0].text).toEqual(valueMap[row.cells[0].value]));
  });

});
