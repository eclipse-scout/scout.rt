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
// see reference implementation org.eclipse.scout.rt.client.ui.form.fields.groupbox.internal.GroupBoxLayout01Test
describe("GroupBoxBodyGrid01", function() {
  var session;

  beforeEach(function() {
    setFixtures(sandbox());
    session = sandboxSession();

    this.fields = [];
    this.groupBox = scout.create('GroupBox', {
      parent: session.desktop,
      mainBox: false
    });
    this.fields.push(scout.create('StringField', {
      parent: this.groupBox,
      label: "Field 01",
      gridDataHints: new scout.GridData({})
    }));
    this.fields.push(scout.create('StringField', {
      parent: this.groupBox,
      label: "Field 02",
      gridDataHints: new scout.GridData({
        h: 2
      })
    }));
    this.fields.push(scout.create('StringField', {
      parent: this.groupBox,
      label: "Field 03",
      gridDataHints: new scout.GridData({
        h: 2
      })
    }));
    this.fields.push(scout.create('StringField', {
      parent: this.groupBox,
      label: "Field 04",
      gridDataHints: new scout.GridData({})
    }));
    this.groupBox.setProperty('fields', this.fields);
    this.groupBox.render(session.$entryPoint);
  });

  describe('group box layout 01', function() {
    it('test horizontal layout', function() {
      var grid = new scout.HorizontalGroupBoxBodyGrid();
      grid.validate(this.groupBox);

      // group box
      expect(grid.getGridRowCount()).toEqual(3);
      expect(grid.getGridColumnCount()).toEqual(2);

      // field01
      scout.GroupBoxSpecHelper.assertGridData(0, 0, 1, 1, this.fields[0].gridData);

      // field02
      scout.GroupBoxSpecHelper.assertGridData(1, 0, 1, 2, this.fields[1].gridData);

      // field03
      scout.GroupBoxSpecHelper.assertGridData(0, 1, 1, 2, this.fields[2].gridData);

      // field04
      scout.GroupBoxSpecHelper.assertGridData(1, 2, 1, 1, this.fields[3].gridData);
    });


    it('test vertical smart layout', function() {
      var grid = new scout.VerticalSmartGroupBoxBodyGrid();
      grid.validate(this.groupBox);

      // group box
      expect(grid.getGridRowCount()).toEqual(3);
      expect(grid.getGridColumnCount()).toEqual(2);

      // field01
      scout.GroupBoxSpecHelper.assertGridData(0, 0, 1, 1, this.fields[0].gridData);

      // field02
      scout.GroupBoxSpecHelper.assertGridData(0, 1, 1, 2, this.fields[1].gridData);

      // field03
      scout.GroupBoxSpecHelper.assertGridData(1, 0, 1, 2, this.fields[2].gridData);

      // field04
      scout.GroupBoxSpecHelper.assertGridData(1, 2, 1, 1, this.fields[3].gridData);
    });
  });

});
