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
  var $targetNode, targetNode, newNodeSelection,
    keycode = event.which,
    $currentNode = this._field.$selectedNodes().eq(0),
    currentNode = $currentNode.data('node');

  if (keycode === scout.keys.SPACE && $currentNode.length > 0) {
    var check = !$($currentNode[0]).data('node').checked;
    for (var j = 0; j < $currentNode.length; j++) {
      var node = $($currentNode[j]).data('node');
      this._field.checkNode(node, check);
    }
  }

  if (keycode === scout.keys.UP) {
    if ($currentNode.length === 0) {
      $targetNode = this._field.$nodes().last();
      targetNode = $targetNode.data('node');
    } else {
      $targetNode = $currentNode.prev('.tree-node');
      targetNode = $targetNode.data('node');
    }
    if (targetNode) {
      newNodeSelection = targetNode;
    }
  } else if (keycode === scout.keys.DOWN) {
    if ($currentNode.length === 0) {
      $targetNode = this._field.$nodes().first();
      targetNode = $targetNode.data('node');
    } else {
      $targetNode = $currentNode.next('.tree-node');
      targetNode = $targetNode.data('node');
    }
    if (targetNode) {
      newNodeSelection = targetNode;
    }
  } else if (currentNode && keycode === scout.keys.SUBTRACT) {
    if (currentNode.expanded) {
      this._field.setNodeExpanded(currentNode, false);
    } else if (currentNode.parentNode) {
      newNodeSelection = currentNode.parentNode;
    }
  } else if (currentNode && keycode === scout.keys.ADD) {
    if (!currentNode.expanded && !currentNode.leaf) {
      this._field.setNodeExpanded(currentNode, true);
    } else if (currentNode.childNodes.length > 0) {
      newNodeSelection = currentNode.childNodes[0];
    }
  }

  if (newNodeSelection) {
    this._field.setNodesSelected(newNodeSelection);
    // scroll selection into view (if not visible)
    this._field.scrollTo(newNodeSelection);
  }
};
/**
 * @Override scout.KeyStroke
 */
scout.TreeControlKeyStrokes.prototype._drawKeyBox = function($container, drawedKeys) {
  var $currentNode = this._field.$selectedNodes().eq(0);
  var currentNode = $currentNode.data('node');
  var $upNode, $downNode;
  var offset = 4;

  if ($currentNode.length === 0) {
    $upNode = this._field.$nodes().last();
  } else {
    $upNode = $currentNode.prev('.tree-node');
  }
  if ($upNode && !scout.keyStrokeBox.keyStrokeAlreadyDrawnAndDraw(drawedKeys, this.ctrl, this.alt, this.shift, scout.keys.UP)) {
    scout.keyStrokeBox.drawSingleKeyBoxItem(offset, '↑', $upNode, this.ctrl, this.alt, this.shift, true);
  }
  if ($currentNode.length === 0) {
    $downNode = this._field.$nodes().first();
  } else {
    $downNode = $currentNode.next('.tree-node');
  }
  if ($downNode && !scout.keyStrokeBox.keyStrokeAlreadyDrawnAndDraw(drawedKeys, this.ctrl, this.alt, this.shift, scout.keys.DOWN)) {
    scout.keyStrokeBox.drawSingleKeyBoxItem(offset, '↓', $downNode, this.ctrl, this.alt, this.shift, true);
  }
  if (currentNode && currentNode.expanded && !scout.keyStrokeBox.keyStrokeAlreadyDrawnAndDraw(drawedKeys, this.ctrl, this.alt, this.shift, scout.keys.LEFT)) {
    scout.keyStrokeBox.drawSingleKeyBoxItem(offset, '-', $currentNode, this.ctrl, this.alt, this.shift, true);
  } else if (currentNode && !currentNode.expanded && !scout.keyStrokeBox.keyStrokeAlreadyDrawnAndDraw(drawedKeys, this.ctrl, this.alt, this.shift, scout.keys.RIGHT)) {
    scout.keyStrokeBox.drawSingleKeyBoxItem(offset, '+', $currentNode, this.ctrl, this.alt, this.shift, true);
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
    $.inArray(event.which, [scout.keys.UP, scout.keys.DOWN, scout.keys.ADD, scout.keys.SUBTRACT, scout.keys.SPACE]) >= 0 &&
    event.ctrlKey === this.ctrl && event.altKey === this.alt && event.shiftKey === this.shift;
};
