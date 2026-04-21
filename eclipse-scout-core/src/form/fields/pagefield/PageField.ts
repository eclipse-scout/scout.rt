/*
 * Copyright (c) 2010, 2026 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {
  Event, EventHandler, Form, GroupBox, GroupBoxEventMap, GroupBoxModel, InitModelOf, MenuBar, ObjectOrChildModel, ObjectType, Outline, Page, PropertyChangeEvent, scout, SearchFormTableControl, Table, TableField, TreeNodesInsertedEvent,
  WrappedFormField
} from '../../../index';

/**
 * Displays a {@link Page} in a form field by embedding the search form and table of that page
 */
export class PageField<TPage extends Page = Page> extends GroupBox implements PageFieldModel<TPage> {
  declare model: PageFieldModel<TPage>;
  declare eventMap: PageFieldEventMap<TPage>;
  declare self: PageField<TPage>;

  outline: Outline;
  page: TPage;
  searchFormField: WrappedFormField;
  tableField: TableField;
  protected _searchFormHandler: EventHandler<PropertyChangeEvent>;
  protected _detailTableHandler: EventHandler<PropertyChangeEvent>;
  protected _pageDestroyHandler: EventHandler;
  protected _outlineNodesInsertedHandler: EventHandler<TreeNodesInsertedEvent>;

  constructor() {
    super();

    this._addWidgetProperties(['outline']);
    this._searchFormHandler = this._onPageSearchFormChange.bind(this);
    this._detailTableHandler = this._onPageDetailTableChange.bind(this);
    this._pageDestroyHandler = this._onPageDestroy.bind(this);
    this._outlineNodesInsertedHandler = this._onOutlineNodesInserted.bind(this);
  }

  protected override _jsonModel(): PageFieldModel<TPage> {
    return {
      gridColumnCount: 1,
      cssClass: 'page-field',
      fields: [{
        id: 'SearchFormField',
        objectType: WrappedFormField,
        cssClass: 'search-form-field',
        gridDataHints: {
          weightY: 0
        }
      }, {
        id: 'TableField',
        objectType: TableField,
        labelVisible: false,
        gridDataHints: {
          h: 7
        }
      }]
    };
  }

  protected override _init(model: InitModelOf<this>) {
    super._init(model);

    this.searchFormField = this.widget('SearchFormField', WrappedFormField);
    this.searchFormField.setVisible(!!this.searchFormField.innerForm);
    this.searchFormField.on('propertyChange:innerForm', event => this.searchFormField.setVisible(!!this.searchFormField.innerForm));

    this.tableField = this.widget('TableField', TableField);
    this.tableField.setVisible(!!this.tableField.table);
    this.tableField.on('propertyChange:table', event => this.tableField.setVisible(!!this.tableField.table));

    this._setOutline(this.outline);
  }

  /** @see PageFieldModel.outline */
  setOutline(outline: ObjectOrChildModel<Outline>) {
    this.setProperty('outline', outline);
  }

  protected _setOutline(outline: Outline) {
    outline = scout.nvl(outline, scout.create(Outline, {parent: this}));
    if (outline.nodes.length > 1) {
      throw new Error('Outline must not have more than one root page');
    }
    this.outline?.off('nodesInserted', this._outlineNodesInsertedHandler);

    this._setProperty('outline', outline);

    this.outline.setNavigateButtonsVisible(false);

    // Ensure outline is not set to compact and does not embed detail content which would happen if desktop.displayStyle is set to COMPACT.
    // Otherwise, detail menus and table controls would be modified (TableControlAdapterMenu), but the page field should display the table and search form as they are.
    this.outline.setCompact(false);
    this.outline.setEmbedDetailContent(false);
    this.outline.on('nodesInserted', this._outlineNodesInsertedHandler);

    this._setPage(this.outline.nodes[0] as TPage || this.page);
  }

  /** @see PageFieldModel.page */
  setPage(page: ObjectType<TPage> | ObjectOrChildModel<TPage>) {
    this.setProperty('page', page);
  }

  protected _setPage(page: ObjectType<TPage> | ObjectOrChildModel<TPage>) {
    if (this.page !== page && this.page instanceof Page) {
      this.page.off('propertyChange:detailTable', this._detailTableHandler);
      this.page.off('destroy', this._pageDestroyHandler);
      this.outline.deleteNode(this.page);
      this._updateDetailTable(null);
    }

    this._setProperty('page', this._ensurePage(page));

    if (this.page) {
      this.outline.insertNode(this.page);
      this.outline.selectNode(this.page);
      this.page.on('propertyChange:detailTable', this._detailTableHandler);
      this.page.on('destroy', this._pageDestroyHandler);
      this._updateDetailTable(this.page.detailTable);
    }
  }

  protected _onPageSearchFormChange(event: PropertyChangeEvent<Form>) {
    this._updateSearchForm(event.newValue);
  }

  protected _onPageDetailTableChange(event: PropertyChangeEvent<Table>) {
    this._updateDetailTable(event.newValue, event.oldValue);
  }

  protected _updateDetailTable(detailTable: Table, oldDetailTable?: Table) {
    let oldSearchFormControl = oldDetailTable?.findTableControl(SearchFormTableControl);
    oldSearchFormControl?.off('propertyChange:form', this._searchFormHandler);

    detailTable?.menuBar.setPosition(MenuBar.Position.BOTTOM);
    this.tableField.setTable(detailTable);

    let searchFormTableControl = detailTable?.findTableControl(SearchFormTableControl);
    if (searchFormTableControl) {
      searchFormTableControl.setVisibleGranted(false);
      searchFormTableControl.on('propertyChange:form', this._searchFormHandler);
    }
    this._updateSearchForm(searchFormTableControl?.form);
  }

  protected _updateSearchForm(searchForm: Form) {
    this.searchFormField.setInnerForm(searchForm);
  }

  protected _ensurePage(page: ObjectType<TPage> | ObjectOrChildModel<TPage>): TPage {
    if (!page || page instanceof Page) {
      if (page && (page as TPage).outline !== this.outline) {
        throw new Error('page must belong to the outline of the page field');
      }
      return page as TPage;
    }

    // object type
    if (typeof page === 'string' || typeof page === 'function') {
      return scout.create(page, {
        parent: this.outline
      } as InitModelOf<TPage>);
    }

    // child model
    return scout.create({
      ...page,
      parent: this.outline
    }) as TPage;
  }

  protected _onPageDestroy(event: Event<Page>) {
    this.setPage(null);
  }

  protected _onOutlineNodesInserted(event: TreeNodesInsertedEvent) {
    if (event.parentNode || !event.nodes.length) {
      // Adding child nodes or no nodes is allowed
      return;
    }

    if (event.nodes.length > 1) {
      throw new Error('Only one node can be inserted');
    }

    if (this.page) {
      if (this.page === event.nodes[0]) {
        // Already inserted
        return;
      }
      throw new Error('There is already a page set');
    }

    this.setPage(event.nodes[0] as TPage);
  }
}

export interface PageFieldModel<TPage extends Page> extends GroupBoxModel {
  /**
   * Specifies the page to be displayed in the page field.
   */
  page?: ObjectType<TPage> | ObjectOrChildModel<TPage>;
  /**
   * Specifies the outline that contains the page to be displayed in the page field.
   *
   * If an outline is set, the first root node is used as {@link page}.
   * If no outline is set, it will be created automatically and the specified {@link page} is inserted.
   *
   * Typically, setting a {@link page} instead of an outline is the preferred way to use the page field.
   */
  outline?: ObjectType<Outline> | ObjectOrChildModel<Outline>;
}

export interface PageFieldEventMap<TPage extends Page = Page> extends GroupBoxEventMap {
  'propertyChange:page': PropertyChangeEvent<TPage>;
  'propertyChange:outline': PropertyChangeEvent<Outline>;
}
