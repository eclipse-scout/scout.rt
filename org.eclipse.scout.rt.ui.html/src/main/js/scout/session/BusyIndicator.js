scout.BusyIndicator = function(session, cancellable) {
  scout.BusyIndicator.parent.call(this);
  this._session = session;
  this._cancellable = (cancellable === undefined ? true : !!cancellable);
  this._addEventSupport();
};
scout.inherits(scout.BusyIndicator, scout.Widget);

scout.BusyIndicator.prototype._render = function($parent) {
  this.$container = $parent.appendDiv('busyindicator');

  var $handle = this.$container.appendDiv('drag-handle');
  this.$container.makeDraggable($handle);

  this.$content = this.$container.appendDiv('busyindicator-content');
  this.$label = this.$content.appendDiv('busyindicator-label');

  if (this._cancellable) {
    this.$buttons = this.$container.appendDiv('busyindicator-buttons');
    this.$cancelButton = this._createButton('cancel', this._session.text('Cancel'));
    this.$cancelButton.css('width', '100%');
  }
  else {
    this.$content.addClass('no-buttons');
  }

  // Render properties
  this.$label.text(this._session.text('ui.PleaseWait_'));

  // Prevent resizing when message-box is dragged off the viewport
  this.$container.addClass('calc-helper');
  this.$container.css('min-width', this.$container.width());
  this.$container.removeClass('calc-helper');
  // Now that all texts, paddings, widths etc. are set, we can calculate the position
  this._position();
  this.$container.addClassForAnimation('shown');
};

scout.BusyIndicator.prototype._position = function() {
  this.$container.cssMarginLeft(-this.$container.outerWidth() / 2);
};

scout.BusyIndicator.prototype._createButton = function(option, text) {
  text = scout.strings.removeAmpersand(text);
  return $('<button>')
    .text(text)
    .on('click', this._onButtonClicked.bind(this))
    .data('buttonOption', option)
    .appendTo(this.$buttons);
};

scout.BusyIndicator.prototype._onButtonClicked = function(event) {
  var $button = $(event.target);
  this.trigger('buttonClick', {
    option: $button.data('buttonOption')
  });
};
