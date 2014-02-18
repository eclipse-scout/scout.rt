// SCOUT GUI 0.2
// (c) Copyright 2013-2014, BSI Business Systems Integration AG
//

Scout.Desktop.OwnViewButton = function (scout, $viewButtonBar, viewButton) {

  this.handleUpdate = function handleUpdate(eventData) {
      if(eventData.selected !== undefined) {
        $viewButton.select(eventData.selected);
      }
    };

    scout.widgetMap[viewButton.id] = this;

    var selected = viewButton.selected;

    var $viewButton = $('#ViewAdd').beforeDiv('', 'view-item view-own', viewButton.text);
    $viewButton.
    on('click', '', onClick).
    appendDiv('', 'view-remove').
    on('click', '', removeOwnView).
    selectOne().
    css('width', 0);

    $viewButton=$viewButton.animateAVCSD('width', $viewButton.width());

    function onClick (event) {
      //TODO what to do on scout?
      $viewButton.selectOne();
      /*
      var response = scout.syncAjax('click', viewButton.id);
      scout.processEvents(response.events);
      */
    }

    function removeOwnView (event) {
      $(this).parent()
        .animateAVCSD('width', 0, $.removeThis)
        .prev().click();
      return false;
    }
};
