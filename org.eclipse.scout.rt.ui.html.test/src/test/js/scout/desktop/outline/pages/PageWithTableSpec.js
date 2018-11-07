/*******************************************************************************
 * Copyright (c) 2014-2017 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
describe('PageWithTable', function() {

  var session, helper;

  /** @type {scout.Outline} */
  var outline;
  /** @type {scout.PageWithTable} */
  var page;

  beforeEach(function() {
    setFixtures(sandbox());
    session = sandboxSession();
    helper = new scout.OutlineSpecHelper(session);
    outline = helper.createOutline();

    page = scout.create('PageWithTable', {
      parent: outline,
      detailTable: {
        objectType: 'Table'
      }
    });
    outline.insertNodes([page], null);
    outline.render();
    page.detailTable.render();
  });

  it('updates the page on table reload', function() {
    var counter = 0;
    page._loadTableData = function(searchFilter){
      counter++;
      return $.resolvedDeferred();
    };
    page.detailTable.reload(); // this should trigger the _loadTableData of the page

    expect(page.detailTable.hasReloadHandler).toBe(true);
    expect(counter).toBe(1);
  });

});
