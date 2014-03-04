Scout.DesktopTreeContainer = function (scout, $desktop, model) {
  var $container = $desktop.appendDiv('DesktopTree');
  $container.appendDiv('DesktopTreeResize')
    .on('mousedown', '', resizeTree);

  // named  functions
  function resizeTree (event) {
    $('body').addClass('col-resize')
      .on('mousemove', '', resizeMove)
      .one('mouseup', '', resizeEnd);

    function resizeMove(event){
      var w = event.pageX + 11;
      $container.width(w);
      $container.next().width('calc(100% - ' + (w + 80) + 'px)')
        .css('left', w);
    }

    function resizeEnd(event){
      $('body').off('mousemove')
        .removeClass('col-resize');
    }
    return false;
  }

  this.handleOutlineCreated = handleOutlineCreated;
  this.handleOutlineChanged = handleOutlineChanged;
  this.attachModel = attachModel;

  this.desktopTree = new Scout.DesktopTree(scout, $container, model);

  function handleOutlineCreated(event) {
    var desktopTree = new Scout.DesktopTree(scout, $container, event);
    desktopTree.attachModel();
  }

  function handleOutlineChanged(outlineId) {
    this.desktopTree.$container.detach();

    this.desktopTree = scout.widgetMap[outlineId];
    this.desktopTree.$container.appendTo($container);
  }

  //TODO attachModel only necessary because setNodeSelection relies on desktop bench which is created later, is there a better way?
  function attachModel() {
    this.desktopTree.attachModel();
  }

};
