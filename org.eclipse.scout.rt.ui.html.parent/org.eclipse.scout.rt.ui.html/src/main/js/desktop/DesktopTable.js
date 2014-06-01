// SCOUT GUI
// (c) Copyright 2013-2014, BSI Business Systems Integration AG

/**
 * @param model represents a node page of an outline
 */
scout.DesktopTable = function(model, session) {
  this.model = model;
  this.session = session;

  this._$infoSelect;
  this._$infoFilter;
  this._$infoLoad;
  this._chart;
  this._map;
  this._graph;

  if (session && model) {
    this.session.widgetMap[model.id] = this;
  }

  this.table = this.session.objectFactory.create(this.model.table);
};

scout.DesktopTable.EVENT_MAP_LOADED = 'mapLoaded';
scout.DesktopTable.EVENT_GRAPH_LOADED = 'graphLoaded';

scout.DesktopTable.prototype._render = function($parent) {
  this._$parent = $parent;
  this.table.attach(this._$parent);

  var $groupSearch = this.table.footer.addGroup('Suche');

  // TODO cru > cgu: wenn suche vorhanden dann im modell übergeben
  var queryControl = {
    cssClass: 'control-query',
    action: controlQuery
  };
  if (true) {
    queryControl.label = 'Abfage';
    queryControl.enabled = true;
  }
  this.table.footer.addControl($groupSearch, queryControl);

  // TODO cru > cgu: wenn erweiterte suche vorhanden dann im modell übergeben
  var analyzeControl = {
    cssClass: 'control-analyze',
    action: controlAnalyze
  };
  if (true) {
    analyzeControl.label = 'Analyse';
    analyzeControl.enabled = true;
  }
  this.table.footer.addControl($groupSearch, analyzeControl);

  // TODO cru > cgu: wenn kategorien vorhanden dann im modell übergeben
  var categoryControl = {
    cssClass: 'control-category',
    action: controlCategory
  };
  if (true) {
    categoryControl.label = 'Kategorien';
    categoryControl.enabled = true;
  }
  this.table.footer.addControl($groupSearch, categoryControl);

  var $groupVisual = this.table.footer.addGroup('Anzeige');

  var chartControl = {
    cssClass: 'control-chart',
    action: controlChart
  };
  if (this.model.chart) {
    chartControl.label = this.model.chart.label;
    chartControl.enabled = true;
  }
  this.table.footer.addControl($groupVisual, chartControl);

  var mapControl = {
    cssClass: 'control-map',
    action: controlMap
  };
  if (this.model.map) {
    mapControl.label = this.model.map.label;
    mapControl.enabled = true;
  }
  this.table.footer.addControl($groupVisual, mapControl);

  var graphControl = {
    cssClass: 'control-graph',
    action: controlGraph
  };
  if (this.model.graph) {
    graphControl.label = this.model.graph.label;
    graphControl.enabled = true;
  }
  this.table.footer.addControl($groupVisual, graphControl);

  var that = this;

  function controlQuery() {
    // TODO cru > cgu: korrekt modell übergeben, zwischenspeichern
    that._query = new scout.DesktopQuery(that.table.footer.$controlContainer);
    that.table.footer.openTableControl();
  }

  function controlAnalyze() {
    // TODO cru > cgu: korrekt modell übergeben, zwischenspeichern
    that._analyze = new scout.DesktopAnalyze(that.table.footer.$controlContainer);
    that.table.footer.openTableControl();
  }

  function controlCategory() {
    // TODO cru > cgu: korrekt modell übergeben, zwischenspeichern
    that._category = new scout.DesktopCategory(that.table.footer.$controlContainer);
    that.table.footer.openTableControl();
  }

  function controlChart() {
    if (that._chart) { //FIXME filter selection should be restored when changing from map to chart and back, maybe don't dispose every time?
      that._chart.dispose();
    }
    that._chart = new scout.DesktopChart(that.table.footer.$controlContainer, that.table, that.model.chart, that.session);

    that.table.footer.openTableControl();
  }

  function controlGraph() {
    that.session.send('graph', that.model.outlineId, {
      "nodeId": that.model.id
    });
  }

  function controlMap() {
    that.session.send('map', that.model.outlineId, {
      "nodeId": that.model.id
    });
  }

};

scout.DesktopTable.prototype.detach = function() {
  this.table.detach();
};

scout.DesktopTable.prototype.attach = function($parent) {
  if (!this.table.$container) {
    this._render($parent);
  } else {
    this.table.attach($parent);
  }
};

scout.DesktopTable.prototype.onModelAction = function(event) {
  if (event.type_ == scout.DesktopTable.EVENT_MAP_LOADED) {
    //enrich with label
    $.extend(event.map, this.model.map);

    if (this._map) {
      this._map.dispose();
    }
    this._map = new scout.DesktopMap(this.table.footer.$controlContainer, this.table, event.map);
    this.table.footer.openTableControl();
  } else if (event.type_ == scout.DesktopTable.EVENT_GRAPH_LOADED) {
    //enrich with label
    $.extend(event.graph, this.model.graph);

    this._graph = new scout.DesktopGraph(this.table.footer.$controlContainer, event.graph);
    this.table.footer.openTableControl();
  } else {
    $.log("Model event not handled. Widget: scout.DesktopTable. Event: " + event.type_ + ".");
  }
};
