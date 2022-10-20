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
import {Menu} from '../index';

export default class CalendarModeMenu extends Menu {

  constructor() {
    super();
  }

  doAction() {
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
