/*
 * Copyright (c) 2010, 2025 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {Cell, CellEditorPopup, Column, FormField, keys, Popup, scout, SmartColumn, SmartField, StaticLookupCall, Status, StringField, Table, TableRow, Widget} from '../../../src/index';
import {FormSpecHelper, JQueryTesting, SpecSmartField, TableSpecHelper} from '../../../src/testing/index';

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
  });

  afterEach(() => {
    session = null;
    jasmine.Ajax.uninstall();
    findPopup()?.destroy();
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
    let table: Table, model, $rows, $cells0, $cells1, $cell0_0, $cell0_1, $cell1_0;

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

    it('starts the cell editor for the next editable cell', async () => {
      table.rows[0].cells[0].editable = true;
      table.rows[1].cells[0].editable = true;

      table.focusCell(table.columns[0], table.rows[0]);
      await sleep();
      assertCellEditorIsOpen(table, table.columns[0], table.rows[0]);

      JQueryTesting.triggerKeyInputCapture($(document.activeElement as HTMLElement), keys.TAB);
      await sleep();
      assertCellEditorIsOpen(table, table.columns[0], table.rows[1]);
    });

    it('starts the cell editor for the next visible cell', async () => {
      table.rows[0].cells[0].setEditable(true);
      table.rows[0].cells[1].setEditable(true);
      table.rows[1].cells[2].setEditable(true);
      table.columns[0].setVisible(false);
      table.columns[1].setVisible(false);

      table.focusCell(table.columns[0], table.rows[0]);
      await sleep();
      assertCellEditorIsOpen(table, table.columns[0], table.rows[0]);

      JQueryTesting.triggerKeyInputCapture($(document.activeElement as HTMLElement), keys.TAB);
      await sleep();
      assertCellEditorIsOpen(table, table.columns[2], table.rows[1]);
    });
  });

  describe('prepareCellEdit', () => {
    let table: Table;

    beforeEach(() => {
      let model = helper.createModelFixture(2, 2);
      table = helper.createTable(model);
      table.render();
      helper.applyDisplayStyle(table);
    });

    it('creates field and calls start', async () => {
      table.columns[0].setEditable(true);
      spyOn(table, 'startCellEdit').and.callThrough();

      await table.prepareCellEdit(table.columns[0], table.rows[0]);
      expect(table.startCellEdit).toHaveBeenCalled();
      assertCellEditorIsOpen(table, table.columns[0], table.rows[0]);
    });

    it('copies the value to the field if cell was valid', async () => {
      let column = table.columns[0];
      let row = table.rows[0];
      column.setEditable(true);
      column.setCellValue(row, 'valid value');
      await table.prepareCellEdit(column, row);
      assertCellEditorIsOpen(table, column, row);
      let field = table.cellEditorPopup.cell.field;
      expect(field.value).toEqual('valid value');
      expect(field.displayText).toEqual('valid value');
      expect(field.errorStatus).toEqual(null);
    });

    it('copies the text and the error to the field if cell was invalid', async () => {
      let column = table.columns[0];
      let row = table.rows[0];
      column.setEditable(true);
      column.setCellValue(row, 'valid value');
      column.setCellText(row, 'invalid value');
      column.setCellErrorStatus(row, Status.error('error'));
      await table.prepareCellEdit(column, row);
      assertCellEditorIsOpen(table, column, row);
      let field = table.cellEditorPopup.cell.field;
      expect(field.value).toEqual(null);
      expect(field.displayText).toEqual('invalid value');
      expect(field.errorStatus.message).toEqual('error');
    });

    it('triggers prepareCellEdit event', async () => {
      let triggeredEvent;
      table.columns[0].setEditable(true);
      table.on('prepareCellEdit', event => {
        triggeredEvent = event;
      });
      await table.prepareCellEdit(table.columns[0], table.rows[0]);
      expect(triggeredEvent.column).toBe(table.columns[0]);
      expect(triggeredEvent.row).toBe(table.rows[0]);
    });

    it('can open the field popup', async () => {
      table.insertColumn({
        objectType: SmartColumn,
        lookupCall: {objectType: DummyLookupCall},
        editable: true
      });
      table.insertRows({cells: ['1', '1', 'key0']});
      table.prepareCellEdit(table.columns[2], table.rows[0], true);
      expect(table.cellEditorPopup).toBe(null);

      // Update buffer is updating because rows with smart values are inserted -> wait until lookup call is resolved and table.loading set to false
      await sleep(500);
      let field = (table.cellEditorPopup.cell.field as SmartField<any>);
      expect(field.$field).toBeFocused();
      expect(field.popup.rendered).toBe(true);

      table.cancelCellEdit();

      // Try again without loading delay so that loading indicator is rendered
      table.loadingSupport.loadingIndicatorDelay = 0;
      table.insertRows({cells: ['2', '2', 'key0']});
      table.prepareCellEdit(table.columns[2], table.rows[0], true);
      expect(table.cellEditorPopup).toBe(null);

      // Again, wait for lookup call. Loading indicator is drawn on a glass pane and indicator removed by CSS animation.
      // Glass pane must be deactivated immediately not only when the animation finishes,
      // otherwise it would prevent the popup from being opened because smart field does not have the focus, see isFocused() in SmartField._lookupByTextOrAllDone.
      await sleep(500);
      field = (table.cellEditorPopup.cell.field as SmartField<any>);
      expect(field.$field).toBeFocused();
      expect(field.popup.rendered).toBe(true);
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

    it('activates cell editor mode', () => {
      table.columns[0].setEditable(true);
      table.columns[0].setHorizontalAlignment(1);

      let field = createStringField();
      expect(field.mode).toBe(FormField.Mode.DEFAULT);
      expect(field.gridData.horizontalAlignment).toBe(-1);
      spyOn(field, 'activateCellEditorMode').and.callThrough();

      table.startCellEdit(table.columns[0], table.rows[0], field);
      expect(field.mode).toBe(FormField.Mode.CELLEDITOR);
      expect(field.gridData.horizontalAlignment).toBe(1);
      expect(field.activateCellEditorMode).toHaveBeenCalledOnceWith({
        column: table.columns[0],
        row: table.rows[0]
      });
    });

    it('triggers startCellEdit event', async () => {
      let triggeredEvent;
      table.columns[0].setEditable(true);
      table.on('startCellEdit', event => {
        triggeredEvent = event;
      });
      await table.prepareCellEdit(table.columns[0], table.rows[0]);
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

    it('postpones opening if update buffer is buffering', async () => {
      table.insertColumn({
        objectType: SmartColumn,
        lookupCall: {objectType: DummyLookupCall},
        editable: true
      });
      table.insertRows({cells: ['a', 'b', 'key0']});
      expect(table.updateBuffer.isBuffering()).toBe(true);

      let field = table.columns[2].createEditor(table.rows[0]);
      table.startCellEdit(table.columns[2], table.rows[0], field);
      expect(table.cellEditorPopup).toBe(null);

      await sleep(500);
      expect(table.updateBuffer.isBuffering()).toBe(false);
      assertCellEditorIsOpen(table, table.columns[2], table.rows[0]);
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

    it('does nothing if the editor is already open for the given field', () => {
      table.columns[0].setEditable(true);
      let triggeredEvents = 0;
      table.on('startCellEdit', event => {
        triggeredEvents++;
      });

      let field = createStringField();
      let popup = table.startCellEdit(table.columns[0], table.rows[0], field);
      expect(popup.cell.field).toBe(field);
      assertCellEditorIsOpen(table, table.columns[0], table.rows[0]);

      let popup2 = table.startCellEdit(table.columns[0], table.rows[0], field);
      expect(popup).toBe(popup2);
      assertCellEditorIsOpen(table, table.columns[0], table.rows[0]);
      expect(triggeredEvents).toBe(1);
    });

    it('ends existing cell edit if an editor is open', async () => {
      table.columns[0].setEditable(true);
      table.insertColumn({
        objectType: SmartColumn,
        lookupCall: {objectType: DummyLookupCall},
        editable: true
      });
      table.insertRows({cells: ['a', 'b', 'key0']});
      let field = table.columns[2].createEditor(table.rows[0]);
      table.startCellEdit(table.columns[2], table.rows[0], field);
      await sleep(500);
      assertCellEditorIsOpen(table, table.columns[2], table.rows[0]);
      let popup = table.cellEditorPopup;
      expect(popup.cell.field).toBe(field);
      expect(field.rendered).toBe(true);

      // Start cell edit on same cell again
      let field2 = table.columns[2].createEditor(table.rows[0]);
      table.startCellEdit(table.columns[2], table.rows[0], field2);
      assertCellEditorIsOpen(table, table.columns[2], table.rows[0]);
      let popup2 = table.cellEditorPopup;
      expect(popup2.cell.field).toBe(field2);
      expect(field2.rendered).toBe(true);
      expect(popup.destroyed).toBe(true);
      expect(field.destroyed).toBe(true);

      // Start cell edit on another cell
      let field3 = table.columns[0].createEditor(table.rows[0]);
      table.startCellEdit(table.columns[0], table.rows[0], field3);
      assertCellEditorIsOpen(table, table.columns[0], table.rows[0]);
      let popup3 = table.cellEditorPopup;
      expect(popup3.cell.field).toBe(field3);
      expect(field3.rendered).toBe(true);
      expect(popup2.destroyed).toBe(true);
      expect(field2.destroyed).toBe(true);
    });
  });

  describe('completeCellEdit', () => {
    let table: Table;

    beforeEach(() => {
      let model = helper.createModelFixture(2, 2);
      table = helper.createTable(model);
      table.render();
      helper.applyDisplayStyle(table);
    });

    it('triggers completeCellEdit event', async () => {
      let triggeredEvent;
      table.columns[0].setEditable(true);
      await table.prepareCellEdit(table.columns[0], table.rows[0]);
      table.on('completeCellEdit', event => {
        triggeredEvent = event;
      });
      table.completeCellEdit();
      expect(triggeredEvent.column).toBe(table.columns[0]);
      expect(triggeredEvent.row).toBe(table.rows[0]);
      expect(triggeredEvent.field).toBe(table.rows[0].cells[0].field);
    });

    it('calls endCellEdit with saveEditorValue=true', async () => {
      table.columns[0].setEditable(true);
      await table.prepareCellEdit(table.columns[0], table.rows[0]);
      spyOn(table, 'endCellEdit').and.callThrough();
      let field = table.cellEditorPopup.cell.field;

      table.completeCellEdit();
      expect(table.endCellEdit).toHaveBeenCalledWith(field, true);
      await Promise.resolve();
      expect($findPopup().length).toBe(0);
    });

    it('saves editor value', async () => {
      table.columns[0].setEditable(true);
      await table.prepareCellEdit(table.columns[0], table.rows[0]);
      table.cellEditorPopup.cell.field.setValue('my new value');

      table.completeCellEdit();
      expect(table.rows[0].cells[0].value).toBe('my new value');
    });

    it('copies the value to the cell if field was valid', async () => {
      table.columns[0].setEditable(true);
      await table.prepareCellEdit(table.columns[0], table.rows[0]);
      table.cellEditorPopup.cell.field.setValue('my new value');

      table.completeCellEdit();
      let cell = table.rows[0].cells[0];
      expect(cell.value).toBe('my new value');
      expect(cell.text).toBe('my new value');
      expect(cell.errorStatus).toBe(null);
      expect($('.tooltip').length).toBe(0);
    });

    it('copies the text and error to the cell if field was invalid', async () => {
      let column = table.columns[0];
      let row = table.rows[0];
      let cell = row.cells[0];
      expect($('.tooltip').length).toBe(0);

      column.setEditable(true);
      column.setCellValue(row, 'valid value');
      await table.prepareCellEdit(column, row);

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

    it('clears the error if value is now valid', async () => {
      let column = table.columns[0];
      let row = table.rows[0];
      let cell = row.cells[0];
      expect($('.tooltip').length).toBe(0);

      column.setEditable(true);
      column.setCellValue(row, 'valid value');
      await table.prepareCellEdit(column, row);

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
      await table.prepareCellEdit(column, row);
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

    it('clears the error if value is now valid even when changed to the original value', async () => {
      let column = table.columns[0];
      let row = table.rows[0];
      let cell = row.cells[0];
      expect($('.tooltip').length).toBe(0);

      column.setEditable(true);
      column.setCellValue(row, 'valid value');
      await table.prepareCellEdit(column, row);

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
      await table.prepareCellEdit(column, row);
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

    it('does not reopen the editor again', async () => {
      table.columns[0].setEditable(true);
      await table.prepareCellEdit(table.columns[0], table.rows[0]);
      table.cellEditorPopup.cell.field.setValue('my new value');

      let triggeredStartCellEditEvent = null;
      table.on('startCellEdit', event => {
        triggeredStartCellEditEvent = event;
      });
      table.completeCellEdit();
      // CompleteCellEdit triggers updateRows which would reopen the editor -> this must not happen if the editor was closed
      expect(triggeredStartCellEditEvent).toBe(null);
    });

    it('is called when another popup opens', async () => {
      table.columns[0].setEditable(true);
      await table.prepareCellEdit(table.columns[0], table.rows[0]);
      table.cellEditorPopup.cell.field.setValue('my new value');

      scout.create(Popup, {parent: session.desktop}).open();
      expect(table.rows[0].cells[0].value).toBe('my new value');
      expect(table.cellEditorPopup).toBe(null);
    });
  });

  describe('completeCellEdit in SmartColumn', () => {
    let table: Table;
    let lookupCall: DummyLookupCall;

    beforeEach(() => {
      lookupCall = new DummyLookupCall();
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
      expect(table.updateBuffer.promises.size).toBe(0);
    });

    it('does not fail when completing edit after removing a value', done => {
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

    it('triggers update row event containing row with correct state', async () => {
      table.columns[0].setEditable(true);
      table.markRowsAsNonChanged();
      table.prepareCellEdit(table.columns[0], table.rows[0], true);
      await sleep(300);
      table.cellEditorPopup.cell.field.setValue('key1');
      await sleep(300);
      let updateRowCount = 0;
      table.on('rowsUpdated', event => {
        expect(event.rows[0].cells[0].value).toBe('key1');
        expect(event.rows[0].cells[0].text).toBe('Key 1');
        expect(event.rows[0].status).toBe(TableRow.Status.UPDATED);
        updateRowCount++;
      });
      table.completeCellEdit();
      await sleep(300);
      expect(updateRowCount).toBe(1);
    });

    it('updates the value even if the table has been removed in the meantime', async () => {
      table.columns[0].setEditable(true);
      table.markRowsAsNonChanged();
      table.prepareCellEdit(table.columns[0], table.rows[0], true);
      await sleep(300);
      let field = table.cellEditorPopup.cell.field as SpecSmartField;
      field.$field.val('Key 1');
      field._userWasTyping = true;
      table.cellEditorPopup.completeEdit(); // Will execute table.completeCellEdit async
      table.remove();
      await sleep(300);
      expect(table.rows[0].cells[0].value).toBe('key1');
      expect(table.rows[0].cells[0].text).toBe('Key 1');
      expect(table.rows[0].status).toBe(TableRow.Status.UPDATED);
    });

    it('closes the editor even if acceptInput was called again', async () => {
      table.columns[0].setEditable(true);
      lookupCall.setDelay(5);
      await table.prepareCellEdit(table.columns[0], table.rows[0], true);
      let popup = table.cellEditorPopup;
      let field = popup.cell.field as SpecSmartField;
      field.$field.val('asdf');
      field._userWasTyping = true;

      let completed = table.cellEditorPopup.completeEdit();
      setTimeout(() => table.cellEditorPopup.cell.field.acceptInput()); // May happen if another popup opens that requests the focus -> smart field loses focus and accepts input
      await completed;
      await popup.when('destroy');
      expect(table.cellEditorPopup).toBe(null);
      expect(table.cell(table.columns[0], table.rows[0]).text).toBe('asdf');
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

    it('triggers cancelCellEdit event', async () => {
      let triggeredEvent;
      table.columns[0].setEditable(true);
      await table.prepareCellEdit(table.columns[0], table.rows[0]);
      table.on('cancelCellEdit', event => {
        triggeredEvent = event;
      });
      table.cancelCellEdit();
      expect(triggeredEvent.column).toBe(table.columns[0]);
      expect(triggeredEvent.row).toBe(table.rows[0]);
      expect(triggeredEvent.field).toBe(table.rows[0].cells[0].field);
    });

    it('calls endCellEdit with saveEditorValue=false', async () => {
      table.columns[0].setEditable(true);
      await table.prepareCellEdit(table.columns[0], table.rows[0]);
      spyOn(table, 'endCellEdit').and.callThrough();
      let field = table.cellEditorPopup.cell.field;

      table.cancelCellEdit();
      expect(table.endCellEdit).toHaveBeenCalledWith(field);
      await Promise.resolve();
      expect($findPopup().length).toBe(0);
    });

    it('does not save editor value', async () => {
      table.columns[0].setEditable(true);
      await table.prepareCellEdit(table.columns[0], table.rows[0]);
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

    it('destroys the field', async () => {
      await table.prepareCellEdit(table.columns[0], table.rows[0]);
      let popup = table.cellEditorPopup;
      let field = popup.cell.field;
      expect(field.destroyed).toBe(false);

      table.endCellEdit(field);
      expect(field.destroyed).toBe(true);
    });

    it('removes the cell editor popup', async () => {
      await table.prepareCellEdit(table.columns[0], table.rows[0]);
      let popup = table.cellEditorPopup;
      let field = popup.cell.field;
      expect(field.destroyed).toBe(false);

      table.endCellEdit(field);
      await Promise.resolve();
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

    it('reopens popup if row gets updated', async () => {
      row0.cells[0].editable = true;
      table.render();
      $cells0 = table.$cellsForRow(row0.$row);
      $cell0_0 = $cells0.eq(0);
      await table.prepareCellEdit(table.columns[0], row0);
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

    it('closes popup if row gets deleted', async () => {
      row0.cells[0].editable = true;
      table.render();
      await table.prepareCellEdit(table.columns[0], row0);
      spyOn(table, 'cancelCellEdit');

      table.deleteRows([row0]);

      // Check if popup is closed
      expect($findPopup().length).toBe(0);

      // Check whether cancel edit has been called
      expect(table.cancelCellEdit).toHaveBeenCalled();
    });

    it('closes popup if all rows get deleted', async () => {
      row0.cells[0].editable = true;
      table.render();
      await table.prepareCellEdit(table.columns[0], row0);
      spyOn(table, 'cancelCellEdit');

      table.deleteAllRows();

      // Check if popup is closed
      expect($findPopup().length).toBe(0);

      // Check whether cancel edit has been called
      expect(table.cancelCellEdit).toHaveBeenCalled();
    });

    it('removes popup when table is detached', async () => {
      row0.cells[0].editable = true;
      table.render();
      await table.prepareCellEdit(table.columns[0], row0);
      expect(table.cellEditorPopup).toBeTruthy();
      table.cellEditorPopup.cell.field.setValue('my new value');
      table.detach();
      await Promise.resolve();
      expect(table.cellEditorPopup.rendered).toBe(false);

      // Destroys popup after complete edit, even if table is not attached anymore
      table.completeCellEdit();
      expect(table.rows[0].cells[0].value).toBe('my new value');
      expect(table.cellEditorPopup).toBe(null);
    });

    it('removes popup when table is removed', async () => {
      row0.cells[0].editable = true;
      table.render();
      await table.prepareCellEdit(table.columns[0], row0);
      expect(table.cellEditorPopup).toBeTruthy();
      table.cellEditorPopup.cell.field.setValue('my new value');
      table.remove();
      await Promise.resolve();
      expect(table.cellEditorPopup.rendered).toBe(false);
      expect(table.cellEditorPopup.cell.field.rendered).toBe(false);

      // Destroys popup after complete edit, even if table is not rendered anymore
      table.completeCellEdit();
      expect(table.rows[0].cells[0].value).toBe('my new value');
      expect(table.cellEditorPopup).toBe(null);
    });

    it('does not fail if table is detached and attached again', async () => {
      row0.cells[0].editable = true;
      table.render();
      await table.prepareCellEdit(table.columns[0], row0);
      expect(table.cellEditorPopup).toBeTruthy();
      table.detach();
      await Promise.resolve();
      expect(table.cellEditorPopup.rendered).toBe(false);

      table.attach();
      expect(table.cellEditorPopup.rendered).toBe(true);
      expect(table.cellEditorPopup.cell.field.rendered).toBe(true);
    });

    it('does not fail if table is removed and rendered again', async () => {
      row0.cells[0].editable = true;
      table.render();
      await table.prepareCellEdit(table.columns[0], row0);
      expect(table.cellEditorPopup).toBeTruthy();
      table.remove();
      await Promise.resolve();
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

  describe('close', () => {
    it('calls completeCellEdit', async () => {
      let model = helper.createModelFixture(2, 2);
      let table = helper.createTable(model);
      table.render();
      table.columns[0].setEditable(true);
      let preparePromise = table.prepareCellEdit(table.columns[0], table.rows[0]);
      spyOn(table, 'completeCellEdit');
      await preparePromise;

      // Editing is normally finished by calling completeCellEdit or cancelCellEdit
      // Calling close just destroys the editor, and it will be re-opened once the table resp. the row will be re-rendered again because the table still has a reference.
      // This is not expected -> if close is called (e.g. if another popup opens) the cell edit should be completed
      table.cellEditorPopup.close();
      expect(table.completeCellEdit).toHaveBeenCalled();
    });
  });
});
