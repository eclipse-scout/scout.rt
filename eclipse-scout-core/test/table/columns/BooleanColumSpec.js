/*
 * Copyright (c) 2010-2019 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
import {BooleanColumn} from '../../../src/index';
import {TableSpecHelper} from '@eclipse-scout/testing';

describe('BooleanColumn', () => {
  let session;
  let helper;

  beforeEach(() => {
    setFixtures(sandbox());
    session = sandboxSession();
    helper = new TableSpecHelper(session);
    jasmine.Ajax.install();
    jasmine.clock().install();
  });

  afterEach(() => {
    session = null;
    jasmine.Ajax.uninstall();
    jasmine.clock().uninstall();
  });

  describe('table checkable column', () => {

    it('a checkbox column gets inserted if table.checkable=true', () => {
      let model = helper.createModelFixture(2);
      model.checkable = true;
      expect(model.columns.length).toBe(2);
      let table = helper.createTable(model);
      expect(table.columns.length).toBe(3);

      table.render();
      expect(table.columns[0] instanceof BooleanColumn).toBeTruthy();
    });

    it('no checkbox column gets inserted if table.checkable=false', () => {
      let model = helper.createModelFixture(2);
      model.checkable = false;
      expect(model.columns.length).toBe(2);
      let table = helper.createTable(model);
      expect(table.columns.length).toBe(2);

      table.render();
      expect(table.columns[0] instanceof BooleanColumn).toBeFalsy();
    });

    it('this.checkableColumn is set to the new column', () => {
      let model = helper.createModelFixture(2);
      model.checkable = true;
      let table = helper.createTable(model);
      table.render();

      expect(table.checkableColumn).not.toBeUndefined();
      expect(table.checkableColumn).toBe(table.columns[0]);
    });

    it('displays the row.checked state as checkbox', () => {
      let model = helper.createModelFixture(2, 2);
      model.checkable = true;
      model.rows[0].checked = true;
      model.rows[1].checked = false;
      let table = helper.createTable(model);
      table.render();
      let $rows = table.$rows();
      let $checkbox = table.columns[0].$checkBox($rows.eq(0));
      expect($checkbox).toHaveClass('checked');
      $checkbox = table.columns[0].$checkBox($rows.eq(1));
      expect($checkbox).not.toHaveClass('checked');
    });

  });

  describe('boolean column', () => {

    it('displays the cell value as checkbox', () => {
      let model = helper.createModelFixture(2, 2);
      model.columns[0].objectType = 'BooleanColumn';
      model.rows[0].cells[0].value = true;
      model.rows[1].cells[0].value = false;
      let table = helper.createTable(model);
      table.render();
      let $rows = table.$rows();
      let $checkbox = table.columns[0].$checkBox($rows.eq(0));
      expect($checkbox).toHaveClass('checked');
      $checkbox = table.columns[0].$checkBox($rows.eq(1));
      expect($checkbox).not.toHaveClass('checked');
    });

  });

});
