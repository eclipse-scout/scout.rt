/**
 * Wrapper for a JQuery selector. Used as replacement for javax.swing.JComponent.
 */
scout.HtmlComponent = function($comp) {
  this.$comp = $comp;
  this.layout;
  this.layoutData;
  // link DOM element with this instance
  $comp.data('htmlComponent', this);
};

scout.HtmlComponent.get = function($comp) {
  var htmlComponent = $comp.data('htmlComponent');
  if (!htmlComponent) {
    throw 'data "htmlComponent" is undefined';
  }
  return htmlComponent;
};

scout.HtmlComponent.create = function($comp, model) {
  var htmlComponent = new scout.HtmlComponent($comp);
  htmlComponent.layout = new scout.LogicalGridData(model);
  return htmlComponent;
};

scout.HtmlComponent.prototype.layout = function() {
  if (!this.layout) {
    throw 'Tried to layout component ' + this.debug() +' but component has no layout';
  }
  this.layout.layout(this.$comp);
};

// TODO AWE: (layout) merge with Layout. static class, check consequences first