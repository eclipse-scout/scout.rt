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
    scout.device.features['_touch'] = true;
  },

  scrollToBottom: function($element) {
    if ($element && $element.length > 0) {
      var scrollHeight = $element[0].scrollHeight;
      $element[0].scrollTop = scrollHeight;
    }
  }

};
