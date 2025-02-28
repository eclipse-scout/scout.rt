/*
 * Copyright (c) 2010, 2025 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */

import {InitModelOf, Menu, MenuOwner, Page, scout, Widget} from '../../../index';

export abstract class PageDetailMenuContributor {
  declare model: PageDetailMenuContributorModel;
  page: Page;

  init(model: InitModelOf<PageDetailMenuContributor>) {
    scout.assertProperty(model, 'page', Page);
    this.page = model.page;
  }

  abstract contribute(originalMenus: Menu[], detailContent: MenuOwner);

  /**
   * Clones the given menus including their children and attaches the clones to the given parent.
   */
  protected _cloneMenus(menus: Menu[], parent: Widget): Menu[] {
    if (!menus) {
      return null;
    }
    return menus.map(menu => this._cloneMenu(menu, parent));
  }

  protected _cloneMenu(menu: Menu, parent: Widget): Menu {
    if (!menu) {
      return null;
    }

    const clone = menu.clone(
      {
        parent: parent,
        menuTypes: []
      },
      {
        delegateEventsToOriginal: ['action'],
        delegateAllPropertiesToClone: true,
        excludePropertiesToClone: ['menuTypes', 'childActions']
      });

    if (menu.childActions?.length > 0) {
      clone.setChildActions(this._cloneMenus(menu.childActions, clone));
    }

    return clone;
  }
}

export interface PageDetailMenuContributorModel {
  page: Page;
}
