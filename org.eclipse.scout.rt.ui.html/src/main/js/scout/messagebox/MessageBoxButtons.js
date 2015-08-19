/**
 * Button utility class for a set of buttons, where each button has an option value.
 */
scout.MessageBoxButtons = function($parent, onClickHandler) {
  this._$parent = $parent;
  this._onClickHandler = onClickHandler;
};

scout.MessageBoxButtons.prototype.renderButton = function(option, text) {
  text = scout.strings.removeAmpersand(text);
  return $('<div>')
    .text(text)
    .attr('tabindex', 0)
    .addClass('button')
    .unfocusable()
    .on('click', this._onClick.bind(this))
    .data('buttonOption', option)
    .appendTo(this._$parent);
};

scout.MessageBoxButtons.prototype._onClick = function(event) {
  var $button = $(event.target);
  this._onClickHandler({
    option: $button.data('buttonOption')
  });
};
