scout.FormToolButton = function() {
  scout.FormToolButton.parent.call(this);
  this.desktop;
  this.$storage;
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

scout.FormToolButton.prototype._onMouseDown = function() {
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

  var containerOffsetBounds = scout.graphics.offsetBounds(this.$container),
    right = this.desktop.$parent.outerWidth() - containerOffsetBounds.x - containerOffsetBounds.width;

  // Tool container has to be visible before rendering and layouting, otherwise sizes cannot be read
  this.desktop.$toolContainer
    .css('right', right)
    .show();

  if (this.$storage && this.$storage.length > 0) {
    this.desktop.$toolContainer.append(this.$storage);
  } else if (this.form) {
    this.form.rootGroupBox.menuBarPosition = 'bottom';
    this.form.render(this.desktop.$toolContainer);
    this.form.htmlComp.pixelBasedSizing = true;
    this.form.htmlComp.pack();
  }
  this.desktop.$toolContainer.data('toolButton', this);

  setTimeout(function() {
    this.desktop.$toolContainer.installFocusContext('auto', this.session.uiSessionId);
    this.desktop.$toolContainer.focus();
  }.bind(this), 0);
};

scout.FormToolButton.prototype._closeContainer = function() {
  if (this.desktop.$toolContainer.data('toolButton') !== this) {
    //Don't close container of other buttons
    return;
  }

  this.$storage = this.desktop.$toolContainer.children();
  this.desktop.$toolContainer.children().detach();
  this.desktop.$toolContainer.hide();
  this.desktop.$toolContainer.removeData('toolButton');
  this.desktop.$toolContainer.uninstallFocusContext(this.session.uiSessionId);
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
  this.$container.select(selected);

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
