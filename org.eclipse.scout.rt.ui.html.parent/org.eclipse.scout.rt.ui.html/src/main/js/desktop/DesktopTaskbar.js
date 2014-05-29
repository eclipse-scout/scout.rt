// SCOUT GUI
// (c) Copyright 2013-2014, BSI Business Systems Integration AG

scout.DesktopTaskbar = function(desktop) {
  this.desktop = desktop;
  this.$div;
  this.$formThumbs;
  this.formThumbsMap = {};
};

scout.DesktopTaskbar.prototype.render = function($desktop) {
  this.$div = $desktop.appendDiv(undefined, 'desktop-taskbar');

  // create tool-items
  for (var i = 0; i < this.desktop.model.toolButtons.length; i++) {
    var toolButton = this.desktop.model.toolButtons[i];
    var state = toolButton.state || '',
      icon = toolButton.icon || '',
      shortcut = toolButton.shortcut || '';

    var $tool = this.$div
      .appendDiv(toolButton.id, 'tool-item taskbar-item ' + state, toolButton.label)
      .attr('data-icon', icon).attr('data-shortcut', shortcut);

    if (!$tool.hasClass('disabled')) {
      $tool.on('click', '', onToolButtonClick);
    }
  }

  this.$formThumbs = this.$div.appendDiv(undefined, 'form-thumbnails');

  var that = this;
  function onToolButtonClick() {
    that.open($(this));
  }
};

scout.DesktopTaskbar.prototype.open = function($tool) {
  $('.tool-open').animateAVCSD('width', 0, $.removeThis, null, 500);

  if ($tool.hasClass("selected")) {
    $tool.removeClass("selected");
  } else {
    $tool.selectOne();
    $('.desktop-taskbar').beforeDiv('', 'tool-open')
      .animateAVCSD('width', 300, null, null, 500);
  }
};

scout.DesktopTaskbar.prototype.formAdded = function(form) {
  var $formThumb = this.$formThumbs.appendDiv(undefined, 'form-thumbnail taskbar-item', form.model.title);
  this.formThumbsMap[form.model.id] = $formThumb;
  $formThumb.attr('data-icon', '\uf096');
  $formThumb.on('click', onFormThumbClick);

  $formThumb.selectOne();

  var that = this;
  function onFormThumbClick() {
    that.desktop.activateForm(form);
  }
};

scout.DesktopTaskbar.prototype.formActivated = function(form) {
  var $formThumb = this.formThumbsMap[form.model.id];

  $formThumb.selectOne();
};

scout.DesktopTaskbar.prototype.formRemoved = function(form) {
  var $formThumb = this.formThumbsMap[form.model.id];
  delete this.formThumbsMap[form.model.id];
  $formThumb.remove();
};

scout.DesktopTaskbar.prototype.formDisabled = function(form) {
  var $formThumb = this.formThumbsMap[form.model.id];

  $formThumb.addClass('disabled');
};

scout.DesktopTaskbar.prototype.formEnabled = function(form) {
  var $formThumb = this.formThumbsMap[form.model.id];

  $formThumb.removeClass('disabled');
};
