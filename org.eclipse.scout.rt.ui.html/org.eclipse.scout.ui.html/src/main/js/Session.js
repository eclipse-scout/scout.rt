// SCOUT GUI
// (c) Copyright 2013-2014, BSI Business Systems Integration AG

Scout.Session = function ($entryPoint, sessionPartId) {
  this.widgetMap = {};
  this.locale;
  this.$entryPoint = $entryPoint;

  //FIXME maybe better separate session object from event processing, create ClientSession.js?
  this.widgetMap[sessionPartId] = this;

  this.sendSync = function (type, id, data) {
    return this.send(type, id, data, false);
  };

  this.send = function (type, id, data, async) {
    return this.sendEvents([new Scout.Event(type, id, data)], async);
  };

  this.sendEventsSync = function (events) {
    return this.sendEventsSync(events, false);
  };

  this.sendEvents = function (events, async) {
    if(async === undefined) {
      async = true;
    }
    var request={
      events : events,
      sessionPartId : sessionPartId
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

  this.processEvents = function processEvents(events) {
    var scout=this;
    for(var i=0; i < events.length; i++) {
      var event = events[i];
      if(event.type_ == "create") {
        var widget = scout.widgetMap[event.parentId];
        if(widget) {
          widget.onModelCreate(event);
        }
        else{
          log("ERROR: No widget found for parentId " + event.parentId);
        }
      }
      else if(event.type_ == "propertyChange") {
        var widget = scout.widgetMap[event.id];
        if(widget) {
            widget.onModelPropertyChange(event);
        }
        else{
          log("ERROR: No widget found for id " + event.id);
        }
      }
      else {
        var widget = scout.widgetMap[event.id];
        if(widget) {
            widget.onModelAction(event);
        }
        else{
          log("ERROR: No widget found for id " + event.id);
        }
      }
    }
  };

  this.init = function init() {
    // create all widgets for entry point
    this.send('startup', sessionPartId);
  };
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
