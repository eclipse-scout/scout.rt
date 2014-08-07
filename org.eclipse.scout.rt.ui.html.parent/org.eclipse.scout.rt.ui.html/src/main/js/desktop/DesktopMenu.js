// SCOUT GUI
// (c) Copyright 2013-2014, BSI Business Systems Integration AG

scout.DesktopMenu = function(desktop, $parent) {
  this.$div;
  this.desktop = desktop;
  this.session = desktop.session;

  //  create container
  var $desktopView = $parent.appendDiv('DesktopViews');
  this.$div = $desktopView;

  //  add view-item, all before #viewAdd
  for (var i = 0; i < desktop.viewButtons.length; i++) {
    var button = desktop.viewButtons[i];
    button.render($desktopView);
  }

  var that = this;
  function addOwnView() {
    var name = $desktopView.children('.selected').text().split('(');
    var c = name.length > 1 ? parseInt(name[1], 0) + 1 : 2;
    //TODO widget id?
    var dummyModel = {
      'id': 'ownView' + c,
      'text': name[0] + ' (' + c + ')'
    }, buttonOwn;
    buttonOwn = new scout.DesktopViewButtonOwn($desktopView);
    buttonOwn.init(dummyModel, that.session);
    buttonOwn.render();
  }

};
