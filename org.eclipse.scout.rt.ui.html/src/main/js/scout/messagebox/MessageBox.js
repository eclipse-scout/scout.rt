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
};
scout.inherits(scout.MessageBox, scout.ModelAdapter);

// represents severity codes from IStatus
scout.MessageBox.SEVERITY = {
  OK: 1,
  INFO: 256,
  WARNING: 65536,
  ERROR: 16777216
};

/**
 * @override ModelAdapter
 */
scout.MessageBox.prototype._initKeyStrokeContext = function(keyStrokeContext) {
  scout.MessageBox.parent.prototype._initKeyStrokeContext.call(this, keyStrokeContext);

  keyStrokeContext.registerKeyStroke([
    new scout.FocusAdjacentElementKeyStroke(this.session, this),
    new scout.ClickActiveElementKeyStroke(this, [
      scout.keys.SPACE, scout.keys.ENTER
    ]),
    new scout.CloseKeyStroke(this, function() {
      return this._$closeButton;
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
  this.$html = this.$content.appendDiv('messagebox-label messagebox-html');
  this.$buttons = this.$container.appendDiv('messagebox-buttons');

  var boxButtons = new scout.BoxButtons(this.$buttons, this._onButtonClick.bind(this));
  this._$closeButton = null; // button to be executed when close() is called, e.g. when ESCAPE is pressed
  if (this.yesButtonText) {
    this.$yesButton = boxButtons.addButton({
      text: this.yesButtonText,
      option: 'yes'
    });
    this._$closeButton = this.$yesButton;
  }
  if (this.noButtonText) {
    this.$noButton = boxButtons.addButton({
      text: this.noButtonText,
      option: 'no'
    });
    this._$closeButton = this.$noButton;
  }
  if (this.cancelButtonText) {
    this.$cancelButton = boxButtons.addButton({
      text: this.cancelButtonText,
      option: 'cancel'
    });
    this._$closeButton = this.$cancelButton;
  }

  // Render properties
  this._renderIconId(this.iconId);
  this._renderSeverity(this.severity);
  this._renderHeader(this.header);
  this._renderBody(this.body);
  this._renderHtml(this.html);
  this._renderHiddenText(this.hiddenText);

  // FIXME bsh: Somehow let the user copy the 'copyPasteText' - but how?

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

scout.MessageBox.prototype._renderIconId = function(iconId) {
  // FIXME bsh: implement
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
    this.$hiddenText = this.$content.appendElement('<!-- \n' + text.replace(/<!--|-->/g, '') + '\n -->');
  }
};

scout.MessageBox.prototype._renderCopyPasteText = function(text) {
  // nop
};

scout.MessageBox.prototype._onButtonClick = function(event, option) {
  this._send('action', {
    option: option
  });
};

/**
 * Used by CloseKeyStroke.js
 */
scout.MessageBox.prototype.close = function() {
  if (this._$closeButton && this.session.focusManager.requestFocus(this._$closeButton)) {
    this._$closeButton.click();
  }
};

/**
 * @override Widget.js
 */
scout.MessageBox.prototype._attach = function() {
  this._$parent.append(this.$container);
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
