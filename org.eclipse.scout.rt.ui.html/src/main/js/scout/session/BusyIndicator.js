scout.BusyIndicator = function(session, cancellable) {
  scout.BusyIndicator.parent.call(this);
  this.parent = session.desktop;
  this.session = session;
  this._cancellable = (cancellable === undefined ? true : !!cancellable);
  this._addEventSupport();
};
scout.inherits(scout.BusyIndicator, scout.Widget);

scout.BusyIndicator.prototype._render = function($parent) {
  // 1. Render glasspane
  this._glassPaneRenderer = new scout.GlassPaneRenderer(this, true);
  this._glassPaneRenderer.renderGlassPanes();
  this._glassPaneRenderer.eachGlassPane(function($glassPane) {
    $glassPane
      .addClass('busy')
      .setMouseCursorWait(true);
  });

  // 2. Render busy indicator (still hidden by CSS, will be shown later in setTimeout)
  this.$container = $parent.appendDiv('busyindicator hidden');

  var $handle = this.$container.appendDiv('drag-handle');
  this.$container.makeDraggable($handle);

  this.$content = this.$container.appendDiv('busyindicator-content');
  this.$label = this.$content.appendDiv('busyindicator-label');

  if (this._cancellable) {
    this.$buttons = this.$container.appendDiv('busyindicator-buttons');
    this.$cancelButton = this._createButton('cancel', this.session.text('Cancel'));
    this.$cancelButton.css('width', '100%');
  }
  else {
    this.$content.addClass('no-buttons');
  }

  // Render properties
  this.$label.text(this.session.text('ui.PleaseWait_'));

  // Prevent resizing when message-box is dragged off the viewport
  this.$container.addClass('calc-helper');
  this.$container.css('min-width', this.$container.width());
  this.$container.removeClass('calc-helper');
  // Now that all texts, paddings, widths etc. are set, we can calculate the position
  this._position();

  // Show busy box with a delay of 2.5 seconds.
  this._busyIndicatorTimeoutId = setTimeout(function() {
    this.$container.removeClass('hidden').addClassForAnimation('shown');
  }.bind(this), 2500);
};

scout.BusyIndicator.prototype._postRender = function() {
  this.$container.installFocusContext(scout.FocusRule.AUTO, this.session.uiSessionId);
};

scout.BusyIndicator.prototype._remove = function() {
  this.$container.uninstallFocusContext(this.session.uiSessionId);

  // Remove busy box (cancel timer in case it was not fired yet)
  clearTimeout(this._busyIndicatorTimeoutId);
  scout.BusyIndicator.parent.prototype._remove.call(this);

  // Remove glasspane
  this._glassPaneRenderer.eachGlassPane(function($glassPane) {
    $glassPane
      .removeClass('busy')
      .setMouseCursorWait(false);
  });
  this._glassPaneRenderer.removeGlassPanes();
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
