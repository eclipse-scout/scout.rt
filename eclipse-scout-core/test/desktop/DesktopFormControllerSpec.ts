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
import {DesktopFormController} from '../../src/index';

describe('DesktopFormController', function() {

  let ctrl, session, $sandbox, popupWindow,
    myWindow, myForm,
    displayParent = this;

  beforeEach(() => {
    setFixtures(sandbox());
    session = sandboxSession();
    $sandbox = $('#sandbox');
    myWindow = $sandbox.window(true);

    // mock form
    myForm = {
      id: 'foo',
      rendered: true,
      remove: () => {
      }
    };

    // mock popupWindow
    popupWindow = {
      form: myForm,
      _onReady: () => {
      },
      close: () => {
      }
    };

    // cross reference form and popup-window
    myForm.popupWindow = popupWindow;

    // de-register ALL existing event-handlers (there are hundreds of them!)
    // otherwise they would run into an error causing our tests here to fail
    // the proper solution would be to properly clean after each call of sandboxSession()
    // which is currently hard to make :-(
    $(document).off('popupWindowReady');

    ctrl = new DesktopFormController({
      displayParent: displayParent,
      session: session
    });

  });

  afterEach(() => {
    ctrl.dispose();
  });

  it('Listens to popupWindowReady event and calls _onReady - having a popupWindow instance', () => {
    spyOn(popupWindow, '_onReady');
    $(document).trigger('popupWindowReady', {
      popupWindow: popupWindow
    });
    expect(popupWindow._onReady).toHaveBeenCalled();
  });

  it('Listens to popupWindowReady event and calls _onReady - having only a form ID (reload case)', () => {
    ctrl._popupWindows.push(popupWindow);
    spyOn(popupWindow, '_onReady');
    $(document).trigger('popupWindowReady', {
      formId: 'foo'
    });
    expect(popupWindow._onReady).toHaveBeenCalled();
  });

  it('_addPopupWindow registers listeners and adds to array with popup-windows', () => {
    expect(ctrl._popupWindows.length).toBe(0);
    ctrl._addPopupWindow(myWindow, myForm, false);
    expect(ctrl._popupWindows.length).toBe(1);
  });

  it('_removePopupWindow cleans up and removes from array with popup-windows', () => {
    spyOn(popupWindow, 'close');
    spyOn(myForm, 'remove');
    ctrl._popupWindows.push(popupWindow);
    ctrl._removePopupWindow(myForm);
    expect(ctrl._popupWindows.length).toBe(0);
    expect(myForm.popupWindow).toBe(undefined);
    expect(popupWindow.close).toHaveBeenCalled();
    expect(myForm.remove).toHaveBeenCalled();
  });
});
