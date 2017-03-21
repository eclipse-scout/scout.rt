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
describe("StringField", function() {
  var session, helper, field;

  beforeEach(function() {
    setFixtures(sandbox());
    session = sandboxSession();
    helper = new scout.FormSpecHelper(session);
    field = createField(createModel());
    linkWidgetAndAdapter(field, 'StringFieldAdapter');
    jasmine.Ajax.install();
    jasmine.clock().install();
  });

  afterEach(function() {
    jasmine.clock().uninstall();
    jasmine.Ajax.uninstall();
  });

  function createField(model) {
    var field = new scout.StringField();
    field.init(model);
    return field;
  }

  function createModel() {
    return helper.createFieldModel();
  }

  describe("Check if field is switched to password field if inputMasked is true", function() {
    it("set input masked", function() {
      field.inputMasked = true;
      field.render(session.$entryPoint);
      expect(field.$field.attr('type')).toBe('password');
    });

    it("set input not masked", function() {
      field.inputMasked = false;
      field.render(session.$entryPoint);
      expect(field.$field.attr('type')).toBe('text');
    });

  });

  describe("insertText", function() {
    it("expects empty field at the beginning", function() {
      field.render(session.$entryPoint);
      expect(field.$field[0].value).toBe('');
    });

    it("inserts text into an empty field", function() {
      field.render(session.$entryPoint);
      field.insertText('Test1');
      expect(field.$field[0].value).toBe('Test1');
    });

    it("appends text to the previous value (if no text is selected)", function() {
      field.render(session.$entryPoint);
      field.insertText('Test1');
      field.insertText('ABC2');
      expect(field.$field[0].value).toBe('Test1ABC2');
    });

    it("replaces selection #1 (if part of the text is selected, selection does not start at the beginning)", function() {
      field.render(session.$entryPoint);
      field.insertText('Test1');
      field.$field[0].selectionStart = 2;
      field.$field[0].selectionEnd = 4;
      field.insertText('sten2');
      expect(field.$field[0].value).toBe('Testen21');
    });

    it("replaces selection #2 (if part of the text is selected, start at the beginning)", function() {
      field.render(session.$entryPoint);
      field.insertText('Test1');
      field.$field[0].selectionStart = 0;
      field.$field[0].selectionEnd = 4;
      field.insertText('ABC2');
      expect(field.$field[0].value).toBe('ABC21');
    });

    it("replaces selection #3 (if whole content is selected)", function() {
      field.render(session.$entryPoint);
      field.insertText('Test1');
      field.$field[0].select();
      field.insertText('ABC2');
      expect(field.$field[0].value).toBe('ABC2');
    });

    it("sends display text changed to server using accept text", function() {
      field.render(session.$entryPoint);
      field.insertText('Test1');
      sendQueuedAjaxCalls();
      expect(jasmine.Ajax.requests.count()).toBe(1);
      var event = new scout.RemoteEvent(field.id, 'displayTextChanged', {
        displayText: 'Test1',
        whileTyping: false,
        showBusyIndicator: true
      });
      expect(mostRecentJsonRequest()).toContainEvents(event);

      field.insertText('ABC2');
      expect(field.$field[0].value).toBe('Test1ABC2');
      sendQueuedAjaxCalls();
      expect(jasmine.Ajax.requests.count()).toBe(2);
      event = new scout.RemoteEvent(field.id, 'displayTextChanged', {
        displayText: 'Test1ABC2',
        whileTyping: false,
        showBusyIndicator: true
      });
      expect(mostRecentJsonRequest()).toContainEvents(event);
    });

    it("sends display text changed to server using accept text, twice, if updateDisplayTextOnModify=true", function() {
      field.updateDisplayTextOnModify = true;
      field.render(session.$entryPoint);
      var message = {
        events: [createPropertyChangeEvent(field, {
          insertText: 'Test1'
        })]
      };
      session._processSuccessResponse(message);
      sendQueuedAjaxCalls();
      expect(jasmine.Ajax.requests.count()).toBe(1);
      var events = [];
      // displayTextChanged needs to be sent twice, with whileTyping = true and = false
      events[0] = new scout.RemoteEvent(field.id, 'displayTextChanged', {
        displayText: 'Test1',
        whileTyping: true,
        showBusyIndicator: false
      });
      events[1] = new scout.RemoteEvent(field.id, 'displayTextChanged', {
        displayText: 'Test1',
        whileTyping: false,
        showBusyIndicator: true
      });
      expect(mostRecentJsonRequest()).toContainEventsExactly(events);

      message = {
        events: [createPropertyChangeEvent(field, {
          insertText: 'ABC2'
        })]
      };
      session._processSuccessResponse(message);
      expect(field.$field[0].value).toBe('Test1ABC2');
      sendQueuedAjaxCalls();
      expect(jasmine.Ajax.requests.count()).toBe(2);
      events = [];
      events[0] = new scout.RemoteEvent(field.id, 'displayTextChanged', {
        displayText: 'Test1ABC2',
        whileTyping: true,
        showBusyIndicator: false
      });
      events[1] = new scout.RemoteEvent(field.id, 'displayTextChanged', {
        displayText: 'Test1ABC2',
        whileTyping: false,
        showBusyIndicator: true
      });
      expect(mostRecentJsonRequest()).toContainEventsExactly(events);
    });

  });

  describe('trim', function() {
    it('should restore selection', function() {
      field.trimText = true;
      field.render(session.$entryPoint);
      field.$field.val(' foo ');
      field.$field[0].select();
      var selection = field._getSelection();
      expect(selection.start).toBe(0);
      expect(selection.end).toBe(5);
      field.setDisplayText('foo');

      selection = field._getSelection();
      expect(selection.start).toBe(0);
      expect(selection.end).toBe(3);
    });

    it('should not break when displayText is very long (regex is too big)', function() {
      // this test doesn't expect anything - the test succeeds when no exception is thrown
      // with a large displayText. In ticket #169354 the issue occured with a displayText
      // that hat 55'577 bytes, so this is about the size which causes Chrome too crash.
      var longText = '';
      for (var i = 0; i < 3500; i++) {
        longText += 'too big to fail '; // 16 bytes x 3'500 = 56'000 bytes
      }
      field.trimText = true;
      field.render(session.$entryPoint);
      field.$field.val(' ' + longText + ' ');
      field.setDisplayText(longText);
      expect(true).toBe(true);
    });
  });

});
