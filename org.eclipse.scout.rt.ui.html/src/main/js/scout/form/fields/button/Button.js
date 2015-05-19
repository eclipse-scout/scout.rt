scout.Button = function() {
  scout.Button.parent.call(this);
  this._$icon;
  this._addAdapterProperties('menus');
  this.keyStroke;
  this.defaultKeyStroke;
};
scout.inherits(scout.Button, scout.FormField);

scout.Button.SYSTEM_TYPE = {
  NONE: 0,
  CANCEL: 1,
  CLOSE: 2,
  OK: 3,
  RESET: 4,
  SAVE: 5,
  SAVE_WITHOUT_MARKER_CHANGE: 6
};

scout.Button.DISPLAY_STYLE = {
  DEFAULT: 0,
  TOGGLE: 1,
  RADIO: 2,
  LINK: 3
};

/**
 * The button form-field has no label and no status. Additionally it also has no container.
 * Container and field are the same thing.
 */
scout.Button.prototype._render = function($parent) {
  var cssClass, $button;
  if (this.displayStyle === scout.Button.DISPLAY_STYLE.LINK) {
    /* Render as link-button/ menu-item.
     * This is a bit weird: the model defines a button, but in the UI it behaves like a menu-item.
     * Probably it would be more reasonable to change the configuration (which would lead to additional
     * effort required to change an existing application).
     */
    $button = $('<div>');
    $button.setTabbable(this.enabled);
    $button.addClass('menu-item');
    cssClass = 'link-button';
  } else {
    // render as button
    $button = $('<button>');
    cssClass = 'button';
  }

  this.addContainer($parent, cssClass, new scout.ButtonLayout(this));
  this.addField($button);

  $button.on('click', this._onClick.bind(this))
  //prevent focus validation on other field on mouse down. -> Safari workaround
  .on('mousedown', function(event) {
    event.preventDefault();
  });
  if (this.systemType === scout.Button.SYSTEM_TYPE.OK ||
    this.systemType === scout.Button.SYSTEM_TYPE.SAVE_WITHOUT_MARKER_CHANGE) {
    $button.addClass('default-button');
  }
  if (this.menus && this.menus.length > 0) {
    for (var j = 0; j < this.menus.length; j++) {
      this.keyStrokeAdapter.registerKeyStroke(this.menus[j]);
    }
    if (this.label || !this.iconId) { // no indicator when _only_ the icon is visible
      $button.addClass('has-submenu');
    }
  }

  this._registerButtonKeyStroke();
};

scout.Button.prototype._createKeyStrokeAdapter = function() {
  return new scout.ButtonKeyStrokeAdapter(this);
};

scout.Button.prototype._onClick = function(event) {
  this.doAction($(event.target));
};

scout.Button.prototype._remove = function() {
  scout.Button.parent.prototype._remove.call(this);
  this._unregisterButtonKeyStroke();
};

scout.Button.prototype.doAction = function($target) {
  var activeValueField = $(document.activeElement).data('valuefield');
  if(activeValueField){
    activeValueField.displayTextChanged();
  }
  if (this.displayStyle === scout.Button.DISPLAY_STYLE.TOGGLE) {
    this.selected = !this.selected;
    this._renderSelected();
    this.session.send(this.id, 'selected', {
      selected: this.selected
    });
  } else if (this.menus.length > 0) {
    this.popup = new scout.MenuBarPopup(this, this.session);
    this.popup.render();
  } else {
    this.session.send(this.id, 'clicked');
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
  if (this.displayStyle === scout.Button.DISPLAY_STYLE.LINK) {
    this.$field.setTabbable(this.enabled);
  }
};

/**
 * @override
 */
scout.Button.prototype._renderVisible = function(visible) {
  scout.Button.parent.prototype._renderVisible.call(this, visible);
  if (visible) {
    if (this.menuBar && !this.$container.hasClass('last')) {
      this.menuBar.updateLastItemMarker();
    }
  } else {
    if (this.menuBar && this.$container.hasClass('last')) {
      this.menuBar.updateLastItemMarker();
    }
  }
};

scout.Button.prototype._renderSelected = function() {
  if (this.displayStyle === scout.Button.DISPLAY_STYLE.TOGGLE) {
    this.$field.toggleClass('selected', this.selected);
  }
};

/**
 * @override
 */
scout.Button.prototype._renderLabel = function(label) {
  this.$field.text(label ? scout.strings.removeAmpersand(label) : '');
};

/**
 * Adds an image or font-based icon to the button by adding either an IMG or SPAN element to the button.
 */
scout.Button.prototype._renderIconId = function() {
  var iconChar, $icon;
  this.$field.find('img, span').remove();
  if (this.iconId) {
    if (scout.strings.startsWith(this.iconId, "font:")) {
      iconChar = this.iconId.substr(5);
      $icon = $('<span>')
        .addClass('font-icon')
        .text(iconChar);
    } else {
      $icon = $('<img>')
        .attr('src', scout.helpers.dynamicResourceUrl(this, this.iconId));
    }
    $icon.toggleClass('with-label', !! this.label);
    this.$field.prepend($icon);
  }
};

scout.Button.prototype._registerButtonKeyStroke = function() {
  //register buttons key stroke on root Groupbox
  if (this.defaultKeyStroke) {
    this._unregisterButtonKeyStroke();
  }
  if (this.keyStroke) {
    this.defaultKeyStroke = new scout.ButtonKeyStroke(this, this.keyStroke);
    this.getForm().rootGroupBox.keyStrokeAdapter.registerKeyStroke(this.defaultKeyStroke);
  }
};

scout.Button.prototype._unregisterButtonKeyStroke = function() {
  //unregister buttons key stroke on root Groupbox
  if (this.defaultKeyStroke) {
    this.getForm().rootGroupBox.keyStrokeAdapter.unregisterKeyStroke(this.defaultKeyStroke);
  }
};

scout.Button.prototype._syncKeyStroke = function(keyStroke) {
  this.keyStroke = keyStroke;
  this._registerButtonKeyStroke();
};
