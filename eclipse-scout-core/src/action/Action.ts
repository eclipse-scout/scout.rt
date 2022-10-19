/*
 * Copyright (c) 2010-2022 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
import {ActionEventMap, ActionKeyStroke, ActionModel, Device, DoubleClickSupport, EnumObject, HtmlComponent, Icon, KeyStrokeContext, NullLayout, scout, tooltips, Widget} from '../index';
import $ from 'jquery';
import AbstractLayout from '../layout/AbstractLayout';
import {TooltipSupportOptions} from '../tooltip/TooltipSupport';
import {TooltipPosition} from '../tooltip/Tooltip';
import {Alignment} from '../cell/Cell';

export type ActionStyle = EnumObject<typeof Action.ActionStyle>;
export type KeyStrokeFirePolicy = EnumObject<typeof Action.KeyStrokeFirePolicy>;
export type ActionTextPosition = EnumObject<typeof Action.TextPosition>;

export default class Action extends Widget implements ActionModel {
  declare model: ActionModel;
  declare eventMap: ActionEventMap;

  actionStyle: ActionStyle;
  compact: boolean;
  compactOrig: boolean;
  iconId: string;
  horizontalAlignment: Alignment;
  keyStroke: string;
  keyStrokeFirePolicy: KeyStrokeFirePolicy;
  selected: boolean;
  preventDoubleClick: boolean;
  tabbable: boolean;
  actionKeyStroke: ActionKeyStroke;
  text: string;
  textPosition: ActionTextPosition;
  htmlEnabled: boolean;
  textVisible: boolean;
  textVisibleOrig: boolean;
  toggleAction: boolean;
  tooltipText: string;
  showTooltipWhenSelected: boolean;
  tooltipPosition: TooltipPosition;
  icon: Icon;
  $text: JQuery;

  protected _doubleClickSupport: DoubleClickSupport;

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
  } as const;

  static TextPosition = {
    DEFAULT: 'default',
    /**
     * The text will be positioned below the icon. It has no effect if no icon is set.
     */
    BOTTOM: 'bottom'
  } as const;

  static KeyStrokeFirePolicy = {
    ACCESSIBLE_ONLY: 0,
    ALWAYS: 1
  } as const;

  protected override _createKeyStrokeContext(): KeyStrokeContext {
    return new KeyStrokeContext();
  }

  protected override _init(model: ActionModel) {
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

  protected override _render() {
    this.$container = this.$parent.appendDiv('action')
      .on('mousedown', event => this._doubleClickSupport.mousedown(event))
      .on('click', this._onClick.bind(this));
    this.htmlComp = HtmlComponent.install(this.$container, this.session);
    this.htmlComp.setLayout(this._createLayout());
  }

  protected _createLayout(): AbstractLayout {
    return new NullLayout();
  }

  protected override _renderProperties() {
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

  protected override _remove() {
    this._removeText();
    this._removeIconId();
    super._remove();
  }

  setText(text: string) {
    this.setProperty('text', text);
  }

  protected _renderText() {
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

  protected _removeText() {
    if (this.$text) {
      this.$text.remove();
      this.$text = null;
    }
  }

  setTextPosition(textPosition: ActionTextPosition) {
    this.setProperty('textPosition', textPosition);
  }

  protected _renderTextPosition() {
    this.$container.toggleClass('bottom-text', this.textPosition === Action.TextPosition.BOTTOM);
    this.invalidateLayoutTree();
  }

  setHtmlEnabled(htmlEnabled: boolean) {
    this.setProperty('htmlEnabled', htmlEnabled);
  }

  protected _renderHtmlEnabled() {
    // Render the text again when html enabled changes dynamically
    this._renderText();
  }

  setIconId(iconId: string) {
    this.setProperty('iconId', iconId);
  }

  protected _renderIconId() {
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
    this.icon = scout.create(Icon, {
      parent: this,
      iconDesc: iconId,
      prepend: true
    });
    this.icon.one('destroy', () => {
      this.icon = null;
    });
    this.icon.render();
  }

  get$Icon(): JQuery {
    if (this.icon) {
      return this.icon.$container;
    }
    return $();
  }

  protected _removeIconId() {
    if (this.icon) {
      this.icon.destroy();
    }
  }

  protected override _renderEnabled() {
    super._renderEnabled();
    if (this.rendered) { // No need to do this during initial rendering
      this._updateTooltip();
      this._renderTabbable();
    }
  }

  setTooltipText(tooltipText: string) {
    this.setProperty('tooltipText', tooltipText);
  }

  protected _renderTooltipText() {
    this._updateTooltip();
  }

  /**
   * Installs or uninstalls tooltip based on tooltipText, selected and enabledComputed.
   */
  protected _updateTooltip() {
    if (this._shouldInstallTooltip()) {
      tooltips.install(this.$container, this._configureTooltip());
    } else {
      tooltips.uninstall(this.$container);
    }
  }

  protected _shouldInstallTooltip(): boolean {
    let show = this.tooltipText && this.enabledComputed;
    if (!this.showTooltipWhenSelected && this.selected) {
      show = false;
    }
    return show;
  }

  protected _renderTabbable() {
    this.$container.setTabbable(this.tabbable && this.enabledComputed && !Device.get().supportsOnlyTouch());
  }

  protected _renderCompact() {
    this.$container.toggleClass('compact', this.compact);
    this.invalidateLayoutTree();
  }

  setTooltipPosition(position: TooltipPosition) {
    this.setProperty('tooltipPosition', position);
  }

  protected _configureTooltip(): TooltipSupportOptions {
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
   * @returns true if the action has been performed or false if it has not been performed (e.g. when the button is not enabledComputed).
   */
  doAction(): boolean {
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

  setToggleAction(toggleAction: boolean) {
    this.setProperty('toggleAction', toggleAction);
  }

  isToggleAction(): boolean {
    return this.toggleAction;
  }

  /**
   * @returns true if the action may be executed, false if it should be ignored.
   */
  prepareDoAction(): boolean {
    if (!this.enabledComputed || !this.visible) {
      return false;
    }
    return true;
  }

  protected _doAction() {
    this.trigger('action');
  }

  setSelected(selected: boolean) {
    this.setProperty('selected', selected);
  }

  protected _renderSelected() {
    this.$container.toggleClass('selected', this.selected);
    if (this.rendered) { // prevent unnecessary tooltip updates during initial rendering
      this._updateTooltip();
    }
  }

  setKeyStroke(keyStroke: string) {
    this.setProperty('keyStroke', keyStroke);
  }

  protected _setKeyStroke(keyStroke: string) {
    this.actionKeyStroke.parseAndSetKeyStroke(keyStroke);
    this._setProperty('keyStroke', keyStroke);
  }

  protected _renderKeyStroke() {
    let keyStroke = this.keyStroke;
    if (keyStroke === undefined) {
      this.$container.removeAttr('data-shortcut');
    } else {
      this.$container.attr('data-shortcut', keyStroke);
    }
  }

  setTabbable(tabbable: boolean) {
    this.setProperty('tabbable', tabbable);
  }

  setTextVisible(textVisible: boolean) {
    if (this.textVisible === textVisible) {
      return;
    }
    this._setProperty('textVisible', textVisible);
    if (this.rendered) {
      this._renderText();
    }
  }

  setCompact(compact: boolean) {
    if (this.compact === compact) {
      return;
    }
    this.compact = compact;
    if (this.rendered) {
      this._renderCompact();
    }
  }

  setHorizontalAlignment(horizontalAlignment: Alignment) {
    this.setProperty('horizontalAlignment', horizontalAlignment);
  }

  protected _createActionKeyStroke(): ActionKeyStroke {
    return new ActionKeyStroke(this);
  }

  setPreventDoubleClick(preventDoubleClick: boolean) {
    this.setProperty('preventDoubleClick', preventDoubleClick);
  }

  protected _allowMouseEvent(event: JQuery.MouseEventBase<HTMLElement, undefined, HTMLElement, HTMLElement>): boolean {
    if (event.which !== 1) {
      return false; // Other button than left mouse button --> nop
    }
    if (event.type === 'click' && this.preventDoubleClick && this._doubleClickSupport.doubleClicked()) {
      return false; // More than one consecutive click --> nop
    }
    return true;
  }

  protected _onClick(event: JQuery.ClickEvent<HTMLElement, undefined, HTMLElement, HTMLElement>) {
    if (!this._allowMouseEvent(event)) {
      return;
    }

    // When the action is clicked the user wants to execute the action and not see the tooltip -> cancel the task
    // If it is already displayed it will stay
    tooltips.cancel(this.$container);

    this.doAction();
  }

  setActionStyle(actionStyle: ActionStyle) {
    this.setProperty('actionStyle', actionStyle);
  }
}
