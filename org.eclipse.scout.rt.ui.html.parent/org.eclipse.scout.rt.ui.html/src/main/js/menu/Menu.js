scout.Menu = function(model, session) {
  scout.Menu.parent.call(this, model, session);
  this.owner;
};
scout.inherits(scout.Menu, scout.ModelAdapter);

scout.Menu.EVENT_ABOUT_TO_SHOW='aboutToShow';

scout.Menu.prototype.sendAboutToShow = function(event) {
  this.session.send(scout.Menu.EVENT_ABOUT_TO_SHOW, this.model.id);
};

scout.Menu.prototype.onModelPropertyChange = function(event) {
  if (event.hasOwnProperty('text')) {
    this.model.text = event.text;
  }
  if (event.hasOwnProperty('iconId')) {
    this.model.iconId = event.iconId;
  }
  if (event.hasOwnProperty('enabled')) {
    this.model.enabled = event.enabled;
  }
  if (event.hasOwnProperty('visible')) {
    this.model.visible = event.visible;
  }
  if(this.owner) {
    this.owner.onMenuPropertyChange(event);
  }
};
