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
// eslint-disable-next-line max-classes-per-file
import {Extension, scout, StringField, StringFieldModel} from '../src/index';
import {LocaleSpecHelper} from '../src/testing/index';

describe('Extension', () => {
  let session: SandboxSession;

  beforeEach(() => {
    setFixtures(sandbox());
    session = sandboxSession();
    session.locale = new LocaleSpecHelper().createLocale(LocaleSpecHelper.DEFAULT_LOCALE);
  });

  describe('extend functions of StringField', () => {

    // create a dummy class and extensions to that class
    class MyStringField extends StringField {
    }

    window['scout'].MyStringField = MyStringField;

    // ---- extension #1 ----
    class MyExtension1 extends Extension<MyStringField> {
      init() {
        let proto = MyStringField.prototype;
        this.extend(proto, '_init');
        this.extend(proto, '_renderProperties');
        this.extend(proto, '_renderInputMasked');
      }

      _init(model) {
        this.next(model);
        this.extended.setProperty('foo', 'bar');
      }

      _renderProperties() {
        this.next();
        this._renderFoo();
      }

      _renderFoo() {
        this.extended.$container.appendDiv('foo').text(this.extended['foo']);
      }

      _renderInputMasked = function() {
        this.extended.$field.attr('type', 'ext-password');
      };
    }

    window['scout'].MyExtension1 = MyExtension1;

    // ---- extension #2 ----
    class MyExtension2 extends Extension<MyStringField> {
      init() {
        let proto = MyStringField.prototype;
        this.extend(proto, '_init');
        this.extend(proto, '_renderProperties');
      }

      _init(model) {
        this.next(model);
        this.extended.setProperty('bar', 'foo');
      }

      _renderProperties() {
        this.next();
        this._renderBar();
      }

      _renderBar() {
        this.extended.$container.appendDiv('bar').text(this.extended['bar']);
      }
    }

    window['scout'].MyExtension2 = MyExtension2;

    // ---- Spec starts here ----

    let myStringField;
    Extension.install([
      'scout.MyExtension1',
      'scout.MyExtension2'
    ]);

    beforeEach(() => {
      let model = createSimpleModel('StringField', session);
      myStringField = scout.create(MyStringField, model as StringFieldModel);
    });

    it('should extend _init method', () => {
      expect(myStringField.foo).toBe('bar');
      expect(myStringField.bar).toBe('foo');
    });

    it('should extend _renderProperties method', () => {
      myStringField.render();
      let $bar = $('.bar');
      let $foo = $('.foo');
      expect($bar.length).toBe(1);
      expect($foo.length).toBe(1);
    });

    it('should extend _renderInputMasked method', () => {
      myStringField.setProperty('inputMasked', true);
      myStringField.render();
      let typeAttr = $('input').attr('type');
      expect(typeAttr).toBe('ext-password');
    });

  });

});
