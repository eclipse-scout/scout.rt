/**
 * Uses the same model
 *  as a real menu (childNodes, menuTypes etc.) and the same signature as well (render, remove) to make it compatible with the menubar.
 * @implements {scout.Menu}
 */
scout.DetachFormMenu = function(form, session) {
  this.form = form;
  this.session = session;
  this.childMenus = [];
  this.menuTypes = [];
  this.visible = true;
};

scout.DetachFormMenu.prototype.render = function($parent) {
  this.$container = $parent
    .appendDiv('menu-item detach-menu')
    .on('click', '', onClicked.bind(this));

  function onClicked(event) {
    if (!this.$container.isEnabled()) {
      return;
    }

    this._onMenuClicked(event);
  }
};

scout.DetachFormMenu.prototype._onMenuClicked = function(event) {
  // TODO BSH Detach | How to pass form to popup? How to handle F5 in popup?
  var childWindow = scout.helpers.openWindow(window.location.href, 'scout:form:' + this.id, 800, 600);
  $(childWindow).one('load', function() {
    // Cannot call this directly, because we get an unload event right after that (and
    // would therefore unregister the window again). This is because the browser starts
    // with the 'about:blank' page. Opening the desired URL causes the blank page to unload.
    // Therefore, we wait until the target page was loaded.
    this.session.registerChildWindow(childWindow);
  }.bind(this));
};

scout.DetachFormMenu.prototype.remove = function($parent) {
  this.$container.remove();
};
