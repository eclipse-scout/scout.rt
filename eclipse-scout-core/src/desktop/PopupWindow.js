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
import {Dimension, EventSupport, HtmlComponent, Rectangle, scout, SingleLayout} from '../index';
import $ from 'jquery';

export default class PopupWindow {

  constructor(myWindow, form) { // use 'myWindow' in place of 'window' to prevent confusion with global window variable
    this.myWindow = myWindow;
    this.form = form;
    this.session = form.session;
    this.events = new EventSupport();
    this.initialized = false;
    this.$container = null;
    this.htmlComp = null;

    // link Form instance with this popupWindow instance
    // this is required when form (and popup-window) is closed by the model
    form.popupWindow = this;

    // link Window instance with this popupWindow instance
    // this is required when we want to check if a certain DOM element belongs
    // to a popup window
    myWindow.popupWindow = this;
    myWindow.name = 'Scout popup-window ' + form.modelClass;
  }

  _onUnload() {
    $.log.isDebugEnabled() && $.log.debug('stored form ID ' + this.form.id + ' to session storage');
    if (this.form.destroyed) {
      $.log.isDebugEnabled() && $.log.debug('form ID ' + this.form.id + ' is already destroyed - don\'t trigger unload event');
    } else {
      this.events.trigger('popupWindowUnload', this);
    }
  }

  _onReady() {
    // set container (used as document-root from callers)
    let myDocument = this.myWindow.document,
      $myWindow = $(this.myWindow),
      $myDocument = $(myDocument);

    // Establish the link again, as Chrome removes this property after a page load.
    // (page load is made by design in PopupBlockerHandler.openWindow)
    this.myWindow.popupWindow = this;

    scout.prepareDOM(myDocument);

    this.$container = $('.scout', myDocument);
    this.htmlComp = HtmlComponent.install(this.$container, this.session);
    this.htmlComp.setLayout(new SingleLayout());
    this.$container.height($myWindow.height());
    this.form.render(this.$container);

    // resize browser-window before layout?
    if (this.resizeToPrefSize) {
      let prefSize = this.htmlComp.prefSize(),
        // we cannot simply set the pref. size of the component as window size,
        // since the window "chrome" (window-border, -title and location bar)
        // occupies some space. That's why we measure the difference between
        // the current document size and the window size first.
        myWindowSize = new Dimension(this.myWindow.outerWidth, this.myWindow.outerHeight),
        myDocumentSize = new Dimension($myDocument.width(), $myDocument.height()),
        windowChromeHoriz = myWindowSize.width - myDocumentSize.width,
        windowChromeVert = myWindowSize.height - myDocumentSize.height;

      this.myWindow.resizeTo(prefSize.width + windowChromeHoriz, prefSize.height + windowChromeVert);
      this.resizeToPrefSize = false;
    }
    this.form.htmlComp.validateLayout();

    // Must register some top-level keystroke- and mouse-handlers on popup-window
    // We do the same thing here, as with the $entryPoint of the main window
    this.session.keyStrokeManager.installTopLevelKeyStrokeHandlers(this.$container);
    this.session.focusManager.installTopLevelMouseHandlers(this.$container);
    scout.installGlobalMouseDownInterceptor(myDocument);

    // Attach event handlers on window
    $(this.myWindow)
      .on('unload', this._onUnload.bind(this))
      .on('resize', this._onResize.bind(this));

    // Delegate uncaught JavaScript errors in the popup-window to the main-window
    if (this.myWindow.opener) {
      this.myWindow.onerror = this.myWindow.opener.onerror;
    }

    // Finally set initialized flag to true, at this point the PopupWindow is fully initialized
    this.initialized = true;
    this.events.trigger('init');
  }

  // Note: currently _onResize is only called when the window is resized, but not when the position of the window changes.
  // if we need to do that in a later release we should take a look on the SO-post below:
  // http://stackoverflow.com/questions/4319487/detecting-if-the-browser-window-is-moved-with-javascript
  _onResize() {
    let $myWindow = $(this.myWindow),
      width = $myWindow.width(),
      height = $myWindow.height(),
      left = this.myWindow.screenX,
      top = this.myWindow.screenY;
    $.log.isDebugEnabled() && $.log.debug('popup-window resize: width=' + width + ' height=' + height + ' top=' + top + ' left=' + left);

    this.form.storeCacheBounds(new Rectangle(left, top, width, height));
    let windowSize = new Dimension($myWindow.width(), $myWindow.height());
    this.htmlComp.setSize(windowSize);
  }

  isClosed() {
    return this.myWindow.closed;
  }

  one(type, func) {
    this.events.one(type, func);
  }

  close() {
    this.myWindow.close();
  }

  title(title) {
    this.myWindow.document.title = title;
  }
}
