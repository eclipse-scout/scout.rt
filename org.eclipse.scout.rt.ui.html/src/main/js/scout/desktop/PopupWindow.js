scout.PopupWindow = function(myWindow, form) { // use 'myWindow' in place of 'window' to prevent confusion with global window variable
  this.myWindow = myWindow;
  this.form = form;
  this.session = form.session;
  this.events = new scout.EventSupport();
  this.$container;
  this.htmlComp;

  // link Form instance with this popupWindow instance
  // this is required when form (and popup-window) is closed by the model
  form.popupWindow = this;

  // link Window instance with this popupWindow instance
  // this is required when we want to check if a certain DOM element belongs
  // to a popup window
  myWindow.popupWindow = this;
};

scout.PopupWindow.prototype._onUnload = function() {
  $.log.debug('stored form ID ' + this.form.id + ' to session storage');
  if (this.form.destroyed) {
    $.log.debug('form ID ' + this.form.id + ' is already destroyed - don\'t trigger unload event');
  } else {
    this.events.trigger('popupWindowUnload', this);
  }
};

scout.PopupWindow.prototype._onReady = function() {
  // set container (used as document-root from callers)
  var scoutElement = this.myWindow.document.getElementsByClassName('scout')[0];
  this.$container = $(scoutElement);
  this.htmlComp = new scout.HtmlComponent(this.$container, this.session);
  this.htmlComp.setLayout(new scout.SingleLayout());

  $(this.myWindow)
    .on('unload', this._onUnload.bind(this))
    .on('resize', this._onResize.bind(this));

  this.$container.height(600);

  // in case window has been reloaded
  if (this.form.rendered) {
    this.form.remove();
  }
  this.form.render(this.$container);
  this.form.htmlComp.validateLayout();
};

// FIXME AWE: (2nd screen) sollen wir auch position-changes vom window registrieren (ohne size-change)?
// Falls ja, bräuchte es das hier: http://stackoverflow.com/questions/4319487/detecting-if-the-browser-window-is-moved-with-javascript
scout.PopupWindow.prototype._onResize = function() {
  var $myWindow = $(this.myWindow),
    width = $myWindow.width(),
    height = $myWindow.height(),
    left = this.myWindow.screenX,
    top = this.myWindow.screenY;
  $.log.debug('popup-window resize: width=' + width + ' height=' + height + ' top=' + top + ' left=' + left);

  // store window bounds by class ID
  scout.PopupWindow.storeWindowBounds(this.form, new scout.Rectangle(left, top, width, height));

  var $parent = this.$container.parent();
  var parentSize = new scout.Dimension($parent.width(), $parent.height());
  this.htmlComp.setSize(parentSize);
};

scout.PopupWindow.storeWindowBounds = function(form, bounds) {
  var storageKey = 'formBounds-' + form.classId;
  window.localStorage[storageKey] = JSON.stringify(bounds);
};

scout.PopupWindow.readWindowBounds = function(form) {
  var storageKey = 'formBounds-' + form.classId,
    bounds = window.localStorage[storageKey];
  if (!bounds) {
    return null;
  }
  bounds = JSON.parse(bounds);
  return new scout.Rectangle(bounds.x, bounds.y, bounds.width, bounds.height);
};

scout.PopupWindow.prototype.isClosed = function() {
  return this.myWindow.closed;
};

scout.PopupWindow.prototype.close = function() {
  this.myWindow.close();
};

scout.PopupWindow.prototype.title = function(title) {
  this.myWindow.document.title = title;
};

