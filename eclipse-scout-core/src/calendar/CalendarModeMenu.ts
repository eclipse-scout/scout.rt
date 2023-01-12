/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {CalendarDisplayMode, CalendarModeMenuModel, Menu} from '../index';

export class CalendarModeMenu extends Menu implements CalendarModeMenuModel {
  declare model: CalendarModeMenuModel;

  displayMode: CalendarDisplayMode;

  override doAction(): boolean {
    if (!this.prepareDoAction()) {
      return false;
    }

    // unselect is not possible
    this.setSelected(true);

    // close menu, cannot be done in parent menu itself because selecting an already selected item does not trigger an event
    this.parentMenu.setSelected(false);
    return true;
  }
}
