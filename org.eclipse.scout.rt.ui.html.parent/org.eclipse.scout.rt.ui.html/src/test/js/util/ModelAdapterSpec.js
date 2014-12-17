describe("ModelAdapter", function() {

  var session;
  var model = {};

  beforeEach(function() {
    setFixtures(sandbox());
    jasmine.Ajax.install();
    jasmine.clock().install();

    session = new scout.Session($('#sandbox'), '1.1');
    session.init();

    var genericFactories = [{
      objectType: 'Generic',
      create: function() {
        return new scout.ModelAdapter();
      }}, {
      objectType: 'HasChildAdapter',
      create: function() {
        var adapter = new scout.ModelAdapter();
        adapter._addAdapterProperties('childAdapter');
        return adapter;
      }}, {
      objectType: 'HasChildAdapters',
      create: function() {
        var adapter = new scout.ModelAdapter();
        adapter._addAdapterProperties('childAdapters');
        return adapter;
      }
    }];
    session.objectFactory.register(genericFactories);
  });

  afterEach(function() {
    session = null;
    jasmine.Ajax.uninstall();
    jasmine.clock().uninstall();
  });

  function createGenericModel() {
    return createSimpleModel('Generic');
  }

  function createModelAdapter(model, adapterProps) {
    var adapter = new scout.ModelAdapter();
    adapter._addAdapterProperties(adapterProps);
    adapter.init(model, session);
    return adapter;
  }

  it("can handle properties in any order", function() {
    var event;

    // Create a dummy object
    var modelAdapter = new scout.ModelAdapter();
    model.id = '2';
    modelAdapter.init(model, session);
    session.registerModelAdapter(modelAdapter);

    // Send a dummy event to this object which contains both a new object and a id-only ref to that new object
    event = new scout.Event('property', '2', {
      'properties': {
        'x1': 'val1',
        'x2': 'val2',
        'o1': {
          'id': '3',
          'objectType': 'GroupBox',
          'visible': true
        },
        'o2': {
          'id': '3'
        }
      }
    });
    session._processEvents([event]);

    expect(modelAdapter.x1).toBe('val1');
    expect(modelAdapter.x2).toBe('val2');
    expect(modelAdapter.o1).toBeDefined();
    expect(modelAdapter.o1.id).toBe('3');
    expect(modelAdapter.o2).toBeDefined();
    expect(modelAdapter.o2.id).toBe('3');

    // Now send a second event, but now send the id-only ref first (in o1).
    event = new scout.Event('property', '2', {
      'properties': {
        'x2': 'val20',
        'x1': 'val10',
        'o1': {
          'id': '4'
        },
        'o2': {
          'id': '4',
          'objectType': 'GroupBox',
          'visible': false
        }
      }
    });
    session._processEvents([event]);

    expect(modelAdapter.x1).toBe('val10');
    expect(modelAdapter.x2).toBe('val20');
    expect(modelAdapter.o1).toBeDefined();
    expect(modelAdapter.o1.id).toBe('4');
    expect(modelAdapter.o2).toBeDefined();
    expect(modelAdapter.o2.id).toBe('4');
  });

  it("_syncPropertiesOnPropertyChange calls _sync* method or sets property", function() {
    var adapter = new scout.ModelAdapter(),
      oldValues = {},
      newValues = {
        foo: 6,
        bar: 7
      };
    adapter.foo = 1;
    adapter.bar = 2;
    adapter._syncFoo = function(value) {
      this.foo = value;
    };
    spyOn(adapter, '_syncFoo').and.callThrough();
    adapter._syncPropertiesOnPropertyChange(oldValues, newValues);
    expect(adapter.foo).toBe(6);
    expect(adapter.bar).toBe(7);
    expect(adapter._syncFoo).toHaveBeenCalled();
    expect(oldValues.foo).toBe(1);
    expect(oldValues.bar).toBe(2);
  });

  describe("_renderPropertiesOnPropertyChange", function() {

    it("for non-adapter property -> expects a _render* method", function() {
      var adapter = new scout.ModelAdapter(),
        $div = $('<div>');
      adapter._renderFoo = function(value) {
        $div.text(value);
      };
      adapter._renderPropertiesOnPropertyChange({}, {foo: 'bar'});
      expect($div.text()).toBe('bar');
    });

    it("for non-adapter property -> throw when _render* method does not exist", function() {
      var adapter = new scout.ModelAdapter();
      expect(adapter._renderPropertiesOnPropertyChange.bind(adapter, {}, {foo: 'bar'})).toThrow();
    });

  });

  describe("init", function() {

    it("check if model properties are copied to adapter", function() {
      var model = { foo: 6 },
        adapter = new scout.ModelAdapter();
      model.id = '123';
      adapter.init(model, session);
      expect(adapter.foo).toBe(6);
    });

  });

  describe("destroy", function() {

    it("destroys the adapter and its children", function() {
      var adapter = createModelAdapter(createGenericModel(), 'childAdapter');
      var model = createGenericModel();

      var message = {
        adapterData : createAdapterData(model),
        events: [createPropertyChangeEvent(adapter, {childAdapter: model.id})]
      };
      session._processSuccessResponse(message);

      expect(session.getModelAdapter(adapter.id)).toBe(adapter);
      expect(adapter.childAdapter).toBeTruthy();
      expect(session.getModelAdapter(model.id)).toBe(adapter.childAdapter);

      adapter.destroy();

      expect(session.getModelAdapter(adapter.id)).toBeFalsy();
      expect(session.getModelAdapter(model.id)).toBeFalsy();
    });

    it("does not destroy children, which are globally used", function() {
      var adapter = createModelAdapter(createGenericModel(), 'childAdapter');
      var model = createGenericModel();
      model.parent = session.rootAdapter.id;

      var message = {
        adapterData : createAdapterData(model),
        events: [createPropertyChangeEvent(adapter, {childAdapter: model.id})]
      };
      session._processSuccessResponse(message);

      expect(session.getModelAdapter(adapter.id)).toBe(adapter);
      expect(adapter.childAdapter).toBeTruthy();
      expect(session.getModelAdapter(model.id)).toBe(adapter.childAdapter);

      adapter.destroy();

      expect(session.getModelAdapter(adapter.id)).toBeFalsy();
      // Child adapter still exists
      expect(session.getModelAdapter(model.id)).toBeTruthy();
    });

  });

  describe("onModelPropertyChange", function() {

    describe("adapter", function() {

      it("creates and registers the new adapter", function() {
        var adapter = createModelAdapter(createGenericModel(), 'childAdapter');
        var model = createGenericModel();

        var message = {
          adapterData : createAdapterData(model),
          events: [createPropertyChangeEvent(adapter, {childAdapter: model.id})]
        };
        session._processSuccessResponse(message);

        expect(adapter.childAdapter).toBeTruthy();
        expect(session.getModelAdapter(model.id)).toBe(adapter.childAdapter);
      });

      it("destroys the old adapter", function() {
        var adapter = createModelAdapter(createGenericModel(), 'childAdapter');
        var model1 = createGenericModel();
        var model2 = createGenericModel();

        var message = {
          adapterData : createAdapterData(model1),
          events: [createPropertyChangeEvent(adapter, {childAdapter: model1.id})]
        };
        session._processSuccessResponse(message);

        expect(adapter.childAdapter).toBeTruthy();
        expect(session.getModelAdapter(model1.id)).toBe(adapter.childAdapter);

        message = {
          adapterData : createAdapterData(model2),
          events: [createPropertyChangeEvent(adapter, {childAdapter: model2.id})]
        };
        session._processSuccessResponse(message);

        expect(session.getModelAdapter(model2.id)).toBe(adapter.childAdapter);
        expect(session.getModelAdapter(model1.id)).toBeFalsy();
      });

    });

    describe("adapters", function() {

      it("creates and registers adapters", function() {
        var adapter = createModelAdapter(createGenericModel(), 'childAdapters');
        var model1 = createGenericModel();
        var model2 = createGenericModel();

        var message = {
          adapterData : createAdapterData([model1, model2]),
          events: [createPropertyChangeEvent(adapter, {childAdapters: [model1.id, model2.id]})]
        };
        session._processSuccessResponse(message);

        expect(adapter.childAdapters[0]).toBeTruthy();
        expect(adapter.childAdapters[1]).toBeTruthy();
        expect(session.getModelAdapter(model1.id)).toBe(adapter.childAdapters[0]);
        expect(session.getModelAdapter(model2.id)).toBe(adapter.childAdapters[1]);
      });

      it("destroys the old adapters", function() {
        var adapter = createModelAdapter(createGenericModel(), 'childAdapters');
        var model1 = createGenericModel();
        var model2 = createGenericModel();

        var message = {
          adapterData : createAdapterData([model1, model2]),
          events: [createPropertyChangeEvent(adapter, {childAdapters: [model1.id, model2.id]})]
        };
        session._processSuccessResponse(message);

        expect(adapter.childAdapters[0]).toBeTruthy();
        expect(adapter.childAdapters[1]).toBeTruthy();
        expect(session.getModelAdapter(model1.id)).toBe(adapter.childAdapters[0]);
        expect(session.getModelAdapter(model2.id)).toBe(adapter.childAdapters[1]);

        message = {
          events: [createPropertyChangeEvent(adapter, {childAdapters: [model2.id]})]
        };
        session._processSuccessResponse(message);

        expect(adapter.childAdapters.length).toBe(1);
        expect(adapter.childAdapters[0]).toBeTruthy();
        expect(session.getModelAdapter(model2.id)).toBe(adapter.childAdapters[0]);
        expect(session.getModelAdapter(model1.id)).toBeFalsy();
      });

      it("destroys the old and creates the new adapters if the array contains both", function() {
        var adapter = createModelAdapter(createGenericModel(), 'childAdapters');
        var model1 = createGenericModel();
        var model2 = createGenericModel();
        var model3 = createGenericModel();

        var message = {
          adapterData : createAdapterData([model1, model2]),
          events: [createPropertyChangeEvent(adapter, {childAdapters: [model1.id, model2.id]})]
        };
        session._processSuccessResponse(message);

        expect(adapter.childAdapters[0]).toBeTruthy();
        expect(adapter.childAdapters[1]).toBeTruthy();
        expect(session.getModelAdapter(model1.id)).toBe(adapter.childAdapters[0]);
        expect(session.getModelAdapter(model2.id)).toBe(adapter.childAdapters[1]);

        message = {
          adapterData : createAdapterData(model3),
          events: [createPropertyChangeEvent(adapter, {childAdapters: [model2.id, model3.id]})]
        };
        session._processSuccessResponse(message);

        expect(adapter.childAdapters.length).toBe(2);
        expect(adapter.childAdapters[0]).toBeTruthy();
        expect(adapter.childAdapters[1]).toBeTruthy();
        expect(session.getModelAdapter(model2.id)).toBe(adapter.childAdapters[0]);
        expect(session.getModelAdapter(model3.id)).toBe(adapter.childAdapters[1]);
        expect(session.getModelAdapter(model1.id)).toBeFalsy();
      });

    });

  });

});
