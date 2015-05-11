scout.CalendarComponent = function() {
  scout.CalendarComponent.parent.call(this);

  this.selected = false;
  this._tooltip;
};
scout.inherits(scout.CalendarComponent, scout.ModelAdapter);

scout.CalendarComponent.prototype._render = function($parent) {

  $.log.debug('(CalendarComponent#_render)');

  this.$container = $parent.appendDiv('calendar-component')
    .html('<b>' + this.item.subject + '</b>')
    .addClass(this.item.cssClass)
    .data('component', this)
    .mousedown(this._onMousedown.bind(this))
    .mouseenter(this._onHoverIn.bind(this))
    .mouseleave(this._onHoverOut.bind(this))
    .on('contextmenu', this._onContextMenu.bind(this));
};

scout.CalendarComponent.prototype._renderProperties = function() {
  this._renderSelected();
};

scout.CalendarComponent.prototype._renderSelected = function() {
  // the rendered check is required because the selected component may be
  // off-screen. in that case it is not rendered.
  if (this.rendered) {
    this.$container.toggleClass('comp-selected', this.selected);
  }
};

scout.CalendarComponent.prototype.setSelected = function(selected) {
  this.selected = selected;
  this._renderSelected();
};

scout.CalendarComponent.prototype._onMousedown = function(event) {
  this.parent._selectedComponentChanged(this);
};

scout.CalendarComponent.prototype._onContextMenu = function(event) {
  this._showContextMenu(event, 'Calendar.CalendarComponent');
};

scout.CalendarComponent.prototype._showContextMenu = function(event, allowedType) {
  event.preventDefault();
  event.stopPropagation();
  var filteredMenus = scout.menus.filter(this.menus, [allowedType]),
  popup = new scout.ContextMenuPopup(this.session, filteredMenus),
    $comp = $(event.currentTarget),
    x = event.pageX,
    y = event.pageY;
  popup.$anchor = $comp;
  popup.render();
  popup.setLocation(new scout.Point(x, y));
};

/**
 * Show tooltip with delay, so user is not flooded with tooltips when filled with many items.
 * Because of the asynchronous nature of the Calendar,
 */
scout.CalendarComponent.prototype._onHoverIn = function(event) {
  this._tooltipDelay = setTimeout(function() {
    this._tooltip = new scout.Tooltip({
      text: this._description(),
      $anchor: this.$container,
      arrowPosition: 15,
      arrowPositionUnit: '%',
      htmlEnabled: true
    });
    this._tooltip.render();
  }.bind(this), 350);
};

// FIXME AWE: remove tooltip when component is removed/disposed!

scout.CalendarComponent.prototype._onHoverOut = function(event) {
  clearTimeout(this._tooltipDelay);
  if (this._tooltip) {
    this._tooltip.remove();
    this._tooltip = null;
  }
};

scout.CalendarComponent.prototype._format = function(date, pattern) {
  return scout.dates.format(date, this.session.locale, pattern);
};

scout.CalendarComponent.prototype._description = function() {
  var range,
    fromDate = scout.dates.parseJsonDate(this.fromDate),
    toDate = scout.dates.parseJsonDate(this.toDate);

  // find time range
  if (this.fullDay) {
    range = '';
  } else if (scout.dates.isSameDay(fromDate, toDate)) {
    range = 'von ' + this._format(fromDate, 'HH:mm') + ' bis ' + this._format(fromDate, 'HH:mm') + '<br>';
  } else {
    range = range = 'von ' + this._format(fromDate, 'EEEE HH:mm ') + ' bis ' + this._format(toDate, ' EEEE HH:mm') + '<br>';
  }

  // compose text
  return '<b>' + this.item.subject + '</b><br>' + range + this.item.body;
};
