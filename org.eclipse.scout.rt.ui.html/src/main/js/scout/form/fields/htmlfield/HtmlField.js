scout.HtmlField = function() {
  scout.HtmlField.parent.call(this);
  this._appLinkKeyStroke = new scout.AppLinkKeyStroke(this, this._onAppLinkAction);
};
scout.inherits(scout.HtmlField, scout.ValueField);

scout.HtmlField.prototype._render = function($parent) {

  this.addContainer($parent, 'html-field', new scout.HtmlFieldLayout(this));
  this.addLabel();

  this.addField($.makeDiv());
  this.addStatus();
};

scout.HtmlField.prototype._renderProperties = function() {
  scout.HtmlField.parent.prototype._renderProperties.call(this);

  this._renderScrollToPosition(this.scrollToPosition);
};

/**
 * @override
 */
scout.HtmlField.prototype._renderDisplayText = function() {
  if (!this.displayText) {
    this.$field.empty();
    return;
  }
  this.$field.html(this.displayText);
  this.$field.find('.app-link')
    .on('click', this._onAppLinkAction.bind(this))
    .attr('tabindex', '0');

  // this method replaces the content, also the scroll bars get lost
  this._renderScrollBarsEnabled(this.scrollBarsEnabled);

  this.invalidateLayoutTree();
};

scout.HtmlField.prototype.init = function(model, session) {
  scout.HtmlField.parent.prototype.init.call(this, model, session);
  this.keyStrokeAdapter.registerKeyStroke(this._appLinkKeyStroke);
};

scout.HtmlField.prototype._remove = function() {
  if (this.scrollBarsEnabled && this._$scrollables.length > 0) {
    this.session.detachHelper.removeScrollable(this.$field);
  }
  scout.HtmlField.parent.prototype._remove.call(this);
};

scout.HtmlField.prototype._renderScrollBarsEnabled = function(scrollBarsEnabled) {
  if (this.scrollBarsEnabled) {
    scout.scrollbars.install(this.$field);
    this.session.detachHelper.pushScrollable(this.$field);
  }
};

// Not called in _renderProperties() because this is not really a property (more like an event)
scout.HtmlField.prototype._renderScrollToEnd = function() {
  if (this.scrollBarsEnabled) {
    scout.scrollbars.scrollToBottom(this.$fieldContainer);
  }
};

scout.HtmlField.prototype._renderScrollToPosition = function(anchor) {
  if (this.scrollBarsEnabled && anchor && this.$field.find(anchor)) {
    var anchorElem = this.$field.find('#'.concat(anchor));
    if (anchorElem && anchorElem.length > 0) {
      scout.scrollbars.scrollTo(this.$fieldContainer, anchorElem);
    }
  }
};

scout.HtmlField.prototype._onAppLinkAction = function(event) {
  var $target = $(event.target);
  var ref = $target.data('ref');
  this._sendAppLinkAction(ref);
};

scout.HtmlField.prototype._sendAppLinkAction = function(ref) {
  this.session.send(this.id, 'appLinkAction', {
    ref: ref
  });
};
