describe("FormToolButton", function() {
  var session;
  var desktop;

  beforeEach(function() {
    setFixtures(sandbox());
    session = sandboxSession();
    desktop = {
      $parent: session.$entryPoint,
      $toolContainer: session.$entryPoint.appendDiv('taskbar-tools').hide()
    };
  });

  var formMock = {
    render: function() {},
    htmlComp: {
      pack: function() {}
    },
    rootGroupBox: {
      menuBar: {
        bottom: function() {}
      }
    },
    _initialFocusElement: function() {
      return null;
    }
  };

  function createAction(model) {
    var action = new scout.FormToolButton();
    action.init(model, session);
    action.desktop = desktop;
    action.form = formMock;
    action.position = function() {};
    return action;
  }

  function findToolContainer() {
    return $('.popup');
  }

  describe("onModelPropertyChange", function() {

    describe("selected", function() {

      it("opens and closes the tool container", function() {
        var action = createAction(createSimpleModel('FormToolButton'));
        action.render(session.$entryPoint);
        expect(findToolContainer()).not.toExist();

        var event = createPropertyChangeEvent(action, {
          "selected": true
        });
        action.onModelPropertyChange(event);
        expect(findToolContainer()).toBeVisible();

        event = createPropertyChangeEvent(action, {
          "selected": false
        });
        action.onModelPropertyChange(event);
        expect(findToolContainer()).not.toExist();
      });

    });

  });

});
