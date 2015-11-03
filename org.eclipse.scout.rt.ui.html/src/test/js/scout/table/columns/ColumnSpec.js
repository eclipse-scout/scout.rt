/*******************************************************************************
 * Copyright (c) 2014-2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
/* global TableSpecHelper */
describe("Column", function() {
  var session;
  var helper;

  beforeEach(function() {
    setFixtures(sandbox());
    session = sandboxSession();
    helper = new TableSpecHelper(session);
    jasmine.Ajax.install();
    jasmine.clock().install();
  });

  afterEach(function() {
    session = null;
    jasmine.Ajax.uninstall();
    jasmine.clock().uninstall();
  });


  /**
   * Test assumes that default values for horiz. alignment are set on cell object.
   */
  it("considers horizontal alignment", function() {
    var model = helper.createModelFixture(3, 2);
    model.columns[1].horizontalAlignment = 0;
    model.columns[2].horizontalAlignment = 1;

    model.rows[0].cells[1].horizontalAlignment = 0;
    model.rows[0].cells[2].horizontalAlignment = 1;

    model.rows[1].cells[1].horizontalAlignment = 0;
    model.rows[1].cells[2].horizontalAlignment = 1;

    var table = helper.createTable(model);
    table.render(session.$entryPoint);

    var $headerItems = table.header.$container.find('.table-header-item');
    var $headerItem0 = $headerItems.eq(0);
    var $headerItem1 = $headerItems.eq(1);
    var $headerItem2 = $headerItems.eq(2);
    var $rows = table.$rows();
    var $cells0 = $rows.eq(0).find('.table-cell');
    var $cells1 = $rows.eq(1).find('.table-cell');

    // Left
    expect($headerItem0).toHaveClass('halign-left');
    expect($headerItem0).not.toHaveClass('halign-center');
    expect($headerItem0).not.toHaveClass('halign-right');
    expect($cells0.eq(0)).toHaveClass('halign-left');
    expect($cells0.eq(0)).not.toHaveClass('halign-center');
    expect($cells0.eq(0)).not.toHaveClass('halign-right');
    expect($cells1.eq(0)).toHaveClass('halign-left');
    expect($cells1.eq(0)).not.toHaveClass('halign-center');
    expect($cells1.eq(0)).not.toHaveClass('halign-right');

    // Center
    expect($headerItem1).not.toHaveClass('halign-left');
    expect($headerItem1).toHaveClass('halign-center');
    expect($headerItem1).not.toHaveClass('halign-right');
    expect($cells0.eq(1)).not.toHaveClass('halign-left');
    expect($cells0.eq(1)).toHaveClass('halign-center');
    expect($cells0.eq(1)).not.toHaveClass('halign-right');
    expect($cells1.eq(1)).not.toHaveClass('halign-left');
    expect($cells1.eq(1)).toHaveClass('halign-center');
    expect($cells1.eq(1)).not.toHaveClass('halign-right');

    // Right
    expect($headerItem2).not.toHaveClass('halign-left');
    expect($headerItem2).not.toHaveClass('halign-center');
    expect($headerItem2).toHaveClass('halign-right');
    expect($cells0.eq(2)).not.toHaveClass('halign-left');
    expect($cells0.eq(2)).not.toHaveClass('halign-center');
    expect($cells0.eq(2)).toHaveClass('halign-right');
    expect($cells1.eq(2)).not.toHaveClass('halign-left');
    expect($cells1.eq(2)).not.toHaveClass('halign-center');
    expect($cells1.eq(2)).toHaveClass('halign-right');
  });

  it("considers custom css class of a column", function() {
    var model = helper.createModelFixture(3, 2);
    model.columns[0].cssClass = 'abc';

    var table = helper.createTable(model);
    table.render(session.$entryPoint);

    var $headerItems = table.header.$container.find('.table-header-item');
    var $headerItem0 = $headerItems.eq(0);
    var $rows = table.$rows();
    var $cells0 = $rows.eq(0).find('.table-cell');
    var $cells1 = $rows.eq(1).find('.table-cell');

    expect($headerItem0).not.toHaveClass('abc');
    expect($cells0.eq(0)).toHaveClass('abc');
    expect($cells0.eq(1)).not.toHaveClass('abc');
    expect($cells1.eq(0)).toHaveClass('abc');
    expect($cells1.eq(1)).not.toHaveClass('abc');
  });

  it("considers custom css class of a column, as well for checkbox columns", function() {
    var model = helper.createModelFixture(3, 2);
    model.columns[0].cssClass = 'abc';
    model.columns[0].objectType = 'BooleanColumn';

    var table = helper.createTable(model);
    table.render(session.$entryPoint);

    var $headerItems = table.header.$container.find('.table-header-item');
    var $headerItem0 = $headerItems.eq(0);
    var $rows = table.$rows();
    var $cells0 = $rows.eq(0).find('.table-cell');
    var $cells1 = $rows.eq(1).find('.table-cell');

    expect($headerItem0).not.toHaveClass('abc');
    expect($cells0.eq(0)).toHaveClass('abc');
    expect($cells0.eq(1)).not.toHaveClass('abc');
    expect($cells1.eq(0)).toHaveClass('abc');
    expect($cells1.eq(1)).not.toHaveClass('abc');
  });

  it("considers custom css class of a cell, if both are set only the cell class is used", function() {
    var model = helper.createModelFixture(3, 2);
    model.columns[0].cssClass = 'abc';
    model.rows[0].cells[0].cssClass = 'custom-cell-0';

    var table = helper.createTable(model);
    table.render(session.$entryPoint);

    var $headerItems = table.header.$container.find('.table-header-item');
    var $headerItem0 = $headerItems.eq(0);
    var $rows = table.$rows();
    var $cells0 = $rows.eq(0).find('.table-cell');
    var $cells1 = $rows.eq(1).find('.table-cell');

    expect($headerItem0).not.toHaveClass('abc');
    expect($cells0.eq(0)).not.toHaveClass('abc');
    expect($cells0.eq(0)).toHaveClass('custom-cell-0');
    expect($cells0.eq(1)).not.toHaveClass('abc');
    expect($cells1.eq(0)).toHaveClass('abc');
    expect($cells1.eq(0)).not.toHaveClass('custom-cell-0');
    expect($cells1.eq(1)).not.toHaveClass('abc');
  });

  it("considers htmlEnabled of a cell", function() {
    var model = helper.createModelFixture(3, 2);
    model.rows[0].cells[0].text = '<b>hi</b>';
    model.rows[0].cells[0].htmlEnabled = false;
    model.rows[0].cells[1].text = '<b>hi</b>';
    model.rows[0].cells[1].htmlEnabled = true;

    var table = helper.createTable(model);
    table.render(session.$entryPoint);

    var $rows = table.$rows();
    var $cells0 = $rows.eq(0).find('.table-cell');

    expect($cells0.eq(0).text()).toBe('<b>hi</b>');
    expect($cells0.eq(1).text()).toBe('hi');
  });

  describe("MultilineText", function() {
    it("replaces\n with br, but only if htmlEnabled is false", function() {
      var model = helper.createModelFixture(3, 2);
      model.multilineText = true;
      model.rows[0].cells[0].text = '<br>hello\nyou';
      model.rows[0].cells[0].htmlEnabled = false;
      model.rows[0].cells[1].text = '<br>hello\nyou';
      model.rows[0].cells[1].htmlEnabled = true;

      var table = helper.createTable(model);
      table.render(session.$entryPoint);

      var $rows = table.$rows();
      var $cells0 = $rows.eq(0).find('.table-cell');

      expect($cells0.eq(0).html()).toBe('&lt;br&gt;hello<br>you');
      // No replacement, provided html should be left untouched
      expect($cells0.eq(1).html()).toBe('<br>hello\nyou');
    });
  });

  describe("TextWrap", function() {
    var table, model, $rows, $cells0, $cell0_0, $cell0_1;

    beforeEach(function() {
      model = helper.createModelFixture(2, 2);
      table = helper.createTable(model);
    });

    it("wraps text if column.textWrap and table.multilineText are true", function() {
      table.multilineText = true;
      table.columns[0].textWrap = true;
      table.render(session.$entryPoint);
      $rows = table.$rows();
      $cells0 = $rows.eq(0).find('.table-cell');
      $cell0_0 = $cells0.eq(0);
      expect($cell0_0).not.toHaveClass('white-space-nowrap');
    });

    it("does not wrap text if column.textWrap is false and table.multilineText is true", function() {
      table.multilineText = true;
      table.columns[0].textWrap = false;
      table.render(session.$entryPoint);
      $rows = table.$rows();
      $cells0 = $rows.eq(0).find('.table-cell');
      $cell0_0 = $cells0.eq(0);
      expect($cell0_0).toHaveClass('white-space-nowrap');
    });

    it("does not wrap text if column.textWrap is true and table.multilineText is false", function() {
      table.multilineText = false;
      table.columns[0].textWrap = true;
      table.render(session.$entryPoint);
      $rows = table.$rows();
      $cells0 = $rows.eq(0).find('.table-cell');
      $cell0_0 = $cells0.eq(0);
      expect($cell0_0).toHaveClass('white-space-nowrap');
    });
  });
});
