/*
 * Copyright (c) 2010, 2025 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {PropertyChangeEvent, Widget, WidgetEventMap, WidgetModel} from '../../index';

export class SearchState extends Widget implements SearchStateModel {
  declare model: SearchStateModel;
  declare eventMap: SearchStateEventMap;
  declare self: SearchState;

  resultCount = 0;
  limited = false;
  pending = true;

  setResultCount(resultCount: number) {
    this.setProperty('resultCount', resultCount);
  }

  setLimited(limited: boolean) {
    this.setProperty('limited', limited);
  }

  setPending(pending: boolean) {
    this.setProperty('pending', pending);
  }
}

export interface SearchStateModel extends WidgetModel {
  resultCount?: number;
  limited?: boolean;
  pending?: boolean;
}

export interface SearchStateEventMap extends WidgetEventMap {
  'propertyChange:limited': PropertyChangeEvent<boolean>;
  'propertyChange:pending': PropertyChangeEvent<boolean>;
  'propertyChange:resultCount': PropertyChangeEvent<number>;
}
