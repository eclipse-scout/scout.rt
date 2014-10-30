scout.MessageBox = function() {
  scout.MessageBox.parent.call(this);
};
scout.inherits(scout.MessageBox, scout.ModelAdapter);

scout.MessageBox.prototype._render = function($parent) {
  this.$container = $.makeDIV('messagebox').hide().appendTo($parent);

  this.$content = this.$container.appendDIV('messagebox-content');
  this.$title = this.$content.appendDIV('messagebox-label');
  this.$introText = this.$content.appendDIV('messagebox-label messagebox-intro-text');
  this.$actionText = this.$content.appendDIV('messagebox-label messagebox-action-text');

  this.$buttons = this.$container.appendDIV('messagebox-buttons');
  this.$yesButton = this._createButton('yes');
  this.$noButton = this._createButton('no');
  this.$cancelButton = this._createButton('cancel');

  setTimeout(function() {
    //Class shown is used for css animation
    this.$container
      .addClass('shown')
      .show();
  }.bind(this));
};

scout.MessageBox.prototype._renderProperties = function() {
  this._renderTitle(this.title);
  this._renderIconId(this.iconId);
  this._renderSeverity(this.severity);
  this._renderIntroText(this.introText);
  this._renderActionText(this.actionText);
  this._renderYesButtonText(this.yesButtonText);
  this._renderNoButtonText(this.noButtonText);
  this._renderCancelButtonText(this.cancelButtonText);

  this.position();
};

scout.MessageBox.prototype.position = function() {
  this.$container.cssMarginLeft(-this.$container.outerWidth() / 2);
};

scout.MessageBox.prototype._createButton = function(option) {
  return $('<button>')
    .appendTo(this.$buttons)
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

scout.MessageBox.prototype._renderSeverity = function(severity) {
  this.$container.removeClass('severity-error');
  if (severity === 4) {
    this.$container.addClass('severity-error');
  }
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
  this._renderButton(this.$yesButton, text);
};

scout.MessageBox.prototype._renderNoButtonText = function(text) {
  this._renderButton(this.$noButton, text);
};

scout.MessageBox.prototype._renderCancelButtonText = function(text) {
  this._renderButton(this.$cancelButton, text);
};

scout.MessageBox.prototype._renderButton = function($button, text) {
  $button.text(text);
  $button.setVisible(text);
  this._updateButtonWidths();
};

scout.MessageBox.prototype._updateButtonWidths = function() {
  var numVisibleButtons = 0,
    width = 1, $button,
    $buttons = this.$container.find('button');

  $buttons.each(function() {
    if ($(this).isVisible()) {
      numVisibleButtons++;
    }
  });

  width = (width / numVisibleButtons) * 100;
  $buttons.each(function() {
    $button = $(this);
    if ($button.isVisible()) {
      $button.css('width', width + '%');
    }
  });
};

scout.MessageBox.prototype.onModelAction = function(event) {
  if (event.type === 'closed') {
    this.destroy();
    this.session.desktop.onMessageBoxClosed(this);
  }
};
