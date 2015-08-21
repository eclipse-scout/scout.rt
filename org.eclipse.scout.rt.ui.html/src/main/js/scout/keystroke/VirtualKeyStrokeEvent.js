scout.VirtualKeyStrokeEvent = function(which, ctrl, alt, shift, target) {
  this.which = which;
  this.ctrlKey = ctrl;
  this.altKey = alt;
  this.shiftKey = shift;
  this.target = target;

  this._propagationStopped = false;
  this._immediatePropagationStopped = false;
  this._defaultPrevented = false;
};

scout.VirtualKeyStrokeEvent.prototype.stopPropagation = function() {
  this._propagationStopped = true;
};

scout.VirtualKeyStrokeEvent.prototype.stopImmediatePropagation = function() {
  this._immediatePropagationStopped = true;
};

scout.VirtualKeyStrokeEvent.prototype.preventDefault = function() {
  this._defaultPrevented = true;
};

scout.VirtualKeyStrokeEvent.prototype.isPropagationStopped = function() {
  return this._propagationStopped;
};

scout.VirtualKeyStrokeEvent.prototype.isImmediatePropagationStopped = function() {
  return this._immediatePropagationStopped;
};

scout.VirtualKeyStrokeEvent.prototype.isDefaultPrevented = function() {
  return this._defaultPrevented;
};

scout.VirtualKeyStrokeEvent.prototype.isAnyPropagationStopped = function() {
  return this._propagationStopped || this._immediatePropagationStopped;
};
