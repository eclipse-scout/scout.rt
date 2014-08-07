// SCOUT GUI
// (c) Copyright 2013-2014, BSI Business Systems Integration AG

scout.MobileDesktopToolButtons = function(toolButtons, session) {
  this.$div;
  this.$parent;
  this.toolButtons = toolButtons;
};

scout.MobileDesktopToolButtons.prototype.render = function($desktop) {
  this.$parent = $desktop;
  var $desktopTools = $desktop.appendDiv('DesktopTools');
  this.$div = $desktopTools;

  // create tool-buttons
  for (var i = 0; i < this.toolButtons.length; i++) {
    var state = this.toolButtons[i].state || '',
      icon = this.toolButtons[i].icon || '',
      shortcut = this.toolButtons[i].shortcut || '';

    var $tool = $desktopTools
      .appendDiv(this.toolButtons[i].id, 'tool-button ' + state)
      .attr('data-icon', icon).attr('data-shortcut', shortcut);


    if (!$tool.hasClass('disabled')) {
      $tool.on('click', '', clickTool);
    }
  }

  // create container for dialogs
  $desktopTools.appendDiv('DesktopDialogs');

  var that = this;
  function clickTool() {
    that.open($(this));
  }
};

scout.MobileDesktopToolButtons.prototype.open = function($tool) {
  $('.tool-open').animateAVCSD('height', 0, $.removeThis, null, 500);

  if ($tool.hasClass('selected')) {
    $tool.removeClass('selected');
  } else {
    $tool.selectOne();
    $('#DesktopTools').beforeDiv('', 'tool-open')
      .animateAVCSD('height', this.$parent.height(), null, null, 500);
  }
};
