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
import {arrays, Calendar, Menu, scout} from '../index';

export default class CalendarModesMenu extends Menu {

  constructor() {
    super();
    this.cssClass = 'calendar-mode-menu';
    this.displayMode;
  }

  _init(model) {
    super._init(model);
    this.calendar = this.parent;
    let menusToAdd = [];
    menusToAdd.push(scout.create('CalendarModeMenu', {
      parent: this,
      displayMode: Calendar.DisplayMode.DAY,
      text: this.session.text('ui.CalendarDay')
    }));
    menusToAdd.push(scout.create('CalendarModeMenu', {
      parent: this,
      displayMode: Calendar.DisplayMode.WORK_WEEK,
      text: this.session.text('ui.CalendarWorkWeek')
    }));
    menusToAdd.push(scout.create('CalendarModeMenu', {
      parent: this,
      displayMode: Calendar.DisplayMode.WEEK,
      text: this.session.text('ui.CalendarWeek')
    }));
    menusToAdd.push(scout.create('CalendarModeMenu', {
      parent: this,
      displayMode: Calendar.DisplayMode.MONTH,
      text: this.session.text('ui.CalendarMonth')
    }));
    this.addChildActions(menusToAdd);

    this.childActions.forEach(function(menu) {
      menu.on('propertyChange', this._onMenuPropertyChange.bind(this));
    }, this);

    this._setDisplayMode(this.displayMode);
  }

  setDisplayMode(displayMode) {
    this.setProperty('displayMode', displayMode);
  }

  _setDisplayMode(displayMode) {
    this._setProperty('displayMode', displayMode);
    let menu = this.getMenuForMode(this.displayMode);
    menu.setSelected(true);
    this.setText(menu.text);
  }

  getMenuForMode(displayMode) {
    return arrays.find(this.childActions, menu => {
      return menu.displayMode === displayMode;
    });
  }

  _onMenuPropertyChange(event) {
    if (event.propertyName === 'selected') {
      let selected = event.newValue;
      if (selected) {
        let displayMode = event.source.displayMode;
        this.setDisplayMode(displayMode);
        this.calendar.setDisplayMode(displayMode);

        // unselect other menu items
        this.childActions.forEach(menu => {
          if (menu !== event.source) {
            menu.setSelected(false);
          }
        }, this);
      }
    }
  }
}
