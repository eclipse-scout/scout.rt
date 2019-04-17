export default class DeferredGlassPaneTarget {

  constructor() {
  }

  static createFor(widget, findGlassPaneTargets) {
    if (widget.rendered) {
      throw new Error('Don\'t call this function if widget is already rendered.');
    }

    var deferred = new DeferredGlassPaneTarget();
    var renderedHandler = function(event) {
      var elements = findGlassPaneTargets();
      deferred.ready(elements);
    };

    widget.one('render', renderedHandler);
    widget.one('destroy', function() {
      widget.off('render', renderedHandler);
    }.bind(widget));
    return [deferred];
  };

}
