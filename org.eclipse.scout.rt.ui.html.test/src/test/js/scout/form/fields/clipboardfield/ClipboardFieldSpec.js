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
/* global FormSpecHelper */
describe('ClipboardField', function() {
  var session, helper, $sandbox, origDevice;

  beforeEach(function() {
    setFixtures(sandbox());
    session = sandboxSession();
    $sandbox = $('#sandbox');
    helper = new FormSpecHelper(session);
    jasmine.Ajax.install();
    jasmine.clock().install();
    origDevice = scout.device;
  });

  afterEach(function() {
    jasmine.clock().uninstall();
    jasmine.Ajax.uninstall();
    scout.device = origDevice;
  });

  function createField(model) {
    var field = new scout.ClipboardField();
    field.init(model || createModel());
    return field;
  }

  function createModel() {
    return helper.createFieldModel();
  }

  it('Rendered container has the right class', function() {
    var field = createField();
    field.render($sandbox);
    expect(field.$container.hasClass('clipboard-field')).toBe(true);
  });

  it('Rendered field DIV has _not_ an unselectable attribute in IE9', function() {
    var field = createField();

    // regular non IE9 browser
    field.render($sandbox);
    expect(scout.device.unselectableAttribute.key).toBe(null);
    expect(field.$field.attr('unselectable')).toBe(undefined);
    field.remove();

    // modify device to look like IE9
    scout.device.unselectableAttribute = {key:'unselectable', value:'on'};
    field.render($sandbox);
    expect(field.$field.attr('unselectable')).toBe(undefined);
  });

});
