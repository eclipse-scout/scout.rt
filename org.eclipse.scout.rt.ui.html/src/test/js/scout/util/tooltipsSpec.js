/* global FormSpecHelper */
describe("scout.tooltips", function() {

  var session, helper, formField, model;

  beforeEach(function() {
    jasmine.clock().install();

    setFixtures(sandbox());
    session = sandboxSession();
    helper = new FormSpecHelper(session);

    // Add class desktop to sandbox, tooltip will be added to closest desktop
    session.$entryPoint.addClass('desktop');

    model = helper.createFieldModel();
    formField = new scout.ValueField();
    formField._render = function($parent) {
      this.addContainer($parent, 'form-field');
      this.addField($('<div>TestField</div>'));
      this.addStatus();
    };
    formField.init(model);
    formField.render(session.$entryPoint);
  });

  afterEach(function() {
    jasmine.clock().uninstall();
  });

  it("can be installed and uninstalled for a form field", function() {
    scout.tooltips.install(formField.$container, {
      parent: new scout.NullWidget(),
      session: session,
      tooltipText: 'Test1'
    });
    expect(formField.$container.data('tooltipSupport')).not.toBeUndefined();
    scout.tooltips.uninstall(formField.$container);
    expect(formField.$container.data('tooltipSupport')).toBeUndefined();
  });

  it("creates a tooltip on mouseenter and removes it on mouseleave", function() {
    scout.tooltips.install(formField.$container, {
      parent: new scout.NullWidget(),
      session: session,
      tooltipText: 'Test2',
      tooltipDelay: 0
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
        parent: new scout.NullWidget(),
        session: session,
        tooltipText: '',
        tooltipDelay: 0
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
        parent: new scout.NullWidget(),
        session: session,
        tooltipText: function() {
          return 'Test3';
        },
        tooltipDelay: 0
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
        parent: new scout.NullWidget(),
        session: session,
        tooltipText: function() {
          return undefined;
        },
        tooltipDelay: 0
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
        parent: new scout.NullWidget(),
        session: session,
        tooltipDelay: 0
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
        parent: new scout.NullWidget(),
        session: session,
        tooltipDelay: 0
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
        parent: new scout.NullWidget(),
        session: session,
        tooltipText: 'Test6',
        tooltipDelay: 0
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
        parent: new scout.NullWidget(),
        session: session,
        tooltipText: function() {
          return (formField.$container.is(arguments[0]) && arguments.length == 1) ? 'Test8' : 'InvalidArguments';
        },
        tooltipDelay: 0
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

});
