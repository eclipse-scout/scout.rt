scout.MessageBoxUI = function(model) {
  this.model = model || {};
};

scout.MessageBoxUI.prototype.render = function($parent) {
  if (!$parent) {
    throw new Error('Missing argument $parent');
  }
  this.$container = $.makeDiv('messagebox').hide().appendTo($parent);
  var $handle = this.$container.appendDiv('drag-handle');
  this.$container.makeDraggable($handle);

  this.$content = this.$container.appendDiv('messagebox-content');
  this.$title = this.$content.appendDiv('messagebox-label');
  this.$introText = this.$content.appendDiv('messagebox-label messagebox-intro-text');
  this.$actionText = this.$content.appendDiv('messagebox-label messagebox-action-text');

  this.$buttons = this.$container.appendDiv('messagebox-buttons');

  if (this.model.yesButtonText) {
    this.$yesButton = this._createButton('yes', this.model.yesButtonText);
    if (!this.$defaultButton) {
      this.$defaultButton = this.$yesButton;
    }
  }
  if (this.model.noButtonText) {
    this.$noButton = this._createButton('no', this.model.noButtonText);
    if (!this.$defaultButton) {
      this.$defaultButton = this.$noButton;
    }
  }
  if (this.model.cancelButtonText) {
    this.$cancelButton = this._createButton('cancel', this.model.cancelButtonText);
    if (!this.$defaultButton) {
      this.$defaultButton = this.$cancelButton;
    }
  }
  this._updateButtonWidths();

  this.previouslyFocusedElemenet = document.activeElement;
  setTimeout(function() {
    // Class 'shown' is used for css animation
    this.$container.addClass('shown').show();
    // Prevent resizing when messagebox is dragged off the viewport
    this.$container.css('min-width', this.$container.width());
    // Focus the default button
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

scout.MessageBoxUI.prototype.renderProperties = function() {
  this._renderTitle(this.model.title);
  this._renderIconId(this.model.iconId);
  this._renderSeverity(this.model.severity);
  this._renderIntroText(this.model.introText);
  this._renderActionText(this.model.actionText);

  // Now that all texts are set, we can calculate the position
  this._position();
};

scout.MessageBoxUI.prototype.remove = function() {
  document.removeEventListener("focus", this.focusListener, true);

  //FIXME CGU does not work, because button gets disabled when clicked (why??).
  this.previouslyFocusedElemenet.focus();
};

scout.MessageBoxUI.prototype._position = function() {
  this.$container.cssMarginLeft(-this.$container.outerWidth() / 2);
};

scout.MessageBoxUI.prototype._createButton = function(option, text) {
  text = scout.strings.removeAmpersand(text);
  return $('<button>')
    .text(text)
    .on('click', this._onButtonClicked.bind(this))
    .data('option', option)
    .appendTo(this.$buttons);
};

scout.MessageBoxUI.prototype._onButtonClicked = function(event) {
  var $button = $(event.target);
  if (this.model.onButtonClicked) {
    this.model.onButtonClicked($button, event);
  }
};

scout.MessageBoxUI.prototype._renderTitle = function(title) {
  this.$title.html(scout.strings.nl2br(title));
  this.$title.setVisible(title);
};

scout.MessageBoxUI.prototype._renderIconId = function(iconId) {
  //FIXME implement
};

scout.MessageBoxUI.prototype._renderSeverity = function(severity) {
  this.$container.removeClass('severity-error');
  if (severity === 4) {
    this.$container.addClass('severity-error');
  }
};

scout.MessageBoxUI.prototype._renderIntroText = function(text) {
  this.$introText.html(scout.strings.nl2br(text));
  this.$introText.setVisible(text);
};

scout.MessageBoxUI.prototype._renderActionText = function(text) {
  this.$actionText.html(scout.strings.nl2br(text));
  this.$actionText.setVisible(text);
};

scout.MessageBoxUI.prototype._updateButtonWidths = function() {
  // Find all visible buttons
  var $visibleButtons = [];
  this.$container.find('button').each(function() {
    var $button = $(this);
    if ($button.isVisible()) {
      $visibleButtons.push($button);
    }
  });
  // Set equal width in percent
  var width = (1 / $visibleButtons.length) * 100;
  $($visibleButtons).each(function() {
    this.css('width', width + '%');
  });
};
