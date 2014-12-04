scout.Action = function() {
  this._hoverBound = false;
  scout.Action.parent.call(this);
};
scout.inherits(scout.Action, scout.ModelAdapter);

scout.Action.prototype._renderProperties = function() {
  scout.Action.parent.prototype._renderProperties.call(this);

  this._renderText(this.text);
  this._renderIconId(this.iconId);
  this._renderTooltipText(this.tooltipText);
  this._renderKeystroke(this.keystroke);
  this._renderEnabled(this.enabled);
  this._renderSelected(this.selected);
  this._renderVisible(this.visible);
};

scout.Action.prototype._remove = function() {
  scout.Action.parent.prototype._remove.call(this);
  this._hoverBound = false;
};

scout.Action.prototype._renderText = function(text) {
  text = text || '';
  this.$container.text(text);
};

scout.Action.prototype._renderIconId = function(iconId) {
  iconId = iconId || '';
  this.$container.icon(iconId);
};

scout.Action.prototype._renderEnabled = function(enabled) {
  this.$container.setEnabled(enabled);
};

scout.Action.prototype._renderVisible = function(enabled) {
  this.$container.setVisible(enabled);
};

scout.Action.prototype._renderSelected = function(selected) {
  this.$container.select(selected);
};

scout.Action.prototype._renderKeystroke = function(keystroke) {
  keystroke = keystroke || '';
  this.$container.attr('data-shortcut', keystroke);
};

scout.Action.prototype._renderTooltipText = function(tooltipText) {
  if (tooltipText && !this._hoverBound) {
    this.$container.hover(this._onHoverIn.bind(this), this._onHoverOut.bind(this));
    this._hoverBound = true;
  } else if (!tooltipText && this.hoverBound) {
    this.$container.off('mouseenter mouseleave');
    this._hoverBound = false;
  }
};

scout.Action.prototype._onHoverIn = function() {
  //Don't show tooltip if action is selected or not enabled
  if (this.enabled && !this.selected) {
    this._showTooltip();
  }
};

scout.Action.prototype._onHoverOut = function() {
  this._removeTooltip();
};

scout.Action.prototype._showTooltip = function() {
  this.tooltip = new scout.Tooltip(this._configureTooltip());
  this.tooltip.render();
};

scout.Action.prototype._configureTooltip = function() {
  return {
    text: this.tooltipText,
    $origin: this.$container,
    arrowPosition: 50,
    arrowPositionUnit: '%',
    position: this.tooltipPosition
  };
};

scout.Action.prototype._removeTooltip = function() {
  if (this.tooltip) {
    this.tooltip.remove();
    this.tooltip = null;
  }
};

scout.Action.prototype._goOffline = function() {
  this._renderEnabled(false);
};

scout.Action.prototype._goOnline = function() {
  this._renderEnabled(true);
};

scout.Action.prototype.sendDoAction = function() {
  this.session.send('doAction', this.id);
};

scout.Action.prototype.sendSelected = function(selected) {
  this.session.send('selected', this.id, {
    selected: selected
  });
};
