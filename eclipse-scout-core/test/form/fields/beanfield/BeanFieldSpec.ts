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
import {FormSpecHelper} from '../../../../src/testing/index';
import {BeanField} from '../../../../src';

describe('BeanField', () => {
  let session: SandboxSession, helper: FormSpecHelper, field: BeanField<any>;

  beforeEach(() => {
    setFixtures(sandbox());
    session = sandboxSession();
    helper = new FormSpecHelper(session);
    field = helper.createField('BeanField:Test', session.desktop, {
      value: {
        sender: 'Jasmine Test Runner',
        message: 'It works!'
      }
    });
    jasmine.Ajax.install();
    jasmine.clock().install();
  });

  afterEach(() => {
    jasmine.clock().uninstall();
    jasmine.Ajax.uninstall();
  });

  it('renders the bean', () => {
    field.render();
    expect(field.$container).toHaveClass('test-bean-field');
    expect(field.$field.children('.msg-from').text()).toBe('Message from Jasmine Test Runner');
    expect(field.$field.children('.msg-text').text()).toBe('It works!');
  });

  it('updates properties correctly', () => {
    field.render();
    expect(field.value.sender).toBe('Jasmine Test Runner');
    expect(field.displayText).toBe(null);

    field.setValue({
      sender: 'Test Method'
    });
    expect(field.$field.children('.msg-from').text()).toBe('Message from Test Method');
    expect(field.$field.children('.msg-text').text()).toBe('\u00A0'); // &nbsp;
    expect(field.value.sender).toBe('Test Method');
    expect(field.displayText).toBe(null);

    field.setDisplayText('XXX'); // should not change anything
    expect(field.$field.children('.msg-from').text()).toBe('Message from Test Method');
    expect(field.$field.children('.msg-text').text()).toBe('\u00A0'); // &nbsp;
    expect(field.value.sender).toBe('Test Method');
    expect(field.displayText).toBe('XXX');

    field.acceptInput(); // should not change anything
    expect(field.$field.children('.msg-from').text()).toBe('Message from Test Method');
    expect(field.$field.children('.msg-text').text()).toBe('\u00A0'); // &nbsp;
    expect(field.value.sender).toBe('Test Method');
    expect(field.displayText).toBe('XXX');

    field.setValue(null); // should only set value, not displayText
    expect(field.$field.children('.msg-from').length).toBe(0);
    expect(field.$field.children('.msg-text').length).toBe(0);
    expect(field.value).toBe(null);
    expect(field.displayText).toBe('XXX');
  });

});
