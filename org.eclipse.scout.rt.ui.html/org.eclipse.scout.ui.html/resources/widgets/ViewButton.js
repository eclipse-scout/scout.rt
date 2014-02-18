// SCOUT GUI 0.2
// (c) Copyright 2013-2014, BSI Business Systems Integration AG
//

Scout.Desktop.ViewButton = function (scout, $viewButtonBar, viewButton) {
    this.handleUpdate = function handleUpdate(eventData) {
      if(eventData.selected !== undefined) {
        $viewButton.select(eventData.selected);
      }
    };
    scout.widgetMap[viewButton.id] = this;

    var selected = viewButton.selected;

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
};
