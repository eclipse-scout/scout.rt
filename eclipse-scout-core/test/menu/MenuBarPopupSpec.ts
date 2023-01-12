/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {MenuSpecHelper} from '../../src/testing/index';

describe('MenuBarPopup', () => {

  let helper: MenuSpecHelper, session: SandboxSession;

  beforeEach(() => {
    setFixtures(sandbox());
    session = sandboxSession();
    helper = new MenuSpecHelper(session);
  });

  afterEach(() => {
    removePopups(session);
  });

  it('is opened on doAction if the menu has child actions', () => {
    let childMenu = helper.createMenu({text: 'child menu'});
    let menu = helper.createMenu({
      text: 'the menu',
      childActions: [childMenu]
    });
    menu.render();
    menu.doAction();
    expect(menu.popup).toBeDefined();
    expect(menu.popup.rendered).toBe(true);
  });
});
