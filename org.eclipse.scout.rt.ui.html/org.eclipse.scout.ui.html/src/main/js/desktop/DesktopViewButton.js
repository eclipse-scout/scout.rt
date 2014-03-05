// SCOUT GUI
// (c) Copyright 2013-2014, BSI Business Systems Integration AG

Scout.DesktopViewButton = function (scout, $parent, viewButton) {
  scout.widgetMap[viewButton.id] = this;

  var state = '';
  if(viewButton.selected) {
    state='selected';
  }
  var $viewButton = $parent.appendDiv(viewButton.id, 'view-item ' + state, viewButton.text);
  $viewButton.on('click', '', onClick);

  function onClick (event) {
    $viewButton.selectOne();
    scout.send('click', $(this).attr('id'));
  }

};

Scout.DesktopViewButton.prototype.onModelPropertyChange = function (event) {
  if(event.selected !== undefined) {
    if(event.selected){
      $viewButton.selectOne();
    }
  }
};

Scout.DesktopViewButton.prototype.onModelAction = function (event) {
};
