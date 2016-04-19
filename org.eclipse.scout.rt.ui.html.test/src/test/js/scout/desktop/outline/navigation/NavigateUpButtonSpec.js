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
describe('NavigateUpButton', function() {

  var session, outline, menu, node = {};

  beforeEach(function() {
    setFixtures(sandbox());
    session = sandboxSession();
    outline = {
      session: session,
      navigateToTop: function() {}
    };
    var model = createSimpleModel('NavigateUpButton', session);
    model.outline = outline;
    model.node = node;
    menu = new scout.NavigateUpButton();
    menu.init(model);
  });

  it('_toggleDetail is always true', function() {
    expect(menu._toggleDetail()).toBe(true);
  });

  it('_isDetail returns true or false depending on the state of the detail-form and detail-table', function() {
    // false when both detailForm and detailTable are visible
    node.detailForm = {};
    node.detailFormVisible = true;
    node.detailFormVisibleByUi = true;
    node.detailTable = {};
    node.detailTableVisible = true;
    expect(menu._isDetail()).toBe(false);

    // false when detailForm is absent, even when if detailFormVisible=true
    delete node.detailForm;
    expect(menu._isDetail()).toBe(false);
    node.detailForm = {};

    // false when detailTable is absent, even when if detailTableVisible=true
    delete node.detailTable;
    expect(menu._isDetail()).toBe(false);
    node.detailTable = {};

    // true when detailForm is hidden by UI
    node.detailFormVisibleByUi = false;
    expect(menu._isDetail()).toBe(true);
    node.detailFormVisibleByUi = true;

    // false when property says to
    node.detailFormVisible = false;
    expect(menu._isDetail()).toBe(false);
    node.detailFormVisible = true;
    node.detailTableVisible = false;
    expect(menu._isDetail()).toBe(false);
  });

  describe('_buttonEnabled', function() {

    it('is true when current node has a parent or...', function() {
      node.parentNode = {};
      outline.defaultDetailForm = undefined;
      expect(menu._buttonEnabled()).toBe(true);
    });

    it('is true when current node is a top-level node and outline a default detail-form or...', function() {
      node.parentNode = undefined;
      outline.defaultDetailForm = {};
      expect(menu._buttonEnabled()).toBe(true);
    });

    it('is false otherwise', function() {
      node.parentNode = undefined;
      outline.defaultDetailForm = undefined;
      expect(menu._buttonEnabled()).toBe(false);
    });

  });

  describe('_drill', function() {

    beforeEach(function() {
      outline.selectNodes = function(node) {};
      outline.collapseNode = function(node) {};
      outline.collapseAll = function(node) {};
    });

    it('drills up to parent node, sets the selection on the tree', function() {
      node.parentNode = {};
      spyOn(outline, 'selectNodes');
      spyOn(outline, 'collapseNode');
      menu._drill();
      expect(outline.navigateUpInProgress).toBe(true);
      expect(outline.selectNodes).toHaveBeenCalledWith(node.parentNode);
      expect(outline.collapseNode).toHaveBeenCalledWith(node.parentNode, {collapseChildNodes: true});
    });

    it('shows default detail-form or outline overview', function() {
      node.parentNode = undefined;
      menu.drill;
      spyOn(outline, 'navigateToTop');
      menu._drill();
      expect(outline.navigateToTop).toHaveBeenCalled();
    });

  });

});
