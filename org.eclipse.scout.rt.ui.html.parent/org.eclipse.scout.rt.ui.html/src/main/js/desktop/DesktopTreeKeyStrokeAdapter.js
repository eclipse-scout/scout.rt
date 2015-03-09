scout.DesktopTreeKeyStrokeAdapter = function(field) {
  scout.DesktopTreeKeyStrokeAdapter.parent.call(this, field);

  //modifier for navigation is ctrl+shift
  this.ctrl = true;
  this.shift = true;
  this.meta = false;
  this.alt = false;
};
scout.inherits(scout.DesktopTreeKeyStrokeAdapter, scout.TreeKeyStrokeAdapter);


scout.DesktopTreeKeyStrokeAdapter.prototype.accept = function(event) {
  if($('.glasspane').length>0){
    return false;
  }
  return true;
  //accept events if focus is on scout div
//  var activeElement = document.activeElement;
//  if(this.$target[0] === activeElement){
//    return true;
//  }
//  return false;
};
