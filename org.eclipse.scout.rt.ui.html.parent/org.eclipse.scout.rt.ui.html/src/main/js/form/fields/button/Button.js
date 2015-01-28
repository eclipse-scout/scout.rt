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
  if (this.systemType === scout.Button.SYSTEM_TYPE.OK) {
    $button.addClass('default-button');
  }
};

scout.Button.prototype._onClick = function() {
  this.session.send(this.id, 'clicked');
};

/**
 * @override
 */
scout.Button.prototype._renderProperties = function() {
  scout.Button.parent.prototype._renderProperties.call(this);
  this._renderLabel();
  this._renderIconId();
};

scout.Button.prototype._renderLabel = function() {
  this.$field.text(this.label ? scout.strings.removeAmpersand(this.label) : '');
};

scout.Button.prototype._renderIconId = function() {
  if (this.iconId) {
    var $img = this.$field.find('img');
    if ($img.length === 0) {
      $img = $('<img>');
      this.$field.prepend($img);
    }
    $img.attr('src', '/static/' + this.iconId + '?sessionId=' + this.session.jsonSessionId);
    $img.toggleClass('with-label', !!this.label);
  } else {
    this.$field.find('img').remove();
  }
};
