// SCOUT GUI
// (c) Copyright 2013-2014, BSI Business Systems Integration AG

scout.DesktopToolButton = function(session, $desktop, toolButtons) {
  this.$div;

  // create container
  var $desktopTools = $desktop.appendDiv('DesktopTools');
  this.$div = $desktopTools;

  // create tool-items
  for (var i = 0; i < toolButtons.length; i++) {
    var state = toolButtons[i].state || '',
      icon = toolButtons[i].icon || '',
      shortcut = toolButtons[i].shortcut || '';

    var $tool = $desktopTools
      .appendDiv(toolButtons[i].id, 'tool-item ' + state, toolButtons[i].label)
      .attr('data-icon', icon).attr('data-shortcut', shortcut);

    if (!$tool.hasClass('disabled')) {
      $tool.on('click', '', clickTool);
    }
  }

  // create container for dialogs
  $desktopTools.appendDiv('DesktopDialogs');

  // named event funktions
  function clickTool() {
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
