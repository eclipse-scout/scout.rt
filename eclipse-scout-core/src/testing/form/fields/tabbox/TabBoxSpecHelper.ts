/*
 * Copyright (c) 2010-2020 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
import $ from 'jquery';
import {scout, Session, TabBox, TabBoxModel, TabItem} from '../../../../index';
import {Optional} from '../../../../types';

export default class TabBoxSpecHelper {
  session: Session;

  constructor(session: Session) {
    this.session = session;
  }

  createTabBoxWith2Tabs(model: TabBoxModel): TabBox {
    model = $.extend({
      tabItems: [{
        objectType: TabItem,
        label: 'first'
      }, {
        objectType: TabItem,
        label: 'second'
      }]
    }, model);
    return this.createTabBox(model);
  }

  createTabBoxWith(tabItems: TabItem[]): TabBox {
    tabItems = scout.nvl(tabItems, []);
    return this.createTabBox({
      tabItems: tabItems,
      selectedTab: tabItems[0]
    });
  }

  createTabBox(model?: Optional<TabBoxModel, 'parent'>): TabBox {
    model = $.extend({
      parent: this.session.desktop
    }, model);

    return scout.create(TabBox, model as TabBoxModel);
  }

  createTabItem(model?: TabBoxModel): TabItem {
    model = $.extend({
      parent: this.session.desktop
    }, model);
    return scout.create(TabItem, model);
  }
}
