scout.BusyIndicator = function(session, cancellable) {
  scout.BusyIndicator.parent.call(this);
  this.parent = session.desktop;
  this.session = session;
  this._cancellable = (cancellable === undefined ? true : !!cancellable);
  this._addEventSupport();
};
scout.inherits(scout.BusyIndicator, scout.Widget);

scout.BusyIndicator.prototype.renderGlassPanes = function() {
  this._glassPaneRenderer = new scout.GlassPaneRenderer(this, true);
  this._glassPaneRenderer.renderGlassPanes();
  this._changeMouseCursorToWaitStyle();
};

scout.BusyIndicator.prototype._render = function($parent) {
  this.$container = $parent.appendDiv('busyindicator');

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
  this.$container.addClassForAnimation('shown');
};

scout.BusyIndicator.prototype._postRender = function() {
  this.$container.installFocusContext(scout.FocusRule.AUTO, this.session.uiSessionId);
};

scout.BusyIndicator.prototype.remove = function() {
  this._changeMouseCursorToDefaultStyle();
  this._glassPaneRenderer.removeGlassPanes();

  scout.BusyIndicator.parent.prototype._remove.call(this);
};

scout.BusyIndicator.prototype._remove = function() {
  this.$container.uninstallFocusContext(this.session.uiSessionId);

  scout.BusyIndicator.parent.prototype._remove.call(this);
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

scout.BusyIndicator.prototype._changeMouseCursorToWaitStyle = function() {
  // Workaround Chrome:
  // Trigger cursor change; otherwise, the cursor is not updated without moving the mouse first.
  // See https://code.google.com/p/chromium/issues/detail?id=26723

  // Change cursor to 'wait' style.
  this._glassPaneRenderer.eachGlassPane(function($glassPane) {
    $glassPane.addClass('busy');
    $glassPane.css('cursor', 'default'); // [Workaround Chrome]
  });

  // >>> [Workaround Chrome]
  setTimeout(function() {
    this._glassPaneRenderer.eachGlassPane(function($glassPane) {
      $glassPane.css('cursor', 'wait');
    });
  }.bind(this), 0);
  // <<< [Workaround Chrome]
};

scout.BusyIndicator.prototype._changeMouseCursorToDefaultStyle = function() {
  // Workaround Chrome:
  // Cursor must be reset explicitly; otherwise, it would not be reset until moving the mouse.
  this._glassPaneRenderer.eachGlassPane(function($glassPane) {
    $glassPane.css('cursor', 'default');
    $glassPane.removeClass('busy');
  });
};
