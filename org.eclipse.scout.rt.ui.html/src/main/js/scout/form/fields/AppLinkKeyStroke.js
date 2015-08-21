scout.AppLinkKeyStroke = function(field, appLinkTriggerFunction) {
  scout.AppLinkKeyStroke.parent.call(this);
  this.field = field;
  this.appLinkTriggerFunction = appLinkTriggerFunction;

  this.which = [scout.keys.SPACE];
  this.renderingHints.render = false;
};
scout.inherits(scout.AppLinkKeyStroke, scout.KeyStroke);

/**
 * @override KeyStroke.js
 */
scout.AppLinkKeyStroke.prototype._accept = function(event) {
  var accepted = scout.AppLinkKeyStroke.parent.prototype._accept.call(this, event);
  return accepted && $(event.target).hasClass('app-link');
};

/**
 * @override KeyStroke.js
 */
scout.AppLinkKeyStroke.prototype.handle = function(event) {
  this.appLinkTriggerFunction.call(this.field, event);
};
