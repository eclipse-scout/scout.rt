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

  if (this.yesButtonText) {
    this.$yesButton = this._createButton('yes', this.yesButtonText);
    if (!this.$defaultButton) {
      this.$defaultButton = this.$yesButton;
    }
  }
  if (this.noButtonText) {
    this.$noButton = this._createButton('no', this.noButtonText);
    if (!this.$defaultButton) {
      this.$defaultButton = this.$noButton;
    }
  }
  if (this.cancelButtonText) {
    this.$cancelButton = this._createButton('cancel', this.cancelButtonText);
    if (!this.$defaultButton) {
      this.$defaultButton = this.$cancelButton;
    }
  }
  this._updateButtonWidths();

  this.previouslyFocusedElemenet = document.activeElement;
  setTimeout(function() {
    //Class shown is used for css animation
    this.$container
      .addClass('shown')
      .show();

    if (this.$defaultButton) {
      this.$defaultButton.focus();
    }
  }.bind(this));

  //FIXME CGU this solution does not allow for backwards tabbing (shift+tab) on the first button. Better do it like jquery ui (listen for keydown events)?
  // Also make more generic to make it reusable by other elements (regular dialog, form)
  this.focusListener = function(event) {
    if (!this.$container[0].contains(event.target)) {
      event.stopPropagation();
      this.$container.find('button').eq(0).focus();
    }
  }.bind(this);
  document.addEventListener("focus", this.focusListener, true);
};

scout.MessageBox.prototype._renderProperties = function() {
  this._renderTitle(this.title);
  this._renderIconId(this.iconId);
  this._renderSeverity(this.severity);
  this._renderIntroText(this.introText);
  this._renderActionText(this.actionText);

  this.position();
};

scout.MessageBox.prototype._remove = function() {
  scout.MessageBox.parent.prototype._remove.call(this);
  document.removeEventListener("focus", this.focusListener, true);

  //FIXME CGU does not work, because button gets disabled when clicked (why??).
  this.previouslyFocusedElemenet.focus();
};

scout.MessageBox.prototype.position = function() {
  this.$container.cssMarginLeft(-this.$container.outerWidth() / 2);
};

scout.MessageBox.prototype._createButton = function(option, text) {
  text = scout.strings.removeAmpersand(text);
  return $('<button>')
    .text(text)
    .on('click', this._onButtonClicked.bind(this))
    .data('option', option)
    .appendTo(this.$buttons);
};

scout.MessageBox.prototype._onButtonClicked = function(event) {
  var $button = $(event.target);
  var option = $button.data('option');
  this.session.send('action', this.id, {option: option});
};

scout.MessageBox.prototype._renderTitle = function(title) {
  this.$title.html(scout.strings.nl2br(title));
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
  this.$introText.html(scout.strings.nl2br(text));
  this.$introText.setVisible(text);
};

scout.MessageBox.prototype._renderActionText = function(text) {
  this.$actionText.html(scout.strings.nl2br(text));
  this.$actionText.setVisible(text);
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
