// SCOUT GUI
// (c) Copyright 2013-2014, BSI Business Systems Integration AG

scout.BaseDesktop = function() {
  scout.BaseDesktop.parent.call(this);
};
scout.inherits(scout.BaseDesktop, scout.ModelAdapter);

scout.BaseDesktop.prototype.showMessage = function(message, type) {
  type = type || 'info';

  if (!this.$message) {
    this.$message = this.$parent.prependDiv('', type + '-message');
  }
  this.$message.text(message);
};

scout.BaseDesktop.prototype.goOffline = function() {
  scout.BaseDesktop.parent.prototype.goOffline.call(this);

  var message = 'Die Netzwerkverbindung ist unterbrochen.',
    $reconnect; //FIXME CGU translate

  if (this.$offline) {
    return;
  }

  this.$offline = this.$parent.prependDiv('', 'offline-message');
  this.$offline.text(message);
  $reconnect = this.$offline.appendDiv('', 'reconnect');
  $reconnect
    .text('Reconnecting...')
    .hide();
  if (scout.device.supportsCssAnimation()) {
    $reconnect.addClass('reconnect-animated');
  }
  this.layout.marginTop += this.$offline.outerHeight(true);
  this.layout.layout();
};

scout.BaseDesktop.prototype.goOnline = function() {
  scout.BaseDesktop.parent.prototype.goOnline.call(this);

  if (!this.hideOfflineMessagePending) {
    this.hideOfflineMessage();
  }
};

scout.BaseDesktop.prototype.hideOfflineMessage = function() {
  if (!this.$offline) {
    return;
  }

  this.layout.marginTop -= this.$offline.outerHeight(true);
  this.$offline.remove();
  this.layout.layout();
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
  var message = 'Die Verbindung wurde wieder hergestellt.'; //FIXME CGU translate
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
