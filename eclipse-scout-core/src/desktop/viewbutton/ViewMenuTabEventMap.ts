/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {PropertyChangeEvent, ViewButton, WidgetEventMap} from '../../index';

export interface ViewMenuTabEventMap extends WidgetEventMap {
  'propertyChange:selected': PropertyChangeEvent<boolean>;
  'propertyChange:selectedButton': PropertyChangeEvent<ViewButton>;
  'propertyChange:selectedButtonVisible': PropertyChangeEvent<boolean>;
  'propertyChange:viewButtons': PropertyChangeEvent<ViewButton[]>;
}
