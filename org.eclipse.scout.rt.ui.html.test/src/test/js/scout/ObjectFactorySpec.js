/*******************************************************************************
 * Copyright (c) 2014-2017 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
describe('ObjectFactory', function() {
  var session;
  beforeEach(function() {
    setFixtures(sandbox());
    session = sandboxSession();
    session.locale = new scout.LocaleSpecHelper().createLocale(scout.LocaleSpecHelper.DEFAULT_LOCALE);
    scout.objectFactory.init();
    // Needed because some model adapters make JSON calls during initialization (e.g. Calendar.js)
    jasmine.Ajax.install();
    jasmine.clock().install();
  });

  afterEach(function() {
    jasmine.Ajax.uninstall();
    jasmine.clock().uninstall();
    scout.device.type = scout.Device.Type.DESKTOP;
  });

  /**
   * This function is used to create a special-model when a model-adapter requires one.
   * Normally a generic model with id and objectType is sufficient, but some adapters require a more complex
   * model in order to make this test succeed. Remember you must add the additional adapter models to the
   * adapaterDataCache of the Session.
   */
  function createModel(session, id, objectType) {
    var model = createSimpleModel(objectType, session, id);
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
    } else if ('TextColumnUserFilter' === objectType) {
      model.table = {};
      model.column = {};
      model.calculate = function() {};
    } else if ('AggregateTableControl' === objectType) {
      model.table = {
        columns: [],
        on: function() {}
      };
    } else if ('TabBox' === objectType) {
      var tabItemId = 'tabItem' + id;
      model.selectedTab = 0;

      model.tabItems = [tabItemId];
      session._adapterDataCache[tabItemId] = {
        id: tabItemId,
        objectType: 'TabItem',
        getForm: function() {
          return createSimpleModel('Form', session);
        }
      };
    } else if ('ButtonAdapterMenu' === objectType) {
      model.button = {
        on: function() {}
      };
    } else if ('GroupBox' === objectType || 'TabItem' === objectType) {
      model.getForm = function() {
        return createSimpleModel('Form', session);
      };
    }

    return model;
  }

  it('creates objects which are registered in scout.objectFactories', function() {
    session.init({
      $entryPoint: $('#sandbox')
    });
    // When this test fails with a message like 'TypeError: scout.[ObjectType] is not a constructor...'
    // you should check if the required .js File is registered in SpecRunnerMaven.html.
    var i, model, factory, object, modelAdapter, objectType;
    for (objectType in scout.objectFactories) {
      model = createModel(session, i, objectType);
      object = scout.objectFactories[objectType]();
      object.init(model);
      session.registerModelAdapter(object);
      modelAdapter = session.getModelAdapter(model.id);
      expect(modelAdapter).toBe(object);
    }
  });

  it('scout.create works with KeyStroke', function() {
    // when creating a KeyStroke via factory, scout.Action should be initialized.
    var keyStroke = scout.create('KeyStroke', {
      parent: session.desktop
    });
    expect(scout.Action.prototype.isPrototypeOf(keyStroke)).toBe(true);
  });

  it('puts the object type to the resulting object', function() {
    var model = {
      parent: session.desktop
      // objectType will be set
    };
    var object = scout.objectFactory.create('StringField', model);
    expect(model.objectType).toBe('StringField');
    expect(object.objectType).toBe('StringField');
  });

  it('puts the object type to the resulting object', function() {
    var model = {
      parent: session.desktop,
      objectType: 'NumberField' // this objectType will be ignored
    };
    var object = scout.objectFactory.create('StringField', model);
    expect(object instanceof scout.StringField).toBe(true);
    expect(model.objectType).toBe('StringField');
    expect(object.objectType).toBe('StringField');
  });

  it('throws an error if no explicit type is specified', function() {
    expect(function() {
      scout.objectFactory.create(null, {
        objectType: 'NumberField'
      });
    }).toThrow();
  });

  it('throws an error if argument list is wrong', function() {
    expect(function() {
      scout.objectFactory.create();
    }).toThrow();
    expect(function() {
      scout.objectFactory.create('StringField');
    }).toThrow();
    expect(function() {
      scout.objectFactory.create({
        someProperty: 'someValue'
      });
    }).toThrow();
    expect(function() {
      scout.objectFactory.create('', {});
    }).toThrow();
    expect(function() {
      scout.objectFactory.create('', {}, {
        objectType: 'StringField'
      });
    }).toThrow();
  });

  describe('finds the correct constructor function if no factory is defined', function() {

    it('uses scout namespace by default', function() {
      var object = scout.objectFactory._createObjectByType('StringField');
      expect(object instanceof scout.StringField).toBe(true);
    });

    it('uses namespace of given object type if provided', function() {
      window.my = {};
      var my = window.my;
      my.StringField = function() {};
      var object = scout.objectFactory._createObjectByType('my.StringField');
      expect(object instanceof my.StringField).toBe(true);
    });

    it('considers variants', function() {
      scout.VariantStringField = function() {};
      var object = scout.objectFactory._createObjectByType('StringField:Variant');
      expect(object instanceof scout.VariantStringField).toBe(true);
      delete scout.VariantStringField;
    });

    // in this case namespace from objectType is also used as namespace for variant
    it('considers variants also within a custom namespace for object type', function() {
      window.my = {};
      var my = window.my;
      my.VariantStringField = function() {};
      var object = scout.objectFactory._createObjectByType('my.StringField:Variant');
      expect(object instanceof my.VariantStringField).toBe(true); // objectType is 'my.StringField'
    });

    it('considers variants also within a custom namespace for variant', function() {
      window.my = {};
      var my = window.my;
      my.VariantStringField = function() {};
      var object = scout.objectFactory._createObjectByType('StringField:my.Variant');
      expect(object instanceof my.VariantStringField).toBe(true); // objectType is '[scout.]StringField'
    });

    it('considers variants also within a custom namespace for variant and a different variant for objectType', function() {
      window.your = {};
      var your = window.your;
      your.VariantStringField = function() {};
      var object = scout.objectFactory._createObjectByType('my.StringField:your.Variant');
      expect(object instanceof your.VariantStringField).toBe(true); // objectType is 'my.StringField'
    });

    it('can handle too many variants in objectType', function() {
      window.my = {};
      var my = window.my;
      my.VariantStringField = function() {};
      var object = scout.objectFactory._createObjectByType('my.StringField:Variant:Foo');
      expect(object instanceof my.VariantStringField).toBe(true);
    });

    it('can handle nested namespaces', function() {
      window.my = {
        inner: {
          space: {}
        }
      };
      var my = window.my;
      my.inner.space.StringField = function() {};
      var object = scout.objectFactory._createObjectByType('my.inner.space.StringField');
      expect(object instanceof my.inner.space.StringField).toBe(true);
    });

    it('throws errors', function() {
      var my = window.my;
      my.VariantStringField = function() {};
      expect(function() {
        scout.objectFactory._createObjectByType('my.StringField:NotExistingVariant');
      }).toThrow();
    });

    describe('variantLenient', function() {
      it('tries to create an object without variant if with variant fails', function() {
        var model = {
          variantLenient: true
        };
        var object = scout.objectFactory._createObjectByType('StringField:Variant', model);
        expect(object instanceof scout.StringField).toBe(true);
      });

      it('tries to create an object without variant if with variant fails also with custom namespace', function() {
        window.my = {};
        var my = window.my;
        my.StringField = function() {};
        var model = {
          variantLenient: true
        };
        var object = scout.objectFactory._createObjectByType('my.StringField:Variant', model);
        expect(object instanceof my.StringField).toBe(true);
      });
    });
  });
});
