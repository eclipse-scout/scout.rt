// SCOUT GUI
// (c) Copyright 2013-2014, BSI Business Systems Integration AG

Scout.DesktopTree = function (scout, $container, model) {
  // create container
  var $desktopTreeScroll = $container.appendDiv('DesktopTreeScroll');
  var scrollbar = new Scout.Scrollbar($desktopTreeScroll, 'y');
  var self=this;

  scout.widgetMap[model.id] = this;

  // set this for later usage
  this.$container = $desktopTreeScroll;
  this.onModelAction = onModelAction;
  this.onModelPropertyChange = onModelPropertyChange;
  this.model = model;
  this.attachModel = attachModel;
  this.addNodes = addNodes;
  this.setNodeExpandedById = setNodeExpandedById;
  this.setNodeSelectedById = setNodeSelectedById;

  function attachModel() {
    addNodes_(this.model.nodes);
    if(this.model.selectedNodeIds) {
      this.setNodeSelectedById(this.model.selectedNodeIds[0]);
    }
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
    //Only expand / collapse if there are child nodes
    if(node.childNodes.length == 0) {
      return true;
    }
    if (expanded) {
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
    else {
      $node.removeClass('expanded');

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

  function setNodeSelectedById (nodeId) {
    var $node;
    if(nodeId) {
      $node = $desktopTreeScroll.find('#'+nodeId);
    }
    setNodeSelected($node);
  }

  function setNodeSelected ($node) {
    if(!$node) {
      $desktopTreeScroll.children().select(false);
      return;
    }

    $node.selectOne();

    var node = $node.data('node');

    //update model
    self.model.selectedNodeIds = [node.id];

    $('#DesktopBench').html('');
    if (node.type == 'table') {
      node.outlineId = self.model.id;
      new Scout.DesktopTable(scout, $('#DesktopBench'), node);
    }
    else{
      $('#DesktopBench').text(JSON.stringify(node));
    }
  }

  function addNodes_ (nodes, $parent) {
    var $allNodes = $('');

    for (var i =  nodes.length - 1; i >= 0; i--) {
      // create node
      var node = nodes[i];
      var state = '';
      if(node.expanded && node.childNodes.length > 0) {
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

      // collect all nodes for later return
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

    // pre select for immediate feedback
    $clicked.selectOne();

    var events = [];
    if (!$clicked.hasClass('expanded')) {
      events.push(new Scout.Event('nodeExpanded', self.model.id, {"nodeId":node.id, "expanded":true}));
    }
    events.push(new Scout.Event('nodeClicked', self.model.id, {"nodeId":nodeId}));
    var response = scout.sendEvents(events);
    scout.processEvents(response.events);
  }

  function clickNodeControl (event) {
    var $node = $(this).parent(),
      expanded = !$node.hasClass('expanded'),
      node = $node.data('node');

    setNodeExpanded($node, expanded);

    //FIXME really necessary? maybe property sync back
    var response = scout.send('nodeExpanded', self.model.id, {"nodeId":node.id, "expanded":expanded});
    scout.processEvents(response.events);

    // prevent immediately reopening
    return false;
  }

  function clickNodeMenu (event) {
    var $clicked = $(this),
      nodeId = $clicked.parent().attr('id'),
      x = $clicked.offset().left,
      y = $clicked.offset().top;

    new Scout.Menu(scout, self.model.id, nodeId, x, y);
  }

  function onModelPropertyChange(event) {
  }

  function onModelAction(event) {
    if (event.type_ == 'nodesInserted') {
      this.addNodes(event.nodes, event.commonParentNodeId);
    }
    else if (event.type_ == 'nodesSelected') {
      this.setNodeSelectedById(event.nodeIds[0]);
    }
    else if (event.type_ == 'nodeExpanded') {
      this.setNodeExpandedById(event.nodeId, event.expanded);
    }
  }

};
