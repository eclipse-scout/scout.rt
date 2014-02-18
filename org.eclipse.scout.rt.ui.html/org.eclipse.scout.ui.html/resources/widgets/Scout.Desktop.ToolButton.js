// SCOUT GUI 0.2
// (c) Copyright 2013-2014, BSI Business Systems Integration AG
//
// tool namespace and container
//

Scout.Desktop.ToolButton = function (scout, $desktop, tools) {
  // create container
  var $desktopTools = $desktop.appendDiv('DesktopTools');

  // create tool-items
  for (var i = 0; i < tools.length; i++) {
    var state = tools[i].state || '',
      icon = tools[i].icon || '',
      shortcut = tools[i].shortcut || '';

    var $tool = $desktopTools
      .appendDiv(tools[i].id, 'tool-item ' + state, tools[i].label)
      .attr('data-icon', icon).attr('data-shortcut', shortcut);

    if (!$tool.hasClass('disabled')) {
      $tool.on('click', '', clickTool);
    }
  }

  // create container for dialogs
  $desktopTools.appendDiv('DesktopDialogs');

  // set this for later usage
  this.$div = $desktopTools;

  // named event funktions
  function clickTool (event) {
    var $clicked = $(this);

    $('.tool-open').animateAVCSD('width', 0, $.removeThis, null, 500);

    if ($clicked.hasClass("selected")) {
      $clicked.removeClass("selected");
    } else {
      $clicked.selectOne();
      $('#DesktopTools').beforeDiv('', 'tool-open')
        .animateAVCSD('width', 300, null, null, 500);
    }
  }
};
