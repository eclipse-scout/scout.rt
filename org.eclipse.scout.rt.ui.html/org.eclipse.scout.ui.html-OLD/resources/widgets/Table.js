// SCOUT GUI 0.2
// (c) Copyright 2013-2014, BSI Business Systems Integration AG
//
// desktop table namespace and element
//

Scout.Desktop.Table = function (scout, $bench) {
  //create container
  var $desktopTable = $bench.appendDiv('DesktopTable');
  $desktopTable.appendDiv('DesktopTableHeader');
  $desktopTable.appendDiv('DesktopTableData');
  $desktopTable.appendDiv('DesktopTableFooter');

  var $tableControl = $desktopTable.appendDiv('DesktopTableControl');
  $tableControl.appendDiv('ControlResizeTop');
  $tableControl.appendDiv('ControlResizeBottom');
  $tableControl.appendDiv('ControlGraph');
  $tableControl.appendDiv('ControlChart');
  $tableControl.appendDiv('ControlMap');
  $tableControl.appendDiv('ControlOrg');
  $tableControl.appendDiv('ControlLabel');

  var $tableControlInfo = $tableControl.appendDiv('ControlInfo');
  $tableControlInfo.appendDiv('ControlInfoSelect');
  $tableControlInfo.appendDiv('ControlInfoFilter');
  $tableControlInfo.appendDiv('ControlInfoMore');
  $tableControlInfo.appendDiv('ControlInfoLoad');

  // set this for later usage
  this.$div = $desktopTable;
};
