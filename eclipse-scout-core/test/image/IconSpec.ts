/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {Icon, icons, scout} from '../../src/index';

describe('Icon', () => {

  let session: SandboxSession;

  beforeEach(() => {
    setFixtures(sandbox());
    session = sandboxSession();
  });

  describe('render', () => {

    it('creates a span if it is a font icon', () => {
      let icon = scout.create(Icon, {
        parent: session.desktop,
        iconDesc: icons.INFO
      });
      icon.render();
      expect(icon.$container[0].tagName).toBe('SPAN');
    });

    it('creates an img if it is an image icon', () => {
      let icon = scout.create(Icon, {
        parent: session.desktop,
        iconDesc: 'icon/image.png'
      });
      icon.render();
      expect(icon.$container[0].tagName).toBe('IMG');
    });

  });

  describe('setIconDesc', () => {

    it('accepts a string representing the iconId', () => {
      let icon = scout.create(Icon, {
        parent: session.desktop,
        iconDesc: 'icon/image.png'
      });
      expect(icon.iconDesc.iconUrl).toBe('icon/image.png');

      icon.setIconDesc('icon/image2.png');
      expect(icon.iconDesc.iconUrl).toBe('icon/image2.png');
    });

    it('accepts a scout.IconDesc', () => {
      let icon = scout.create(Icon, {
        parent: session.desktop,
        iconDesc: icons.parseIconId('icon/image.png')
      });
      expect(icon.iconDesc.iconUrl).toBe('icon/image.png');

      icon.setIconDesc(icons.parseIconId('icon/image2.png'));
      expect(icon.iconDesc.iconUrl).toBe('icon/image2.png');
    });

  });

  describe('aria properties', () => {

    it('has aria role img and empty aria-label if it is a font icon', () => {
      let icon = scout.create(Icon, {
        parent: session.desktop,
        iconDesc: icons.INFO
      });
      icon.render();
      expect(icon.$container[0]).toHaveAttr('aria-hidden', 'true');
    });

    it('has empty alt attribute if html img element', () => {
      let icon = scout.create(Icon, {
        parent: session.desktop,
        iconDesc: 'icon/image.png'
      });
      icon.render();
      expect(icon.$container[0]).toHaveAttr('alt', '');
    });
  });
});
