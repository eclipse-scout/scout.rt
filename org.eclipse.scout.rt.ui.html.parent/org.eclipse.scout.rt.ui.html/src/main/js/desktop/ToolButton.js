//FIXME maybe rename to FormToolButton or DesktopToolButton, or rename DesktopViewButton
scout.ToolButton = function() {
  scout.ToolButton.parent.call(this);
  this._addAdapterProperties(['form']);
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
    that.parent.formOfClickedButton = that.form;
    that._setSelected(!that.selected);
    that.parent.formOfClickedButton = null;
  }
};

scout.ToolButton.prototype._setForm = function(form) {
  // NOP //FIXME CGU eventuell funktion nicht aufrufen wenn es sie nicht gibt? Oder müsste hier rendering passieren?
};

scout.ToolButton.prototype._setSelected = function(selected) {
//  if (this.selected === selected) {
//    return; //FIXME CGU wird zur Zeit vom Caller geprüft (DesktopTaskBar), wäre es nicht besser hier? bräuchte aber ein oldValue param oder was ähnliches
//  }

  this.$container.select(selected);

  this.parent.toolButtonSelected(this, selected);

  if (this.selected !== selected) {
    this.session.send('selected', this.id);
  }
  this.selected = selected;   //FIXME CGU necessary to prevent double sending when closing / opening phone form
};

scout.ToolButton.prototype._setEnabled = function(enabled) {

  //FIXME CGU not the same as taskbar disabled, this is the real disabled state
  //  if (enabled) {
  //    this.$container.removeClass('disabled');
  //  } else {
  //    this.$container.addClass('disabled');
  //  }
};
