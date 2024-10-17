/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {Cell, CellEditorPopup, Column, keys, scout, SmartColumn, StaticLookupCall, Status, StringField, Table, TableRow, Widget} from '../../../src/index';
import {FormSpecHelper, JQueryTesting, TableSpecHelper} from '../../../src/testing/index';

describe('CellEditor', () => {
  let session: SandboxSession;
  let helper: TableSpecHelper;
  let formHelper: FormSpecHelper;

  beforeEach(() => {
    setFixtures(sandboxDesktop());
    session = sandboxSession();
    helper = new TableSpecHelper(session);
    formHelper = new FormSpecHelper(session);
    jasmine.Ajax.install();
    jasmine.clock().install();
  });

  afterEach(() => {
    session = null;
    jasmine.Ajax.uninstall();
    jasmine.clock().uninstall();
    let popup = findPopup();
    if (popup) {
      popup.close();
    }
  });

  class DummyLookupCall extends StaticLookupCall<string> {
    override _data() {
      return [
        ['key0', 'Key 0'],
        ['key1', 'Key 1']
      ];
    }
  }

  function createStringField(): StringField {
    return scout.create(StringField, {
      parent: session.desktop
    });
  }

  function $findPopup(): JQuery {
    return $('.cell-editor-popup');
  }

  function findPopup(): CellEditorPopup<string> {
    return $findPopup().data('popup');
  }

  function assertCellEditorIsOpen(table: Table, column: Column<any>, row: TableRow) {
    let popup = table.cellEditorPopup;
    expect(popup.cell.field.rendered).toBe(true);
    expect(popup.column).toBe(column);
    expect(popup.row).toBe(row);
    let $popup = $findPopup();
    expect($popup.length).toBe(1);
    expect(popup.$container[0]).toBe($popup[0]);
    expect($popup.find('.form-field').length).toBe(1);
  }

  describe('mouse click', () => {
    let table, model, $rows, $cells0, $cells1, $cell0_0, $cell0_1, $cell1_0;

    beforeEach(() => {
      model = helper.createModelFixture(2, 2);
      table = helper.createTable(model);
      table.render();
      helper.applyDisplayStyle(table);
      $rows = table.$rows();
      $cells0 = $rows.eq(0).find('.table-cell');
      $cells1 = $rows.eq(1).find('.table-cell');
      $cell0_0 = $cells0.eq(0);
      $cell0_1 = $cells0.eq(1);
      $cell1_0 = $cells1.eq(0);
    });

    it('starts cell edit if cell is editable', () => {
      table.rows[0].cells[0].editable = true;
      table.rows[1].cells[0].editable = false;

      spyOn(table, 'prepareCellEdit');
      JQueryTesting.triggerClick($cell1_0);
      expect(table.prepareCellEdit).not.toHaveBeenCalled();
      JQueryTesting.triggerClick($cell0_0);
      expect(table.prepareCellEdit).toHaveBeenCalled();
    });

    it('does not start cell edit if cell is not editable', () => {
      table.rows[0].cells[0].editable = false;

      spyOn(table, 'prepareCellEdit');
      JQueryTesting.triggerClick($cell0_0);
      expect(table.prepareCellEdit).not.toHaveBeenCalled();
    });

    it('does not start cell edit if row is disabled', () => {
      table.rows[0].cells[0].setEditable(true);
      table.rows[0].setEnabled(false);

      spyOn(table, 'prepareCellEdit');
      JQueryTesting.triggerClick($cell0_0);
      expect(table.prepareCellEdit).not.toHaveBeenCalled();
    });

    it('does not start cell edit if table is disabled', () => {
      table.rows[0].cells[0].setEditable(true);
      table.setEnabled(false);
      table.recomputeEnabled();

      spyOn(table, 'prepareCellEdit');
      JQueryTesting.triggerClick($cell0_0);
      expect(table.prepareCellEdit).not.toHaveBeenCalled();
    });

    it('does not start cell edit if form is disabled', () => {
      table.rows[0].cells[0].editable = true;
      table.enabledComputed = false;

      spyOn(table, 'prepareCellEdit');
      JQueryTesting.triggerClick($cell0_0);
      expect(table.prepareCellEdit).not.toHaveBeenCalled();
    });

    it('does not start cell edit if mouse down and up happened on different cells', () => {
      table.rows[0].cells[0].editable = true;
      table.rows[0].cells[1].editable = true;

      spyOn(table, 'prepareCellEdit');
      JQueryTesting.triggerMouseDown($cell0_1);
      JQueryTesting.triggerMouseUp($cell0_0);
      expect(table.prepareCellEdit).not.toHaveBeenCalled();
    });

    it('does not start cell edit if right mouse button was pressed', () => {
      table.rows[0].cells[0].editable = true;

      spyOn(table, 'prepareCellEdit');
      JQueryTesting.triggerMouseDown($cell0_0, {which: 3});
      JQueryTesting.triggerMouseUp($cell0_0, {which: 3});
      expect(table.prepareCellEdit).not.toHaveBeenCalled();
    });

    it('does not start cell edit if middle mouse button was pressed', () => {
      table.rows[0].cells[0].editable = true;

      spyOn(table, 'prepareCellEdit');
      JQueryTesting.triggerMouseDown($cell0_0, {which: 2});
      JQueryTesting.triggerMouseUp($cell0_0, {which: 2});
      expect(table.prepareCellEdit).not.toHaveBeenCalled();
    });

    it('does not open cell editor if a ctrl or shift is pressed, because the user probably wants to do row selection rather than cell editing', () => {
      table.rows[0].cells[0].editable = true;
      table.rows[1].cells[0].editable = true;

      spyOn(table, 'prepareCellEdit');
      // row 0 is selected, user presses shift and clicks row 2
      table.selectRows([table.rows[0]]);
      JQueryTesting.triggerClick($cell1_0, {modifier: 'shift'});
      expect(table.prepareCellEdit).not.toHaveBeenCalled();

      JQueryTesting.triggerClick($cell1_0, {modifier: 'ctrl'});
      expect(table.prepareCellEdit).not.toHaveBeenCalled();
    });
  });

  describe('TAB key', () => {
    let table, $rows, $cells0;

    beforeEach(() => {
      table = helper.createTable(helper.createModelFixture(3, 2));
      table.render();
      helper.applyDisplayStyle(table);
      $rows = table.$rows();
      $cells0 = $rows.eq(0).find('.table-cell');
    });

    it('starts the cell editor for the next editable cell', () => {
      table.rows[0].cells[0].editable = true;
      table.rows[1].cells[0].editable = true;

      table.focusCell(table.columns[0], table.rows[0]);
      jasmine.clock().tick(0);
      assertCellEditorIsOpen(table, table.columns[0], table.rows[0]);

      JQueryTesting.triggerKeyInputCapture($(document.activeElement as HTMLElement), keys.TAB);
      jasmine.clock().tick(0);
      jasmine.clock().tick(0);
      assertCellEditorIsOpen(table, table.columns[0], table.rows[1]);
    });

    it('starts the cell editor for the next visible cell', () => {
      table.rows[0].cells[0].setEditable(true);
      table.rows[0].cells[1].setEditable(true);
      table.rows[1].cells[2].setEditable(true);
      table.columns[0].setVisible(false);
      table.columns[1].setVisible(false);

      table.focusCell(table.columns[0], table.rows[0]);
      jasmine.clock().tick(0);
      assertCellEditorIsOpen(table, table.columns[0], table.rows[0]);

      JQueryTesting.triggerKeyInputCapture($(document.activeElement as HTMLElement), keys.TAB);
      jasmine.clock().tick(0);
      jasmine.clock().tick(0);
      assertCellEditorIsOpen(table, table.columns[2], table.rows[1]);
    });
  });

  describe('prepareCellEdit', () => {
    let table;

    beforeEach(() => {
      let model = helper.createModelFixture(2, 2);
      table = helper.createTable(model);
      table.render();
      helper.applyDisplayStyle(table);
    });

    it('creates field and calls start', () => {
      table.columns[0].setEditable(true);
      spyOn(table, 'startCellEdit').and.callThrough();

      table.prepareCellEdit(table.columns[0], table.rows[0]);
      jasmine.clock().tick(0);
      expect(table.startCellEdit).toHaveBeenCalled();
      assertCellEditorIsOpen(table, table.columns[0], table.rows[0]);
    });

    it('copies the value to the field if cell was valid', () => {
      let column = table.columns[0];
      let row = table.rows[0];
      column.setEditable(true);
      column.setCellValue(row, 'valid value');
      table.prepareCellEdit(column, row);
      jasmine.clock().tick(0);
      assertCellEditorIsOpen(table, column, row);
      let field = table.cellEditorPopup.cell.field;
      expect(field.value).toEqual('valid value');
      expect(field.displayText).toEqual('valid value');
      expect(field.errorStatus).toEqual(null);
    });

    it('copies the text and the error to the field if cell was invalid', () => {
      let column = table.columns[0];
      let row = table.rows[0];
      column.setEditable(true);
      column.setCellValue(row, 'valid value');
      column.setCellText(row, 'invalid value');
      column.setCellErrorStatus(row, Status.error('error'));
      table.prepareCellEdit(column, row);
      jasmine.clock().tick(0);
      assertCellEditorIsOpen(table, column, row);
      let field = table.cellEditorPopup.cell.field;
      expect(field.value).toEqual(null);
      expect(field.displayText).toEqual('invalid value');
      expect(field.errorStatus.message).toEqual('error');
    });

    it('triggers prepareCellEdit event', () => {
      let triggeredEvent;
      table.columns[0].setEditable(true);
      table.on('prepareCellEdit', event => {
        triggeredEvent = event;
      });
      table.prepareCellEdit(table.columns[0], table.rows[0]);
      jasmine.clock().tick(0);
      expect(triggeredEvent.column).toBe(table.columns[0]);
      expect(triggeredEvent.row).toBe(table.rows[0]);
    });
  });

  describe('startCellEdit', () => {
    let table: Table;

    beforeEach(() => {
      let model = helper.createModelFixture(2, 2);
      table = helper.createTable(model);
      table.render();
      helper.applyDisplayStyle(table);
    });

    it('opens popup with field', () => {
      table.columns[0].setEditable(true);
      let field = createStringField();
      table.startCellEdit(table.columns[0], table.rows[0], field);
      assertCellEditorIsOpen(table, table.columns[0], table.rows[0]);
      expect(table.cellEditorPopup.cell.field).toBe(field);
    });

    it('triggers startCellEdit event', () => {
      let triggeredEvent;
      table.columns[0].setEditable(true);
      table.on('startCellEdit', event => {
        triggeredEvent = event;
      });
      table.prepareCellEdit(table.columns[0], table.rows[0]);
      jasmine.clock().tick(0);
      expect(triggeredEvent.row).toBe(table.rows[0]);
      expect(triggeredEvent.column).toBe(table.columns[0]);
      expect(triggeredEvent.field instanceof Widget).toBe(true);
    });

    it('postpones opening if table is not rendered yet', () => {
      table.remove();
      table.columns[0].setEditable(true);
      let field = createStringField();
      table.startCellEdit(table.columns[0], table.rows[0], field);
      expect(table.cellEditorPopup).toBe(null);

      table.render();
      assertCellEditorIsOpen(table, table.columns[0], table.rows[0]);
      expect(table.cellEditorPopup.cell.field).toBe(field);
    });

    it('postpones opening if table is not attached yet', () => {
      table.detach();
      table.columns[0].setEditable(true);
      let field = createStringField();
      table.startCellEdit(table.columns[0], table.rows[0], field);
      expect(table.cellEditorPopup).toBe(null);

      table.attach();
      assertCellEditorIsOpen(table, table.columns[0], table.rows[0]);
      expect(table.cellEditorPopup.cell.field).toBe(field);
    });

    it('does nothing if cell is not rendered', () => {
      table.columns[0].setEditable(true);
      table.remove(); // Remove, so that filtering won't be animated and cells won't be rendered when editing starts
      table.addFilter(() => false); // Don't accept any row
      table.render();
      helper.applyDisplayStyle(table);
      let cancelEvent;
      table.on('cancelCellEdit', event => {
        cancelEvent = event;
      });

      let field = createStringField();
      let popup = table.startCellEdit(table.columns[0], table.rows[0], field);
      expect(popup.cell.field).toBe(field);
      // Popup is not rendered because cell is not rendered
      expect($findPopup().length).toBe(0);
      // Expect cancel event so field will be disposed correctly
      expect(cancelEvent.column).toBe(table.columns[0]);
      expect(cancelEvent.row).toBe(table.rows[0]);
      expect(cancelEvent.field).toBe(table.rows[0].cells[0].field);
      expect(cancelEvent.field.destroyed).toBe(true);
    });

    it('does nothing if row is hiding', () => {
      table.columns[0].setEditable(true);
      table.addFilter(() => false); // Don't accept any row
      expect(table.rows[0].$row).toHaveClass('hiding');

      let cancelEvent;
      table.on('cancelCellEdit', event => {
        cancelEvent = event;
      });

      let field = createStringField();
      let popup = table.startCellEdit(table.columns[0], table.rows[0], field);
      expect(popup.cell.field).toBe(field);
      // Popup is not rendered because row is hiding
      // -> Popup would be positioned on the wrong row
      // -> Repositioning initiated by table layout or scrolling could cause errors because there is no anchor
      expect($findPopup().length).toBe(0);
      // Expect cancel event so field will be disposed correctly
      expect(cancelEvent.column).toBe(table.columns[0]);
      expect(cancelEvent.row).toBe(table.rows[0]);
      expect(cancelEvent.field).toBe(table.rows[0].cells[0].field);
      expect(cancelEvent.field.destroyed).toBe(true);

      popup.position(); // Must not fail if popup is not rendered
    });
  });

  describe('completeCellEdit', () => {
    let table;

    beforeEach(() => {
      let model = helper.createModelFixture(2, 2);
      table = helper.createTable(model);
      table.render();
      helper.applyDisplayStyle(table);
    });

    it('triggers completeCellEdit event', () => {
      let triggeredEvent;
      table.columns[0].setEditable(true);
      table.prepareCellEdit(table.columns[0], table.rows[0]);
      jasmine.clock().tick(0);
      table.on('completeCellEdit', event => {
        triggeredEvent = event;
      });
      table.completeCellEdit();
      expect(triggeredEvent.column).toBe(table.columns[0]);
      expect(triggeredEvent.row).toBe(table.rows[0]);
      expect(triggeredEvent.field).toBe(table.rows[0].cells[0].field);
    });

    it('calls endCellEdit with saveEditorValue=true', () => {
      table.columns[0].setEditable(true);
      table.prepareCellEdit(table.columns[0], table.rows[0]);
      jasmine.clock().tick(0);
      spyOn(table, 'endCellEdit').and.callThrough();
      let field = table.cellEditorPopup.cell.field;

      table.completeCellEdit();
      expect(table.endCellEdit).toHaveBeenCalledWith(field, true);
      jasmine.clock().tick(0);
      expect($findPopup().length).toBe(0);
    });

    it('saves editor value', () => {
      table.columns[0].setEditable(true);
      table.prepareCellEdit(table.columns[0], table.rows[0]);
      jasmine.clock().tick(0);
      table.cellEditorPopup.cell.field.setValue('my new value');

      table.completeCellEdit();
      expect(table.rows[0].cells[0].value).toBe('my new value');
    });

    it('copies the value to the cell if field was valid', () => {
      table.columns[0].setEditable(true);
      table.prepareCellEdit(table.columns[0], table.rows[0]);
      jasmine.clock().tick(0);
      table.cellEditorPopup.cell.field.setValue('my new value');

      table.completeCellEdit();
      let cell = table.rows[0].cells[0];
      expect(cell.value).toBe('my new value');
      expect(cell.text).toBe('my new value');
      expect(cell.errorStatus).toBe(null);
      expect($('.tooltip').length).toBe(0);
    });

    it('copies the text and error to the cell if field was invalid', () => {
      let column = table.columns[0];
      let row = table.rows[0];
      let cell = row.cells[0];
      expect($('.tooltip').length).toBe(0);

      column.setEditable(true);
      column.setCellValue(row, 'valid value');
      table.prepareCellEdit(column, row);
      jasmine.clock().tick(0);

      let field = table.cellEditorPopup.cell.field;
      field.setValidator(value => {
        throw 'Validation failed';
      });
      field.setValue('invalid value');
      expect(field.value).toBe('valid value');
      expect(field.errorStatus.message).toBe('Validation failed');
      expect(field.displayText).toBe('invalid value');
      table.completeCellEdit();
      expect(cell.value).toBe('valid value');
      expect(cell.text).toBe('invalid value');
      expect(cell.errorStatus.message).toBe('Validation failed');
      expect($('.tooltip').length).toBe(1);
      expect($('.tooltip')).toContainText('Validation failed');
    });

    it('clears the error if value is now valid', () => {
      let column = table.columns[0];
      let row = table.rows[0];
      let cell = row.cells[0];
      expect($('.tooltip').length).toBe(0);

      column.setEditable(true);
      column.setCellValue(row, 'valid value');
      table.prepareCellEdit(column, row);
      jasmine.clock().tick(0);

      let field = table.cellEditorPopup.cell.field;
      field.setValidator(value => {
        throw 'Validation failed';
      });
      field.setValue('invalid value');
      expect(field.value).toBe('valid value');
      expect(field.errorStatus.message).toBe('Validation failed');
      expect(field.displayText).toBe('invalid value');
      table.completeCellEdit();
      expect(cell.value).toBe('valid value');
      expect(cell.text).toBe('invalid value');
      expect(cell.errorStatus.message).toBe('Validation failed');
      expect($('.tooltip').length).toBe(1);
      expect($('.tooltip')).toContainText('Validation failed');

      // Second time -> make it valid
      table.prepareCellEdit(column, row);
      jasmine.clock().tick(0);
      field = table.cellEditorPopup.cell.field;
      field.setValidator(null);
      field.setValue('new valid value');
      expect(field.value).toBe('new valid value');
      expect(field.errorStatus).toBe(null);
      expect(field.displayText).toBe('new valid value');
      table.completeCellEdit();
      expect(cell.value).toBe('new valid value');
      expect(cell.text).toBe('new valid value');
      expect(cell.errorStatus).toBe(null);
      expect($('.tooltip').length).toBe(0);
    });

    it('clears the error if value is now valid even when changed to the original value', () => {
      let column = table.columns[0];
      let row = table.rows[0];
      let cell = row.cells[0];
      expect($('.tooltip').length).toBe(0);

      column.setEditable(true);
      column.setCellValue(row, 'valid value');
      table.prepareCellEdit(column, row);
      jasmine.clock().tick(0);

      let field = table.cellEditorPopup.cell.field;
      field.setValidator(value => {
        throw 'Validation failed';
      });
      field.setValue('invalid value');
      expect(field.value).toBe('valid value');
      expect(field.errorStatus.message).toBe('Validation failed');
      expect(field.displayText).toBe('invalid value');
      table.completeCellEdit();
      expect(cell.value).toBe('valid value');
      expect(cell.text).toBe('invalid value');
      expect(cell.errorStatus.message).toBe('Validation failed');
      expect($('.tooltip').length).toBe(1);
      expect($('.tooltip')).toContainText('Validation failed');

      // Second time -> make it valid
      table.prepareCellEdit(column, row);
      jasmine.clock().tick(0);
      field = table.cellEditorPopup.cell.field;
      field.setValidator(null);
      field.setValue('valid value'); // Same as at the beginning
      expect(field.value).toBe('valid value');
      expect(field.errorStatus).toBe(null);
      expect(field.displayText).toBe('valid value');
      table.completeCellEdit();
      expect(cell.value).toBe('valid value');
      expect(cell.text).toBe('valid value');
      expect(cell.errorStatus).toBe(null);
      expect($('.tooltip').length).toBe(0);
    });

    it('does not reopen the editor again', () => {
      table.columns[0].setEditable(true);
      table.prepareCellEdit(table.columns[0], table.rows[0]);
      jasmine.clock().tick(0);
      table.cellEditorPopup.cell.field.setValue('my new value');

      let triggeredStartCellEditEvent = null;
      table.on('startCellEdit', event => {
        triggeredStartCellEditEvent = event;
      });
      table.completeCellEdit();
      // CompleteCellEdit triggers updateRows which would reopen the editor -> this must not happen if the editor was closed
      expect(triggeredStartCellEditEvent).toBe(null);
    });
  });

  describe('completeCellEdit in SmartColumn', () => {
    let table;

    beforeEach(() => {
      let lookupCall = new DummyLookupCall();
      lookupCall.init({session: session});

      table = helper.createTable({
        columns: [{
          objectType: SmartColumn,
          lookupCall: lookupCall
        }]
      });
      let cell = new Cell();
      cell.init({value: 'key0', text: 'Key 0'});
      table.insertRow({
        cells: [cell]
      });
      table.render();
      helper.applyDisplayStyle(table);
      // Ensure texts are set and no updates are pending
      expect(table.rows[0].cells[0].text).toEqual('Key 0');
      expect(table.updateBuffer.promises.length).toBe(0);
    });

    it('does not fail when completing edit after removing a value', done => {
      jasmine.clock().uninstall();
      table.columns[0].setEditable(true);
      table.sort(table.columns[0]); // Column needs to be sorted to force a re-rendering of the rows at the end when rows are updated (_sortAfterUpdate)
      table.prepareCellEdit(table.columns[0], table.rows[0], true).then(() => {
        table.cellEditorPopup.cell.field.clear();

        let triggeredStartCellEditEvent = null;
        table.on('startCellEdit', event => {
          triggeredStartCellEditEvent = event;
        });
        // Use completeEdit to simulate a mouse click (see CellEditorPopup._onMouseDownOutside)
        // Compared to table.completeEdit it sets the flag _pendingCompleteCellEdit which delays the destruction of the popup (see _destroyCellEditorPopup)
        table.cellEditorPopup.completeEdit().then(() => {

          // CompleteCellEdit triggers setCellTextDeferred which adds the promise to the updateBuffer which eventually renders the viewport and would reopen the editor
          // -> reopening must not happen if the editor was closed
          expect(triggeredStartCellEditEvent).toBe(null);
          done();
        });
      });
    });

    it('triggers update row event containing row with correct state', () => {
      table.columns[0].setEditable(true);
      table.markRowsAsNonChanged();
      table.prepareCellEdit(table.columns[0], table.rows[0], true);
      jasmine.clock().tick(300);
      table.cellEditorPopup.cell.field.setValue('key1');
      jasmine.clock().tick(300);
      let updateRowCount = 0;
      table.on('rowsUpdated', event => {
        expect(event.rows[0].cells[0].value).toBe('key1');
        expect(event.rows[0].cells[0].text).toBe('Key 1');
        expect(event.rows[0].status).toBe(TableRow.Status.UPDATED);
        updateRowCount++;
      });
      table.completeCellEdit();
      jasmine.clock().tick(300);
      expect(updateRowCount).toBe(1);
    });

    it('updates the value even if the table has been removed in the meantime', () => {
      table.columns[0].setEditable(true);
      table.markRowsAsNonChanged();
      table.prepareCellEdit(table.columns[0], table.rows[0], true);
      jasmine.clock().tick(300);
      table.cellEditorPopup.cell.field.$field.val('Key 1');
      table.cellEditorPopup.cell.field._userWasTyping = true;
      table.cellEditorPopup.completeEdit(); // Will execute table.completeCellEdit async
      table.remove();
      jasmine.clock().tick(300);
      expect(table.rows[0].cells[0].value).toBe('key1');
      expect(table.rows[0].cells[0].text).toBe('Key 1');
      expect(table.rows[0].status).toBe(TableRow.Status.UPDATED);
    });
  });

  describe('cancelCellEdit', () => {
    let table;

    beforeEach(() => {
      let model = helper.createModelFixture(2, 2);
      table = helper.createTable(model);
      table.render();
      helper.applyDisplayStyle(table);
    });

    it('triggers cancelCellEdit event', () => {
      let triggeredEvent;
      table.columns[0].setEditable(true);
      table.prepareCellEdit(table.columns[0], table.rows[0]);
      jasmine.clock().tick(0);
      table.on('cancelCellEdit', event => {
        triggeredEvent = event;
      });
      table.cancelCellEdit();
      expect(triggeredEvent.column).toBe(table.columns[0]);
      expect(triggeredEvent.row).toBe(table.rows[0]);
      expect(triggeredEvent.field).toBe(table.rows[0].cells[0].field);
    });

    it('calls endCellEdit with saveEditorValue=false', () => {
      table.columns[0].setEditable(true);
      table.prepareCellEdit(table.columns[0], table.rows[0]);
      jasmine.clock().tick(0);
      spyOn(table, 'endCellEdit').and.callThrough();
      let field = table.cellEditorPopup.cell.field;

      table.cancelCellEdit();
      expect(table.endCellEdit).toHaveBeenCalledWith(field);
      jasmine.clock().tick(0);
      expect($findPopup().length).toBe(0);
    });

    it('does not save editor value', () => {
      table.columns[0].setEditable(true);
      table.prepareCellEdit(table.columns[0], table.rows[0]);
      jasmine.clock().tick(0);
      table.cellEditorPopup.cell.field.setValue('my new value');

      table.cancelCellEdit();
      expect(table.rows[0].cells[0].value).toBe('cell0_0');
    });
  });

  describe('endCellEdit', () => {
    let table;

    beforeEach(() => {
      let model = helper.createModelFixture(2, 2);
      table = helper.createTable(model);
      table.render();
      helper.applyDisplayStyle(table);
    });

    it('destroys the field', () => {
      table.prepareCellEdit(table.columns[0], table.rows[0]);
      jasmine.clock().tick(0);
      let popup = table.cellEditorPopup;
      let field = popup.cell.field;
      expect(field.destroyed).toBe(false);

      table.endCellEdit(field);
      expect(field.destroyed).toBe(true);
    });

    it('removes the cell editor popup', () => {
      table.prepareCellEdit(table.columns[0], table.rows[0]);
      jasmine.clock().tick(0);
      let popup = table.cellEditorPopup;
      let field = popup.cell.field;
      expect(field.destroyed).toBe(false);

      table.endCellEdit(field);
      jasmine.clock().tick(0);
      expect($findPopup().length).toBe(0);
      expect($findPopup().find('.form-field').length).toBe(0);
      expect(popup.rendered).toBe(false);
      expect(popup.cell.field.rendered).toBe(false);
    });
  });

  describe('validation', () => {
    let table, model, cell0_0, $tooltip;

    beforeEach(() => {
      model = helper.createModelFixture(2, 2);
      table = helper.createTable(model);
      cell0_0 = table.rows[0].cells[0];
    });

    it('shows a tooltip if field has an error', () => {
      cell0_0.editable = true;
      cell0_0.errorStatus = 'Validation error';
      $tooltip = $('.tooltip');

      expect($tooltip.length).toBe(0);
      table.render();
      $tooltip = $('.tooltip');
      expect($tooltip.length).toBe(1);
    });

    it('does not show a tooltip if field has no error', () => {
      cell0_0.editable = true;
      $tooltip = $('.tooltip');

      expect($tooltip.length).toBe(0);
      table.render();
      $tooltip = $('.tooltip');
      expect($tooltip.length).toBe(0);
    });
  });

  describe('popup recovery', () => {
    let model, table, row0, $cells0, $cell0_0;

    beforeEach(() => {
      model = helper.createModelFixture(2, 3);
      table = helper.createTable(model);
      row0 = table.rows[0];
    });

    it('reopens popup if row gets updated', () => {
      row0.cells[0].editable = true;
      table.render();
      $cells0 = table.$cellsForRow(row0.$row);
      $cell0_0 = $cells0.eq(0);
      table.prepareCellEdit(table.columns[0], row0);
      jasmine.clock().tick(0);
      expect(table.cellEditorPopup.row).toBe(row0);
      expect(table.cellEditorPopup.$anchor[0]).toBe($cell0_0[0]);

      let oldPopup = table.cellEditorPopup;
      let updatedRows = helper.createModelRows(2, 1);
      updatedRows[0].id = row0.id;
      table.updateRows(updatedRows);

      // Check if popup is correctly linked to updated row and new $cell
      row0 = table.rows[0];
      $cells0 = table.$cellsForRow(row0.$row);
      $cell0_0 = $cells0.eq(0);
      expect($findPopup().length).toBe(1);
      expect(table.cellEditorPopup.row).toBe(row0);
      expect(table.cellEditorPopup.$anchor[0]).toBe($cell0_0[0]);
      expect(oldPopup.destroyed).toBe(true);
    });

    it('closes popup if row gets deleted', () => {
      row0.cells[0].editable = true;
      table.render();
      table.prepareCellEdit(table.columns[0], row0);
      jasmine.clock().tick(0);
      spyOn(table, 'cancelCellEdit');

      table.deleteRows([row0]);

      // Check if popup is closed
      expect($findPopup().length).toBe(0);

      // Check whether cancel edit has been called
      expect(table.cancelCellEdit).toHaveBeenCalled();
    });

    it('closes popup if all rows get deleted', () => {
      row0.cells[0].editable = true;
      table.render();
      table.prepareCellEdit(table.columns[0], row0);
      jasmine.clock().tick(0);
      spyOn(table, 'cancelCellEdit');

      table.deleteAllRows();

      // Check if popup is closed
      expect($findPopup().length).toBe(0);

      // Check whether cancel edit has been called
      expect(table.cancelCellEdit).toHaveBeenCalled();
    });

    it('removes popup when table is detached', () => {
      row0.cells[0].editable = true;
      table.render();
      table.prepareCellEdit(table.columns[0], row0);
      jasmine.clock().tick(0);
      expect(table.cellEditorPopup).toBeTruthy();
      table.cellEditorPopup.cell.field.setValue('my new value');
      table.detach();
      jasmine.clock().tick(0);
      expect(table.cellEditorPopup.rendered).toBe(false);

      // Destroys popup after complete edit, even if table is not attached anymore
      table.completeCellEdit();
      expect(table.rows[0].cells[0].value).toBe('my new value');
      expect(table.cellEditorPopup).toBe(null);
    });

    it('removes popup when table is removed', () => {
      row0.cells[0].editable = true;
      table.render();
      table.prepareCellEdit(table.columns[0], row0);
      jasmine.clock().tick(0);
      expect(table.cellEditorPopup).toBeTruthy();
      table.cellEditorPopup.cell.field.setValue('my new value');
      table.remove();
      jasmine.clock().tick(0);
      expect(table.cellEditorPopup.rendered).toBe(false);
      expect(table.cellEditorPopup.cell.field.rendered).toBe(false);

      // Destroys popup after complete edit, even if table is not rendered anymore
      table.completeCellEdit();
      expect(table.rows[0].cells[0].value).toBe('my new value');
      expect(table.cellEditorPopup).toBe(null);
    });

    it('does not fail if table is detached and attached again', () => {
      row0.cells[0].editable = true;
      table.render();
      table.prepareCellEdit(table.columns[0], row0);
      jasmine.clock().tick(0);
      expect(table.cellEditorPopup).toBeTruthy();
      table.detach();
      jasmine.clock().tick(0);
      expect(table.cellEditorPopup.rendered).toBe(false);

      table.attach();
      expect(table.cellEditorPopup.rendered).toBe(true);
      expect(table.cellEditorPopup.cell.field.rendered).toBe(true);
    });

    it('does not fail if table is removed and rendered again', () => {
      row0.cells[0].editable = true;
      table.render();
      table.prepareCellEdit(table.columns[0], row0);
      jasmine.clock().tick(0);
      expect(table.cellEditorPopup).toBeTruthy();
      table.remove();
      jasmine.clock().tick(0);
      expect(table.cellEditorPopup.rendered).toBe(false);

      table.render();
      expect(table.cellEditorPopup.rendered).toBe(true);
      expect(table.cellEditorPopup.cell.field.rendered).toBe(true);
    });
  });

  describe('tooltip recovery', () => {
    let model, table, row0;

    beforeEach(() => {
      model = helper.createModelFixture(2, 3);
      table = helper.createTable(model);
      row0 = model.rows[0];
    });

    it('removes tooltip if row gets deleted', () => {
      row0.cells[0].editable = true;
      row0.cells[0].errorStatus = 'Validation error';

      table.render();
      expect($('.tooltip').length).toBe(1);
      expect(table.tooltips.length).toBe(1);

      table.deleteRows([row0]);

      expect($('.tooltip').length).toBe(0);
      expect(table.tooltips.length).toBe(0);
    });
  });
});
