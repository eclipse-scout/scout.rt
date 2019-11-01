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
import {FormFieldLayout} from '../../../index';
import * as $ from 'jquery';


export default class BrowserFieldLayout extends FormFieldLayout {

constructor(browserField) {
  super( browserField);
  this.browserField = browserField;
}


preferredLayoutSize($container, options) {
  var prefSize = super.preferredLayoutSize( $container, options);
  var sandboxPermissions = this.browserField.sandboxPermissions;
  if (!this.browserField.sandboxEnabled || (sandboxPermissions && sandboxPermissions.indexOf('allow-same-origin') > -1)) {
    if (this.browserField.$field.contents().attr('readyState') !== 'loading') {
      prefSize.height = this.browserField.$field.contents().height() + // get height of content
        this.browserField.iframe.htmlComp.insets().vertical() + // add insets of iframe
        this.browserField.htmlComp.insets().vertical(); // add insets of browser field
    }
  } else {
    $.log.isInfoEnabled() && $.log.info('Could not read height of sandboxed iframe content if permission \'allow-same-origin\' is not set.');
  }
  return prefSize;
}
}
