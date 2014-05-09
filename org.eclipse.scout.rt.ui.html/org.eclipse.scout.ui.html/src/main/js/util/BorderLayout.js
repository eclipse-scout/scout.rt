scout.BorderLayout = function(marginTop, marginRight) {
  this.marginTop=marginTop;
  this.marginRight=marginRight;
};

scout.BorderLayout.prototype.position = function($element, position, splitterVertical) {
  $element.css('position', 'absolute');
  if (position === 'west' || position === 'center' || position === 'east') {
    $element.attr('data-row', 1);

    if (splitterVertical) {
      this._addSplitterVertical($element);
    }

    $element.css('top', this.marginTop);

    if (this.marginTop > 0) {
      $element.height('calc(100% - ' + this.marginTop + 'px)');
    } else {
      $element.height('100%');
    }
  }

  if (position === 'center') {
    var siblingWidth = 0,
      leftWidth = 0,
      rightWidth = 0;
    $element.prevAll('[data-row="1"]').each(function() {
      leftWidth += $(this).outerWidth();
    });
    $element.nextAll('[data-row="1"]').each(function() {
      rightWidth += $(this).outerWidth();
    });

    $element.css('left', leftWidth);

    siblingWidth = leftWidth + rightWidth + this.marginRight;
    if (siblingWidth > 0) {
      $element.width('calc(100% - ' + siblingWidth + 'px)');
    } else {
      $element.width('100%');
    }
  }

  if (position === 'north') {
    $element.data('row', 0);
    this.$top = $element;
  }

  $element.addClass('layout-' + position);
};


scout.BorderLayout.prototype._addSplitterVertical = function($div) {
  $div.appendDiv(undefined, 'splitter-vertical')
    .on('mousedown', '', resize);

  function resize() {
    $('body').addClass('col-resize')
      .on('mousemove', '', resizeMove)
      .one('mouseup', '', resizeEnd);

    function resizeMove(event) {
      var w = event.pageX + 11;
      $div.width(w);
      $div.next().width('calc(100% - ' + (w + 80) + 'px)')
        .css('left', w);
    }

    function resizeEnd() {
      $('body').off('mousemove')
        .removeClass('col-resize');
    }
    return false;
  }
};
