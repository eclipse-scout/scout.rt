// SCOUT GUI
// (c) Copyright 2013-2014, BSI Business Systems Integration AG

Scout.Session = function ($entryPoint, sessionPartId) {
  this.widgetMap = {};

  this.send = function (type, id, data) {
    return this.sendEvents([new Scout.Event(type, id, data)]);
  };

  this.sendEvents = function (events) {
    var request={
      events : events,
      sessionPartId : sessionPartId
    };
    var url = 'json';
    var ret;
    $.ajax({
      async : false,
      type : "POST",
      dataType : "json",
      cache : false,
      url : url,
      data : JSON.stringify(request),
      success : function (message) {ret = message; }
    });
    return ret;
  };

  this.processEvents = function processEvents(events) {
    var scout=this;
    for(var i=0; i < events.length; i++) {
      var event = events[i];
      if(event.type_ == "create") {
        if(event.parentId === undefined){
          createTopLevelWidget(scout, $entryPoint, event);
        }
        else{
            var widget = scout.widgetMap[event.parentId];
            if(widget) {
                widget.onModelCreate(event);
            }
            else{
                log("ERROR: No widget found for parentId " + event.parentId);
            }
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
    var response = this.send('startup', $entryPoint.attr('id'));
    this.processEvents(response.events);
  };

  // create single widget based on a model object
  function createTopLevelWidget (scout, $parent, event) {
    if (event.objectType == "Desktop") {
      return new Scout.Desktop(scout, $parent, event);
    }
    return undefined;
  }

};
