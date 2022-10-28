/*
 * Copyright (c) 2010-2022 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
import {DateField, GroupBox, GroupBoxGridConfig, HorizontalGrid, Menu, scout, SmartField, StringField} from '../../../../src/index';
import {CloneSpecHelper, FormSpecHelper} from '../../../../src/testing/index';

describe('GroupBox', () => {
  let session: SandboxSession;
  let helper: FormSpecHelper;
  let cloneHelper: CloneSpecHelper;

  beforeEach(() => {
    setFixtures(sandbox());
    session = sandboxSession();
    helper = new FormSpecHelper(session);
    cloneHelper = new CloneSpecHelper();
  });

  describe('clone', () => {

    it('considers the clone properties and deep clones fields and menus', () => {
      let groupBox = scout.create(GroupBox, {
        parent: session.desktop,
        id: 'gb01',
        subLabel: 'abc',
        gridColumnCount: 3,
        logicalGrid: 'HorizontalGrid',
        fields: [{
          objectType: StringField
        }, {
          objectType: SmartField,
          label: 'a label'
        }, {
          objectType: DateField
        }],
        menus: [{
          objectType: Menu
        }]
      });
      let clone = groupBox.clone({
        parent: groupBox.parent
      });

      cloneHelper.validateClone(groupBox, clone);
      expect(clone.fields.length).toBe(3);
      expect(clone.menus.length).toBe(1);
      expect(clone.cloneOf).toBe(groupBox);
      expect(clone.gridColumnCount).toBe(3);
      expect(clone.fields[0].cloneOf).toBe(groupBox.fields[0]);
      expect(clone.fields[1].cloneOf).toBe(groupBox.fields[1]);
      expect(clone.fields[1].label).toBe('a label');
      expect(clone.menus[0].cloneOf).toBe(groupBox.menus[0]);

      // Assert that logical grid is a new instance
      expect(clone.logicalGrid).not.toBe(groupBox.logicalGrid);
      expect(clone.logicalGrid instanceof HorizontalGrid).toBe(true);
      expect(clone.logicalGrid.gridConfig instanceof GroupBoxGridConfig).toBe(true);
    });

    it('does not render the cloned box', () => {
      let clone,
        groupBox = scout.create(GroupBox, {
          parent: session.desktop,
          id: 'gb01',
          subLabel: 'abc',
          gridColumnCount: 2,
          fields: [{
            objectType: StringField
          }, {
            objectType: SmartField
          }, {
            objectType: DateField
          }],
          menus: [{
            objectType: Menu
          }]
        });
      groupBox.render($('#sandbox'));
      clone = groupBox.clone({
        parent: groupBox.parent
      });

      expect(groupBox.rendered).toBe(true);
      expect(clone.rendered).toBe(false);

      cloneHelper.validateClone(groupBox, clone);
      expect(clone.controls.length).toBe(3);
      expect(clone.menus.length).toBe(1);
    });
  });

});
