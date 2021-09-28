/*
 * Copyright (c) 2010-2021 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
/**
 * Utility functions that are used in the Selenium test suite (see SeleniumJavaScript.java).
 */
scout.selenium = {

  origSendCancelRequest: scout.Session.prototype._sendCancelRequest,

  delayCancelRequest: function(delayMs) {
    var origFunc = this.origSendCancelRequest;
    scout.Session.prototype._sendCancelRequest = function() {
      setTimeout(origFunc.bind(this), delayMs);
    };
  },

  restoreCancelRequest: function() {
    scout.Session.prototype._sendCancelRequest = this.origSendCancelRequest;
  },

  setSupportsTouch: touch => {
    scout.Device.get().features['_onlyTouch'] = touch;
  },

  scrollToBottom: $element => {
    if ($element && $element.length > 0) {
      $element[0].scrollTop = $element[0].scrollHeight;
    }
  },

  scrollToRight: $element => {
    if ($element && $element.length > 0) {
      $element[0].scrollLeft = $element[0].scrollWidth;
    }
  }

};
