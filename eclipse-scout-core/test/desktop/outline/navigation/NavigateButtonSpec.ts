/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {NavigateButton, NavigateDownButton, NavigateUpButton} from '../../../../src/index';
import {FormSpecHelper, OutlineSpecHelper} from '../../../../src/testing/index';

describe('NavigateButton', () => {

  let session: SandboxSession, helper: OutlineSpecHelper, formHelper: FormSpecHelper;

  beforeEach(() => {
    setFixtures(sandbox());
    session = sandboxSession();
    helper = new OutlineSpecHelper(session);
    formHelper = new FormSpecHelper(session);
  });

  it('is only created once for each node', () => {
    let model = helper.createModelFixture(3, 2, true);
    model.nodes[0].detailForm = formHelper.createFormWithOneField();
    model.nodes[0].detailFormVisible = true;
    model.nodes[1].detailForm = formHelper.createFormWithOneField();
    model.nodes[1].detailFormVisible = true;
    let outline = helper.createOutline(model);
    let staticMenus = outline.nodes[0].detailForm.rootGroupBox.staticMenus;
    expect(staticMenus[0] instanceof NavigateUpButton).toBe(true);
    expect(staticMenus[1] instanceof NavigateDownButton).toBe(true);

    outline.selectNode(outline.nodes[1]);
    outline.selectNode(outline.nodes[0]);
    let newStaticMenus = outline.nodes[0].detailForm.rootGroupBox.staticMenus;
    expect(newStaticMenus[0] instanceof NavigateUpButton).toBe(true);
    expect(newStaticMenus[1] instanceof NavigateDownButton).toBe(true);
    // static menus should still be the same
    expect(newStaticMenus[0]).toBe(staticMenus[0]);
    expect(newStaticMenus[1]).toBe(staticMenus[1]);
  });

  it('will be destroyed when navigateButtonsVisible is set to false', () => {
    let model = helper.createModelFixture(3, 2, true);
    model.nodes[0].detailForm = formHelper.createFormWithOneField();
    model.nodes[0].detailFormVisible = true;
    model.nodes[1].detailForm = formHelper.createFormWithOneField();
    model.nodes[1].detailFormVisible = true;
    let outline = helper.createOutline(model);
    let staticMenus = outline.nodes[0].detailForm.rootGroupBox.staticMenus;
    expect(staticMenus[0] instanceof NavigateUpButton).toBe(true);
    expect(staticMenus[1] instanceof NavigateDownButton).toBe(true);

    outline.setNavigateButtonsVisible(false);
    let newStaticMenus = outline.nodes[0].detailForm.rootGroupBox.staticMenus;
    expect(newStaticMenus.length).toBe(0);
    expect(staticMenus[0].destroyed).toBe(true);
    expect(staticMenus[1].destroyed).toBe(true);
  });

  describe('aria properties', () => {

    it('has aria-label set', () => {
      let model = helper.createModelFixture(3, 2, true);
      model.nodes[0].detailForm = formHelper.createFormWithOneField();
      model.nodes[0].detailFormVisible = true;
      model.nodes[1].detailForm = formHelper.createFormWithOneField();
      model.nodes[1].detailFormVisible = true;
      let outline = helper.createOutline(model);
      outline.nodes[0].detailForm.render();
      let staticMenus = outline.nodes[0].detailForm.rootGroupBox.staticMenus;
      expect(staticMenus[0] instanceof NavigateUpButton).toBe(true);
      expect(staticMenus[0].$container.attr('aria-label')).toBeTruthy();
      expect(staticMenus[0].$container.attr('aria-labelledby')).toBeFalsy();
      expect(staticMenus[1] instanceof NavigateDownButton).toBe(true);
      expect(staticMenus[1].$container.attr('aria-label')).toBeTruthy();
      expect(staticMenus[1].$container.attr('aria-labelledby')).toBeFalsy();
    });
  });
});
