// FIXME AWE: use LocalObject?
// FIXME AWE: add "no-overflow" property to menu-bar / -item
scout.NavigationMenu = function(outline, node) {
  scout.NavigationMenu.parent.call(this);
  this.node = node;
  this.outline = outline;
  this.session = outline.session;
  this._onClickFunc;
  this.selected = false;
  this.visible = true;
  this.enabled = true;
  this.mandatory = false;
  this.actionStyle = scout.Action.ActionStyle.BUTTON;

  this._upButton = new scout.NavigateUpButton(outline, node);
  this._downButton = new scout.NavigateDownButton(outline, node);

  this.addChild(this._upButton);
  this.addChild(this._downButton);

};
scout.inherits(scout.NavigationMenu, scout.Menu);

scout.NavigationMenu.prototype._render = function($parent) {
  this.$container = $.makeDiv('navigation-menu')
    .addClass('menu-item menu-button')
    .appendTo($parent);
  this._upButton.render(this.$container);
  this._downButton.render(this.$container);
};

scout.NavigationMenu.prototype._remove = function() {
  scout.NavigationMenu.parent.prototype._remove.call(this);
};

scout.NavigationMenu.prototype._renderText = function(text) {
  // NOP - text would override child elements in DOM
};

/**
 * Called when enabled state must be re-calculated and probably rendered.
 */
scout.NavigationMenu.prototype.updateEnabled = function() {
  this._upButton.updateEnabled();
  this._downButton.updateEnabled();
};
