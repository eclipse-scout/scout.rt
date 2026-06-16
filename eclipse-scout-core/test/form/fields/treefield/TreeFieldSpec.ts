/*
 * Copyright (c) 2010, 2026 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {FormField, scout, Tree, TreeField} from '../../../../src';

describe('TreeField', () => {
  let session: SandboxSession;

  beforeEach(() => {
    setFixtures(sandbox());
    session = sandboxSession();
  });

  describe('aria-properties', () => {
    it('has aria-labelledby set to link label with field', () => {
      let treeField = scout.create(TreeField, {
        parent: session.desktop,
        tree: {
          objectType: Tree
        },
        label: 'hello'
      });
      treeField.render();
      expect(treeField.tree.$data).toHaveAttr('aria-labelledby', treeField.$label.attr('id'));
      expect(treeField.tree.$data.attr('aria-label')).toBeFalsy();

      treeField.setLabel(null);
      expect(treeField.tree.$data.attr('aria-labelledby')).toBeFalsy();
      expect(treeField.tree.$data.attr('aria-label')).toBeFalsy();

      treeField.setLabel('hallo');
      expect(treeField.tree.$data).toHaveAttr('aria-labelledby', treeField.$label.attr('id'));
      expect(treeField.tree.$data.attr('aria-label')).toBeFalsy();

      treeField.setLabelVisible(false);
      expect(treeField.tree.$data).toHaveAttr('aria-labelledby', treeField.$label.attr('id'));
      expect(treeField.tree.$data.attr('aria-label')).toBeFalsy();

      treeField.remove();
      treeField.render();
      expect(treeField.tree.$data).toHaveAttr('aria-labelledby', treeField.$label.attr('id'));
      expect(treeField.tree.$data.attr('aria-label')).toBeFalsy();
    });

    it('has aria-label set if label position is on field', () => {
      let treeField = scout.create(TreeField, {
        parent: session.desktop,
        tree: {
          objectType: Tree
        },
        labelPosition: FormField.LabelPosition.ON_FIELD,
        label: 'hello'
      });
      treeField.render();
      expect(treeField.tree.$data).toHaveAttr('aria-label', 'hello');
      expect(treeField.tree.$data.attr('aria-labelledby')).toBeFalsy();
    });
  });
})
;
