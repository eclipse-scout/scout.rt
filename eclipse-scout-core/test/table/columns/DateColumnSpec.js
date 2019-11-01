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
import {dates} from '../../../src/index';
import {TableSpecHelper} from '@eclipse-scout/testing';


/* global LocaleSpecHelper */
/* global linkWidgetAndAdapter */
describe('DateColumn', function() {
  var session;
  var helper;
  var locale;

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

  describe('format', function() {
    it('updates the value and the display text if the format changes', function() {
      var testDate = dates.create('2017-01-01 13:01');
      var model = helper.createModelSingleColumnByValues([testDate], 'DateColumn');
      var table = helper.createTable(model);
      var column0 = table.columns[0];
      column0.setFormat();
      table.render();

      expect(column0.cell(table.rows[0]).text).toBe('01.01.2017');
      expect(column0.cell(table.rows[0]).value).toBe(testDate);

      column0.setFormat('yyyy-MM-dd');
      expect(column0.cell(table.rows[0]).text).toBe('2017-01-01');
    });
  });
});
