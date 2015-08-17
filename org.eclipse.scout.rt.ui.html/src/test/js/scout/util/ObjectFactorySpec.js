/* global LocaleSpecHelper*/
describe("ObjectFactory", function() {

  beforeEach(function() {
    // Needed because some model adapters make JSON calls during initialization (e.g. Calendar.js)
    jasmine.Ajax.install();
    jasmine.clock().install();
  });

  afterEach(function() {
    jasmine.Ajax.uninstall();
    jasmine.clock().uninstall();
  });

  /**
   * This function is used to create a special-model when a model-adapter requires one.
   * Normally a generic model with id and objectType is sufficient, but some adapters require a more complex
   * model in order to make this test succeed. Remember you must add the additional adapter models to the
   * adapaterDataCache of the Session.
   */
  function createModel(session, id, objectType) {
    var model = createSimpleModel(objectType, id);
    if ('Menu.NavigateUp' === objectType || 'Menu.NavigateDown' === objectType) {
      var outlineId = 'outline' + id;
      model.outline = outlineId;
      session._adapterDataCache[outlineId] = {
        id: outlineId,
        objectType: 'Outline'
      };
    } else if ('Calendar' === objectType) {
      model.displayMode = 3;
      model.selectedDate = '2015-04-06 00:00:00.000';
    } else if ('Form' === objectType) {
      model.displayHint = 'view';
    } else if ('ColumnUserTableFilter' === objectType) {
      model.table = {};
      model.column = {};
      model.calculateCube = function(){};
    } else if ('TabBox' === objectType) {
      var tabItemId = 'tabItem' + id;
      model.selectedTab = 0;
      model.tabItems = [tabItemId];
      session._adapterDataCache[tabItemId] = {
        id: tabItemId,
        objectType: 'TabItem'
      };
    }
    else if ('ButtonAdapterMenu' === objectType) {
      model.button = {
          on: function() {}
      };
    }
    return model;
  }

  /**
   * When this test fails with a message like 'TypeError: scout.[ObjectType] is not a constructor...'
   * you should check if the required .js File is registered in SpecRunnerMaven.html.
   */
  function verifyCreationAndRegistration(session, factories) {
    var i, model, factory, object, modelAdapter;
    session.objectFactory.register(factories);

    for (i = 0; i < factories.length; i++) {
      factory = factories[i];
      model = createModel(session, i, factory.objectType);
      object = factory.create(model);
      object.init(model, session);
      session.registerModelAdapter(object);
      modelAdapter = session.getModelAdapter(model.id);
      expect(modelAdapter).toBe(object);
    }
  }

  it("creates objects which are getting registered in the widget map", function() {
    setFixtures(sandbox());
    var session = new scout.Session($('#sandbox'), '1.1'),
      factories = scout.defaultObjectFactories;
    session.locale = new LocaleSpecHelper().createLocale('de');
    verifyCreationAndRegistration(session, factories);
  });

  it("distinguishes between mobile and desktop objects", function() {
    setFixtures(sandbox());
    var session = new scout.Session($('#sandbox'), '1.1'),
      factories = scout.mobileObjectFactories;
    session.locale = new LocaleSpecHelper().createLocale('de');
    verifyCreationAndRegistration(session, factories);
  });

});
