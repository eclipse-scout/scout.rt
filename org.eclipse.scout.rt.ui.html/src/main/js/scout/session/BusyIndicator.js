scout.BusyIndicator = function(session, cancellable) {
  scout.BusyIndicator.parent.call(this);
  this.parent = session.desktop;
  this.session = session;
  this._cancellable = (cancellable === undefined ? true : !!cancellable);
  this._addEventSupport();
};
scout.inherits(scout.BusyIndicator, scout.Widget);

scout.BusyIndicator.prototype._render = function($parent) {
  // 1. Render modality glasspanes (must precede adding the busy indicator to the DOM)
  this._glassPaneRenderer = new scout.GlassPaneRenderer(this, true, this.session.uiSessionId);
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
    var buttons = new scout.MessageBoxButtons(this);
    this.$buttons = this.$container.appendDiv('busyindicator-buttons');
    this.$cancelButton = buttons.renderButton('cancel', this.session.text('Cancel'));
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
    // Validate first focusable element
    // FIXME [dwi] maybe, this is not required if problem with single-button form is solved!
    scout.focusManager.validateFocus(this.session.uiSessionId);
  }.bind(this), 2500);
};

scout.BusyIndicator.prototype._postRender = function() {
  this.$container.installFocusContext(this.session, scout.FocusRule.AUTO);
};

scout.BusyIndicator.prototype._remove = function() {
  // Remove busy box (cancel timer in case it was not fired yet)
  clearTimeout(this._busyIndicatorTimeoutId);

  // Remove glasspane
  this._glassPaneRenderer.eachGlassPane(function($glassPane) {
    $glassPane
      .removeClass('busy')
      .setMouseCursorWait(false);
  });
  this._glassPaneRenderer.removeGlassPanes();
  this.$container.uninstallFocusContext(this.session);

  scout.BusyIndicator.parent.prototype._remove.call(this);
};

scout.BusyIndicator.prototype._position = function() {
  this.$container.cssMarginLeft(-this.$container.outerWidth() / 2);
};

