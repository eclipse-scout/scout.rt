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
  var text = 'Zurück'; //FIXME CGU translation
  var $parentNode;
  var parentNode = this.selectedNode.parentNode;

  if (parentNode) {
    $parentNode = this.outline._findNodeById(parentNode.id);
    text = 'Zurück zu ' + $parentNode.text(); //FIXME CGU if the text of the parent gets changed this needs to change as well
  }

  this.$container = $parent
    .appendDIV('menu-item')
    .on('click', '', onClicked.bind(this))
    .text(text);

  if (!parentNode) {
    this.$container.setEnabled(false);
  }

  function onClicked(event) {
    if (!this.$container.isEnabled()) {
      return;
    }

    if (!parentNode) {
      throw "Already on top";
    }

    this.outline.setNodesSelected([parentNode], [$parentNode]);
  }
};

scout.OutlineNavigateUpMenu.prototype.remove = function($parent) {
  this.$container.remove();
};
