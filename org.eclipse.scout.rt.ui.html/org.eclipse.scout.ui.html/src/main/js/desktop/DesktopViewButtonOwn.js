// SCOUT GUI
// (c) Copyright 2013-2014, BSI Business Systems Integration AG

Scout.DesktopViewButtonOwn = function(scout, $parent, viewButton) {

  scout.widgetMap[viewButton.id] = this;

  this._$viewButton = $('#ViewAdd').beforeDiv('', 'view-item view-own', viewButton.text);
  this._$viewButton.on('click', '', onClick)
    .appendDiv('', 'view-remove')
    .on('click', '', removeOwnView)
    .selectOne();

  var w = this._$viewButton.width();
  this._$viewButton.css('width', 0).animateAVCSD('width', w);

  function onClick() {
    this._$viewButton.selectOne();
    //FIXME what to do on scout?
    /*
      scout.send('click', viewButton.id);
       */
  }

  function removeOwnView() {
    $(this).parent()
      .animateAVCSD('width', 0, $.removeThis)
      .prev().click();
    return false;
  }

};

Scout.DesktopViewButtonOwn.prototype.onModelPropertyChange = function(event) {
  if (event.selected !== undefined) {
    if (event.selected) {
      this._$viewButton.selectOne();
    }
  }
};

Scout.DesktopViewButtonOwn.prototype.onModelAction = function() {};
