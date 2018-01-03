/*******************************************************************************
 * Copyright (c) 2014-2017 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
scout.Group = function() {
  scout.Group.parent.call(this);
  this.bodyAnimating = false;
  this.collapsed = false;
  this.title = null;
  this.headerVisible = true;
  this.body = null;

  this.$container = null;
  this.$header = null;
  this.$title = null;
  this.$collapseIcon = null;
  this.htmlComp = null;
  this.htmlHeader = null;
  this.htmlBody = null;
  this._addWidgetProperties(['body']);
};
scout.inherits(scout.Group, scout.Widget);

scout.Group.prototype._init = function(model) {
  scout.Group.parent.prototype._init.call(this, model);
  this._setBody(this.body);
};

scout.Group.prototype._render = function() {
  this.$container = this.$parent.appendDiv('group');
  this.htmlComp = scout.HtmlComponent.install(this.$container, this.session);
  this.htmlComp.setLayout(new scout.GroupLayout(this));

  this.$header = this.$container.prependDiv('group-header')
    .on('click', this._onHeaderClick.bind(this));
  this.htmlHeader = scout.HtmlComponent.install(this.$header, this.session);
  this.$title = this.$header.appendDiv('group-title');
  this.$titleSuffix = this.$header.appendDiv('group-title-suffix');
  this.$collapseIcon = this.$header.appendDiv('group-collapse-icon');

  scout.tooltips.installForEllipsis(this.$title, {
    parent: this
  });
};

scout.Group.prototype._renderProperties = function() {
  scout.Group.parent.prototype._renderProperties.call(this);
  this._renderTitle();
  this._renderTitleSuffix();
  this._renderHeaderVisible();
  this._renderCollapsed();
};

scout.Group.prototype._remove = function() {
  this.$header = null;
  this.$title = null;
  this.$titleSuffix = null;
  this.$collapseIcon = null;
  scout.Group.parent.prototype._remove.call(this);
};

scout.Group.prototype.setTitle = function(title) {
  this.setProperty('title', title);
};

scout.Group.prototype._renderTitle = function() {
  this.$title.textOrNbsp(this.title);
};

scout.Group.prototype.setTitleSuffix = function(titleSuffix) {
  this.setProperty('titleSuffix', titleSuffix);
};

scout.Group.prototype._renderTitleSuffix = function() {
  this.$titleSuffix.textOrNbsp(this.titleSuffix);
};

scout.Group.prototype.setHeaderVisible = function(headerVisible) {
  this.setProperty('headerVisible', headerVisible);
};

scout.Group.prototype._renderHeaderVisible = function() {
  this.$header.setVisible(this.headerVisible);
  this.invalidateLayoutTree();
};

scout.Group.prototype.setBody = function(body) {
  this.setProperty('body', body);
};

scout.Group.prototype._setBody = function(body) {
  if (!body) {
    // Create empty body if no body was provided
    body = scout.create('Widget', {
      parent: this,
      _render: function() {
        this.$container = this.$parent.appendDiv('group');
        this.htmlComp = scout.HtmlComponent.install(this.$container, this.session);
      }
    });
  }
  this._setProperty('body', body);
};

scout.Group.prototype._renderBody = function() {
  this.body.render();
  this.body.$container.addClass('group-body');
};

scout.Group.prototype.toggleCollapse = function() {
  this.setCollapsed(!this.collapsed);
};

scout.Group.prototype.setCollapsed = function(collapsed) {
  this.setProperty('collapsed', collapsed);
};

scout.Group.prototype._renderCollapsed = function() {
  this.$container.toggleClass('collapsed', this.collapsed);
  this.$collapseIcon.toggleClass('collapsed', this.collapsed);
  if (!this.collapsed && !this.bodyAnimating) {
    this._renderBody();
  }
  if (this.rendered) {
    this.animateToggleCollapse().done(function() {
      if (this.bodyAnimating) {
        // Another animation has been started in the mean time -> ignore done event
        return;
      }
      if (this.collapsed) {
        this.body.remove();
      }
      this.invalidateLayoutTree();
    }.bind(this));
  } else if (this.collapsed) {
    // Body will be removed after the animation, if there is no animation, remove it now
    this.body.remove();
  }
};

scout.Group.prototype._onHeaderClick = function(event) {
  this.setCollapsed(!this.collapsed);
};

/**
 * @returns {Promise}
 */
scout.Group.prototype.animateToggleCollapse = function(options) {
  var currentHeight = this.body.$container.cssHeight();
  var targetHeight = this.collapsed ? 0 : this.body.htmlComp.prefSize().height;
  var currentMargins, targetMargins;
  if (this.collapsed) {
    currentMargins = scout.graphics.margins(this.body.$container);
    targetMargins = new scout.Insets();
  } else {
    currentMargins = new scout.Insets();
    targetMargins = scout.graphics.margins(this.body.$container);
  }

  if (targetHeight === currentHeight && currentMargins.top === targetMargins.top && currentMargins.bottom === targetMargins.bottom) {
    // nothing to do
    return $.resolvedPromise();
  }
  this.bodyAnimating = true;
  if (this.collapsed) {
    this.$container.addClass('collapsing');
  }
  return this.body.$container
    .stop(true)
    .cssHeight(currentHeight)
    .cssMarginTop(currentMargins.top)
    .cssMarginBottom(currentMargins.bottom)
    .animate({
      height: targetHeight,
      marginTop: targetMargins.top,
      marginBottom: targetMargins.bottom
    }, {
      duration: 350,
      progress: function() {
        this.trigger('bodyHeightChange');
        this.revalidateLayoutTree();
      }.bind(this),
      complete: function() {
        this.bodyAnimating = false;
        if (this.body.rendered) {
          this.body.$container.cssMarginTop('');
          this.body.$container.cssMarginBottom('');
        }
        if (this.rendered) {
          this.$container.removeClass('collapsing');
        }
        this.trigger('bodyHeightChange');
      }.bind(this)
    })
    .promise();
};
