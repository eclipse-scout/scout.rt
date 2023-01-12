/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */

// noinspection JSUnresolvedVariable

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
