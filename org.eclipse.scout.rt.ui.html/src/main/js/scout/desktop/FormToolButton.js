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

scout.FormToolButton.prototype._remove = function() {
  scout.FormToolButton.parent.prototype._remove.call(this);
  this.popup = null;
};

scout.FormToolButton.prototype.toggle = function() {
  this.setSelected(!this.selected);
};

scout.FormToolButton.prototype._onMouseDown = function(event) {
  this.toggle();
};

scout.FormToolButton.prototype.setSelected = function(selected) {
  if (selected === this.selected) {
    return;
  }
  scout.FormToolButton.parent.prototype.setSelected.call(this, selected);
};

scout.FormToolButton.prototype._openContainer = function() {
  // Create a new popup if it was not yet created OR the form is not rendered (i.e. if it was closed by the model)
  if (!this.popup || !this.form.rendered) {
    this.popup = new scout.FormToolPopup(this, this.session);
    this.popup.render();
    this.addChild(this.popup);
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
  } else {
    if (this.form) {
      this._closeContainer();
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

/**
 * @override KeyStroke.js
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
scout.FormToolButton.prototype._createActionKeyStroke = function() {
  return new scout.FormToolButtonActionKeyStroke(this);
};

/**
 * FormToolButtonActionKeyStroke
 */
scout.FormToolButtonActionKeyStroke = function(action) {
  scout.FormToolButtonActionKeyStroke.parent.call(this, action);
};
scout.inherits(scout.FormToolButtonActionKeyStroke, scout.ActionKeyStroke);

scout.FormToolButtonActionKeyStroke.prototype.handle = function(event) {
  this.field.toggle();
};

scout.FormToolButtonActionKeyStroke.prototype._postRenderKeyBox = function($drawingArea) {
  if (this.field.iconId) {
    var wIcon = $drawingArea.find('.icon').width();
    var wKeybox = $drawingArea.find('.key-box').outerWidth();
    var containerPadding = Number($drawingArea.css('padding-left').replace('px', ''));
    var leftKeyBox = wIcon / 2 - wKeybox / 2 + containerPadding;
    $drawingArea.find('.key-box').css('left', leftKeyBox + 'px');
  }
};
