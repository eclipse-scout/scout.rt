/*
 * Copyright (c) 2010, 2026 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {Accordion, AccordionField, Group, scout} from '../../src';

describe('Group', () => {
  let session: SandboxSession;

  beforeEach(() => {
    setFixtures(sandbox());
    session = sandboxSession();
  });

  describe('header', () => {
    it('prevents the initial focus', () => {
      const group = scout.create(Group, {parent: session.desktop});
      group.render();
      session.focusManager.validateFocus();
      expect(group.$header).not.toBeFocused();
    });

    it('prevents the initial focus even if used in an accordion field', () => {
      const accordionField = scout.create(AccordionField, {
        parent: session.desktop,
        preventInitialFocus: false, // Explicitly set to false, so class will be removed from focusable element (Scout Classic default)
        accordion: {
          objectType: Accordion,
          groups: [{
            objectType: Group
          }]
        }
      });
      accordionField.render();
      session.focusManager.validateFocus();
      expect(accordionField.accordion.groups[0].$header).not.toBeFocused();
    });
  });
});
