scout.TableHeaderMenuButton = function() {
  scout.TableHeaderMenuButton.parent.call(this);
  this._addEventSupport();
  this.text;
  this.cssClass;
  this.enabled = true;
  this.visible = true;
  this.selected = false;
  this.togglable = false;
  this.icon;
};
scout.inherits(scout.TableHeaderMenuButton, scout.Widget);

scout.TableHeaderMenuButton.prototype._init = function(options) {
  scout.TableHeaderMenuButton.parent.prototype._init.call(this, options);
  $.extend(this, options);
  this.text = this.session.text(this.textKey);
  this.visible = scout.nvl(options.visible, true);
};

scout.TableHeaderMenuButton.prototype._render = function($parent) {
  this.$container = $parent.appendDiv('table-header-menu-command')
    .on('click', this._onClick.bind(this))
    .on('mouseenter click', this._onMouseOver.bind(this))
    .on('mouseleave', this._onMouseOut.bind(this));
  if (this.cssClass) {
    this.$container.addClass(this.cssClass);
  }
  this._renderVisible();
  this._renderSelected();
  this._renderTogglable();
  this._renderIcon();
  this._renderEnabled();
};

scout.TableHeaderMenuButton.prototype._onClick = function() {
  if (this.enabled) {
    this.clickHandler.call(this);
  }
};

// Show 'remove' text when button is already selected
scout.TableHeaderMenuButton.prototype._onMouseOver = function() {
  if (this.enabled) {
    var text = this.selected ?
        this.session.text('ui.remove') : this.text;
    this.parent.appendText(text);
  }
};

scout.TableHeaderMenuButton.prototype._onMouseOut = function() {
  this.parent.resetText();
};

scout.TableHeaderMenuButton.prototype._renderVisible = function() {
  this.$container.setVisible(this.visible);
};

scout.TableHeaderMenuButton.prototype._renderSelected = function() {
  this.$container.select(this.selected);
};

scout.TableHeaderMenuButton.prototype._renderTogglable = function() {
  this.$container.toggleClass('togglable', this.togglable);
};

scout.TableHeaderMenuButton.prototype._renderIcon = function() {
  if (this.icon) {
    this.$container.attr('data-icon', this.icon);
  } else {
    this.$container.removeAttr('data-icon');
  }
};

scout.TableHeaderMenuButton.prototype.setVisible = function(visible) {
  this.visible = visible;
  if (this.rendered) {
    this._renderVisible();
  }
};

scout.TableHeaderMenuButton.prototype.setSelected = function(selected) {
  this.selected = selected;
  if (this.rendered) {
    this._renderSelected();
  }
  this._updateEnabled();
};

scout.TableHeaderMenuButton.prototype._updateEnabled = function() {
  var enabled = true;
  if (this.selected) {
    enabled = this.togglable;
  }
  this.enabled = enabled;
  if (this.rendered) {
    this._renderEnabled();
  }
};

scout.TableHeaderMenuButton.prototype._renderEnabled = function() {
  this.$container.toggleClass('disabled', !this.enabled);
};

scout.TableHeaderMenuButton.prototype.setIcon = function(icon) {
  this.icon = icon;
  if (this.rendered) {
    this._renderIcon();
  }
};

scout.TableHeaderMenuButton.prototype.toggle = function() {
  this.setSelected(!this.selected);
};
