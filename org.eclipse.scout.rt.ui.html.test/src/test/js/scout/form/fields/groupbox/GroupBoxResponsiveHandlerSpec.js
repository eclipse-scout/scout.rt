/*******************************************************************************
 * Copyright (c) 2014-2018 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
describe("GroupBoxResponsiveHandler", function() {
  var session;
  var helper;

  beforeEach(function() {
    setFixtures(sandbox());
    session = sandboxSession();
    helper = new scout.FormSpecHelper(session);
  });

  function createField(model, parent) {
    var field = new scout.GroupBox();
    model.session = session;
    model.parent = parent || session.desktop;
    field.init(model);
    return field;
  }

  function createGroupBox(fields) {
    fields = fields || [{
      objectType: 'StringField',
    }, {
      objectType: 'CheckBoxField',
    }, {
      objectType: 'LabelField',
    }, {
      objectType: 'GroupBox',
      fields: [{
        objectType: 'StringField',
      }]
    }];
    var groupBox = scout.create('GroupBox', {
      parent: session.desktop,
      fields: fields,
      responsive: true
    });
    scout.responsiveManager.registerHandler(groupBox, new scout.GroupBoxResponsiveHandler(groupBox));
    return groupBox;
  }

  var normalWidth = scout.HtmlEnvironment.formColumnWidth * 2 + 10;
  var condensedWidth = scout.HtmlEnvironment.formColumnWidth  + 10;
  var compactWidth = scout.HtmlEnvironment.formColumnWidth - 10;

  function expectNormal(groupBox) {
    expect(groupBox.fields[0].labelPosition).toBe(scout.FormField.LabelPosition.DEFAULT);
    expect(groupBox.fields[1].labelPosition).toBe(scout.FormField.LabelPosition.DEFAULT);
    expect(groupBox.fields[1].labelVisible).toBe(true);
    expect(groupBox.fields[2].labelPosition).toBe(scout.FormField.LabelPosition.DEFAULT);
    expect(groupBox.gridColumnCount).toBe(2);

    var innerGroupBox = groupBox.fields[3];
    if (innerGroupBox.responsive === null || innerGroupBox.responsive === false) {
      expect(innerGroupBox.fields[0].labelPosition).toBe(scout.FormField.LabelPosition.DEFAULT);
      expect(innerGroupBox.gridColumnCount).toBe(2);
    }
  }

  function expectCondensed(groupBox) {
    expect(groupBox.fields[0].labelPosition).toBe(scout.FormField.LabelPosition.TOP);
    expect(groupBox.fields[1].labelPosition).toBe(scout.FormField.LabelPosition.DEFAULT);
    expect(groupBox.fields[1].labelVisible).toBe(false);
    expect(groupBox.fields[2].labelPosition).toBe(scout.FormField.LabelPosition.DEFAULT);
    expect(groupBox.gridColumnCount).toBe(2);

    var innerGroupBox = groupBox.fields[3];
    if (innerGroupBox.responsive === null || innerGroupBox.responsive === true) {
      expect(innerGroupBox.fields[0].labelPosition).toBe(scout.FormField.LabelPosition.TOP);
      expect(innerGroupBox.gridColumnCount).toBe(2);
    }else{
      expect(innerGroupBox.fields[0].labelPosition).toBe(scout.FormField.LabelPosition.DEFAULT);
      expect(innerGroupBox.gridColumnCount).toBe(2);
    }
  }

  function expectCompact(groupBox) {
    expect(groupBox.fields[0].labelPosition).toBe(scout.FormField.LabelPosition.TOP);
    expect(groupBox.fields[0].statusPosition).toBe(scout.FormField.StatusPosition.TOP);
    expect(groupBox.fields[0].statusVisible).toBe(false);
    expect(groupBox.fields[1].labelPosition).toBe(scout.FormField.LabelPosition.DEFAULT);
    expect(groupBox.fields[1].labelVisible).toBe(false);
    expect(groupBox.fields[2].labelPosition).toBe(scout.FormField.LabelPosition.DEFAULT);
    expect(groupBox.gridColumnCount).toBe(1);

    var innerGroupBox = groupBox.fields[3];
    if (innerGroupBox.responsive === null || innerGroupBox.responsive === true) {
      expect(innerGroupBox.fields[0].labelPosition).toBe(scout.FormField.LabelPosition.TOP);
      expect(innerGroupBox.gridColumnCount).toBe(1);
    }
  }

  describe("handleResponsive", function() {
    var groupBox;

    beforeEach(function() {
      groupBox = createGroupBox();
    });

    it("switches to condensed mode if width under threshold", function() {
      // normal state
      groupBox.render($('#sandbox'));
      groupBox.$container.cssWidth(normalWidth);
      groupBox.invalidateLayout();
      groupBox.validateLayout();
      expectNormal(groupBox);

      // condensed state
      groupBox.$container.cssWidth(condensedWidth);
      groupBox.invalidateLayout();
      groupBox.validateLayout();
      expectCondensed(groupBox);

      // reset
      groupBox.$container.cssWidth(normalWidth);
      groupBox.invalidateLayout();
      groupBox.validateLayout();
      scout.responsiveManager.reset(groupBox);
      expectNormal(groupBox);

    });

    it("switches to compact mode if width under threshold", function() {
      // normal state
      groupBox.render($('#sandbox'));
      groupBox.$container.cssWidth(normalWidth);
      groupBox.invalidateLayout();
      groupBox.validateLayout();
      expectNormal(groupBox);

      // compact state
      groupBox.$container.cssWidth(compactWidth);
      groupBox.invalidateLayout();
      groupBox.validateLayout();
      expectCompact(groupBox);

      // back to condensed
      groupBox.$container.cssWidth(condensedWidth);
      groupBox.invalidateLayout();
      groupBox.validateLayout();
      expectCondensed(groupBox);

      // back to normal
      groupBox.$container.cssWidth(normalWidth);
      groupBox.invalidateLayout();
      groupBox.validateLayout();
      expectNormal(groupBox);
    });

    it("does not switch inner group box to condensed mode if inner group box is not responsive", function() {
      groupBox.fields[3].setResponsive(false);

      // normal state
      groupBox.render($('#sandbox'));
      groupBox.$container.cssWidth(normalWidth);
      groupBox.invalidateLayout();
      groupBox.validateLayout();
      expectNormal(groupBox);

      // condensed state
      groupBox.$container.cssWidth(condensedWidth);
      groupBox.invalidateLayout();
      groupBox.validateLayout();
      expectCondensed(groupBox);

      // back to normal
      groupBox.$container.cssWidth(normalWidth);
      groupBox.invalidateLayout();
      groupBox.validateLayout();
      expectNormal(groupBox);
    });

    it("reacts to dynamically inserted field", function() {
      groupBox.fields[3].setResponsive(false);

      // normal state
      groupBox.render($('#sandbox'));
      groupBox.$container.cssWidth(normalWidth);
      groupBox.invalidateLayout();
      groupBox.validateLayout();
      expectNormal(groupBox);

      // condensed state
      groupBox.$container.cssWidth(condensedWidth);
      groupBox.invalidateLayout();
      groupBox.validateLayout();
      expectCondensed(groupBox);

      var dynamicField = scout.create('StringField', {
        parent: session.desktop,
        labelPosition: scout.FormField.LabelPosition.DEFAULT
      });
      groupBox.insertField(dynamicField);
      expect(dynamicField.labelPosition).toBe(scout.FormField.LabelPosition.TOP);

      // back to normal
      groupBox.$container.cssWidth(normalWidth);
      groupBox.invalidateLayout();
      groupBox.validateLayout();
      expectNormal(groupBox);

      expect(dynamicField.labelPosition).toBe(scout.FormField.LabelPosition.DEFAULT);
    });
  });

  describe("setResponsive", function() {
    var groupBox;

    beforeEach(function() {
      groupBox = createGroupBox();
    });

    it("switches the responsive state for the inner group box", function() {
      // normal state
      groupBox.render($('#sandbox'));
      groupBox.$container.cssWidth(normalWidth);
      groupBox.invalidateLayout();
      groupBox.validateLayout();
      expectNormal(groupBox);

      // condensed state
      groupBox.$container.cssWidth(condensedWidth);
      groupBox.invalidateLayout();
      groupBox.validateLayout();
      expectCondensed(groupBox);

      // disable responsiveness for inner group box
      groupBox.fields[3].setResponsive(false);
      expectCondensed(groupBox);

      // back to normal
      groupBox.$container.cssWidth(normalWidth);
      groupBox.invalidateLayout();
      groupBox.validateLayout();
      expectNormal(groupBox);
    });
  });
});
