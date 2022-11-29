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
import {BasicFieldAdapter, RemoteEvent, scout, StringField} from '../../../src/index';
import {FormSpecHelper} from '../../../src/testing/index';
import {triggerBlur} from '../../../src/testing/jquery-testing';

describe('BasicField', () => {
  let session, helper, field;

  beforeEach(() => {
    setFixtures(sandbox());
    session = sandboxSession();
    helper = new FormSpecHelper(session);
    field = createField(createModel());
    jasmine.Ajax.install();
    jasmine.clock().install();
  });

  afterEach(() => {
    jasmine.clock().uninstall();
    jasmine.Ajax.uninstall();
  });

  function createField(model) {
    let adapter = new BasicFieldAdapter();
    adapter.init(model);
    let field = adapter.createWidget(model, session.desktop);
    field.$field = $('<input>').on('blur', field._onFieldBlur.bind(field));
    return field;
  }

  function createModel() {
    return helper.createFieldModel();
  }

  describe('acceptInput must always be sent to server at the end of input, if at least one change has been made', () => {
    it('updateDisplayTextOnModify = true, with changed text', () => {
      field.updateDisplayTextOnModify = true;
      field.render();
      field.$field.val('Test1');
      field.$field.trigger('input');
      jasmine.clock().tick(251); // because of debounce
      sendQueuedAjaxCalls();
      let event = new RemoteEvent(field.id, 'acceptInput', {
        displayText: 'Test1',
        whileTyping: true,
        showBusyIndicator: false
      });
      expect(mostRecentJsonRequest()).toContainEvents(event);
      triggerBlur(field.$field);
      sendQueuedAjaxCalls();
      event = new RemoteEvent(field.id, 'acceptInput', {
        displayText: 'Test1',
        whileTyping: false,
        showBusyIndicator: true
      });
      expect(mostRecentJsonRequest()).toContainEvents(event);
    });

    it('updateDisplayTextOnModify = true, with custom delay', () => {
      field.updateDisplayTextOnModify = true;
      field.updateDisplayTextOnModifyDelay = 20;
      field.render();
      field.$field.val('Test1');
      field.$field.trigger('input');
      expect(mostRecentJsonRequest()).toBeUndefined(); // nothing

      jasmine.clock().tick(10);
      expect(mostRecentJsonRequest()).toBeUndefined(); // not yet...

      jasmine.clock().tick(15);
      let event = new RemoteEvent(field.id, 'acceptInput', {
        displayText: 'Test1',
        whileTyping: true,
        showBusyIndicator: false
      });
      expect(mostRecentJsonRequest()).toContainEvents(event);
    });

    it('updateDisplayTextOnModify = true, with no delay', () => {
      field.updateDisplayTextOnModify = true;
      field.updateDisplayTextOnModifyDelay = 0;
      field.render();
      field.$field.val('Test7');
      field.$field.trigger('input');
      sendQueuedAjaxCalls();
      let event = new RemoteEvent(field.id, 'acceptInput', {
        displayText: 'Test7',
        whileTyping: true,
        showBusyIndicator: false
      });
      expect(mostRecentJsonRequest()).toContainEvents(event);
    });

    it('updateDisplayTextOnModify = false, with changed text', () => {
      field.updateDisplayTextOnModify = false;
      field.render();
      field.$field.val('Test2');
      field.$field.trigger('input');
      sendQueuedAjaxCalls();
      let event = new RemoteEvent(field.id, 'acceptInput', {
        displayText: 'Test2',
        whileTyping: true,
        showBusyIndicator: false
      });
      expect(mostRecentJsonRequest()).not.toContainEvents(event);
      triggerBlur(field.$field);
      sendQueuedAjaxCalls();
      event = new RemoteEvent(field.id, 'acceptInput', {
        displayText: 'Test2',
        whileTyping: false,
        showBusyIndicator: true
      });
      expect(mostRecentJsonRequest()).toContainEvents(event);
    });

    it('updateDisplayTextOnModify = true, then property change to updateDisplayTextOnModify = false, with changed text', () => {
      field.updateDisplayTextOnModify = true;
      field.render();
      field.$field.val('Test3');
      field.$field.trigger('input');
      jasmine.clock().tick(251); // because of debounce
      sendQueuedAjaxCalls();
      let event = new RemoteEvent(field.id, 'acceptInput', {
        displayText: 'Test3',
        whileTyping: true,
        showBusyIndicator: false
      });
      expect(mostRecentJsonRequest()).toContainEvents(event);
      field.setUpdateDisplayTextOnModify(false);
      triggerBlur(field.$field);
      sendQueuedAjaxCalls();
      event = new RemoteEvent(field.id, 'acceptInput', {
        displayText: 'Test3',
        whileTyping: false,
        showBusyIndicator: true
      });
      expect(mostRecentJsonRequest()).toContainEvents(event);
    });

    it('updateDisplayTextOnModify = true, then property change to updateDisplayTextOnModify = false, with *pending* changed text', () => {
      field.updateDisplayTextOnModify = true;
      field.render();
      field.$field.val('Test3');
      field.$field.trigger('input');
      jasmine.clock().tick(100); // debounced function has not been executed yet!
      sendQueuedAjaxCalls();
      let event = new RemoteEvent(field.id, 'acceptInput', {
        displayText: 'Test3',
        whileTyping: true,
        showBusyIndicator: false
      });
      expect(mostRecentJsonRequest()).not.toContainEvents(event); // not!

      field.setUpdateDisplayTextOnModify(false); // this should trigger to immediate execution of acceptInput(true)
      sendQueuedAjaxCalls();
      expect(mostRecentJsonRequest()).toContainEvents(event);

      triggerBlur(field.$field);
      sendQueuedAjaxCalls();
      event = new RemoteEvent(field.id, 'acceptInput', {
        displayText: 'Test3',
        whileTyping: false,
        showBusyIndicator: true
      });
      expect(mostRecentJsonRequest()).toContainEvents(event);
    });

    it('updateDisplayTextOnModify = true, then acceptInput(false) is fired. -> send should be done immediately', () => {
      field.updateDisplayTextOnModify = true;
      field.render();
      field.$field.val('Test3');
      field.$field.trigger('input');
      field.acceptInput(false);
      sendQueuedAjaxCalls();
      let event = new RemoteEvent(field.id, 'acceptInput', {
        displayText: 'Test3',
        whileTyping: false,
        showBusyIndicator: true
      });
      expect(mostRecentJsonRequest()).toContainEvents(event);
    });

    it('updateDisplayTextOnModify = true, w/o changed text', () => {
      field.updateDisplayTextOnModify = true;
      field.render();
      field.displayText = 'Test4'; // fake previous display text
      field.$field.val('Test4');
      field.$field.trigger('input');
      sendQueuedAjaxCalls();
      let event = new RemoteEvent(field.id, 'acceptInput', {
        displayText: 'Test4',
        whileTyping: true,
        showBusyIndicator: false
      });
      expect(mostRecentJsonRequest()).not.toContainEvents(event);
      triggerBlur(field.$field);
      sendQueuedAjaxCalls();
      event = new RemoteEvent(field.id, 'acceptInput', {
        displayText: 'Test4',
        whileTyping: false,
        showBusyIndicator: false
      });
      expect(mostRecentJsonRequest()).not.toContainEvents(event);
    });

    it('updateDisplayTextOnModify = false, w/o changed text', () => {
      field.updateDisplayTextOnModify = false;
      field.render();
      field.displayText = 'Test5'; // fake previous display text
      field.$field.val('Test5');
      field.$field.trigger('input');
      sendQueuedAjaxCalls();
      let event = new RemoteEvent(field.id, 'acceptInput', {
        displayText: 'Test5',
        whileTyping: true,
        showBusyIndicator: false
      });
      expect(mostRecentJsonRequest()).not.toContainEvents(event);
      triggerBlur(field.$field);
      sendQueuedAjaxCalls();
      event = new RemoteEvent(field.id, 'acceptInput', {
        displayText: 'Test5',
        whileTyping: false,
        showBusyIndicator: true
      });
      expect(mostRecentJsonRequest()).not.toContainEvents(event);
    });

    it('does not fail if field is removed while acceptInput is still pending', () => {
      field.updateDisplayTextOnModify = true;
      field.render();
      field.$field.val('Test1');
      field.$field.trigger('input');

      field.remove();
      jasmine.clock().tick(251); // because of debounce
      // expect not to fail
      expect().nothing();
    });
  });

  describe('clear', () => {

    it('removes the text and accepts input also with updateDisplayTextOnAnyKey set to true', () => {
      // Behavior should be similar to ctrl+A
      let field = scout.create(StringField, {
        parent: session.desktop,
        updateDisplayTextOnAnyKey: true
      });
      let inputAccepted = false;
      field.render();
      field.setValue('abc');
      field.on('acceptInput', () => {
        inputAccepted = true;
      });
      expect(field.$field.val()).toBe('abc');
      expect(field.value).toBe('abc');
      expect(field.displayText).toBe('abc');

      field.clear();
      expect(field.$field.val()).toBe('');
      expect(field.value).toBe(null);
      expect(field.displayText).toBe('');
      expect(inputAccepted).toBe(true);
    });

  });

});
