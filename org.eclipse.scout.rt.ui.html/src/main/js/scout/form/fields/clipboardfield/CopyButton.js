scout.CopyButton = function() {
  scout.CopyButton.parent.call(this);
  this.$input = null;
  this.label = 'copy';
  this.cssClass = 'copy-button';
};
scout.inherits(scout.CopyButton, scout.Button);

scout.CopyButton.prototype._doAction = function() {
  scout.CopyButton.parent.prototype._doAction.call(this);
  this.copyToClipboard();
};

scout.CopyButton.prototype.copyToClipboard = function() {
  if (!this.$input) {
    return;
  }
  this.$input.selectAllText();

  try {
    var successful = document.execCommand('copy');
    if (successful) {
      this._showSuccessMessage();
    } else {
      this._showFailedMessage();
    }
  } catch (error) {
    this._showFailedMessage(error);
  }
};

scout.CopyButton.prototype._showSuccessMessage = function() {
  var tooltip = this._createTooltip({
    parent: this,
    autoRemove: true,
    $anchor: this.$field,
    text: 'Copied successfully'
  });
  tooltip.render();
};

scout.CopyButton.prototype._showFailedMessage = function(error) {
  var tooltip = this._createTooltip({
    parent: this,
    autoRemove: true,
    $anchor: this.$field,
    text: 'Sorry, copying has failed',
    severity: scout.Status.Severity.ERROR
  });
  tooltip.render();
  $.log.warn('Copying has failed', error);
};
