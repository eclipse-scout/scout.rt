// SCOUT GUI
// (c) Copyright 2013-2014, BSI Business Systems Integration AG

Scout.DesktopTree = function (scout, $desktop, jsonOutline) {
  // create container
  var $desktopTree = $desktop.appendDiv('DesktopTree');
  var $desktopTreeScroll = $desktopTree.appendDiv('DesktopTreeScroll');
  var scrollbar = new Scout.Scrollbar($desktopTreeScroll, 'y');
  var self=this;

  $desktopTree.appendDiv('DesktopTreeResize')
    .on('mousedown', '', resizeTree);

  // set this for later usage
  this.$div = $desktopTreeScroll;

  this.outline = new Scout.Outline(scout, this, jsonOutline);
  addNodes_(jsonOutline.pages);

  this.setOutline = setOutline;
  this.addNodes = addNodes;
  this.setNodeExpandedById = setNodeExpandedById;
  this.setNodeSelectedById = setNodeSelectedById;
  this.clearNodes = clearNodes;

  // named  funktions
  function resizeTree (event) {
    $('body').addClass('col-resize')
      .on('mousemove', '', resizeMove)
      .one('mouseup', '', resizeEnd);

    function resizeMove(event){
      var w = event.pageX + 11;
      $desktopTree.width(w);
      $desktopTree.next().width('calc(100% - ' + (w + 80) + 'px)')
        .css('left', w);
    }

    function resizeEnd(event){
      $('body').off('mousemove')
        .removeClass('col-resize');
    }

    return false;
  }

  function clearNodes() {
    $desktopTreeScroll.find("div").remove();
  }

  function addNodes (nodes, parentNodeId) {
    var $parent = $desktopTreeScroll.find('#'+parentNodeId);
    var parentNode = $parent.data('node');
    if(parentNode === undefined) {
      throw "No parentNode found for id " + parentNodeId;
    }

    //update parent with new child nodes
    parentNode.childNodes.push.apply(parentNode.childNodes, nodes);

    if(parentNode.expanded) {
      addNodes_(nodes, $parent);
    }
  }

  function setNodeExpandedById (nodeId, expanded) {
    var $node = $desktopTreeScroll.find('#'+nodeId);
    setNodeExpanded($node, expanded);
  }

  function setNodeExpanded ($node, expanded) {
    if (expanded == $node.hasClass('expanded')) {
      return true;
    }

    var node = $node.data('node');
    node.expanded = expanded;
    if (expanded) {
      if(node.childNodes.length>0) {
        addNodes_(node.childNodes,$node);

        // open node
        if ($node.hasClass('can-expand') && !$node.hasClass('expanded')) {
          var level = $node.attr('data-level');
          var $newNodes = $node.nextUntil(
            function() {
              return $(this).attr("data-level") <= level;
            }
          );
          if ($newNodes.length) {
            // animated opening ;)
            $newNodes.wrapAll('<div id="TreeItemAnimate"></div>)');
            var h = $newNodes.height() * $newNodes.length,
              removeContainer = function () {$(this).replaceWith($(this).contents()); scrollbar.initThumb();};

            $('#TreeItemAnimate').css('height', 0)
              .animateAVCSD('height', h, removeContainer, scrollbar.initThumb);

            // animated control, at the end: parent is expanded
            var $control = $node.children('.tree-item-control'),
              rotateControl = function (now, fx) {
                $control.css('transform', 'rotate(' + now + 'deg)'); },
              addExpanded = function () {
                $node.addClass('expanded');};

            $control.css('borderSpacing', 0)
              .animateAVCSD('borderSpacing', 90, addExpanded, rotateControl);
          }
        }
      }
    }
    else {
      // click always select, even if closed
      $node.selectOne().removeClass('expanded');

      // animated closing ;)
      $node.nextUntil(function() {return $(this).attr("data-level") <= level;})
        .wrapAll('<div id="TreeItemAnimate"></div>)');
      $('#TreeItemAnimate').animateAVCSD('height', 0, $.removeThis, scrollbar.initThumb);

      // animated control
      var $control = $node.children('.tree-item-control'),
        rotateControl = function(now, fx){$control.css('transform', 'rotate(' + now + 'deg)');};
      $control.css('borderSpacing', 90)
        .animateAVCSD('borderSpacing', 0, null, rotateControl);
     }
  }

  function setNodeSelectedById(nodeId) {
    var $node = $desktopTreeScroll.find('#'+nodeId),
     node = $node.data('node');

    $('#DesktopBench').html('');
    if (node.type == 'table') {
      node.outlineId = self.outline.jsonOutline.id;
      new Scout.DesktopTable(scout, $('#DesktopBench'), node);
    } else{
      $('#DesktopBench').text(JSON.stringify(node));
    }
  }

  function addNodes_ (nodes, $parent) {
    var $allNodes = $('');

    for (var i =  nodes.length - 1; i >= 0; i--) {
      // create node
      var node = nodes[i];
      var state = '';
      if(node.expanded) {
        state='expanded ';
      }
      if(!node.leaf) {
        state+='can-expand '; //TODO rename to leaf
      }
      level = $parent ? $parent.data('level') + 1 : 0;

      var $node = $.makeDiv(node.id, 'tree-item ' + state, node.text)
              .on('click', '', clickNode)
              .data('node', node)
              .attr('data-level', level)
              .css('margin-left', level * 20)
              .css('width', 'calc(100% - ' + (level * 20 + 20) + 'px)');

      // decorate with (close) control
      var $control = $node.appendDiv('', 'tree-item-control')
        .on('click', '', clickNodeControl);

      // rotate control if expanded
      if ($node.hasClass('expanded')) {
        $control.css('transform', 'rotate(90deg)');
      }

      // decorate with menu
      $node.appendDiv('', 'tree-item-menu')
        .on('click', '', clickNodeMenu);

      // append first node and successors
      if ($parent) {
        $node.insertAfter($parent);
      } else {
        $node.appendTo($desktopTreeScroll);
      }

      // collect all nodes for later retur
      $allNodes = $allNodes.add($node);

      // if model demands children, create them
      if (node.expanded && node.childNodes) {
        var $n = addNodes_(node.childNodes, $node);
        $allNodes = $allNodes.add($n);
      }
    }

    // return all created nodes
    return $allNodes;
  }

  function clickNode (event) {
    var $clicked = $(this),
      node = $clicked.data('node'),
      nodeId = $clicked.attr('id');

    // selected the one
    $clicked.selectOne();

    //FIXME should be sent in one server call
    if (!$clicked.hasClass('expanded')) {
      var response = scout.syncAjax('nodeExpanded', self.outline.jsonOutline.id, {"nodeId":node.id, "expanded":true});
      scout.processEvents(response.events);
    }
    var response = scout.syncAjax('nodeClicked', self.outline.jsonOutline.id, {"nodeId":nodeId});
    scout.processEvents(response.events);
  }

  function clickNodeControl (event) {
    var $node = $(this).parent(),
      expanded = !$node.hasClass('expanded'),
      node = $node.data('node');

    setNodeExpanded($node, expanded);

    //FIXME really necessary? maybe property sync back
    var response = scout.syncAjax('nodeExpanded', self.outline.jsonOutline.id, {"nodeId":node.id, "expanded":expanded});
    scout.processEvents(response.events);

    // prevent immediately reopening
    return false;
  }

  function clickNodeMenu (event) {
    var $clicked = $(this),
      nodeId = $clicked.parent().attr('id'),
      x = $clicked.offset().left,
      y = $clicked.offset().top;

    new Scout.Menu(scout, self.outline.jsonOutline.id, nodeId, x, y);
  }

  function setOutline(outlineId) {
    this.outline = scout.widgetMap[outlineId];
    clearNodes();
    addNodes_(this.outline.jsonOutline.pages);
  }

};
