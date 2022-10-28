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
import {RemoteEvent, scout, StringField, StringFieldModel} from '../../../../src/index';
import {FormSpecHelper} from '../../../../src/testing/index';
import {triggerClick} from '../../../../src/testing/jquery-testing';
import {StringFieldSelection} from '../../../../src/form/fields/stringfield/StringField';

describe('StringField', () => {
  let session: SandboxSession, helper: FormSpecHelper, field: SpecStringField;

  beforeEach(() => {
    setFixtures(sandbox());
    session = sandboxSession();
    helper = new FormSpecHelper(session);
    field = createField(createModel());
    linkWidgetAndAdapter(field, 'StringFieldAdapter');
    jasmine.Ajax.install();
    jasmine.clock().install();
  });

  afterEach(() => {
    jasmine.clock().uninstall();
    jasmine.Ajax.uninstall();
  });

  class SpecStringField extends StringField {
    override _getSelection(): StringFieldSelection {
      return super._getSelection();
    }
  }

  function createField(model: StringFieldModel): SpecStringField {
    let field = new SpecStringField();
    field.init(model);
    return field;
  }

  function createModel(): StringFieldModel {
    return helper.createFieldModel();
  }

  describe('init', () => {
    it('clear does not throw exception when called in init function', () => {
      class MyStringField extends StringField {
        override init(model) {
          super.init(model);
          this.clear();
        }
      }

      let myField = new MyStringField();
      myField.init(createModel());
      // without the bugfix the init function would throw an error
      expect(true).toBe(true);

      // When not rendered, _readDisplayText must return an empty string
      // This function is also called by clear()
      expect(myField._readDisplayText()).toEqual('');

      // When rendered, _readDisplayText must return $field.val()
      myField.render();
      myField.setValue('foo');
      expect(myField._readDisplayText()).toEqual('foo');
    });
  });

  describe('inputMasked', () => {
    it('sets the field into password mode, if true', () => {
      field.inputMasked = true;
      field.render();
      expect(field.$field.attr('type')).toBe('password');
    });

    it('unsets the password mode, if false', () => {
      field.inputMasked = false;
      field.render();
      expect(field.$field.attr('type')).toBe('text');
    });

  });

  describe('insertText', () => {
    it('expects empty field at the beginning', () => {
      field.render();
      let element = field.$field[0] as HTMLInputElement;
      expect(element.value).toBe('');
    });

    it('inserts text into an empty field', () => {
      field.render();
      field.insertText('Test1');
      let element = field.$field[0] as HTMLInputElement;
      expect(element.value).toBe('Test1');
    });

    it('appends text to the previous value (if no text is selected)', () => {
      field.render();
      field.insertText('Test1');
      field.insertText('ABC2');
      let element = field.$field[0] as HTMLInputElement;
      expect(element.value).toBe('Test1ABC2');
    });

    it('replaces selection #1 (if part of the text is selected, selection does not start at the beginning)', () => {
      field.render();
      field.insertText('Test1');
      let element = field.$field[0] as HTMLInputElement;
      element.selectionStart = 2;
      element.selectionEnd = 4;
      field.insertText('sten2');
      expect(element.value).toBe('Testen21');
    });

    it('replaces selection #2 (if part of the text is selected, start at the beginning)', () => {
      field.render();
      field.insertText('Test1');
      let element = field.$field[0] as HTMLInputElement;
      element.selectionStart = 0;
      element.selectionEnd = 4;
      field.insertText('ABC2');
      expect(element.value).toBe('ABC21');
    });

    it('replaces selection #3 (if whole content is selected)', () => {
      field.render();
      field.insertText('Test1');
      let element = field.$field[0] as HTMLInputElement;
      element.select();
      field.insertText('ABC2');
      expect(element.value).toBe('ABC2');
    });

    it('sends display text changed to server using accept text', () => {
      field.render();
      field.insertText('Test1');
      sendQueuedAjaxCalls();
      expect(jasmine.Ajax.requests.count()).toBe(1);
      let event = new RemoteEvent(field.id, 'acceptInput', {
        displayText: 'Test1',
        whileTyping: false,
        showBusyIndicator: true
      });
      expect(mostRecentJsonRequest()).toContainEvents(event);

      field.insertText('ABC2');
      let element = field.$field[0] as HTMLInputElement;
      expect(element.value).toBe('Test1ABC2');
      sendQueuedAjaxCalls();
      expect(jasmine.Ajax.requests.count()).toBe(2);
      event = new RemoteEvent(field.id, 'acceptInput', {
        displayText: 'Test1ABC2',
        whileTyping: false,
        showBusyIndicator: true
      });
      expect(mostRecentJsonRequest()).toContainEvents(event);
    });

    it('sends display text changed to server using accept text, twice, if updateDisplayTextOnModify=true', () => {
      field.updateDisplayTextOnModify = true;
      field.render();
      let message = {
        events: [createPropertyChangeEvent(field, {
          insertText: 'Test1'
        })]
      };
      session._processSuccessResponse(message);
      sendQueuedAjaxCalls();
      expect(jasmine.Ajax.requests.count()).toBe(1);
      let events = [];
      // acceptInput needs to be sent twice, with whileTyping = true and = false
      events[0] = new RemoteEvent(field.id, 'acceptInput', {
        displayText: 'Test1',
        whileTyping: true,
        showBusyIndicator: false
      });
      events[1] = new RemoteEvent(field.id, 'acceptInput', {
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
      let element = field.$field[0] as HTMLInputElement;
      expect(element.value).toBe('Test1ABC2');
      sendQueuedAjaxCalls();
      expect(jasmine.Ajax.requests.count()).toBe(2);
      events = [];
      events[0] = new RemoteEvent(field.id, 'acceptInput', {
        displayText: 'Test1ABC2',
        whileTyping: true,
        showBusyIndicator: false
      });
      events[1] = new RemoteEvent(field.id, 'acceptInput', {
        displayText: 'Test1ABC2',
        whileTyping: false,
        showBusyIndicator: true
      });
      expect(mostRecentJsonRequest()).toContainEventsExactly(events);
    });

  });

  describe('trim', () => {
    it('should restore selection', () => {
      field.trimText = true;
      field.render();
      field.$field.val(' foo ');
      let element = field.$field[0] as HTMLInputElement;
      element.select();
      let selection = field._getSelection();
      expect(selection.start).toBe(0);
      expect(selection.end).toBe(5);
      field.setDisplayText('foo');

      selection = field._getSelection();
      expect(selection.start).toBe(0);
      expect(selection.end).toBe(3);
    });

    it('should not break when displayText is very long (regex is too big)', () => {
      // this test doesn't expect anything - the test succeeds when no exception is thrown
      // with a large displayText. In ticket #169354 the issue occurred with a displayText
      // that hat 55'577 bytes, so this is about the size which causes Chrome too crash.
      let longText = '';
      for (let i = 0; i < 3500; i++) {
        longText += 'too big to fail '; // 16 bytes x 3'500 = 56'000 bytes
      }
      field.trimText = true;
      field.render();
      field.$field.val(' ' + longText + ' ');
      field.setDisplayText(longText);
      expect(true).toBe(true);
    });
  });

  describe('setValue', () => {
    let field;

    beforeEach(() => {
      field = helper.createField('StringField');
    });

    it('sets the value and display text if the value is valid', () => {
      field.render();
      field.setValue('hello');
      expect(field.value).toBe('hello');
      expect(field.displayText).toBe('hello');
    });

    it('tries to convert the value into a string', () => {
      field.render();
      field.setValue(10);
      expect(field.value).toBe('10');
      expect(field.displayText).toBe('10');
    });

    it('sets the value to null if given value is empty', () => {
      field.render();
      field.setValue('');
      expect(field.value).toBe(null);
      expect(field.displayText).toBe('');
    });

    it('sets value to null if given value only consists of whitespaces and trim is true', () => {
      field.render();
      field.setValue('  ');
      expect(field.value).toBe(null);
      expect(field.displayText).toBe('');
    });

    it('does not set value to null if given value only consists of whitespaces and trim is false', () => {
      field.setTrimText(false);
      field.render();
      field.setValue('  ');
      expect(field.value).toBe('  ');
      expect(field.displayText).toBe('  ');
    });
  });

  describe('label', () => {

    it('is linked with the field', () => {
      let field = scout.create(StringField, {
        parent: session.desktop,
        label: 'label'
      });
      field.render();
      expect(field.$field.attr('aria-labelledby')).toBeTruthy();
      expect(field.$field.attr('aria-labelledby')).toBe(field.$label.attr('id'));
    });

    it('focuses the field when clicked', () => {
      let field = scout.create(StringField, {
        parent: session.desktop,
        label: 'label'
      });
      field.render();
      triggerClick(field.$label);
      expect(field.$field).toBeFocused();
    });

  });

});
