scout.DesktopToolButton = function() {
  scout.DesktopToolButton.parent.call(this);
  this.desktopTaskbar;
  this._addAdapterProperties('form');
};
scout.inherits(scout.DesktopToolButton, scout.ModelAdapter);

scout.DesktopToolButton.prototype._render = function($parent) {
  var state = this.state || '',
    iconId = this.iconId || '',
    keystroke = this.keystroke || '';

  this.$tool = $parent
    .appendDiv(this.id, 'taskbar-tool-item ' + state, this.text)
    .attr('data-icon', iconId).attr('data-shortcut', keystroke);

  if (!this.$tool.hasClass('disabled')) {
    this.$tool.on('click', '', onClick);
  }

  var that = this;
  function onClick() {
    that.desktopTaskBar.selectTool(that);

    if (!this.session.processingEvents) {
      this.session.send('selected', this.id, {
        selected: that.$container.isSelected()
      });
    }
  }
};
