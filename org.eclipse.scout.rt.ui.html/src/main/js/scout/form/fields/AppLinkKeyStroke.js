scout.AppLinkKeyStroke = function(field, appLinkTriggerFunction) {
  scout.AppLinkKeyStroke.parent.call(this);
  this.keyStroke = 'SPACE';
  this.drawHint = false;
  this.initKeyStrokeParts();
  this.appLinkTriggerFunction = appLinkTriggerFunction;
  this.field=field;
};
scout.inherits(scout.AppLinkKeyStroke, scout.KeyStroke);

scout.AppLinkKeyStroke.prototype.accept = function(event) {
  var $target = $(event.target);
  if($target.hasClass('app-link')){
    return true;
  }
  return false;
};

scout.AppLinkKeyStroke.prototype.handle = function(event) {
  this.appLinkTriggerFunction.call(this.field, event);
};
