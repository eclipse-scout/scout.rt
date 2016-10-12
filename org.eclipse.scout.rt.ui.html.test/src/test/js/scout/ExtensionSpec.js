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
describe('Extension', function() {
  var session;

  beforeEach(function() {
    setFixtures(sandbox());
    session = sandboxSession();
    session.locale = new scout.LocaleSpecHelper().createLocale(scout.LocaleSpecHelper.DEFAULT_LOCALE);
  });

  describe('extend functions of StringField', function() {

    // create a dummy class and extensions to that class
    scout.MyStringField = function() {
      scout.MyStringField.parent.call(this);
    };
    scout.inherits(scout.MyStringField, scout.StringField);

    // ---- extension #1 ----
    scout.MyExtension1 = function() {
    };
    scout.inherits(scout.MyExtension1, scout.Extension);

    scout.MyExtension1.prototype.init = function() {
      var proto = scout.MyStringField.prototype;
      this.extend(proto, '_init');
      this.extend(proto, '_renderProperties');
      this.extend(proto, '_renderInputMasked');
    };

    scout.MyExtension1.prototype._init = function(model) {
      this.next(model);
      this.extended.setProperty('foo', 'bar');
    };

    scout.MyExtension1.prototype._renderProperties = function() {
      this.next();
      this._renderFoo();
    };

    scout.MyExtension1.prototype._renderFoo = function() {
      this.extended.$container.appendDiv('foo').text(this.extended.foo);
    };

    scout.MyExtension1.prototype._renderInputMasked = function() {
      this.extended.$field.attr('type', 'ext-password');
    };

    // ---- extension #2 ----
    scout.MyExtension2 = function() {
    };
    scout.inherits(scout.MyExtension2, scout.Extension);

    scout.MyExtension2.prototype.init = function() {
      var proto = scout.MyStringField.prototype;
      this.extend(proto, '_init');
      this.extend(proto, '_renderProperties');
    };

    scout.MyExtension2.prototype._init = function(model) {
      this.next(model);
      this.extended.setProperty('bar', 'foo');
    };

    scout.MyExtension2.prototype._renderProperties = function() {
      this.next();
      this._renderBar();
    };

    scout.MyExtension2.prototype._renderBar = function() {
      this.extended.$container.appendDiv('bar').text(this.extended.bar);
    };

    // ---- Spec starts here ----

    var myStringField;
    scout.Extension.install([
      'scout.MyExtension1',
      'scout.MyExtension2'
    ]);

    beforeEach(function() {
      var model = createSimpleModel('StringField', session);
      myStringField = scout.create('MyStringField', model);
    });

    it('should extend _init method', function() {
      expect(myStringField.foo).toBe('bar');
      expect(myStringField.bar).toBe('foo');
    });

    it('should extend _renderProperties method', function() {
      myStringField.render(session.$entryPoint);
      var $bar = $('.bar');
      var $foo = $('.foo');
      expect($bar.length).toBe(1);
      expect($foo.length).toBe(1);
    });

    it('should extend _renderInputMasked method', function() {
      myStringField.setProperty('inputMasked', true);
      myStringField.render(session.$entryPoint);
      var typeAttr = $('input').attr('type');
      expect(typeAttr).toBe('ext-password');
    });

  });

});
