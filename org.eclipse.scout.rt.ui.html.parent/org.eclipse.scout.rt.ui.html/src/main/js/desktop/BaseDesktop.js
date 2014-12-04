// SCOUT GUI
// (c) Copyright 2013-2014, BSI Business Systems Integration AG

scout.BaseDesktop = function() {
  scout.BaseDesktop.parent.call(this);
};
scout.inherits(scout.BaseDesktop, scout.ModelAdapter);

scout.BaseDesktop.prototype.showFatalMessage = function(title, text, buttonName, buttonAction) {
  var that = this;
  var $glasspane = this.$parent.appendDiv('glasspane');
  var ui = new scout.MessageBox({
    title: title,
    iconId: undefined,
    severity: 4,
    introText: text,
    actionText: undefined,
    yesButtonText: buttonName,
    onButtonClicked: function($button, event) {
      buttonAction.apply(that);
    }
  });
  ui.render($glasspane);
  ui.renderProperties();
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
    .text(this.session.text('Reconnecting'))
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
