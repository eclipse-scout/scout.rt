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
import {TableSpecHelper} from '../../../src/testing/index';
import {arrays, Cell, LookupCall, objects, scout, SmartColumn} from '../../../src';

describe('SmartColumn', () => {
  let session: SandboxSession, helper: TableSpecHelper;

  beforeEach(() => {
    setFixtures(sandbox());
    session = sandboxSession();
    helper = new TableSpecHelper(session);
    jasmine.clock().install();
  });

  afterEach(() => {
    jasmine.clock().uninstall();
  });

  it('rows with object key can be resolved', () => {
    const table = helper.createTable({
      columns: [{
        objectType: SmartColumn
      }]
    });

    const lookupCall = scout.create(LookupCall, {session: session, batch: true});
    (table.columns[0] as SmartColumn<any>).setLookupCall(lookupCall);

    const key1 = {a: 1, b: 1};
    const key2 = {a: 1, b: 2};
    const key3 = {a: 2, b: 2};
    const valueMap = {};
    // ensureValidKey will stringify an object key
    valueMap[objects.ensureValidKey(key1)] = 'Value 1';
    valueMap[objects.ensureValidKey(key2)] = 'Value 2';
    valueMap[objects.ensureValidKey(key3)] = 'Value 3';
    spyOn(lookupCall, 'textsByKeys').and.returnValue($.resolvedPromise(valueMap));
    spyOn(lookupCall, 'textByKey').and.callFake(key => {
      return $.resolvedPromise(valueMap[objects.ensureValidKey(key)]);
    });

    // insert 6 rows
    table.insertRows(Object.keys(valueMap).concat(Object.keys(valueMap)).map(getRow));
    table.render();
    jasmine.clock().tick(500);

    // text should get resolved with a single batch lookup call
    expect(lookupCall.textsByKeys).toHaveBeenCalledTimes(1);

    // textsByKeys should be called with unique keys
    expect(lookupCall.textsByKeys).toHaveBeenCalledWith(arrayEqualsIgnoreOrder(Object.keys(valueMap)));

    table.insertRow(getRow(objects.ensureValidKey(key1)));
    jasmine.clock().tick(500);
    expect(lookupCall.textsByKeys).toHaveBeenCalledTimes(2);

    table.insertRow(getRow(objects.ensureValidKey(key2)));
    jasmine.clock().tick(500);
    expect(lookupCall.textsByKeys).toHaveBeenCalledTimes(3);

    // disable batch mode, now textByKey should be called instead
    lookupCall.setBatch(false);

    table.insertRows(Object.keys(valueMap).map(getRow));
    jasmine.clock().tick(500);

    expect(lookupCall.textsByKeys).toHaveBeenCalledTimes(3);
    expect(lookupCall.textByKey).toHaveBeenCalledTimes(3);

    // rows have texts returned by lookup call
    checkTableRowTexts(table, valueMap);
  });

  // SmartColumn must deal with values of type number or string
  it('isContentValid', () => {
    let table = helper.createTable({
      columns: [{
        objectType: SmartColumn,
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
   * Makes sure no lookup call is executed (this would throw an error, because no lookup call is configured for the column / smart-field).
   */
  it('must NOT execute a lookup by key when the editor is initialized', () => {
    let table = helper.createTable({
      columns: [{
        objectType: SmartColumn,
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
    jasmine.clock().tick(0);
    expect(field.displayText).toEqual('Foo');
    expect(field.value).toEqual(7);
  });

  it('must use batch lookup calls when enabled', () => {
    const table = helper.createTable({
      columns: [{
        objectType: SmartColumn
      }]
    });

    const lookupCall = scout.create(LookupCall, {session: session, batch: true});
    (table.columns[0] as SmartColumn<any>).setLookupCall(lookupCall);

    const valueMap = {key1: 'Value 1', key2: 'Value 2', key3: 'Value 3'};
    spyOn(lookupCall, 'textsByKeys').and.returnValue($.resolvedPromise(valueMap));
    spyOn(lookupCall, 'textByKey').and.callFake(key => $.resolvedPromise(valueMap[objects.ensureValidKey(key)]));

    // insert 6 rows
    table.insertRows(Object.keys(valueMap).concat(Object.keys(valueMap)).map(getRow));
    table.render();
    jasmine.clock().tick(500);

    // text should get resolved with a single batch lookup call
    expect(lookupCall.textsByKeys).toHaveBeenCalledTimes(1);

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
    checkTableRowTexts(table, valueMap);
  });

  /**
   * Makes sure the prepareLookupCall event contains a property 'row'.
   */
  it('prepareLookupCall event contains a property row', () => {
    let table = helper.createTable({
      columns: [{
        objectType: SmartColumn,
        mandatory: true
      }]
    });
    let column = table.columns[0] as SmartColumn<any>;

    let lookupCall = scout.create(LookupCall, {session: session, batch: true});
    column.setLookupCall(lookupCall);

    let cell0 = new Cell();
    let value0 = 7;
    cell0.setText('Foo');
    cell0.setValue(value0);
    cell0.setEditable(true);
    let cell1 = new Cell();
    let value1 = 9;
    cell1.setText('Bar');
    cell1.setValue(value1);
    cell1.setEditable(true);

    table.insertRows([
      {cells: [cell0]},
      {cells: [cell1]}
    ]);

    let field = column.createEditor(table.rows[1]);

    column.on('prepareLookupCall', event => {
      event.row.cells[0].value += 1;
    });
    field.trigger('prepareLookupCall', {
      lookupCall: lookupCall
    });

    jasmine.clock().tick(500);
    expect(table.rows[0].cells[0].value).toEqual(value0);
    expect(table.rows[1].cells[0].value).toEqual(value1 + 1);
  });

  /**
   * The LookupCall must trigger a prepareLookupCall event.
   */
  it('must trigger a prepareLookupCall event.', () => {
    let table = helper.createTable({
      columns: [{
        objectType: SmartColumn
      }]
    });

    let lookupCall = scout.create(LookupCall, {session: session, batch: true});
    let column1 = table.columns[0] as SmartColumn<any>;
    column1.setLookupCall(lookupCall);

    let counter = 0;
    let rowAvailable;
    column1.on('prepareLookupCall', event => {
      counter++;
      expect(event.type).toBe('prepareLookupCall');
      expect(event.source).toBe(column1);
      rowAvailable = event.row;
    });

    let valueMap = {key1: 'Value 1', key2: 'Value 2', key3: 'Value 3'};
    spyOn(lookupCall, 'textsByKeys').and.returnValue($.resolvedPromise(valueMap));
    spyOn(lookupCall, 'textByKey').and.callFake(key => $.resolvedPromise(valueMap[objects.ensureValidKey(key)]));

    let getRow = key => ({cells: [key]});

    lookupCall.batch = false;
    table.insertRows(Object.keys(valueMap).map(getRow));
    jasmine.clock().tick(500);
    expect(counter).toBe(3); // Key lookups
    expect(rowAvailable).not.toBe(undefined);

    counter = 0;
    lookupCall.batch = true;
    table.deleteAllRows();
    table.insertRows(Object.keys(valueMap).map(getRow));
    table.render();
    jasmine.clock().tick(500);
    expect(counter).toBe(1); // Only one prepareLookup event should be triggered when doing batch lookups
    expect(rowAvailable).toBe(undefined);
  });

  const checkTableRowTexts = (table, valueMap) => table.rows.forEach(row => expect(row.cells[0].text).toEqual(valueMap[row.cells[0].value]));

  const getRow = key => ({cells: [key]});

  const arrayEqualsIgnoreOrder = arr => {
    // noinspection HtmlUnknownAttribute
    return {
      asymmetricMatch: compareTo => arrays.equalsIgnoreOrder(arr, compareTo),
      jasmineToString: () => '<arrayWithEqualElements: [' + arr.toString() + ']>'
    };
  };

});
