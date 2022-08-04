/* eslint-disable max-classes-per-file */
/*
 * Copyright (c) 2010-2020 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
import {Action, Button, ObjectFactory, scout, StringField} from '../src/index';
import {LocaleSpecHelper} from '../src/testing/index';

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

  it('creates objects which are registered in objectFactories', () => {
    session.init({
      $entryPoint: $('#sandbox')
    });
    let registry = ObjectFactory.get()._registry;
    for (let objectType of registry.keys()) {
      let model = createSimpleModel(objectType, session);
      let obj = scout.create(model);
      expect(obj).toBeTruthy();
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

  it('throws an error if no object could be found', () => {
    expect(() => {
      ObjectFactory.get().create('UnknownObject');
    }).toThrow();
  });

  it('throws an error if argument list is wrong', () => {
    expect(() => {
      // noinspection JSCheckFunctionSignatures
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
      // noinspection JSCheckFunctionSignatures
      ObjectFactory.get().create('', {}, {
        objectType: 'StringField'
      });
    }).toThrow();
  });

  describe('uses the registered factory to create the object', () => {
    class ReplacedButton extends Button {

    }

    let factory;

    beforeEach(() => {
      factory = new ObjectFactory();
    });

    it('works with objectType as class reference', () => {
      factory.register(Button, () => new ReplacedButton());
      let button = factory.create(Button, {parent: session.desktop});
      expect(button instanceof ReplacedButton).toBe(true);

      let button2 = factory.create('Button', {parent: session.desktop});
      expect(button2 instanceof ReplacedButton).toBe(true);
    });

    it('works with objectType as string', () => {
      factory.register('Button', () => new ReplacedButton());
      let button = factory.create(Button, {parent: session.desktop});
      expect(button instanceof ReplacedButton).toBe(true);

      let button2 = factory.create('Button', {parent: session.desktop});
      expect(button2 instanceof ReplacedButton).toBe(true);
    });

    it('works with objectTypes that don\'t reference a class', () => {
      // Use Case: Scout maps KeyStrokeAdapter to ActionAdapter in its objectFactory since KeyStrokeAdapter does not exist.
      factory.register('CustomType', () => new Action());
      let action = factory.create('CustomType', {parent: session.desktop});
      expect(action instanceof Action).toBe(true);
    });

    it('works with objectTypes containing a namespace', () => {
      window.my = {};
      let my = window.my;
      my.CustomField = class CustomField {
        constructor() {
        }
      };

      my.CustomFieldExt = class CustomFieldExt extends my.CustomField {
        constructor() {
          super();
        }
      };

      factory.register('my.CustomField', () => new my.CustomFieldExt());
      let object = factory.create('my.CustomField', {parent: session.desktop});
      expect(object instanceof my.CustomFieldExt).toBe(true);

      let object2 = factory.create(my.CustomField, {parent: session.desktop});
      expect(object2 instanceof my.CustomFieldExt).toBe(true);

      let object3 = factory.create(my.CustomFieldExt, {parent: session.desktop});
      expect(object3 instanceof my.CustomFieldExt).toBe(true);
    });

    it('works with objectTypes containing a variant', () => {
      window.my = {};
      let my = window.my;
      my.VarStringField = class VarStringField extends StringField {
        constructor(model, abc) {
          super();
          this.abc = abc;
        }
      };

      let object = factory.create('StringField:my.Var', {parent: session.desktop});
      expect(object instanceof my.VarStringField).toBe(true);
      expect(object.abc).toBeUndefined();

      let object2 = factory.create(my.VarStringField, {parent: session.desktop});
      expect(object2 instanceof my.VarStringField).toBe(true);
      expect(object2.abc).toBeUndefined();

      // Should create the same result if the VarStringField is registered explicitly
      factory.register(my.VarStringField, model => new my.VarStringField(model, 'hi there'));
      let object3 = factory.create('StringField:my.Var', {parent: session.desktop});
      expect(object3 instanceof my.VarStringField).toBe(true);
      expect(object3.abc).toBe('hi there');

      let object4 = factory.create(my.VarStringField, {parent: session.desktop});
      expect(object4 instanceof my.VarStringField).toBe(true);
      expect(object4.abc).toBe('hi there');
    });

    it('works with objectTypes as string including a variant', () => {
      window.my = {};
      let my = window.my;
      my.VarStringField = class VarStringField extends StringField {
        constructor(abc) {
          super();
          this.abc = abc;
        }
      };

      factory.register('StringField:my.Var', () => new my.VarStringField('hi there'));
      let object = factory.create('StringField:my.Var', {parent: session.desktop});
      expect(object instanceof my.VarStringField).toBe(true);
      expect(object.abc).toBe('hi there');

      let object2 = factory.create(my.VarStringField, {parent: session.desktop});
      expect(object2 instanceof my.VarStringField).toBe(true);
      expect(object2.abc).toBe('hi there');
    });
  });

  describe('finds the correct constructor function if no factory is defined', () => {

    it('uses scout namespace by default', () => {
      let object = ObjectFactory.get()._createObjectByType('StringField');
      expect(object instanceof StringField).toBe(true);
    });

    it('uses namespace of given object type if provided', () => {
      window.my = {};
      let my = window.my;
      my.StringField = class StringField {
        constructor() {
        }
      };
      let object = ObjectFactory.get().create('my.StringField');
      expect(object instanceof my.StringField).toBe(true);

      let object2 = ObjectFactory.get().create(my.StringField);
      expect(object2 instanceof my.StringField).toBe(true);
    });

    it('considers variants', () => {
      window.test = {};
      window.test.VariantStringField = class VariantStringField {
        constructor() {
        }
      };
      let object = ObjectFactory.get().create('test.StringField:Variant');
      expect(object).not.toBe(null);

      let object2 = ObjectFactory.get().create(window.test.VariantStringField);
      expect(object2).not.toBe(null);
      delete window.test;
    });

    // in this case namespace from objectType is also used as namespace for variant
    it('considers variants also within a custom namespace for object type', () => {
      window.my = {};
      let my = window.my;
      my.VariantStringField = class VariantStringField {
        constructor() {
        }
      };
      let object = ObjectFactory.get().create('my.StringField:Variant');
      expect(object instanceof my.VariantStringField).toBe(true); // objectType is 'my.StringField'

      let object2 = ObjectFactory.get().create(my.VariantStringField);
      expect(object2 instanceof my.VariantStringField).toBe(true);
    });

    it('considers variants also within a custom namespace for variant', () => {
      window.my = {};
      let my = window.my;
      my.VariantStringField = class VariantStringField {
        constructor() {
        }
      };
      let object = ObjectFactory.get().create('StringField:my.Variant');
      expect(object instanceof my.VariantStringField).toBe(true); // objectType is '[scout.]StringField'
    });

    it('considers variants also within a custom namespace for variant and a different variant for objectType', () => {
      window.your = {};
      let your = window.your;
      your.VariantStringField = class VariantStringField {
        constructor() {
        }
      };
      let object = ObjectFactory.get().create('my.StringField:your.Variant');
      expect(object instanceof your.VariantStringField).toBe(true); // objectType is 'my.StringField'
    });

    it('can handle too many variants in objectType', () => {
      window.my = {};
      let my = window.my;
      my.VariantStringField = class VariantStringField {
        constructor() {
        }
      };
      let object = ObjectFactory.get().create('my.StringField:Variant:Foo');
      expect(object instanceof my.VariantStringField).toBe(true);
    });

    it('can handle nested namespaces', () => {
      window.my = {
        inner: {
          space: {}
        }
      };
      let my = window.my;
      my.inner.space.StringField = class StringField {
        constructor() {
        }
      };
      let object = ObjectFactory.get().create('my.inner.space.StringField');
      expect(object instanceof my.inner.space.StringField).toBe(true);
    });

    it('throws errors', () => {
      let my = window.my;
      my.VariantStringField = class VariantStringField {
        constructor() {
        }
      };
      expect(() => {
        ObjectFactory.get().create('my.StringField:NotExistingVariant');
      }).toThrow();
    });

    describe('variantLenient', () => {
      it('tries to create an object without variant if with variant fails', () => {
        let options = {
          variantLenient: true
        };
        let object = ObjectFactory.get()._createObjectByType('StringField:Variant', options);
        expect(object instanceof StringField).toBe(true);
      });

      it('tries to create an object without variant if with variant fails also with custom namespace', () => {
        window.my = {};
        let my = window.my;
        my.StringField = class StringField {
          constructor() {
          }
        };
        let options = {
          variantLenient: true
        };
        let object = ObjectFactory.get()._createObjectByType('my.StringField:Variant', options);
        expect(object instanceof my.StringField).toBe(true);
      });
    });
  });
});
