// SCOUT GUI
// (c) Copyright 2013-2014, BSI Business Systems Integration AG

scout.Desktop = function(session, $parent, model) {
  this.session = session;
  this.tree;
  this._$parent = $parent;
  this.session.widgetMap[model.id] = this;

  //  this.$entryPoint.addClass('desktop'); //FIXME desktop elements use ids, maybe better change to class to support multiple session divs with multiple desktops

  var view,tool,tree;
  // create all 4 containers
  if (model.viewButtons) {
    view = new scout.DesktopViewButtonBar(this.session, $parent, model.viewButtons);
  }
  if (model.toolButtons) {
    tool = new scout.DesktopToolButton(this.session, $parent, model.toolButtons);
  }
  if (model.outline) {
    tree = new scout.DesktopTreeContainer(this.session, $parent, model.outline);
  }
  if (view || tool || tree) {
    scout.keystrokeManager.addAdapter(new scout.DesktopKeystrokeAdapter(view, tool, tree));
  }

  var bench = new scout.DesktopBench(this.session, $parent);
  this._bench = bench;

  if(tree) {
    this.tree = tree;
    this.tree.attachModel();
  }
};

scout.Desktop.prototype.onModelPropertyChange = function() {};

scout.Desktop.prototype.onModelCreate = function(event) {
  if (event.objectType == "Outline") {
    this.tree.onOutlineCreated(event);
  } else if (event.objectType == "Form") {
    if (event.displayHint == "view") {
      //FIXME separate into View and Dialog which use Form?
      new scout.Form(this.session, this._bench.$container, event);
    } else if (event.displayHint == "dialog") {
      new scout.Form(this.session, this._$parent, event);
    } else {
      $.log("Form displayHint not handled: '" + event.displayHint + "'.");
    }
  } else {
    $.log("Widget creation not handled for object type '" + event.objectType + "'.");
  }
};

scout.Desktop.prototype.onModelAction = function(event) {
  if (event.type_ == 'outlineChanged') {
    this.tree.onOutlineChanged(event.outlineId);
  } else if (event.type_ == 'formRemoved') {
    var form = this.session.widgetMap[event.formId];
    if (form) {
      form.hide();
    }
  } else {
    $.log("Model event not handled. Widget: Desktop. Event: " + event.type_ + ".");
  }
};
