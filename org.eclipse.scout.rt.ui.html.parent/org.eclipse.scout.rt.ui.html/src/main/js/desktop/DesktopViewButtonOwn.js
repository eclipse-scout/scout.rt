// SCOUT GUI
// (c) Copyright 2013-2014, BSI Business Systems Integration AG

scout.DesktopViewButtonOwn = function() {
  scout.DesktopViewButtonOwn.parent.call(this);
};
scout.inherits(scout.DesktopViewButtonOwn, scout.ModelAdapter);

scout.DesktopViewButtonOwn.prototype._render = function($parent) {
  this._$viewButton = $('#ViewAdd').beforeDiv('', 'view-item view-own', this.text);
  this._$viewButton.on('click', '', onClick)
    .appendDiv('', 'view-remove')
    .on('click', '', removeOwnView)
    .selectOne();

  var w = this._$viewButton.width();
  this._$viewButton.css('width', 0).animateAVCSD('width', w);

  var that = this;
  function onClick() {
    that._$viewButton.selectOne();
    //FIXME what to do on session?
    /*
      session.send('click', fhat.id);
       */
  }

  function removeOwnView() {
    $(this).parent()
      .animateAVCSD('width', 0, $.removeThis)
      .prev().click();
    return false;
  }

};

scout.DesktopViewButtonOwn.prototype.onModelPropertyChange = function(event) {
  if (event.properties.selected !== undefined) {
    if (event.properties.selected) {
      this._$viewButton.selectOne();
    }
  }
  // FIXME BSH Do we need a "super" call? Check with C.GU/A.WE
  //scout.DesktopViewButtonOwn.parent.prototype.onModelPropertyChange.call(this, event);
};

scout.DesktopViewButtonOwn.prototype.onModelAction = function() {};
