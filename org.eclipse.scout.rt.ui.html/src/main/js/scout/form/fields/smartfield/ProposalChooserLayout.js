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
      _$table: null,
      _$tableData: null,
      cssSelector: '.table',
      modifyDom: function($container) {
        this._$table = $container.find('.table')
          .css('display', 'inline-block')
          .css('width', 'auto')
          .css('height', 'auto');
        this._$tableData = this._$table.children('.table-data')
          .css('display', 'inline-block');
      },
      restoreDom: function($container) {
        this._$table
          .css('display', 'block')
          .css('width', '100%')
          .css('height', '100%');
       this._$tableData
          .css('display', 'block');
      }
    },

    TREE: {
      _$tree: null,
      _$treeData: null,
      cssSelector: '.tree',
      modifyDom: function($container) {
        this._$tree = $container.find('.tree')
          .css('display', 'inline-block')
          .css('width', 'auto')
          .css('height', 'auto');
        this._$treeData = this._$tree.children('.tree-data')
          .css('display', 'inline-block');
      },
      restoreDom: function($container) {
        this._$tree
          .css('display', 'block')
          .css('width', '100%')
          .css('height', '100%');
        this._$treeData
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
  var htmlContainer = scout.HtmlComponent.get($container),
    htmlModel = scout.HtmlComponent.get($container.children(this._typeHandler.cssSelector)),
    size = htmlContainer.getSize().subtract(htmlContainer.getInsets()),
    $status = $container.children('.status:visible'),
    $activeFilter = $container.children('.active-filter:visible');

  if ($status.length) {
    size.height -= scout.graphics.getSize($status).height;
  }
  if ($activeFilter.length) {
    size.height -= scout.graphics.getSize($activeFilter).height;
  }

  // when status or active-filter is available we must explicitly set the
  // height of the model (table or tree) in pixel. Otherwise we'd rely on
  // the CSS height which is set to 100%.
  if ($status.length || $activeFilter.length) {
    htmlModel.pixelBasedSizing = true;
  }

  htmlModel.setSize(size);
};

/**
 * This preferred size implementation creates a temporary/hidden DIV on which the table/tree is rendered
 * Then the size of this DIV is read. Thus this reads the effective size of the component on the screen
 * and doesn't try to find the preferred size by algorithm.
 */
scout.ProposalChooserLayout.prototype.preferredLayoutSize = function($container) {
  var $oldParent = $container.parent();

  // modify
  this._typeHandler.modifyDom($container);
  $container
    .css('display', 'inline-block')
    .css('height', 'auto');

  $container.detach();
  var $measurementDiv = $.makeDiv('measurement')
      .append($container)
      .appendTo(this._proposalChooser.session.$entryPoint);
  var prefSize = scout.graphics.getVisibleSize($measurementDiv);

  $container.detach();
  $measurementDiv.remove();
  $oldParent.append($container);

  // restore
  this._typeHandler.restoreDom($container);
  $container.css('display', 'block');
  return prefSize;
};

