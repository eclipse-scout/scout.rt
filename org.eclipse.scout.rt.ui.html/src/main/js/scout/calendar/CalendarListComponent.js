/*******************************************************************************
 * Copyright (c) 2014-2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
/**
 * Calendar component as used in the list panel of the calendar.
 * Delegates most functions to the scout.CalendarComponent instance used as source.
 * It's important we clean-up the registered listeners on the model-adapter, since
 * new instances of CalendarListComponent are created every time we click on a date
 * in the calendar, but the CalendarComponent instance is always the same.
 */
scout.CalendarListComponent = function(partDay, source) {
  this.partDay = partDay;
  this.source = source;
  this.$container;
  this._selectedListener = source.on('selected', function(event) {
    this.$container.toggleClass('comp-selected', event.selected);
  }.bind(this));
  this._removeListener = source.on('remove', this.remove.bind(this));
};

scout.CalendarListComponent.prototype.render = function($parent) {
  var source = this.source;
  this.$container = $parent
    .appendDiv('calendar-component')
    .data('partDay', this.partDay)
    .addClass(source.item.cssClass)
    .toggleClass('comp-selected', source._selected)
    .mousedown(source._onMousedown.bind(source))
    .on('contextmenu', source._onContextMenu.bind(source))
    .html(source._description());
};

scout.CalendarListComponent.prototype.remove = function() {
  this.source.removeListener(this._selectedListener);
  this.source.removeListener(this._removeListener);
  this.$container.remove();
};
