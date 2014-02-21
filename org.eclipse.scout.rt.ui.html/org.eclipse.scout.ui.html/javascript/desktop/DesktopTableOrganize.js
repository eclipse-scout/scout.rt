// SCOUT GUI
// (c) Copyright 2013-2014, BSI Business Systems Integration AG

Scout.DesktopTableOrganize = function (scout, $controlContainer, model, columns, data) {
  var response = scout.syncAjax('dataModel', model.outlineId, {"nodeId": model.nodeId});
  var dataModel = response.events[0].dataModel;
  log(dataModel);

};
