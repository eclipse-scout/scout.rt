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
// see reference implementation org.eclipse.scout.rt.client.ui.form.fields.groupbox.internal.GroupBoxLayout08Test
describe("GroupBoxBodyGrid08", function() {
  var session;

  beforeEach(function() {
    setFixtures(sandbox());
    session = sandboxSession();

    this.fields = [];
    this.groupBox = scout.create('GroupBox', {
      parent: session.desktop,
      gridColumnCountHint: 4,
      mainBox: false
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
    this.groupBox.render(session.$entryPoint);
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
  });

});
