scout.FormToolButton = function() {
  scout.FormToolButton.parent.call(this);
  this.desktop;
  this.$storage;
  this.popup;
  this._addAdapterProperties('form');
};
scout.inherits(scout.FormToolButton, scout.Action);

scout.FormToolButton.prototype._render = function($parent) {
  this.$container = $parent.appendDiv('taskbar-tool-item');
};

scout.FormToolButton.prototype.toggle = function() {
  if (this.desktop.selectedTool === this) {
    this.setSelected(false);
  } else {
    this.setSelected(true);
  }
};

scout.FormToolButton.prototype._onMouseDown = function(event) {
  this.toggle();
};

scout.FormToolButton.prototype.setSelected = function(selected) {
  if (selected === this.selected) {
    return;
  }

  if (this.desktop.selectedTool && this.desktop.selectedTool !== this) {
    this.desktop.selectedTool.setSelected(false);
  }

  this.selected = selected;
  this.sendSelected(selected);
  this._renderSelected(this.selected);
};

scout.FormToolButton.prototype._openContainer = function() {
  if (!this.popup) {
    this.popup = new scout.FormToolPopup(this, this.session);
    this.popup.render();
  } else {
    this.popup.attach();
  }

};

scout.FormToolButton.prototype._closeContainer = function() {
  if (this.popup) {
    this.popup.detach();
  }
};

/* event handling */

scout.FormToolButton.prototype._renderForm = function() {
  if (!this.rendered) {
    //Don't execute initially since _renderSelected will be executed
    return;
  }
  this._renderSelected(this.selected);
};

scout.FormToolButton.prototype._renderSelected = function(selected) {
  if (selected) {
    if (this.form) {
      this._openContainer();
    }
    this.desktop.selectedTool = this;
  } else {
    if (this.form) {
      this._closeContainer();
    }
    //Don't update the desktop initially -> only the selected tool is allowed to set it
    if (this.rendered) {
      this.desktop.selectedTool = null;
    }
  }
};

scout.FormToolButton.prototype._renderEnabled = function(enabled) {
  if (enabled) {
    this.$container.on('mousedown', '', this._onMouseDown.bind(this));
  } else {
    this.$container.off('mousedown');
  }
  this.$container.setEnabled(enabled);
};

scout.FormToolButton.prototype.handle = function(event) {
  this.toggle();
  if (this.preventDefaultOnEvent) {
    event.preventDefault();
  }
};
