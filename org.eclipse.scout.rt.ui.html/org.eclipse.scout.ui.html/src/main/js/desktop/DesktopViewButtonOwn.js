// SCOUT GUI
// (c) Copyright 2013-2014, BSI Business Systems Integration AG

Scout.DesktopViewButtonOwn = function(scout, $parent, viewButton) {

  scout.widgetMap[viewButton.id] = this;

  var $viewButton = $('#ViewAdd').beforeDiv('', 'view-item view-own', viewButton.text);
  $viewButton.on('click', '', onClick)
    .appendDiv('', 'view-remove')
    .on('click', '', removeOwnView)
    .selectOne();

  var w = $viewButton.width();
  $viewButton.css('width', 0).animateAVCSD('width', w);

  function onClick(event) {
    $viewButton.selectOne();
    //TODO what to do on scout?
    /*
      scout.send('click', viewButton.id);
       */
  }

  function removeOwnView(event) {
    $(this).parent()
      .animateAVCSD('width', 0, $.removeThis)
      .prev().click();
    return false;
  }

};

Scout.DesktopViewButtonOwn.prototype.onModelPropertyChange = function(event) {
  if (event.selected !== undefined) {
    if (event.selected) {
      $viewButton.selectOne();
    }
  }
};

Scout.DesktopViewButtonOwn.prototype.onModelAction = function(event) {};
