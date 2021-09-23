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
import {ActionKeyStroke, Device, DoubleClickSupport, HtmlComponent, Icon, KeyStrokeContext, NullLayout, scout, tooltips, Widget} from '../index';
import $ from 'jquery';

export default class Action extends Widget {

  // noinspection DuplicatedCode
  constructor() {
    super();

    this.actionStyle = Action.ActionStyle.DEFAULT;
    this.compact = false;
    this.iconId = null;
    this.horizontalAlignment = -1;
    this.keyStroke = null;
    this.keyStrokeFirePolicy = Action.KeyStrokeFirePolicy.ACCESSIBLE_ONLY;
    this.selected = false;
    this.preventDoubleClick = false;
    /**
     * This property decides whether or not the tabindex attribute is set in the DOM.
     */
    this.tabbable = false;
    this.text = null;
    this.textPosition = Action.TextPosition.DEFAULT;
    this.htmlEnabled = false;
    this.textVisible = true;
    this.toggleAction = false;
    this.tooltipText = null;
    this.showTooltipWhenSelected = true;

    this._doubleClickSupport = new DoubleClickSupport();
    this._addCloneProperties(['actionStyle', 'horizontalAlignment', 'iconId', 'selected', 'preventDoubleClick', 'tabbable', 'text', 'textPosition', 'htmlEnabled', 'tooltipText', 'toggleAction']);
  }

  static ActionStyle = {
    /**
     * regular menu-look, also used in overflow menus
     */
    DEFAULT: 0,
    /**
     * menu looks like a button
     */
    BUTTON: 1
  };

  static TextPosition = {
    DEFAULT: 'default',
    /**
     * The text will be positioned below the icon. It has no effect if no icon is set.
     */
    BOTTOM: 'bottom'
  };

  static KeyStrokeFirePolicy = {
    ACCESSIBLE_ONLY: 0,
    ALWAYS: 1
  };

  /**
   * @override
   */
  _createKeyStrokeContext() {
    return new KeyStrokeContext();
  }

  _init(model) {
    super._init(model);
    this.actionKeyStroke = this._createActionKeyStroke();
    this.resolveConsts([{
      property: 'actionStyle',
      constType: Action.ActionStyle
    }, {
      property: 'textPosition',
      constType: Action.TextPosition
    }, {
      property: 'keyStrokeFirePolicy',
      constType: Action.KeyStrokeFirePolicy
    }]);
    this.resolveTextKeys(['text', 'tooltipText']);
    this.resolveIconIds(['iconId']);
    this._setKeyStroke(this.keyStroke);
  }

  _render() {
    this.$container = this.$parent.appendDiv('action')
      .on('mousedown', event => this._doubleClickSupport.mousedown(event))
      .on('click', this._onClick.bind(this));
    this.htmlComp = HtmlComponent.install(this.$container, this.session);
    this.htmlComp.setLayout(this._createLayout());
  }

  _createLayout() {
    return new NullLayout();
  }

  _renderProperties() {
    super._renderProperties();

    this._renderText();
    this._renderTextPosition();
    this._renderIconId();
    this._renderTooltipText();
    this._renderKeyStroke();
    this._renderSelected();
    this._renderTabbable();
    this._renderCompact();
  }

  _remove() {
    this._removeText();
    this._removeIconId();
    super._remove();
  }

  setText(text) {
    this.setProperty('text', text);
  }

  _renderText() {
    let text = this.text || '';
    if (text && this.textVisible) {
      if (!this.$text) {
        // Create a separate text element to so that setting the text does not remove the icon
        this.$text = this.$container.appendSpan('content text');
        HtmlComponent.install(this.$text, this.session);
      }
      if (this.htmlEnabled) {
        this.$text.html(text);
      } else {
        this.$text.text(text);
      }
    } else {
      this._removeText();
    }
  }

  _removeText() {
    if (this.$text) {
      this.$text.remove();
      this.$text = null;
    }
  }

  setTextPosition(textPosition) {
    this.setProperty('textPosition', textPosition);
  }

  _renderTextPosition() {
    this.$container.toggleClass('bottom-text', this.textPosition === Action.TextPosition.BOTTOM);
    this.invalidateLayoutTree();
  }

  setHtmlEnabled(htmlEnabled) {
    this.setProperty('htmlEnabled', htmlEnabled);
  }

  _renderHtmlEnabled() {
    // Render the text again when html enabled changes dynamically
    this._renderText();
  }

  setIconId(iconId) {
    this.setProperty('iconId', iconId);
  }

  _renderIconId() {
    let iconId = this.iconId || '';
    // If the icon is an image (and not a font icon), the Icon class will invalidate the layout when the image has loaded
    if (!iconId) {
      this._removeIconId();
      return;
    }
    if (this.icon) {
      this.icon.setIconDesc(iconId);
      return;
    }
    this.icon = scout.create('Icon', {
      parent: this,
      iconDesc: iconId,
      prepend: true
    });
    this.icon.one('destroy', () => {
      this.icon = null;
    });
    this.icon.render();
  }

  get$Icon() {
    if (this.icon) {
      return this.icon.$container;
    }
    return $();
  }

  _removeIconId() {
    if (this.icon) {
      this.icon.destroy();
    }
  }

  /**
   * @override
   */
  _renderEnabled() {
    super._renderEnabled();
    if (this.rendered) { // No need to do this during initial rendering
      this._updateTooltip();
      this._renderTabbable();
    }
  }

  setTooltipText(tooltipText) {
    this.setProperty('tooltipText', tooltipText);
  }

  _renderTooltipText() {
    this._updateTooltip();
  }

  /**
   * Installs or uninstalls tooltip based on tooltipText, selected and enabledComputed.
   */
  _updateTooltip() {
    if (this._shouldInstallTooltip()) {
      tooltips.install(this.$container, this._configureTooltip());
    } else {
      tooltips.uninstall(this.$container);
    }
  }

  _shouldInstallTooltip() {
    let show = this.tooltipText && this.enabledComputed;
    if (!this.showTooltipWhenSelected && this.selected) {
      show = false;
    }
    return show;
  }

  _renderTabbable() {
    this.$container.setTabbable(this.tabbable && this.enabledComputed && !Device.get().supportsOnlyTouch());
  }

  _renderCompact() {
    this.$container.toggleClass('compact', this.compact);
    this.invalidateLayoutTree();
  }

  setTooltipPosition(position) {
    this.setProperty('tooltipPosition', position);
  }

  _configureTooltip() {
    return {
      parent: this,
      text: this.tooltipText,
      $anchor: this.$container,
      arrowPosition: 50,
      arrowPositionUnit: '%',
      tooltipPosition: this.tooltipPosition
    };
  }

  /**
   * @return {Boolean}
   *          <code>true</code> if the action has been performed or <code>false</code> if it
   *          has not been performed (e.g. when the button is not enabledComputed).
   */
  doAction() {
    if (!this.prepareDoAction()) {
      return false;
    }

    if (this.isToggleAction()) {
      this.setSelected(!this.selected);
    }
    this._doAction();
    return true;
  }

  toggle() {
    if (this.isToggleAction()) {
      this.setSelected(!this.selected);
    }
  }

  setToggleAction(toggleAction) {
    this.setProperty('toggleAction', toggleAction);
  }

  isToggleAction() {
    return this.toggleAction;
  }

  /**
   * @returns {Boolean} <code>true</code> if the action may be executed, <code>false</code> if it should be ignored.
   */
  prepareDoAction() {
    if (!this.enabledComputed || !this.visible) {
      return false;
    }

    return true;
  }

  _doAction() {
    this.trigger('action');
  }

  setSelected(selected) {
    this.setProperty('selected', selected);
  }

  _renderSelected() {
    this.$container.toggleClass('selected', this.selected);
    if (this.rendered) { // prevent unnecessary tooltip updates during initial rendering
      this._updateTooltip();
    }
  }

  setKeyStroke(keyStroke) {
    this.setProperty('keyStroke', keyStroke);
  }

  _setKeyStroke(keyStroke) {
    this.actionKeyStroke.parseAndSetKeyStroke(keyStroke);
    this._setProperty('keyStroke', keyStroke);
  }

  _renderKeyStroke() {
    let keyStroke = this.keyStroke;
    if (keyStroke === undefined) {
      this.$container.removeAttr('data-shortcut');
    } else {
      this.$container.attr('data-shortcut', keyStroke);
    }
  }

  setTabbable(tabbable) {
    this.setProperty('tabbable', tabbable);
  }

  setTextVisible(textVisible) {
    if (this.textVisible === textVisible) {
      return;
    }
    this._setProperty('textVisible', textVisible);
    if (this.rendered) {
      this._renderText();
    }
  }

  setCompact(compact) {
    if (this.compact === compact) {
      return;
    }
    this.compact = compact;
    if (this.rendered) {
      this._renderCompact();
    }
  }

  setHorizontalAlignment(horizontalAlignment) {
    this.setProperty('horizontalAlignment', horizontalAlignment);
  }

  /**
   * @return {ActionKeyStroke}
   */
  _createActionKeyStroke() {
    return new ActionKeyStroke(this);
  }

  setPreventDoubleClick(preventDoubleClick) {
    this.setProperty('preventDoubleClick', preventDoubleClick);
  }

  _allowMouseEvent(event) {
    if (event.which !== 1) {
      return false; // Other button than left mouse button --> nop
    }
    if (event.type === 'click' && this.preventDoubleClick && this._doubleClickSupport.doubleClicked()) {
      return false; // More than one consecutive click --> nop
    }
    return true;
  }

  _onClick(event) {
    if (!this._allowMouseEvent(event)) {
      return;
    }

    // When the action is clicked the user wants to execute the action and not see the tooltip -> cancel the task
    // If it is already displayed it will stay
    tooltips.cancel(this.$container);

    this.doAction();
  }

  setActionStyle(actionStyle) {
    this.setProperty('actionStyle', actionStyle);
  }
}
