/*
 * Copyright (c) 2010-2022 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
import {AbortKeyStroke, BoxButtons, ClickActiveElementKeyStroke, clipboard, CopyKeyStroke, FocusAdjacentElementKeyStroke, FocusRule, Form, GlassPaneRenderer, HtmlComponent, keys, KeyStrokeContext, MessageBoxLayout, objects, scout, Status, strings, Widget} from '../index';

export default class MessageBox extends Widget {

  constructor() {
    super();

    this.severity = Status.Severity.INFO;
    this.body = null;
    this.iconId = null;
    this.header = null;
    this.hiddenText = null;
    this.html = null;
    this.yesButtonText = null;
    this.noButtonText = null;
    this.cancelButtonText = null;
    this.displayParent = null;

    this.buttons = [];
    this.boxButtons = null;
    this.yesButton = null;
    this.noButton = null;
    this.cancelButton = null;
    this.abortButton = null; // button to be executed when abort() is called, e.g. when ESCAPE is pressed. points to the last (most right) button in the list (one of yes, no or cancel)
    this.inheritAccessibility = false; // do not inherit enabled-state by default. Otherwise the MessageBox cannot be closed anymore
    this.$content = null;
    this.$header = null;
    this.$body = null;
    this.$buttons = null;
    this._icon = null;

    this._addWidgetProperties(['buttons', 'boxButtons', 'yesButton', 'noButton', 'cancelButton', 'abortButton']);
  }

  static Buttons = {
    YES: 'yes',
    NO: 'no',
    CANCEL: 'cancel'
  };

  _init(model) {
    super._init(model);
    this._setDisplayParent(this.displayParent);
    this._setIconId(this.iconId);

    this.boxButtons = scout.create('BoxButtons', {parent: this});
    this.yesButton = this._createMessageBoxButton(this.yesButtonText, MessageBox.Buttons.YES);
    this.noButton = this._createMessageBoxButton(this.noButtonText, MessageBox.Buttons.NO);
    this.cancelButton = this._createMessageBoxButton(this.cancelButtonText, MessageBox.Buttons.CANCEL);
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
      new AbortKeyStroke(this, () => {
        if (this.abortButton) {
          return this.abortButton.$container;
        }
        return null;
      })
    ]);
  }

  _createMessageBoxButton(text, option) {
    if (!text) {
      return null;
    }
    let button = this.boxButtons.addButton({text: text});
    button.one('action', event => this._onButtonClick(event, option));
    this.buttons.push(button);
    this.abortButton = button;
    return button;
  }

  _render() {
    this.$container = this.$parent.appendDiv('messagebox')
      .on('mousedown', this._onMouseDown.bind(this))
      .on('copy', this._onCopy.bind(this));

    let $handle = this.$container.appendDiv('drag-handle');
    this.$container.draggable($handle);

    this.$content = this.$container.appendDiv('messagebox-content');
    this.$header = this.$content.appendDiv('messagebox-label messagebox-header');
    this.$body = this.$content.appendDiv('messagebox-label messagebox-body');
    this.$html = this.$content.appendDiv('messagebox-label messagebox-html prevent-initial-focus');

    this.boxButtons.render();
    this.$buttons = this.boxButtons.$container;
    this.$buttons.addClass('messagebox-buttons');
    this.$buttons.on('copy', this._onCopy.bind(this));

    this._installScrollbars({
      axis: 'y',
      scrollShadow: 'none'
    });

    // Render properties
    this._renderSeverity();
    this._renderHeader();
    this._renderIconId();
    this._renderBody();
    this._renderHtml();
    this._renderHiddenText();

    this.htmlComp = HtmlComponent.install(this.$container, this.session);
    this.htmlComp.setLayout(new MessageBoxLayout(this));
    this.htmlComp.validateLayout();

    this.$container.addClassForAnimation('animate-open');

    // Render modality glass-panes
    this._glassPaneRenderer = new GlassPaneRenderer(this);
    this._glassPaneRenderer.renderGlassPanes();
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
    let hasIcon = !!this._icon;
    this.$container.toggleClass('has-icon', hasIcon);
    this.$container.toggleClass('no-icon', !hasIcon);
    if (hasIcon) {
      this._icon.render(this.$header);
      this._icon.$container.addClass('messagebox-icon');
    }
  }

  _renderSeverity() {
    this.$container.removeClass(Status.SEVERITY_CSS_CLASSES);
    this.$container.addClass(Status.cssClassForSeverity(this.severity));
  }

  _renderHeader() {
    this.$header.html(strings.nl2br(this.header));
    this.$header.setVisible(this.header || this.iconId);
    this.$header.toggleClass('has-text', strings.hasText(this.header));
  }

  _renderBody() {
    this.$body.html(strings.nl2br(this.body));
    this.$body.setVisible(!!this.body);
    this.$content.toggleClass('has-body', !!this.body);
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
    let parent = this.findParent(p => {
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
    let myDocument = this.$container.document(true);
    let range = myDocument.createRange();
    range.selectNodeContents(this.$content[0]);
    let selection = this.$container.window(true).getSelection();
    selection.removeAllRanges();
    selection.addRange(range);
    myDocument.execCommand('copy');
  }

  _onCopy(event) {
    let clipboardData = objects.optProperty(event, 'originalEvent', 'clipboardData');

    if (clipboardData) {
      let htmlText = strings.join('<br/>',
        this.$header[0].outerHTML,
        this.$body[0].outerHTML,
        this.$html[0].outerHTML,
        this.hiddenText);
      clipboardData.setData('text/html', htmlText);

      let plainText = strings.join('\n\n',
        this.header,
        this.body,
        strings.plainText(this.$html[0].outerHTML, {compact: true, trim: true}),
        this.hiddenText);
      clipboardData.setData('text/plain', plainText);

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

  setIconId(iconId) {
    this.setProperty('iconId', iconId);
  }

  _setIconId(iconId) {
    this._setProperty('iconId', iconId);
    if (iconId) {
      if (this._icon) {
        this._icon.setIconDesc(iconId);
      } else {
        this._icon = scout.create('Icon', {
          parent: this,
          iconDesc: iconId,
          prepend: true
        });
        this._icon.one('destroy', () => {
          this._icon = null;
        });
      }
    } else if (this._icon) {
      this._icon.destroy();
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
    if (this.abortButton && this.abortButton.$container && this.session.focusManager.requestFocus(this.abortButton.$container)) {
      this.abortButton.doAction();
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
