scout.Button = function() {
  scout.Button.parent.call(this);
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

/**
 * The button form-field has no label and no status. Additionally it also has no container.
 * Container and field are the same thing.
 */
scout.Button.prototype._render = function($parent) {
  this.addContainer($parent, 'button', new scout.ButtonLayout());
  var $button = $('<button>')
     .on('click', this._onClick.bind(this));
  this.addField($button);
  if (this.systemType === scout.Button.SYSTEM_TYPE.OK) {
    $button.addClass('default-button');
  }
};

scout.Button.prototype._onClick = function() {
  this.session.send(this.id, 'clicked');
};

scout.Button.prototype._renderLabel = function(label) {
  if (label) {
    label = scout.strings.removeAmpersand(label);
  } else {
    label = '';
  }
  this.$field.text(label);
};
