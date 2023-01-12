/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {arrays, Calendar, CalendarDisplayMode, CalendarModeMenu, CalendarModesMenuEventMap, CalendarModesMenuModel, InitModelOf, Menu, PropertyChangeEvent, scout} from '../index';

export class CalendarModesMenu extends Menu implements CalendarModesMenuModel {
  declare model: CalendarModesMenuModel;
  declare eventMap: CalendarModesMenuEventMap;
  declare self: CalendarModesMenu;
  declare parent: Calendar;
  declare childActions: CalendarModeMenu[];

  calendar: Calendar;
  displayMode: CalendarDisplayMode;

  constructor() {
    super();
    this.cssClass = 'calendar-mode-menu';
  }

  protected override _init(model: InitModelOf<this>) {
    super._init(model);
    this.calendar = this.parent;
    let menusToAdd: CalendarModeMenu[] = [];
    menusToAdd.push(scout.create(CalendarModeMenu, {
      parent: this,
      displayMode: Calendar.DisplayMode.DAY,
      text: this.session.text('ui.CalendarDay')
    }));
    menusToAdd.push(scout.create(CalendarModeMenu, {
      parent: this,
      displayMode: Calendar.DisplayMode.WORK_WEEK,
      text: this.session.text('ui.CalendarWorkWeek')
    }));
    menusToAdd.push(scout.create(CalendarModeMenu, {
      parent: this,
      displayMode: Calendar.DisplayMode.WEEK,
      text: this.session.text('ui.CalendarWeek')
    }));
    menusToAdd.push(scout.create(CalendarModeMenu, {
      parent: this,
      displayMode: Calendar.DisplayMode.MONTH,
      text: this.session.text('ui.CalendarMonth')
    }));
    this.insertChildActions(menusToAdd);

    this.childActions.forEach(menu => menu.on('propertyChange', this._onMenuPropertyChange.bind(this)));

    this._setDisplayMode(this.displayMode);
  }

  setDisplayMode(displayMode: CalendarDisplayMode) {
    this.setProperty('displayMode', displayMode);
  }

  protected _setDisplayMode(displayMode: CalendarDisplayMode) {
    this._setProperty('displayMode', displayMode);
    let menu = this.getMenuForMode(this.displayMode);
    menu.setSelected(true);
    this.setText(menu.text);
  }

  getMenuForMode(displayMode: CalendarDisplayMode): CalendarModeMenu {
    return arrays.find(this.childActions, menu => menu.displayMode === displayMode);
  }

  protected _onMenuPropertyChange(event: PropertyChangeEvent) {
    if (event.propertyName === 'selected') {
      let selectedEvent = event as PropertyChangeEvent<boolean>;
      let selected = selectedEvent.newValue;
      if (selected) {
        let source = selectedEvent.source as CalendarModeMenu;
        let displayMode = source.displayMode;
        this.setDisplayMode(displayMode);
        this.calendar.setDisplayMode(displayMode);

        // unselect other menu items
        this.childActions.forEach(menu => {
          if (menu !== source) {
            menu.setSelected(false);
          }
        });
      }
    }
  }
}
