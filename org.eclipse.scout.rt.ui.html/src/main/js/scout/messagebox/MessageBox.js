/*******************************************************************************
 * Copyright (c) 2014-2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
scout.MessageBox = function() {
  scout.MessageBox.parent.call(this);

  this.iconId;
  this.severity;
  this.body;
  this.cancelButtonText;
  this.header;
  this.hiddenText;
  this.html;
  this.noButtonText;
  this.yesButtonText;
  this.$container;
  this.$content;
  this.$header;
  this.$body;
  this.$buttons;
  this.$yesButton;
  this.$noButton;
  this.$cancelButton;
  this._$abortButton;
  this.displayParent;
};
scout.inherits(scout.MessageBox, scout.Widget);

// represents severity codes from IStatus // FIXME [awe] 6.1 - same as scout.Status.Severity!? merge
scout.MessageBox.SEVERITY = {
  OK: 1,
  INFO: 256,
  WARNING: 65536,
  ERROR: 16777216
};

scout.MessageBox.Buttons = {
  YES: 'yes',
  NO: 'no',
  CANCEL: 'cancel'
};

/**
 * @override
 */
scout.MessageBox.prototype._createKeyStrokeContext = function() {
  return new scout.KeyStrokeContext();
};

/**
 * @override
 */
scout.MessageBox.prototype._initKeyStrokeContext = function() {
  scout.MessageBox.parent.prototype._initKeyStrokeContext.call(this);

  this.keyStrokeContext.registerKeyStroke([
    new scout.FocusAdjacentElementKeyStroke(this.session, this),
    new scout.ClickActiveElementKeyStroke(this, [
      scout.keys.SPACE, scout.keys.ENTER
    ]),
    new scout.AbortKeyStroke(this, function() {
      return this._$abortButton;
    }.bind(this))
  ]);
};

scout.MessageBox.prototype._render = function($parent) {
  if (!$parent) {
    throw new Error('Missing argument $parent');
  }
  // Render modality glasspanes (must precede adding the message box to the DOM)
  this._glassPaneRenderer = new scout.GlassPaneRenderer(this.session, this, true);
  this._glassPaneRenderer.renderGlassPanes();

  this.$container = $parent.appendDiv('messagebox');

  var $handle = this.$container.appendDiv('drag-handle');
  this.$container.makeDraggable($handle);

  this.$content = this.$container.appendDiv('messagebox-content');
  this.$header = this.$content.appendDiv('messagebox-label messagebox-header');
  this.$body = this.$content.appendDiv('messagebox-label messagebox-body');
  this.$html = this.$content.appendDiv('messagebox-label messagebox-html prevent-initial-focus');
  this.$buttons = this.$container.appendDiv('messagebox-buttons');

  var boxButtons = new scout.BoxButtons(this.$buttons, this._onButtonClick.bind(this));
  this._$abortButton = null; // button to be executed when abort() is called, e.g. when ESCAPE is pressed
  if (this.yesButtonText) {
    this.$yesButton = boxButtons.addButton({
      text: this.yesButtonText,
      option: scout.MessageBox.Buttons.YES
    });
    this._$abortButton = this.$yesButton;
  }
  if (this.noButtonText) {
    this.$noButton = boxButtons.addButton({
      text: this.noButtonText,
      option: scout.MessageBox.Buttons.NO
    });
    this._$abortButton = this.$noButton;
  }
  if (this.cancelButtonText) {
    this.$cancelButton = boxButtons.addButton({
      text: this.cancelButtonText,
      option: scout.MessageBox.Buttons.CANCEL
    });
    this._$abortButton = this.$cancelButton;
  }

  // Render properties
  this._renderIconId();
  this._renderSeverity();
  this._renderHeader();
  this._renderBody();
  this._renderHtml();
  this._renderHiddenText();

  // TODO [6.2] bsh: Somehow let the user copy the 'copyPasteText' - but how?

  // Prevent resizing when message-box is dragged off the viewport
  this.$container.addClass('calc-helper');
  var naturalWidth = this.$container.width();
  this.$container.removeClass('calc-helper');
  this.$container.css('min-width', Math.max(naturalWidth, boxButtons.buttonCount() * 100));
  boxButtons.updateButtonWidths(this.$container.width());
  // Now that all texts, paddings, widths etc. are set, we can calculate the position
  this._position();
  this.$container.addClassForAnimation('animate-open');
};

scout.MessageBox.prototype._postRender = function() {
  scout.MessageBox.parent.prototype._postRender.call(this);
  this.session.focusManager.installFocusContext(this.$container, scout.focusRule.AUTO);
};

scout.MessageBox.prototype._remove = function() {
  this._glassPaneRenderer.removeGlassPanes();
  this.session.focusManager.uninstallFocusContext(this.$container);
  scout.MessageBox.parent.prototype._remove.call(this);
};

scout.MessageBox.prototype._position = function() {
  this.$container.cssMarginLeft(-this.$container.outerWidth() / 2);
};

scout.MessageBox.prototype._renderIconId = function() {
  // TODO [6.2] bsh: implement
};

scout.MessageBox.prototype._renderSeverity = function() {
  this.$container.removeClass('severity-error');
  if (this.severity === scout.MessageBox.SEVERITY.ERROR) {
    this.$container.addClass('severity-error');
  }
};

scout.MessageBox.prototype._renderHeader = function() {
  this.$header.html(scout.strings.nl2br(this.header));
  this.$header.setVisible(this.header);
};

scout.MessageBox.prototype._renderBody = function() {
  this.$body.html(scout.strings.nl2br(this.body));
  this.$body.setVisible(this.body);
};

scout.MessageBox.prototype._renderHtml = function() {
  this.$html.html(this.html);
  this.$html.setVisible(this.html);
  // Don't change focus when a link is clicked by mouse
  this.$html.find('a, .app-link')
    .attr('tabindex', '0')
    .unfocusable();
};

scout.MessageBox.prototype._renderHiddenText = function() {
  if (this.$hiddenText) {
    this.$hiddenText.remove();
  }
  if (this.hiddenText) {
    this.$hiddenText = this.$content.appendElement('<!-- \n' + this.hiddenText.replace(/<!--|-->/g, '') + '\n -->');
  }
};

scout.MessageBox.prototype._onButtonClick = function(event, option) {
  this.trigger('action', {
    option: option
  });
};

/**
 * Renders the message box and links it with the display parent.
 */
scout.MessageBox.prototype.open = function() {
  this.displayParent = this.displayParent || this.session.desktop;
  this.displayParent.messageBoxController.registerAndRender(this);
};

/**
 * Destroys the message box and unlinks it from the display parent.
 */
scout.MessageBox.prototype.close = function() {
  this.displayParent = this.displayParent || this.session.desktop;
  this.displayParent.messageBoxController.unregisterAndRemove(this);
  this.destroy();
};

/**
 * Aborts the message box by using the default abort button. Used by the ESC key stroke.
 */
scout.MessageBox.prototype.abort = function() {
  if (this._$abortButton && this.session.focusManager.requestFocus(this._$abortButton)) {
    this._$abortButton.click();
  }
};

/**
 * @override Widget.js
 */
scout.MessageBox.prototype._attach = function() {
  this.$parent.append(this.$container);
  this.session.detachHelper.afterAttach(this.$container);
  scout.MessageBox.parent.prototype._attach.call(this);
};

/**
 * @override Widget.js
 */
scout.MessageBox.prototype._detach = function() {
  this.session.detachHelper.beforeDetach(this.$container);
  this.$container.detach();
  scout.MessageBox.parent.prototype._detach.call(this);
};

