/**
 * This specialised subclass is required to avoid flickering when we click on the "navigate up" button.
 * Without this menu, the server would send a property change for detailFormVisible after the button has pressed,
 * which results in a slight delay. With this class and the _navigateUp flag we can avoid this delay. However,
 * the price for this is code duplication, because the JS code does the same thing as the Java code on the server
 * side.
 */
scout.MenuNavigateUp = function() {
  scout.MenuNavigateUp.parent.call(this);
};
scout.inherits(scout.MenuNavigateUp, scout.Menu);

scout.MenuNavigateUp.prototype._onMenuClicked = function(event) {
  this.session.desktop.outline._navigateUp = true;
  scout.MenuNavigateUp.parent.prototype._onMenuClicked.call(this, event);
};
