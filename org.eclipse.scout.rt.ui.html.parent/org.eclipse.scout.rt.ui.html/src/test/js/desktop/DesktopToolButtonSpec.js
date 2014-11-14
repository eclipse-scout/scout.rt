describe("DesktopToolButton", function() {
  var session;
  var desktop;

  beforeEach(function() {
    setFixtures(sandbox());
    session = new scout.Session($('#sandbox'), '1.1');
    desktop = {
        $parent: session.$entryPoint,
        $toolContainer: session.$entryPoint.appendDIV('desktop-tool-container').hide(),
    };
  });

  function createModel(id) {
    if (id === undefined) {
      id = createUniqueAdapterId();
    }

    var model =  {
      "id": id,
      "enabled": true,
      "visible": true
    };

    return model;
  }

  var formMock = {
      render : function() {},
      htmlComp : {
        pack: function() {}
      }
  };

  function createAction(model) {
    var action =  new scout.DesktopToolButton();
    action.init(model, session);
    action.desktop = desktop;
    action.form = formMock;
    action.position = function() {};
    return action;
  }

  function findToolContainer() {
    return $('.desktop-tool-container');
  }

  describe("onModelPropertyChange", function() {

    describe("selected", function() {

      it("opens and closes the tool container", function() {
        var action = createAction(createModel());
        action.render(session.$entryPoint);
        expect(findToolContainer()).toBeHidden();

        var event = createPropertyChangeEvent(action, {
          "selected": true
        });
        action.onModelPropertyChange(event);
        expect(findToolContainer()).toBeVisible();

        event = createPropertyChangeEvent(action, {
          "selected": false
        });
        action.onModelPropertyChange(event);
        expect(findToolContainer()).toBeHidden();
      });

    });

  });

});
