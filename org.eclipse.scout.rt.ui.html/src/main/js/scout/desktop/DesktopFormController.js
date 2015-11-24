scout.DesktopFormController = function(displayParent, session) {
  scout.DesktopFormController.parent.call(this, displayParent, session);
  this._popupWindows = [];

  // must use a document-event, since when popup-window is reloading it does
  // only know the opener of its own window (and nothing about Scout).
  $(document).on('popupWindowReady', this._onDocumentPopupWindowReady.bind(this));
};
scout.inherits(scout.DesktopFormController, scout.FormController);

/**
 * @override FormController.js
 */
scout.DesktopFormController.prototype._renderPopupWindow = function(form) {
  var windowSpecs,
    resizeToPrefSize, // flag used to resize browser-window later (see PopupWindow.js)
    bounds = scout.PopupWindow.readWindowBounds(form);

  if (bounds) {
    windowSpecs = 'left=' + bounds.x + ',top=' + bounds.y + ',width=' + bounds.width + ',height=' + bounds.height;
    resizeToPrefSize = false;
  } else {
    var $mainDocument = $(document),
      documentSize = new scout.Dimension($mainDocument.width(), $mainDocument.height());
    windowSpecs = 'left=0,top=0,width=' + documentSize.width + ',height=' + documentSize.height;
    resizeToPrefSize = true;
  }

  // Note: Chrome does not allow to position a popup outside of the primary monitor (Firefox does)
  // So the popup will always appear on the primary monitor even if we have stored the correct
  // bounds to position the popup on the secondary monitor!
  // See: https://developer.mozilla.org/en-US/docs/Web/API/Window/open#Position_and_size_features
  windowSpecs += ',location=no,toolbar=no,menubar=no,resizable=yes';

  var popupBlockerHandler = new scout.PopupBlockerHandler(this.session),
    // form ID in URL is required for 'reload window' support
    url = 'popup-window.html?formId=' + form.id,
    // we must use '_blank' as window-name so browser-windows are never reused
    newWindow = popupBlockerHandler.openWindow(url, '_blank', windowSpecs);

  if (newWindow) {
    this._addPopupWindow(newWindow, form, resizeToPrefSize);
  } else {
    $.log.warn('Popup-blocker detected! Show link to open window manually');
    popupBlockerHandler.showNotification(function() {
      newWindow = window.open(url, '_blank', windowSpecs);
      this._addPopupWindow(newWindow, form, resizeToPrefSize);
    }.bind(this));
  }
};

scout.DesktopFormController.prototype._addPopupWindow = function(newWindow, form, resizeToPrefSize) {
  var popupWindow = new scout.PopupWindow(newWindow, form);
  popupWindow.resizeToPrefSize = resizeToPrefSize;
  popupWindow.events.on('popupWindowUnload', this._onPopupWindowUnload.bind(this));
  this._popupWindows.push(popupWindow);
  $.log.debug('Opened new popup window for form ID ' + form.id);
};

scout.DesktopFormController.prototype._onDocumentPopupWindowReady = function(event, data) {
  $.log.debug('(FormController#_onDocumentPopupWindowReady) data=' + data);
  var popupWindow;
  if (data.formId) {
    // reload (existing popup window)
    var i, formId = data.formId;
    $.log.debug('Popup window for form ID ' + formId + ' has been reloaded');
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
};

scout.DesktopFormController.prototype._onPopupWindowUnload = function(popupWindow) {
  var form = popupWindow.form;
  $.log.debug('Popup window for form ID ' + form.id + ' is unloaded - don\'t know if its closed or reloaded yet');

  // this remove() is important: when a popup-window in IEis closed all references to a HTMLDivElement become
  // invalid. Every call or read on such invalid objects will cause an Error. Even though the DOM element
  // is invalid, the JQuery object which references the DOM element is still alive and occupies memory. That's
  // why we must remove JQuery objects _before_ the popup-window is closed finally.
  form.remove();

  // must do this with setTimeout because at this point window is always still open
  setTimeout(function() {
    if (popupWindow.isClosed()) {
      $.log.debug('Popup window for form ID ' + form.id + ' has been closed');
      form.close(); // FIXME AWE: discuss is close Ok or do we need the now unsupported "killed from UI" event?
      // was passiert im model wenn noch fenster / message boxen offen sind`?
    }
  }.bind(this), 250); // FIXME AWE: geht hier auch 0?
};

/**
 * We only close browser windows here, since during an unload event, we cannot send
 * anything with a HTTP request anyway. So we cannot inform the server that it
 * should "kill" the forms - instead we simply render the popupWindows and forms
 * again when the page has been reloaded.
 */
scout.DesktopFormController.prototype.closePopupWindows = function() {
  this._popupWindows.forEach(function(popupWindow) {
    this._removePopupWindow(popupWindow.form);
  }, this);
};

/**
 * @override FormController.js
 */
scout.DesktopFormController.prototype._removePopupWindow = function(form) {
  var popupWindow = form.popupWindow;
  if (!popupWindow) {
    throw new Error('Form has no popupWindow reference');
  }
  scout.arrays.remove(this._popupWindows, popupWindow);
  if (form.rendered) {
    form.remove();
    popupWindow.close();
    delete form.popupWindow;
  }
};
