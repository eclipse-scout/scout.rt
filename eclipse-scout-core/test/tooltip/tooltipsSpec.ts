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
import {FormSpecHelper} from '../../src/testing/index';
import {tooltips, ValueField} from '../../src/index';
import {triggerMouseEnter, triggerMouseLeave} from '../../src/testing/jquery-testing';

describe('scout.tooltips', () => {

  let session: SandboxSession, helper: FormSpecHelper, formField: SpecValueField;

  class SpecValueField extends ValueField<string> {
    protected override _render() {
      this.addContainer(this.$parent, 'form-field');
      this.addField($('<div>TestField</div>'));
      this.addStatus();
    }
  }

  beforeEach(() => {
    jasmine.clock().install();

    setFixtures(sandbox());
    session = sandboxSession();
    helper = new FormSpecHelper(session);

    // Add class desktop to sandbox, tooltip will be added to the closest desktop
    session.$entryPoint.addClass('desktop');

    let model: any = helper.createFieldModel();
    formField = new SpecValueField();
    formField.init(model);
    formField.render();
  });

  afterEach(() => {
    jasmine.clock().uninstall();
  });

  it('can be installed and uninstalled for a form field', () => {
    tooltips.install(formField.$container, {
      parent: session.desktop,
      session: session,
      text: 'Test1'
    });
    expect(formField.$container.data('tooltipSupport')).not.toBeUndefined();
    tooltips.uninstall(formField.$container);
    expect(formField.$container.data('tooltipSupport')).toBeUndefined();
  });

  it('creates a tooltip on mouseenter and removes it on mouseleave', () => {
    tooltips.install(formField.$container, {
      parent: session.desktop,
      session: session,
      text: 'Test2',
      delay: 0
    });
    expect(formField.$container.data('tooltipSupport')).not.toBeUndefined();

    triggerMouseEnter(formField.$container);
    jasmine.clock().tick(100);

    let tooltip = $('body').find('.tooltip');
    let tooltipContent = tooltip.children('.tooltip-content');
    expect(tooltip).not.toBeUndefined();
    expect(tooltip.length).toBe(1);
    expect(tooltipContent.text()).toBe('Test2');

    triggerMouseLeave(formField.$container);

    tooltip = $('body').find('.tooltip');
    expect(tooltip).not.toBeUndefined();
    expect(tooltip.length).toBe(0);

    tooltips.uninstall(formField.$container);
  });

  describe('if text', () => {

    it('is empty no tooltip will be shown', () => {
      tooltips.install(formField.$container, {
        parent: session.desktop,
        session: session,
        text: '',
        delay: 0
      });
      expect(formField.$container.data('tooltipSupport')).not.toBeUndefined();

      triggerMouseEnter(formField.$container);
      jasmine.clock().tick(100);

      let tooltip = $('body').find('.tooltip');
      expect(tooltip).not.toBeUndefined();
      expect(tooltip.length).toBe(0);

      tooltips.uninstall(formField.$container);
    });

    it('is a function, it will be called for tooltip text', () => {
      tooltips.install(formField.$container, {
        parent: session.desktop,
        session: session,
        text: () => 'Test3',
        delay: 0
      });
      expect(formField.$container.data('tooltipSupport')).not.toBeUndefined();

      triggerMouseEnter(formField.$container);
      jasmine.clock().tick(100);

      let tooltip = $('body').find('.tooltip');
      let tooltipContent = tooltip.children('.tooltip-content');
      expect(tooltip).not.toBeUndefined();
      expect(tooltip.length).toBe(1);
      expect(tooltipContent.text()).toBe('Test3');

      tooltips.uninstall(formField.$container);
    });

    it('is undefined no tooltip will be shown', () => {
      tooltips.install(formField.$container, {
        parent: session.desktop,
        session: session,
        text: () => undefined,
        delay: 0
      });
      expect(formField.$container.data('tooltipSupport')).not.toBeUndefined();

      triggerMouseEnter(formField.$container);
      jasmine.clock().tick(100);

      let tooltip = $('body').find('.tooltip');
      expect(tooltip).not.toBeUndefined();
      expect(tooltip.length).toBe(0);

      tooltips.uninstall(formField.$container);
    });

    it('is provided by component, it will be used as tooltip text', () => {
      tooltips.install(formField.$container, {
        parent: session.desktop,
        session: session,
        delay: 0
      });
      expect(formField.$container.data('tooltipSupport')).not.toBeUndefined();

      formField.$container.data('tooltipText', 'Test4');
      triggerMouseEnter(formField.$container);
      jasmine.clock().tick(100);

      let tooltip = $('body').find('.tooltip');
      let tooltipContent = tooltip.children('.tooltip-content');
      expect(tooltip).not.toBeUndefined();
      expect(tooltip.length).toBe(1);
      expect(tooltipContent.text()).toBe('Test4');

      tooltips.uninstall(formField.$container);
    });

    it('is provided as function by component, it will be called and used as tooltip text', () => {
      tooltips.install(formField.$container, {
        parent: session.desktop,
        session: session,
        delay: 0
      });
      expect(formField.$container.data('tooltipSupport')).not.toBeUndefined();

      formField.$container.data('tooltipText', () => {
        return 'Test5';
      });
      triggerMouseEnter(formField.$container);
      jasmine.clock().tick(100);

      let tooltip = $('body').find('.tooltip');
      let tooltipContent = tooltip.children('.tooltip-content');
      expect(tooltip).not.toBeUndefined();
      expect(tooltip.length).toBe(1);
      expect(tooltipContent.text()).toBe('Test5');

      tooltips.uninstall(formField.$container);
    });

    it('is provided using options and by component, text provided using options will be used', () => {
      tooltips.install(formField.$container, {
        parent: session.desktop,
        session: session,
        text: 'Test6',
        delay: 0
      });
      expect(formField.$container.data('tooltipSupport')).not.toBeUndefined();

      formField.$container.data('tooltipText', 'Test7');
      triggerMouseEnter(formField.$container);
      jasmine.clock().tick(100);

      let tooltip = $('body').find('.tooltip');
      let tooltipContent = tooltip.children('.tooltip-content');
      expect(tooltip).not.toBeUndefined();
      expect(tooltip.length).toBe(1);
      expect(tooltipContent.text()).toBe('Test6');

      tooltips.uninstall(formField.$container);
    });

    it('is a function, component is passed as first and only argument', () => {
      tooltips.install(formField.$container, {
        parent: session.desktop,
        session: session,
        text: function(container) {
          return (formField.$container.is(container) && arguments.length === 1) ? 'Test8' : 'InvalidArguments';
        },
        delay: 0
      });
      expect(formField.$container.data('tooltipSupport')).not.toBeUndefined();

      triggerMouseEnter(formField.$container);
      jasmine.clock().tick(100);

      let tooltip = $('body').find('.tooltip');
      let tooltipContent = tooltip.children('.tooltip-content');
      expect(tooltip).not.toBeUndefined();
      expect(tooltip.length).toBe(1);
      expect(tooltipContent.text()).toBe('Test8');

      tooltips.uninstall(formField.$container);
    });

  });

  it('can update the text of an already visible tooltip', () => {
    // 1. Test with 'tooltipText' data attribute in DOM
    let $testElement = session.$entryPoint.appendDiv('tooltip-test')
      .data('tooltipText', 'initial text');

    tooltips.install($testElement, {
      parent: session.desktop,
      session: session,
      delay: 123
    });

    let tooltip = $('body').find('.tooltip');
    expect(tooltip.length).toBe(0);

    triggerMouseEnter($testElement);
    jasmine.clock().tick(100);

    tooltip = $('body').find('.tooltip');
    expect(tooltip.length).toBe(0);

    jasmine.clock().tick(50);

    tooltip = $('body').find('.tooltip');
    expect(tooltip.length).toBe(1);
    expect(tooltip.text()).toBe('initial text');

    $testElement.data('tooltipText', 'updated text');

    tooltip = $('body').find('.tooltip');
    expect(tooltip.length).toBe(1);
    expect(tooltip.text()).toBe('initial text');

    let support = $testElement.data('tooltipSupport');
    tooltips.update($testElement);

    tooltip = $('body').find('.tooltip');
    expect(tooltip.length).toBe(1);
    expect(tooltip.text()).toBe('updated text');
    let support2 = $testElement.data('tooltipSupport');
    expect(support2).toBe(support);

    tooltips.uninstall($testElement);

    tooltip = $('body').find('.tooltip');
    expect(tooltip.length).toBe(0);

    // 2. Test with 'text' property in tooltip support
    $testElement.removeData('tooltipText');

    tooltips.install($testElement, {
      parent: session.desktop,
      session: session,
      delay: 123,
      text: 'hard coded text'
    });

    triggerMouseEnter($testElement);
    jasmine.clock().tick(150);

    tooltip = $('body').find('.tooltip');
    expect(tooltip.length).toBe(1);
    expect(tooltip.text()).toBe('hard coded text');

    tooltips.update($testElement, {
      text: 'my new text',
      delay: 70
    });

    tooltip = $('body').find('.tooltip');
    expect(tooltip.length).toBe(1);
    expect(tooltip.text()).toBe('my new text');

    triggerMouseLeave($testElement);

    tooltip = $('body').find('.tooltip');
    expect(tooltip.length).toBe(0);

    triggerMouseEnter($testElement);
    jasmine.clock().tick(80);

    tooltip = $('body').find('.tooltip');
    expect(tooltip.length).toBe(1);
    expect(tooltip.text()).toBe('my new text');

    tooltips.uninstall($testElement);
  });

});
