/**
 * Button utility class for a set of buttons, where each button has an option value.
 *
 * Constructor arguments:
 *
 * @param $parent
 *          required
 * @param onClickHandler
 *          optional, global onClickHandler to attach to each button without a specific clickHandler
 */
scout.BoxButtons = function($parent, onClickHandler) {
  if (!$parent) {
    throw new Error('Missing $parent');
  }
  $parent.addClass('box-buttons');

  this._$parent = $parent;
  this._onClickHandler = onClickHandler;

  this._$buttons = [];
};

/**
 * @param opts
 *          [text] required, text of button to add
 *          [tabIndex] optional, tabindex to assign (default '0')
 *          [enabled] optional, if button should be enabled or not (default true)
 *          [onClick] optional, function to be executed when button is clicked.
 *            This function does _not_ have to check by itself if the button is
 *            enabled. If this argument is omitted, the global onClickHandler is
 *            used (see constructor).
 *          [option] optiona, a string that is assigned to be button and is passed
 *            to the global onClickHandler as an argument.
 */
scout.BoxButtons.prototype.addButton = function(opts) {
  opts = opts || {};

  var $button = $('<div>')
    .text(scout.strings.removeAmpersand(opts.text))
    .attr('tabindex', opts.tabIndex || '0')
    .addClass('button')
    .unfocusable()
    .setEnabled(scout.helpers.nvl(opts.enabled, true))
    .appendTo(this._$parent);

  if (opts.onClick) {
    var onClick = opts.onClick;
    $button.on('click', function(event) {
      if ($.suppressEventIfDisabled(event)) {
        return;
      }
      onClick(event);
    });
  } else if (this._onClickHandler) {
    $button.on('click', this._onClick.bind(this));
  }
  $button.data('buttonOption', opts.option);

  this._$buttons.push($button);
  return $button;
};

scout.BoxButtons.prototype._onClick = function(event) {
  var $button = $(event.target);
  if ($.suppressEventIfDisabled(event, $button)) {
    return;
  }
  this._onClickHandler(event, $button.data('buttonOption'));
};

scout.BoxButtons.prototype.updateButtonWidths = function(availableWidth) {
  // Find all visible buttons
  var $visibleButtons = [];
  this._$buttons.forEach(function($button) {
    if ($button.isVisible()) {
      $visibleButtons.push($button);
    }
  });

  var hasVisibleButtons = $visibleButtons.length > 0;
  this._$parent.toggleClass('empty', !hasVisibleButtons);

  // Manually calculate equal width fore each button, adding remaining pixels to last button.
  // (We don't use CSS percentage values, because sometimes browser calculations lead to wrong results.)
  availableWidth = availableWidth || this._$parent.width();
  var w = Math.floor(availableWidth / $visibleButtons.length);
  $visibleButtons.forEach(function($button, index) {
    if (index === $visibleButtons.length - 1) {
      w = availableWidth;
    } else {
      availableWidth -= w;
    }
    $button.outerWidth(w);
  });
};

scout.BoxButtons.prototype.buttonCount = function() {
  return this._$buttons.length;
};
