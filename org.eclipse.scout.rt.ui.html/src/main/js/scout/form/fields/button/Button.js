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
scout.Button = function() {
  scout.Button.parent.call(this);
  this.$buttonLabel;
  this._addAdapterProperties(['menus']);

  this.buttonKeyStroke = new scout.ButtonKeyStroke(this, null);
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

scout.Button.prototype._init = function(model) {
  scout.Button.parent.prototype._init.call(this, model);
  this._syncKeyStroke(this.keyStroke);
};

/**
 * @override ModelAdapter.js
 */
scout.Button.prototype._initKeyStrokeContext = function(keyStrokeContext) {
  scout.Button.parent.prototype._initKeyStrokeContext.call(this, keyStrokeContext);

  this._initDefaultKeyStrokes(keyStrokeContext);

  this.formKeyStrokeContext = new scout.KeyStrokeContext();
  this.formKeyStrokeContext.invokeAcceptInputOnActiveValueField = true;
  this.formKeyStrokeContext.registerKeyStroke(this.buttonKeyStroke);
  this.formKeyStrokeContext.$bindTarget = function() {
    if (this.keyStrokeScope) {
      var adapter = this.session.getModelAdapter(this.keyStrokeScope);
      if (adapter) {
        return adapter.$container;
      }
    }
    //failsave if scope is not set use form
    return this.getForm().$container;
  }.bind(this);
};

scout.Button.prototype._initDefaultKeyStrokes = function(keyStrokeContext) {
  keyStrokeContext.registerKeyStroke([
    new scout.ButtonKeyStroke(this, 'ENTER'),
    new scout.ButtonKeyStroke(this, 'SPACE')
  ]);
};

/**
 * The button form-field has no label and no status. Additionally it also has no container.
 * Container and field are the same thing.
 */
scout.Button.prototype._render = function($parent) {
  var $button;
  if (this.displayStyle === scout.Button.DisplayStyle.LINK) {
    // Render as link-button/ menu-item.
    // This is a bit weird: the model defines a button, but in the UI it behaves like a menu-item.
    // Probably it would be more reasonable to change the configuration (which would lead to additional
    // effort required to change an existing application).
    $button = $parent.makeDiv('link-button');
    $button.setTabbable(this.enabled);
    // Separate $link element to have a smaller focus border
    this.$link = $button.appendDiv('menu-item link');
    this.$buttonLabel = this.$link.appendSpan('button-label text');
  } else {
    // render as button
    $button = $parent.makeElement('<button>')
      .addClass('button');
    this.$buttonLabel = $button.appendSpan('button-label');
  }
  this.addContainer($parent, 'button-field', new scout.ButtonLayout(this));
  this.addField($button);
  // FIXME cgu: should we add a label? -> would make it possible to control the space left of the button using labelVisible, like it is possible with checkboxes
  this.addStatus();

  $button
    .on('click', this._onClick.bind(this))
    .on('mousedown', function(event) {
      // prevent focus validation on other field on mouse down. -> Safari workaround
      event.preventDefault();
    });

  if (this.menus && this.menus.length > 0) {
    this.menus.forEach(function(menu) {
      this.keyStrokeContext.registerKeyStroke(menu);
    }, this);
    if (this.label || !this.iconId) { // no indicator when _only_ the icon is visible
      this.$submenuIcon = (this.$link || $button).appendSpan('submenu-icon');
    }
  }
  $button.unfocusable();
  this.session.keyStrokeManager.installKeyStrokeContext(this.formKeyStrokeContext);
};

scout.Button.prototype._remove = function() {
  scout.Button.parent.prototype._remove.call(this);
  this.session.keyStrokeManager.uninstallKeyStrokeContext(this.formKeyStrokeContext);
  this.$submenuIcon = null;
};

scout.Button.prototype._renderForegroundColor = function() {
  scout.Button.parent.prototype._renderForegroundColor.call(this);
  // Color button label as well, otherwise the color would not be visible because button label has already a color set using css
  this.$buttonLabel.css('color', scout.styles.modelToCssColor(this.foregroundColor));
};

/**
 * @returns {Boolean}
 *          <code>true</code> if the action has been performed or <code>false</code> if it
 *          has not been performed (e.g. when the button is not enabled).
 */
scout.Button.prototype.doAction = function() {
  if (!this.enabled || !this.visible) {
    return false;
  }

  if (this.displayStyle === scout.Button.DisplayStyle.TOGGLE) {
    this.setSelected(!this.selected);
  } else if (this.menus.length > 0) {
    this.togglePopup();
  } else {
    this._send('clicked');
  }
  return true;
};

scout.Button.prototype.togglePopup = function() {
  if (this.popup) {
    this.popup.close();
  } else {
    this.popup = this._openPopup();
    this.popup.on('remove', function(event) {
      this.popup = null;
    }.bind(this));
  }
};

scout.Button.prototype._openPopup = function() {
  // FIXME bsh: Improve this
  var popup = scout.create('ContextMenuPopup', {
    parent: this,
    menuItems: this.menus,
    cloneMenuItems: false,
    closeOnAnchorMousedown: false,
    $anchor: this.$field
  });
  popup.open();
  return popup;
};
scout.Button.prototype._doActionTogglesSubMenu = function() {
  return false;
};

scout.Button.prototype.setSelected = function(selected) {
  this._setProperty('selected', selected);
  if (this.rendered) {
    this._renderSelected(this.selected);
  }
  this._send('selected', {
    selected: selected
  });
};

scout.Button.prototype.setIconId = function(iconId) {
  this._setProperty('iconId', iconId);
  if (this.rendered) {
    this._renderIconId();
  }
};

/**
 * @override
 */
scout.Button.prototype._renderProperties = function() {
  scout.Button.parent.prototype._renderProperties.call(this);
  this._renderIconId();
  this._renderSelected();
};

/**
 * @override
 */
scout.Button.prototype._renderEnabled = function() {
  scout.Button.parent.prototype._renderEnabled.call(this);
  if (this.displayStyle === scout.Button.DisplayStyle.LINK) {
    this.$link.setEnabled(this.enabled);
    this.$field.setTabbable(this.enabled);
  }
};

scout.Button.prototype._renderSelected = function() {
  if (this.displayStyle === scout.Button.DisplayStyle.TOGGLE) {
    this.$field.toggleClass('selected', this.selected);
  }
};

/**
 * @override
 */
scout.Button.prototype._renderLabel = function() {
  var label = scout.strings.removeAmpersand(this.label);
  if (this.iconId) {
    // If there is an icon, we don't need to ensure &nbsp; for empty strings
    this.$buttonLabel.text(label);
  } else {
    this.$buttonLabel.textOrNbsp(label);
  }
  // Invalidate layout because button may now be longer or shorter
  this.htmlComp.invalidateLayoutTree();
};

/**
 * Adds an image or font-based icon to the button by adding either an IMG or SPAN element to the button.
 */
scout.Button.prototype._renderIconId = function() {
  var $iconTarget = this.$link || this.$field;
  $iconTarget.icon(this.iconId);
  var $icon = $iconTarget.data('$icon');
  if ($icon) {
    $icon.toggleClass('with-label', !!this.label);
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
  }
  // Invalidate layout because button may now be longer or shorter
  this.htmlComp.invalidateLayoutTree();

  // ----- Helper functions -----

  function updateButtonLayoutAfterImageLoaded(success) {
    $icon.removeClass('loading');
    $icon.toggleClass('broken', !success);
    this.htmlComp.invalidateLayoutTree();
  }
};

scout.Button.prototype._syncKeyStroke = function(keyStroke) {
  this.keyStroke = keyStroke;
  this.buttonKeyStroke.parseAndSetKeyStroke(this.keyStroke);
};

scout.Button.prototype._onClick = function(event) {
  if (this.enabled) {
    this.doAction();
  }
};
