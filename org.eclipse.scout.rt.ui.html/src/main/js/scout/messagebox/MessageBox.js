scout.MessageBox = function(model, session) {
  scout.MessageBox.parent.call(this);
  if (!(model instanceof scout.ModelAdapter)) {
    // If message box is used gui only, otherwise the model gets written by the model adapter.
    $.extend(this, model);
  }
  this.$container;
  this.$content;
  this.$title;
  this.$introText;
  this.$actionText;
  this.$buttons;
  this.$yesButton;
  this.$defaultButton;
  this.$noButton;
  this.$cancelButton;
  this.previouslyFocusedElement;
  this.focusListener;
  this._$glassPane;
  this._session = session;
  this._addEventSupport();
};
scout.inherits(scout.MessageBox, scout.Widget);

scout.MessageBox.prototype._render = function($parent) {
  if (!$parent) {
    throw new Error('Missing argument $parent');
  }

  this._$glassPane = scout.fields.new$Glasspane(this._session.uiSessionId).appendTo($parent);
  this.$container = this._$glassPane.appendDiv('messagebox');

  var $handle = this.$container.appendDiv('drag-handle');
  this.$container.makeDraggable($handle);

  this.$content = this.$container.appendDiv('messagebox-content');
  this.$title = this.$content.appendDiv('messagebox-label');
  this.$introText = this.$content.appendDiv('messagebox-label messagebox-intro-text');
  this.$actionText = this.$content.appendDiv('messagebox-label messagebox-action-text');
  this.$buttons = this.$container.appendDiv('messagebox-buttons');

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

  this.previouslyFocusedElement = document.activeElement;
  setTimeout(function() {
    // Class 'shown' is used for css animation
    this.$container.addClass('shown');
    this._$glassPane.installFocusContext('auto', this._session.uiSessionId);
    // Prevent resizing when message-box is dragged off the viewport
    this.$container.css('min-width', this.$container.width());
  }.bind(this));

  // Render properties
  this._renderTitle(this.title);
  this._renderIconId(this.iconId);
  this._renderSeverity(this.severity);
  this._renderIntroText(this.introText);
  this._renderActionText(this.actionText);

  // Now that all texts are set, we can calculate the position
  this._position();
  this.keyStrokeAdapter = this._createKeyStrokeAdapter();
};

scout.MessageBox.prototype._remove = function() {
  scout.MessageBox.parent.prototype._remove.call(this);
  this._$glassPane.fadeOutAndRemove();
};

scout.MessageBox.prototype._createKeyStrokeAdapter = function() {
  return new scout.MessageBoxKeyStrokeAdapter(this);
};

scout.MessageBox.prototype._position = function() {
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
  this.trigger('buttonClick', {
    option: $button.data('option')
  });
};

scout.MessageBox.prototype._renderTitle = function(title) {
  this.$title.html(scout.strings.nl2br(title));
  this.$title.setVisible(title);
};

scout.MessageBox.prototype._renderIconId = function(iconId) {
  // FIXME implement
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
