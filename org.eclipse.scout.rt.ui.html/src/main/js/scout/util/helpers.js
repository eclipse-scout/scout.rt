/*******************************************************************************
 * Copyright (c) 2014-2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
scout.helpers = {

  prepareDOM: function() {
    // Cleanup DOM
    $('noscript').remove();

    // Prevent "Do you want to translate this page?" in Google Chrome
    if (scout.device.browser === scout.Device.SupportedBrowsers.CHROME) {
      var metaNoTranslate = '<meta name="google" content="notranslate" />';
      var $title = $('head > title');
      if ($title.length === 0) {
        // Add to end of head
        $('head').append(metaNoTranslate);
      } else {
        $title.after(metaNoTranslate);
      }
    }
  },

  /**
   * If 'value' is undefined or null, 'defaultValue' is returned. Otherwise, 'value' is returned.
   */
  nvl: function(value, defaultValue) {
    if (value === undefined || value === null) {
      return defaultValue;
    }
    return value;
  },

  isOneOf: function() {
    if (arguments && arguments.length >= 2) {
      var value = arguments[0];
      var argsToCheck;
      if (arguments.length === 2 && Array.isArray(arguments[1])) {
        argsToCheck = arguments[1];
      } else {
        argsToCheck = Array.prototype.slice.call(arguments, 1);
      }
      return argsToCheck.indexOf(value) !== -1;
    }
    return false;
  }

};
