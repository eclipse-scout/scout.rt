scout.DesktopNavigation = function(desktop, $parent) {
  this.desktop = desktop;
  this.session = desktop.session;
  this.outline = desktop.outline;

  this._$desktopTree = $parent.appendDiv('DesktopTree');
  this.$div = this._$desktopTree;

  this.menu = new scout.DesktopMenu(desktop, this.$div);
};

scout.DesktopNavigation.prototype.renderOutline = function() {
  this.outline.render(this._$desktopTree);
  this._addVerticalSplitter(this._$desktopTree);
};

scout.DesktopNavigation.prototype.onSearchPerformed = function(event) {
  this.menu.onSearchPerformed(event);
};

scout.DesktopNavigation.prototype.onOutlineChanged = function(outline) {
  this.outline.remove();
  this.outline = outline;
  this.menu.onOutlineChanged(outline);
  this.outline.render(this._$desktopTree);
  if (this._$splitter) {
    this._$splitter.appendTo(this._$desktopTree); //move after tree, otherwise tree overlays splitter after outline change
  }
};

scout.DesktopNavigation.prototype._addVerticalSplitter = function($div) {
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
        that.outline.doBreadCrumb(true);
      } else {
        that.outline.doBreadCrumb(false);
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

