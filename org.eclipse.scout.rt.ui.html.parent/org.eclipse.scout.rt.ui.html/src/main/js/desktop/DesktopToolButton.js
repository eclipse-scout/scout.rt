scout.DesktopToolButton = function() {
  scout.DesktopToolButton.parent.call(this);
  this.desktop;
  this.$storage;
  this._addAdapterProperties('form');
};
scout.inherits(scout.DesktopToolButton, scout.Action);

scout.DesktopToolButton.prototype._render = function($parent) {
  this.$container = $parent.appendDIV('taskbar-tool-item');
};

scout.DesktopToolButton.prototype._onMouseDown = function() {
  if (this.desktop.selectedTool === this) {
    this.desktop.selectedTool.setSelected(false);
  } else if (this.desktop.selectedTool) {
    this.desktop.selectedTool.setSelected(false);
    this.setSelected(true);
  } else {
    this.setSelected(true);
  }
};

scout.DesktopToolButton.prototype.setSelected = function(selected) {
  if (selected === this.selected) {
    return;
  }

  this.selected = selected;
  this.session.send('selected', this.id, {
    selected: selected
  });
  this._renderSelected(this.selected);
};

scout.DesktopToolButton.prototype._openContainer = function() {
  if (this.$storage && this.$storage.length > 0) {
    this.desktop.$toolContainer.append(this.$storage);
  } else if (this.form) {
    this.form.menubarPosition = 'bottom';
    this.form.detachable = false;
    this.form.render(this.desktop.$toolContainer);
    this.form.htmlComp.pixelBasedSizing = true;
    this.form.htmlComp.pack();
  }
  this.desktop.$toolContainer.data('toolButton', this);

  var right = parseFloat(this.desktop.$parent[0].offsetWidth) - parseFloat(this.$container.offset().left) - parseFloat(this.$container[0].offsetWidth);
  this.desktop.$toolContainer
    .css('right', right)
    .show();

  // find the 1st focusable element in the $container
  // TODO AWE wollen wir nicht einfach JQuery UI :focusable dafür verwenden?
  // SetTimeout is used because the container is opened on mouse down
  setTimeout(function() {
    this.desktop.$toolContainer.find('input').first().focus();
  }.bind(this), 0);
};

scout.DesktopToolButton.prototype._closeContainer = function() {
  if (this.desktop.$toolContainer.data('toolButton') !== this) {
    //Don't close container of other buttons
    return;
  }

  this.$storage = this.desktop.$toolContainer.children();
  this.desktop.$toolContainer.children().detach();
  this.desktop.$toolContainer.hide();
  this.desktop.$toolContainer.removeData('toolButton');
};

/* event handling */

scout.DesktopToolButton.prototype._renderForm = function() {
  if (!this.rendered) {
    //Don't execute initially since _renderSelected will be executed
    return;
  }
  this._renderSelected(this.selected);
};

scout.DesktopToolButton.prototype._renderSelected = function(selected) {
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
