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
describe('main', function() {
  var session;

  beforeEach(function() {
    setFixtures(sandbox());
    session = sandboxSession();
  });

  describe("nvl", function() {

    it("can return alternative value", function() {
      expect(scout.nvl()).toBe(undefined);
      expect(scout.nvl('X')).toBe('X');
      expect(scout.nvl('X', 'Y')).toBe('X');
      expect(scout.nvl(undefined)).toBe(undefined);
      expect(scout.nvl(undefined, undefined)).toBe(undefined);
      expect(scout.nvl(undefined, null)).toBe(null);
      expect(scout.nvl(undefined, '')).toBe('');
      expect(scout.nvl(undefined, 'X')).toBe('X');
      expect(scout.nvl(null, 'X')).toBe('X');
      expect(scout.nvl(null, '')).toBe('');
      expect(scout.nvl(null, undefined)).toBe(undefined);
      expect(scout.nvl(null, null)).toBe(null);
      expect(scout.nvl(null)).toBe(undefined);
      expect(scout.nvl(0, '123')).toBe(0);
      expect(scout.nvl(1, '123')).toBe(1);
      expect(scout.nvl(undefined, '123')).toBe('123');
      expect(scout.nvl(undefined, 123)).toBe(123);
      expect(scout.nvl(0.000000000000000000000001, -1)).toBe(0.000000000000000000000001);
      expect(scout.nvl({}, {x: 2})).toEqual({});
      expect(scout.nvl({y: undefined}, {x: 2})).toEqual({y: undefined});
      expect(scout.nvl(null, {x: 2})).toEqual({x: 2});
    });

  });

  describe("isOneOf", function() {

    it("can check if value is one of multiple values", function() {
      expect(scout.isOneOf()).toBe(false);
      expect(scout.isOneOf('test')).toBe(false);
      expect(scout.isOneOf('test', 'bla')).toBe(false);
      expect(scout.isOneOf('test', {test: 'test'})).toBe(false);
      expect(scout.isOneOf('test', 'bla', 123, {test: 'test'})).toBe(false);
      expect(scout.isOneOf('test', 'bla', 123, {test: 'test'}, 'test', true)).toBe(true);
      expect(scout.isOneOf('test', 'bla', 123, {test: 'test'}, ['test'], true)).toBe(false);
      expect(scout.isOneOf('test', 'bla', 123, {test: 'test'}, 'Test', true)).toBe(false);
      expect(scout.isOneOf('test', ['bla', 123, {test: 'test'}, 'test', true])).toBe(true);
      expect(scout.isOneOf(123, '123', 123.00000000000001, -123)).toBe(false);
      expect(scout.isOneOf(-123, '123', 123.00000000000001, -123)).toBe(true);
    });

  });

  describe('create', function() {

    it('accepts string, object or functions as first argument', function() {
      // must fail
      expect(function() {
        scout.create(1);
      }).toThrow();
      expect(function() {
        scout.create();
      }).toThrow();
      expect(function() {
        scout.create(true);
      }).toThrow();

      var menu = scout.create('Menu', {
        parent: new scout.NullWidget(),
        session: session
      });
      expect(menu instanceof scout.Menu).toBe(true);

      menu = scout.create({
        parent: new scout.NullWidget(),
        session: session,
        objectType: 'Menu'
      });
      expect(menu instanceof scout.Menu).toBe(true);
    });

    it('creates a new widget if the first parameter is a constructor function', function() {
      var parent = new scout.NullWidget();
      var widget = scout.create(scout.Tooltip, {
        parent: parent,
        session: session
      });
      expect(widget).toBeTruthy();
      expect(widget instanceof scout.Tooltip).toBe(true);
      expect(widget.parent).toBe(parent);
      expect(widget.session).toBe(session);
    });

    describe('creates local object if first parameter is the objectType', function() {

      it('sets property \'id\' correctly when no ID is provided', function() {
        var expectedSeqNo = scout.objectFactory.uniqueIdSeqNo + 1,
          menu = scout.create('Menu', {
            parent: new scout.NullWidget(),
            session: session
          });
        expect(menu.id).toBe('ui' + expectedSeqNo.toString());
        expect(scout.objectFactory.uniqueIdSeqNo).toBe(expectedSeqNo);
      });

      it('session must be set, but adapter should not be registered', function() {
        var oldNumProperties = scout.objects.countProperties(session.modelAdapterRegistry),
          menu = scout.create('Menu', {
            parent: new scout.NullWidget(),
            session: session
          });
        expect(menu.session === session).toBe(true);
        expect(menu._register).toBe(false);
        expect(scout.objects.countProperties(session.modelAdapterRegistry)).toBe(oldNumProperties);
      });

    });

    it('creates local object if first parameter of type object and contains objectType property', function() {
      var expectedSeqNo = scout.objectFactory.uniqueIdSeqNo + 1,
        menu = scout.create({
          parent: new scout.NullWidget(),
          session: session,
          objectType: 'Menu'
        });
      expect(menu.id).toBe('ui' + expectedSeqNo.toString());
      expect(scout.objectFactory.uniqueIdSeqNo).toBe(expectedSeqNo);
    });

  });

});
