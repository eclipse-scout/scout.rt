// SCOUT GUI
// (c) Copyright 2013-2014, BSI Business Systems Integration AG

scout.MobileDesktopToolButtons = function(toolButtons, session) {
  this.$container;
  this.$parent;
  this.toolButtons = toolButtons;
};

scout.MobileDesktopToolButtons.prototype.render = function($desktop) {
  this.$parent = $desktop;
  var $desktopTools = $desktop.appendDiv('', '', 'DesktopTools'); // TODO CGU Check if #DesktopTools should really be an ID and not a class
  this.$container = $desktopTools;

  // create tool-buttons
  for (var i = 0; i < this.toolButtons.length; i++) {
    var state = this.toolButtons[i].state || '',
      icon = this.toolButtons[i].icon || '',
      shortcut = this.toolButtons[i].shortcut || '';

    var $tool = $desktopTools
      .appendDiv('tool-button ' + state, '', this.toolButtons[i].id)
      .attr('data-icon', icon).attr('data-shortcut', shortcut);

    if (!$tool.hasClass('disabled')) {
      $tool.on('click', '', clickTool);
    }
  }

  // create container for dialogs
  $desktopTools.appendDiv('', '', 'DesktopDialogs'); // TODO CGU Check if #DesktopDialogs should really be an ID and not a class

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
    $('#DesktopTools').beforeDiv('tool-open')
      .animateAVCSD('height', this.$parent.height(), null, null, 500);
  }
};
