Scout.DesktopTreeContainer = function(scout, $parent, model) {
  this.scout = scout;
  this._$desktopTree = $parent.appendDiv('DesktopTree');
  this.$div = this._$desktopTree;
  this.desktopTree = new Scout.DesktopTree(this.scout, this._$desktopTree, model);

  this._$desktopTree.appendDiv('DesktopTreeResize')
    .on('mousedown', '', resizeTree);

  var that = this;

  function resizeTree(event) {
    $('body').addClass('col-resize')
      .on('mousemove', '', resizeMove)
      .one('mouseup', '', resizeEnd);

    function resizeMove(event) {
      var w = event.pageX + 11;
      that._$desktopTree.width(w);
      that._$desktopTree.next().width('calc(100% - ' + (w + 80) + 'px)')
        .css('left', w);
    }

    function resizeEnd(event) {
      $('body').off('mousemove')
        .removeClass('col-resize');
    }
    return false;
  }
};

Scout.DesktopTreeContainer.prototype.onOutlineCreated = function(event) {
  if (this.desktopTree) {
    this.desktopTree.detach();
  }
  this.desktopTree = new Scout.DesktopTree(this.scout, this._$desktopTree, event);
  this.desktopTree.attachModel();
};

Scout.DesktopTreeContainer.prototype.onOutlineChanged = function(outlineId) {
  this.desktopTree.detach();
  this.desktopTree = this.scout.widgetMap[outlineId];
  this.desktopTree.attach(this._$desktopTree);
  this.desktopTree.attachModel();
};

Scout.DesktopTreeContainer.prototype.attachModel = function() {
  //TODO attachModel only necessary because setNodeSelection relies on desktop bench which is created later, is there a better way?
  this.desktopTree.attachModel();
};
