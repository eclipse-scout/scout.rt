/**
 * KeyStroke to prevent the browser from switching between browser tabs.
 *
 * See DisableBrowserTabSwitchingKeyStroke.js where switching between views is implemented, but only up to the current number of open views.
 * That means, that if 3 views are open, ctrl-4 is prevented by this keystroke.
 */
scout.DisableBrowserTabSwitchingKeyStroke = function(desktop) {
  scout.DisableBrowserTabSwitchingKeyStroke.parent.call(this);
  this.field = desktop;

  // modifier
  this.parseAndSetKeyStroke(desktop.autoTabKeyStrokeModifier);

  // range [1..9]
  this.registerRange(
    scout.keys['1'], // range from
    scout.keys['9'] // range to
  );

  // rendering hints
  this.renderingHints.render = false;

  this.preventDefault = true;
};
scout.inherits(scout.DisableBrowserTabSwitchingKeyStroke, scout.RangeKeyStroke);

/**
 * @override KeyStroke.js
 */
scout.DisableBrowserTabSwitchingKeyStroke.prototype._isEnabled = function() {
  var enabled = scout.DisableBrowserTabSwitchingKeyStroke.parent.prototype._isEnabled.call(this);
  return enabled && this.field.autoTabKeyStrokesEnabled;
};

/**
 * @override KeyStroke.js
 */
scout.DisableBrowserTabSwitchingKeyStroke.prototype.handle = function(event) {
  // NOOP
};
