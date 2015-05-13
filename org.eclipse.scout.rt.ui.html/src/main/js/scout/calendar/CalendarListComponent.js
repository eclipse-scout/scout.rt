/**
 * Calendar component as used in the list panel of the calendar.
 * Delegates most functions to the scout.CalendarComponent instance used as source.
 * It's important we clean-up the registered listeners on the model-adapter, since
 * new instances of CalendarListComponent are created every time we click on a date
 * in the calendar, but the CalendarComponent instance is always the same.
 */
scout.CalendarListComponent = function(source) {
  this.source = source;
  this.$container;
  this._selectedListener = source.events.on('selected', function(event) {
    this.$container.toggleClass('comp-selected', event.selected);
  }.bind(this));
  this._removedListener = source.events.on('removed', this.remove.bind(this));
};

scout.CalendarListComponent.prototype.render = function($parent) {
  var source = this.source;
  this.$container = $parent
   .appendDiv('calendar-component')
   .addClass(source.item.cssClass)
   .toggleClass('comp-selected', source._selected)
   .mousedown(source._onMousedown.bind(source))
   .on('contextmenu', source._onContextMenu.bind(source))
   .html(source._description());
};

scout.CalendarListComponent.prototype.remove = function() {
  this.source.events.removeListener(this._selectedListener);
  this.source.events.removeListener(this._removedListener);
  this.$container.remove();
};
