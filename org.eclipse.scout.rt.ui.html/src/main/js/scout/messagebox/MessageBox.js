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
import {Device} from '../index';
import {ClickActiveElementKeyStroke} from '../index';
import {Form} from '../index';
import {objects} from '../index';
import {HtmlComponent} from '../index';
import {Status} from '../index';
import {keys} from '../index';
import {KeyStrokeContext} from '../index';
import {clipboard} from '../index';
import {CopyKeyStroke} from '../index';
import {AbortKeyStroke} from '../index';
import {strings} from '../index';
import {GlassPaneRenderer} from '../index';
import {BoxButtons} from '../index';
import {Widget} from '../index';
import {MessageBoxLayout} from '../index';
import {FocusAdjacentElementKeyStroke} from '../index';
import {FocusRule} from '../index';

export default class MessageBox extends Widget {

constructor() {
  super();

  this.severity = null;
  this.body = null;
  this.cancelButtonText = null;
  this.header = null;
  this.hiddenText = null;
  this.html = null;
  this.noButtonText = null;
  this.yesButtonText = null;
  this.$content = null;
  this.$header = null;
  this.$body = null;
  this.$buttons = null;
  this.$yesButton = null;
  this.$noButton = null;
  this.$cancelButton = null;
  this._$abortButton = null;
  this.displayParent = null;
}


static Buttons = {
  YES: 'yes',
  NO: 'no',
  CANCEL: 'cancel'
};

_init(model) {
  super._init( model);
  this._setDisplayParent(this.displayParent);
}

/**
 * @override
 */
_createKeyStrokeContext() {
  return new KeyStrokeContext();
}

/**
 * @override
 */
_initKeyStrokeContext() {
  super._initKeyStrokeContext();

  this.keyStrokeContext.registerKeyStroke([
    new CopyKeyStroke(this),
    new FocusAdjacentElementKeyStroke(this.session, this),
    new ClickActiveElementKeyStroke(this, [
      keys.SPACE, keys.ENTER
    ]),
    new AbortKeyStroke(this, function() {
      return this._$abortButton;
    }.bind(this))
  ]);
}

_render() {
  // Render modality glasspanes (must precede adding the message box to the DOM)
  this._glassPaneRenderer = new GlassPaneRenderer(this);
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

  var boxButtons = new BoxButtons(this.$buttons, this._onButtonClick.bind(this));
  this._$abortButton = null; // button to be executed when abort() is called, e.g. when ESCAPE is pressed
  if (this.yesButtonText) {
    this.$yesButton = boxButtons.addButton({
      text: this.yesButtonText,
      option: MessageBox.Buttons.YES
    });
    this._$abortButton = this.$yesButton;
  }
  if (this.noButtonText) {
    this.$noButton = boxButtons.addButton({
      text: this.noButtonText,
      option: MessageBox.Buttons.NO
    });
    this._$abortButton = this.$noButton;
  }
  if (this.cancelButtonText) {
    this.$cancelButton = boxButtons.addButton({
      text: this.cancelButtonText,
      option: MessageBox.Buttons.CANCEL
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

  this.htmlComp = HtmlComponent.install(this.$container, this.session);
  this.htmlComp.setLayout(new MessageBoxLayout(this));
  this.htmlComp.validateLayout();

  this.$container.addClassForAnimation('animate-open');
  this.$container.select();
}

get$Scrollable() {
  return this.$content;
}

_postRender() {
  super._postRender();
  this._installFocusContext();
}

_remove() {
  this._glassPaneRenderer.removeGlassPanes();
  this._uninstallFocusContext();
  super._remove();
}

_installFocusContext() {
  this.session.focusManager.installFocusContext(this.$container, FocusRule.AUTO);
}

_uninstallFocusContext() {
  this.session.focusManager.uninstallFocusContext(this.$container);
}

_renderIconId() {
  // TODO [7.0] bsh: implement
}

_renderSeverity() {
  this.$container.removeClass('severity-error');
  if (this.severity === Status.Severity.ERROR) {
    this.$container.addClass('severity-error');
  }
}

_renderHeader() {
  this.$header.html(strings.nl2br(this.header));
  this.$header.setVisible(this.header);
}

_renderBody() {
  this.$body.html(strings.nl2br(this.body));
  this.$body.setVisible(this.body);
}

_renderHtml() {
  this.$html.html(this.html);
  this.$html.setVisible(this.html);
  // Don't change focus when a link is clicked by mouse
  this.$html.find('a, .app-link')
    .attr('tabindex', '0')
    .unfocusable();
}

_renderHiddenText() {
  if (this.$hiddenText) {
    this.$hiddenText.remove();
  }
  if (this.hiddenText) {
    this.$hiddenText = this.$content.appendElement('<!-- \n' + this.hiddenText.replace(/<!--|-->/g, '') + '\n -->');
  }
}

_onMouseDown() {
  // If there is a dialog in the parent-hierarchy activate it in order to bring it on top of other dialogs.
  var parent = this.findParent(function(p) {
    return p instanceof Form && p.isDialog();
  });
  if (parent) {
    parent.activate();
  }
}

_setCopyable(copyable) {
  this.$header.toggleClass('copyable', copyable);
  this.$body.toggleClass('copyable', copyable);
  this.$html.toggleClass('copyable', copyable);
}

copy() {
  this._setCopyable(true);
  var myDocument = this.$container.document(true);
  var range = myDocument.createRange();
  range.selectNodeContents(this.$content[0]);
  var selection = this.$container.window(true).getSelection();
  selection.removeAllRanges();
  selection.addRange(range);
  myDocument.execCommand('copy');
}

_onCopy(event) {
  var ie = Device.get().isInternetExplorer();
  var clipboardData = ie ? this.$container.window(true).clipboardData : objects.optProperty(event, 'originalEvent', 'clipboardData');

  if (clipboardData) {
    // Internet Explorer only allows plain text (which must have data-type 'Text')
    if (!ie) {
      var htmlText = strings.join('<br/>',
        this.$header[0].outerHTML,
        this.$body[0].outerHTML,
        this.$html[0].outerHTML,
        this.hiddenText);
      clipboardData.setData('text/html', htmlText);
    }
    var dataType = ie ? 'Text' : 'text/plain';
    var plainText = strings.join('\n\n',
      this.$header.text(),
      this.$body.text(),
      this.$html.text(),
      this.hiddenText);
    clipboardData.setData(dataType, plainText);
    this.$container.window(true).getSelection().removeAllRanges();
    this._setCopyable(false);
    clipboard.showNotification(this);
    event.preventDefault(); // We want to write our data to the clipboard, not data from any user selection
  }
  // else: do default
}

_onButtonClick(event, option) {
  this.trigger('action', {
    option: option
  });
}

setDisplayParent(displayParent) {
  this.setProperty('displayParent', displayParent);
}

_setDisplayParent(displayParent) {
  this._setProperty('displayParent', displayParent);
  if (displayParent) {
    this.setParent(this.findDesktop().computeParentForDisplayParent(displayParent));
  }
}

/**
 * Renders the message box and links it with the display parent.
 */
open() {
  this.setDisplayParent(this.displayParent || this.session.desktop);
  this.displayParent.messageBoxController.registerAndRender(this);
}

/**
 * Destroys the message box and unlinks it from the display parent.
 */
close() {
  if (this.displayParent) {
    this.displayParent.messageBoxController.unregisterAndRemove(this);
  }
  this.destroy();
}

/**
 * Aborts the message box by using the default abort button. Used by the ESC key stroke.
 */
abort() {
  if (this._$abortButton && this.session.focusManager.requestFocus(this._$abortButton)) {
    this._$abortButton.click();
  }
}

/**
 * @override Widget.js
 */
_attach() {
  this.$parent.append(this.$container);
  super._attach();
}

/**
 * @override Widget.js
 */
_detach() {
  this.$container.detach();
  super._detach();
}
}
