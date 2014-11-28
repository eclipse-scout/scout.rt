//FIXME read keycodes from model
scout.DesktopKeystrokeAdapter = function(navigation, taskbar) {
  var that = this;

  this.$target = undefined; // set by KeystrokeManager
  this.controller = undefined; // set by KeystrokeManager
  this.handlers = [];
  this._navigation = navigation;
  this._viewButtonBar = navigation.menu;
  this._taskbar = taskbar;

  if (this._taskbar) {
    $('.taskbar-item', this._taskbar.$container).each(function(i, element) {
      var keystroke = $(element).attr('data-shortcut');
      if (keystroke) {
        keystroke = keystroke.toUpperCase();
        var shortcut = parseInt(keystroke.replace('F', ''), 10) + 111;
        that.handlers.push({
          $element: $(element),
          accept: function(event) {
            if (event && event.which === shortcut && event.ctrlKey) {
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

  //FIXME Keypad?
  if (this._viewButtonBar) {
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

        $('.view-item', that._viewButtonBar.$div).eq(keycode - 49).click();

        return false;
      }
    });
  }

  if (this._navigation) {
    that.handlers.push({
      removeKeyBox: true,
      accept: function(event) {
        return event &&
          $.inArray(event.which, [scout.keys.UP, scout.keys.DOWN, scout.keys.LEFT, scout.keys.RIGHT]) >= 0 &&
          event.ctrlKey;
      },
      handle: function(event) {
        var $targetNode, targetNode,
          keycode = event.which,
          outline = that._navigation.outline,
          $currentNode = outline.$selectedNodes().eq(0),
          currentNode = $currentNode.data('node');

        if (keycode === scout.keys.UP) {
          if ($currentNode.length === 0) {
            $targetNode = outline.$nodes().last();
            targetNode = $targetNode.data('node');
          } else {
            $targetNode = $currentNode.prev('.tree-item');
            targetNode = $targetNode.data('node');
          }
          if (targetNode) {
            outline.setNodesSelected(targetNode, $targetNode);
          }
        } else if (keycode === scout.keys.DOWN) {
          if ($currentNode.length === 0) {
            $targetNode = outline.$nodes().first();
            targetNode = $targetNode.data('node');
          } else {
            $targetNode = $currentNode.next('.tree-item');
            targetNode = $targetNode.data('node');
          }
          if (targetNode) {
            outline.setNodesSelected(targetNode, $targetNode);
          }
        } else if (currentNode && keycode === scout.keys.LEFT) {
          if (currentNode.expanded) {
            outline.setNodeExpanded(currentNode, $currentNode, false);
          } else if (currentNode.parentNode) {
            outline.setNodesSelected(currentNode.parentNode);
          }
        } else if (currentNode && keycode === scout.keys.RIGHT) {
          if (!currentNode.expanded && !currentNode.leaf) {
            outline.setNodeExpanded(currentNode, $currentNode, true);
          } else if (currentNode.childNodes.length > 0) {
            outline.setNodesSelected(currentNode.childNodes[0]);
          }
        }

        return false;
      }
    });
  }
};

scout.DesktopKeystrokeAdapter.prototype.drawKeyBox = function() {
  if (this._taskbar) {
    $('.taskbar-item', this._taskbar.$container).each(function(i, e) {
      $(e).appendDiv('key-box', $(e).attr('data-shortcut'));
    });
  }

  if (this._viewButtonBar) {
    $('.view-item', this._viewButtonBar.$div).each(function(i, element) {
      if (i < 9) {
        $(element).appendDiv('key-box', i + 1);
      }
    });
  }

  if (this._navigation) {
    var $node = $('.selected', this._navigation.$div),
      $prev = $node.prev('.tree-item'),
      $next = $node.next('.tree-item');

    if (!$node.hasClass('leaf')) {
      if ($node.hasClass('expanded')) {
        $node.appendDiv('key-box large', '←');
      } else {
        $node.appendDiv('key-box large', '→');
      }
      $node.children('.tree-item-control').css('display', 'none');
    }

    if ($prev.length) {
      $prev.appendDiv('key-box', '↑');
      $prev.children('.tree-item-control').css('display', 'none');
    }

    if ($next.length) {
      $next.appendDiv('key-box', '↓');
      $next.children('.tree-item-control').css('display', 'none');
    }
  }
};

scout.DesktopKeystrokeAdapter.prototype.removeKeyBox = function() {
  $('.tree-item-control').css('display', '');
};

scout.DesktopKeystrokeAdapter.prototype.accept = function(keycode) {

};
