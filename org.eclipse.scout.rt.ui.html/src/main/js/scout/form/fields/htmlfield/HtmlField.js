scout.HtmlField = function() {
  scout.HtmlField.parent.call(this);
};
scout.inherits(scout.HtmlField, scout.ValueField);

/**
 * @override FormField.js
 */
scout.HtmlField.prototype._initKeyStrokeContext = function(keyStrokeContext) {
  scout.HtmlField.parent.prototype._initKeyStrokeContext.call(this, keyStrokeContext);

  keyStrokeContext.registerKeyStroke(new scout.AppLinkKeyStroke(this, this._onAppLinkAction));
};

scout.HtmlField.prototype._render = function($parent) {
  this.addContainer($parent, 'html-field');
  this.addLabel();

  this.addField($.makeDiv());
  this.addStatus();
};

scout.HtmlField.prototype._renderProperties = function() {
  scout.HtmlField.parent.prototype._renderProperties.call(this);

  this._renderScrollBarsEnabled();
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
    .attr('tabindex', '0')
    .unfocusable();

  // this method replaces the content, the scroll bars get lost -> render again (only necessary if already rendered, otherwise it is done by renderProperties)
  if (this.rendered) {
    this._renderScrollBarsEnabled(this.scrollBarsEnabled);
  }

  this.invalidateLayoutTree();
};

scout.HtmlField.prototype._remove = function() {
  scout.scrollbars.uninstall(this.$field, this.session);
  scout.HtmlField.parent.prototype._remove.call(this);
};

scout.HtmlField.prototype._renderScrollBarsEnabled = function() {
  if (this.scrollBarsEnabled) {
    scout.scrollbars.install(this.$field, {
      parent: this
    });
  } else {
    scout.scrollbars.uninstall(this.$field, this.session);
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
  this._send('appLinkAction', {
    ref: ref
  });
};
