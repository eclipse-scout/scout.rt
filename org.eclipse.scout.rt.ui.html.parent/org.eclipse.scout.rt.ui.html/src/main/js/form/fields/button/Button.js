scout.Button = function() {
  scout.Button.parent.call(this);
  this._$icon;
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
    $button.addClass('menu-item');
    cssClass = 'link-button';
  } else {
    // render as button
    $button = $('<button>');
    cssClass = 'button';
  }

  this.addContainer($parent, cssClass, new scout.ButtonLayout(this));
  this.addField($button);
  $button.on('click', this._onClick.bind(this));
  if (this.systemType === scout.Button.SYSTEM_TYPE.OK ||
      this.systemType === scout.Button.SYSTEM_TYPE.SAVE_WITHOUT_MARKER_CHANGE) {
    $button.addClass('default-button');
  }
};

scout.Button.prototype._onClick = function() {
  if (this.displayStyle === scout.Button.DISPLAY_STYLE.TOGGLE) {
    this.selected = !this.selected;
    this._renderSelected();
    this.session.send(this.id, 'selected', {selected: this.selected});
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

scout.Button.prototype._renderSelected = function() {
  if (this.displayStyle === scout.Button.DISPLAY_STYLE.TOGGLE) {
    this.$field.toggleClass('selected', !!this.selected);
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
      iconChar = this.iconId.substr(5, 1);
      $icon = $('<span>')
        .addClass('font-icon')
        .text(iconChar);
    } else {
      $icon = $('<img>')
        .attr('src', scout.helpers.dynamicResourceUrl(this, this.iconId));
    }
    $icon.toggleClass('with-label', !!this.label);
    this.$field.prepend($icon);
  }
};
