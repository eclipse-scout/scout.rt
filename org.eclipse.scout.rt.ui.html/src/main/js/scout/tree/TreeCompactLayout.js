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
scout.TreeCompactLayout = function(tree) {
  scout.TreeCompactLayout.parent.call(this);
  this.tree = tree;
};
scout.inherits(scout.TreeCompactLayout, scout.AbstractLayout);

scout.TreeCompactLayout.prototype.layout = function($container) {
  var $filter = this.tree.$filter,
    $nodesWrapper = this.tree.$nodesWrapper,
    height = 0;

  height += scout.graphics.getSize($filter).height;
  height += $nodesWrapper.cssMarginTop() + $nodesWrapper.cssMarginBottom();

  $nodesWrapper.css('height', 'calc(100% - ' + height + 'px)');
  scout.scrollbars.update($nodesWrapper);
};
