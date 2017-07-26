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
 * -------------------------------------------------------
 *    Group01   |   Group01   |   Group05   |   Group05
 * -------------------------------------------------------
 *    Group02   |   Group02   |   Group06   |
 * -------------------------------------------------------
 *    Group03   |   Group03   |   Group07   |   Group07
 * -------------------------------------------------------
 *    Group04   |   Group04   |             |
 * -------------------------------------------------------
 * </pre>
 *
 * <h4>Horizontal</h4>
 *
 * <pre>
 * -------------------------------------------------------
 *    Group01   |   Group01   |   Group02   |   Group02
 * -------------------------------------------------------
 *    Group03   |   Group03   |   Group04   |   Group04
 * -------------------------------------------------------
 *    Group06   |   Group05   |   Group06   |
 * -------------------------------------------------------
 *    Group07   |   Group07   |             |
 * -------------------------------------------------------
 * </pre>
 *
 * @author Andreas Hoegger
 * @since 4.0.0 M6 25.02.2014
 */
// see reference implementation org.eclipse.scout.rt.client.ui.form.fields.groupbox.internal.GroupBoxLayout08Test
describe("GroupBoxBodyGrid08", function() {
  var session;

  beforeEach(function() {
    setFixtures(sandbox());
    session = sandboxSession();

    this.fields = [];
    this.groupBox = scout.create('GroupBox', {
      parent: session.desktop,
      gridColumnCount: 4
    });
    this.fields.push(scout.create('GroupBox', {
      parent: this.groupBox,
      label: "Field 01",
      gridDataHints: new scout.GridData({
        w: 2
      })
    }));
    this.fields[this.fields.length - 1].fields = [scout.create('TableField', {
      parent: this.fields[this.fields.length - 1]
    })];
    this.fields.push(scout.create('GroupBox', {
      parent: this.groupBox,
      label: "Field 02",
      gridDataHints: new scout.GridData({
        w: 2
      })
    }));
    this.fields[this.fields.length - 1].fields = [scout.create('TableField', {
      parent: this.fields[this.fields.length - 1]
    })];
    this.fields.push(scout.create('GroupBox', {
      parent: this.groupBox,
      label: "Field 03",
      gridDataHints: new scout.GridData({
        w: 2
      })
    }));
    this.fields[this.fields.length - 1].fields = [scout.create('TableField', {
      parent: this.fields[this.fields.length - 1]
    })];
    this.fields.push(scout.create('GroupBox', {
      parent: this.groupBox,
      label: "Field 04",
      gridDataHints: new scout.GridData({
        w: 2
      })
    }));
    this.fields[this.fields.length - 1].fields = [scout.create('TableField', {
      parent: this.fields[this.fields.length - 1]
    })];
    this.fields.push(scout.create('GroupBox', {
      parent: this.groupBox,
      label: "Field 05",
      gridDataHints: new scout.GridData({
        w: 2
      })
    }));
    this.fields[this.fields.length - 1].fields = [scout.create('TableField', {
      parent: this.fields[this.fields.length - 1]
    })];
    this.fields.push(scout.create('GroupBox', {
      parent: this.groupBox,
      label: "Field 06",
      gridDataHints: new scout.GridData({
        w: 1
      })
    }));
    this.fields[this.fields.length - 1].fields = [scout.create('TableField', {
      parent: this.fields[this.fields.length - 1]
    })];
    this.fields.push(scout.create('GroupBox', {
      parent: this.groupBox,
      label: "Field 07",
      gridDataHints: new scout.GridData({
        w: 2
      })
    }));
    this.fields[this.fields.length - 1].fields = [scout.create('TableField', {
      parent: this.fields[this.fields.length - 1]
    })];
    this.fields.push(scout.create('Button', {
      parent: this.groupBox,
      label: "Close",
      systemType: scout.Button.SystemType.CLOSE
    }));
    this.groupBox.setProperty('fields', this.fields);
    this.groupBox.render();
  });

  describe('group box layout 08', function() {
    it('test horizontal layout', function() {
      var grid = new scout.HorizontalGroupBoxBodyGrid();
      grid.validate(this.groupBox);

      // group box
      expect(grid.getGridRowCount()).toEqual(4);
      expect(grid.getGridColumnCount()).toEqual(4);

      // field01
      scout.GroupBoxSpecHelper.assertGridData(0, 0, 2, 1, this.fields[0].gridData);

      // field02
      scout.GroupBoxSpecHelper.assertGridData(2, 0, 2, 1, this.fields[1].gridData);

      // field03
      scout.GroupBoxSpecHelper.assertGridData(0, 1, 2, 1, this.fields[2].gridData);

      // field04
      scout.GroupBoxSpecHelper.assertGridData(2, 1, 2, 1, this.fields[3].gridData);

      // field05
      scout.GroupBoxSpecHelper.assertGridData(0, 2, 2, 1, this.fields[4].gridData);

      // field06
      scout.GroupBoxSpecHelper.assertGridData(2, 2, 1, 1, this.fields[5].gridData);

      // field07
      scout.GroupBoxSpecHelper.assertGridData(0, 3, 2, 1, this.fields[6].gridData);
    });

    it('test vertical smart layout', function() {
      var grid = new scout.VerticalSmartGroupBoxBodyGrid();
      grid.validate(this.groupBox);

      // group box
      expect(grid.getGridRowCount()).toEqual(4);
      expect(grid.getGridColumnCount()).toEqual(4);

      // field01
      scout.GroupBoxSpecHelper.assertGridData(0, 0, 2, 1, this.fields[0].gridData);

      // field02
      scout.GroupBoxSpecHelper.assertGridData(0, 1, 2, 1, this.fields[1].gridData);

      // field03
      scout.GroupBoxSpecHelper.assertGridData(0, 2, 2, 1, this.fields[2].gridData);

      // field04
      scout.GroupBoxSpecHelper.assertGridData(0, 3, 2, 1, this.fields[3].gridData);

      // field05
      scout.GroupBoxSpecHelper.assertGridData(2, 0, 2, 1, this.fields[4].gridData);

      // field06
      scout.GroupBoxSpecHelper.assertGridData(2, 1, 1, 1, this.fields[5].gridData);

      // field07
      scout.GroupBoxSpecHelper.assertGridData(2, 2, 2, 1, this.fields[6].gridData);
    });
  });

});
