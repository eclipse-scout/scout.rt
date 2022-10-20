/*
 * Copyright (c) 2014-2019 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
import {Dimension, FormFieldLayout, HtmlCompPrefSizeOptions} from '../../../index';
import $ from 'jquery';
import BrowserField from './BrowserField';

export default class BrowserFieldLayout extends FormFieldLayout {
  browserField: BrowserField;

  constructor(browserField: BrowserField) {
    super(browserField);
    this.browserField = browserField;
  }

  override preferredLayoutSize($container: JQuery, options: HtmlCompPrefSizeOptions): Dimension {
    let prefSize = super.preferredLayoutSize($container, options);
    if (this._isIFrameReadable()) {
      prefSize.height = this.browserField.$field.contents().height() + // get height of content
        this.browserField.iframe.htmlComp.insets().vertical() + // add insets of iframe
        this.browserField.htmlComp.insets().vertical(); // add insets of browser field
    }
    return prefSize;
  }

  protected _isIFrameReadable(): boolean {
    let field = this.browserField;
    let perms = field.sandboxPermissions;
    if (field.sandboxEnabled && (perms && perms.indexOf('allow-same-origin') === -1)) {
      $.log.isWarnEnabled() && $.log.warn('Access to IFrame denied, cannot read height.' +
        ' Reason: sandbox is enabled or "allow-same-origin" is not set');
      return false;
    }
    try {
      (field.$field[0] as HTMLIFrameElement).contentWindow.document;
    } catch (e) {
      $.log.isWarnEnabled() && $.log.warn('Access to IFrame denied, cannot read height. Reason: denied by browser');
      return false;
    }
    if (field.$field.contents().attr('readyState') === 'loading') {
      $.log.isWarnEnabled() && $.log.warn('Access to IFrame denied, cannot read height. Reason: readyState == "loading"');
      return false;
    }
    return true;
  }
}
