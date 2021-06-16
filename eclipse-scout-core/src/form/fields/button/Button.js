/*
 * Copyright (c) 2010-2021 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
import {ButtonKeyStroke, ButtonLayout, Device, DoubleClickSupport, FormField, icons, KeyStrokeContext, LoadingSupport, scout, styles, tooltips} from '../../../index';

export default class Button extends FormField {

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
    SAVE: 5,
    SAVE_WITHOUT_MARKER_CHANGE: 6
  };

  static DisplayStyle = {
    DEFAULT: 0,
    TOGGLE: 1,
    RADIO: 2,
    LINK: 3,
    BORDERLESS: 4
  };

  static SUBMENU_ICON = icons.ANGLE_DOWN_BOLD;

  _init(model) {
    super._init(model);
    this.resolveIconIds(['iconId']);
    this._setKeyStroke(this.keyStroke);
    this._setKeyStrokeScope(this.keyStrokeScope);
    this._setInheritAccessibility(this.inheritAccessibility && !this._isIgnoreAccessibilityFlags());
  }

  getContextMenuItems(onlyVisible = true) {
    return [];
  }

  /**
   * @override
   */
  _initKeyStrokeContext() {
    super._initKeyStrokeContext();

    this._initDefaultKeyStrokes();

    this.formKeyStrokeContext = new KeyStrokeContext();
    this.formKeyStrokeContext.invokeAcceptInputOnActiveValueField = true;
    this.formKeyStrokeContext.registerKeyStroke(this.buttonKeyStroke);
    this.formKeyStrokeContext.$bindTarget = function() {
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
    }.bind(this);
  }

  _isIgnoreAccessibilityFlags() {
    return this.systemType === Button.SystemType.CANCEL || this.systemType === Button.SystemType.CLOSE;
  }

  _initDefaultKeyStrokes() {
    this.keyStrokeContext.registerKeyStroke([
      new ButtonKeyStroke(this, 'ENTER'),
      new ButtonKeyStroke(this, 'SPACE')
    ]);
  }

  /**
   * @override
   */
  _createLoadingSupport() {
    return new LoadingSupport({
      widget: this,
      $container: function() {
        return this.$field;
      }.bind(this)
    });
  }

  /**
   * The button form-field has no label and no status. Additionally it also has no container.
   * Container and field are the same thing.
   */
  _render() {
    let $button;
    if (this.displayStyle === Button.DisplayStyle.LINK) {
      // Render as link-button
      $button = this.$parent.makeDiv('link-button menu-item');
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

    if (this.menus && this.menus.length > 0) {
      this.menus.forEach(function(menu) {
        this.keyStrokeContext.registerKeyStroke(menu);
      }, this);
      if (this.label || !this.iconId) { // no indicator when _only_ the icon is visible
        let icon = icons.parseIconId(Button.SUBMENU_ICON);
        this.$submenuIcon = $button
          .appendSpan('submenu-icon')
          .text(icon.iconCharacter);
      }
    }
    this.session.keyStrokeManager.installKeyStrokeContext(this.formKeyStrokeContext);

    tooltips.installForEllipsis(this.$buttonLabel, {
      parent: this
    });
  }

  _remove() {
    super._remove();
    tooltips.uninstall(this.$buttonLabel);
    this.session.keyStrokeManager.uninstallKeyStrokeContext(this.formKeyStrokeContext);
    this.$submenuIcon = null;
  }

  /**
   * @override
   */
  _renderProperties() {
    super._renderProperties();
    this._renderIconId();
    this._renderSelected();
    this._renderDefaultButton();
  }

  _renderForegroundColor() {
    super._renderForegroundColor();
    // Color button label as well, otherwise the color would not be visible because button label has already a color set using css
    styles.legacyForegroundColor(this, this.$buttonLabel);
    styles.legacyForegroundColor(this, this.get$Icon());
    styles.legacyForegroundColor(this, this.$submenuIcon);
  }

  _renderBackgroundColor() {
    super._renderBackgroundColor();
    styles.legacyBackgroundColor(this, this.$fieldContainer);
  }

  _renderFont() {
    super._renderFont();
    styles.legacyFont(this, this.$buttonLabel);
    // Changing the font may enlarge or shrink the field (e.g. set the style to bold makes the text bigger) -> invalidate layout
    this.invalidateLayoutTree();
  }

  /**
   * @returns {Boolean}
   *          <code>true</code> if the action has been performed or <code>false</code> if it
   *          has not been performed (e.g. when the button is not enabled).
   */
  doAction() {
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

  _doAction() {
    this.trigger('click');
  }

  togglePopup() {
    if (this.popup) {
      this.popup.close();
    } else {
      this.popup = this._openPopup();
      this.popup.one('destroy', event => {
        this.popup = null;
      });
    }
  }

  _openPopup() {
    let popup = scout.create('ContextMenuPopup', {
      parent: this,
      menuItems: this.menus,
      cloneMenuItems: false,
      closeOnAnchorMouseDown: false,
      $anchor: this.$field
    });
    popup.open();
    return popup;
  }

  _doActionTogglesSubMenu() {
    return false;
  }

  setDefaultButton(defaultButton) {
    this.setProperty('defaultButton', defaultButton);
  }

  _renderDefaultButton() {
    this.$field.toggleClass('default', this.defaultButton);
  }

  /**
   * @override
   */
  _renderEnabled() {
    super._renderEnabled();
    if (this.displayStyle === Button.DisplayStyle.LINK) {
      this.$field.setTabbable(this.enabledComputed && !Device.get().supportsOnlyTouch());
    }
  }

  setSelected(selected) {
    this.setProperty('selected', selected);
  }

  _renderSelected() {
    if (this.displayStyle === Button.DisplayStyle.TOGGLE) {
      this.$field.toggleClass('selected', this.selected);
    }
  }

  /**
   * @override
   */
  _renderLabel() {
    this.$buttonLabel.contentOrNbsp(this.labelHtmlEnabled, this.label, 'empty');
    this._updateLabelAndIconStyle();

    // Invalidate layout because button may now be longer or shorter
    this.invalidateLayoutTree();
  }

  setIconId(iconId) {
    this.setProperty('iconId', iconId);
  }

  /**
   * Adds an image or font-based icon to the button by adding either an IMG or SPAN element to the button.
   */
  _renderIconId() {
    let $iconTarget = this.$fieldContainer;
    $iconTarget.icon(this.iconId);
    let $icon = $iconTarget.data('$icon');
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

    this._updateLabelAndIconStyle();
    // Invalidate layout because button may now be longer or shorter
    this.invalidateLayoutTree();

    // ----- Helper functions -----

    function updateButtonLayoutAfterImageLoaded(success) {
      $icon.removeClass('loading');
      $icon.toggleClass('broken', !success);
      this.invalidateLayoutTree();
    }
  }

  get$Icon() {
    let $iconTarget = this.$fieldContainer;
    return $iconTarget.children('.icon');
  }

  _updateLabelAndIconStyle() {
    let hasText = !!this.label;
    this.$buttonLabel.setVisible(hasText || !this.iconId);
    this.get$Icon().toggleClass('with-label', hasText);
  }

  setKeyStroke(keyStroke) {
    this.setProperty('keyStroke', keyStroke);
  }

  _setKeyStroke(keyStroke) {
    this._setProperty('keyStroke', keyStroke);
    this.buttonKeyStroke.parseAndSetKeyStroke(this.keyStroke);
  }

  _setKeyStrokeScope(keyStrokeScope) {
    if (typeof keyStrokeScope === 'string') {
      keyStrokeScope = this._resolveKeyStrokeScope(keyStrokeScope);
      if (!keyStrokeScope) {
        // Will be resolved later
        return;
      }
    }

    this._setProperty('keyStrokeScope', keyStrokeScope);
  }

  _resolveKeyStrokeScope(keyStrokeScope) {
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
    keyStrokeScope = form.widget(keyStrokeScope);
    if (!keyStrokeScope) {
      throw new Error('Could not resolve keyStrokeScope ' + keyStrokeScope + ' using form ' + form);
    }
    return keyStrokeScope;
  }

  _onClick(event) {
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

  setStackable(stackable) {
    this.setProperty('stackable', stackable);
  }

  setShrinkable(shrinkable) {
    this.setProperty('shrinkable', shrinkable);
  }

  setPreventDoubleClick(preventDoubleClick) {
    this.setProperty('preventDoubleClick', preventDoubleClick);
  }

  /**
   * @override
   */
  getFocusableElement() {
    if (this.adaptedBy) {
      return this.adaptedBy.getFocusableElement();
    }
    return super.getFocusableElement();
  }

  /**
   * @override
   */
  isFocusable() {
    if (this.adaptedBy) {
      return this.adaptedBy.isFocusable();
    }
    return super.isFocusable();
  }

  /**
   * @override
   */
  focus() {
    if (this.adaptedBy) {
      return this.adaptedBy.focus();
    }
    return super.focus();
  }
}
