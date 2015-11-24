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
scout.NavigateUpMenu = function(options) {
  scout.NavigateUpMenu.parent.call(this);
  this._defaultIconId = scout.icons.ANGLE_UP;
  this._defaultText = 'ui.Up';
  this._additionalCssClass = 'small-gap';
  this.iconId = this._defaultIconId;
  this.keyStroke = 'backspace';
};
scout.inherits(scout.NavigateUpMenu, scout.AbstractNavigateMenu);

scout.NavigateUpMenu.prototype._render = function($parent) {
  scout.NavigateUpMenu.parent.prototype._render.call(this, $parent);
  this.$container.addClass('up');
};

scout.NavigateUpMenu.prototype._isDetail = function() {
  // Button is in "detail mode" if there are both detail form and detail table visible and detail form _is_ hidden.
  return !!(this.node.detailFormVisible && this.node.detailForm &&
    this.node.detailTableVisible && this.node.detailTable && !this.node.detailFormVisibleByUi);
};

scout.NavigateUpMenu.prototype._toggleDetail = function() {
  return true;
};

/**
 * Returns true when current node has either a parentNode or if current node is a
 * top-level node without a parent and the outline has a default detail-form.
 */
scout.NavigateUpMenu.prototype._buttonEnabled = function() {
  var parentNode = this.node.parentNode;
  return !!parentNode || !!this.outline.defaultDetailForm || !!this.outline.outlineOverview;
};

scout.NavigateUpMenu.prototype._drill = function() {
  var parentNode = this.node.parentNode;
  if (parentNode) {
    $.log.debug('drill up to node ' + parentNode);
    this.outline.navigateUpInProgress = true;
    this.outline.selectNodes(parentNode);
    this.outline.handleOutlineContent(true);
    this.outline.collapseNode(parentNode, {
      collapseChildNodes: true
    });
  } else {
    $.log.debug('drill up to top');
    this.outline.navigateToTop();
  }
};
