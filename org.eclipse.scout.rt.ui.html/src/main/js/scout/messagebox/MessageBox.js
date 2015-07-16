scout.MessageBox = function(model, session) {
  scout.MessageBox.parent.call(this);
  if (!(model instanceof scout.ModelAdapter)) {
    // If message box is used gui only, otherwise the model gets written by the model adapter.
    $.extend(this, model);
  }
  this.$container;
  this.$content;
  this.$header;
  this.$body;
  this.$buttons;
  this.$yesButton;
  this.$noButton;
  this.$cancelButton;
  this._$closeButton;
  this.focusListener;
  this.session = session;
  this._addEventSupport();
  this.attached = false; // Indicates whether this message box is currently visible to the user.
};
scout.inherits(scout.MessageBox, scout.Widget);

// represents severity codes from IStatus
scout.MessageBox.SEVERITY = {
  OK: 1,
  INFO: 256,
  WARNING: 65536,
  ERROR: 16777216
};

scout.MessageBox.prototype._render = function($parent) {
  if (!$parent) {
    throw new Error('Missing argument $parent');
  }
  this._$parent = $parent;

  // Add modality glassPane; must precede appending the message box to the DOM.
  this._modalityController = new scout.ModalityController(this);
  this._modalityController.addGlassPane();

  this.$container = $parent.appendDiv('messagebox');

  var $handle = this.$container.appendDiv('drag-handle');
  this.$container.makeDraggable($handle);

  this.$content = this.$container.appendDiv('messagebox-content');
  this.$header = this.$content.appendDiv('messagebox-label messagebox-header');
  this.$body = this.$content.appendDiv('messagebox-label messagebox-body');
  this.$html = this.$content.appendDiv('messagebox-label messagebox-html');
  this.$buttons = this.$container.appendDiv('messagebox-buttons');

  this._$closeButton = null; // button to be executed when close() is called, e.g. when ESCAPE is pressed
  if (this.yesButtonText) {
    this.$yesButton = this._createButton('yes', this.yesButtonText);
    this._$closeButton = this.$yesButton;
  }
  if (this.noButtonText) {
    this.$noButton = this._createButton('no', this.noButtonText);
    this._$closeButton = this.$noButton;
  }
  if (this.cancelButtonText) {
    this.$cancelButton = this._createButton('cancel', this.cancelButtonText);
    this._$closeButton = this.$cancelButton;
  }
  this._updateButtonWidths();

  // Render properties
  this._renderIconId(this.iconId);
  this._renderSeverity(this.severity);
  this._renderHeader(this.header);
  this._renderBody(this.body);
  this._renderHtml(this.html);
  this._renderHiddenText(this.hiddenText);

  this.keyStrokeAdapter = this._createKeyStrokeAdapter();

  // TODO Somehow let the user copy the 'copyPasteText' - but how?

  // Prevent resizing when message-box is dragged off the viewport
  this.$container.addClass('calc-helper');
  this.$container.css('min-width', this.$container.width());
  this.$container.removeClass('calc-helper');
  // Now that all texts, paddings, widths etc. are set, we can calculate the position
  this._position();
  // Class 'shown' is used for css animation
  this.$container.addClass('shown');

  this.attached = true;
};

scout.MessageBox.prototype._postRender = function() {
  this.$container.installFocusContext('auto', this.session.uiSessionId);
};

scout.MessageBox.prototype._remove = function() {
  this.$container.uninstallFocusContext(this.session.uiSessionId);
  this._modalityController.removeGlassPane();
  this.attached = false;

  scout.MessageBox.parent.prototype._remove.call(this);
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
    .data('buttonOption', option)
    .appendTo(this.$buttons);
};

scout.MessageBox.prototype._onButtonClicked = function(event) {
  var $button = $(event.target);
  this.trigger('buttonClick', {
    option: $button.data('buttonOption')
  });
};

scout.MessageBox.prototype._renderIconId = function(iconId) {
  // FIXME implement
};

scout.MessageBox.prototype._renderSeverity = function(severity) {
  this.$container.removeClass('severity-error');
  if (severity === scout.MessageBox.SEVERITY.ERROR) {
    this.$container.addClass('severity-error');
  }
};

scout.MessageBox.prototype._renderHeader = function(text) {
  this.$header.html(scout.strings.nl2br(text));
  this.$header.setVisible(text);
};

scout.MessageBox.prototype._renderBody = function(text) {
  this.$body.html(scout.strings.nl2br(text));
  this.$body.setVisible(text);
};

scout.MessageBox.prototype._renderHtml = function(text) {
  this.$html.html(text);
  this.$html.setVisible(text);
};

scout.MessageBox.prototype._renderHiddenText = function(text) {
  if (this.$hiddenText) {
    this.$hiddenText.remove();
  }
  if (text) {
    this.$hiddenText = $('<!-- \n' + text.replace(/<!--|-->/g, '') + '\n -->').appendTo(this.$content);
  }
};

scout.MessageBox.prototype._renderCopyPasteText = function(text) {
  // nop
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

scout.MessageBox.prototype.close = function() {
  if (this._$closeButton) {
    this._$closeButton.focus();
    this._$closeButton.click();
  }
};

/**
 * === Method required for objects attached to a 'displayParent' ===
 *
 * Method invoked once the 'displayParent' is attached;
 *
 *  In contrast to 'render/remove', this method uses 'JQuery attach/detach mechanism' to retain CSS properties, so that the model must not be interpreted anew.
 *  This method has no effect if already attached.
 */
scout.MessageBox.prototype.attach = function() {
  if (this.attached || !this.rendered) {
    return;
  }

  this._$parent.append(this.$container);
  this.$container.installFocusContext('auto', this.session.uiSessionId);
  this.session.detachHelper.afterAttach(this.$container);

  if (this.keyStrokeAdapter) {
    scout.keyStrokeManager.installAdapter(this.$container, this.keyStrokeAdapter);
  }

  this.attached = true;
};

/**
 * === Method required for objects attached to a 'displayParent' ===
 *
 * Method invoked once the 'displayParent' is detached;
 *
 *  In contrast to 'render/remove', this method uses 'JQuery attach/detach mechanism' to retain CSS properties, so that the model must not be interpreted anew.
 *  This method has no effect if already detached.
 */
scout.MessageBox.prototype.detach = function() {
  if (!this.attached || !this.rendered) {
    return;
  }

  if (scout.keyStrokeManager.isAdapterInstalled(this.keyStrokeAdapter)) {
    scout.keyStrokeManager.uninstallAdapter(this.keyStrokeAdapter);
  }

  this.session.detachHelper.beforeDetach(this.$container);
  this.$container.uninstallFocusContext(this.session.uiSessionId);
  this.$container.detach();

  this.attached = false;
};
