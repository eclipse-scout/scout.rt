var FocusManagerSpecHelper = function() {};

FocusManagerSpecHelper.prototype.checkListenersRegistered = function() {
  var activeElement = document.activeElement;
  this.checkListeners(activeElement, true);
};

FocusManagerSpecHelper.prototype.checkListeners = function(field, registered) {
  var focusout = false,
    hide = false,
    remove = false,
    focusoutListener=[],
    hideListener = [],
    removeListener = [],
    i;
  if($._data(field).events){
    if($._data(field).events.focusout){
      focusoutListener = $._data(field).events.focusout;
    }
    if($._data(field).events.hide){
      hideListener = $._data(field).events.hide;
    }
    if($._data(field).events.remove){
      removeListener = $._data(field).events.remove;
    }
  }
      focusout = focusoutListener.length>0;
      hide = hideListener.length>0;
      remove = removeListener.length>0;
  if (registered) {
    expect(focusout).toBeTruthy();
    expect(hide).toBeTruthy();
    expect(remove).toBeTruthy();
  } else {
    expect(focusout).toBeFalsy();
    expect(hide).toBeFalsy();
    expect(remove).toBeFalsy();
  }
};
