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
scout.ProposalChooserLayout = function(proposalChooser) {
  scout.ProposalChooserLayout.parent.call(this);
  this._proposalChooser = proposalChooser;
  this._typeHandler = this._createTypeHandler(proposalChooser);
};
scout.inherits(scout.ProposalChooserLayout, scout.AbstractLayout);

/**
 * This factory creates type handlers for the various proposal types. By default we support Table and Tree.
 * If one must support other types, a LayoutResetter class must be implemented for that type.
 */
scout.ProposalChooserLayout.prototype._createTypeHandler = function(proposalChooser) {
  var handlerObjectType = proposalChooser.model.objectType + 'LayoutResetter';
  return scout.create(handlerObjectType, proposalChooser.model);
};

scout.ProposalChooserLayout.prototype.layout = function($container) {
  var filterPrefSize,
    htmlContainer = scout.HtmlComponent.get($container),
    htmlComp = scout.HtmlComponent.get($container.children(this._typeHandler.cssSelector)),
    size = htmlContainer.size().subtract(htmlContainer.insets()),
    $status = this._proposalChooser.$status,
    hasStatus = $status && $status.isVisible(),
    filter = this._proposalChooser.activeFilterGroup;

  if (hasStatus) {
    size.height -= scout.graphics.size($status).height;
  }
  if (filter) {
    filterPrefSize = filter.htmlComp.prefSize();
    size.height -= filterPrefSize.height;
  }

  // when status or active-filter is available we must explicitly set the
  // height of the model (table or tree) in pixel. Otherwise we'd rely on
  // the CSS height which is set to 100%.
  if (hasStatus || filter) {
    htmlComp.pixelBasedSizing = true;
  }

  htmlComp.setSize(size);

  if (filter) {
    filter.htmlComp.setSize(new scout.Dimension(size.width, filterPrefSize.height));
  }
};

/**
 * This preferred size implementation modifies the DIV where the table/tree is rendered
 * in a way the DIV does not limit the size of the table/tree. Thus we can read the preferred
 * size of the table/tree. After that the original width and height is restored.
 */
scout.ProposalChooserLayout.prototype.preferredLayoutSize = function($container) {
  var oldDisplay, prefSize, modelSize, statusSize, filterPrefSize,
    pcWidth, pcHeight,
    htmlComp = this._proposalChooser.htmlComp,
    $status = this._proposalChooser.$status,
    filter = this._proposalChooser.activeFilterGroup,
    $parent = $container.parent();

  modelSize = this._proposalChooser.model.htmlComp.prefSize();
  prefSize = modelSize;
  scout.scrollbars.storeScrollPositions($container, this._proposalChooser.session);

  // pref size of table and tree don't return accurate values for width -> measure width
  pcWidth = $container.css('width');
  pcHeight = $container.css('height');

  $container.detach();
  this._typeHandler.modifyDom();
  $container
    .css('display', 'inline-block')
    .css('width', 'auto')
    .css('height', 'auto');
  $parent.append($container);
  modelSize.width = scout.graphics.prefSize($container, {
    restoreScrollPositions: false
  }).width;

  $container.detach();
  this._typeHandler.restoreDom();
  $container
    .css('display', 'block')
    .css('width', pcWidth)
    .css('height', pcHeight);
  $parent.append($container);
  scout.scrollbars.restoreScrollPositions($container, this._proposalChooser.session);

  if ($status && $status.isVisible()) {
    oldDisplay = $status.css('display');
    $status.css('display', 'inline-block');
    statusSize = scout.graphics.prefSize($status, {
      includeMargin: true,
      useCssSize: true
    });
    $status.css('display', oldDisplay);
    prefSize = new scout.Dimension(Math.max(prefSize.width, statusSize.width), prefSize.height + statusSize.height);
  }

  if (filter) {
    filterPrefSize = filter.htmlComp.prefSize();
    prefSize = new scout.Dimension(Math.max(prefSize.width, filterPrefSize.width), prefSize.height + filterPrefSize.height);
  }

  $container.toggleClass('empty', modelSize.height === 0);
  return prefSize.add(htmlComp.insets());
};
