scout.DesktopToolButton = function() {
  scout.DesktopToolButton.parent.call(this);
  this.desktop;
  this.$storage;
  this._addAdapterProperties('form');
};
scout.inherits(scout.DesktopToolButton, scout.ModelAdapter);

scout.DesktopToolButton.prototype._render = function($parent) {
  var iconId = this.iconId || '',
    keystroke = this.keystroke || '';

  this.$div = $parent
    .appendDIV('taskbar-tool-item', this.text)
    .attr('data-icon', iconId).attr('data-shortcut', keystroke);

  if (this.enabled) {
    this.$div.on('click', '', this._click.bind(this));
  } else  {
    this.$div.setClass('disabled');
  }

  if (this.selected && this.form) {
    this.$div.select(true);
    this._openContainer();
  }
};

scout.DesktopToolButton.prototype._click = function () {
  // without storage; event handling will do the job
  if (this.desktop.selectedTool === this) {
    this.desktop.selectedTool._sendSelected(false);
    this.desktop.selectedTool._closeContainer();
  } else if (this.desktop.selectedTool) {
    this.desktop.selectedTool._sendSelected(false);
    this.desktop.selectedTool._closeContainer();
    this._sendSelected(false);
    this._openContainer();
  } else if (this.$storage || this.form) {
    this._sendSelected(true);
    this._openContainer();
  } else {
    this._sendSelected(true);
  }
};

/* container handling and storage */

scout.DesktopToolButton.prototype._sendSelected = function (selected) {
  if (!this.session.processingEvents) {
    this.session.send('selected', this.id, { selected: selected });
  }
};

scout.DesktopToolButton.prototype._openContainer = function () {
  if (this.$storage) {
    this.desktop.$toolContainer.append(this.$storage);
  } else if (this.form) {
    this.form.render(this.desktop.$toolContainer);
  }

  var right = parseFloat(this.desktop.$parent[0].offsetWidth) - parseFloat(this.$div.offset().left) - parseFloat(this.$div[0].offsetWidth);
  this.desktop.$toolContainer
    .css('right', right)
    .show();

  this.desktop.selectedTool = this;
  this.$div.select(true);
};

scout.DesktopToolButton.prototype._closeContainer = function () {
  if (!this.session.processingEvents) {
    this.session.send('selected', this.id, { selected: false });
  }

  this.$storage = this.desktop.$toolContainer.children();
  this.desktop.$toolContainer.children().detach();

  this.desktop.$toolContainer.hide();

  this.desktop.selectedTool = null;
  this.$div.select(false);

};


/* event handling */

scout.DesktopToolButton.prototype._setForm = function () {
  if (this.form) {this._openContainer();}
};
