scout.DesktopKeystrokeAdapter = function(viewButtonBar, toolBox, tree) {
  this.handlers = [];
  this._viewButtonBar = viewButtonBar;
  this._tree = tree;
  this._toolBox = toolBox;

  var handler;

  var that = this;
  //FIXME read keycodes from model
  if (toolBox) {
    $('.tool-item', toolBox.$div).each(function(i, element) {
      var shortcut = parseInt($(element).attr('data-shortcut').replace('F', ''), 10) + 111;
      handler = {
        keycodes: shortcut,
        $element: $(element),
        handle: function() {
          this.$element.click();

          return false;
        }
      };
      that.handlers.push(handler);
    });

  }

  //FIXME read keycodes from model
  if (viewButtonBar) {
    handler = {
      keycodeRangeStart: 49,
      keycodeRangeEnd: 57,
      handle: function(keycode) {
        $('.view-item', viewButtonBar.$div).eq(keycode - 49).click();

        return false;
      }
    };
    that.handlers.push(handler);
  }

  if (tree) {
    handler = {
      keycodes: [37, 39, 107, 109],
      removeKeyBox: true,
      handle: function(keycode) {
        // left: up in tree
        if (keycode == 37) {
          $('.selected', that._tree.$div).prev().click();
        }
        // right: down in tree
        else if (keycode == 39) {
          $('.selected', that._tree.$div).next().click();
        }
        // +/-: open and close tree
        else if (keycode == 109 || keycode == 107) {
          $('.selected', that._tree.$div).children('.tree-item-control').click();
        }

        return false;
      }
    };
    this.handlers.push(handler);

  }
};

scout.DesktopKeystrokeAdapter.prototype.drawKeyBox = function() {
  if (this._toolBox) {
    $('.tool-item', this._toolBox.$div).each(function(i, e) {
      $(e).appendDiv('', 'key-box', $(e).attr('data-shortcut'));
    });
  }

  if (this._viewButtonBar) {
    $('.view-item', this._viewButtonBar.$div).each(function(i, element) {
      if (i < 9) $(element).appendDiv('', 'key-box', i + 1);
    });
  }

  if (this._tree) {
    var $node = $('.selected', this._tree.$div),
      $prev = $node.prev(),
      $next = $node.next();

    if ($node.hasClass('can-expand')) {
      if ($node.hasClass('expanded')) {
        $node.appendDiv('', 'key-box large', '-');
      } else {
        $node.appendDiv('', 'key-box large', '+');
      }
      $node.children('.tree-item-control').hide();
    }

    if ($prev.length) {
      $prev.appendDiv('', 'key-box', '←');
      $prev.children('.tree-item-control').hide();
    }

    if ($next.length) {
      $next.appendDiv('', 'key-box', '→');
      $next.children('.tree-item-control').hide();
    }
  }
};

scout.DesktopKeystrokeAdapter.prototype.removeKeyBox = function() {
  $('.tree-item-control').show();
};

scout.DesktopKeystrokeAdapter.prototype.accept = function(keycode) {

};
