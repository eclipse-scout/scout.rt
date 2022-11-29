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
import {GridData, GroupBox, GroupBoxGridConfig, HorizontalGrid, scout, StringField, VerticalSmartGrid} from '../../../src/index';
import {GroupBoxSpecHelper} from '../../../src/testing/index';

// see reference implementation org.eclipse.scout.rt.client.ui.form.fields.groupbox.internal.GroupBoxLayout01Test
describe('AbstractGrid01', () => {
  let session;

  beforeEach(function() {
    setFixtures(sandbox());
    session = sandboxSession();

    this.fields = [];
    this.groupBox = scout.create(GroupBox, {
      parent: session.desktop
    });
    this.fields.push(scout.create(StringField, {
      parent: this.groupBox,
      label: 'Field 01',
      gridDataHints: new GridData({})
    }));
    this.fields.push(scout.create(StringField, {
      parent: this.groupBox,
      label: 'Field 02',
      gridDataHints: new GridData({
        h: 2
      })
    }));
    this.fields.push(scout.create(StringField, {
      parent: this.groupBox,
      label: 'Field 03',
      gridDataHints: new GridData({
        h: 2
      })
    }));
    this.fields.push(scout.create(StringField, {
      parent: this.groupBox,
      label: 'Field 04',
      gridDataHints: new GridData({})
    }));
    this.groupBox.setProperty('fields', this.fields);
    this.groupBox.render();
  });

  describe('group box layout 01', () => {
    it('test horizontal layout', function() {
      let grid = new HorizontalGrid();
      grid.setGridConfig(new GroupBoxGridConfig());
      grid.validate(this.groupBox);

      // group box
      expect(grid.getGridRowCount()).toEqual(3);
      expect(grid.getGridColumnCount()).toEqual(2);

      // field01
      GroupBoxSpecHelper.assertGridData(0, 0, 1, 1, this.fields[0].gridData);

      // field02
      GroupBoxSpecHelper.assertGridData(1, 0, 1, 2, this.fields[1].gridData);

      // field03
      GroupBoxSpecHelper.assertGridData(0, 1, 1, 2, this.fields[2].gridData);

      // field04
      GroupBoxSpecHelper.assertGridData(1, 2, 1, 1, this.fields[3].gridData);
    });

    it('test vertical smart layout', function() {
      let grid = new VerticalSmartGrid();
      grid.setGridConfig(new GroupBoxGridConfig());
      grid.validate(this.groupBox);

      // group box
      expect(grid.getGridRowCount()).toEqual(3);
      expect(grid.getGridColumnCount()).toEqual(2);

      // field01
      GroupBoxSpecHelper.assertGridData(0, 0, 1, 1, this.fields[0].gridData);

      // field02
      GroupBoxSpecHelper.assertGridData(0, 1, 1, 2, this.fields[1].gridData);

      // field03
      GroupBoxSpecHelper.assertGridData(1, 0, 1, 2, this.fields[2].gridData);

      // field04
      GroupBoxSpecHelper.assertGridData(1, 2, 1, 1, this.fields[3].gridData);
    });
  });

});
