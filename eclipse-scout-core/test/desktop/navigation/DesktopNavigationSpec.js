/*
 * Copyright (c) 2010-2020 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
import {scout, Tree, ViewButton} from '../../../src/index';
import {OutlineSpecHelper} from '../../../src/testing/index';

describe('DesktopNavigation', () => {
  let session, desktop, outlineHelper;

  beforeEach(() => {
    setFixtures(sandbox());
    session = sandboxSession({
      renderDesktop: false,
      desktop: {
        navigationVisible: true
      }
    });
    desktop = session.desktop;
    outlineHelper = new OutlineSpecHelper(session);
  });

  describe('viewButtonBox', () => {

    it('is visible when there are more than one ViewButtons', () => {
      desktop.viewButtons = [
        scout.create(ViewButton, {
          parent: desktop,
          text: 'Button1',
          displayStyle: 'TAB'
        }),
        scout.create(ViewButton, {
          parent: desktop,
          text: 'Button2',
          displayStyle: 'TAB'
        })
      ];
      desktop.render(session.$entryPoint);
      expect(desktop.navigation.viewButtonBox.visible).toBe(true);
    });

    it('is not visible if there are no view buttons', () => {
      desktop.viewButtons = [];
      desktop.render(session.$entryPoint);
      expect(desktop.navigation.viewButtonBox.visible).toBeFalsy();
    });

    it('is not visible when there is only one ViewButton', () => {
      desktop.viewButtons = [scout.create(ViewButton, {
        parent: desktop,
        displayStyle: 'MENU'
      })];
      desktop.render(session.$entryPoint);
      expect(desktop.navigation.viewButtonBox.visible).toBe(false);
    });

    it('has only one ViewButtonMenu if all buttons are displayType menu', () => {
      desktop.viewButtons = [
        scout.create(ViewButton, {
          parent: desktop,
          text: 'VB1',
          displayStyle: 'MENU'
        }),
        scout.create(ViewButton, {
          parent: desktop,
          text: 'VB2',
          displayStyle: 'MENU'
        })
      ];
      desktop.render(session.$entryPoint);
      expect(desktop.navigation.viewButtonBox.visible).toBe(true);
      expect(desktop.navigation.viewButtonBox.viewMenuTab.visible).toBe(true);
    });
  });

  describe('outline', () => {

    it('collapses and expands in two steps when breadcrumb toggling enabled', () => {
      _setupOutline(outlineHelper, desktop, true);

      expect(desktop.navigationVisible).toBe(true);
      expect(desktop.outline.displayStyle).toBe(Tree.DisplayStyle.DEFAULT);
      expect(desktop.navigation.handle.leftVisible).toBe(true);
      expect(desktop.navigation.handle.rightVisible).toBe(false);

      // collapse to breadcrumb
      desktop.shrinkNavigation();
      expect(desktop.navigationVisible).toBe(true);
      expect(desktop.outline.displayStyle).toBe(Tree.DisplayStyle.BREADCRUMB);
      expect(desktop.navigation.handle.leftVisible).toBe(true);
      expect(desktop.navigation.handle.rightVisible).toBe(true);

      // collapse completely
      desktop.shrinkNavigation();
      expect(desktop.navigationVisible).toBe(false); // complete navigation invisible, handle-visibility not updated

      // enlarge to breadcrumb
      desktop.enlargeNavigation();
      expect(desktop.navigationVisible).toBe(true);
      expect(desktop.outline.displayStyle).toBe(Tree.DisplayStyle.BREADCRUMB);
      expect(desktop.navigation.handle.leftVisible).toBe(true);
      expect(desktop.navigation.handle.rightVisible).toBe(true);

      // enlarge to default
      desktop.enlargeNavigation();
      expect(desktop.navigationVisible).toBe(true);
      expect(desktop.outline.displayStyle).toBe(Tree.DisplayStyle.DEFAULT);
      expect(desktop.navigation.handle.leftVisible).toBe(true);
      expect(desktop.navigation.handle.rightVisible).toBe(false);
    });

    it('collapses and expands in one step when breadcrumb toggling disabled', () => {
      _setupOutline(outlineHelper, desktop, false);

      expect(desktop.navigation.handle.leftVisible).toBe(true);
      expect(desktop.navigation.handle.rightVisible).toBe(false);
      expect(desktop.outline.displayStyle).toBe(Tree.DisplayStyle.DEFAULT);

      // collapse completely
      desktop.shrinkNavigation();
      expect(desktop.navigationVisible).toBe(false); // complete navigation invisible, handle-visibility not updated

      // enlarge to default
      desktop.enlargeNavigation();
      expect(desktop.navigationVisible).toBe(true);
      expect(desktop.outline.displayStyle).toBe(Tree.DisplayStyle.DEFAULT);
      expect(desktop.navigation.handle.leftVisible).toBe(true);
      expect(desktop.navigation.handle.rightVisible).toBe(false);
    });

  });

});

function _setupOutline(outlineHelper, desktop, toggleBreadcrumbStyleEnabled) {
  let model = outlineHelper.createModelFixture(3, 2);
  let outline = outlineHelper.createOutline(model);
  outline.toggleBreadcrumbStyleEnabled = toggleBreadcrumbStyleEnabled;
  outline.displayStyle = Tree.DisplayStyle.DEFAULT;
  desktop.setOutline(outline);
  desktop.render(desktop.session.$entryPoint);
  return outline;
}
