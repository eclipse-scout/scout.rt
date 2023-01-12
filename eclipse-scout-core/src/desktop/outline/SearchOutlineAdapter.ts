/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {Event, OutlineAdapter, SearchOutline, SearchOutlineModel, SearchOutlineSearchEvent} from '../../index';

export class SearchOutlineAdapter extends OutlineAdapter {
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
