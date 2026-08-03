/*
 * Copyright (c) 2010, 2026 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {LoadingSupport, LoadingSupportOptions, PropertyChangeEvent, Table} from '../index';

export class TableLoadingSupport extends LoadingSupport {

  declare widget: Table;

  protected _busy = false;
  protected _tableAsyncLoadingChangeHandler = this._onTableAsyncLoadingChange.bind(this);
  protected _tableRemoveHandler = this._onTableRemove.bind(this);

  constructor(options: LoadingSupportOptions) {
    super(options);

    this.widget.on('propertyChange:asyncLoading', this._tableAsyncLoadingChangeHandler);
    this.widget.on('remove', this._tableRemoveHandler);
    this.widget.one('destroy', e => {
      this.widget.off('propertyChange:asyncLoading', this._tableAsyncLoadingChangeHandler);
      this.widget.off('remove', this._tableRemoveHandler);
    });
  }

  protected override _renderLoadingIndicator() {
    // only render the loading indicator if table supports async loading
    if (this.widget.asyncLoading) {
      super._renderLoadingIndicator();
    }

    // update busy indicator
    this._updateDesktopBusy();
  }

  protected override _removeLoadingIndicator() {
    // remove loading indicator and update busy indicator
    super._removeLoadingIndicator();
    this._updateDesktopBusy();
  }

  protected _onTableAsyncLoadingChange(event: PropertyChangeEvent<boolean>) {
    // remove loading indicator if there is one and render a new one
    this._removeLoadingIndicator();
    this.renderLoading(true);
  }

  protected _onTableRemove() {
    // remove busy indicator, this can not be done in remove() as it is also asynchronously triggered by _removeLoadingIndicator()
    this._setDesktopBusy(false);
  }

  protected _updateDesktopBusy() {
    // mark desktop busy if table does not support asyncLoading and is loading
    this._setDesktopBusy(!this.widget.asyncLoading && this.widget.isLoading());
  }

  protected _setDesktopBusy(busy: boolean) {
    // check and update _busy to ensure Desktop.setBusy is not called multiple times with the same value
    if (this._busy === busy) {
      return;
    }
    this._busy = busy;

    // set desktop busy, use same delay as Session and abort-properties from loading support
    this.widget.session.desktop.setBusy({
      busy,
      renderDelay: 500, // longer delay as otherwise the cursor flickers on every backend call
      busyIndicatorModel: {
        cancellable: this.abortable
      },
      onCancel: this.abortHandler
    });
  }
}
