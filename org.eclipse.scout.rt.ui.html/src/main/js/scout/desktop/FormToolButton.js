scout.FormToolButton = function() {
  scout.FormToolButton.parent.call(this);
  this.desktop;
  this.$content;
  this.$title;
  this.popup;
  this._addAdapterProperties('form');
};
scout.inherits(scout.FormToolButton, scout.Action);

scout.FormToolButton.prototype._render = function($parent) {
  this.$container = $parent
      .appendDiv('taskbar-tool-item')
      .unfocusable();
  this.$title = this.$container.appendSpan('taskbar-tool-item-title');
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

  scout.FormToolButton.parent.prototype.setSelected.call(this, selected);
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
    // Don't execute initially since _renderSelected will be executed
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
    // Don't update the desktop initially -> only the selected tool is allowed to set it
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

/**
 * @override Action.js
 */
scout.FormToolButton.prototype._renderText = function(text) {
  text = text || '';
  this.$title.text(text);
  if (this.popup) {
    this.popup.rerenderHead();
    this.popup.alignTo();
  }
};

/**
 * @override Action.js
 */
scout.FormToolButton.prototype._drawKeyBox = function($container) {
  scout.FormToolButton.parent.prototype._drawKeyBox.call(this, $container);
  if(this.iconId){
    var wIcon = this.$container.find('.icon').width();
    var wKeybox = this.$container.find('.key-box').outerWidth();
    var containerPadding = Number(this.$container.css('padding-left').replace('px', ''));
    var leftKeyBox = wIcon/2 - wKeybox/2 + containerPadding;
    this.$container.find('.key-box').css('left', leftKeyBox+'px');
  }
};
