// SCOUT GUI 0.2
// (c) Copyright 2013-2014, BSI Business Systems Integration AG
//

Scout.Desktop.ViewButton = function (scout, $viewButtonBar, viewButton) {
    this.handleUpdate = function handleUpdate(event) {
      if(event.selected !== undefined) {
        $viewButton.select(event.selected);
      }
    };
    scout.widgetMap[viewButton.id] = this;

    var selected = viewButton.selected;
    var state = '';
    if(viewButton.selected) {
      state='selected';
    }
    var $viewButton = $viewButtonBar.appendDiv(viewButton.id, 'view-item ' + state, viewButton.text)
    .on('click', '', onClick);

    function onClick (event) {
      var response = scout.syncAjax('click', $(this).attr('id'));
      scout.processEvents(response.events);
    }
};
