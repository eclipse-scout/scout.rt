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
import {Event, Form, Outline, OutlineOverview, Page, PropertyChangeEvent, TableRow, TableRowDetail, TreeEventMap} from '../../index';
import {OutlineContent} from '../bench/DesktopBench';

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

export default interface OutlineEventMap extends TreeEventMap {
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
