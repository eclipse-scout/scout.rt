scout.DesktopKeystrokeAdapter = function(viewButtonBar, taskbar, tree) {
  var that = this;

  this.$target = undefined; // set by KeystrokeManager
  this.controller = undefined; // set by KeystrokeManager
  this.handlers = [];
  this._viewButtonBar = viewButtonBar;
  this._tree = tree;
  this._taskbar = taskbar;

  //FIXME read keycodes from model
  if (taskbar) {
    $('.taskbar-item', taskbar.$div).each(function(i, element) {
      var keystroke = $(element).attr('data-shortcut');
      if (keystroke) {
        keystroke = keystroke.toUpperCase();
        var shortcut = parseInt(keystroke.replace('F', ''), 10) + 111;
        that.handlers.push({
          $element: $(element),
          accept: function(event) {
            if (event && event.which == shortcut && event.ctrlKey) {
              return true;
            }
            return false;
          },
          handle: function(event) {
            this.$element.click();

            return false;
          }
        });
      }
    });

  }

  //FIXME read keycodes from model
  //FIXME Keypad?
  if (viewButtonBar) {
    that.handlers.push({
      accept: function(event) {
        if (event && event.which >= 49 && event.which <= 57 && // 1-9
          !event.ctrlKey && !event.altKey && !event.metaKey) {
          return true;
        }
        return false;
      },
      handle: function(event) {
        var keycode = event.which;

        $('.view-item', viewButtonBar.$div).eq(keycode - 49).click();

        return false;
      }
    });
  }

  if (tree) {
    that.handlers.push({
      removeKeyBox: true,
      accept: function(event) {
        if (event && $.inArray(event.which, [37, 39, 107, 109]) >= 0 && // left, right, keypad_plus, keypad_minus
          !event.shiftKey && !event.ctrlKey && !event.altKey && !event.metaKey) {
          return true;
        }
        return false;
      },
      handle: function(event) {
        var keycode = event.which;
        var $currentNode = $('.selected', that._tree.$div);
        var $targetNode;

        // left: up in tree
        if (keycode == 37) {
          if ($currentNode.hasClass('expanded')) {
            $currentNode.children('.tree-item-control').click();
          } else {
            $targetNode = $currentNode.prevAll('.tree-item[data-level=' + (+$currentNode.attr('data-level') - 1) + ']');
            if ($targetNode.attr('id')) {
              // FIXME BSH "scroll into view"
              that._tree.desktopTree.setNodeSelectedById($targetNode.attr('id'));
            }
          }
        }
        // right: down in tree
        else if (keycode == 39) {
          if (!$currentNode.hasClass('expanded')) {
            $currentNode.children('.tree-item-control').click();
          } else {
            $targetNode = $currentNode.next('.tree-item[data-level=' + (+$currentNode.attr('data-level') + 1) + ']');
            if ($targetNode.attr('id')) {
              // FIXME BSH "scroll into view"
              that._tree.desktopTree.setNodeSelectedById($targetNode.attr('id'));
            }
          }
        }
        // +/-: open and close tree
        else if (keycode == 109 || keycode == 107) {
          if ((!$currentNode.hasClass('expanded') && keycode == 107) || ($currentNode.hasClass('expanded') && keycode == 109)) {
            $currentNode.children('.tree-item-control').click();
          }
        }

        return false;
      }
    });

    that.handlers.push({
      removeKeyBox: true,
      accept: function(event) {
        if (event && $.inArray(event.which, [38, 40]) >= 0 && // up, down
          event.ctrlKey) {
          return true;
        }
        return false;
      },
      handle: function(event) {
        var keycode = event.which;
        var $currentNode = $('.selected', that._tree.$div);
        var $targetNode;

        // ctrl-up: go up (same level)
        if (keycode == 38) {
          $targetNode = $currentNode.prevAll('.tree-item[data-level=' + $currentNode.attr('data-level') + ']').first();
          if ($targetNode.attr('id')) {
            // FIXME BSH "scroll into view"
            that._tree.desktopTree.setNodeSelectedById($targetNode.attr('id'));
          }
        }
        // ctrl-down: go down (same level)
        else if (keycode == 40) {
          $targetNode = $currentNode.nextAll('.tree-item[data-level=' + $currentNode.attr('data-level') + ']').first();
          if ($targetNode.attr('id')) {
            // FIXME BSH "scroll into view"
            that._tree.desktopTree.setNodeSelectedById($targetNode.attr('id'));
          }
        }

        return false;
      }
    });
  }
};

scout.DesktopKeystrokeAdapter.prototype.drawKeyBox = function() {
  if (this._taskbar) {
    $('.taskbar-item', this._taskbar.$div).each(function(i, e) {
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
      $prev = $node.prev('.tree-item'),
      $next = $node.next('.tree-item');

    if ($node.hasClass('can-expand')) {
      if ($node.hasClass('expanded')) {
        $node.appendDiv('', 'key-box large', '-');
      } else {
        $node.appendDiv('', 'key-box large', '+');
      }
      $node.children('.tree-item-control').css('display', 'none');
    }

    if ($prev.length) {
      $prev.appendDiv('', 'key-box', '←');
      $prev.children('.tree-item-control').css('display', 'none');
    }

    if ($next.length) {
      $next.appendDiv('', 'key-box', '→');
      $next.children('.tree-item-control').css('display', 'none');
    }
  }
};

scout.DesktopKeystrokeAdapter.prototype.removeKeyBox = function() {
  $('.tree-item-control').css('display', '');
};

scout.DesktopKeystrokeAdapter.prototype.accept = function(keycode) {

};
