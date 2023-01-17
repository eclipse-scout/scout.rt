/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {DesktopFormController, Form, PopupWindow} from '../../src/index';

describe('DesktopFormController', function() {

  let ctrl: SpecDesktopFormController, session: SandboxSession, $sandbox: JQuery, popupWindow: PopupWindow,
    myWindow: Window, myForm: Form,
    displayParent = this;

  class SpecDesktopFormController extends DesktopFormController {
    override _addPopupWindow(newWindow: Window, form: Form, resizeToPrefSize: boolean) {
      super._addPopupWindow(newWindow, form, resizeToPrefSize);
    }

    override _removePopupWindow(form: Form) {
      super._removePopupWindow(form);
    }
  }

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
        // nop
      }
    } as Form;

    // mock popupWindow
    popupWindow = {
      form: myForm,
      _onReady: () => {
        // nop
      },
      close: () => {
        // nop
      }
    } as PopupWindow;

    // cross-reference form and popup-window
    myForm.popupWindow = popupWindow;

    // de-register ALL existing event-handlers (there are hundreds of them!)
    // otherwise they would run into an error causing our tests here to fail
    // the proper solution would be to properly clean after each call of sandboxSession()
    // which is currently hard to make :-(
    $(document).off('popupWindowReady');

    ctrl = new SpecDesktopFormController({
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
    ctrl.popupWindows.push(popupWindow);
    spyOn(popupWindow, '_onReady');
    $(document).trigger('popupWindowReady', {
      formId: 'foo'
    });
    expect(popupWindow._onReady).toHaveBeenCalled();
  });

  it('_addPopupWindow registers listeners and adds to array with popup-windows', () => {
    expect(ctrl.popupWindows.length).toBe(0);
    ctrl._addPopupWindow(myWindow, myForm, false);
    expect(ctrl.popupWindows.length).toBe(1);
  });

  it('_removePopupWindow cleans up and removes from array with popup-windows', () => {
    spyOn(popupWindow, 'close');
    spyOn(myForm, 'remove');
    ctrl.popupWindows.push(popupWindow);
    ctrl._removePopupWindow(myForm);
    expect(ctrl.popupWindows.length).toBe(0);
    expect(myForm.popupWindow).toBe(undefined);
    expect(popupWindow.close).toHaveBeenCalled();
    expect(myForm.remove).toHaveBeenCalled();
  });
});
