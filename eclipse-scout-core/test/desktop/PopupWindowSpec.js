/*
 * Copyright (c) 2010-2019 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
import {HtmlComponent, PopupWindow} from '../../src/index';


describe('PopupWindow', function() {
  var session, helper, $sandbox, origDevice, myForm, myWindow,
    myErrorHandler = function() {
    };

  beforeEach(function() {
    setFixtures(sandbox());
    session = sandboxSession();
    $sandbox = $('#sandbox');

    // form mock
    myForm = {
      modelClass: 'Foo',
      session: session,
      render: function() {
      },
      htmlComp: HtmlComponent.install($sandbox, session)
    };

    // window mock
    myWindow = $sandbox.window(true);
    myWindow.opener = {
      location: {href: null},
      onerror: myErrorHandler
    };
  });

  afterEach(function() {
  });

  it('Constructor sets cross references and window-name', function() {
    var popupWindow = new PopupWindow(myWindow, myForm);

    expect(myWindow.popupWindow).toBe(popupWindow);
    expect(myWindow.name).toBe('Scout popup-window Foo');
    expect(myForm.popupWindow).toBe(popupWindow);
  });

  it('Initialization in _onReady', function() {
    var popupWindow = new PopupWindow(myWindow, myForm),
      called = false;

    popupWindow.one('init', function() {
      called = true;
    });
    expect(popupWindow.initialized).toBe(false);
    popupWindow._onReady();
    popupWindow._onResize = function() {
      // Don't execute during spec
    };
    expect(called).toBe(true);
    expect(popupWindow.initialized).toBe(true);
    expect(myWindow.onerror).toBe(myErrorHandler);
  });

});
