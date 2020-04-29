/*
 * Copyright (c) 2014-2017 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */

/**
 * Calendar component as used in the list panel of the calendar.
 * Delegates most functions to the CalendarComponent instance used as source.
 * It's important we clean-up the registered listeners on the model-adapter, since
 * new instances of CalendarListComponent are created every time we click on a date
 * in the calendar, but the CalendarComponent instance is always the same.
 */
export default class CalendarListComponent {

  constructor(partDay, source) {
    this.partDay = partDay;
    this.source = source;
    this.$container = null;
    this._selectedListener = source.on('selected', event => {
      this.$container.toggleClass('comp-selected', event.selected);
    });
    this._removeListener = source.on('remove', this.remove.bind(this));
  }

  render($parent) {
    let source = this.source;
    this.$container = $parent
      .appendDiv('calendar-component')
      .data('partDay', this.partDay)
      .addClass(source.item.cssClass)
      .toggleClass('comp-selected', source._selected)
      .mousedown(this._onMouseDown.bind(this, source))
      .on('contextmenu', source._onContextMenu.bind(source));
    this.$container.appendDiv('calendar-component-leftcolorborder');
    this.$container.appendDiv('content')
      .html(source._description());
  }

  /**
   * Prevent list-component from gaining focus (*1). Since the component is removed/rendered
   * after the click the focus would be on the body afterwards #222862.
   */
  _onMouseDown(source, event) {
    event.preventDefault(); // *1
    let $part = $(event.delegateTarget);
    source.updateSelectedComponent($part, true);
  }

  remove() {
    this.source.removeListener(this._selectedListener);
    this.source.removeListener(this._removeListener);
    this.$container.remove();
  }
}
