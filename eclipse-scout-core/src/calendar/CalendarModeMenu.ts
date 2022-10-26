/*
 * Copyright (c) 2010-2022 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
import {CalendarModeMenuModel, Menu} from '../index';
import {CalendarDisplayMode} from './Calendar';

export default class CalendarModeMenu extends Menu implements CalendarModeMenuModel {
  declare model: CalendarModeMenuModel;

  displayMode: CalendarDisplayMode;

  constructor() {
    super();
  }

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
