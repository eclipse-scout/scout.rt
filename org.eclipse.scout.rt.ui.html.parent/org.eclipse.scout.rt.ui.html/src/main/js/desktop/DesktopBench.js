// SCOUT GUI
// (c) Copyright 2013-2014, BSI Business Systems Integration AG

scout.DesktopBench = function(desktop, $parent) {
  this.session = desktop.session;

  this.$container = $parent.appendDiv('DesktopBench');

  this.taskbar = new scout.DesktopTaskbar(desktop);
  this.taskbar.render(this.$container);

  this.menubar = new scout.DesktopMenubar(this.$container, this.session);

  //FIXME CGU remove, just simulating offline
  $('#ViewLogo').on('click', function(){
    if(this.session.url==='json') {
      this.session.url='http://localhost:123';
    }
    else {
      this.session.url='json';
    }
  }.bind(this));
};

scout.DesktopBench.prototype.addTable = function(table) {
  table.render(this.$container);
//  this.taskbar.addTab({id: table.id, content: table});
};

scout.DesktopBench.prototype.removeTable = function(table) {
  table.remove();
//  this.taskbar.removeTab(this.taskbar.tabs[table.id]);
};
