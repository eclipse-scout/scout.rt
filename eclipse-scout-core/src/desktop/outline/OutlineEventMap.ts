/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {Event, Form, Outline, OutlineContent, OutlineOverview, Page, PropertyChangeEvent, TableRow, TableRowDetail, TreeEventMap} from '../../index';

export interface OutlinePageChangedEvent<T = Outline> extends Event<T> {
  page: Page;
}

export interface OutlinePageInitEvent<T = Outline> extends Event<T> {
  page: Page;
}

export interface OutlinePageRowLinkEvent<T = Outline> extends Event<T> {
  page: Page;
  row: TableRow;
}

export interface OutlineEventMap extends TreeEventMap {
  'pageChanged': OutlinePageChangedEvent;
  'pageInit': OutlinePageInitEvent;
  'pageRowLink': OutlinePageRowLinkEvent;
  'propertyChange:compact': PropertyChangeEvent<boolean>;
  'propertyChange:defaultDetailForm': PropertyChangeEvent<Form>;
  'propertyChange:detailContent': PropertyChangeEvent<OutlineContent | TableRowDetail>;
  'propertyChange:detailMenuBarVisible': PropertyChangeEvent<boolean>;
  'propertyChange:embedDetailContent': PropertyChangeEvent<boolean>;
  'propertyChange:iconId': PropertyChangeEvent<string>;
  'propertyChange:iconVisible': PropertyChangeEvent<boolean>;
  'propertyChange:navigateButtonsVisible': PropertyChangeEvent<boolean>;
  'propertyChange:nodeMenuBarVisible': PropertyChangeEvent<boolean>;
  'propertyChange:outlineOverview': PropertyChangeEvent<OutlineOverview>;
  'propertyChange:outlineOverviewVisible': PropertyChangeEvent<boolean>;
  'propertyChange:views': PropertyChangeEvent<Form[]>;
}
