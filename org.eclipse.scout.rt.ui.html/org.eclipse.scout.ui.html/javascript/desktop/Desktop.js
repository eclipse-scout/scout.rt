// SCOUT GUI
// (c) Copyright 2013-2014, BSI Business Systems Integration AG

Scout.Desktop = function (scout, $parent, widget) {
  scout.widgetMap[widget.id] = this;

  // main container
  var view, tool, tree, bench;
  
  // create all 4 containers
  var view = new Scout.DesktopViewButtonBar(scout, $parent, widget.viewButtons);
  var tool = new Scout.DesktopToolButton(scout, $parent, widget.toolButtons);
  var tree = new Scout.DesktopTree(scout, $parent);
  var bench = new Scout.DesktopBench(scout, $parent);

  // show nodes
  tree.outlineId = widget.outline.id;
  tree.addNodes(widget.outline.pages);

  // alt and f1-help
  $(window).keydown(function (event)  {
    if (event.which == 18) {
      removeKeyBox();
      drawKeyBox();
    }
  });

  $(window).keyup(function (event)  {
    if (event.which == 18) {
      removeKeyBox();
      return false;
    }
  });

  $(window).blur(function (event)  {
    removeKeyBox();
  });


  // key handling
  var fKeys = {};
  if (tool) {
      $('.tool-item', tool.$div).each(function (i, e) {
        var shortcut = parseInt($(e).attr('data-shortcut').replace('F', '')) + 111;
        fKeys[shortcut] = e;
      });
  }

  $(window).keydown(function (event)  {
    // numbers: views
    if (event.which >= 49 && event.which <= 57){
      $('.view-item', view.$div).eq(event.which - 49).click();
    }

    // function keys: tools
    if (fKeys[event.which]){
      $(fKeys[event.which]).click();
      return false;
    }

    // left: up in tree
    if (event.which == 37){
      $('.selected', tree.$div).prev().click();
      removeKeyBox();
      return false;
    }

    // right: down in tree
    if (event.which == 39){
      $('.selected', tree.$div).next().click();
      removeKeyBox();
      return false;
    }

    // +/-: open and close tree
    if (event.which == 109 || event.which == 107){
      $('.selected', tree.$div).children('.tree-item-control').click();
      removeKeyBox();
      return false;
    }

    //todo: make clicked row visible
    //todo: first select one ow then fire;
    if ([38, 40, 36, 35, 33, 34].indexOf(event.which) > -1){
      var $rowsAll = $('.table-row', bench.$div),
        $rowsSelected = $('.row-selected', bench.$div),
        $rowClick;
      
      // up: move up
      if (event.which == 38){
        if ($rowsSelected.length > 0) {
          $rowClick = $rowsSelected.first().prev();
        } else {
          $rowClick = $rowsAll.last();
        }
      }

      // down: move down
      if (event.which == 40){
        var $row = $('.row-selected', bench.$div);
        if ($rowsSelected.length > 0) {
          $rowClick = $rowsSelected.last().next();
        } else {
          $rowClick = $rowsAll.first();
        }
      }

      // home: top of table
      if (event.which == 36){
        $rowClick = $rowsAll.first();
      }

      // end: bottom of table
      if (event.which == 35){
        $rowClick = $rowsAll.last();
      }

      // pgup: jump up
      if (event.which == 33){
        if ($rowsSelected.length > 0) {
          var $prev = $rowsSelected.first().prevAll();
          if ($prev.length > 10) {
            $rowClick = $prev.eq(10);
          } else { 
            $rowClick = $rowsAll.first();
          }
        } else {
          $rowClick = $rowsAll.last();
        }
      }

      // pgdn: jump down
      if (event.which == 34){
        if ($rowsSelected.length > 0) {
          var $prev = $rowsSelected.last().nextAll();
          if ($prev.length > 10) {
            $rowClick = $prev.eq(10);
          } else { 
            $rowClick = $rowsAll.last();
          }
        } else {
          $rowClick = $rowsAll.first();
        }
      }
      
      $rowClick.trigger('mousedown').trigger('mouseup');
    }
  });

  this.onModelAction = onModelAction;
  this.onModelPropertyChange = onModelPropertyChange;

  function onModelPropertyChange(event) {
  }

  function onModelAction(event) {
    if (event.type_ == 'outlineChanged') {
      tree.outlineId = event.outline.id;
      tree.clearNodes();
      tree.addNodes(event.outline.pages);
      return;
    }
  }

  function removeKeyBox () {
    $('.key-box').remove();
    $('.tree-item-control').show();
  }

  function drawKeyBox () {
    // keys for views
    $('.view-item', view.$div).each(function (i, e) {
        if (i < 9)  $(e).appendDiv('', 'key-box', i + 1);
      });

    // keys for tools
    if (tool) {
      $('.tool-item', tool.$div).each(function (i, e) {
        $(e).appendDiv('', 'key-box', $(e).attr('data-shortcut'));
      });
    }
   
    // keys for tree
    var node = $('.selected', tree.$div),
      prev = node.prev(),
      next = node.next();

    if (node.hasClass('can-expand')) {
      if (node.hasClass('expanded')) {
        node.appendDiv('', 'key-box large', '-');
      } else {
        node.appendDiv('', 'key-box large', '+');
      }
      node.children('.tree-item-control').hide();
    }

    if (prev) {
      prev.appendDiv('', 'key-box', '←');
      prev.children('.tree-item-control').hide();
    }

    if (next) {
      next.appendDiv('', 'key-box', '→');
      next.children('.tree-item-control').hide();
    }

    // keys for table

    var node = $('#TableData', bench.$div);
    if (node) {
      node.appendDiv('', 'key-box top3', 'Home');
      node.appendDiv('', 'key-box top2', 'PgUp');
      node.appendDiv('', 'key-box top1', '↑');
      node.appendDiv('', 'key-box bottom1', '↓');
      node.appendDiv('', 'key-box bottom2', 'PgDn');
      node.appendDiv('', 'key-box bottom3', 'End');
    }
  }


};

