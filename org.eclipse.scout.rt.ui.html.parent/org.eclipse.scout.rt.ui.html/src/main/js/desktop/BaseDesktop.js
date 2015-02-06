// SCOUT GUI
// (c) Copyright 2013-2014, BSI Business Systems Integration AG

scout.BaseDesktop = function() {
  scout.BaseDesktop.parent.call(this);
};
scout.inherits(scout.BaseDesktop, scout.ModelAdapter);

scout.BaseDesktop.prototype._renderProperties = function() {
  scout.BaseDesktop.parent.prototype._renderProperties.call(this);

  this._renderTitle(this.title);
};

scout.BaseDesktop.prototype._renderTitle = function(title) {
  var $scoutDivs = $('div.scout');
  if ($scoutDivs.length <= 1) { // only set document title in non-portlet case
    document.title = title;
  }
};

scout.BaseDesktop.prototype.showFatalMessage = function(options) {
  options = options || {};
  var that = this;
  var $glasspane = this.$parent.appendDiv('glasspane');
  var model = {
    title: options.title,
    iconId: options.iconId,
    severity: options.severity || 4,
    introText: options.text || options.introText,
    actionText: options.actionText,
    yesButtonText: options.yesButtonText,
    noButtonText: options.noButtonText,
    cancelButtonText: options.cancelButtonText
  };
  var ui = new scout.MessageBox(model);

  model.onButtonClicked = function($button, event) {
    var option = $button.data('option');
    // Close message box
    ui.remove();
    $glasspane.remove();
    // Custom actions
    if (option === 'yes' && options.yesButtonAction) {
      options.yesButtonAction.apply(that);
    } else if (option === 'no' && options.noButtonAction) {
      options.noButtonAction.apply(that);
    } else if (option === 'cancel' && options.cancelButtonAction) {
      options.cancelButtonAction.apply(that);
    }
  };

  ui.render($glasspane);
};

scout.BaseDesktop.prototype._goOffline = function() {
  var message = this.session.text('ConnectionInterrupted'),
    $reconnect;

  if (this.$offline) {
    return;
  }

  this.$offline = this.$parent.prependDiv('offline-message');
  this.$offline.text(message);
  $reconnect = this.$offline.appendDiv('reconnect');
  $reconnect
    .text(this.session.text('Reconnecting_'))
    .hide();
  if (scout.device.supportsCssAnimation()) {
    $reconnect.addClass('reconnect-animated');
  }
};

scout.BaseDesktop.prototype._goOnline = function() {
  if (!this.hideOfflineMessagePending) {
    this.hideOfflineMessage();
  }
};

scout.BaseDesktop.prototype.hideOfflineMessage = function() {
  if (!this.$offline) {
    return;
  }

  this.$offline.remove();
  this.hideOfflineMessagePending = false;
  this.$offline = null;
};

scout.BaseDesktop.prototype.onReconnecting = function() {
  if (!this.$offline) {
    return;
  }

  this.$offline.find('.reconnect').show();
  this._reconnectionTimestamp = new Date();
};

scout.BaseDesktop.prototype.onReconnectingSucceeded = function() {
  var message = this.session.text('ConnectionReestablished');
  if (!this.$offline) {
    return;
  }

  this.$offline.find('.reconnect').hide();
  this.$offline.text(message);
  this.$offline.addClass('reconnect-successful');
  this.hideOfflineMessagePending = true;
  setTimeout(this.hideOfflineMessage.bind(this), 3000);
};

scout.BaseDesktop.prototype.onReconnectingFailed = function() {
  if (!this.$offline) {
    return;
  }

  this.$offline.find('.reconnect').hide();
};
