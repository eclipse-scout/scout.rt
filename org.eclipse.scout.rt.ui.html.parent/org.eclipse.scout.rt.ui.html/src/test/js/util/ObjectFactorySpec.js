describe("ObjectFactory", function() {

  function verifyCreationAndRegistration(session, factories) {
    session.objectFactory.register(factories);

    var i, model, factory, object, registeredObject;
    for (i = 0; i < factories.length; i++) {
      factory = factories[i];
      model = {
        id: i,
        objectType: factory.objectType
      };

      object = null;
      try {
        object = factory.create(session, model);
      } catch (e) {
        //Object probably not registered, check SpecRunnerMaven.html
        expect(object).toBeTruthy();
      }

      registeredObject = session.widgetMap[model.id];
      expect(registeredObject).toBe(object);
    }
  }

  it("creates objects which are getting registered in the widget map", function() {
    var session = new scout.Session($('#sandbox'), '1.1');
    var factories = scout.defaultObjectFactories;

    verifyCreationAndRegistration(session, factories);
  });

  it("distinguishes between mobile and desktop objects", function() {
    var session = new scout.Session($('#sandbox'), '1.1');
    var factories = scout.mobileObjectFactories;

    verifyCreationAndRegistration(session, factories);
  });

});
