scout.TreeControlKeyStrokes = function(field) {
  scout.TreeControlKeyStrokes.parent.call(this);
  this.drawHint = true;
  this._field = field;
  this.initKeyStrokeParts();
};
scout.inherits(scout.TreeControlKeyStrokes, scout.KeyStroke);
/**
 * @Override scout.KeyStroke
 */
scout.TreeControlKeyStrokes.prototype.handle = function(event) {
  var $targetNode, targetNode,
    keycode = event.which,
    $currentNode = this._field.$selectedNodes().eq(0),
    currentNode = $currentNode.data('node');

  if (keycode === scout.keys.SPACE) {
    if ($currentNode.length > 0) {
      var check = !$($currentNode[0]).data('node').checked;
      for (var j = 0; j < $currentNode.length; j++) {
        var node = $($currentNode[j]).data('node');
        this._field.checkNodeAndRender(node, check);
      }
    }
  }
  if (keycode === scout.keys.UP) {
    if ($currentNode.length === 0) {
      $targetNode = this._field.$nodes().last();
      targetNode = $targetNode.data('node');
    } else {
      $targetNode = $currentNode.prev('.tree-item');
      targetNode = $targetNode.data('node');
    }
    if (targetNode) {
      this._field.setNodesSelected(targetNode);
    }
  } else if (keycode === scout.keys.DOWN) {
    if ($currentNode.length === 0) {
      $targetNode = this._field.$nodes().first();
      targetNode = $targetNode.data('node');
    } else {
      $targetNode = $currentNode.next('.tree-item');
      targetNode = $targetNode.data('node');
    }
    if (targetNode) {
      this._field.setNodesSelected(targetNode);
    }
  } else if (currentNode && keycode === scout.keys.LEFT) {
    if (currentNode.expanded) {
      this._field.setNodeExpanded(currentNode, false);
    } else if (currentNode.parentNode) {
      this._field.setNodesSelected(currentNode.parentNode);
    }
  } else if (currentNode && keycode === scout.keys.RIGHT) {
    if (!currentNode.expanded && !currentNode.leaf) {
      this._field.setNodeExpanded(currentNode, true);
    } else if (currentNode.childNodes.length > 0) {
      this._field.setNodesSelected(currentNode.childNodes[0]);
    }
  }
};
/**
 * @Override scout.KeyStroke
 */
scout.TreeControlKeyStrokes.prototype._drawKeyBox = function($container, drawedKeys) {
  var $currentNode = this._field.$selectedNodes().eq(0);
  var currentNode = $currentNode.data('node');
  var $upNode, $downNode, $leftNode, $rightNode;
  var offset = 4;

  if (currentNode) {
    if ($currentNode.length === 0) {
      $upNode = this._field.$nodes().last();
    } else {
      $upNode = $currentNode.prev('.tree-item');
    }
    if ($upNode && !scout.KeyStrokeUtil.keyStrokeAlreadyDrawnAndDraw(drawedKeys, this.ctrl, this.alt, this.shift, scout.keys.UP)) {
      scout.KeyStrokeUtil.drawSingleKeyBoxItem(offset, '↑', $upNode, this.ctrl, this.alt, this.shift);
    }
    if ($currentNode.length === 0) {
      $downNode = this._field.$nodes().first();
    } else {
      $downNode = $currentNode.next('.tree-item');
    }
    if ($downNode && !scout.KeyStrokeUtil.keyStrokeAlreadyDrawnAndDraw(drawedKeys, this.ctrl, this.alt, this.shift, scout.keys.DOWN)) {
      scout.KeyStrokeUtil.drawSingleKeyBoxItem(offset, '↓', $downNode, this.ctrl, this.alt, this.shift);
    }
    if (currentNode.expanded && !scout.KeyStrokeUtil.keyStrokeAlreadyDrawnAndDraw(drawedKeys, this.ctrl, this.alt, this.shift, scout.keys.LEFT)) {
      scout.KeyStrokeUtil.drawSingleKeyBoxItem(offset, '←', $currentNode, this.ctrl, this.alt, this.shift);
    } else if (!currentNode.expanded && !scout.KeyStrokeUtil.keyStrokeAlreadyDrawnAndDraw(drawedKeys, this.ctrl, this.alt, this.shift, scout.keys.RIGHT)) {
      scout.KeyStrokeUtil.drawSingleKeyBoxItem(offset, '→', $currentNode, this.ctrl, this.alt, this.shift);
    }
  }
};

/**
 * @Override scout.KeyStroke
 */
scout.TreeControlKeyStrokes.prototype.checkAndDrawKeyBox = function($container, drawedKeys) {
  this._drawKeyBox($container, drawedKeys);
};
/**
 * @Override scout.KeyStroke
 */
scout.TreeControlKeyStrokes.prototype.accept = function(event) {
  return event &&
    $.inArray(event.which, [scout.keys.UP, scout.keys.DOWN, scout.keys.LEFT, scout.keys.RIGHT, scout.keys.SPACE]) >= 0 &&
    event.ctrlKey === this.ctrl && event.altKey === this.alt && event.shiftKey === this.shift;
};
