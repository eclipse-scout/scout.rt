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
scout.ProposalChooserLayout = function(proposalChooser) {
  scout.ProposalChooserLayout.parent.call(this);
  this._proposalChooser = proposalChooser;
  this._typeHandler = this._createTypeHandler(proposalChooser);
};
scout.inherits(scout.ProposalChooserLayout, scout.AbstractLayout);

/**
 * This factory creates type handlers for the various proposal types. By default we support Table and Tree.
 * If one must support other types, this factory must be extended.
 */
scout.ProposalChooserLayout.TYPE_HANDLER = {

  TABLE: {
    _table: null,
    cssSelector: '.table',
    prepare: function($container, layout) {
      this._table = layout._proposalChooser.model;
    },
    /**
     * Modifies the table in a way that the preferred width may be read.
     * Removes explicit widths on rows, cells, fillers and sets display to inline-block.
     */
    modifyDom: function($container) {
      this._table.$container
        .css('display', 'inline-block')
        .css('width', 'auto')
        .css('height', 'auto');
      this._table.$data
        .css('display', 'inline-block');
      if (this._table.$fillBefore) {
        this._table.$fillBefore.css('width', '');
      }
      if (this._table.$fillAfter) {
        this._table.$fillAfter.css('width', '');
      }
      var $rows = this._table.$rows();
      $rows.each(function(i, elem) {
        var $row = $(elem);
        $row.css('width', '');
        this._table.$cellsForRow($row)
          .css('min-width', '')
          .css('max-width', '');
      }.bind(this));
    },
    restoreDom: function($container) {
      this._table.$container
        .css('display', 'block')
        .css('width', '100%')
        .css('height', '100%');
      this._table.$data
        .css('display', 'block');
    }
  },

  TREE: {
    _tree: null,
    cssSelector: '.tree',
    prepare: function($container, layout) {
      this._tree = layout._proposalChooser.model;
      var $nodes = this._tree.$data
        .children('.tree-node')
        .removeClass('first last');
      $nodes.first()
        .addClass('first');
      $nodes.last()
        .addClass('last');
    },
    modifyDom: function($container) {
      this._tree.$container
        .css('display', 'inline-block')
        .css('width', 'auto')
        .css('height', 'auto');
      this._tree.$data
        .css('display', 'inline-block');
    },
    restoreDom: function($container) {
      this._tree.$container
        .css('display', 'block')
        .css('width', '100%')
        .css('height', '100%');
      this._tree.$data
        .css('display', 'block');
    }
  }
};

scout.ProposalChooserLayout.prototype._createTypeHandler = function(proposalChooser) {
  var typeId = proposalChooser.model.objectType.toUpperCase(),
    typeHandler = scout.ProposalChooserLayout.TYPE_HANDLER[typeId];
  if (!typeHandler) {
    throw new Error('No type handler defined for type=' + typeId);
  }
  return typeHandler;
};

scout.ProposalChooserLayout.prototype.layout = function($container) {
  var filterPrefSize,
    htmlContainer = scout.HtmlComponent.get($container),
    htmlComp = scout.HtmlComponent.get($container.children(this._typeHandler.cssSelector)),
    size = htmlContainer.getSize().subtract(htmlContainer.getInsets()),
    $status = this._proposalChooser.$status,
    hasStatus = $status && $status.isVisible(),
    filter = this._proposalChooser.activeFilterGroup;

  if (hasStatus) {
    size.height -= scout.graphics.getSize($status).height;
  }
  if (filter) {
    filterPrefSize = filter.htmlComp.getPreferredSize();
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
 * This preferred size implementation creates a temporary/hidden DIV on which the table/tree is rendered
 * Then the size of this DIV is read. Thus this reads the effective size of the component on the screen
 * and doesn't try to find the preferred size by algorithm.
 */
scout.ProposalChooserLayout.prototype.preferredLayoutSize = function($container) {
  var oldDisplay, prefSize, modelSize, statusSize, activeFilterSize, filterPrefSize,
    htmlComp = this._proposalChooser.htmlComp,
    $status = this._proposalChooser.$status,
    filter = this._proposalChooser.activeFilterGroup;

  this._typeHandler.prepare($container, this);

  modelSize = this._proposalChooser.model.htmlComp.getPreferredSize();
  prefSize = modelSize;

  // pref size of table and tree don't return accurate values for width -> measure width
  this._typeHandler.modifyDom($container);
  $container
    .css('display', 'inline-block')
    .css('width', 'auto')
    .css('height', 'auto');
  modelSize.width = scout.graphics.prefSize($container).width;
  this._typeHandler.restoreDom($container);
  $container.css('display', 'block');

  if ($status && $status.isVisible()) {
    oldDisplay = $status.css('display');
    $status.css('display', 'inline-block');
    statusSize = scout.graphics.prefSize($status, true, true);
    $status.css('display', oldDisplay);
    prefSize = new scout.Dimension(Math.max(prefSize.width, statusSize.width), prefSize.height + statusSize.height);
  }

  if (filter) {
    filterPrefSize = filter.htmlComp.getPreferredSize();
    prefSize = new scout.Dimension(Math.max(prefSize.width, filterPrefSize.width), prefSize.height + filterPrefSize.height);
  }

  $container.toggleClass('empty', modelSize.height === 0);
  return prefSize.add(htmlComp.getInsets());
};
