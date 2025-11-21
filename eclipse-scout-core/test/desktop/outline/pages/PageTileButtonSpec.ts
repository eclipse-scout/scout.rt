/*
 * Copyright (c) 2010, 2025 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {icons, Outline, Page, PageTileButton, scout} from '../../../../src';
import {OutlineSpecHelper} from '../../../../src/testing';

describe('PageTileButton', () => {

  let session: SandboxSession;
  let helper: OutlineSpecHelper;
  let outline: Outline;
  let page: Page;

  beforeEach(() => {
    setFixtures(sandbox());
    session = sandboxSession();
    helper = new OutlineSpecHelper(session);
    outline = helper.createOutline(helper.createModelFixture(1));
    page = outline.nodes[0];
  });

  describe('overview properties of page', () => {

    it('are used if set', () => {
      page.setText('Foo');
      page.setIconId(icons.SLIPPERY);
      page.setHtmlEnabled(false);

      const pageTileButton = scout.create(PageTileButton, {
        parent: outline,
        outline,
        page
      });

      // no overview properties set -> use original properties
      expect(pageTileButton.label).toBe('Foo');
      expect(pageTileButton.iconId).toBe(icons.SLIPPERY);
      expect(pageTileButton.labelHtmlEnabled).toBeFalse();

      // overview properties set
      page.setOverviewText('Bar');
      page.setOverviewIconId(icons.CLOCK);
      page.setOverviewHtmlEnabled(true);
      pageTileButton.notifyPageChanged();

      expect(pageTileButton.label).toBe('Bar');
      expect(pageTileButton.iconId).toBe(icons.CLOCK);
      expect(pageTileButton.labelHtmlEnabled).toBeTrue();

      // overview properties set to null -> overwrites original values
      page.setOverviewText(null);
      page.setOverviewIconId(null);
      page.setOverviewHtmlEnabled(null);
      pageTileButton.notifyPageChanged();

      expect(pageTileButton.label).toBeNull();
      expect(pageTileButton.iconId).toBeNull();
      expect(pageTileButton.labelHtmlEnabled).toBeFalse();

      // overview properties reset to undefined -> use original properties
      page.setOverviewText(undefined);
      page.setOverviewIconId(undefined);
      page.setOverviewHtmlEnabled(undefined);
      pageTileButton.notifyPageChanged();

      expect(pageTileButton.label).toBe('Foo');
      expect(pageTileButton.iconId).toBe(icons.SLIPPERY);
      expect(pageTileButton.labelHtmlEnabled).toBeFalse();
    });
  });
});
