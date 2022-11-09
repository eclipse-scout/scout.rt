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
import {Event, PropertyChangeEvent, SimpleTab, SimpleTabArea, SimpleTabAreaDisplayStyle, SimpleTabView, WidgetEventMap} from '../index';

export interface SimpleTabAreaTabSelectEvent<TView extends SimpleTabView = SimpleTabView, S extends SimpleTabArea<TView> = SimpleTabArea<TView>> extends Event<S> {
  viewTab: SimpleTab<TView>;
}

export interface SimpleTabAreaEventMap<TView extends SimpleTabView = SimpleTabView> extends WidgetEventMap {
  'tabSelect': SimpleTabAreaTabSelectEvent<TView>;
  'propertyChange:displayStyle': PropertyChangeEvent<SimpleTabAreaDisplayStyle>;
}
