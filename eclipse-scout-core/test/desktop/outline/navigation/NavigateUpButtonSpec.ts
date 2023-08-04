/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {Form, NavigateUpButton, Outline, Page, Table} from '../../../../src/index';

describe('NavigateUpButton', () => {

  let session: SandboxSession, outline: Outline, menu: SpecNavigateUpButton, node = {} as Page;

  class SpecNavigateUpButton extends NavigateUpButton {
    override _toggleDetail(): boolean {
      return super._toggleDetail();
    }

    override _isDetail(): boolean {
      return super._isDetail();
    }

    override _buttonEnabled(): boolean {
      return super._buttonEnabled();
    }

    override _drill() {
      super._drill();
    }
  }

  beforeEach(() => {
    setFixtures(sandbox());
    session = sandboxSession();
    outline = {
      session: session,
      navigateToTop: () => {
        // nop
      }
    } as unknown as Outline;
    let model = createSimpleModel(SpecNavigateUpButton, session);
    model.outline = outline;
    model.node = node;
    menu = new SpecNavigateUpButton();
    menu.init(model);
  });

  it('_toggleDetail is always true', () => {
    expect(menu._toggleDetail()).toBe(true);
  });

  it('_isDetail returns true or false depending on the state of the detail-form and detail-table', () => {
    // false when both detailForm and detailTable are visible
    node.detailForm = new Form();
    node.detailFormVisible = true;
    node.detailFormVisibleByUi = true;
    node.detailTable = new Table();
    node.detailTableVisible = true;
    expect(menu._isDetail()).toBe(false);

    // false when detailForm is absent, even when if detailFormVisible=true
    delete node.detailForm;
    expect(menu._isDetail()).toBe(false);
    node.detailForm = new Form();

    // false when detailTable is absent, even when if detailTableVisible=true
    delete node.detailTable;
    expect(menu._isDetail()).toBe(false);
    node.detailTable = new Table();

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

  describe('_buttonEnabled', () => {

    it('is true when current node has a parent or...', () => {
      node.parentNode = new Page();
      outline.defaultDetailForm = undefined;
      expect(menu._buttonEnabled()).toBe(true);
    });

    it('is true when current node is a top-level node and outline a default detail-form or...', () => {
      node.parentNode = undefined;
      outline.defaultDetailForm = new Form();
      expect(menu._buttonEnabled()).toBe(true);
    });

    it('is false otherwise', () => {
      node.parentNode = undefined;
      outline.defaultDetailForm = undefined;
      expect(menu._buttonEnabled()).toBe(false);
    });

  });

  describe('_drill', () => {

    beforeEach(() => {
      outline.selectNodes = node => {
        // nop
      };
      outline.collapseNode = node => {
        // nop
      };
      outline.collapseAll = () => {
        // nop
      };
    });

    it('drills up to parent node, sets the selection on the tree', () => {
      node.parentNode = new Page();
      spyOn(outline, 'selectNodes');
      spyOn(outline, 'collapseNode');
      menu._drill();
      expect(outline.navigateUpInProgress).toBe(true);
      expect(outline.selectNodes).toHaveBeenCalledWith(node.parentNode);
      expect(outline.collapseNode).toHaveBeenCalledWith(node.parentNode, {collapseChildNodes: true});
    });

    it('shows default detail-form or outline overview', () => {
      node.parentNode = undefined;
      spyOn(outline, 'navigateToTop');
      menu._drill();
      expect(outline.navigateToTop).toHaveBeenCalled();
    });

  });
});
