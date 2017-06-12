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
describe('NavigateButton', function() {

  var session, helper, formHelper;

  beforeEach(function() {
    setFixtures(sandbox());
    session = sandboxSession();
    helper = new scout.OutlineSpecHelper(session);
    formHelper = new scout.FormSpecHelper(session);
  });

  it('is only created once for each node', function() {
    var model = helper.createModelFixture(3, 2, true);
    model.nodes[0].detailForm = formHelper.createFormWithOneField();
    model.nodes[0].detailFormVisible = true;
    model.nodes[1].detailForm = formHelper.createFormWithOneField();
    model.nodes[1].detailFormVisible = true;
    var outline = helper.createOutline(model);
    var staticMenus = outline.nodes[0].detailForm.rootGroupBox.staticMenus;
    expect(staticMenus[0] instanceof scout.NavigateUpButton).toBe(true);
    expect(staticMenus[1] instanceof scout.NavigateDownButton).toBe(true);

    outline.selectNode(outline.nodes[1]);
    outline.selectNode(outline.nodes[0]);
    var newStaticMenus = outline.nodes[0].detailForm.rootGroupBox.staticMenus;
    expect(newStaticMenus[0] instanceof scout.NavigateUpButton).toBe(true);
    expect(newStaticMenus[1] instanceof scout.NavigateDownButton).toBe(true);
    // static menus should still be the same
    expect(newStaticMenus[0]).toBe(staticMenus[0]);
    expect(newStaticMenus[1]).toBe(staticMenus[1]);
  });

  it('will be destroyed when navigateButtonsVisible is set to false', function() {
    var model = helper.createModelFixture(3, 2, true);
    model.nodes[0].detailForm = formHelper.createFormWithOneField();
    model.nodes[0].detailFormVisible = true;
    model.nodes[1].detailForm = formHelper.createFormWithOneField();
    model.nodes[1].detailFormVisible = true;
    var outline = helper.createOutline(model);
    var staticMenus = outline.nodes[0].detailForm.rootGroupBox.staticMenus;
    expect(staticMenus[0] instanceof scout.NavigateUpButton).toBe(true);
    expect(staticMenus[1] instanceof scout.NavigateDownButton).toBe(true);

    outline.setNavigateButtonsVisible(false);
    var newStaticMenus = outline.nodes[0].detailForm.rootGroupBox.staticMenus;
    expect(newStaticMenus.length).toBe(0);
    expect(staticMenus[0].destroyed).toBe(true);
    expect(staticMenus[1].destroyed).toBe(true);
  });

});
