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
scout.Reconnector = function(session) {
  this.session = session;
  this.interval = 3000;
};

scout.Reconnector.prototype.start = function() {
  this._schedulePing();
};

scout.Reconnector.prototype._schedulePing = function() {
  if (this.pingScheduled) {
    return;
  }

  this.pingScheduled = true;
  setTimeout(function() {
    this.ping();
    this.pingScheduled = false;
  }.bind(this), this.interval);
};

scout.Reconnector.prototype.ping = function() {
  this.pingTime = new Date();
  this.session.onReconnecting();

  var request = {
    ping: true
  };

  var ajaxOptions = this.session.defaultAjaxOptions(request);

  $.ajax(ajaxOptions)
    .done(onAjaxDone.bind(this))
    .fail(onAjaxFail.bind(this));

  // --- Helper methods ---

  function onAjaxDone(data) {
    this._onSuccess(data);
  }

  function onAjaxFail(jqXHR, textStatus, errorThrown) {
    this._onFailure(request, jqXHR, textStatus, errorThrown);
  }
};

scout.Reconnector.prototype._onSuccess = function() {
  this.session.onReconnectingSucceeded();
};

scout.Reconnector.prototype._onFailure = function() {
  var minDuration = 1000;
  var pingDuration = new Date().getTime() - this.pingTime.getTime();

  if (pingDuration > minDuration) {
    this._onFailureImpl();
  } else {
    //Wait at least a certain time before informing about connection failure (to prevent flickering of the reconnecting notification)
    setTimeout(function() {
      this._onFailureImpl();
    }.bind(this), minDuration - pingDuration);
  }
};

scout.Reconnector.prototype._onFailureImpl = function() {
  this.session.onReconnectingFailed();
  this._schedulePing();
};
