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
        var expectedSeqNo = scout._uniqueIdSeqNo + 1,
          menu = scout.create('Menu', {
            parent: new scout.NullWidget(),
            session: session
          });
        expect(menu.id).toBe('ui' + expectedSeqNo.toString());
        expect(scout._uniqueIdSeqNo).toBe(expectedSeqNo);
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
      var expectedSeqNo = scout._uniqueIdSeqNo + 1,
        menu = scout.create({
          parent: new scout.NullWidget(),
          session: session,
          objectType: 'Menu'
        });
      expect(menu.id).toBe('ui' + expectedSeqNo.toString());
      expect(scout._uniqueIdSeqNo).toBe(expectedSeqNo);
    });

  });

});
