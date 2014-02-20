// SCOUT GUI 0.2
// (c) Copyright 2013-2014, BSI Business Systems Integration AG
//

Scout.DesktopViewButton = function (scout, $viewButtonBar, viewButton) {

    scout.widgetMap[viewButton.id] = this;

    var state = '';
    if(viewButton.selected) {
      state='selected';
    }
    var $viewButton = $viewButtonBar.appendDiv(viewButton.id, 'view-item ' + state, viewButton.text);
    $viewButton.on('click', '', onClick);

    function onClick (event) {
      $viewButton.selectOne();
      var response = scout.syncAjax('click', $(this).attr('id'));
      scout.processEvents(response.events);
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
