// SCOUT GUI
// (c) Copyright 2013-2014, BSI Business Systems Integration AG

Scout.Desktop = function (scout, $parent, widget) {
  scout.widgetMap[widget.id] = this;

  // create all 4 containers
  var view = new Scout.DesktopViewButtonBar(scout, $parent, widget.viewButtons);
  if(widget.toolButtons) {
    var tool = new Scout.DesktopToolButton(scout, $parent, widget.toolButtons);
  }
  var tree = new Scout.DesktopTree(scout, $parent);
  var bench = new Scout.DesktopBench(scout, $parent);

  // show nodes
  tree.outlineId = widget.outline.id;
  tree.addNodes(widget.outline.pages);

  // key handling


  // alt and f1-help
  $('body').keydown(function (event)  {
    log(event);
    if (event.which == 18) {
      $('.key-box').remove();

      // keys for views
      $('.view-item', view.$div).each(function (i) {
          log(1);
          $(this).appendDiv('', '.key-box', '');
        });

      // keys for tools

      // keys for tree

      // keys for table

    }
  });

  $('body').keyup(function (event)  {
    log(1);
    if (event.which == 18) {
      $('.key_box').remove();
    }
  });

  this.onModelAction = onModelAction;
  this.onModelPropertyChange =  onModelPropertyChange;

  function onModelPropertyChange(event) {
  }

  function onModelAction(event) {
    if(event.type_=="outlineChanged") {
      tree.outlineId = event.outline.id;
      tree.clearNodes();
      tree.addNodes(event.outline.pages);
      return;
    }
  }

};

