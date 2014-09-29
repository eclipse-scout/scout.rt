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

scout.MessageBox.prototype._renderProperties = function() {
  this._renderTitle(this.title);
  this._renderIconId(this.iconId);
  this._renderIntroText(this.introText);
  this._renderActionText(this.actionText);
  this._renderYesButtonText(this.yesButtonText);
  this._renderNoButtonText(this.noButtonText);
  this._renderCancelButtonText(this.cancelButtonText);
};

scout.MessageBox.prototype._createButton = function(option) {
  return $('<button>')
    .appendTo(this.$container)
    .on('click', this._onButtonClicked.bind(this))
    .data('option', option);
};

scout.MessageBox.prototype._onButtonClicked = function(event) {
  var $button = $(event.target);
  var option = $button.data('option');
  this.session.send('action', this.id, {option: option});
};

scout.MessageBox.prototype._renderTitle = function(title) {
  this.$title.html($.nl2br(title));
  this.$title.setVisible(title);
};

scout.MessageBox.prototype._renderIconId = function(iconId) {
  //FIXME implement
};

scout.MessageBox.prototype._renderIntroText = function(text) {
  this.$introText.html($.nl2br(text));
  this.$introText.setVisible(text);
};

scout.MessageBox.prototype._renderActionText = function(text) {
  this.$actionText.html($.nl2br(text));
  this.$actionText.setVisible(text);
};

scout.MessageBox.prototype._renderYesButtonText = function(text) {
  this.$yesButton.text(text);
  this.$yesButton.setVisible(text);
};

scout.MessageBox.prototype._renderNoButtonText = function(text) {
  this.$noButton.text(text);
  this.$noButton.setVisible(text);
};

scout.MessageBox.prototype._renderCancelButtonText = function(text) {
  this.$cancelButton.text(text);
  this.$cancelButton.setVisible(text);
};

scout.MessageBox.prototype.onModelAction = function(event) {
  if (event.type === 'closed') {
    this.destroy();
    this.session.desktop.onMessageBoxClosed();
  }
};
