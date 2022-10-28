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
import {Button, GridData, GroupBox, GroupBoxGridConfig, HorizontalGrid, scout, StringField, VerticalSmartGrid} from '../../../src/index';
import {GroupBoxSpecHelper} from '../../../src/testing/index';

/**
 * Reference impl. javadoc:
 *
 * <h4>Vertical</h4>
 *
 * <pre>
 * -----------------------------------------
 *    Field01   |   Field01   |   Field02
 * -----------------------------------------
 *    Field01   |   Field01   |   Field03
 * -----------------------------------------
 * </pre>
 *
 * <h4>Horizontal</h4>
 *
 * <pre>
 * -----------------------------------------
 *    Field01   |   Field01   |   Field02
 * -----------------------------------------
 *    Field01   |   Field01   |   Field03
 * -----------------------------------------
 * </pre>
 *
 * @author Andreas Hoegger
 * @since 4.0.0 M6 25.02.2014
 */
// see reference implementation org.eclipse.scout.rt.client.ui.form.fields.groupbox.internal.GroupBoxLayout05Test
describe('AbstractGrid05', () => {
  let session: SandboxSession;

  beforeEach(function() {
    setFixtures(sandbox());
    session = sandboxSession();

    this.fields = [];
    this.groupBox = scout.create(GroupBox, {
      parent: session.desktop,
      gridColumnCount: 3
    });
    this.fields.push(scout.create(StringField, {
      parent: this.groupBox,
      label: 'Field 01',
      gridDataHints: new GridData({
        h: 2,
        w: 2
      })
    }));
    this.fields.push(scout.create(StringField, {
      parent: this.groupBox,
      label: 'Field 02'
    }));
    this.fields.push(scout.create(StringField, {
      parent: this.groupBox,
      label: 'Field 03',
      gridDataHints: new GridData()
    }));
    this.fields.push(scout.create(Button, {
      parent: this.groupBox,
      label: 'Close',
      systemType: Button.SystemType.CLOSE
    }));
    this.groupBox.setProperty('fields', this.fields);
    this.groupBox.render();
  });

  describe('group box layout 05', () => {
    it('test horizontal layout', function() {
      let grid = new HorizontalGrid();
      grid.setGridConfig(new GroupBoxGridConfig());
      grid.validate(this.groupBox);

      // group box
      expect(grid.getGridRowCount()).toEqual(2);
      expect(grid.getGridColumnCount()).toEqual(3);

      // field01
      GroupBoxSpecHelper.assertGridData(0, 0, 2, 2, this.fields[0].gridData);

      // field02
      GroupBoxSpecHelper.assertGridData(2, 0, 1, 1, this.fields[1].gridData);

      // field03
      GroupBoxSpecHelper.assertGridData(2, 1, 1, 1, this.fields[2].gridData);
    });

    it('test vertical smart layout', function() {
      let grid = new VerticalSmartGrid();
      grid.setGridConfig(new GroupBoxGridConfig());
      grid.validate(this.groupBox);

      // group box
      expect(grid.getGridRowCount()).toEqual(2);
      expect(grid.getGridColumnCount()).toEqual(3);

      // field01
      GroupBoxSpecHelper.assertGridData(0, 0, 2, 2, this.fields[0].gridData);

      // field02
      GroupBoxSpecHelper.assertGridData(2, 0, 1, 1, this.fields[1].gridData);

      // field03
      GroupBoxSpecHelper.assertGridData(2, 1, 1, 1, this.fields[2].gridData);
    });
  });

});
