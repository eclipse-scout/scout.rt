/**
 * Button utility class for a set of buttons, where each button has an option value.
 * The parent passed to the constructor must have a $buttons property (JQuery object)
 * where the buttons will be added when renderButton is called.
 */
scout.MessageBoxButtons = function(parent) {
  this._parent = parent;
  this.events = new scout.EventSupport();
  this._clickHandler = this._onButtonClicked.bind(this);
};

scout.MessageBoxButtons.prototype.renderButton = function(option, text) {
  text = scout.strings.removeAmpersand(text);
  return $('<button>')
    .text(text)
    .unfocusable()
    .on('click', this._clickHandler)
    .data('buttonOption', option)
    .appendTo(this._parent.$buttons);
};

scout.MessageBoxButtons.prototype._onButtonClicked = function(event) {
  var $button = $(event.target);
  this._parent.trigger('buttonClick', {
    option: $button.data('buttonOption')
  });
};
