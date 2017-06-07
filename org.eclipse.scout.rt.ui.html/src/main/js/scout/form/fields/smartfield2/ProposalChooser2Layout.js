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

/**
 * @param {scout.ProposalChooser2} proposalChooser (available by property this.popup)
 */
scout.ProposalChooser2Layout = function(proposalChooser) {
  scout.ProposalChooser2Layout.parent.call(this, proposalChooser);

  this._typeHandler = this._createTypeHandler(proposalChooser);
  this.animating = false;
};
scout.inherits(scout.ProposalChooser2Layout, scout.PopupLayout);

/**
 * This factory creates type handlers for the various proposal types. By default we support Table and Tree.
 * If one must support other types, this factory must be extended.
 */
scout.ProposalChooser2Layout.TYPE_HANDLER = {

  TABLE: {
    _table: null,
    _fillerWidth: null,
    cssSelector: '.table',
    prepare: function($container, layout) {
      this._table = layout.popup.model;
    },
    /**
     * Clears the given CSS property and stores the old value as data with prefix 'backup'
     * which is used to restore the CSS property later.
     */
    cssBackup: function($element, property) {
      var oldValue = $element.css(property);
      $element
        .css(property, '')
        .data('backup' + property, oldValue);
    },
    cssRestore: function($element, property) {
      var dataProperty = 'backup' + property,
        oldValue = $element.data(dataProperty);
      $element
        .css(property, oldValue)
        .removeData(dataProperty);
    },
    /**
     * Go through all rows and cells and call the given modifyFunc (backup/restore) on each element.
     */
    modifyTableData: function(modifyFunc) {
      var that = this;
      this._table.$rows().each(function() {
        var $row = $(this);
        modifyFunc($row, 'width');
        that._table.$cellsForRow($row).each(function() {
          var $cell = $(this);
          modifyFunc($cell, 'min-width');
          modifyFunc($cell, 'max-width');
        });
      });
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

      this.modifyFiller(this._table.$fillBefore);
      this.modifyFiller(this._table.$fillAfter);
      this.modifyTableData(this.cssBackup);
    },

    modifyFiller: function($filler) {
      if ($filler) {
        this._fillerWidth = $filler.css('width');
        $filler.css('width', '');
      }
    },

    restoreDom: function($container) {
      this._table.$container
        .css('display', 'block')
        .css('width', '100%')
        .css('height', '100%');
      this._table.$data
        .css('display', 'block');

      this.restoreFiller(this._table.$fillBefore);
      this.restoreFiller(this._table.$fillAfter);
      this.modifyTableData(this.cssRestore);
    },

    restoreFiller: function($filler) {
      if ($filler) {
        $filler.css('width', this._fillerWidth);
      }
    }
  },

  TREE: {
    _tree: null,
    cssSelector: '.tree',
    prepare: function($container, layout) {
      this._tree = layout.popup.model;
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

scout.ProposalChooser2Layout.prototype._createTypeHandler = function(proposalChooser) {
  var typeId = proposalChooser.model.objectType.toUpperCase(),
    typeHandler = scout.ProposalChooser2Layout.TYPE_HANDLER[typeId];
  if (!typeHandler) {
    throw new Error('No type handler defined for type=' + typeId);
  }
  return typeHandler;
};

scout.ProposalChooser2Layout.prototype.layout = function($container) {
  // skip layout while CSS animation is running
  if (this.animating) {
    return;
  }

  scout.ProposalChooser2Layout.parent.prototype.layout.call(this, $container);

  var popupHtmlComp =  scout.HtmlComponent.get($container),
    popupSize = popupHtmlComp.getSize().subtract(popupHtmlComp.getInsets());

  var
    modelHtmlComp = scout.HtmlComponent.get($container.children(this._typeHandler.cssSelector)),
    modelSize = popupSize.subtract(modelHtmlComp.getInsets()),
    $status = this.popup.$status,
    hasStatus = $status && $status.isVisible(),
    filter = this.popup.activeFilterGroup,
    filterPrefSize;

  if (hasStatus) {
    modelSize.height -= scout.graphics.getSize($status).height;
  }

  if (filter) {
    filterPrefSize = filter.htmlComp.getPreferredSize();
    modelSize.height -= filterPrefSize.height;
  }

  // when status or active-filter is available we must explicitly set the
  // height of the model (table or tree) in pixel. Otherwise we'd rely on
  // the CSS height which is set to 100%.
  if (hasStatus || filter) {
    modelHtmlComp.pixelBasedSizing = true;
  }

  modelHtmlComp.setSize(modelSize);

  if (filter) {
    filter.htmlComp.setSize(new scout.Dimension(modelSize.width, filterPrefSize.height));
  }

  if (popupHtmlComp.layouted) {
    // Reposition because opening direction may have to be switched if popup gets bigger
    // Don't do it the first time (will be done by popup.open), only if the popup is already open and gets layouted again
    this.popup.position();
  } else {
    // The first time it gets layouted, add CSS class to be able to animate
    this.animating = true;
    popupHtmlComp.$comp.oneAnimationEnd(function() {
      this.animating = false;
    }.bind(this));
    popupHtmlComp.$comp.addClassForAnimation('animate-open');
  }
};

/**
 * This preferred size implementation modifies the DIV where the table/tree is rendered
 * in a way the DIV does not limit the size of the table/tree. Thus we can read the preferred
 * size of the table/tree. After that the original width and height is restored.
 */
scout.ProposalChooser2Layout.prototype.preferredLayoutSize = function($container) {
  var oldDisplay, prefSize, modelSize, statusSize, filterPrefSize,
    pcWidth, pcHeight,
    popupHtmlComp = this.popup.htmlComp,
    $status = this.popup.$status,
    filter = this.popup.activeFilterGroup,
    fieldBounds = scout.graphics.offsetBounds(this.popup.smartField.$field),
    detachHelper = this.popup.session.detachHelper,
    $parent = $container.parent();

  this._typeHandler.prepare($container, this);
  modelSize = this.popup.model.htmlComp.getPreferredSize();
  prefSize = modelSize;
  detachHelper._storeScrollPositions($container);

  // pref size of table and tree don't return accurate values for width -> measure width
  pcWidth = $container.css('width');
  pcHeight = $container.css('height');

  $container.detach();
  this._typeHandler.modifyDom($container);
  $container
    .css('display', 'inline-block')
    .css('width', 'auto')
    .css('height', 'auto');
  $parent.append($container);
  modelSize.width = scout.graphics.prefSize($container, {
    restoreScrollPositions: false
  }).width;

  $container.detach();
  this._typeHandler.restoreDom($container);
  $container
    .css('display', 'block')
    .css('width', pcWidth)
    .css('height', pcHeight);
  $parent.append($container);
  detachHelper._restoreScrollPositions($container);

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
    filterPrefSize = filter.htmlComp.getPreferredSize();
    prefSize = new scout.Dimension(Math.max(prefSize.width, filterPrefSize.width), prefSize.height + filterPrefSize.height);
  }

  $container.toggleClass('empty', modelSize.height === 0);
  prefSize = prefSize.add(popupHtmlComp.getInsets());
  prefSize.width = Math.max(fieldBounds.width, prefSize.width);
  prefSize.height = Math.max(15, Math.min(350, prefSize.height)); // at least some pixels height in case there is no data, no status, no active filter

  if (prefSize.width > this._maxWindowSize()) {
    prefSize.width = this._maxWindowSize();
  }

  return prefSize;
};

scout.ProposalChooser2Layout.prototype._maxWindowSize = function() {
  return this.popup.$container.window().width() - (2 * this.popup.windowPaddingX);
};
