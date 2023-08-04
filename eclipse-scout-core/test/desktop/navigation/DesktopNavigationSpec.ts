/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {Desktop, scout, Tree, ViewButton} from '../../../src/index';
import {OutlineSpecHelper} from '../../../src/testing/index';

describe('DesktopNavigation', () => {
  let session: SandboxSession, desktop: Desktop, outlineHelper: OutlineSpecHelper;

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

  describe('aria properties', () => {

    it('has aria role navigation', () => {
      desktop.render(session.$entryPoint);
      expect(desktop.navigation.$container).toHaveAttr('role', 'navigation');
    });

    it('has aria-label set', () => {
      desktop.render(session.$entryPoint);
      expect(desktop.navigation.$container.attr('aria-label')).toBeTruthy();
      expect(desktop.navigation.$container.attr('aria-labelledby')).toBeFalsy();
    });

    it('has a non empty status container that represent current outline state', () => {
      _setupOutline(outlineHelper, desktop, true);
      expect(desktop.navigation.$screenReaderStatus).toBeTruthy();
      expect(desktop.navigation.$screenReaderStatus).toHaveAttr('role', 'status');
      expect(desktop.navigation.$screenReaderStatus).toHaveClass('sr-only');
      expect(desktop.navigation.$screenReaderStatus.children('.sr-outline-path').length).toBe(1);
      expect(desktop.navigation.$screenReaderStatus.children('.sr-outline-path').eq(0)).not.toBeEmpty();
    });

    it('has a collapse handlers with role button', () => {
      // test 2 step collapse/expand
      _setupOutline(outlineHelper, desktop, true);
      // both should have a label
      expect(desktop.navigation.handle.$left.attr('aria-label')).toBeTruthy();
      expect(desktop.navigation.handle.$right.attr('aria-label')).toBeTruthy();
      // only left should be visible at first
      expect(desktop.navigation.handle.$left.attr('aria-hidden')).toBeFalsy();
      expect(desktop.navigation.handle.$right).toHaveAttr('aria-hidden', 'true');
      // shrink navigation once so both handlers are visible
      desktop.shrinkNavigation();
      expect(desktop.navigation.handle.$left.attr('aria-hidden')).toBeFalsy();
      expect(desktop.navigation.handle.$right.attr('aria-hidden')).toBeFalsy();
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
