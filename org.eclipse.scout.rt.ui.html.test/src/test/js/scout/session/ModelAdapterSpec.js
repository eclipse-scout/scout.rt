/*******************************************************************************
 * Copyright (c) 2014-2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
describe('ModelAdapter', function() {

  var session, $sandbox, myObjectFactory,
    model = {},
    originalObjectFactory = scout.objectFactory;

  beforeEach(function() {
    setFixtures(sandbox());
    jasmine.Ajax.install();
    jasmine.clock().install();
    session = sandboxSession();
    session.init();
    uninstallUnloadHandlers(session);
    $sandbox = $('#sandbox');

    // Create a private object factory used for these tests
    myObjectFactory = new scout.ObjectFactory();
    myObjectFactory.register('Generic', function() {
      return new scout.ModelAdapter();
    });
    myObjectFactory.register('HasChildAdapter', function() {
      var adapter = new scout.ModelAdapter();
      adapter._addAdapterProperties('childAdapter');
      return adapter;
    });
    myObjectFactory.register('HasChildAdapters', function() {
      var adapter = new scout.ModelAdapter();
      adapter._addAdapterProperties('childAdapters');
      return adapter;
    });
    scout.objectFactory = myObjectFactory;
  });

  afterEach(function() {
    session = null;
    jasmine.Ajax.uninstall();
    jasmine.clock().uninstall();
    scout.objectFactory = originalObjectFactory;
  });

  function createGenericModel() {
    return createSimpleModel('Generic', session);
  }

  function createModelAdapter(model, adapterProps) {
    model = $.extend(createSimpleModel('NullModelAdapter', session), model);
    var adapter = scout.create('NullModelAdapter', model);
    adapter._addAdapterProperties(adapterProps);
    return adapter;
  }

  it('can handle properties in any order', function() {
    var adapter = createModelAdapter({id: '2'});
    var widget = adapter.getOrCreateWidget(session.desktop);

    // Send a dummy event to this object which contains both a new object and a id-only ref to that new object
    var event = new scout.Event('2', 'property', {
      properties: {
        x1: 'val1',
        x2: 'val2',
        o1: {
          id: '3',
          objectType: 'GroupBox',
          visible: true
        },
        o2: {
          id: '3'
        }
      }
    });
    session._processEvents([event]);

    expect(widget.x1).toBe('val1');
    expect(widget.x2).toBe('val2');
    expect(widget.o1).toBeDefined();
    expect(widget.o1.id).toBe('3');
    expect(widget.o2).toBeDefined();
    expect(widget.o2.id).toBe('3');

    // Now send a second event, but now send the id-only ref first (in o1).
    event = new scout.Event('2', 'property', {
      properties: {
        x2: 'val20',
        x1: 'val10',
        o1: {
          id: '4'
        },
        o2: {
          id: '4',
          objectType: 'GroupBox',
          visible: false
        }
      }
    });
    session._processEvents([event]);

    expect(widget.x1).toBe('val10');
    expect(widget.x2).toBe('val20');
    expect(widget.o1).toBeDefined();
    expect(widget.o1.id).toBe('4');
    expect(widget.o2).toBeDefined();
    expect(widget.o2.id).toBe('4');
  });

  it('_syncPropertiesOnPropertyChange calls set* methods or _setProperty method', function() {
    var adapter = createModelAdapter({
      id: '2',
      foo: 1,
      bar: 2});
    var widget = adapter.getOrCreateWidget(session.desktop);
    widget.setFoo = function(value) {
      this.foo = value;
    };
    var newValues = {
        foo: 6,
        bar: 7
      };
    spyOn(widget, 'setFoo').and.callThrough();
    spyOn(widget, 'setProperty').and.callThrough();

    adapter._syncPropertiesOnPropertyChange(newValues);
    expect(widget.foo).toBe(6);
    expect(widget.bar).toBe(7);
    expect(widget.setFoo).toHaveBeenCalled(); // for property 'foo'
    expect(widget.setProperty).toHaveBeenCalled(); // for property 'bar'
  });

  describe('_renderPropertiesOnPropertyChange', function() {

    it('for non-adapter property -> expects a _render* method', function() {
      var adapter = createModelAdapter();
      var widget = adapter.getOrCreateWidget(session.desktop);
      var $div = $('<div>');
      widget.render($sandbox);
      widget._renderFoo = function() {
        $div.text(this.foo);
      };
      adapter._syncPropertiesOnPropertyChange({
        foo: 'bar'
      });
      expect($div.text()).toBe('bar');
    });

    it("for non-adapter property -> throw when _render* method does not exist", function() {
      var adapter = new scout.ModelAdapter();
      expect(adapter._renderPropertiesOnPropertyChange.bind(adapter, {}, {
        foo: 'bar'
      })).toThrow();
    });

  });

  describe("init", function() {

    it("copies properties to adapter", function() {
      var model = {
          foo: 6
        },
        adapter = new scout.ModelAdapter();
      model.id = '123';
      model.parent = new scout.NullWidget();
      model.session = session;
      adapter.init(model);
      expect(adapter.foo).toBe(6);
    });

    it("sets default values", function() {
      // model does not contain a property visible
      var model = createSimpleModel('Button', session);
      expect(model.visible).toBe(undefined);

      // because visible is a default property, the property is set on the adapter
      var adapter = createModelAdapter(model);
      expect(adapter.visible).toBe(true);

      // verify that the original model is not modified
      expect(model.visible).toBe(undefined);
    });

  });

  describe('destroy', function() {

    it('destroys the adapter and its children', function() {
      var adapter = createModelAdapter(createGenericModel(), 'childAdapter');
      var model = createGenericModel();

      var message = {
        adapterData: createAdapterData(model),
        events: [createPropertyChangeEvent(adapter, {
          childAdapter: model.id
        })]
      };
      session._processSuccessResponse(message);

      expect(session.getModelAdapter(adapter.id)).toBe(adapter);
      expect(adapter.childAdapter).toBeTruthy();
      expect(session.getModelAdapter(model.id)).toBe(adapter.childAdapter);

      adapter.destroy();

      expect(session.getModelAdapter(adapter.id)).toBeFalsy();
      expect(session.getModelAdapter(model.id)).toBeFalsy();
    });

    it('does not destroy children, which are globally used', function() {
      var adapter = createModelAdapter(createGenericModel(), 'childAdapter');
      var model = createGenericModel();
      model.owner = session.rootAdapter.id;

      var message = {
        adapterData: createAdapterData(model),
        events: [createPropertyChangeEvent(adapter, {
          childAdapter: model.id
        })]
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

  describe('_firePropertyChange', function() {

    var propertyChangeEvent, adapter;

    beforeEach(function() {
      adapter = createModelAdapter(createGenericModel(), 'childAdapter');
    });

    function firePropertyChange(oldValue, newValue) {
      adapter.on('propertyChange', function(event) {
        propertyChangeEvent = event;
      });
      adapter._firePropertyChange('selected', oldValue, newValue);
    }

    it('fires the expected event object', function() {
      firePropertyChange(false, true);

      expect(scout.objects.countOwnProperties(propertyChangeEvent.oldProperties)).toBe(1);
      expect(scout.objects.countOwnProperties(propertyChangeEvent.newProperties)).toBe(1);
      expect(propertyChangeEvent.changedProperties.length).toBe(1);

      expect(propertyChangeEvent.oldProperties.selected).toBe(false);
      expect(propertyChangeEvent.newProperties.selected).toBe(true);
      expect(propertyChangeEvent.changedProperties[0]).toBe('selected');
    });

    // FIXME awe: discuss with B.SH - when a property has _not_ changed, should it be
    // fired as new/old property anyway? When no property has changed, should the propertyChange
    // event be fired anyway?
    it('changedProperties is only set when new and old value are not equals', function() {
      firePropertyChange(true, true);
      expect(scout.objects.countOwnProperties(propertyChangeEvent.oldProperties)).toBe(1);
      expect(scout.objects.countOwnProperties(propertyChangeEvent.newProperties)).toBe(1);
      expect(propertyChangeEvent.changedProperties.length).toBe(0);
    });

  });

  describe('onModelPropertyChange', function() {

    describe('adapter', function() {

      it('creates and registers the new adapter', function() {
        var adapter = createModelAdapter(createGenericModel(), 'childAdapter');
        var model = createGenericModel();

        var message = {
          adapterData: createAdapterData(model),
          events: [createPropertyChangeEvent(adapter, {
            childAdapter: model.id
          })]
        };
        session._processSuccessResponse(message);

        expect(adapter.childAdapter).toBeTruthy();
        expect(session.getModelAdapter(model.id)).toBe(adapter.childAdapter);
      });

      it('destroys the old adapter', function() {
        var adapter = createModelAdapter(createGenericModel(), 'childAdapter');
        var model1 = createGenericModel();
        var model2 = createGenericModel();

        var message = {
          adapterData: createAdapterData(model1),
          events: [createPropertyChangeEvent(adapter, {
            childAdapter: model1.id
          })]
        };
        session._processSuccessResponse(message);

        expect(adapter.childAdapter).toBeTruthy();
        expect(session.getModelAdapter(model1.id)).toBe(adapter.childAdapter);

        message = {
          adapterData: createAdapterData(model2),
          events: [createPropertyChangeEvent(adapter, {
            childAdapter: model2.id
          })]
        };
        session._processSuccessResponse(message);

        expect(session.getModelAdapter(model2.id)).toBe(adapter.childAdapter);
        expect(session.getModelAdapter(model1.id)).toBeFalsy();
      });

    });

    describe('adapters', function() {

      it('creates and registers adapters', function() {
        var adapter = createModelAdapter(createGenericModel(), 'childAdapters');
        var model1 = createGenericModel();
        var model2 = createGenericModel();

        var message = {
          adapterData: createAdapterData([model1, model2]),
          events: [createPropertyChangeEvent(adapter, {
            childAdapters: [model1.id, model2.id]
          })]
        };
        session._processSuccessResponse(message);

        expect(adapter.childAdapters[0]).toBeTruthy();
        expect(adapter.childAdapters[1]).toBeTruthy();
        expect(session.getModelAdapter(model1.id)).toBe(adapter.childAdapters[0]);
        expect(session.getModelAdapter(model2.id)).toBe(adapter.childAdapters[1]);
      });

      it('destroys the old adapters', function() {
        var adapter = createModelAdapter(createGenericModel(), 'childAdapters');
        var model1 = createGenericModel();
        var model2 = createGenericModel();

        var message = {
          adapterData: createAdapterData([model1, model2]),
          events: [createPropertyChangeEvent(adapter, {
            childAdapters: [model1.id, model2.id]
          })]
        };
        session._processSuccessResponse(message);

        expect(adapter.childAdapters[0]).toBeTruthy();
        expect(adapter.childAdapters[1]).toBeTruthy();
        expect(session.getModelAdapter(model1.id)).toBe(adapter.childAdapters[0]);
        expect(session.getModelAdapter(model2.id)).toBe(adapter.childAdapters[1]);

        message = {
          events: [createPropertyChangeEvent(adapter, {
            childAdapters: [model2.id]
          })]
        };
        session._processSuccessResponse(message);

        expect(adapter.childAdapters.length).toBe(1);
        expect(adapter.childAdapters[0]).toBeTruthy();
        expect(session.getModelAdapter(model2.id)).toBe(adapter.childAdapters[0]);
        expect(session.getModelAdapter(model1.id)).toBeFalsy();
      });

      it('destroys the old and creates the new adapters if the array contains both', function() {
        var adapter = createModelAdapter(createGenericModel(), 'childAdapters');
        var model1 = createGenericModel();
        var model2 = createGenericModel();
        var model3 = createGenericModel();

        var message = {
          adapterData: createAdapterData([model1, model2]),
          events: [createPropertyChangeEvent(adapter, {
            childAdapters: [model1.id, model2.id]
          })]
        };
        session._processSuccessResponse(message);

        expect(adapter.childAdapters[0]).toBeTruthy();
        expect(adapter.childAdapters[1]).toBeTruthy();
        expect(session.getModelAdapter(model1.id)).toBe(adapter.childAdapters[0]);
        expect(session.getModelAdapter(model2.id)).toBe(adapter.childAdapters[1]);

        message = {
          adapterData: createAdapterData(model3),
          events: [createPropertyChangeEvent(adapter, {
            childAdapters: [model2.id, model3.id]
          })]
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
