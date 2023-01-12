/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {ClipboardField, Device} from '../../../../src/index';
import {FormSpecHelper} from '../../../../src/testing/index';

describe('ClipboardField', () => {
  let session: SandboxSession, helper: FormSpecHelper, $sandbox: JQuery, origDevice: Device;

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

  function createField(): ClipboardField {
    return helper.createField(ClipboardField);
  }

  it('Rendered container has the right class', () => {
    let field = createField();
    field.render($sandbox);
    expect(field.$container.hasClass('clipboard-field')).toBe(true);
  });

});
