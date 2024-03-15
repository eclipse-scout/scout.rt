/*
 * Copyright (c) 2010, 2024 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {FormSpecHelper, JQueryTesting} from '../../src/testing/index';
import {scout, Tooltip, tooltips, TooltipSupport, ValueField} from '../../src/index';

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

    JQueryTesting.triggerMouseEnter(formField.$container);
    jasmine.clock().tick(100);

    let tooltip = $('body').find('.tooltip');
    let tooltipContent = tooltip.children('.tooltip-content');
    expect(tooltip).not.toBeUndefined();
    expect(tooltip.length).toBe(1);
    expect(tooltipContent.text()).toBe('Test2');

    JQueryTesting.triggerMouseLeave(formField.$container);

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

      JQueryTesting.triggerMouseEnter(formField.$container);
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

      JQueryTesting.triggerMouseEnter(formField.$container);
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

      JQueryTesting.triggerMouseEnter(formField.$container);
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
      JQueryTesting.triggerMouseEnter(formField.$container);
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
      JQueryTesting.triggerMouseEnter(formField.$container);
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
      JQueryTesting.triggerMouseEnter(formField.$container);
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

      JQueryTesting.triggerMouseEnter(formField.$container);
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

    JQueryTesting.triggerMouseEnter($testElement);
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

    JQueryTesting.triggerMouseEnter($testElement);
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

    JQueryTesting.triggerMouseLeave($testElement);

    tooltip = $('body').find('.tooltip');
    expect(tooltip.length).toBe(0);

    JQueryTesting.triggerMouseEnter($testElement);
    jasmine.clock().tick(80);

    tooltip = $('body').find('.tooltip');
    expect(tooltip.length).toBe(1);
    expect(tooltip.text()).toBe('my new text');

    tooltips.uninstall($testElement);
  });

  it('can update an already visible tooltip when the DOM element is replaced', () => {
    let $container = session.$entryPoint.appendDiv('tooltip-test-container');
    let $element1 = $container.appendDiv('tooltip-test')
      .attr('id', 'el1')
      .data('tooltipText', 'Text <b>1</b>')
      .data('htmlEnabled', true);
    let $element2 = $container.appendDiv('tooltip-test')
      .attr('id', 'el2')
      .data('tooltipText', 'Text < 2');

    // Install tooltip support on container, address elements with 'selector' property
    tooltips.install($container, {
      parent: session.desktop,
      selector: '.tooltip-test',
      delay: 123
    });
    let support = $container.data('tooltipSupport') as TooltipSupport;
    let $tooltip: JQuery;
    let tooltip: Tooltip;

    // -----------------

    $tooltip = $('body').find('.tooltip');
    expect($tooltip.length).toBe(0);

    JQueryTesting.triggerMouseEnter($element1);
    jasmine.clock().tick(150);

    // HTML content
    $tooltip = $('body').find('.tooltip');
    expect($tooltip.length).toBe(1);
    expect($tooltip.text()).toBe('Text 1');
    expect($tooltip.children('.tooltip-content').html()).toBe('Text <b>1</b>');
    tooltip = scout.widget($tooltip);
    expect(tooltip.$anchor[0]).toBe($element1[0]);

    // Remove tooltip text -> tooltip should be removed
    $element1.data('tooltipText', '');
    support.update($element1);

    $tooltip = $('body').find('.tooltip');
    expect($tooltip.length).toBe(0);

    JQueryTesting.triggerMouseLeave($element1);

    // -----------------

    JQueryTesting.triggerMouseEnter($element2);
    jasmine.clock().tick(150);

    // Plain text content
    $tooltip = $('body').find('.tooltip');
    expect($tooltip.length).toBe(1);
    expect($tooltip.text()).toBe('Text < 2');
    expect($tooltip.children('.tooltip-content').html()).toBe('Text &lt; 2');
    tooltip = scout.widget($tooltip);
    expect(tooltip.$anchor[0]).toBe($element2[0]);

    // Change 'htmlEnabled' property -> same tooltip should now support HTML content
    $element2.data('tooltipText', '<b>Up</b>dated');
    $element2.data('htmlEnabled', true);
    support.update($element2);

    $tooltip = $('body').find('.tooltip');
    expect($tooltip.length).toBe(1);
    expect($tooltip.text()).toBe('Updated');
    expect($tooltip.children('.tooltip-content').html()).toBe('<b>Up</b>dated');
    expect(scout.widget($tooltip)).toBe(tooltip);
    tooltip = scout.widget($tooltip);
    expect(tooltip.$anchor[0]).toBe($element2[0]);

    // -----------------

    // Replace element2 with a new DIV -> Tooltip should be updated, including the $anchor
    let $newElement2 = $container.makeDiv('tooltip-test')
      .attr('id', 'el2_new')
      .attr('data-tooltip-text', 'New');
    $element2.replaceWith($newElement2);

    JQueryTesting.triggerMouseEnter($newElement2);
    jasmine.clock().tick(150);

    $tooltip = $('body').find('.tooltip');
    expect($tooltip.length).toBe(1);
    expect($tooltip.text()).toBe('New');
    expect(scout.widget($tooltip)).toBe(tooltip);
    tooltip = scout.widget($tooltip);
    expect(tooltip.$anchor[0]).toBe($newElement2[0]);
    expect(tooltip.$anchor[0]).not.toBe($element2[0]);
  });
});
