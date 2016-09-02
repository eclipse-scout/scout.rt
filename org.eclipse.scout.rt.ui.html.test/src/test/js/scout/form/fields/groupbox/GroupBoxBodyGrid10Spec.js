/*******************************************************************************
 * Copyright (c) 2014-2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
/**
 * Reference implementation javadoc:
 *
 * <h4>Vertical</h4>
 *
 * <pre>
 * -----------------------------------------
 *    Field01   |   Field02   |   Field05
 * -----------------------------------------
 *    Field03   |   Field03   |
 * -----------------------------------------
 *    Field04   |   Field06   |   Field06
 * -----------------------------------------
 * </pre>
 *
 * <h4>Horizontal</h4>
 *
 * <pre>
 * -----------------------------------------
 *    Field01   |   Field02   |
 * -----------------------------------------
 *    Field03   |   Field03   |   Field04
 * -----------------------------------------
 *    Field05   |   Field06   |   Field06
 * -----------------------------------------
 * </pre>
 *
 * @author Andreas Hoegger
 * @since 4.0.0 M6 25.02.2014
 */
// see reference implementation org.eclipse.scout.rt.client.ui.form.fields.groupbox.internal.GroupBoxLayout10Test
describe("GroupBoxBodyGrid10", function() {
  var session;

  beforeEach(function() {
    setFixtures(sandbox());
    session = sandboxSession();

    this.fields = [];
    this.groupBox = scout.create('GroupBox', {
      parent: session.desktop,
      gridColumnCount: 3,
      mainBox: false
    });
    this.fields.push(scout.create('StringField', {
      parent: this.groupBox,
      label: "Field 01",
      gridDataHints: new scout.GridData({
        w: 1
      })
    }));
    this.fields.push(scout.create('StringField', {
      parent: this.groupBox,
      label: "Field 02"
    }));
    this.fields.push(scout.create('StringField', {
      parent: this.groupBox,
      label: "Field 03",
      gridDataHints: new scout.GridData({
        w: 2
      })
    }));
    this.fields.push(scout.create('StringField', {
      parent: this.groupBox,
      label: "Field 04",
      gridDataHints: new scout.GridData({})
    }));
    this.fields.push(scout.create('StringField', {
      parent: this.groupBox,
      label: "Field 05"
    }));
    this.fields.push(scout.create('StringField', {
      parent: this.groupBox,
      label: "Field 06",
      gridDataHints: new scout.GridData({
        w: 2
      })
    }));
    this.fields.push(scout.create('Button', {
      parent: this.groupBox,
      label: "Toggle",
      displayStyle: scout.Button.DisplayStyle.TOGGLE
    }));
    this.fields.push(scout.create('Button', {
      parent: this.groupBox,
      label: "Close",
      systemType: scout.Button.SystemType.CLOSE
    }));
    this.groupBox.setProperty('fields', this.fields);
    this.groupBox.render(session.$entryPoint);
  });

  describe('group box layout 10', function() {
    it('test horizontal layout', function() {
      var grid = new scout.HorizontalGroupBoxBodyGrid();
      grid.validate(this.groupBox);

      // group box
      expect(grid.getGridRowCount()).toEqual(3);
      expect(grid.getGridColumnCount()).toEqual(3);

      // field01
      scout.GroupBoxSpecHelper.assertGridData(0, 0, 1, 1, this.fields[0].gridData);

      // field02
      scout.GroupBoxSpecHelper.assertGridData(1, 0, 1, 1, this.fields[1].gridData);

      // field03
      scout.GroupBoxSpecHelper.assertGridData(0, 1, 2, 1, this.fields[2].gridData);

      // field04
      scout.GroupBoxSpecHelper.assertGridData(2, 1, 1, 1, this.fields[3].gridData);

      // field05
      scout.GroupBoxSpecHelper.assertGridData(0, 2, 1, 1, this.fields[4].gridData);

      // field06
      scout.GroupBoxSpecHelper.assertGridData(1, 2, 2, 1, this.fields[5].gridData);
    });

    it('test vertical smart layout', function() {
      var grid = new scout.VerticalSmartGroupBoxBodyGrid();
      grid.validate(this.groupBox);

      // group box
      expect(grid.getGridRowCount()).toEqual(3);
      expect(grid.getGridColumnCount()).toEqual(3);

      // field01
      scout.GroupBoxSpecHelper.assertGridData(0, 0, 1, 1, this.fields[0].gridData);

      // field02
      scout.GroupBoxSpecHelper.assertGridData(1, 0, 1, 1, this.fields[1].gridData);

      // field03
      scout.GroupBoxSpecHelper.assertGridData(0, 1, 2, 1, this.fields[2].gridData);

      // field04
      scout.GroupBoxSpecHelper.assertGridData(0, 2, 1, 1, this.fields[3].gridData);

      // field05
      scout.GroupBoxSpecHelper.assertGridData(2, 0, 1, 1, this.fields[4].gridData);

      // field06
      scout.GroupBoxSpecHelper.assertGridData(1, 2, 2, 1, this.fields[5].gridData);
    });
  });

});
