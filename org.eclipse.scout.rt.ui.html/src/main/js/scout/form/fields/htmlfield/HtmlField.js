/*******************************************************************************
 * Copyright (c) 2014-2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
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

  this.addField($parent.makeDiv());
  this.addStatus();
};

scout.HtmlField.prototype._renderProperties = function() {
  scout.HtmlField.parent.prototype._renderProperties.call(this);

  this._renderScrollBarEnabled();
  this._renderScrollToAnchor(this.scrollToAnchor);
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

  // Add action and focus behavior to app-links
  this.$field.find('.app-link')
    .on('click', this._onAppLinkAction.bind(this))
    .attr('tabindex', '0')
    .unfocusable();

  // Add listener to images to update the layout when the images are loaded
  this.$field.find('img')
    .on('load', this._onImageLoad.bind(this))
    .on('error', this._onImageError.bind(this));

  // Because this method replaces the content, the scroll bars might have to be added or removed
  if (this.rendered) { // (only necessary if already rendered, otherwise it is done by renderProperties)
    this._renderScrollBarEnabled();
  }

  this.invalidateLayoutTree();
};

scout.HtmlField.prototype._remove = function() {
  scout.scrollbars.uninstall(this.$field, this.session);
  scout.HtmlField.parent.prototype._remove.call(this);
};

scout.HtmlField.prototype._renderScrollBarEnabled = function() {
  if (this.scrollBarEnabled) {
    scout.scrollbars.install(this.$field, {
      parent: this
    });
  } else {
    scout.scrollbars.uninstall(this.$field, this.session);
  }
};

// Not called in _renderProperties() because this is not really a property (more like an event)
scout.HtmlField.prototype._renderScrollToEnd = function() {
  if (this.scrollBarEnabled) {
    scout.scrollbars.scrollToBottom(this.$fieldContainer);
  }
};

scout.HtmlField.prototype._renderScrollToAnchor = function(anchor) {
  if (this.scrollBarEnabled && anchor && this.$field.find(anchor)) {
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

scout.HtmlField.prototype._onImageLoad = function(event) {
  this.invalidateLayoutTree();
};

scout.HtmlField.prototype._onImageError = function(event) {
  this.invalidateLayoutTree();
};
