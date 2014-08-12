scout.MessageBox = function() {
  scout.MessageBox.parent.call(this);
};
scout.inherits(scout.MessageBox, scout.ModelAdapter);

scout.MessageBox.prototype._render = function($parent) {
  this.$container = $parent.appendDIV('messagebox');

  this.$title = this.$container.appendDIV('messagebox-label');
  this.$introText = this.$container.appendDIV('messagebox-label');
  this.$actionText = this.$container.appendDIV('messagebox-label');

  this.$yesButton = this._createButton('yes');
  this.$noButton = this._createButton('no');
  this.$cancelButton = this._createButton('cancel');
};

scout.MessageBox.prototype._callSetters = function() {
  this._setTitle(this.title);
  this._setIconId(this.iconId);
  this._setIntroText(this.introText);
  this._setActionText(this.actionText);
  this._setYesButtonText(this.yesButtonText);
  this._setNoButtonText(this.noButtonText);
  this._setCancelButtonText(this.cancelButtonText);
};

scout.MessageBox.prototype._createButton = function(option) {
  return $('<button>')
    .appendTo(this.$container)
    .on('click', this._onButtonClicked.bind(this))
    .data('option', option);
};

scout.MessageBox.prototype._onButtonClicked = function() {
  var $button = $(event.target);
  var option = $button.data('option');
  this.session.send('action', this.id, {option: option});
};

scout.MessageBox.prototype._setTitle = function(title) {
  this.$title.html($.nl2br(title));
  this.$title.setVisible(title);
};

scout.MessageBox.prototype._setIconId = function(iconId) {
  //FIXME implement
};

scout.MessageBox.prototype._setIntroText = function(text) {
  this.$introText.html($.nl2br(text));
  this.$introText.setVisible(text);
};

scout.MessageBox.prototype._setActionText = function(text) {
  this.$actionText.html($.nl2br(text));
  this.$actionText.setVisible(text);
};

scout.MessageBox.prototype._setYesButtonText = function(text) {
  this.$yesButton.text(text);
  this.$yesButton.setVisible(text);
};

scout.MessageBox.prototype._setNoButtonText = function(text) {
  this.$noButton.text(text);
  this.$noButton.setVisible(text);
};

scout.MessageBox.prototype._setCancelButtonText = function(text) {
  this.$cancelButton.text(text);
  this.$cancelButton.setVisible(text);
};

scout.MessageBox.prototype.onModelAction = function(event) {
  if (event.type === 'closed') {
    this.destroy();
    this.session.desktop.onMessageBoxClosed();
  }
};
