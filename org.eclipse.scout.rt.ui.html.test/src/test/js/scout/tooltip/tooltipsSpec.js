/*
 * Copyright (c) 2014-2018 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
describe("scout.tooltips", function() {

  var session, helper, formField, model;

  beforeEach(function() {
    jasmine.clock().install();

    setFixtures(sandbox());
    session = sandboxSession();
    helper = new scout.FormSpecHelper(session);

    // Add class desktop to sandbox, tooltip will be added to closest desktop
    session.$entryPoint.addClass('desktop');

    model = helper.createFieldModel();
    formField = new scout.ValueField();
    formField._render = function() {
      this.addContainer(this.$parent, 'form-field');
      this.addField($('<div>TestField</div>'));
      this.addStatus();
    };
    formField.init(model);
    formField.render();
  });

  afterEach(function() {
    jasmine.clock().uninstall();
  });

  it("can be installed and uninstalled for a form field", function() {
    scout.tooltips.install(formField.$container, {
      parent: session.desktop,
      session: session,
      text: 'Test1'
    });
    expect(formField.$container.data('tooltipSupport')).not.toBeUndefined();
    scout.tooltips.uninstall(formField.$container);
    expect(formField.$container.data('tooltipSupport')).toBeUndefined();
  });

  it("creates a tooltip on mouseenter and removes it on mouseleave", function() {
    scout.tooltips.install(formField.$container, {
      parent: session.desktop,
      session: session,
      text: 'Test2',
      delay: 0
    });
    expect(formField.$container.data('tooltipSupport')).not.toBeUndefined();

    formField.$container.triggerMouseEnter();
    jasmine.clock().tick(100);

    var tooltip = $('body').find('.tooltip');
    var tooltipContent = tooltip.children('.tooltip-content');
    expect(tooltip).not.toBeUndefined();
    expect(tooltip.length).toBe(1);
    expect(tooltipContent.text()).toBe('Test2');

    formField.$container.triggerMouseLeave();

    tooltip = $('body').find('.tooltip');
    expect(tooltip).not.toBeUndefined();
    expect(tooltip.length).toBe(0);

    scout.tooltips.uninstall(formField.$container);
  });

  describe("if text", function() {

    it("is empty no tooltip will be shown", function() {
      scout.tooltips.install(formField.$container, {
        parent: session.desktop,
        session: session,
        text: '',
        delay: 0
      });
      expect(formField.$container.data('tooltipSupport')).not.toBeUndefined();

      formField.$container.triggerMouseEnter();
      jasmine.clock().tick(100);

      var tooltip = $('body').find('.tooltip');
      expect(tooltip).not.toBeUndefined();
      expect(tooltip.length).toBe(0);

      scout.tooltips.uninstall(formField.$container);
    });

    it("is a function, it will be called for tooltip text", function() {
      scout.tooltips.install(formField.$container, {
        parent: session.desktop,
        session: session,
        text: function() {
          return 'Test3';
        },
        delay: 0
      });
      expect(formField.$container.data('tooltipSupport')).not.toBeUndefined();

      formField.$container.triggerMouseEnter();
      jasmine.clock().tick(100);

      var tooltip = $('body').find('.tooltip');
      var tooltipContent = tooltip.children('.tooltip-content');
      expect(tooltip).not.toBeUndefined();
      expect(tooltip.length).toBe(1);
      expect(tooltipContent.text()).toBe('Test3');

      scout.tooltips.uninstall(formField.$container);
    });

    it("is undefined no tooltip will be shown", function() {
      scout.tooltips.install(formField.$container, {
        parent: session.desktop,
        session: session,
        text: function() {
          return undefined;
        },
        delay: 0
      });
      expect(formField.$container.data('tooltipSupport')).not.toBeUndefined();

      formField.$container.triggerMouseEnter();
      jasmine.clock().tick(100);

      var tooltip = $('body').find('.tooltip');
      expect(tooltip).not.toBeUndefined();
      expect(tooltip.length).toBe(0);

      scout.tooltips.uninstall(formField.$container);
    });

    it("is provided by component, it will be used as tooltip text", function() {
      scout.tooltips.install(formField.$container, {
        parent: session.desktop,
        session: session,
        delay: 0
      });
      expect(formField.$container.data('tooltipSupport')).not.toBeUndefined();

      formField.$container.data('tooltipText', 'Test4');
      formField.$container.triggerMouseEnter();
      jasmine.clock().tick(100);

      var tooltip = $('body').find('.tooltip');
      var tooltipContent = tooltip.children('.tooltip-content');
      expect(tooltip).not.toBeUndefined();
      expect(tooltip.length).toBe(1);
      expect(tooltipContent.text()).toBe('Test4');

      scout.tooltips.uninstall(formField.$container);
    });

    it("is provided as function by component, it will be called and used as tooltip text", function() {
      scout.tooltips.install(formField.$container, {
        parent: session.desktop,
        session: session,
        delay: 0
      });
      expect(formField.$container.data('tooltipSupport')).not.toBeUndefined();

      formField.$container.data('tooltipText', function() {
        return 'Test5';
      });
      formField.$container.triggerMouseEnter();
      jasmine.clock().tick(100);

      var tooltip = $('body').find('.tooltip');
      var tooltipContent = tooltip.children('.tooltip-content');
      expect(tooltip).not.toBeUndefined();
      expect(tooltip.length).toBe(1);
      expect(tooltipContent.text()).toBe('Test5');

      scout.tooltips.uninstall(formField.$container);
    });

    it("is provided using options and by component, text provided using options will be used", function() {
      scout.tooltips.install(formField.$container, {
        parent: session.desktop,
        session: session,
        text: 'Test6',
        delay: 0
      });
      expect(formField.$container.data('tooltipSupport')).not.toBeUndefined();

      formField.$container.data('tooltipText', 'Test7');
      formField.$container.triggerMouseEnter();
      jasmine.clock().tick(100);

      var tooltip = $('body').find('.tooltip');
      var tooltipContent = tooltip.children('.tooltip-content');
      expect(tooltip).not.toBeUndefined();
      expect(tooltip.length).toBe(1);
      expect(tooltipContent.text()).toBe('Test6');

      scout.tooltips.uninstall(formField.$container);
    });

    it("is a function, component is passed as first and only argument", function() {
      scout.tooltips.install(formField.$container, {
        parent: session.desktop,
        session: session,
        text: function() {
          return (formField.$container.is(arguments[0]) && arguments.length == 1) ? 'Test8' : 'InvalidArguments';
        },
        delay: 0
      });
      expect(formField.$container.data('tooltipSupport')).not.toBeUndefined();

      formField.$container.triggerMouseEnter();
      jasmine.clock().tick(100);

      var tooltip = $('body').find('.tooltip');
      var tooltipContent = tooltip.children('.tooltip-content');
      expect(tooltip).not.toBeUndefined();
      expect(tooltip.length).toBe(1);
      expect(tooltipContent.text()).toBe('Test8');

      scout.tooltips.uninstall(formField.$container);
    });

  });

  it("can update the text of an already visible tooltip", function() {
    // 1. Test with 'tooltipText' data attribute in DOM
    var $testElement = session.$entryPoint.appendDiv('tooltip-test')
      .data('tooltipText', 'initial text');

    scout.tooltips.install($testElement, {
      parent: session.desktop,
      session: session,
      delay: 123
    });

    var tooltip = $('body').find('.tooltip');
    expect(tooltip.length).toBe(0);

    $testElement.triggerMouseEnter();
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

    var support = $testElement.data('tooltipSupport');
    scout.tooltips.update($testElement);

    tooltip = $('body').find('.tooltip');
    expect(tooltip.length).toBe(1);
    expect(tooltip.text()).toBe('updated text');
    var support2 = $testElement.data('tooltipSupport');
    expect(support2).toBe(support);

    scout.tooltips.uninstall($testElement);

    tooltip = $('body').find('.tooltip');
    expect(tooltip.length).toBe(0);

    // 2. Test with 'text' property in tooltip support
    $testElement.removeData('tooltipText');

    scout.tooltips.install($testElement, {
      parent: session.desktop,
      session: session,
      delay: 123,
      text: 'hard coded text'
    });

    $testElement.triggerMouseEnter();
    jasmine.clock().tick(150);

    tooltip = $('body').find('.tooltip');
    expect(tooltip.length).toBe(1);
    expect(tooltip.text()).toBe('hard coded text');

    scout.tooltips.update($testElement, {
      text: 'my new text',
      delay: 70
    });

    tooltip = $('body').find('.tooltip');
    expect(tooltip.length).toBe(1);
    expect(tooltip.text()).toBe('my new text');

    $testElement.triggerMouseLeave();

    tooltip = $('body').find('.tooltip');
    expect(tooltip.length).toBe(0);

    $testElement.triggerMouseEnter();
    jasmine.clock().tick(80);

    tooltip = $('body').find('.tooltip');
    expect(tooltip.length).toBe(1);
    expect(tooltip.text()).toBe('my new text');

    scout.tooltips.uninstall($testElement);
  });

});
