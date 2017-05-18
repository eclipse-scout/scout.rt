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
describe("BeanField", function() {
  var session, helper, field;

  beforeEach(function() {
    setFixtures(sandbox());
    session = sandboxSession();
    helper = new scout.FormSpecHelper(session);
    field = helper.createField(createModel());
    jasmine.Ajax.install();
    jasmine.clock().install();
  });

  afterEach(function() {
    jasmine.clock().uninstall();
    jasmine.Ajax.uninstall();
  });

  function createModel() {
    return helper.createFieldModel('BeanField:Test', session.desktop, {
      value: {
        sender: 'Jasmine Test Runner',
        message: 'It works!'
      }
    });
  }

  it("renders the bean", function() {
    field.render(session.$entryPoint);
    expect(field.$container).toHaveClass('test-bean-field');
    expect(field.$field.children('.msg-from').text()).toBe('Message from Jasmine Test Runner');
    expect(field.$field.children('.msg-text').text()).toBe('It works!');
  });

  it("updates properties correctly", function() {
    field.render(session.$entryPoint);
    expect(field.value.sender).toBe('Jasmine Test Runner');
    expect(field.displayText).toBe('');

    field.setValue({
      sender: 'Test Method'
    });
    expect(field.$field.children('.msg-from').text()).toBe('Message from Test Method');
    expect(field.$field.children('.msg-text').text()).toBe('\u00A0'); // &nbsp;
    expect(field.value.sender).toBe('Test Method');
    expect(field.displayText).toBe('');

    field.setDisplayText('XXX');  // should not change anything
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
