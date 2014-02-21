// SCOUT GUI
// (c) Copyright 2013-2014, BSI Business Systems Integration AG

Scout.DesktopOwnViewButton = function (scout, $viewButtonBar, viewButton) {

    scout.widgetMap[viewButton.id] = this;

    var $viewButton = $('#ViewAdd').beforeDiv('', 'view-item view-own', viewButton.text);
    $viewButton.on('click', '', onClick)
    	.appendDiv('', 'view-remove')
    	.on('click', '', removeOwnView)
    	.selectOne()
    	.css('width', 0);

    $viewButton=$viewButton.animateAVCSD('width', $viewButton.width());

    function onClick (event) {
      $viewButton.selectOne();
      //TODO what to do on scout?
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

    this.onModelPropertyChange = function onModelPropertyChange(event) {
      if(event.selected !== undefined) {
        if(event.selected){
          $viewButton.selectOne();
        }
        return;
      }
    };

    this.onModelAction = function onModelAction(event) {
    };

};
