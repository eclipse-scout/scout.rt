/*
 * Copyright (c) 2010-2019 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
import {Action, ObjectFactory, scout, StringField} from '../src/index';
import {LocaleSpecHelper} from '@eclipse-scout/testing';

describe('ObjectFactory', () => {
  let session;
  beforeEach(() => {
    setFixtures(sandbox());
    session = sandboxSession();
    session.locale = new LocaleSpecHelper().createLocale(LocaleSpecHelper.DEFAULT_LOCALE);
    ObjectFactory.get().init();
    // Needed because some model adapters make JSON calls during initialization (e.g. Calendar.js)
    jasmine.Ajax.install();
    jasmine.clock().install();
  });

  afterEach(() => {
    jasmine.Ajax.uninstall();
    jasmine.clock().uninstall();
  });

  /**
   * This function is used to create a special-model when a model-adapter requires one.
   * Normally a generic model with id and objectType is sufficient, but some adapters require a more complex
   * model in order to make this test succeed. Remember you must add the additional adapter models to the
   * adapterDataCache of the Session.
   */
  function createModel(session, id, objectType) {
    let model = createSimpleModel(objectType, session, id);
    if ('Menu.NavigateUp' === objectType || 'Menu.NavigateDown' === objectType) {
      let outlineId = 'outline' + id;
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
      model.calculate = () => {
      };
    } else if ('AggregateTableControl' === objectType) {
      model.table = {
        columns: [],
        on: () => {
        }
      };
    } else if ('TabBox' === objectType) {
      let tabItemId = 'tabItem' + id;
      model.selectedTab = 0;

      model.tabItems = [tabItemId];
      session._adapterDataCache[tabItemId] = {
        id: tabItemId,
        objectType: 'TabItem',
        getForm: () => createSimpleModel('Form', session)
      };
    } else if ('ButtonAdapterMenu' === objectType) {
      model.button = {
        on: () => {
        }
      };
    } else if ('GroupBox' === objectType || 'TabItem' === objectType) {
      model.getForm = () => createSimpleModel('Form', session);
    }

    return model;
  }

  it('creates objects which are registered in scout.objectFactories', () => {
    session.init({
      $entryPoint: $('#sandbox')
    });
    // When this test fails with a message like 'TypeError: scout.[ObjectType] is not a constructor...'
    // you should check if the required .js File is registered in SpecRunnerMaven.html.
    let i, model, object, modelAdapter, objectType;
    for (objectType in scout.objectFactories) {
      model = createModel(session, i, objectType);
      object = scout.objectFactories[objectType]();
      object.init(model);
      session.registerModelAdapter(object);
      modelAdapter = session.getModelAdapter(model.id);
      expect(modelAdapter).toBe(object);
    }
  });

  it('scout.create works with KeyStroke', () => {
    // when creating a KeyStroke via factory, Action should be initialized.
    let keyStroke = scout.create('KeyStroke', {
      parent: session.desktop
    });
    expect(Action.prototype.isPrototypeOf(keyStroke)).toBe(true);
  });

  it('puts the object type to the resulting object', () => {
    let model = {
      parent: session.desktop
      // objectType will be set
    };
    let object = ObjectFactory.get().create('StringField', model);
    expect(model.objectType).toBe('StringField');
    expect(object.objectType).toBe('StringField');
  });

  it('puts the object type to the resulting object', () => {
    let model = {
      parent: session.desktop,
      objectType: 'NumberField' // this objectType will be ignored
    };
    let object = ObjectFactory.get().create('StringField', model);
    expect(object instanceof StringField).toBe(true);
    expect(model.objectType).toBe('StringField');
    expect(object.objectType).toBe('StringField');
  });

  it('throws an error if no explicit type is specified', () => {
    expect(() => {
      ObjectFactory.get().create(null, {
        objectType: 'NumberField'
      });
    }).toThrow();
  });

  it('throws an error if argument list is wrong', () => {
    expect(() => {
      ObjectFactory.get().create();
    }).toThrow();
    expect(() => {
      ObjectFactory.get().create('StringField');
    }).toThrow();
    expect(() => {
      ObjectFactory.get().create({
        someProperty: 'someValue'
      });
    }).toThrow();
    expect(() => {
      ObjectFactory.get().create('', {});
    }).toThrow();
    expect(() => {
      ObjectFactory.get().create('', {}, {
        objectType: 'StringField'
      });
    }).toThrow();
  });

  describe('finds the correct constructor function if no factory is defined', () => {

    it('uses scout namespace by default', () => {
      let object = ObjectFactory.get()._createObjectByType('StringField');
      expect(object instanceof StringField).toBe(true);
    });

    it('uses namespace of given object type if provided', () => {
      window.my = {};
      let my = window.my;
      my.StringField = () => {
      };
      let object = ObjectFactory.get()._createObjectByType('my.StringField');
      expect(object instanceof my.StringField).toBe(true);
    });

    it('considers variants', () => {
      window.test = {};
      window.test.VariantStringField = () => {
      };
      let object = ObjectFactory.get()._createObjectByType('test.StringField:Variant');
      expect(object).not.toBe(null);
      delete window.scout.VariantStringField;
    });

    // in this case namespace from objectType is also used as namespace for variant
    it('considers variants also within a custom namespace for object type', () => {
      window.my = {};
      let my = window.my;
      my.VariantStringField = () => {
      };
      let object = ObjectFactory.get()._createObjectByType('my.StringField:Variant');
      expect(object instanceof my.VariantStringField).toBe(true); // objectType is 'my.StringField'
    });

    it('considers variants also within a custom namespace for variant', () => {
      window.my = {};
      let my = window.my;
      my.VariantStringField = () => {
      };
      let object = ObjectFactory.get()._createObjectByType('StringField:my.Variant');
      expect(object instanceof my.VariantStringField).toBe(true); // objectType is '[scout.]StringField'
    });

    it('considers variants also within a custom namespace for variant and a different variant for objectType', () => {
      window.your = {};
      let your = window.your;
      your.VariantStringField = () => {
      };
      let object = ObjectFactory.get()._createObjectByType('my.StringField:your.Variant');
      expect(object instanceof your.VariantStringField).toBe(true); // objectType is 'my.StringField'
    });

    it('can handle too many variants in objectType', () => {
      window.my = {};
      let my = window.my;
      my.VariantStringField = () => {
      };
      let object = ObjectFactory.get()._createObjectByType('my.StringField:Variant:Foo');
      expect(object instanceof my.VariantStringField).toBe(true);
    });

    it('can handle nested namespaces', () => {
      window.my = {
        inner: {
          space: {}
        }
      };
      let my = window.my;
      my.inner.space.StringField = () => {
      };
      let object = ObjectFactory.get()._createObjectByType('my.inner.space.StringField');
      expect(object instanceof my.inner.space.StringField).toBe(true);
    });

    it('throws errors', () => {
      let my = window.my;
      my.VariantStringField = () => {
      };
      expect(() => {
        ObjectFactory.get()._createObjectByType('my.StringField:NotExistingVariant');
      }).toThrow();
    });

    describe('variantLenient', () => {
      it('tries to create an object without variant if with variant fails', () => {
        let model = {
          variantLenient: true
        };
        let object = ObjectFactory.get()._createObjectByType('StringField:Variant', model);
        expect(object instanceof StringField).toBe(true);
      });

      it('tries to create an object without variant if with variant fails also with custom namespace', () => {
        window.my = {};
        let my = window.my;
        my.StringField = () => {
        };
        let model = {
          variantLenient: true
        };
        let object = ObjectFactory.get()._createObjectByType('my.StringField:Variant', model);
        expect(object instanceof my.StringField).toBe(true);
      });
    });
  });
});
