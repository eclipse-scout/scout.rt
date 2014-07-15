//FIXME maybe rename to FormToolButton or DesktopToolButton, or rename DesktopViewButton
scout.ToolButton = function() {
  scout.ToolButton.parent.call(this);
  this.desktopTaskBar;
  this._addAdapterProperties('form');
};
scout.inherits(scout.ToolButton, scout.ModelAdapter);

scout.ToolButton.prototype._render = function($parent) {
  var state = this.state || '',
    iconId = this.iconId || '',
    keystroke = this.keystroke || '';

  this.$container = $parent
    .appendDiv(this.id, 'taskbar-item ' + state, this.text)
    .attr('data-icon', iconId).attr('data-shortcut', keystroke);

  if (!this.$container.hasClass('disabled')) {
    this.$container.on('click', '', onClick);
  }

  var that = this;

  function onClick() {
    that.desktopTaskBar.formOfClickedButton = that.form;
    that._setSelected(!that.selected);
    that.desktopTaskBar.formOfClickedButton = null;
  }
};

scout.ToolButton.prototype._setForm = function(form) {
  this.form = form;
  //dynamically changing a form not supported so far
};

scout.ToolButton.prototype._setSelected = function(selected) {
  if (selected == this.$container.isSelected()) {
    return;
  }

  this.selected = selected;
  this.$container.select(selected);
  this.desktopTaskBar.toolButtonSelected(this, selected);

  if (!this.session.processingEvents) {
    this.session.send('selected', this.id, {
      selected: selected
    });
  }
};

scout.ToolButton.prototype._setEnabled = function(enabled) {

  //FIXME CGU not the same as taskbar disabled, this is the real disabled state
  //  if (enabled) {
  //    this.$container.removeClass('disabled');
  //  } else {
  //    this.$container.addClass('disabled');
  //  }
};
