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
  this._$label;
  this._addAdapterProperties('menus');

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
 * @override ModelAdapter
 */
scout.Button.prototype._initKeyStrokeContext = function(keyStrokeContext) {
  scout.Button.parent.prototype._initKeyStrokeContext.call(this, keyStrokeContext);

  keyStrokeContext.registerKeyStroke([
    new scout.ButtonKeyStroke(this, 'ENTER'),
    new scout.ButtonKeyStroke(this, 'SPACE')
  ]);

  this.formKeyStrokeContext = new scout.KeyStrokeContext();
  this.formKeyStrokeContext.invokeAcceptInputOnActiveValueField = true;
  this.formKeyStrokeContext.registerKeyStroke(this.buttonKeyStroke);
  this.formKeyStrokeContext.$bindTarget = function() {
    // keystrokes have form scope
    return this.getForm().$container;
  }.bind(this);
};

/**
 * The button form-field has no label and no status. Additionally it also has no container.
 * Container and field are the same thing.
 */
scout.Button.prototype._render = function($parent) {
  var cssClass, $button;
  if (this.displayStyle === scout.Button.DisplayStyle.LINK) {
    /* Render as link-button/ menu-item.
     * This is a bit weird: the model defines a button, but in the UI it behaves like a menu-item.
     * Probably it would be more reasonable to change the configuration (which would lead to additional
     * effort required to change an existing application).
     */
    $button = $parent.makeDiv('menu-item');
    $button.setTabbable(this.enabled);
    cssClass = 'link-button';
  } else {
    // render as button
    $button = $parent.makeElement('<button>');
    cssClass = 'button';
  }
  this._$label = $button.appendSpan('button-label');
  this.addContainer($parent, cssClass, new scout.ButtonLayout(this));
  this.addField($button);
  // FIXME CGU: should we add a label? -> would make it possible to control the space left of the button using labelVisible, like it is possible with checkboxes
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
      $button.addClass('has-submenu');
    }
  }
  $button.unfocusable();
  this.session.keyStrokeManager.installKeyStrokeContext(this.formKeyStrokeContext);
};

scout.Button.prototype._remove = function() {
  scout.Button.parent.prototype._remove.call(this);
  this.session.keyStrokeManager.uninstallKeyStrokeContext(this.formKeyStrokeContext);
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
  var popup = scout.create(scout.MenuBarPopup, {
    parent: this,
    menu: this
  });
  popup.open();
  return popup;
};
scout.Button.prototype._doActionTogglesSubMenu = function() {
  return false;
};

scout.Button.prototype.setSelected = function(selected) {
  this.selected = selected;
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
  this._$label.textOrNbsp(scout.strings.removeAmpersand(this.label));
  // Invalidate layout because button may now be longer or shorter
  this.htmlComp.invalidateLayoutTree();
};

/**
 * Adds an image or font-based icon to the button by adding either an IMG or SPAN element to the button.
 */
scout.Button.prototype._renderIconId = function() {
  this.$field.icon(this.iconId);
  if (this.iconId) {
    var $icon = this.$field.data('$icon');
    $icon.toggleClass('with-label', !!this.label);
  }
  // Invalidate layout because button may now be longer or shorter
  this.htmlComp.invalidateLayoutTree();
};

scout.Button.prototype._syncKeyStroke = function(keyStroke) {
  this.keyStroke = keyStroke;
  this.buttonKeyStroke.parseAndSetKeyStroke(this.keyStroke);
};

scout.Button.prototype._onClick = function() {
  if (this.enabled) {
    this.doAction();
  }
};
