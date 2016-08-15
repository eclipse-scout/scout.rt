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

  /**
   * Creates a model and stores it as adapterData in the session.
   */
  function createModel(model) {
    model = scout.nvl(model, {});
    if (!model.objectType) {
      model.objectType = 'NullWidget';
    }
    model = $.extend(createSimpleModel(model.objectType, session), model);
    session._copyAdapterData(createAdapterData(model));
    return model;
  }

  /**
   * Creates widget and remote-adapter.
   */
  function createWidget(model) {
    model = createModel(model);
    return session.getOrCreateWidget(model.id, session.desktop);
  }

  it('can handle properties in any order', function() {
    var widget = createWidget({id: '2'});

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
    var widget = createWidget({
      foo: 1,
      bar: 2});
    widget.setFoo = function(value) {
      this.foo = value;
    };
    var newValues = {
        foo: 6,
        bar: 7
      };
    spyOn(widget, 'setFoo').and.callThrough();
    spyOn(widget, 'setProperty').and.callThrough();

    widget.remoteAdapter._syncPropertiesOnPropertyChange(newValues);
    expect(widget.foo).toBe(6);
    expect(widget.bar).toBe(7);
    expect(widget.setFoo).toHaveBeenCalled(); // for property 'foo'
    expect(widget.setProperty).toHaveBeenCalled(); // for property 'bar'
  });

  describe('_renderPropertiesOnPropertyChange', function() {

    it('for non-adapter property -> expects a _render* method', function() {
      var widget = createWidget();
      var $div = $('<div>');
      widget.render($sandbox);
      widget._renderFoo = function() {
        $div.text(this.foo);
      };
      widget.remoteAdapter._syncPropertiesOnPropertyChange({
        foo: 'bar'
      });
      expect($div.text()).toBe('bar');
    });

    it('for non-adapter property -> throw when _render* method does not exist', function() {
      var widget = createWidget();
      var adapter = widget.remoteAdapter;
      widget.render($sandbox);
      expect(adapter._syncPropertiesOnPropertyChange.bind(adapter, {
        foo: 'bar'
      })).toThrow();
    });

  });

  describe('init', function() {

    it('copies properties to widget', function() {
      var widget = createWidget({foo: 6});
      expect(widget.foo).toBe(6);
    });

    it('sets default values', function() {
      // model does not contain a property visible
      var model = createModel({objectType: 'Button'});
      expect(model.visible).toBe(undefined);

      // because visible is a default property, the property is set on the widget/adapter
      var widget = createWidget(model);
      expect(widget.visible).toBe(true);

      // verify that the original model is not modified
      expect(model.visible).toBe(undefined);
    });

  });

  describe('destroy', function() {

    // FIXME [awe] 6.1 tests umbauen sobald klar ist wohin die adapterProperties gehören und wie sie heissen sollen
    var widget, adapter, childModel;

    beforeEach(function() {
      widget = createWidget();
      adapter = widget.remoteAdapter;
      childModel = createModel();
    });

    it('destroys the adapter and its children', function() {
      var message = {
        adapterData: createAdapterData(childModel),
        events: [createPropertyChangeEvent(adapter, {
          childWidget: childModel.id
        })]
      };
      session._processSuccessResponse(message);

      expect(session.getModelAdapter(adapter.id)).toBe(adapter);
      expect(widget.childWidget).toBeTruthy();
      expect(session.getModelAdapter(childModel.id)).toBe(widget.childWidget.remoteAdapter);

      adapter.destroy();

      expect(session.getModelAdapter(adapter.id)).toBeFalsy();
      expect(session.getModelAdapter(childModel.id)).toBeFalsy();
    });

    it('does not destroy children, which are globally used', function() {
      childModel.owner = session.rootAdapter.id;

      var message = {
        adapterData: createAdapterData(childModel),
        events: [createPropertyChangeEvent(adapter, {
          childWidget: childModel.id
        })]
      };
      session._processSuccessResponse(message);

      expect(session.getModelAdapter(adapter.id)).toBe(adapter);
      expect(widget.childWidget).toBeTruthy();
      expect(session.getModelAdapter(childModel.id)).toBe(widget.childWidget.remoteAdapter);

      adapter.destroy();

      expect(session.getModelAdapter(adapter.id)).toBeFalsy();
      // Child adapter still exists
      expect(session.getModelAdapter(childModel.id)).toBeTruthy();
    });

  });

  describe('onModelPropertyChange', function() {

    var widget, adapter, childModel, childModel2;

    beforeEach(function() {
      widget = createWidget();
      adapter = widget.remoteAdapter;
      childModel = createModel();
      childModel2 = createModel();
    });

    describe('adapter', function() {

      it('creates and registers the new adapter', function() {
        var message = {
          adapterData: createAdapterData(childModel),
          events: [createPropertyChangeEvent(adapter, {
            childWidget: childModel.id
          })]
        };
        session._processSuccessResponse(message);

        expect(widget.childWidget).toBeTruthy();
        expect(session.getModelAdapter(childModel.id)).toBe(widget.childWidget.remoteAdapter);
      });

      it('destroys the old adapter', function() {
        var message = {
          adapterData: createAdapterData(childModel),
          events: [createPropertyChangeEvent(adapter, {
            childWidget: childModel.id
          })]
        };
        session._processSuccessResponse(message);

        expect(widget.childWidget).toBeTruthy();
        expect(session.getModelAdapter(childModel.id)).toBe(widget.childWidget.remoteAdapter);

        message = {
          adapterData: createAdapterData(childModel2),
          events: [createPropertyChangeEvent(adapter, {
            childWidget: childModel2.id
          })]
        };
        session._processSuccessResponse(message);

        expect(session.getModelAdapter(childModel2.id)).toBe(widget.childWidget.remoteAdapter);
        expect(session.getModelAdapter(childModel.id)).toBeFalsy();
      });

    });

    describe('adapters', function() {

      it('creates and registers adapters', function() {
        var message = {
          adapterData: createAdapterData([childModel, childModel2]),
          events: [createPropertyChangeEvent(adapter, {
            childWidget: [childModel.id, childModel2.id]
          })]
        };
        session._processSuccessResponse(message);

        expect(widget.childWidget[0]).toBeTruthy();
        expect(widget.childWidget[1]).toBeTruthy();
        expect(session.getModelAdapter(childModel.id)).toBe(widget.childWidget[0].remoteAdapter);
        expect(session.getModelAdapter(childModel2.id)).toBe(widget.childWidget[1].remoteAdapter);
      });

      it('destroys the old adapters', function() {
        var message = {
          adapterData: createAdapterData([childModel, childModel2]),
          events: [createPropertyChangeEvent(adapter, {
            childWidget: [childModel.id, childModel2.id]
          })]
        };
        session._processSuccessResponse(message);
        expect(widget.childWidget[0]).toBeTruthy();
        expect(widget.childWidget[1]).toBeTruthy();
        expect(session.getModelAdapter(childModel.id)).toBe(widget.childWidget[0].remoteAdapter);
        expect(session.getModelAdapter(childModel2.id)).toBe(widget.childWidget[1].remoteAdapter);

        var childWidget = widget.childWidget[0];
        var childWidget2 = widget.childWidget[1];
        message = {
          events: [createPropertyChangeEvent(adapter, {
            childWidget: [childModel2.id]
          })]
        };
        session._processSuccessResponse(message);
        expect(widget.childWidget.length).toBe(1);
        expect(widget.childWidget[0]).toBeTruthy();
        expect(session.getModelAdapter(childModel.id)).toBeFalsy();
        expect(childWidget.destroyed).toBe(true);
        expect(session.getModelAdapter(childModel2.id)).toBe(widget.childWidget[0].remoteAdapter);
        expect(childWidget2.destroyed).toBe(false);
      });

      it('destroys the old and creates the new adapters if the array contains both', function() {
        var childModel3 = createModel();
        var message = {
          adapterData: createAdapterData([childModel, childModel2]),
          events: [createPropertyChangeEvent(adapter, {
            childWidget: [childModel.id, childModel2.id]
          })]
        };
        session._processSuccessResponse(message);

        expect(widget.childWidget[0]).toBeTruthy();
        expect(widget.childWidget[1]).toBeTruthy();
        expect(session.getModelAdapter(childModel.id)).toBe(widget.childWidget[0].remoteAdapter);
        expect(session.getModelAdapter(childModel2.id)).toBe(widget.childWidget[1].remoteAdapter);

        message = {
          adapterData: createAdapterData(childModel3),
          events: [createPropertyChangeEvent(adapter, {
            childWidget: [childModel2.id, childModel3.id]
          })]
        };
        session._processSuccessResponse(message);

        expect(widget.childWidget.length).toBe(2);
        expect(widget.childWidget[0]).toBeTruthy();
        expect(widget.childWidget[1]).toBeTruthy();
        expect(session.getModelAdapter(childModel2.id)).toBe(widget.childWidget[0].remoteAdapter);
        expect(session.getModelAdapter(childModel3.id)).toBe(widget.childWidget[1].remoteAdapter);
        expect(session.getModelAdapter(childModel.id)).toBeFalsy();
      });

    });

  });

});
