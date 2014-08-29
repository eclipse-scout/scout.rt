/**
 * Uses the same model as a real menu (childNodes, menuTypes etc.) and the same signature as well (render, remove) to make it compatible with the menubar.
 */
scout.OutlineNavigateUpMenu = function(outline, selectedNode) {
  this.outline = outline;
  this.selectedNode = selectedNode;
  this.childMenus = [];
  this.menuTypes = ['Table.EmptySpace', 'Form.System'];
  this.visible = true;
};

scout.OutlineNavigateUpMenu.prototype.render = function($parent) {
  var text = 'Zurück';
  var $parentNode;
  var parentNode;

  this.$container = $parent
    .appendDIV('menu-item')
    .on('click', '', onClicked.bind(this))
    .text(text);

  if (!this.selectedNode) {
    this.$container.setEnabled(false);
  }
  else {
    parentNode = this.selectedNode.parentNode;
    if (parentNode) {
      $parentNode = this.outline._findNodeById(parentNode.id);
    }
  }

  function onClicked(event) {
    if (!this.$container.isEnabled()) {
      return;
    }

    if (!parentNode) {
      this.outline.clearSelection();
    }
    else {
      this.outline.setNodesSelected([parentNode], [$parentNode]);
    }
  }
};

scout.OutlineNavigateUpMenu.prototype.remove = function($parent) {
  this.$container.remove();
};
