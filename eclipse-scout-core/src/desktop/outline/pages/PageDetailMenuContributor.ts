/*
 * Copyright (c) 2010, 2025 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */

import {Menu, MenuOwner, ObjectWithType, Page, scout, Widget} from '../../../index';

/**
 * A page detail menu contributor can adapt the {@link Menu}s of a {@link Page.detailTable} or {@link Page.detailForm}.
 *
 * The contributor is called when:
 * - a detail table or form is initialized. Typically, this happens when a page is selected the first time.
 * - the detail form or table change dynamically (using {@link Page.setDetailForm} or {@link Page.setDetailTable}).
 * - the menus of the detail form or detail table change.
 *
 * It is guaranteed that a menu added by a contributor is added only once even if a contributor is called multiple times.
 */
export abstract class PageDetailMenuContributor implements ObjectWithType {
  page: Page;
  objectType: string;

  setPage(page: Page) {
    scout.assertParameter('page', page);
    this.page = page;
  }

  /**
   * Creates a list of menus that will be set to the detail content.
   *
   * - The original menus are completely replaced. If you would like to keep them, add the {@link originalMenus} to the resulting array.
   * - If you create new menus you likely want to use {@link detailContent} as parent.
   * - If you want to copy existing menus (e.g. from the parent page), consider using {@link _cloneMenus}.
   *
   * @param originalMenus the current menus of the detail content.
   *        Menus that are added by a contributor are excluded to ensure they won't be contributed multiple times if the contributor is called more than once.
   * @param detailContent the detail form or table that should receive the new menus.
   * @returns the menus that should be set to the detail content
   */
  abstract contribute(originalMenus: Menu[], detailContent: MenuOwner): Menu[];

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

    const clone = menu.clone({
      parent: parent,
      menuTypes: []
    }, {
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
