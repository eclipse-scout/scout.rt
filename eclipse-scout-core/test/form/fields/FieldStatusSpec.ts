/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {FieldStatus, GroupBox, Menu, scout, SmartField, Status, StringField} from '../../../src/index';
import {FormSpecHelper, JQueryTesting} from '../../../src/testing/index';

describe('FieldStatus', () => {
  let session: SandboxSession, helper: FormSpecHelper;

  beforeEach(() => {
    setFixtures(sandbox());
    session = sandboxSession();
    helper = new FormSpecHelper(session);
    jasmine.clock().install();
  });

  afterEach(() => {
    jasmine.clock().uninstall();
  });

  describe('parent changes visibility', () => {
    let formField: StringField;

    beforeEach(() => {
      let model = helper.createFieldModel(StringField);
      formField = new StringField();
      formField.init(model);
      formField.render();
      formField.setErrorStatus({
        severity: Status.Severity.ERROR,
        message: 'foo'
      });
    });

    it('when desktop becomes invisible popup must be removed', () => {
      expect(formField.fieldStatus.tooltip.rendered).toBe(true);

      // hide desktop -> tooltip must be removed too
      formField.parent.setVisible(false);
      expect(formField.fieldStatus.tooltip.rendered).toBe(false);

      // show desktop again -> tooltip must be rendered again
      // happens in a timeout, thus we must let the clock tick
      formField.parent.setVisible(true);
      jasmine.clock().tick(0);
      expect(formField.fieldStatus.tooltip.rendered).toBe(true);
    });

    /**
     * This test relies on a property change event for the 'parent' property, triggered by Widget.js.
     */
    it('must update listeners when one of its parent changes', () => {
      let groupBox = scout.create(GroupBox, {
        parent: session.desktop
      });
      groupBox.render();
      formField.setParent(groupBox);

      // hide groupBox -> tooltip must be removed too
      groupBox.setVisible(false);
      expect(formField.fieldStatus.tooltip.rendered).toBe(false);

      // show groupBox again -> tooltip must be rendered again
      // happens in a timeout, thus we must let the clock tick
      groupBox.setVisible(true);
      jasmine.clock().tick(0);
      expect(formField.fieldStatus.tooltip.rendered).toBe(true);
    });

    it('de-register all listeners when tooltip is destroyed', () => {
      // parents = StringField, Desktop, NullWidget, NullWidget (root parent)
      // @ts-expect-error
      expect(formField.fieldStatus._parents.length).toBe(4);
      formField.setErrorStatus(null);
      // @ts-expect-error
      expect(formField.fieldStatus._parents.length).toBe(0);
    });

  });

  /**
   * Test for the case where we had an error-status with a message before and then a status with an empty message is set.
   * In that case the tooltip must be closed. Set ticket 250554.
   */
  it('must hide tooltip when new status has no message', () => {
    let model = helper.createFieldModel();
    let formField = new StringField();
    formField.init(model);
    formField.render();

    // same structure as MultiStatus.java received from UI-server
    let status1 = new Status({
      message: 'Foo',
      severity: Status.Severity.ERROR,
      children: [{
        message: 'Foo',
        severity: Status.Severity.ERROR
      }]
    });
    formField.setErrorStatus(status1);
    expect(session.desktop.$container.find('.tooltip').length).toBe(1);

    // same structure as MultiStatus.java which has no children anymore
    let status2 = new Status({
      message: '',
      severity: Status.Severity.OK
    });
    formField.setErrorStatus(status2);
    expect(session.desktop.$container.find('.tooltip').length).toBe(0);
  });

  it('must not close tooltip if a tooltip submenu is opened', () => {
    let formField = scout.create(SmartField, {parent: session.desktop});
    formField.render();
    formField.focus();
    formField.setMenus([{
      objectType: Menu,
      childActions: [{objectType: Menu}]
    }]);
    formField.setTooltipText('hi there');
    formField.fieldStatus.togglePopup();
    let tooltip = formField.fieldStatus.tooltip;
    tooltip.menus[0].setSelected(true);
    let popup = tooltip.menus[0].popup;
    expect(tooltip.rendered).toBeTrue();
    expect(popup.rendered).toBeTrue();

    // Focus will be set to menu after popup has been opened -> simulate this
    formField.fieldStatus.tooltip.menus[0].popup.validateFocus();
    // @ts-expect-error
    formField._onFieldBlur();
    expect(tooltip.rendered).toBeTrue();
    expect(popup.rendered).toBeTrue();
  });

  it('closes submenus of tooltip if tooltip is destroyed ', () => {
    let formField = scout.create(StringField, {parent: session.desktop});
    formField.setTooltipText('hi there');
    formField.setMenus([{
      objectType: Menu,
      childActions: [{objectType: Menu}]
    }]);
    let $outside = session.$entryPoint.appendDiv();
    formField.render();

    formField.fieldStatus.togglePopup();
    let tooltip = formField.fieldStatus.tooltip;
    tooltip.menus[0].setSelected(true);
    let popup = tooltip.menus[0].popup;
    expect(tooltip.rendered).toBeTrue();
    expect(popup.rendered).toBeTrue();

    JQueryTesting.triggerMouseDownCapture($outside);
    expect(tooltip.rendered).toBeFalse();
    expect(popup.rendered).toBeFalse();
    expect(popup.destroyed).toBe(true);
    expect(tooltip.menus[0].selected).toBe(false);
  });

  describe('aria properties', () => {

    it('alerts error message', () => {
      let model = helper.createFieldModel();
      let formField = new StringField();
      formField.init(model);
      formField.render();

      let status1 = new Status({
        message: 'Foo',
        severity: Status.Severity.ERROR
      });
      formField.setErrorStatus(status1);
      expect(formField.fieldStatus.tooltip.$content).toHaveAttr('role', 'alert');
    });
  });
});
