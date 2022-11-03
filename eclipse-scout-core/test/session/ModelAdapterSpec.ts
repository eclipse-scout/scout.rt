/*
 * Copyright (c) 2010-2022 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
import {FormSpecHelper} from '../../src/testing/index';
import {Button, GroupBox, ModelAdapter, RemoteEvent, scout, Widget, WidgetModel} from '../../src/index';
import {RemoteResponse} from '../../src/session/Session';

describe('ModelAdapter', () => {

  let session: SandboxSession, $sandbox: JQuery, helper: FormSpecHelper;

  beforeEach(() => {
    setFixtures(sandbox());
    jasmine.Ajax.install();
    jasmine.clock().install();
    session = sandboxSession();
    helper = new FormSpecHelper(session);
    uninstallUnloadHandlers(session);
    $sandbox = $('#sandbox');

    // // Create a private object factory used for these tests
    // myObjectFactory = new ObjectFactory();
    // myObjectFactory.register('Generic', () => {
    //   return new ModelAdapter();
    // });
    // myObjectFactory.register('HasChildAdapter', () => {
    //   let adapter = new ModelAdapter();
    //   adapter._addWidgetProperties('childAdapter');
    //   return adapter;
    // });
    // myObjectFactory.register('HasChildAdapters', () => {
    //   let adapter = new ModelAdapter();
    //   adapter._addWidgetProperties('childAdapters');
    //   return adapter;
    // });
    // // @ts-expect-error
    // ObjectFactory._set(myObjectFactory);
  });

  afterEach(() => {
    session = null;
    jasmine.Ajax.uninstall();
    jasmine.clock().uninstall();
    // // @ts-expect-error
    // ObjectFactory._set(originalObjectFactory);
  });

  /**
   * Creates a model and stores it as adapterData in the session.
   */
  function createModel(model?: Omit<WidgetModel, 'parent'>): WidgetModel {
    model = scout.nvl(model, {});
    if (!model.objectType) {
      model.objectType = 'NullWidget';
    }
    let m = $.extend(createSimpleModel(model.objectType, session), model) as WidgetModel;
    registerAdapterData(m, session);
    return m;
  }

  /**
   * Creates widget and remote-adapter.
   */
  function createWidget(model?: Omit<WidgetModel, 'parent'>): Widget {
    model = createModel(model);
    return session.getOrCreateWidget(model.id, session.desktop);
  }

  it('can handle properties in any order', () => {
    let widget = createWidget({id: '2'});

    // Send a dummy event to this object which contains both a new object and an id-only ref to that new object
    let event = new RemoteEvent('2', 'property', {
      properties: {
        x1: 'val1',
        x2: 'val2',
        o1: {
          id: '3',
          objectType: GroupBox,
          visible: true
        },
        o2: {
          id: '3'
        }
      }
    });
    session._processEvents([event]);

    expect(widget['x1']).toBe('val1');
    expect(widget['x2']).toBe('val2');
    let o1 = widget['o1'];
    expect(o1).toBeDefined();
    expect(o1.id).toBe('3');
    let o2 = widget['o2'];
    expect(o2).toBeDefined();
    expect(o2.id).toBe('3');

    // Now send a second event, but now send the id-only ref first (in o1).
    event = new RemoteEvent('2', 'property', {
      properties: {
        x2: 'val20',
        x1: 'val10',
        o1: {
          id: '4'
        },
        o2: {
          id: '4',
          objectType: GroupBox,
          visible: false
        }
      }
    });
    session._processEvents([event]);

    expect(widget['x1']).toBe('val10');
    expect(widget['x2']).toBe('val20');
    o1 = widget['o1'];
    expect(o1).toBeDefined();
    expect(o1.id).toBe('4');
    o2 = widget['o2'];
    expect(o2).toBeDefined();
    expect(o2.id).toBe('4');
  });

  it('_syncPropertiesOnPropertyChange calls set* methods or setProperty method', () => {
    let widget = createWidget({
      foo: 1,
      bar: 2
    }) as WidgetModel & { setFoo?(value): void };
    widget['setFoo'] = function(value) {
      this.foo = value;
    };
    let newValues = {
      foo: 6,
      bar: 7
    };
    spyOn(widget, 'setFoo').and.callThrough();
    spyOn(widget, 'setProperty').and.callThrough();

    widget.modelAdapter._syncPropertiesOnPropertyChange(newValues);
    expect(widget.foo).toBe(6);
    expect(widget.bar).toBe(7);
    expect(widget.setFoo).toHaveBeenCalled(); // for property 'foo'
    expect(widget.setProperty).toHaveBeenCalled(); // for property 'bar'
  });

  describe('init', () => {

    it('copies properties to widget', () => {
      let widget = createWidget({foo: 6});
      expect(widget['foo']).toBe(6);
    });

    it('sets default values', () => {
      // model does not contain a property visible
      let model = createModel({objectType: 'Button'});
      expect(model.visible).toBe(undefined);

      // because visible is a default property, the property is set on the widget/adapter
      let widget = createWidget(model);
      expect(widget.visible).toBe(true);

      // verify that the original model is not modified
      expect(model.visible).toBe(undefined);
    });

  });

  describe('destroy', () => {

    let widget, adapter, childModel;

    beforeEach(() => {
      widget = createWidget();
      adapter = widget.modelAdapter;
      childModel = createModel();
    });

    it('destroys the adapter and its children', () => {
      let message = {
        adapterData: mapAdapterData(childModel),
        events: [createPropertyChangeEvent(adapter, {
          childWidget: childModel.id
        })]
      };
      session._processSuccessResponse(message);

      expect(session.getModelAdapter(adapter.id)).toBe(adapter);
      expect(widget.childWidget).toBeTruthy();
      expect(session.getModelAdapter(childModel.id)).toBe(widget.childWidget.modelAdapter);

      adapter.destroy();

      expect(session.getModelAdapter(adapter.id)).toBeFalsy();
      expect(session.getModelAdapter(childModel.id)).toBeFalsy();
    });

    it('does not destroy children, which are globally used', () => {
      childModel.global = true;

      let message = {
        adapterData: mapAdapterData(childModel),
        events: [createPropertyChangeEvent(adapter, {
          childWidget: childModel.id
        })]
      };
      session._processSuccessResponse(message);

      expect(session.getModelAdapter(adapter.id)).toBe(adapter);
      expect(widget.childWidget).toBeTruthy();
      expect(session.getModelAdapter(childModel.id)).toBe(widget.childWidget.modelAdapter);

      adapter.destroy();

      expect(session.getModelAdapter(adapter.id)).toBeFalsy();
      // Child adapter still exists
      expect(session.getModelAdapter(childModel.id)).toBeTruthy();
    });

  });

  describe('onModelPropertyChange', () => {

    let widget, adapter, childModel, childModel2;

    beforeEach(() => {
      widget = createWidget();
      adapter = widget.modelAdapter;
      childModel = createModel();
      childModel2 = createModel();
    });

    describe('adapter', () => {

      it('creates and registers the new adapter', () => {
        let message = {
          adapterData: mapAdapterData(childModel),
          events: [createPropertyChangeEvent(adapter, {
            childWidget: childModel.id
          })]
        };
        session._processSuccessResponse(message);

        expect(widget.childWidget).toBeTruthy();
        expect(session.getModelAdapter(childModel.id)).toBe(widget.childWidget.modelAdapter);
      });

      it('destroys the old adapter', () => {
        let message = {
          adapterData: mapAdapterData(childModel),
          events: [createPropertyChangeEvent(adapter, {
            childWidget: childModel.id
          })]
        };
        session._processSuccessResponse(message);

        expect(widget.childWidget).toBeTruthy();
        expect(session.getModelAdapter(childModel.id)).toBe(widget.childWidget.modelAdapter);

        message = {
          adapterData: mapAdapterData(childModel2),
          events: [createPropertyChangeEvent(adapter, {
            childWidget: childModel2.id
          })]
        };
        session._processSuccessResponse(message);

        expect(session.getModelAdapter(childModel2.id)).toBe(widget.childWidget.modelAdapter);
        expect(session.getModelAdapter(childModel.id)).toBeFalsy();
      });

    });

    describe('filters', () => {

      let widget, adapter;

      beforeEach(() => {
        jasmine.Ajax.requests.reset();
        widget = createWidget();
        adapter = widget.modelAdapter;
        adapter._addRemoteProperties(['foo']);
      });

      describe('propertyChange events', () => {

        it('should send event when property change is triggered by widget', () => {
          widget.setProperty('foo', 'bar');
          sendQueuedAjaxCalls();
          expect(jasmine.Ajax.requests.count()).toBe(1);

          let event = new RemoteEvent(widget.id, 'property', {
            foo: 'bar'
          });
          expect(mostRecentJsonRequest()).toContainEvents(event);
          expect(widget.foo).toBe('bar');
        });

        it('should not send event when property is triggered by server', () => {
          let propertyChangeEvent = new RemoteEvent('123', 'propertyChange', {
            properties: {foo: 'bar'}
          });
          adapter.onModelPropertyChange(propertyChangeEvent);
          sendQueuedAjaxCalls();
          expect(jasmine.Ajax.requests.count()).toBe(0);
          expect(widget.foo).toBe('bar');
        });

      });

      describe('widget events', () => {

        it('should handle widget event when it is not filtered', () => {
          spyOn(adapter, '_onWidgetEvent');
          widget.trigger('fooHappened');
          expect(adapter._onWidgetEvent).toHaveBeenCalled();
        });

        it('should not handle widget event when it is filtered', () => {
          adapter.addFilterForWidgetEventType('fooHappened');
          spyOn(adapter, '_onWidgetEvent');
          widget.trigger('fooHappened');
          expect(adapter._onWidgetEvent).not.toHaveBeenCalled();

          // reset filters, then onWidgetEvent should be called again
          adapter.resetEventFilters();
          widget.trigger('fooHappened');
          expect(adapter._onWidgetEvent).toHaveBeenCalled();
        });

      });

    });

    describe('export adapter', () => {

      it('exportAdapterData should export last part of model-class as ID', () => {
        let adapter = new ModelAdapter();

        // regular top-level classes (.)
        let model = {modelClass: 'com.bsiag.sandbox.FooField', objectType: ''};
        adapter.exportAdapterData(model);
        expect(model['id']).toBe('FooField');

        // inner classes ($)
        model = {modelClass: 'com.bsiag.sandbox.FooBox$BarField', objectType: ''};
        adapter.exportAdapterData(model);
        expect(model['id']).toBe('BarField');
      });

    });

    describe('adapters', () => {

      it('creates and registers adapters', () => {
        let message = {
          adapterData: mapAdapterData([childModel, childModel2]),
          events: [createPropertyChangeEvent(adapter, {
            childWidget: [childModel.id, childModel2.id]
          })]
        };
        session._processSuccessResponse(message);

        expect(widget.childWidget[0]).toBeTruthy();
        expect(widget.childWidget[1]).toBeTruthy();
        expect(session.getModelAdapter(childModel.id)).toBe(widget.childWidget[0].modelAdapter);
        expect(session.getModelAdapter(childModel2.id)).toBe(widget.childWidget[1].modelAdapter);
      });

      it('destroys the old adapters', () => {
        let message: RemoteResponse = {
          adapterData: mapAdapterData([childModel, childModel2]),
          events: [createPropertyChangeEvent(adapter, {
            childWidget: [childModel.id, childModel2.id]
          })]
        };
        session._processSuccessResponse(message);
        expect(widget.childWidget[0]).toBeTruthy();
        expect(widget.childWidget[1]).toBeTruthy();
        expect(session.getModelAdapter(childModel.id)).toBe(widget.childWidget[0].modelAdapter);
        expect(session.getModelAdapter(childModel2.id)).toBe(widget.childWidget[1].modelAdapter);

        let childWidget = widget.childWidget[0];
        let childWidget2 = widget.childWidget[1];
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
        expect(session.getModelAdapter(childModel2.id)).toBe(widget.childWidget[0].modelAdapter);
        expect(childWidget2.destroyed).toBe(false);
      });

      it('destroys the old and creates the new adapters if the array contains both', () => {
        let childModel3 = createModel();
        let message = {
          adapterData: mapAdapterData([childModel, childModel2]),
          events: [createPropertyChangeEvent(adapter, {
            childWidget: [childModel.id, childModel2.id]
          })]
        };
        session._processSuccessResponse(message);

        expect(widget.childWidget[0]).toBeTruthy();
        expect(widget.childWidget[1]).toBeTruthy();
        expect(session.getModelAdapter(childModel.id)).toBe(widget.childWidget[0].modelAdapter);
        expect(session.getModelAdapter(childModel2.id)).toBe(widget.childWidget[1].modelAdapter);

        message = {
          adapterData: mapAdapterData(childModel3),
          events: [createPropertyChangeEvent(adapter, {
            childWidget: [childModel2.id, childModel3.id]
          })]
        };
        session._processSuccessResponse(message);

        expect(widget.childWidget.length).toBe(2);
        expect(widget.childWidget[0]).toBeTruthy();
        expect(widget.childWidget[1]).toBeTruthy();
        expect(session.getModelAdapter(childModel2.id)).toBe(widget.childWidget[0].modelAdapter);
        expect(session.getModelAdapter(childModel3.id)).toBe(widget.childWidget[1].modelAdapter);
        expect(session.getModelAdapter(childModel.id)).toBeFalsy();
      });

    });

  });

});
