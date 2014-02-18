// SCOUT GUI 0.2
// (c) Copyright 2013-2014, BSI Business Systems Integration AG
//
// tree
//

Scout.Desktop.Tree = function (scout, $desktop) {
  // create container
  var $desktopTree = $desktop.appendDiv('DesktopTree');
  var $desktopTreeScroll = $desktopTree.appendDiv('DesktopTreeScroll');
  var scrollbar = new Scout.Scrollbar(scout, $desktopTreeScroll, 'y', true);
  $desktopTree.appendDiv('DesktopTreeResize')
    .on('mousedown', '', resizeTree);

  // set this for later usage
  this.$div = $desktopTree;
  this.addNodes = addNodes;

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

  function addNodes (nodes, $parent) {
    var $allNodes = $('');

    for (var i =  nodes.length - 1; i >= 0; i--) {
      // create node
      var node = nodes[i],
        state = node.state || '',
        level = $parent ? $parent.data('level') + 1 : 0;

      var $node = $.makeDiv(node.id, 'tree-item ' + state, node.label)
              .on('click', '', clickNode)
              .data('bench', node.bench)
              .attr('data-level', level)
              .css('margin-left', level * 20)
              .css('width', 'calc(100% - ' + (level * 20 + 30) + 'px)');

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
      if (node.children) {
        var $n = addNodes(node.children, $node);
        $allNodes = $allNodes.add($n);
      }
    }

    // return all created nodes
    return $allNodes;
  }

  function clickNode (event) {
    var $clicked = $(this),
      bench = $clicked.data('bench');

    // selected the one
    $clicked.selectOne();

    // show bench
    if (bench.type == 'table') {
      new Scout.Desktop.Table(scout, $('#DesktopBench'));
    } else{
      $('#DesktopBench').text(JSON.stringify(bench));
    }

    // open node
    if ($clicked.hasClass('can-expand') && !$clicked.hasClass('expanded')) {
      // load model and draw nodes
      var nodes = scout.syncAjax('drilldown', $clicked.attr('id'));
      var $newNodes = addNodes(nodes, $clicked);

      if ($newNodes.length) {
        // animated opening ;)
        $newNodes.wrapAll('<div id="TreeItemAnimate"></div>)');
        var h = $newNodes.height() * $newNodes.length,
          removeContainer = function () {$(this).replaceWith($(this).contents());};

        $('#TreeItemAnimate').css('height', 0)
          .animateAVCSD('height', h, removeContainer, scrollbar.initThumb);

        // animated control, at the end: parent is expanded
        var $control = $clicked.children('.tree-item-control'),
          rotateControl = function (now, fx) {
            $control.css('transform', 'rotate(' + now + 'deg)'); },
          addExpanded = function () {
            $clicked.addClass('expanded');};

        $control.css('borderSpacing', 0)
          .animateAVCSD('borderSpacing', 90, addExpanded, rotateControl);
      }
    }
  }

  function clickNodeControl (event) {
    var $close = $(this).parent(),
      level = $close.attr('data-level');

    // only go further (and return false) if expanded
    if (!$close.hasClass('expanded')) return true;

    // click always select, even if closed
    $close.selectOne().removeClass('expanded');

    // animated closing ;)
    $close.nextUntil(function() {return $(this).attr("data-level") <= level;})
      .wrapAll('<div id="TreeItemAnimate"></div>)');
    $('#TreeItemAnimate').animateAVCSD('height', 0, $.removeThis, scrollbar.initThumb);

    // animated control
    var $control = $close.children('.tree-item-control'),
      rotateControl = function(now, fx){$control.css('transform', 'rotate(' + now + 'deg)');};
    $control.css('borderSpacing', 90)
      .animateAVCSD('borderSpacing', 0, null, rotateControl);

    // prevent immediately reopening
    return false;
  }

  function clickNodeMenu (event) {
    var $clicked = $(this),
      id = $clicked.parent().attr('id'),
      x = $clicked.offset().left,
      y = $clicked.offset().top;

    new Scout.Menu(scout, id, x, y);
  }
};
