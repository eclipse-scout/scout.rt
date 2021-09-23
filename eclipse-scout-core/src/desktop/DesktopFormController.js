/*
 * Copyright (c) 2014-2018 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
import {arrays, Dimension, FormController, PopupBlockerHandler, PopupWindow, scout} from '../index';
import $ from 'jquery';

export default class DesktopFormController extends FormController {

  constructor(model) {
    super(model);
    this.desktop = model.displayParent;
    this._popupWindows = [];
    this._documentPopupWindowReadyHandler = this._onDocumentPopupWindowReady.bind(this);

    // must use a document-event, since when popup-window is reloading it does
    // only know the opener of its own window (and nothing about Scout).
    $(document).on('popupWindowReady', this._documentPopupWindowReadyHandler);
  }

  render() {
    super.render();
    let activeForm = this.desktop.activeForm;
    if (activeForm) {
      activeForm.activate();
    } else {
      this.desktop.bringOutlineToFront();
    }
  }

  _renderViews() {
    super._renderViews();

    if (this.desktop.selectedViewTabs) {
      this.desktop.selectedViewTabs.forEach(selectedView => {
        this._activateView(selectedView);
      });
    }

    // ensure in all view stacks the last view is activated
    if (this.desktop.bench) {
      this.desktop.bench.postRender();
    }
  }

  isFormShown(form) {
    if (form.isPopupWindow()) {
      return this._popupWindows.some(popup => popup.form.id === form.id);
    }
    return super.isFormShown(form);
  }

  /**
   * @override FormController.js
   */
  _renderPopupWindow(form) {
    let windowSpecs,
      resizeToPrefSize; // flag used to resize browser-window later (see PopupWindow.js)

    let bounds = form.readCacheBounds();
    if (bounds) {
      windowSpecs = 'left=' + bounds.x + ',top=' + bounds.y + ',width=' + bounds.width + ',height=' + bounds.height;
      resizeToPrefSize = false;
    } else {
      let $mainDocument = $(document),
        documentSize = new Dimension($mainDocument.width(), $mainDocument.height());
      windowSpecs = 'left=0,top=0,width=' + documentSize.width + ',height=' + documentSize.height;
      resizeToPrefSize = true;
    }

    // Note: Chrome does not allow to position a popup outside of the primary monitor (Firefox does)
    // So the popup will always appear on the primary monitor even if we have stored the correct
    // bounds to position the popup on the secondary monitor!
    // See: https://developer.mozilla.org/en-US/docs/Web/API/Window/open#Position_and_size_features
    windowSpecs += ',location=no,toolbar=no,menubar=no,resizable=yes';

    let popupBlockerHandler = scout.create('PopupBlockerHandler', {session: this.session, preserveOpener: true /* no external untrusted URI: Can keep the opener for callback. */});
    // form ID in URL is required for 'reload window' support
    let url = 'popup-window.html?formId=' + form.id;

    // use '_blank' as window-name so browser-windows are never reused
    popupBlockerHandler.openWindow(url, '_blank', windowSpecs, popup => {
      this._addPopupWindow(popup, form, resizeToPrefSize);
    });
  }

  _addPopupWindow(newWindow, form, resizeToPrefSize) {
    let popupWindow = new PopupWindow(newWindow, form);
    popupWindow.resizeToPrefSize = resizeToPrefSize;
    popupWindow.events.on('popupWindowUnload', this._onPopupWindowUnload.bind(this));
    this._popupWindows.push(popupWindow);
    $.log.isDebugEnabled() && $.log.debug('Opened new popup window for form ID ' + form.id);
  }

  _onDocumentPopupWindowReady(event, data) {
    $.log.isDebugEnabled() && $.log.debug('(FormController#_onDocumentPopupWindowReady) data=' + data);
    let popupWindow;
    if (data.formId) {
      // reload (existing popup window)
      let i, formId = data.formId;
      $.log.isDebugEnabled() && $.log.debug('Popup window for form ID ' + formId + ' has been reloaded');
      for (i = 0; i < this._popupWindows.length; i++) {
        popupWindow = this._popupWindows[i];
        if (popupWindow.form.id === formId) {
          break;
        }
      }
      if (!popupWindow) {
        throw new Error('Couldn\'t find popupWindow reference while popup window was reloaded');
      }
    } else if (data.popupWindow) {
      // open new child window
      popupWindow = data.popupWindow;
    } else {
      // error assertion
      throw new Error('Neither property \'formId\' nor \'popupWindow\' exists on data parameter');
    }
    popupWindow._onReady();
  }

  _onPopupWindowUnload(popupWindow) {
    let form = popupWindow.form;
    $.log.isDebugEnabled() && $.log.debug('Popup window for form ID ' + form.id + ' is unloaded - don\'t know if its closed or reloaded yet');

    // this remove() is important: when a popup-window in IE is closed, all references to a HTMLDivElement become
    // invalid. Every call or read on such invalid objects will cause an Error. Even though the DOM element
    // is invalid, the JQuery object which references the DOM element is still alive and occupies memory. That's
    // why we must remove JQuery objects _before_ the popup-window is closed finally.
    form.remove();

    // must do this with setTimeout because at this point window is always still open
    // Note: timeout with 0 milliseconds will not work
    setTimeout(() => {
      // Check if popup is closed (when the unload event was triggered by page reload it will still be open)
      if (popupWindow.isClosed()) {
        $.log.isDebugEnabled() && $.log.debug('Popup window for form ID ' + form.id + ' has been closed');
        form.close();
      }
    }, 250);
  }

  /**
   * We only close browser windows here, since during an unload event, we cannot send
   * anything with a HTTP request anyway. So we cannot inform the server that it
   * should "kill" the forms - instead we simply render the popupWindows and forms
   * again when the page has been reloaded.
   */
  closePopupWindows() {
    this._popupWindows.forEach(function(popupWindow) {
      this._removePopupWindow(popupWindow.form);
    }, this);
    this._popupWindows = [];
  }

  /**
   * @override FormController.js
   */
  _removePopupWindow(form) {
    let popupWindow = form.popupWindow;
    if (!popupWindow) {
      throw new Error('Form has no popupWindow reference');
    }
    delete form.popupWindow;
    arrays.remove(this._popupWindows, popupWindow);
    if (form.rendered) {
      form.remove();
      popupWindow.close();
    }
  }

  dispose() {
    $(document).off('popupWindowReady', this._documentPopupWindowReadyHandler);
  }
}
