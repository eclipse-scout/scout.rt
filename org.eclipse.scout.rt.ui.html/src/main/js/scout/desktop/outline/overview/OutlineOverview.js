/*******************************************************************************
 * Copyright (c) 2014-2018 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
scout.OutlineOverview = function() {
  scout.OutlineOverview.parent.call(this);
  this.outline = null;
};
scout.inherits(scout.OutlineOverview, scout.Widget);

scout.OutlineOverview.prototype._init = function(model) {
  scout.OutlineOverview.parent.prototype._init.call(this, model);
  if (!this.outline && this.parent instanceof scout.Outline) {
    this.outline = this.parent;
  }
  scout.assertProperty(this, 'outline', scout.Outline);
};

scout.OutlineOverview.prototype._render = function() {
  this.$container = this.$parent.appendDiv('outline-overview');
  this.htmlComp = scout.HtmlComponent.install(this.$container, this.session);
  this.$content = this.$container.appendDiv('outline-overview-content');
  this.$content.appendDiv('outline-overview-icon').icon(this.outline.iconId);
  this.$content.appendDiv('outline-overview-title').text(this.outline.title);
};

