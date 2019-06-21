/*******************************************************************************
 * Copyright (c) 2014-2018 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
scout.Button = function() {
  scout.Button.parent.call(this);

  this.defaultButton = false;
  this.displayStyle = scout.Button.DisplayStyle.DEFAULT;
  this.gridDataHints.fillHorizontal = false;
  this.htmlEnabled = false;
  this.iconId = null;
  this.keyStroke = null;
  this.processButton = true;
  this.selected = false;
  this.statusVisible = false;
  this.systemType = scout.Button.SystemType.NONE;
  this.preventDoubleClick = false;
  this.stackable = true;
  this.shrinkable = false;

  this.$buttonLabel = null;
  this.buttonKeyStroke = new scout.ButtonKeyStroke(this, null);
  this._addCloneProperties(['defaultButton', 'displayStyle', 'iconId', 'keyStroke', 'processButton', 'selected', 'systemType', 'preventDoubleClick', 'stackable', 'shrinkable']);
};
scout.inherits(scout.Button, scout.FormField);

scout.Button.SystemType = {
  NONE: 0,
  CANCEL: 1,
  CLOSE: 2,
  OK: 3,
  RESET: 4,
  SAVE: 5,
  SAVE_WITHOUT_MARKER_CHANGE: 6
};

scout.Button.DisplayStyle = {
  DEFAULT: 0,
  TOGGLE: 1,
  RADIO: 2,
  LINK: 3
};

scout.Button.SUBMENU_ICON = scout.icons.ANGLE_DOWN_BOLD;

scout.Button.prototype._init = function(model) {
  scout.Button.parent.prototype._init.call(this, model);
  this.resolveIconIds(['iconId']);
  this._setKeyStroke(this.keyStroke);
  this._setKeyStrokeScope(this.keyStrokeScope);
  this._setInheritAccessibility(this.inheritAccessibility && !this._isIgnoreAccessibilityFlags());
};

/**
 * @override
 */
scout.Button.prototype._initKeyStrokeContext = function() {
  scout.Button.parent.prototype._initKeyStrokeContext.call(this);

  this._initDefaultKeyStrokes();

  this.formKeyStrokeContext = new scout.KeyStrokeContext();
  this.formKeyStrokeContext.invokeAcceptInputOnActiveValueField = true;
  this.formKeyStrokeContext.registerKeyStroke(this.buttonKeyStroke);
  this.formKeyStrokeContext.$bindTarget = function() {
    if (this.keyStrokeScope) {
      return this.keyStrokeScope.$container;
    }
    // use form if available
    var form = this.getForm();
    if (form) {
      return form.$container;
    }
    // use desktop otherwise
    return this.session.desktop.$container;
  }.bind(this);
};

scout.Button.prototype._isIgnoreAccessibilityFlags = function() {
  return this.systemType === scout.Button.SystemType.CANCEL || this.systemType === scout.Button.SystemType.CLOSE;
};

scout.Button.prototype._initDefaultKeyStrokes = function() {
  this.keyStrokeContext.registerKeyStroke([
    new scout.ButtonKeyStroke(this, 'ENTER'),
    new scout.ButtonKeyStroke(this, 'SPACE')
  ]);
};

/**
 * @override
 */
scout.Button.prototype._createLoadingSupport = function() {
  return new scout.LoadingSupport({
    widget: this,
    $container: function() {
      return this.$field;
    }.bind(this)
  });
};

/**
 * The button form-field has no label and no status. Additionally it also has no container.
 * Container and field are the same thing.
 */
scout.Button.prototype._render = function() {
  var $button;
  if (this.displayStyle === scout.Button.DisplayStyle.LINK) {
    // Render as link-button/ menu-item.
    // This is a bit weird: the model defines a button, but in the UI it behaves like a menu-item.
    // Probably it would be more reasonable to change the configuration (which would lead to additional
    // effort required to change an existing application).
    $button = this.$parent.makeDiv('link-button');
    // Separate $link element to have a smaller focus border
    this.$link = $button.appendDiv('menu-item link');
    this.$buttonLabel = this.$link.appendSpan('button-label text');
  } else {
    // render as button
    $button = this.$parent.makeElement('<button>')
      .addClass('button');
    this.$buttonLabel = $button.appendSpan('button-label');

    if (scout.device.supportsTouch()) {
      $button.setTabbable(false);
    }
  }
  this.addContainer(this.$parent, 'button-field', new scout.ButtonLayout(this));
  this.addField($button);
  // TODO [10.0] cgu: should we add a label? -> would make it possible to control the space left of the button using labelVisible, like it is possible with checkboxes
  this.addStatus();

  $button.on('click', this._onClick.bind(this))
    .unfocusable();

  if (this.menus && this.menus.length > 0) {
    this.menus.forEach(function(menu) {
      this.keyStrokeContext.registerKeyStroke(menu);
    }, this);
    if (this.label || !this.iconId) { // no indicator when _only_ the icon is visible
      var icon = scout.icons.parseIconId(scout.Button.SUBMENU_ICON);
      this.$submenuIcon = (this.$link || $button)
        .appendSpan('submenu-icon')
        .text(icon.iconCharacter);
    }
  }
  this.session.keyStrokeManager.installKeyStrokeContext(this.formKeyStrokeContext);

  scout.tooltips.installForEllipsis(this.$buttonLabel, {
    parent: this
  });
};

scout.Button.prototype._remove = function() {
  scout.Button.parent.prototype._remove.call(this);
  scout.tooltips.uninstall(this.$buttonLabel);
  this.session.keyStrokeManager.uninstallKeyStrokeContext(this.formKeyStrokeContext);
  this.$submenuIcon = null;
};

/**
 * @override
 */
scout.Button.prototype._renderProperties = function() {
  scout.Button.parent.prototype._renderProperties.call(this);
  this._renderIconId();
  this._renderSelected();
  this._renderDefaultButton();
};

scout.Button.prototype._renderForegroundColor = function() {
  scout.Button.parent.prototype._renderForegroundColor.call(this);
  // Color button label as well, otherwise the color would not be visible because button label has already a color set using css
  scout.styles.legacyForegroundColor(this, this.$buttonLabel);
  scout.styles.legacyForegroundColor(this, this.get$Icon());
  scout.styles.legacyForegroundColor(this, this.$submenuIcon);
};

scout.Button.prototype._renderBackgroundColor = function() {
  scout.Button.parent.prototype._renderBackgroundColor.call(this);
  scout.styles.legacyBackgroundColor(this, this.$fieldContainer);
};

scout.Button.prototype._renderFont = function() {
  scout.Button.parent.prototype._renderFont.call(this);
  scout.styles.legacyFont(this, this.$buttonLabel);
  // Changing the font may enlarge or shrink the field (e.g. set the style to bold makes the text bigger) -> invalidate layout
  this.invalidateLayoutTree();
};

/**
 * @returns {Boolean}
 *          <code>true</code> if the action has been performed or <code>false</code> if it
 *          has not been performed (e.g. when the button is not enabled).
 */
scout.Button.prototype.doAction = function() {
  if (!this.enabledComputed || !this.visible) {
    return false;
  }

  if (this.displayStyle === scout.Button.DisplayStyle.TOGGLE) {
    this.setSelected(!this.selected);
  } else if (this.menus.length > 0) {
    this.togglePopup();
  }
  this._doAction();
  return true;
};

scout.Button.prototype._doAction = function() {
  this.trigger('click');
};

scout.Button.prototype.togglePopup = function() {
  if (this.popup) {
    this.popup.close();
  } else {
    this.popup = this._openPopup();
    this.popup.one('destroy', function(event) {
      this.popup = null;
    }.bind(this));
  }
};

scout.Button.prototype._openPopup = function() {
  var popup = scout.create('ContextMenuPopup', {
    parent: this,
    menuItems: this.menus,
    cloneMenuItems: false,
    closeOnAnchorMouseDown: false,
    $anchor: this.$field
  });
  popup.open();
  return popup;
};

scout.Button.prototype._doActionTogglesSubMenu = function() {
  return false;
};

scout.Button.prototype.setDefaultButton = function(defaultButton) {
  this.setProperty('defaultButton', defaultButton);
};

scout.Button.prototype._renderDefaultButton = function() {
  this.$field.toggleClass('default', this.defaultButton);
};

/**
 * @override
 */
scout.Button.prototype._renderEnabled = function() {
  scout.Button.parent.prototype._renderEnabled.call(this);
  if (this.displayStyle === scout.Button.DisplayStyle.LINK) {
    this.$link.setEnabled(this.enabledComputed);
    this.$field.setTabbable(this.enabledComputed && !scout.device.supportsTouch());
  }
};

scout.Button.prototype.setSelected = function(selected) {
  this.setProperty('selected', selected);
};

scout.Button.prototype._renderSelected = function() {
  if (this.displayStyle === scout.Button.DisplayStyle.TOGGLE) {
    this.$field.toggleClass('selected', this.selected);
  }
};

scout.Button.prototype.setHtmlEnabled = function(htmlEnabled) {
  this.setProperty('htmlEnabled', htmlEnabled);
};

scout.Button.prototype._renderHtmlEnabled = function() {
  // Render the label again when html enabled changes dynamically
  this._renderLabel();
};

/**
 * @override
 */
scout.Button.prototype._renderLabel = function() {
  if (this.htmlEnabled) {
    this.$buttonLabel.html(this.label || '');
  } else {
    this.$buttonLabel.textOrNbsp(this.label, 'empty');
  }
  this._updateLabelAndIconStyle();

  // Invalidate layout because button may now be longer or shorter
  this.invalidateLayoutTree();
};

scout.Button.prototype.setIconId = function(iconId) {
  this.setProperty('iconId', iconId);
};

/**
 * Adds an image or font-based icon to the button by adding either an IMG or SPAN element to the button.
 */
scout.Button.prototype._renderIconId = function() {
  var $iconTarget = this.$link || this.$fieldContainer;
  $iconTarget.icon(this.iconId);
  var $icon = $iconTarget.data('$icon');
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
      scout.styles.legacyForegroundColor(this, $icon);
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
};

scout.Button.prototype.get$Icon = function() {
  var $iconTarget = this.$link || this.$fieldContainer;
  return $iconTarget.children('.icon');
};

scout.Button.prototype._updateLabelAndIconStyle = function() {
  var hasText = !!this.label;
  this.$buttonLabel.setVisible(hasText || !this.iconId);
  this.get$Icon().toggleClass('with-label', hasText);
};

scout.Button.prototype.setKeyStroke = function(keyStroke) {
  this.setProperty('keyStroke', keyStroke);
};

scout.Button.prototype._setKeyStroke = function(keyStroke) {
  this._setProperty('keyStroke', keyStroke);
  this.buttonKeyStroke.parseAndSetKeyStroke(this.keyStroke);
};

scout.Button.prototype._setKeyStrokeScope = function(keyStrokeScope) {
  if (typeof keyStrokeScope === 'string') {
    keyStrokeScope = this._resolveKeyStrokeScope(keyStrokeScope);
    if (!keyStrokeScope) {
      // Will be resolved later
      return;
    }
  }

  this._setProperty('keyStrokeScope', keyStrokeScope);
};

scout.Button.prototype._resolveKeyStrokeScope = function(keyStrokeScope) {
  // Basically, the desktop could be used to find the scope, but that would mean to traverse the whole widget tree.
  // To make it faster the form is used instead but that limits the resolving to the form.
  // This should be acceptable because the scope can still be set explicitly without using an id.
  var form = this.findNonWrappedForm();
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
};

scout.Button.prototype._onClick = function(event) {
  if (event.which !== 1) {
    return; // Other button than left mouse button --> nop
  }
  if (event.detail > 1 && this.preventDoubleClick) {
    return; // More than one consecutive click --> nop
  }

  if (this.enabledComputed) {
    this.doAction();
  }
};

scout.Button.prototype.setStackable = function(stackable) {
  this.setProperty('stackable', stackable);
};

scout.Button.prototype.setShrinkable = function(shrinkable) {
  this.setProperty('shrinkable', shrinkable);
};

/**
 * @override
 */
scout.Button.prototype.getFocusableElement = function() {
  if (this.adaptedBy) {
    return this.adaptedBy.getFocusableElement();
  } else {
    return scout.Button.parent.prototype.getFocusableElement.call(this);
  }
};

/**
 * @override
 */
scout.Button.prototype.isFocusable = function() {
  if (this.adaptedBy) {
    return this.adaptedBy.isFocusable();
  } else {
    return scout.Button.parent.prototype.isFocusable.call(this);
  }
};

/**
 * @override
 */
scout.Button.prototype.focus = function() {
  if (this.adaptedBy) {
    return this.adaptedBy.focus();
  } else {
    return scout.Button.parent.prototype.focus.call(this);
  }
};
