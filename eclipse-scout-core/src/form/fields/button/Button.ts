/*
 * Copyright (c) 2010, 2024 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {
  aria, ButtonAdapterMenu, ButtonEventMap, ButtonKeyStroke, ButtonLayout, ButtonModel, ContextMenuPopup, Device, DoubleClickSupport, EnumObject, FormField, icons, InitModelOf, KeyStrokeContext, LoadingSupport, Menu, objects, scout, styles,
  tooltips, Widget
} from '../../../index';

export class Button extends FormField implements ButtonModel {
  declare model: ButtonModel;
  declare eventMap: ButtonEventMap;
  declare self: Button;

  defaultButton: boolean;
  displayStyle: ButtonDisplayStyle;
  iconId: string;
  keyStroke: string;
  keyStrokeScope: Widget;
  processButton: boolean;
  selected: boolean;
  systemType: ButtonSystemType;
  preventDoubleClick: boolean;
  stackable: boolean;
  shrinkable: boolean;
  adaptedBy: ButtonAdapterMenu;
  buttonKeyStroke: ButtonKeyStroke;
  formKeyStrokeContext: KeyStrokeContext;
  popup: ContextMenuPopup;
  $buttonLabel: JQuery;
  $submenuIcon: JQuery;
  protected _doubleClickSupport: DoubleClickSupport;

  constructor() {
    super();

    this.adaptedBy = null;
    this.defaultButton = false;
    this.displayStyle = Button.DisplayStyle.DEFAULT;
    this.gridDataHints.fillHorizontal = false;
    this.iconId = null;
    this.keyStroke = null;
    this.keyStrokeScope = null;
    this.processButton = true;
    this.selected = false;
    this.statusVisible = false;
    this.systemType = Button.SystemType.NONE;
    this.preventDoubleClick = false;
    this.stackable = true;
    this.shrinkable = false;
    this.$buttonLabel = null;
    this.buttonKeyStroke = new ButtonKeyStroke(this, null);
    this._doubleClickSupport = new DoubleClickSupport();
    this._addCloneProperties(['defaultButton', 'displayStyle', 'iconId', 'keyStroke', 'processButton', 'selected', 'systemType', 'preventDoubleClick', 'stackable', 'shrinkable']);
  }

  static SystemType = {
    NONE: 0,
    CANCEL: 1,
    CLOSE: 2,
    OK: 3,
    RESET: 4,
    SAVE: 5
  } as const;

  static DisplayStyle = {
    DEFAULT: 0,
    TOGGLE: 1,
    RADIO: 2,
    LINK: 3,
    BORDERLESS: 4
  } as const;

  static SUBMENU_ICON = icons.ANGLE_DOWN_BOLD;

  protected override _init(model: InitModelOf<this>) {
    super._init(model);
    this.resolveIconIds(['iconId']);
    this._setKeyStroke(this.keyStroke);
    this._setKeyStrokeScope(this.keyStrokeScope);
    this._setInheritAccessibility(this.inheritAccessibility && !this._isIgnoreAccessibilityFlags());
  }

  override getContextMenuItems(onlyVisible = true): Menu[] {
    return [];
  }

  protected override _initKeyStrokeContext() {
    super._initKeyStrokeContext();

    this._initDefaultKeyStrokes();

    this.formKeyStrokeContext = new KeyStrokeContext();
    this.formKeyStrokeContext.invokeAcceptInputOnActiveValueField = true;
    this.formKeyStrokeContext.registerKeyStroke(this.buttonKeyStroke);
    this.formKeyStrokeContext.$bindTarget = () => {
      if (this.keyStrokeScope) {
        return this.keyStrokeScope.$container;
      }
      // use form if available
      let form = this.getForm();
      if (form) {
        return form.$container;
      }
      // use desktop otherwise
      return this.session.desktop.$container;
    };
  }

  protected _isIgnoreAccessibilityFlags(): boolean {
    return this.systemType === Button.SystemType.CANCEL || this.systemType === Button.SystemType.CLOSE;
  }

  protected _initDefaultKeyStrokes() {
    this.keyStrokeContext.registerKeyStrokes([
      new ButtonKeyStroke(this, 'ENTER'),
      new ButtonKeyStroke(this, 'SPACE')
    ]);
  }

  protected override _createLoadingSupport(): LoadingSupport {
    return new LoadingSupport({
      widget: this,
      $container: () => {
        return this.$field;
      }
    });
  }

  protected override _render() {
    let $button: JQuery;
    if (this.displayStyle === Button.DisplayStyle.LINK) {
      // Render as link-button
      $button = this.$parent.makeDiv('link-button menu-item');
      aria.role($button, 'link');
      this.$buttonLabel = $button.appendSpan('button-label text');
    } else {
      // render as button
      $button = this.$parent.makeElement('<button>')
        .addClass('button');
      if (this.displayStyle === Button.DisplayStyle.BORDERLESS) {
        $button.addClass('borderless');
      }
      this.$buttonLabel = $button.appendSpan('button-label text');

      if (Device.get().supportsOnlyTouch()) {
        $button.setTabbable(false);
      }
    }
    this.addContainer(this.$parent, 'button-field', new ButtonLayout(this));
    this.addField($button);
    // TODO [10.0] cgu: should we add a label? -> would make it possible to control the space left of the button using labelVisible, like it is possible with checkboxes
    this.addStatus();

    $button
      .on('mousedown', event => this._doubleClickSupport.mousedown(event))
      .on('click', this._onClick.bind(this))
      .unfocusable();

    this.session.keyStrokeManager.installKeyStrokeContext(this.formKeyStrokeContext);

    tooltips.installForEllipsis(this.$buttonLabel, {
      parent: this
    });
  }

  protected override _remove() {
    super._remove();
    tooltips.uninstall(this.$buttonLabel);
    this.session.keyStrokeManager.uninstallKeyStrokeContext(this.formKeyStrokeContext);
    this.$submenuIcon = null;
  }

  protected override _renderProperties() {
    super._renderProperties();
    this._renderIconId();
    this._renderSelected();
    this._renderDefaultButton();
    this._updateLabelAndIconStyle();
  }

  protected override _renderForegroundColor() {
    super._renderForegroundColor();
    // Color button label as well, otherwise the color would not be visible because button label has already a color set using css
    styles.legacyForegroundColor(this, this.$buttonLabel);
    styles.legacyForegroundColor(this, this.get$Icon());
    styles.legacyForegroundColor(this, this.$submenuIcon);
  }

  protected override _renderBackgroundColor() {
    super._renderBackgroundColor();
    styles.legacyBackgroundColor(this, this.$fieldContainer);
  }

  protected override _renderFont() {
    super._renderFont();
    styles.legacyFont(this, this.$buttonLabel);
    // Changing the font may enlarge or shrink the field (e.g. set the style to bold makes the text bigger) -> invalidate layout
    this.invalidateLayoutTree();
  }

  override _updateMenus() {
    super._updateMenus();

    let hasMenus = this.menus.length > 0;
    aria.hasPopup(this.$field, hasMenus ? 'menu' : null);
    aria.expanded(this.$field, hasMenus ? !objects.isNullOrUndefined(this.popup) : null);

    this._renderSubmenuIcon();
  }

  protected _renderSubmenuIcon() {
    let hasMenus = this.menus?.length > 0;
    if (hasMenus && (this.label || !this.iconId)) { // no indicator when _only_ the icon is visible
      if (!this.$submenuIcon) {
        let icon = icons.parseIconId(Button.SUBMENU_ICON);
        this.$submenuIcon = this.$field
          .appendSpan('submenu-icon')
          .text(icon.iconCharacter);
        aria.hidden(this.$submenuIcon, true);
        this.invalidateLayoutTree();
      }
    } else if (this.$submenuIcon) {
      this.$submenuIcon.remove();
      this.$submenuIcon = null;
      this.invalidateLayoutTree();
    }
    if (!this.rendering) {
      this._updateLabelAndIconStyle();
    }
  }

  /**
   * @returns true if the action has been performed or false if it has not been performed (e.g. when the button is not enabled).
   */
  doAction(): boolean {
    if (!this.enabledComputed || !this.visible) {
      return false;
    }

    if (this.displayStyle === Button.DisplayStyle.TOGGLE) {
      this.setSelected(!this.selected);
    } else if (this.menus.length > 0) {
      this.togglePopup();
    }
    this._doAction();
    return true;
  }

  protected _doAction() {
    this.trigger('click');
  }

  togglePopup() {
    if (this.popup) {
      this.popup.close();
    } else {
      this.popup = this._openPopup();
      this.popup.one('destroy', event => {
        this.popup = null;
        aria.expanded(this.$field, false);
      });
      aria.expanded(this.$field, true);
    }
  }

  protected _openPopup(): ContextMenuPopup {
    let popup = scout.create(ContextMenuPopup, {
      parent: this,
      menuItems: this.menus,
      cloneMenuItems: false,
      closeOnAnchorMouseDown: false,
      $anchor: this.$field
    });
    popup.open();
    return popup;
  }

  protected _doActionTogglesSubMenu(): boolean {
    return false;
  }

  setDefaultButton(defaultButton: boolean) {
    this.setProperty('defaultButton', defaultButton);
  }

  protected _renderDefaultButton() {
    this.$field.toggleClass('default', this.defaultButton);
  }

  protected override _renderEnabled() {
    super._renderEnabled();
    if (this.displayStyle === Button.DisplayStyle.LINK) {
      this.$field.setTabbable(this.enabledComputed && !Device.get().supportsOnlyTouch());
    }
  }

  setSelected(selected: boolean) {
    this.setProperty('selected', selected);
  }

  protected _renderSelected() {
    if (this.displayStyle === Button.DisplayStyle.TOGGLE) {
      this.$field.toggleClass('selected', this.selected);
    }
    aria.pressed(this.$field, this.displayStyle === Button.DisplayStyle.TOGGLE ? this.selected : null);
  }

  protected override _renderLabel() {
    this.$buttonLabel.contentOrNbsp(this.labelHtmlEnabled, this.label, 'empty');
    if (!this.rendering) {
      this._renderSubmenuIcon();
    }

    // Invalidate layout because button may now be longer or shorter
    this.invalidateLayoutTree();
  }

  setIconId(iconId: string) {
    this.setProperty('iconId', iconId);
  }

  /**
   * Adds an image or font-based icon to the button by adding either an IMG or SPAN element to the button.
   */
  protected _renderIconId() {
    let $iconTarget = this.$fieldContainer;
    $iconTarget.icon(this.iconId);
    let $icon = $iconTarget.data('$icon') as JQuery;
    if ($icon) {
      // <img>s are loaded asynchronously. The real image size is not known until the image is loaded.
      // We add a listener to revalidate the button layout after this has happened. The 'loading' and
      // 'broken' classes ensure the incomplete icon is not taking any space.
      $icon.removeClass('loading broken');
      if ($icon.is('img')) {
        $icon.addClass('loading');
        $icon
          .off('load error')
          .on('load', updateButtonLayoutAfterImageLoaded.bind(this, true))
          .on('error', updateButtonLayoutAfterImageLoaded.bind(this, false));
      }
      if (!this.rendered) {
        styles.legacyForegroundColor(this, $icon);
      }
    }

    if (!this.rendering) {
      this._renderSubmenuIcon();
    }
    // Invalidate layout because button may now be longer or shorter
    this.invalidateLayoutTree();

    // ----- Helper functions -----

    function updateButtonLayoutAfterImageLoaded(success: boolean) {
      $icon.removeClass('loading');
      $icon.toggleClass('broken', !success);
      this.invalidateLayoutTree();
    }
  }

  get$Icon(): JQuery {
    let $iconTarget = this.$fieldContainer;
    return $iconTarget.children('.icon');
  }

  protected _updateLabelAndIconStyle() {
    let hasText = !!this.label;
    let hasIcon = !!this.iconId;
    let hasSubMenuIcon = !!this.$submenuIcon;
    let hasAnyIcon = hasIcon || hasSubMenuIcon;
    this.$buttonLabel.setVisible(hasText || !hasAnyIcon);
    this.$submenuIcon?.toggleClass('with-label', hasText);
    this.get$Icon().toggleClass('with-label', hasText);
  }

  setKeyStroke(keyStroke: string) {
    this.setProperty('keyStroke', keyStroke);
  }

  protected _setKeyStroke(keyStroke: string) {
    this._setProperty('keyStroke', keyStroke);
    this.buttonKeyStroke.parseAndSetKeyStroke(this.keyStroke);
  }

  protected _setKeyStrokeScope(keyStrokeScope: Widget | string) {
    if (typeof keyStrokeScope === 'string') {
      keyStrokeScope = this._resolveKeyStrokeScope(keyStrokeScope);
      if (!keyStrokeScope) {
        // Will be resolved later
        return;
      }
    }

    this._setProperty('keyStrokeScope', keyStrokeScope);
  }

  protected _resolveKeyStrokeScope(keyStrokeScope: string): Widget {
    // Basically, the desktop could be used to find the scope, but that would mean to traverse the whole widget tree.
    // To make it faster the form is used instead but that limits the resolving to the form.
    // This should be acceptable because the scope can still be set explicitly without using an id.
    let form = this.findNonWrappedForm();
    if (!form) {
      throw new Error('Could not resolve keyStrokeScope ' + keyStrokeScope + ' because no form has been found.');
    }
    if (!form.initialized) {
      // KeyStrokeScope is another widget (form or formfield) which may not be initialized yet.
      // The widget must be on the same form as the button, so once that form is initialized the keyStrokeScope has to be available
      form.one('init', this._setKeyStrokeScope.bind(this, keyStrokeScope));
      return;
    }
    let scope = form.widget(keyStrokeScope);
    if (!scope) {
      throw new Error('Could not resolve keyStrokeScope ' + keyStrokeScope + ' using form ' + form);
    }
    return scope;
  }

  protected _onClick(event: JQuery.ClickEvent) {
    if (event.which !== 1) {
      return; // Other button than left mouse button --> nop
    }
    if (this.preventDoubleClick && this._doubleClickSupport.doubleClicked()) {
      return; // More than one consecutive click --> nop
    }

    // When the action is clicked the user wants to execute the action and not see the tooltip -> cancel the task
    // If it is already displayed it will stay
    tooltips.cancel(this.$buttonLabel);
    tooltips.cancel(this.$fieldContainer);

    if (this.enabledComputed) {
      this.doAction();
    }
  }

  setStackable(stackable: boolean) {
    this.setProperty('stackable', stackable);
  }

  setShrinkable(shrinkable: boolean) {
    this.setProperty('shrinkable', shrinkable);
  }

  setPreventDoubleClick(preventDoubleClick: boolean) {
    this.setProperty('preventDoubleClick', preventDoubleClick);
  }

  protected override _linkWithLabel($element: JQuery) {
    super._linkWithLabel($element);
    aria.linkElementWithLabel($element, this.$buttonLabel);
  }

  override getFocusableElement(): HTMLElement | JQuery {
    if (this.adaptedBy) {
      return this.adaptedBy.getFocusableElement();
    }
    return super.getFocusableElement();
  }

  override isFocusable(checkTabbable?: boolean): boolean {
    if (this.adaptedBy) {
      return this.adaptedBy.isFocusable();
    }
    return super.isFocusable();
  }

  override focus(): boolean {
    if (this.adaptedBy) {
      return this.adaptedBy.focus();
    }
    return super.focus();
  }
}

export type ButtonSystemType = EnumObject<typeof Button.SystemType>;
export type ButtonDisplayStyle = EnumObject<typeof Button.DisplayStyle>;
