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
import {Form, GroupBox, PopupWindow, scout} from '../../src/index';

describe('PopupWindow', () => {
  let session: SandboxSession, $sandbox: JQuery, myForm: Form, myWindow: Window,
    myErrorHandler = () => {
      // nop
    };

  class SpecPopupWindow extends PopupWindow {
    override _onResize() {
      super._onResize();
    }
  }

  beforeEach(() => {
    setFixtures(sandbox());
    session = sandboxSession();
    $sandbox = $('#sandbox');

    myForm = scout.create(Form, {
      parent: session.desktop,
      modelClass: 'Foo',
      rootGroupBox: {
        objectType: GroupBox
      }
    });

    // window mock
    myWindow = $sandbox.window(true);
    myWindow.opener = {
      location: {href: null},
      onerror: myErrorHandler
    };
  });

  it('Constructor sets cross references and window-name', () => {
    let popupWindow = new SpecPopupWindow(myWindow, myForm);

    expect(myWindow[PopupWindow.PROP_POPUP_WINDOW]).toBe(popupWindow);
    expect(myWindow.name).toBe('Scout popup-window Foo');
    expect(myForm.popupWindow).toBe(popupWindow);
  });

  it('Initialization in _onReady', () => {
    let popupWindow = new SpecPopupWindow(myWindow, myForm),
      called = false;

    popupWindow.one('init', () => {
      called = true;
    });
    expect(popupWindow.initialized).toBe(false);
    popupWindow._onReady();
    popupWindow._onResize = () => {
      // Don't execute during spec
    };
    expect(called).toBe(true);
    expect(popupWindow.initialized).toBe(true);
    expect(myWindow.onerror).toBe(myErrorHandler);
  });

});
