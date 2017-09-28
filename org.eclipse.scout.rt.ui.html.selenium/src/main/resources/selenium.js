/*******************************************************************************
 * Copyright (c) 2010-2017 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
/**
 * This file adds some JavaScript patches which are required in our Selenium test suite.
 */
scout.selenium = {

  /* Properties */
  origSendCancelRequest: scout.Session.prototype._sendCancelRequest,

  /* Functions */
  delayCancelRequest: function(delayMs) {
    var origFunc = this.origSendCancelRequest,
      scoutSession = scout.sessions[0];

    scout.Session.prototype._sendCancelRequest = function() {
      setTimeout(origFunc.bind(scoutSession), delayMs);
    };
  },

  restoreCancelRequest: function() {
    scout.Session.prototype._sendCancelRequest = this.origSendCancelRequest;
  },

  setSupportsTouch: function(touch) {
    scout.device.features['_touch'] = touch;
    /* Also load FastClick because without it, we would not test the real thing */
    /* However: once FastClick is loaded, we cannot unload it. This means you   */
    /* should not switch touch mode in the middle of a selenium test.           */
    if (touch) {
      scout.device._loadFastClickDeferred();
    }
  },

  scrollToBottom: function($element) {
    if ($element && $element.length > 0) {
      var scrollHeight = $element[0].scrollHeight;
      $element[0].scrollTop = scrollHeight;
    }
  },

  scrollToRight: function($element) {
    if ($element && $element.length > 0) {
      var scrollWidth = $element[0].scrollWidth;
      $element[0].scrollLeft = scrollWidth;
    }
  }

};
