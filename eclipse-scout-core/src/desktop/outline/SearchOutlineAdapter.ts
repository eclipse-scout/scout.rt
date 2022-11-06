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
import {Event, OutlineAdapter, SearchOutline, SearchOutlineModel} from '../../index';
import {SearchOutlineSearchEvent} from './SearchOutlineEventMap';

export default class SearchOutlineAdapter extends OutlineAdapter {
  declare widget: SearchOutline;

  protected override _initProperties(model: SearchOutlineModel & { requestFocusQueryField?: boolean }) {
    if (model.requestFocusQueryField !== undefined) {
      // ignore pseudo property initially (to prevent the function SearchOutlineAdapter#requestFocusQueryField() to be replaced)
      delete model.requestFocusQueryField;
    }
  }

  protected _syncRequestFocusQueryField() {
    this.widget.focusQueryField();
  }

  protected _onWidgetSearch(event: SearchOutlineSearchEvent) {
    this._send('search', {
      query: event.query
    }, {
      showBusyIndicator: false
    });
  }

  protected override _onWidgetEvent(event: Event<SearchOutline>) {
    if (event.type === 'search') {
      this._onWidgetSearch(event as SearchOutlineSearchEvent);
    } else {
      super._onWidgetEvent(event);
    }
  }
}
