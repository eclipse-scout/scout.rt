/*
 * Copyright (c) 2014-2018 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
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
  this.displayParent = null;
};
scout.inherits(scout.MessageBox, scout.Widget);

scout.MessageBox.Buttons = {
  YES: 'yes',
  NO: 'no',
  CANCEL: 'cancel'
};

scout.MessageBox.prototype._init = function(model) {
  scout.MessageBox.parent.prototype._init.call(this, model);
  this._setDisplayParent(this.displayParent);
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
    new scout.CopyKeyStroke(this),
    new scout.FocusAdjacentElementKeyStroke(this.session, this),
    new scout.ClickActiveElementKeyStroke(this, [
      scout.keys.SPACE, scout.keys.ENTER
    ]),
    new scout.AbortKeyStroke(this, function() {
      return this._$abortButton;
    }.bind(this))
  ]);
};

scout.MessageBox.prototype._render = function() {
  // Render modality glasspanes (must precede adding the message box to the DOM)
  this._glassPaneRenderer = new scout.GlassPaneRenderer(this);
  this._glassPaneRenderer.renderGlassPanes();

  this.$container = this.$parent.appendDiv('messagebox')
    .on('mousedown', this._onMouseDown.bind(this))
    .on('copy', this._onCopy.bind(this));

  var $handle = this.$container.appendDiv('drag-handle');
  this.$container.draggable($handle);

  this.$content = this.$container.appendDiv('messagebox-content');
  this.$header = this.$content.appendDiv('messagebox-label messagebox-header');
  this.$body = this.$content.appendDiv('messagebox-label messagebox-body');
  this.$html = this.$content.appendDiv('messagebox-label messagebox-html prevent-initial-focus');
  this.$buttons = this.$container.appendDiv('messagebox-buttons')
    .on('copy', this._onCopy.bind(this));

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

  this._installScrollbars({
    axis: 'y'
  });

  // Render properties
  this._renderIconId();
  this._renderSeverity();
  this._renderHeader();
  this._renderBody();
  this._renderHtml();
  this._renderHiddenText();

  // Prevent resizing when message-box is dragged off the viewport
  this.$container.addClass('calc-helper');
  var naturalWidth = this.$container.width();
  this.$container.removeClass('calc-helper');
  this.$container.css('min-width', Math.max(naturalWidth, boxButtons.buttonCount() * 100));
  boxButtons.updateButtonWidths(this.$container.width());

  this.htmlComp = scout.HtmlComponent.install(this.$container, this.session);
  this.htmlComp.setLayout(new scout.MessageBoxLayout(this));
  this.htmlComp.pixelBasedSizing = true;
  this.htmlComp.validateLayout();

  this.$container.addClassForAnimation('animate-open');
  this.$container.select();
};

scout.MessageBox.prototype.get$Scrollable = function() {
  return this.$content;
};

scout.MessageBox.prototype._postRender = function() {
  scout.MessageBox.parent.prototype._postRender.call(this);
  this._installFocusContext();
};

scout.MessageBox.prototype._remove = function() {
  this._glassPaneRenderer.removeGlassPanes();
  this._uninstallFocusContext();
  scout.MessageBox.parent.prototype._remove.call(this);
};

scout.MessageBox.prototype._installFocusContext = function() {
  this.session.focusManager.installFocusContext(this.$container, scout.focusRule.AUTO);
};

scout.MessageBox.prototype._uninstallFocusContext = function() {
  this.session.focusManager.uninstallFocusContext(this.$container);
};

scout.MessageBox.prototype._renderIconId = function() {
  // TODO [7.0] bsh: implement
};

scout.MessageBox.prototype._renderSeverity = function() {
  this.$container.removeClass('severity-error');
  if (this.severity === scout.Status.Severity.ERROR) {
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

scout.MessageBox.prototype._onMouseDown = function() {
  // If there is a dialog in the parent-hierarchy activate it in order to bring it on top of other dialogs.
  var parent = this.findParent(function(p) {
    return p instanceof scout.Form && p.isDialog();
  });
  if (parent) {
    parent.activate();
  }
};

scout.MessageBox.prototype._setCopyable = function(copyable) {
  this.$header.toggleClass('copyable', copyable);
  this.$body.toggleClass('copyable', copyable);
  this.$html.toggleClass('copyable', copyable);
};

scout.MessageBox.prototype.copy = function() {
  this._setCopyable(true);
  var myDocument = this.$container.document(true);
  var range = myDocument.createRange();
  range.selectNodeContents(this.$content[0]);
  var selection = this.$container.window(true).getSelection();
  selection.removeAllRanges();
  selection.addRange(range);
  myDocument.execCommand('copy');
};

scout.MessageBox.prototype._onCopy = function(event) {
  var ie = scout.device.isInternetExplorer();
  var clipboardData = ie ? this.$container.window(true).clipboardData : scout.objects.optProperty(event, 'originalEvent', 'clipboardData');

  if (clipboardData) {
    // Internet Explorer only allows plain text (which must have data-type 'Text')
    if (!ie) {
      var htmlText = scout.strings.join('<br/>',
        this.$header[0].outerHTML,
        this.$body[0].outerHTML,
        this.$html[0].outerHTML,
        this.hiddenText);
      clipboardData.setData('text/html', htmlText);
    }
    var dataType = ie ? 'Text' : 'text/plain';
    var plainText = scout.strings.join('\n\n',
      this.$header.text(),
      this.$body.text(),
      this.$html.text(),
      this.hiddenText);
    clipboardData.setData(dataType, plainText);
    this.$container.window(true).getSelection().removeAllRanges();
    this._setCopyable(false);
    scout.clipboard.showNotification(this);
    event.preventDefault(); // We want to write our data to the clipboard, not data from any user selection
  }
  // else: do default
};

scout.MessageBox.prototype._onButtonClick = function(event, option) {
  this.trigger('action', {
    option: option
  });
};

scout.MessageBox.prototype.setDisplayParent = function(displayParent) {
  this.setProperty('displayParent', displayParent);
};

scout.MessageBox.prototype._setDisplayParent = function(displayParent) {
  this._setProperty('displayParent', displayParent);
  if (displayParent) {
    this.setParent(this.findDesktop().computeParentForDisplayParent(displayParent));
  }
};

/**
 * Renders the message box and links it with the display parent.
 */
scout.MessageBox.prototype.open = function() {
  this.setDisplayParent(this.displayParent || this.session.desktop);
  this.displayParent.messageBoxController.registerAndRender(this);
};

/**
 * Destroys the message box and unlinks it from the display parent.
 */
scout.MessageBox.prototype.close = function() {
  if (this.displayParent) {
    this.displayParent.messageBoxController.unregisterAndRemove(this);
  }
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
  scout.MessageBox.parent.prototype._attach.call(this);
};

/**
 * @override Widget.js
 */
scout.MessageBox.prototype._detach = function() {
  this.$container.detach();
  scout.MessageBox.parent.prototype._detach.call(this);
};
