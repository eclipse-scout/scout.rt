// SCOUT GUI
// (c) Copyright 2013-2014, BSI Business Systems Integration AG

Scout.Session = function ($entryPoint, sessionPartId) {
  this.widgetMap = {};

  // server communication, data is optional
  this.syncAjax = function (eventType, id, event) {
    if(!event){
      event={};
    }
    event.type_ = eventType;
    event.id = id;
    event.sessionPartId = sessionPartId;
    var url = 'http://localhost:8082/json'; // TODO URL anpassen
    var ret;
    $.ajax({
      async : false,
      type : "POST",
      dataType : "json",
      cache : false,
      url : url,
      data : JSON.stringify(event),
      success : function (message) {ret = message; }
    });
    return ret;
  };

  this.processEvents = function processEvents(events) {
    var scout=this;
    $.each(events, function (index, event) {
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
    });
  };

  this.init = function init() {
    // create all widgets for entry point
    var response = this.syncAjax('startup', $entryPoint.attr('id'));
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
