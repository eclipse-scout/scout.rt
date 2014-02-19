// SCOUT GUI 0.2
// (c) Copyright 2013-2014, BSI Business Systems Integration AG

Scout.Session = function ($entryPoint, sessionPartId) {
  this.widgetMap = {};

  // server communication, data is optional
  this.syncAjax = function (type, id, data) {
  var event={"type": type, "id": id, "sessionPartId": sessionPartId};
    if(data){
      event.data=data;
    }
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
      if(event.type == "create") {
        var widget = createWidget(scout, $entryPoint, event.data);
        scout.widgetMap[event.id] = widget;
      }
      else if(event.type == "update") {
        var widget = scout.widgetMap[event.id];
        if(widget) {
          updateWidget(scout, widget, event.data);
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
  function createWidget (scout, $parent, eventData) {
    if (eventData.id == "Desktop") {
      return new Scout.Desktop(scout, $parent, eventData);
    }
  }

  function updateWidget(scout, widget, eventData) {
    widget.handleUpdate(eventData);
  }
};
