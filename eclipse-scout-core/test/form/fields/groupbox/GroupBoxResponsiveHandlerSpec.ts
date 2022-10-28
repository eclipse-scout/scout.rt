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
import {CheckBoxField, FormField, FormFieldModel, GroupBox, HtmlEnvironment, LabelField, RadioButton, RadioButtonGroup, RefModel, ResponsiveManager, scout, StringField} from '../../../../src/index';
import {FormSpecHelper} from '../../../../src/testing/index';

describe('GroupBoxResponsiveHandler', () => {
  let session: SandboxSession;
  let helper: FormSpecHelper;

  beforeEach(() => {
    setFixtures(sandbox());
    session = sandboxSession();
    helper = new FormSpecHelper(session);
  });

  function createGroupBox(fields?: RefModel<FormFieldModel>[]): GroupBox {
    fields = fields || [{
      objectType: StringField
    }, {
      objectType: CheckBoxField
    }, {
      objectType: LabelField
    }, {
      objectType: RadioButtonGroup,
      fields: [{
        objectType: RadioButton
      }, {
        objectType: RadioButton
      }]
    }, {
      objectType: GroupBox,
      fields: [{
        objectType: StringField
      }]
    }];
    return scout.create(GroupBox, {
      parent: session.desktop,
      fields: fields,
      responsive: true
    });
  }

  let normalWidth = HtmlEnvironment.get().formColumnWidth * 2 + 10;
  let condensedWidth = HtmlEnvironment.get().formColumnWidth + 10;
  let compactWidth = HtmlEnvironment.get().formColumnWidth - 10;

  function expectNormal(groupBox) {
    expect(groupBox.fields[0].labelPosition).toBe(FormField.LabelPosition.DEFAULT);
    expect(groupBox.fields[1].labelPosition).toBe(FormField.LabelPosition.DEFAULT);
    expect(groupBox.fields[1].labelVisible).toBe(true);
    expect(groupBox.fields[2].labelPosition).toBe(FormField.LabelPosition.DEFAULT);
    expect(groupBox.fields[3].gridDataHints.useUiHeight).toBe(false);
    expect(groupBox.gridColumnCount).toBe(2);

    let innerGroupBox = groupBox.fields[4];
    if (innerGroupBox.responsive === null || innerGroupBox.responsive === false) {
      expect(innerGroupBox.fields[0].labelPosition).toBe(FormField.LabelPosition.DEFAULT);
      expect(innerGroupBox.gridColumnCount).toBe(2);
    }
  }

  function expectCondensed(groupBox) {
    expect(groupBox.fields[0].labelPosition).toBe(FormField.LabelPosition.TOP);
    expect(groupBox.fields[1].labelPosition).toBe(FormField.LabelPosition.DEFAULT);
    expect(groupBox.fields[1].labelVisible).toBe(false);
    expect(groupBox.fields[2].labelPosition).toBe(FormField.LabelPosition.DEFAULT);
    expect(groupBox.fields[3].gridDataHints.useUiHeight).toBe(true);
    expect(groupBox.gridColumnCount).toBe(2);

    let innerGroupBox = groupBox.fields[4];
    if (innerGroupBox.responsive === null || innerGroupBox.responsive === true) {
      expect(innerGroupBox.fields[0].labelPosition).toBe(FormField.LabelPosition.TOP);
      expect(innerGroupBox.gridColumnCount).toBe(2);
    } else {
      expect(innerGroupBox.fields[0].labelPosition).toBe(FormField.LabelPosition.DEFAULT);
      expect(innerGroupBox.gridColumnCount).toBe(2);
    }
  }

  function expectCompact(groupBox) {
    expect(groupBox.fields[0].labelPosition).toBe(FormField.LabelPosition.TOP);
    expect(groupBox.fields[0].statusPosition).toBe(FormField.StatusPosition.TOP);
    expect(groupBox.fields[0].statusVisible).toBe(false);
    expect(groupBox.fields[1].labelPosition).toBe(FormField.LabelPosition.DEFAULT);
    expect(groupBox.fields[1].labelVisible).toBe(false);
    expect(groupBox.fields[2].labelPosition).toBe(FormField.LabelPosition.DEFAULT);
    expect(groupBox.fields[3].gridDataHints.useUiHeight).toBe(true);
    expect(groupBox.gridColumnCount).toBe(1);

    let innerGroupBox = groupBox.fields[4];
    if (innerGroupBox.responsive === null || innerGroupBox.responsive === true) {
      expect(innerGroupBox.fields[0].labelPosition).toBe(FormField.LabelPosition.TOP);
      expect(innerGroupBox.gridColumnCount).toBe(1);
    }
  }

  describe('handleResponsive', () => {
    let groupBox;

    beforeEach(() => {
      groupBox = createGroupBox();
    });

    it('switches to condensed mode if width under threshold', () => {
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
      ResponsiveManager.get().reset(groupBox);
      expectNormal(groupBox);

    });

    it('switches to compact mode if width under threshold', () => {
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

    it('does not switch inner group box to condensed mode if inner group box is not responsive', () => {
      groupBox.fields[4].setResponsive(false);

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

    it('reacts to dynamically inserted field', () => {
      groupBox.fields[4].setResponsive(false);

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

      let dynamicField = scout.create(StringField, {
        parent: session.desktop,
        labelPosition: FormField.LabelPosition.DEFAULT
      });
      groupBox.insertField(dynamicField);
      expect(dynamicField.labelPosition).toBe(FormField.LabelPosition.TOP);

      // back to normal
      groupBox.$container.cssWidth(normalWidth);
      groupBox.invalidateLayout();
      groupBox.validateLayout();
      expectNormal(groupBox);

      expect(dynamicField.labelPosition).toBe(FormField.LabelPosition.DEFAULT);
    });
  });

  describe('setResponsive', () => {
    let groupBox;

    beforeEach(() => {
      groupBox = createGroupBox();
    });

    it('switches the responsive state for the inner group box', () => {
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
      groupBox.fields[4].setResponsive(false);
      expectCondensed(groupBox);

      // back to normal
      groupBox.$container.cssWidth(normalWidth);
      groupBox.invalidateLayout();
      groupBox.validateLayout();
      expectNormal(groupBox);
    });
  });
});
