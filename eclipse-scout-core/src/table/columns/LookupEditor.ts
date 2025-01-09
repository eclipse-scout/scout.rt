/*
 * Copyright (c) 2010, 2025 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {
  CellEditorPopup, CellEditorRenderedOptions, ChildModelOf, CodeType, Column, InitModelOf, ListBox, LookupBox, LookupCall, LookupEditorEventMap, LookupEditorModel, LookupEditorTreeLayout, Popup, PopupLayout, scout, scrollbars, SmartField,
  strings, Table, Tree, TreeBox, ValueField, WidgetPopup
} from '../../index';

export class LookupEditor<TValue> extends ValueField<TValue[]> implements LookupEditorModel<TValue> {
  declare model: LookupEditorModel<TValue>;
  declare eventMap: LookupEditorEventMap<TValue>;

  lookupCall: LookupCall<TValue> = null;
  codeType: string | (new() => CodeType<TValue>) = null;
  browseHierarchy = false;
  browseMaxRowCount = SmartField.DEFAULT_BROWSE_MAX_COUNT;

  protected _popup: WidgetPopup<LookupBox<TValue>> = null;

  protected override _init(model: InitModelOf<this>) {
    super._init(model);

    this.lookupCall = LookupCall.ensure(this.lookupCall, this.session);
    this.lookupCall?.setMaxRowCount(this.browseMaxRowCount);
    this.clearable = ValueField.Clearable.ALWAYS;
  }

  protected override _render() {
    super._render();
    this.$field.addClass('input-field focused');
  }

  protected override _formatValue(value: TValue[]): string | JQuery.Promise<string> {
    if (this._popup) {
      return this._popup.content.displayText;
    }
    return LookupEditor.formatValues(value, this.lookupCall, lookupCall => this.trigger('prepareLookupCall', {lookupCall}));
  }

  static formatValues<TValue>(values: TValue[], lookupCall: LookupCall<TValue>, prepareLookupCall?: (lookupCall: LookupCall<TValue>) => void): string | JQuery.Promise<string> {
    if (!values?.length || !lookupCall) {
      return '';
    }
    lookupCall = lookupCall.clone();
    prepareLookupCall?.(lookupCall);
    return lookupCall.textsByKeys(values)
      .then(result => strings.join(', ', ...Object.values(result)));
  }

  protected override _renderDisplayText() {
    super._renderDisplayText();
    this.$field.text(this.displayText || '');
  }

  protected override _clear() {
    this._popup?.widget('LookupBox', LookupBox<TValue>).setValue(null);
  }

  onCellEditorRendered(options: CellEditorRenderedOptions<TValue[]>) {
    this._openPopup(options.cellEditorPopup);
  }

  protected _openPopup(cellEditorPopup: CellEditorPopup<TValue[]>): void {
    this._popup = scout.create(WidgetPopup<LookupBox<TValue>>, {
      parent: this,
      anchor: this,
      horizontalAlignment: Popup.Alignment.LEFTEDGE,
      verticalAlignment: Popup.Alignment.BOTTOM,
      closeOnAnchorMouseDown: false,
      cssClass: 'lookup-cell-editor-popup',
      animateResize: false,
      scrollType: 'layoutAndPosition', // popup must not be closed when cell editor scrolls into view
      content: {
        ...this._createLookupBoxModel(),
        id: 'LookupBox'
      }
    });

    const lookupBox = this._popup.widget('LookupBox', LookupBox<TValue>);
    lookupBox.on('propertyChange:lookupCall', e => e.newValue?.setMaxRowCount(this.browseMaxRowCount));

    // sync value
    lookupBox.on('propertyChange:value', e => this.setValue(e.newValue));

    // pass popup events to editor
    lookupBox.on('prepareLookupCall', e => this.trigger('prepareLookupCall', {
      lookupCall: e.lookupCall
    }));
    lookupBox.on('lookupCallDone', e => this.trigger('lookupCallDone', {
      result: e.result
    }));

    lookupBox.one('lookupCallDone', e => this._popup.open());

    if (this.browseHierarchy) {
      const tree = (lookupBox as TreeBox<TValue>).tree;
      tree.on('render', e => tree.htmlComp.setLayout(new LookupEditorTreeLayout(tree)));
      tree.on('nodesInserted', e => {
        tree.setNodeExpandedRecursive(e.nodes, true);
        tree.invalidateLayoutTree();
      });
    }

    this._popup.on('render', e => {
      if (!cellEditorPopup.rendered) {
        return;
      }
      // Ensure cell editor is fully visible.
      // This is important when tabbing through the cells to show the cell editor if it is not visible yet.
      // The LookupBoxPopup also requires the center of its anchor (cell editor) to be visible, otherwise it will get invisible (see Popup._validateVisibility)
      // This is only necessary because the cell editor itself will never be focused. Otherwise the browser would scroll it into view automatically, as it happens for cell editors of other columns.
      scrollbars.scrollTo(cellEditorPopup.table.$data, cellEditorPopup.$container);
      scrollbars.scrollHorizontalTo(cellEditorPopup.table.$data, cellEditorPopup.$container);

      // the focus will always be inside the _popup and therefore the keyStrokes that are meant for the cellEditorPopup need to be transferred
      // this is done by changing the $bindTarget of the cellEditorPopup's keyStrokeContext
      const keyStrokeContext = cellEditorPopup.keyStrokeContext;
      this.session.keyStrokeManager.uninstallKeyStrokeContext(keyStrokeContext);
      keyStrokeContext.$bindTarget = this._popup.$container;
      this.session.keyStrokeManager.installKeyStrokeContext(keyStrokeContext);

      // Workaround for unexpected reset of popup size when lookup structure resizes (collapse/expand of tree nodes)
      // TODO [25.1]: Remove when a better solution has been implemented (#379394)
      this.session.layoutValidator.schedulePostValidateFunction(() => {
        if (!this._popup) {
          return;
        }
        const layout = this._popup.htmlComp.layout as PopupLayout;
        layout.autoSize = false;
        this._popup.position();
      });
    });

    lookupBox.refreshLookup();
  }

  protected _createLookupBoxModel(): ChildModelOf<LookupBox<TValue>> {
    if (this.browseHierarchy) {
      return {
        objectType: TreeBox<TValue>,
        lookupCall: this.lookupCall,
        codeType: this.codeType,
        value: this.value,
        cssClass: 'no-mandatory-indicator',
        labelVisible: false,
        statusVisible: false,
        tree: {
          objectType: Tree,
          autoCheckChildren: true
        }
      };
    }
    return {
      objectType: ListBox<TValue>,
      lookupCall: this.lookupCall,
      value: this.value,
      cssClass: 'no-mandatory-indicator',
      labelVisible: false,
      statusVisible: false,
      table: {
        objectType: Table,
        columns: [{
          objectType: Column,
          autoOptimizeWidth: true,
          autoOptimizeMaxWidth: 450
        }]
      }
    };
  }
}
