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
import {Device} from '../../../../src/index';
import {FormSpecHelper} from '../../../../src/testing/index';

describe('ClipboardField', () => {
  let session, helper, $sandbox, origDevice;

  beforeEach(() => {
    setFixtures(sandbox());
    session = sandboxSession();
    $sandbox = $('#sandbox');
    helper = new FormSpecHelper(session);
    jasmine.Ajax.install();
    jasmine.clock().install();
    origDevice = Device.get();
  });

  afterEach(() => {
    jasmine.clock().uninstall();
    jasmine.Ajax.uninstall();
  });

  function createField() {
    return helper.createField('ClipboardField');
  }

  it('Rendered container has the right class', () => {
    let field = createField();
    field.render($sandbox);
    expect(field.$container.hasClass('clipboard-field')).toBe(true);
  });

});
