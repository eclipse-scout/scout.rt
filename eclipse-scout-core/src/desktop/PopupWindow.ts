/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {Dimension, EventEmitter, Form, HtmlComponent, PopupWindowEventMap, Rectangle, scout, Session, SingleLayout, strings} from '../index';
import $ from 'jquery';

export class PopupWindow extends EventEmitter {
  declare eventMap: PopupWindowEventMap;
  declare self: PopupWindow;

  static PROP_POPUP_WINDOW = 'popupWindow';

  myWindow: Window;
  form: Form;
  session: Session;
  initialized: boolean;
  resizeToPrefSize: boolean;
  htmlComp: HtmlComponent;
  $container: JQuery;

  constructor(myWindow: Window, form: Form) { // use 'myWindow' in place of 'window' to prevent confusion with global window variable
    super();

    this.myWindow = myWindow;
    this.form = form;
    this.session = form.session;
    this.initialized = false;
    this.$container = null;
    this.htmlComp = null;

    // link Form instance with this popupWindow instance
    // this is required when form (and popup-window) is closed by the model
    form.popupWindow = this;

    // link Window instance with this popupWindow instance
    // this is required when we want to check if a certain DOM element belongs to a popup window
    myWindow[PopupWindow.PROP_POPUP_WINDOW] = this;
    myWindow.name = 'Scout popup-window ' + form.modelClass;
  }

  protected _onUnload() {
    $.log.isDebugEnabled() && $.log.debug('stored form ID ' + this.form.id + ' to session storage');
    if (this.form.destroyed) {
      $.log.isDebugEnabled() && $.log.debug('form ID ' + this.form.id + ' is already destroyed - don\'t trigger unload event');
    } else {
      this.trigger('popupWindowUnload', this);
    }
  }

  /** @internal */
  _onReady() {
    // set container (used as document-root from callers)
    let myDocument = this.myWindow.document,
      $myWindow = $(this.myWindow),
      $myDocument = $(myDocument);

    // Establish the link again, as Chrome removes this property after a page load.
    // (page load is made by design in PopupBlockerHandler.openWindow)
    this.myWindow[PopupWindow.PROP_POPUP_WINDOW] = this;

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

    this._updateTitle();
    this.form.on('propertyChange:title propertyChange:subTitle', event => this._updateTitle());

    // Finally set initialized flag to true, at this point the PopupWindow is fully initialized
    this.initialized = true;
    this.trigger('init');
  }

  // Note: currently _onResize is only called when the window is resized, but not when the position of the window changes.
  // if we need to do that in a later release we should take a look on the SO-post below:
  // http://stackoverflow.com/questions/4319487/detecting-if-the-browser-window-is-moved-with-javascript
  protected _onResize() {
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

  isClosed(): boolean {
    return this.myWindow.closed;
  }

  close() {
    this.myWindow.close();
  }

  _updateTitle() {
    let formTitle = strings.join(' - ', this.form.title, this.form.subTitle);
    let applicationTitle = this.session.desktop.title;
    this.title(formTitle || applicationTitle);
  }

  title(title: string) {
    this.myWindow.document.title = title;
  }
}
