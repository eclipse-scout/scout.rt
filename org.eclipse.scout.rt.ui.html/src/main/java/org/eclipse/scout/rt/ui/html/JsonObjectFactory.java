/*
 * Copyright (c) 2010, 2024 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.ui.html;

import java.text.DecimalFormat;
import java.util.Date;

import org.eclipse.scout.rt.client.IClientSession;
import org.eclipse.scout.rt.client.extension.ui.NotificationBadgeStatus;
import org.eclipse.scout.rt.client.ui.accordion.IAccordion;
import org.eclipse.scout.rt.client.ui.action.keystroke.IKeyStroke;
import org.eclipse.scout.rt.client.ui.action.menu.IComboMenu;
import org.eclipse.scout.rt.client.ui.action.menu.IMenu;
import org.eclipse.scout.rt.client.ui.action.menu.form.fields.IFormFieldMenu;
import org.eclipse.scout.rt.client.ui.action.view.IViewButton;
import org.eclipse.scout.rt.client.ui.basic.breadcrumbbar.IBreadcrumbBar;
import org.eclipse.scout.rt.client.ui.basic.breadcrumbbar.IBreadcrumbItem;
import org.eclipse.scout.rt.client.ui.basic.calendar.CalendarComponent;
import org.eclipse.scout.rt.client.ui.basic.calendar.ICalendar;
import org.eclipse.scout.rt.client.ui.basic.filechooser.IFileChooser;
import org.eclipse.scout.rt.client.ui.basic.planner.IPlanner;
import org.eclipse.scout.rt.client.ui.basic.table.ITable;
import org.eclipse.scout.rt.client.ui.basic.table.ITableRowTileMapping;
import org.eclipse.scout.rt.client.ui.basic.table.ITableTileGridMediator;
import org.eclipse.scout.rt.client.ui.basic.table.columns.IAlphanumericSortingStringColumn;
import org.eclipse.scout.rt.client.ui.basic.table.columns.IBeanColumn;
import org.eclipse.scout.rt.client.ui.basic.table.columns.IBooleanColumn;
import org.eclipse.scout.rt.client.ui.basic.table.columns.IColumn;
import org.eclipse.scout.rt.client.ui.basic.table.columns.IDateColumn;
import org.eclipse.scout.rt.client.ui.basic.table.columns.IIconColumn;
import org.eclipse.scout.rt.client.ui.basic.table.columns.INumberColumn;
import org.eclipse.scout.rt.client.ui.basic.table.columns.ISmartColumn;
import org.eclipse.scout.rt.client.ui.basic.table.columns.IStringColumn;
import org.eclipse.scout.rt.client.ui.basic.table.controls.IAggregateTableControl;
import org.eclipse.scout.rt.client.ui.basic.table.controls.IFormTableControl;
import org.eclipse.scout.rt.client.ui.basic.table.controls.ITableControl;
import org.eclipse.scout.rt.client.ui.basic.table.userfilter.ColumnUserFilterState;
import org.eclipse.scout.rt.client.ui.basic.table.userfilter.DateColumnUserFilterState;
import org.eclipse.scout.rt.client.ui.basic.table.userfilter.NumberColumnUserFilterState;
import org.eclipse.scout.rt.client.ui.basic.table.userfilter.TableTextUserFilterState;
import org.eclipse.scout.rt.client.ui.basic.table.userfilter.TextColumnUserFilterState;
import org.eclipse.scout.rt.client.ui.basic.tree.ITree;
import org.eclipse.scout.rt.client.ui.desktop.IDesktop;
import org.eclipse.scout.rt.client.ui.desktop.hybrid.BrowserCallbacks;
import org.eclipse.scout.rt.client.ui.desktop.hybrid.HybridManager;
import org.eclipse.scout.rt.client.ui.desktop.notification.IDesktopNotification;
import org.eclipse.scout.rt.client.ui.desktop.outline.IOutline;
import org.eclipse.scout.rt.client.ui.desktop.outline.IOutlineTileField;
import org.eclipse.scout.rt.client.ui.desktop.outline.IOutlineViewButton;
import org.eclipse.scout.rt.client.ui.desktop.outline.ISearchOutline;
import org.eclipse.scout.rt.client.ui.desktop.outline.pages.IPage;
import org.eclipse.scout.rt.client.ui.form.IForm;
import org.eclipse.scout.rt.client.ui.form.IFormMenu;
import org.eclipse.scout.rt.client.ui.form.ITileOverviewForm;
import org.eclipse.scout.rt.client.ui.form.fields.IStatusMenuMapping;
import org.eclipse.scout.rt.client.ui.form.fields.IWidgetField;
import org.eclipse.scout.rt.client.ui.form.fields.ParsingFailedStatus;
import org.eclipse.scout.rt.client.ui.form.fields.ValidationFailedStatus;
import org.eclipse.scout.rt.client.ui.form.fields.accordionfield.IAccordionField;
import org.eclipse.scout.rt.client.ui.form.fields.beanfield.IBeanField;
import org.eclipse.scout.rt.client.ui.form.fields.booleanfield.IBooleanField;
import org.eclipse.scout.rt.client.ui.form.fields.breadcrumbbarfield.IBreadcrumbBarField;
import org.eclipse.scout.rt.client.ui.form.fields.browserfield.IBrowserField;
import org.eclipse.scout.rt.client.ui.form.fields.button.IButton;
import org.eclipse.scout.rt.client.ui.form.fields.button.IRadioButton;
import org.eclipse.scout.rt.client.ui.form.fields.calendarfield.ICalendarField;
import org.eclipse.scout.rt.client.ui.form.fields.clipboardfield.IClipboardField;
import org.eclipse.scout.rt.client.ui.form.fields.colorfield.IColorField;
import org.eclipse.scout.rt.client.ui.form.fields.datefield.IDateField;
import org.eclipse.scout.rt.client.ui.form.fields.filechooserbutton.IFileChooserButton;
import org.eclipse.scout.rt.client.ui.form.fields.filechooserfield.IFileChooserField;
import org.eclipse.scout.rt.client.ui.form.fields.groupbox.IGroupBox;
import org.eclipse.scout.rt.client.ui.form.fields.htmlfield.IHtmlField;
import org.eclipse.scout.rt.client.ui.form.fields.imagefield.IImageField;
import org.eclipse.scout.rt.client.ui.form.fields.labelfield.ILabelField;
import org.eclipse.scout.rt.client.ui.form.fields.listbox.IListBox;
import org.eclipse.scout.rt.client.ui.form.fields.mode.IMode;
import org.eclipse.scout.rt.client.ui.form.fields.modeselector.IModeSelectorField;
import org.eclipse.scout.rt.client.ui.form.fields.numberfield.INumberField;
import org.eclipse.scout.rt.client.ui.form.fields.placeholder.IPlaceholderField;
import org.eclipse.scout.rt.client.ui.form.fields.plannerfield.IPlannerField;
import org.eclipse.scout.rt.client.ui.form.fields.radiobuttongroup.IRadioButtonGroup;
import org.eclipse.scout.rt.client.ui.form.fields.sequencebox.ISequenceBox;
import org.eclipse.scout.rt.client.ui.form.fields.smartfield.IProposalField;
import org.eclipse.scout.rt.client.ui.form.fields.smartfield.ISmartField;
import org.eclipse.scout.rt.client.ui.form.fields.splitbox.ISplitBox;
import org.eclipse.scout.rt.client.ui.form.fields.stringfield.IStringField;
import org.eclipse.scout.rt.client.ui.form.fields.tabbox.ITabBox;
import org.eclipse.scout.rt.client.ui.form.fields.tablefield.ITableField;
import org.eclipse.scout.rt.client.ui.form.fields.tagfield.ITagField;
import org.eclipse.scout.rt.client.ui.form.fields.tilefield.ITileField;
import org.eclipse.scout.rt.client.ui.form.fields.treebox.ITreeBox;
import org.eclipse.scout.rt.client.ui.form.fields.treefield.ITreeField;
import org.eclipse.scout.rt.client.ui.form.fields.wizard.IWizardProgressField;
import org.eclipse.scout.rt.client.ui.form.fields.wrappedform.IWrappedFormField;
import org.eclipse.scout.rt.client.ui.form.js.IJsForm;
import org.eclipse.scout.rt.client.ui.group.IGroup;
import org.eclipse.scout.rt.client.ui.label.ILabel;
import org.eclipse.scout.rt.client.ui.messagebox.IMessageBox;
import org.eclipse.scout.rt.client.ui.notification.INotification;
import org.eclipse.scout.rt.client.ui.popup.IMobilePopup;
import org.eclipse.scout.rt.client.ui.popup.IPopup;
import org.eclipse.scout.rt.client.ui.popup.IWidgetPopup;
import org.eclipse.scout.rt.client.ui.popup.PopupManager;
import org.eclipse.scout.rt.client.ui.tile.IBeanTile;
import org.eclipse.scout.rt.client.ui.tile.IFormFieldTile;
import org.eclipse.scout.rt.client.ui.tile.IHtmlTile;
import org.eclipse.scout.rt.client.ui.tile.ITile;
import org.eclipse.scout.rt.client.ui.tile.ITileAccordion;
import org.eclipse.scout.rt.client.ui.tile.ITileGrid;
import org.eclipse.scout.rt.client.ui.tile.IWidgetTile;
import org.eclipse.scout.rt.client.uuidpool.IUuidPool;
import org.eclipse.scout.rt.platform.Bean;
import org.eclipse.scout.rt.platform.Order;
import org.eclipse.scout.rt.platform.status.IStatus;
import org.eclipse.scout.rt.ui.html.json.AbstractJsonObjectFactory;
import org.eclipse.scout.rt.ui.html.json.IJsonAdapter;
import org.eclipse.scout.rt.ui.html.json.IJsonObject;
import org.eclipse.scout.rt.ui.html.json.JsonByteArray;
import org.eclipse.scout.rt.ui.html.json.JsonClientSession;
import org.eclipse.scout.rt.ui.html.json.JsonDate;
import org.eclipse.scout.rt.ui.html.json.JsonDecimalFormat;
import org.eclipse.scout.rt.ui.html.json.JsonNotificationBadgeStatus;
import org.eclipse.scout.rt.ui.html.json.JsonParsingFailedStatus;
import org.eclipse.scout.rt.ui.html.json.JsonStatus;
import org.eclipse.scout.rt.ui.html.json.JsonValidationFailedStatus;
import org.eclipse.scout.rt.ui.html.json.accordion.JsonAccordion;
import org.eclipse.scout.rt.ui.html.json.action.keystroke.JsonKeyStroke;
import org.eclipse.scout.rt.ui.html.json.basic.breadcrumbbar.JsonBreadcrumbBar;
import org.eclipse.scout.rt.ui.html.json.basic.breadcrumbbar.JsonBreadcrumbItem;
import org.eclipse.scout.rt.ui.html.json.basic.filechooser.JsonFileChooser;
import org.eclipse.scout.rt.ui.html.json.basic.planner.JsonPlanner;
import org.eclipse.scout.rt.ui.html.json.calendar.JsonCalendar;
import org.eclipse.scout.rt.ui.html.json.calendar.JsonCalendarComponent;
import org.eclipse.scout.rt.ui.html.json.desktop.JsonDesktop;
import org.eclipse.scout.rt.ui.html.json.desktop.JsonDesktopNotification;
import org.eclipse.scout.rt.ui.html.json.desktop.JsonFormMenu;
import org.eclipse.scout.rt.ui.html.json.desktop.JsonOutline;
import org.eclipse.scout.rt.ui.html.json.desktop.JsonOutlineTileField;
import org.eclipse.scout.rt.ui.html.json.desktop.JsonOutlineViewButton;
import org.eclipse.scout.rt.ui.html.json.desktop.JsonSearchOutline;
import org.eclipse.scout.rt.ui.html.json.desktop.JsonViewButton;
import org.eclipse.scout.rt.ui.html.json.desktop.hybrid.JsonBrowserCallbacks;
import org.eclipse.scout.rt.ui.html.json.desktop.hybrid.JsonHybridManager;
import org.eclipse.scout.rt.ui.html.json.form.JsonForm;
import org.eclipse.scout.rt.ui.html.json.form.JsonTileOverviewForm;
import org.eclipse.scout.rt.ui.html.json.form.fields.JsonDateField;
import org.eclipse.scout.rt.ui.html.json.form.fields.JsonStatusMenuMapping;
import org.eclipse.scout.rt.ui.html.json.form.fields.JsonWidgetField;
import org.eclipse.scout.rt.ui.html.json.form.fields.accordionfield.JsonAccordionField;
import org.eclipse.scout.rt.ui.html.json.form.fields.beanfield.JsonBeanField;
import org.eclipse.scout.rt.ui.html.json.form.fields.breadcrumbbarfield.JsonBreadcrumbBarField;
import org.eclipse.scout.rt.ui.html.json.form.fields.browserfield.JsonBrowserField;
import org.eclipse.scout.rt.ui.html.json.form.fields.button.JsonButton;
import org.eclipse.scout.rt.ui.html.json.form.fields.calendar.JsonCalendarField;
import org.eclipse.scout.rt.ui.html.json.form.fields.checkbox.JsonCheckBoxField;
import org.eclipse.scout.rt.ui.html.json.form.fields.clipboardfield.JsonClipboardField;
import org.eclipse.scout.rt.ui.html.json.form.fields.colorfield.JsonColorField;
import org.eclipse.scout.rt.ui.html.json.form.fields.filechooserbutton.JsonFileChooserButton;
import org.eclipse.scout.rt.ui.html.json.form.fields.filechooserfield.JsonFileChooserField;
import org.eclipse.scout.rt.ui.html.json.form.fields.groupbox.JsonGroupBox;
import org.eclipse.scout.rt.ui.html.json.form.fields.htmlfield.JsonHtmlField;
import org.eclipse.scout.rt.ui.html.json.form.fields.imagefield.JsonImageField;
import org.eclipse.scout.rt.ui.html.json.form.fields.labelfield.JsonLabelField;
import org.eclipse.scout.rt.ui.html.json.form.fields.listbox.JsonListBox;
import org.eclipse.scout.rt.ui.html.json.form.fields.mode.JsonMode;
import org.eclipse.scout.rt.ui.html.json.form.fields.modeselector.JsonModeSelectorField;
import org.eclipse.scout.rt.ui.html.json.form.fields.numberfield.JsonNumberField;
import org.eclipse.scout.rt.ui.html.json.form.fields.placeholder.JsonPlaceholderField;
import org.eclipse.scout.rt.ui.html.json.form.fields.plannerfield.JsonPlannerField;
import org.eclipse.scout.rt.ui.html.json.form.fields.radiobutton.JsonRadioButton;
import org.eclipse.scout.rt.ui.html.json.form.fields.radiobutton.JsonRadioButtonGroup;
import org.eclipse.scout.rt.ui.html.json.form.fields.sequencebox.JsonSequenceBox;
import org.eclipse.scout.rt.ui.html.json.form.fields.smartfield.JsonProposalField;
import org.eclipse.scout.rt.ui.html.json.form.fields.smartfield.JsonSmartField;
import org.eclipse.scout.rt.ui.html.json.form.fields.splitbox.JsonSplitBox;
import org.eclipse.scout.rt.ui.html.json.form.fields.stringfield.JsonStringField;
import org.eclipse.scout.rt.ui.html.json.form.fields.tabbox.JsonTabBox;
import org.eclipse.scout.rt.ui.html.json.form.fields.tabbox.JsonTabItem;
import org.eclipse.scout.rt.ui.html.json.form.fields.tablefield.JsonTableField;
import org.eclipse.scout.rt.ui.html.json.form.fields.tagfield.JsonTagField;
import org.eclipse.scout.rt.ui.html.json.form.fields.tilefield.JsonTileField;
import org.eclipse.scout.rt.ui.html.json.form.fields.treebox.JsonTreeBox;
import org.eclipse.scout.rt.ui.html.json.form.fields.treefield.JsonTreeField;
import org.eclipse.scout.rt.ui.html.json.form.fields.wizard.JsonWizardProgressField;
import org.eclipse.scout.rt.ui.html.json.form.fields.wrappedform.JsonWrappedFormField;
import org.eclipse.scout.rt.ui.html.json.form.js.JsonJsForm;
import org.eclipse.scout.rt.ui.html.json.group.JsonGroup;
import org.eclipse.scout.rt.ui.html.json.label.JsonLabel;
import org.eclipse.scout.rt.ui.html.json.menu.JsonComboMenu;
import org.eclipse.scout.rt.ui.html.json.menu.JsonMenu;
import org.eclipse.scout.rt.ui.html.json.menu.form.field.JsonFormFieldMenu;
import org.eclipse.scout.rt.ui.html.json.messagebox.JsonMessageBox;
import org.eclipse.scout.rt.ui.html.json.notification.JsonNotification;
import org.eclipse.scout.rt.ui.html.json.popup.JsonMobilePopup;
import org.eclipse.scout.rt.ui.html.json.popup.JsonPopup;
import org.eclipse.scout.rt.ui.html.json.popup.JsonPopupManager;
import org.eclipse.scout.rt.ui.html.json.popup.JsonWidgetPopup;
import org.eclipse.scout.rt.ui.html.json.table.JsonAlphanumericSortingStringColumn;
import org.eclipse.scout.rt.ui.html.json.table.JsonBeanColumn;
import org.eclipse.scout.rt.ui.html.json.table.JsonBooleanColumn;
import org.eclipse.scout.rt.ui.html.json.table.JsonColumn;
import org.eclipse.scout.rt.ui.html.json.table.JsonDateColumn;
import org.eclipse.scout.rt.ui.html.json.table.JsonIconColumn;
import org.eclipse.scout.rt.ui.html.json.table.JsonNumberColumn;
import org.eclipse.scout.rt.ui.html.json.table.JsonOutlineTable;
import org.eclipse.scout.rt.ui.html.json.table.JsonSmartColumn;
import org.eclipse.scout.rt.ui.html.json.table.JsonStringColumn;
import org.eclipse.scout.rt.ui.html.json.table.JsonTable;
import org.eclipse.scout.rt.ui.html.json.table.JsonTableRowTileMapping;
import org.eclipse.scout.rt.ui.html.json.table.JsonTableTileGridMediator;
import org.eclipse.scout.rt.ui.html.json.table.control.JsonAggregateTableControl;
import org.eclipse.scout.rt.ui.html.json.table.control.JsonFormTableControl;
import org.eclipse.scout.rt.ui.html.json.table.control.JsonTableControl;
import org.eclipse.scout.rt.ui.html.json.table.userfilter.JsonColumnUserFilter;
import org.eclipse.scout.rt.ui.html.json.table.userfilter.JsonDateColumnUserFilter;
import org.eclipse.scout.rt.ui.html.json.table.userfilter.JsonNumberColumnUserFilter;
import org.eclipse.scout.rt.ui.html.json.table.userfilter.JsonTableTextUserFilter;
import org.eclipse.scout.rt.ui.html.json.table.userfilter.JsonTextColumnUserFilter;
import org.eclipse.scout.rt.ui.html.json.tile.JsonBeanTile;
import org.eclipse.scout.rt.ui.html.json.tile.JsonFormFieldTile;
import org.eclipse.scout.rt.ui.html.json.tile.JsonHtmlTile;
import org.eclipse.scout.rt.ui.html.json.tile.JsonTile;
import org.eclipse.scout.rt.ui.html.json.tile.JsonTileAccordion;
import org.eclipse.scout.rt.ui.html.json.tile.JsonTileGrid;
import org.eclipse.scout.rt.ui.html.json.tile.JsonWidgetTile;
import org.eclipse.scout.rt.ui.html.json.tree.JsonTree;
import org.eclipse.scout.rt.ui.html.uuidpool.JsonUuidPool;

@Bean
@Order(5500)
public class JsonObjectFactory extends AbstractJsonObjectFactory {

  @SuppressWarnings({"unchecked", "squid:S138"})
  @Override
  public IJsonAdapter<?> createJsonAdapter(Object model, IUiSession session, String id, IJsonAdapter<?> parent) {
    // --- form fields ----
    if (model instanceof IGroupBox) {
      // we must distinct between normal group-boxes and group-boxes in tab-boxes
      // the use the same model, but we need different adapters
      IGroupBox groupBox = (IGroupBox) model;
      if (groupBox.getParentField() instanceof ITabBox) {
        return new JsonTabItem<>(groupBox, session, id, parent);
      }
      else {
        return new JsonGroupBox<>(groupBox, session, id, parent);
      }
    }
    if (model instanceof ISequenceBox) {
      return new JsonSequenceBox<>((ISequenceBox) model, session, id, parent);
    }
    if (model instanceof ITabBox) {
      return new JsonTabBox<>((ITabBox) model, session, id, parent);
    }
    if (model instanceof IBooleanField) {
      return new JsonCheckBoxField<>((IBooleanField) model, session, id, parent);
    }
    if (model instanceof ILabelField) {
      return new JsonLabelField<>((ILabelField) model, session, id, parent);
    }
    if (model instanceof IImageField) {
      return new JsonImageField<>((IImageField) model, session, id, parent);
    }
    if (model instanceof ITableField<?>) {
      return new JsonTableField<ITableField<? extends ITable>>((ITableField<?>) model, session, id, parent);
    }
    if (model instanceof IListBox<?>) {
      return new JsonListBox((IListBox<?>) model, session, id, parent);
    }
    if (model instanceof ITreeField) {
      return new JsonTreeField<>((ITreeField) model, session, id, parent);
    }
    if (model instanceof ITreeBox<?>) {
      return new JsonTreeBox<ITreeBox<?>>((ITreeBox<?>) model, session, id, parent);
    }
    if (model instanceof IRadioButton<?>) {
      return new JsonRadioButton<IRadioButton<?>>((IRadioButton<?>) model, session, id, parent);
    }
    if (model instanceof IRadioButtonGroup<?>) {
      return new JsonRadioButtonGroup<IRadioButtonGroup<?>>((IRadioButtonGroup<?>) model, session, id, parent);
    }
    if (model instanceof IButton) {
      return new JsonButton<>((IButton) model, session, id, parent);
    }
    if (model instanceof IStringField) {
      return new JsonStringField<>((IStringField) model, session, id, parent);
    }
    if (model instanceof INumberField<?>) {
      return new JsonNumberField<INumberField<?>>((INumberField<?>) model, session, id, parent);
    }
    if (model instanceof IProposalField<?>) {
      return new JsonProposalField((IProposalField<?>) model, session, id, parent);
    }
    if (model instanceof ISmartField<?>) {
      return new JsonSmartField((ISmartField<?>) model, session, id, parent);
    }
    if (model instanceof IDateField) {
      return new JsonDateField<>((IDateField) model, session, id, parent);
    }
    if (model instanceof ICalendarField<?>) {
      return new JsonCalendarField<ICalendarField<? extends ICalendar>>((ICalendarField<?>) model, session, id, parent);
    }
    if (model instanceof IPlannerField<?>) {
      return new JsonPlannerField((IPlannerField<?>) model, session, id, parent);
    }
    if (model instanceof IWrappedFormField<?>) {
      return new JsonWrappedFormField<IWrappedFormField<? extends IForm>>((IWrappedFormField<?>) model, session, id, parent);
    }
    if (model instanceof ISplitBox) {
      return new JsonSplitBox<>((ISplitBox) model, session, id, parent);
    }
    if (model instanceof IPlaceholderField) {
      return new JsonPlaceholderField<>((IPlaceholderField) model, session, id, parent);
    }
    if (model instanceof IWizardProgressField) {
      return new JsonWizardProgressField<>((IWizardProgressField) model, session, id, parent);
    }
    if (model instanceof IHtmlField) {
      return new JsonHtmlField<>((IHtmlField) model, session, id, parent);
    }
    if (model instanceof IBeanField) {
      return new JsonBeanField<IBeanField<?>>((IBeanField<?>) model, session, id, parent);
    }
    if (model instanceof IFileChooserButton) {
      return new JsonFileChooserButton<>((IFileChooserButton) model, session, id, parent);
    }
    if (model instanceof IFileChooserField) {
      return new JsonFileChooserField<>((IFileChooserField) model, session, id, parent);
    }
    if (model instanceof IColorField) {
      return new JsonColorField<>((IColorField) model, session, id, parent);
    }
    if (model instanceof IBrowserField) {
      return new JsonBrowserField<>((IBrowserField) model, session, id, parent);
    }
    if (model instanceof IClipboardField) {
      return new JsonClipboardField<>((IClipboardField) model, session, id, parent);
    }
    if (model instanceof ITileField<?>) {
      return new JsonTileField((ITileField<?>) model, session, id, parent);
    }
    if (model instanceof IAccordionField<?>) {
      return new JsonAccordionField<IAccordionField<? extends IAccordion>>((IAccordionField<?>) model, session, id, parent);
    }
    if (model instanceof ITagField) {
      return new JsonTagField((ITagField) model, session, id, parent);
    }
    if (model instanceof IModeSelectorField<?>) {
      return new JsonModeSelectorField<IModeSelectorField<?>>((IModeSelectorField<?>) model, session, id, parent);
    }
    if (model instanceof IBreadcrumbBarField) {
      return new JsonBreadcrumbBarField<>((IBreadcrumbBarField) model, session, id, parent);
    }
    if (model instanceof IWidgetField) {
      return new JsonWidgetField<>((IWidgetField) model, session, id, parent);
    }

    // --- other model objects ---
    if (model instanceof IDesktop) {
      return new JsonDesktop<>((IDesktop) model, session, id, parent);
    }
    if (model instanceof IFormMenu<?>) {
      return new JsonFormMenu((IFormMenu<?>) model, session, id, parent);
    }
    if (model instanceof IFormFieldMenu) {
      return new JsonFormFieldMenu<>((IFormFieldMenu) model, session, id, parent);
    }
    if (model instanceof IComboMenu) {
      return new JsonComboMenu<>((IComboMenu) model, session, id, parent);
    }
    if (model instanceof IMenu) {
      return new JsonMenu<>((IMenu) model, session, id, parent);
    }
    if (model instanceof IKeyStroke) {
      return new JsonKeyStroke<>((IKeyStroke) model, session, id, parent);
    }
    if (model instanceof IJsForm<?, ?>) {
      return new JsonJsForm<>((IJsForm<?, ?>) model, session, id, parent);
    }
    if (model instanceof ITileOverviewForm) {
      return new JsonTileOverviewForm((ITileOverviewForm) model, session, id, parent);
    }
    if (model instanceof IForm) {
      return new JsonForm<>((IForm) model, session, id, parent);
    }
    if (model instanceof IMessageBox) {
      return new JsonMessageBox<>((IMessageBox) model, session, id, parent);
    }
    if (model instanceof IDesktopNotification) {
      return new JsonDesktopNotification<>((IDesktopNotification) model, session, id, parent);
    }
    if (model instanceof INotification) {
      return new JsonNotification<>((INotification) model, session, id, parent);
    }
    if (model instanceof IOutlineViewButton) {
      return new JsonOutlineViewButton<>((IOutlineViewButton) model, session, id, parent);
    }
    if (model instanceof IOutlineTileField) {
      return new JsonOutlineTileField((IOutlineTileField) model, session, id, parent);
    }
    if (model instanceof IViewButton) {
      return new JsonViewButton<>((IViewButton) model, session, id, parent);
    }
    if (model instanceof ISearchOutline) {
      return new JsonSearchOutline<>((ISearchOutline) model, session, id, parent);
    }
    if (model instanceof IOutline) {
      return new JsonOutline<>((IOutline) model, session, id, parent);
    }
    if (model instanceof ITree) {
      return new JsonTree<>((ITree) model, session, id, parent);
    }
    if (model instanceof ITable) {
      ITable table = (ITable) model;
      IPage<?> page = (IPage<?>) table.getProperty(JsonOutlineTable.PROP_PAGE);
      if (page != null) {
        return new JsonOutlineTable<>(table, session, id, parent, page);
      }
      return new JsonTable<>(table, session, id, parent);
    }
    if (model instanceof ITableRowTileMapping) {
      return new JsonTableRowTileMapping<>((ITableRowTileMapping) model, session, id, parent);
    }
    if (model instanceof ITableTileGridMediator) {
      return new JsonTableTileGridMediator<>((ITableTileGridMediator) model, session, id, parent);
    }
    if (model instanceof IClientSession) {
      return new JsonClientSession<>((IClientSession) model, session, id, parent);
    }
    if (model instanceof ICalendar) {
      return new JsonCalendar<>((ICalendar) model, session, id, parent);
    }
    if (model instanceof CalendarComponent) {
      return new JsonCalendarComponent<>((CalendarComponent) model, session, id, parent);
    }
    if (model instanceof IPlanner<?, ?>) {
      return new JsonPlanner<IPlanner<?, ?>>((IPlanner<?, ?>) model, session, id, parent);
    }
    if (model instanceof IFileChooser) {
      return new JsonFileChooser<>((IFileChooser) model, session, id, parent);
    }
    if (model instanceof IAggregateTableControl) { // needs to be before ITableControl
      return new JsonAggregateTableControl<>((IAggregateTableControl) model, session, id, parent);
    }
    if (model instanceof IFormTableControl) { // needs to be before ITableControl
      return new JsonFormTableControl<>((IFormTableControl) model, session, id, parent);
    }
    if (model instanceof ITableControl) {
      return new JsonTableControl<>((ITableControl) model, session, id, parent);
    }
    if (model instanceof IHtmlTile) {
      return new JsonHtmlTile<>((IHtmlTile) model, session, id, parent);
    }
    if (model instanceof IBeanTile) {
      return new JsonBeanTile<>((IBeanTile<?>) model, session, id, parent);
    }
    if (model instanceof IFormFieldTile) {
      return new JsonFormFieldTile<>((IFormFieldTile<?>) model, session, id, parent);
    }
    if (model instanceof IWidgetTile) {
      return new JsonWidgetTile<>((IWidgetTile<?>) model, session, id, parent);
    }
    if (model instanceof ITile) {
      return new JsonTile<>((ITile) model, session, id, parent);
    }
    if (model instanceof ITileGrid) {
      return new JsonTileGrid<>((ITileGrid<?>) model, session, id, parent);
    }
    if (model instanceof ITileAccordion) {
      return new JsonTileAccordion<>((ITileAccordion<?>) model, session, id, parent);
    }
    if (model instanceof IGroup) {
      return new JsonGroup<>((IGroup) model, session, id, parent);
    }
    if (model instanceof IAccordion) {
      return new JsonAccordion<>((IAccordion) model, session, id, parent);
    }
    if (model instanceof IStatusMenuMapping) {
      return new JsonStatusMenuMapping<>((IStatusMenuMapping) model, session, id, parent);
    }
    if (model instanceof ILabel) {
      return new JsonLabel<>((ILabel) model, session, id, parent);
    }
    if (model instanceof IMode<?>) {
      return new JsonMode<IMode<?>>((IMode<?>) model, session, id, parent);
    }
    if (model instanceof IBreadcrumbBar) {
      return new JsonBreadcrumbBar<>((IBreadcrumbBar) model, session, id, parent);
    }
    if (model instanceof IBreadcrumbItem) {
      return new JsonBreadcrumbItem<>((IBreadcrumbItem) model, session, id, parent);
    }
    if (model instanceof PopupManager) {
      return new JsonPopupManager<>((PopupManager) model, session, id, parent);
    }
    if (model instanceof IMobilePopup<?>) {
      return new JsonMobilePopup<>((IMobilePopup<?>) model, session, id, parent);
    }
    if (model instanceof IWidgetPopup<?>) {
      return new JsonWidgetPopup<>((IWidgetPopup<?>) model, session, id, parent);
    }
    if (model instanceof IPopup) {
      return new JsonPopup((IPopup) model, session, id, parent);
    }
    if (model instanceof IUuidPool) {
      return new JsonUuidPool((IUuidPool) model, session, id, parent);
    }
    if (model instanceof HybridManager) {
      return new JsonHybridManager<>((HybridManager) model, session, id, parent);
    }
    if (model instanceof BrowserCallbacks) {
      return new JsonBrowserCallbacks<>((BrowserCallbacks) model, session, id, parent);
    }
    return null;
  }

  @SuppressWarnings("unchecked")
  @Override
  public IJsonObject createJsonObject(Object object) {
    if (object instanceof Date) {
      return new JsonDate((Date) object);
    }
    if (object instanceof byte[]) {
      return new JsonByteArray((byte[]) object);
    }
    if (object instanceof ParsingFailedStatus) {
      return new JsonParsingFailedStatus((ParsingFailedStatus) object);
    }
    if (object instanceof ValidationFailedStatus) {
      return new JsonValidationFailedStatus((ValidationFailedStatus) object);
    }
    if (object instanceof NotificationBadgeStatus) {
      return new JsonNotificationBadgeStatus((NotificationBadgeStatus) object);
    }
    if (object instanceof IStatus) {
      return new JsonStatus((IStatus) object);
    }
    if (object instanceof INumberColumn<?>) {
      return new JsonNumberColumn((INumberColumn<?>) object);
    }
    if (object instanceof IDateColumn) {
      return new JsonDateColumn((IDateColumn) object);
    }
    if (object instanceof IBooleanColumn) {
      return new JsonBooleanColumn((IBooleanColumn) object);
    }
    if (object instanceof IAlphanumericSortingStringColumn) { // needs to be before IStringColumn
      return new JsonAlphanumericSortingStringColumn((IAlphanumericSortingStringColumn) object);
    }
    if (object instanceof IStringColumn) {
      return new JsonStringColumn((IStringColumn) object);
    }
    if (object instanceof IBeanColumn<?>) {
      return new JsonBeanColumn((IColumn<?>) object);
    }
    if (object instanceof IIconColumn) {
      return new JsonIconColumn((IIconColumn) object);
    }
    if (object instanceof ISmartColumn) {
      return new JsonSmartColumn((ISmartColumn) object);
    }
    if (object instanceof IColumn<?>) {
      return new JsonColumn((IColumn<?>) object);
    }
    if (object instanceof DateColumnUserFilterState) { // needs to be before ColumnUserFilterState
      return new JsonDateColumnUserFilter((DateColumnUserFilterState) object);
    }
    if (object instanceof NumberColumnUserFilterState) { // needs to be before ColumnUserFilterState
      return new JsonNumberColumnUserFilter((NumberColumnUserFilterState) object);
    }
    if (object instanceof TextColumnUserFilterState) { // needs to be before ColumnUserFilterState
      return new JsonTextColumnUserFilter((TextColumnUserFilterState) object);
    }
    if (object instanceof ColumnUserFilterState) {
      return new JsonColumnUserFilter((ColumnUserFilterState) object);
    }
    if (object instanceof TableTextUserFilterState) {
      return new JsonTableTextUserFilter((TableTextUserFilterState) object);
    }
    if (object instanceof DecimalFormat) {
      return new JsonDecimalFormat((DecimalFormat) object);
    }
    return null;
  }
}
