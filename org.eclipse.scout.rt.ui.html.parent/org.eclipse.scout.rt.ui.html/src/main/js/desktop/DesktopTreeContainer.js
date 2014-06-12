scout.DesktopTreeContainer = function($parent, model, session) {
  this.session = session;
  this.desktopTree = session.getOrCreateModelAdapter(model, this);

  this._$desktopTree = $parent.appendDiv('DesktopTree');
  this.$div = this._$desktopTree;
};

scout.DesktopTreeContainer.prototype.renderTree = function() {
  this.desktopTree.render(this._$desktopTree);
  this._addVerticalSplitter(this._$desktopTree);
};

scout.DesktopTreeContainer.prototype.onOutlineChanged = function(outline) {
  this.desktopTree.remove(); //FIXME CGU refactor to ModelAdapter.updateModelAdapterAndRender, but DesktopTreecontainer does not extend ModelAdapter
  this.desktopTree = this.session.getOrCreateModelAdapter(outline, this); //FIXME CGU actually this.desktop
  this.desktopTree.render(this._$desktopTree);
  if (this._$splitter) {
    this._$splitter.appendTo(this._$desktopTree); //move after tree, otherwise tree overlays splitter after outline change
  }
};

scout.DesktopTreeContainer.prototype._addVerticalSplitter = function($div) {
  this._$splitter = $div.appendDiv(undefined, 'splitter-vertical')
    .on('mousedown', '', resize);

  var that = this;

  function resize() {
    var w;

    $('body').addClass('col-resize')
      .on('mousemove', '', resizeMove)
      .one('mouseup', '', resizeEnd);

    function resizeMove(event) {
      w = event.pageX + 11;

      $div.width(w);
      $div.next().width('calc(100% - ' + (w + 80) + 'px)')
        .css('left', w);

      if (w <= 180) {
        that.desktopTree.doBreadCrumb(true);
      } else {
        that.desktopTree.doBreadCrumb(false);
      }

    }

    function resizeEnd() {
      $('body').off('mousemove')
        .removeClass('col-resize');

      if (w < 180) {
        w = 180;
        $div.animateAVCSD('width', w, null,
            function(i){$div.next().css('width', 'calc(100% - ' + (i + 80) + 'px)'); });
        $div.next().animateAVCSD('left', w);
      }
    }
    return false;
  }
};

