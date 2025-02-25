/*
 * Copyright (c) 2010, 2025 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */

import {InitModelOf, Menu, MenuOwner, Page, scout} from '../../../index';

export abstract class PageDetailMenuContributor {
  declare model: PageDetailMenuContributorModel;
  page: Page;

  init(model: InitModelOf<PageDetailMenuContributor>) {
    scout.assertProperty(model, 'page', Page);
    this.page = model.page;
  }

  abstract contribute(originalMenus: Menu[], detailContent: MenuOwner);
}

export interface PageDetailMenuContributorModel {
  page: Page;
}
