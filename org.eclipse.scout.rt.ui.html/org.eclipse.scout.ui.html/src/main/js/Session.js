// SCOUT GUI
// (c) Copyright 2013-2014, BSI Business Systems Integration AG

Scout.Session = function ($entryPoint, sessionPartId) {
  this.widgetMap = {};
  this.locale;
  this.$entryPoint = $entryPoint;
  this._asyncEvents = [];
  this._asyncRequestQueued;
  this._sessionPartId = sessionPartId;

  //FIXME maybe better separate session object from event processing, create ClientSession.js?
  this.widgetMap[sessionPartId] = this;
};

Scout.Session.prototype.sendSync = function (type, id, data) {
  return this.send(type, id, data, false);
};

/**
 * Async == true means: <br>
 * 1. the request is sent asynchronously and the response processed later<br>
 * 2. the request is sent delayed. If send is called multiple times during the same user interaction,
 *    the events are collected and sent in one request at the end of the user interaction
 * <p>
 * If async is set to false, the request will be sent immediately and the response returned.<br>
 * If there are queued async events then these events are sent right before sending the sync request
 */
Scout.Session.prototype.send = function (type, id, data, async) {
  if (async === undefined) {
    async = true;
  }

  if (async) {
    this._asyncEvents.push(new Scout.Event(type, id, data));
    if (!this._asyncRequestQueued) {

      var that = this;
      setTimeout(function() {
        that._sendNow(that._asyncEvents, true);
        that._asyncRequestQueued = false;
        that._asyncEvents = [];
      }, 0);

      this._asyncRequestQueued = true;
    }
  }
  else {
    //Before sending a sync request make sure the queued async request is executed before
    if (this._asyncRequestQueued) {
      var message = this._sendNow(this._asyncEvents, false);
      this.processEvents(message.events);
      this._asyncEvents = [];
    }

    var events = [new Scout.Event(type, id, data)];
    return this._sendNow(events, false);
  }
};

Scout.Session.prototype._sendNow = function (events, async) {
  var request = {
    events : events,
    sessionPartId : this._sessionPartId
  };

  var url = 'json';
  var ret;
  var that = this;
  $.ajax({
    async : async,
    type : "POST",
    dataType : "json",
    cache : false,
    url : url,
    data : JSON.stringify(request),
    success : function (message) {
      if (async) {
        that.processEvents(message.events);
      }
      else {
        ret = message;
      }
    }
  });
  return ret;
};

Scout.Session.prototype.processEvents = function (events) {
  var scout=this;
  for(var i=0; i < events.length; i++) {
    var event = events[i];
    if(event.type_ == "create") {
      var widget = scout.widgetMap[event.parentId];
      if(widget) {
        widget.onModelCreate(event);
      }
      else{
        throw "No widget found for parentId " + event.parentId;
      }
    }
    else if(event.type_ == "property") {
      var widget = scout.widgetMap[event.id];
      if(widget) {
          widget.onModelPropertyChange(event);
      }
      else{
        throw "No widget found for id " + event.id;
      }
    }
    else {
      var widget = scout.widgetMap[event.id];
      if(widget) {
          widget.onModelAction(event);
      }
      else{
        throw "No widget found for id " + event.id;
      }
    }
  }
};

Scout.Session.prototype.init = function () {
  // create all widgets for entry point
  this.send('startup', this._sessionPartId);
};

Scout.Session.prototype.onModelAction = function (event) {
  if (event.type_ == 'initialized') {
    this.locale = new Scout.Locale(event.locale);
    new Scout.Desktop(this, this.$entryPoint, event.desktop);
  }
  else if (event.type_ == 'localeChanged') {
    this.locale = new Scout.Locale(event);
    //FIXME inform components to reformat display text?
  }
};
