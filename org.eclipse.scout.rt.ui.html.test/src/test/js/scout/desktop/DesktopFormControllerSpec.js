/*******************************************************************************
 * Copyright (c) 2014-2017 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
describe('DesktopFormController', function() {

  var ctrl, session, $sandbox, popupWindow,
    myWindow, myForm,
    displayParent = this;

  beforeEach(function() {
    setFixtures(sandbox());
    session = sandboxSession();
    $sandbox = $('#sandbox');
    myWindow = $sandbox.window(true);

    // mock form
    myForm = {
      id: 'foo',
      rendered: true,
      remove: function() {}
    };

    // mock popupWindow
    popupWindow = {
      form: myForm,
      _onReady: function() {},
      close: function() {}
    };

    // cross reference form and popup-window
    myForm.popupWindow = popupWindow;

    // de-register ALL existing event-handlers (there are hundreds of them!)
    // otherwise they would run into an error causing our tests here to fail
    // the proper solution would be to properly clean after each call of sandboxSession()
    // which is currently hard to make :-(
    $(document).off('popupWindowReady');

    ctrl = new scout.DesktopFormController({
      displayParent: displayParent,
      session: session
    });

  });

  afterEach(function() {
    ctrl.dispose();
  });

  it('Listens to popupWindowReady event and calls _onReady - having a popupWindow instance', function() {
    spyOn(popupWindow, '_onReady');
    $(document).trigger('popupWindowReady', {
      popupWindow: popupWindow
    });
    expect(popupWindow._onReady).toHaveBeenCalled();
  });

  it('Listens to popupWindowReady event and calls _onReady - having only a form ID (reload case)', function() {
    ctrl._popupWindows.push(popupWindow);
    spyOn(popupWindow, '_onReady');
    $(document).trigger('popupWindowReady', {
      formId: 'foo'
    });
    expect(popupWindow._onReady).toHaveBeenCalled();
  });

  it('_addPopupWindow registers listeners and adds to array with popup-windows', function() {
    expect(ctrl._popupWindows.length).toBe(0);
    ctrl._addPopupWindow(myWindow, myForm, false);
    expect(ctrl._popupWindows.length).toBe(1);
  });

  it('_removePopupWindow cleans up and removes from array with popup-windows', function() {
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
