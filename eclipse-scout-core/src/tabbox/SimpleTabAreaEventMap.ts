/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {Event, PropertyChangeEvent, SimpleTab, SimpleTabArea, SimpleTabAreaDisplayStyle, SimpleTabView, WidgetEventMap} from '../index';

export interface SimpleTabAreaTabSelectEvent<TView extends SimpleTabView = SimpleTabView, S extends SimpleTabArea<TView> = SimpleTabArea<TView>> extends Event<S> {
  viewTab: SimpleTab<TView>;
}

export interface SimpleTabAreaEventMap<TView extends SimpleTabView = SimpleTabView> extends WidgetEventMap {
  'tabSelect': SimpleTabAreaTabSelectEvent<TView>;
  'propertyChange:displayStyle': PropertyChangeEvent<SimpleTabAreaDisplayStyle>;
}
