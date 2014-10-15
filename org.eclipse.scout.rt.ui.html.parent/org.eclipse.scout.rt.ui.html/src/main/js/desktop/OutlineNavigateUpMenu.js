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
  var text,
    $parentNode,
    parentNode = this.selectedNode.parentNode;
  if (parentNode) {
    $parentNode = this.outline._findNodeById(parentNode.id);
    text = scout.texts.get('back');
  } else {
    text = scout.texts.get('home');
  }

  // FIXME AWE: (menu) discuss with C.GU - das ist copy/paste code von Menu.js ... zusammenführen
  // Wäre es nicht sinnvoll, wenn es diese Menüs schon im Modell gäbe?
  this.$container = $('<button>').
    appendTo($parent).
    addClass('menu-button').addClass('system').
    on('click', '', onClicked.bind(this)).
    text(text);

//  this.$container = $parent
//    .appendDIV('menu-item')
//    .on('click', '', onClicked.bind(this))
//    .text(text);

  function onClicked(event) {
    if (!this.$container.isEnabled()) {
      return;
    }

    if (parentNode) {
      this.outline.setNodesSelected([parentNode], [$parentNode]);
    }
    else {
      this.outline.clearSelection();
      this.outline.collapseAll();
    }
  }
};

scout.OutlineNavigateUpMenu.prototype.remove = function($parent) {
  this.$container.remove();
};
