/* global TableSpecHelper, LocaleSpecHelper */
describe("TableControl", function() {
  var session;
  var tableHelper;

  beforeEach(function() {
    setFixtures(sandbox());
    session = new scout.Session($('#sandbox'), '1.1');
    tableHelper = new TableSpecHelper(session);

    jasmine.Ajax.install();
    jasmine.clock().install();
  });

  afterEach(function() {
    session = null;
    jasmine.Ajax.uninstall();
    jasmine.clock().uninstall();
  });

  function createModel(id) {
    if (id === undefined) {
      id = createUniqueAdapterId();
    }

    var model = {
      "id": id,
      "enabled": true,
      "visible": true
    };

    return model;
  }

  function createFormMock() {
    var form = {
      render: function() {},
      remove: function() {},
      $container: $('<div>')
    };
    form.htmlComp = new scout.HtmlComponent(form.$container, session);
    return form;
  }

  function createAction(model) {
    var action = new scout.TableControl();
    action.init(model, session);
    action.form = createFormMock();
    return action;
  }

  function createTable() {
    var tableModel = tableHelper.createModelFixture(2);
    return tableHelper.createTable(tableModel);
  }

  describe("onModelPropertyChange", function() {
    var table;

    beforeEach(function() {
      table = createTable();
    });

    describe("selected", function() {

      beforeEach(function() {
        // Open and closing of the container is animated -> disable animation in order to be able to test it
        $.fx.off = true;
      });

      afterEach(function() {
        $.fx.off = false;
      });

      it("opens and closes the control container", function() {
        var action = createAction(createModel());
        table.controls = [action];
        table.render(session.$entryPoint);
        var $controlContainer = table.footer._$controlContainer;

        expect($controlContainer).toBeHidden();

        var event = createPropertyChangeEvent(action, {
          "selected": true
        });
        action.onModelPropertyChange(event);
        expect($controlContainer).toBeVisible();

        event = createPropertyChangeEvent(action, {
          "selected": false
        });
        action.onModelPropertyChange(event);
        expect($controlContainer).toBeHidden();
      });

    });

    describe("tooltipText", function() {

      it("attaches hover listener, but only once", function() {
        var action = createAction(createModel());
        table.controls = [action];
        table.render(session.$entryPoint);

        spyOn(action.$container, 'hover').and.callThrough();

        var event = createPropertyChangeEvent(action, {
          "tooltipText": 'my tooltip'
        });
        action.onModelPropertyChange(event);

        event = createPropertyChangeEvent(action, {
          "tooltipText": 'my tooltip new'
        });
        action.onModelPropertyChange(event);

        expect(action.$container.hover.calls.count()).toEqual(1);
      });

    });

  });

  describe("setSelected", function() {
    var table;

    beforeEach(function() {
      table = createTable();

      // Open and closing of the container is animated -> disable animation in order to be able to test it
      $.fx.off = true;
    });

    afterEach(function() {
      $.fx.off = false;
    });

    it("removes the content of the previous selected control without closing the container", function() {
      var action = createAction(createModel());
      var action2 = createAction(createModel());
      table.controls = [action, action2];

      action.selected = true;
      table.render(session.$entryPoint);
      var $controlContainer = table.footer._$controlContainer;

      expect($controlContainer).toBeVisible();
      expect(action.contentRendered).toBe(true);
      expect(action2.contentRendered).toBe(false);

      action2.setSelected(true);

      expect($controlContainer).toBeVisible();
      expect(action.contentRendered).toBe(false);
      expect(action.selected).toBe(false);
      expect(action2.contentRendered).toBe(true);
      expect(action2.selected).toBe(true);
    });

    it("sends selected events (for current and previous selection)", function() {
      var action = createAction(createModel());
      var action2 = createAction(createModel());
      table.controls = [action, action2];

      action.selected = true;
      table.render(session.$entryPoint);

      action2.setSelected(true);

      sendQueuedAjaxCalls();
      var events = [
        new scout.Event('selected', action.id, {
          "selected": false
        }),
        new scout.Event('selected', action2.id, {
          "selected": true
        }),
      ];
      expect(mostRecentJsonRequest()).toContainEvents(events);
    });

  });

});
