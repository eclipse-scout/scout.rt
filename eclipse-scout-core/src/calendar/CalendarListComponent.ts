/*
 * Copyright (c) 2010, 2024 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */

import {CalendarComponent, EventListener, PropertyChangeEvent} from '../index';

/**
 * Calendar component as used in the list panel of the calendar.
 * Delegates most functions to the CalendarComponent instance used as source.
 * It's important we clean-up the registered listeners on the model-adapter, since
 * new instances of CalendarListComponent are created every time we click on a date
 * in the calendar, but the CalendarComponent instance is always the same.
 */
export class CalendarListComponent {
  partDay: Date;
  source: CalendarComponent;
  $container: JQuery;
  protected _selectedListener: EventListener;
  protected _removeListener: EventListener;

  constructor(partDay: Date, source: CalendarComponent) {
    this.partDay = partDay;
    this.source = source;
    this.$container = null;
    this._selectedListener = source.on('propertyChange:selected', (event: PropertyChangeEvent<boolean>) => {
      this.$container.toggleClass('comp-selected', event.newValue);
    });
    this._removeListener = source.on('remove', this.remove.bind(this));
  }

  render($parent: JQuery) {
    let source = this.source;
    let resource = source.parent.findResourceForComponent(source);
    this.$container = $parent
      .appendDiv('calendar-component')
      .data('partDay', this.partDay)
      .addClass(source.item.cssClass)
      .addClass(resource ? resource.cssClass : null)
      .toggleClass('comp-selected', source.selected)
      .on('mousedown', this._onMouseDown.bind(this, source))
      .on('contextmenu', source._onContextMenu.bind(source));
    this.$container.appendDiv('calendar-component-leftcolorborder');
    this.$container.appendDiv('content')
      .html(source.description(false));
  }

  /**
   * Prevent list-component from gaining focus (*1). Since the component is removed/rendered
   * after the click the focus would be on the body afterwards #222862.
   */
  protected _onMouseDown(source: CalendarComponent, event: JQuery.MouseDownEvent) {
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
