/*
 * Copyright (c) 2010, 2026 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {GroupBox, Menu, scout, SmartField, Status, StringField, ValueField} from '../../../src/index';
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

  describe('visibility', () => {
    it('is invisible if it has no menus and no status', () => {
      let model = helper.createFieldModel(StringField);
      let formField = new StringField();
      formField.init(model);
      formField.render();
      expect(formField.fieldStatus.$container).toHaveClass('invisible');

      formField.setErrorStatus({
        severity: Status.Severity.ERROR,
        message: 'foo'
      });
      expect(formField.fieldStatus.$container).not.toHaveClass('invisible');

      formField.setErrorStatus(null);
      expect(formField.fieldStatus.$container).toHaveClass('invisible');

      formField.setTooltipText('text');
      expect(formField.fieldStatus.$container).not.toHaveClass('invisible');

      formField.setTooltipText(null);
      expect(formField.fieldStatus.$container).toHaveClass('invisible');

      formField.setMenus([{objectType: Menu}]);
      expect(formField.fieldStatus.$container).not.toHaveClass('invisible');

      formField.setMenus(null);
      expect(formField.fieldStatus.$container).toHaveClass('invisible');
    });

    it('is invisible if menusVisible is false', () => {
      let model = helper.createFieldModel(StringField);
      let formField = new StringField();
      formField.init(model);
      formField.render();
      expect(formField.fieldStatus.$container).toHaveClass('invisible');

      formField.setMenus([{objectType: Menu}]);
      expect(formField.fieldStatus.$container).not.toHaveClass('invisible');

      formField.setMenusVisible(false);
      expect(formField.fieldStatus.$container).toHaveClass('invisible');

      formField.setMenusVisible(true);
      expect(formField.fieldStatus.$container).not.toHaveClass('invisible');
    });

    it('is invisible if it has no visible menus', () => {
      let model = helper.createFieldModel(StringField);
      let formField = new StringField();
      formField.init(model);
      formField.render();
      expect(formField.fieldStatus.$container).toHaveClass('invisible');

      formField.setMenus([{objectType: Menu, menuTypes: [ValueField.MenuType.NotNull]}]);
      expect(formField.fieldStatus.$container).toHaveClass('invisible');

      formField.setValue('value');
      expect(formField.fieldStatus.$container).not.toHaveClass('invisible');

      formField.setValue(null);
      expect(formField.fieldStatus.$container).toHaveClass('invisible');
    });

    it('focuses next focusable element if it gets invisible while focused', () => {
      let groupBox = scout.create(GroupBox, {
        parent: session.desktop,
        fields: [{
          objectType: StringField,
          menus: [{objectType: Menu, menuTypes: [ValueField.MenuType.NotNull]}],
          value: 'hi'
        }, {
          objectType: StringField
        }]
      });
      groupBox.render();
      groupBox.fields[0].fieldStatus.focus();
      expect(groupBox.fields[0].fieldStatus.$container).not.toHaveClass('invisible');
      expect(groupBox.fields[0].fieldStatus.get$Focusable()).toBeFocused();

      (groupBox.fields[0] as StringField).setValue(null);
      expect(groupBox.fields[0].fieldStatus.get$Focusable()).toHaveClass('invisible');
      expect(groupBox.fields[1].get$Focusable()).toBeFocused();
    });
  });

  describe('enabled', () => {
    it('is enabled but children disabled even if parent is disabled', () => {
      let formField = scout.create(StringField, {
        parent: session.desktop,
        enabled: false,
        menus: [{
          objectType: Menu,
          childActions: [{objectType: Menu}]
        }]
      });
      formField.render();
      expect(formField.enabledComputed).toBe(false);
      expect(formField.fieldStatus.enabledComputed).toBe(true);
      expect(formField.menus[0].enabledComputed).toBe(false);
      expect(formField.menus[0].childActions[0].enabledComputed).toBe(false);

      // open menu
      formField.fieldStatus.doAction();
      expect(formField.fieldStatus.contextMenu.menuItems[0].enabledComputed).toBe(false);
      expect(formField.fieldStatus.contextMenu.menuItems[0].childActions[0].enabledComputed).toBe(false);

      // close menu
      formField.fieldStatus.contextMenu.animateRemoval = false;
      formField.fieldStatus.doAction();
      expect(formField.fieldStatus.contextMenu).toBe(null);

      formField.setEnabled(true);
      expect(formField.enabledComputed).toBe(true);
      expect(formField.fieldStatus.enabledComputed).toBe(true);
      expect(formField.menus[0].enabledComputed).toBe(true);
      expect(formField.menus[0].childActions[0].enabledComputed).toBe(true);

      // open menu
      formField.fieldStatus.doAction();
      expect(formField.fieldStatus.contextMenu.menuItems[0].enabledComputed).toBe(true);
      expect(formField.fieldStatus.contextMenu.menuItems[0].childActions[0].enabledComputed).toBe(true);

      // close menu
      formField.fieldStatus.contextMenu.animateRemoval = false;
      formField.fieldStatus.doAction();
      expect(formField.fieldStatus.contextMenu).toBe(null);

      formField.setEnabled(false);
      expect(formField.enabledComputed).toBe(false);
      expect(formField.fieldStatus.enabledComputed).toBe(true);
      expect(formField.menus[0].enabledComputed).toBe(false);
      expect(formField.menus[0].childActions[0].enabledComputed).toBe(false);

      // open menu
      formField.fieldStatus.doAction();
      expect(formField.fieldStatus.contextMenu.menuItems[0].enabledComputed).toBe(false);
      expect(formField.fieldStatus.contextMenu.menuItems[0].childActions[0].enabledComputed).toBe(false);

      formField.insertMenu({objectType: Menu});
      expect(formField.menus[1].enabledComputed).toBe(false);
      expect(formField.enabledComputed).toBe(false);
      expect(formField.fieldStatus.enabledComputed).toBe(true);

      // close menu
      formField.fieldStatus.contextMenu.animateRemoval = false;
      formField.fieldStatus.doAction();
      expect(formField.fieldStatus.contextMenu).toBe(null);

      // open menu
      formField.fieldStatus.doAction();
      expect(formField.fieldStatus.contextMenu.menuItems[1].enabledComputed).toBe(false);

      // close menu
      formField.fieldStatus.contextMenu.animateRemoval = false;
      formField.fieldStatus.doAction();
      expect(formField.fieldStatus.contextMenu).toBe(null);

      formField.insertMenu({objectType: Menu, inheritAccessibility: false});
      expect(formField.menus[2].enabledComputed).toBe(true);
      expect(formField.enabledComputed).toBe(false);
      expect(formField.fieldStatus.enabledComputed).toBe(true);

      // open menu
      formField.fieldStatus.doAction();
      expect(formField.fieldStatus.contextMenu.menuItems[1].enabledComputed).toBe(false);
      expect(formField.fieldStatus.contextMenu.menuItems[2].enabledComputed).toBe(true);
    });

    it('is enabled but children disabled even if parent is disabled and menus are shown in tooltip', () => {
      let formField = scout.create(StringField, {
        parent: session.desktop,
        enabled: false,
        tooltipText: 'hi',
        menus: [{
          objectType: Menu,
          childActions: [{objectType: Menu}]
        }]
      });
      formField.render();
      expect(formField.enabledComputed).toBe(false);
      expect(formField.fieldStatus.enabledComputed).toBe(true);
      expect(formField.menus[0].enabledComputed).toBe(false);
      expect(formField.menus[0].childActions[0].enabledComputed).toBe(false);

      // open tooltip
      formField.fieldStatus.doAction();
      expect(formField.fieldStatus.tooltip.menus[0].enabledComputed).toBe(false);
      expect(formField.fieldStatus.tooltip.menus[0].childActions[0].enabledComputed).toBe(false);

      // close tooltip
      formField.fieldStatus.doAction();
      expect(formField.fieldStatus.tooltip).toBe(null);

      formField.setEnabled(true);
      expect(formField.enabledComputed).toBe(true);
      expect(formField.fieldStatus.enabledComputed).toBe(true);
      expect(formField.menus[0].enabledComputed).toBe(true);
      expect(formField.menus[0].childActions[0].enabledComputed).toBe(true);

      // open tooltip
      formField.fieldStatus.doAction();
      expect(formField.fieldStatus.tooltip.menus[0].enabledComputed).toBe(true);
      expect(formField.fieldStatus.tooltip.menus[0].childActions[0].enabledComputed).toBe(true);

      // close tooltip
      formField.fieldStatus.doAction();
      expect(formField.fieldStatus.tooltip).toBe(null);

      formField.setEnabled(false);
      expect(formField.enabledComputed).toBe(false);
      expect(formField.fieldStatus.enabledComputed).toBe(true);
      expect(formField.menus[0].enabledComputed).toBe(false);
      expect(formField.menus[0].childActions[0].enabledComputed).toBe(false);

      // open tooltip
      formField.fieldStatus.doAction();
      expect(formField.fieldStatus.tooltip.menus[0].enabledComputed).toBe(false);
      expect(formField.fieldStatus.tooltip.menus[0].childActions[0].enabledComputed).toBe(false);

      // close tooltip
      formField.fieldStatus.doAction();
      expect(formField.fieldStatus.tooltip).toBe(null);

      formField.insertMenu({objectType: Menu});
      expect(formField.menus[1].enabledComputed).toBe(false);
      expect(formField.enabledComputed).toBe(false);
      expect(formField.fieldStatus.enabledComputed).toBe(true);

      // open tooltip
      formField.fieldStatus.doAction();
      expect(formField.fieldStatus.tooltip.menus[1].enabledComputed).toBe(false);

      // close tooltip
      formField.fieldStatus.tooltip.animateRemoval = false;
      formField.fieldStatus.doAction();
      expect(formField.fieldStatus.tooltip).toBe(null);

      formField.insertMenu({objectType: Menu, inheritAccessibility: false});
      expect(formField.menus[2].enabledComputed).toBe(true);
      expect(formField.enabledComputed).toBe(false);
      expect(formField.fieldStatus.enabledComputed).toBe(true);

      // open tooltip
      formField.fieldStatus.doAction();
      expect(formField.fieldStatus.tooltip.menus[1].enabledComputed).toBe(false);
      expect(formField.fieldStatus.tooltip.menus[2].enabledComputed).toBe(true);
    });
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

    it('toggles expanded and controls attributes of tooltip', () => {
      let model = helper.createFieldModel();
      let formField = new StringField();
      formField.init(model);
      formField.render();
      expect(formField.fieldStatus.$container).toHaveAttr('role', 'button');
      expect(formField.fieldStatus.$container).toHaveAttr('aria-haspopup', 'menu');
      expect(formField.fieldStatus.$container).toHaveAttr('aria-expanded', 'false');
      expect(formField.fieldStatus.$container).not.toHaveAttr('aria-controls');

      formField.setTooltipText('hi there');
      expect(formField.fieldStatus.$container).not.toHaveAttr('aria-controls');
      expect(formField.fieldStatus.$container).toHaveAttr('aria-expanded', 'false');

      formField.fieldStatus.doAction();
      expect(formField.fieldStatus.$container).toHaveAttr('aria-controls', formField.fieldStatus.tooltip.$container.attr('id'));
      expect(formField.fieldStatus.$container).toHaveAttr('aria-expanded', 'true');

      formField.fieldStatus.doAction();
      expect(formField.fieldStatus.$container).not.toHaveAttr('aria-controls');
      expect(formField.fieldStatus.$container).toHaveAttr('aria-expanded', 'false');
    });

    it('toggles expanded and controls attributes of menu', () => {
      let model = helper.createFieldModel();
      let formField = new StringField();
      formField.init(model);
      formField.render();
      expect(formField.fieldStatus.$container).toHaveAttr('role', 'button');
      expect(formField.fieldStatus.$container).toHaveAttr('aria-haspopup', 'menu');
      expect(formField.fieldStatus.$container).toHaveAttr('aria-expanded', 'false');
      expect(formField.fieldStatus.$container).not.toHaveAttr('aria-controls');

      formField.setMenus([{objectType: Menu}]);
      expect(formField.fieldStatus.$container).not.toHaveAttr('aria-controls');
      expect(formField.fieldStatus.$container).toHaveAttr('aria-expanded', 'false');

      session.desktop.one('popupOpen', event => {
        event.popup.animateOpening = false;
      });
      formField.fieldStatus.doAction();
      expect(formField.fieldStatus.$container).toHaveAttr('aria-controls', formField.fieldStatus.contextMenu.$container.attr('id'));
      expect(formField.fieldStatus.$container).toHaveAttr('aria-expanded', 'true');

      formField.fieldStatus.contextMenu.animateRemoval = false;
      formField.fieldStatus.doAction();
      expect(formField.fieldStatus.$container).not.toHaveAttr('aria-controls');
      expect(formField.fieldStatus.$container).toHaveAttr('aria-expanded', 'false');
    });
  });
});
