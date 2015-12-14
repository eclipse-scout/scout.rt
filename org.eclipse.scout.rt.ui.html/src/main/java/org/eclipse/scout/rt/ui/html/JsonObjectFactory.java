/*******************************************************************************
 * Copyright (c) 2014-2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.ui.html;

import java.util.Date;

import org.eclipse.scout.rt.client.IClientSession;
import org.eclipse.scout.rt.client.ui.action.keystroke.IKeyStroke;
import org.eclipse.scout.rt.client.ui.action.menu.IMenu;
import org.eclipse.scout.rt.client.ui.action.menu.root.IContextMenu;
import org.eclipse.scout.rt.client.ui.action.view.IViewButton;
import org.eclipse.scout.rt.client.ui.basic.calendar.CalendarComponent;
import org.eclipse.scout.rt.client.ui.basic.calendar.ICalendar;
import org.eclipse.scout.rt.client.ui.basic.filechooser.IFileChooser;
import org.eclipse.scout.rt.client.ui.basic.planner.IPlanner;
import org.eclipse.scout.rt.client.ui.basic.table.ITable;
import org.eclipse.scout.rt.client.ui.basic.table.columns.IBeanColumn;
import org.eclipse.scout.rt.client.ui.basic.table.columns.IBooleanColumn;
import org.eclipse.scout.rt.client.ui.basic.table.columns.IColumn;
import org.eclipse.scout.rt.client.ui.basic.table.columns.IDateColumn;
import org.eclipse.scout.rt.client.ui.basic.table.columns.IIconColumn;
import org.eclipse.scout.rt.client.ui.basic.table.columns.INumberColumn;
import org.eclipse.scout.rt.client.ui.basic.table.columns.IStringColumn;
import org.eclipse.scout.rt.client.ui.basic.table.controls.IAggregateTableControl;
import org.eclipse.scout.rt.client.ui.basic.table.controls.ITableControl;
import org.eclipse.scout.rt.client.ui.basic.table.userfilter.ColumnUserFilterState;
import org.eclipse.scout.rt.client.ui.basic.table.userfilter.DateColumnUserFilterState;
import org.eclipse.scout.rt.client.ui.basic.table.userfilter.NumberColumnUserFilterState;
import org.eclipse.scout.rt.client.ui.basic.table.userfilter.TableTextUserFilterState;
import org.eclipse.scout.rt.client.ui.basic.table.userfilter.TextColumnUserFilterState;
import org.eclipse.scout.rt.client.ui.basic.tree.ITree;
import org.eclipse.scout.rt.client.ui.desktop.IDesktop;
import org.eclipse.scout.rt.client.ui.desktop.outline.IFormToolButton;
import org.eclipse.scout.rt.client.ui.desktop.outline.IOutline;
import org.eclipse.scout.rt.client.ui.desktop.outline.IOutlineViewButton;
import org.eclipse.scout.rt.client.ui.desktop.outline.ISearchOutline;
import org.eclipse.scout.rt.client.ui.desktop.outline.pages.IPage;
import org.eclipse.scout.rt.client.ui.desktop.outline.pages.ISearchForm;
import org.eclipse.scout.rt.client.ui.form.IForm;
import org.eclipse.scout.rt.client.ui.form.fields.beanfield.IBeanField;
import org.eclipse.scout.rt.client.ui.form.fields.booleanfield.IBooleanField;
import org.eclipse.scout.rt.client.ui.form.fields.browserfield.IBrowserField;
import org.eclipse.scout.rt.client.ui.form.fields.button.IButton;
import org.eclipse.scout.rt.client.ui.form.fields.button.IRadioButton;
import org.eclipse.scout.rt.client.ui.form.fields.calendarfield.ICalendarField;
import org.eclipse.scout.rt.client.ui.form.fields.clipboardfield.IClipboardField;
import org.eclipse.scout.rt.client.ui.form.fields.colorfield.IColorField;
import org.eclipse.scout.rt.client.ui.form.fields.composer.IComposerField;
import org.eclipse.scout.rt.client.ui.form.fields.datefield.IDateField;
import org.eclipse.scout.rt.client.ui.form.fields.filechooserfield.IFileChooserField;
import org.eclipse.scout.rt.client.ui.form.fields.groupbox.IGroupBox;
import org.eclipse.scout.rt.client.ui.form.fields.htmlfield.IHtmlField;
import org.eclipse.scout.rt.client.ui.form.fields.imagefield.IImageField;
import org.eclipse.scout.rt.client.ui.form.fields.labelfield.ILabelField;
import org.eclipse.scout.rt.client.ui.form.fields.listbox.IListBox;
import org.eclipse.scout.rt.client.ui.form.fields.numberfield.INumberField;
import org.eclipse.scout.rt.client.ui.form.fields.placeholder.IPlaceholderField;
import org.eclipse.scout.rt.client.ui.form.fields.plannerfield.IPlannerField;
import org.eclipse.scout.rt.client.ui.form.fields.radiobuttongroup.IRadioButtonGroup;
import org.eclipse.scout.rt.client.ui.form.fields.sequencebox.ISequenceBox;
import org.eclipse.scout.rt.client.ui.form.fields.smartfield.IContentAssistField;
import org.eclipse.scout.rt.client.ui.form.fields.smartfield.IProposalChooser;
import org.eclipse.scout.rt.client.ui.form.fields.splitbox.ISplitBox;
import org.eclipse.scout.rt.client.ui.form.fields.stringfield.IStringField;
import org.eclipse.scout.rt.client.ui.form.fields.tabbox.ITabBox;
import org.eclipse.scout.rt.client.ui.form.fields.tablefield.ITableField;
import org.eclipse.scout.rt.client.ui.form.fields.treebox.ITreeBox;
import org.eclipse.scout.rt.client.ui.form.fields.treefield.ITreeField;
import org.eclipse.scout.rt.client.ui.form.fields.wizard.IWizardProgressField;
import org.eclipse.scout.rt.client.ui.form.fields.wrappedform.IWrappedFormField;
import org.eclipse.scout.rt.client.ui.messagebox.IMessageBox;
import org.eclipse.scout.rt.platform.Bean;
import org.eclipse.scout.rt.platform.Order;
import org.eclipse.scout.rt.ui.html.json.AbstractJsonObjectFactory;
import org.eclipse.scout.rt.ui.html.json.IJsonAdapter;
import org.eclipse.scout.rt.ui.html.json.IJsonObject;
import org.eclipse.scout.rt.ui.html.json.JsonByteArray;
import org.eclipse.scout.rt.ui.html.json.JsonClientSession;
import org.eclipse.scout.rt.ui.html.json.JsonDate;
import org.eclipse.scout.rt.ui.html.json.action.keystroke.JsonKeyStroke;
import org.eclipse.scout.rt.ui.html.json.basic.filechooser.JsonFileChooser;
import org.eclipse.scout.rt.ui.html.json.basic.planner.JsonPlanner;
import org.eclipse.scout.rt.ui.html.json.calendar.JsonCalendar;
import org.eclipse.scout.rt.ui.html.json.calendar.JsonCalendarComponent;
import org.eclipse.scout.rt.ui.html.json.desktop.JsonDesktop;
import org.eclipse.scout.rt.ui.html.json.desktop.JsonFormToolButton;
import org.eclipse.scout.rt.ui.html.json.desktop.JsonOutline;
import org.eclipse.scout.rt.ui.html.json.desktop.JsonOutlineViewButton;
import org.eclipse.scout.rt.ui.html.json.desktop.JsonSearchOutline;
import org.eclipse.scout.rt.ui.html.json.desktop.JsonViewButton;
import org.eclipse.scout.rt.ui.html.json.form.JsonForm;
import org.eclipse.scout.rt.ui.html.json.form.JsonSearchForm;
import org.eclipse.scout.rt.ui.html.json.form.fields.JsonDateField;
import org.eclipse.scout.rt.ui.html.json.form.fields.beanfield.JsonBeanField;
import org.eclipse.scout.rt.ui.html.json.form.fields.browserfield.JsonBrowserField;
import org.eclipse.scout.rt.ui.html.json.form.fields.button.JsonButton;
import org.eclipse.scout.rt.ui.html.json.form.fields.calendar.JsonCalendarField;
import org.eclipse.scout.rt.ui.html.json.form.fields.checkbox.JsonCheckBoxField;
import org.eclipse.scout.rt.ui.html.json.form.fields.clipboardfield.JsonClipboardField;
import org.eclipse.scout.rt.ui.html.json.form.fields.colorfield.JsonColorField;
import org.eclipse.scout.rt.ui.html.json.form.fields.composer.JsonComposerField;
import org.eclipse.scout.rt.ui.html.json.form.fields.filechooserfield.JsonFileChooserField;
import org.eclipse.scout.rt.ui.html.json.form.fields.groupbox.JsonGroupBox;
import org.eclipse.scout.rt.ui.html.json.form.fields.htmlfield.JsonHtmlField;
import org.eclipse.scout.rt.ui.html.json.form.fields.imagefield.JsonImageField;
import org.eclipse.scout.rt.ui.html.json.form.fields.labelfield.JsonLabelField;
import org.eclipse.scout.rt.ui.html.json.form.fields.listbox.JsonListBox;
import org.eclipse.scout.rt.ui.html.json.form.fields.numberfield.JsonNumberField;
import org.eclipse.scout.rt.ui.html.json.form.fields.placeholder.JsonPlaceholderField;
import org.eclipse.scout.rt.ui.html.json.form.fields.plannerfield.JsonPlannerField;
import org.eclipse.scout.rt.ui.html.json.form.fields.radiobutton.JsonRadioButton;
import org.eclipse.scout.rt.ui.html.json.form.fields.radiobutton.JsonRadioButtonGroup;
import org.eclipse.scout.rt.ui.html.json.form.fields.sequencebox.JsonSequenceBox;
import org.eclipse.scout.rt.ui.html.json.form.fields.smartfield.JsonProposalChooser;
import org.eclipse.scout.rt.ui.html.json.form.fields.smartfield.JsonSmartField;
import org.eclipse.scout.rt.ui.html.json.form.fields.splitbox.JsonSplitBox;
import org.eclipse.scout.rt.ui.html.json.form.fields.stringfield.JsonStringField;
import org.eclipse.scout.rt.ui.html.json.form.fields.tabbox.JsonTabBox;
import org.eclipse.scout.rt.ui.html.json.form.fields.tabbox.JsonTabItem;
import org.eclipse.scout.rt.ui.html.json.form.fields.tablefield.JsonTableField;
import org.eclipse.scout.rt.ui.html.json.form.fields.treebox.JsonTreeBox;
import org.eclipse.scout.rt.ui.html.json.form.fields.treefield.JsonTreeField;
import org.eclipse.scout.rt.ui.html.json.form.fields.wizard.JsonWizardProgressField;
import org.eclipse.scout.rt.ui.html.json.form.fields.wrappedform.JsonWrappedFormField;
import org.eclipse.scout.rt.ui.html.json.menu.JsonContextMenu;
import org.eclipse.scout.rt.ui.html.json.menu.JsonMenu;
import org.eclipse.scout.rt.ui.html.json.messagebox.JsonMessageBox;
import org.eclipse.scout.rt.ui.html.json.table.JsonBeanColumn;
import org.eclipse.scout.rt.ui.html.json.table.JsonBooleanColumn;
import org.eclipse.scout.rt.ui.html.json.table.JsonColumn;
import org.eclipse.scout.rt.ui.html.json.table.JsonDateColumn;
import org.eclipse.scout.rt.ui.html.json.table.JsonIconColumn;
import org.eclipse.scout.rt.ui.html.json.table.JsonNumberColumn;
import org.eclipse.scout.rt.ui.html.json.table.JsonOutlineTable;
import org.eclipse.scout.rt.ui.html.json.table.JsonStringColumn;
import org.eclipse.scout.rt.ui.html.json.table.JsonTable;
import org.eclipse.scout.rt.ui.html.json.table.control.JsonAggregateTableControl;
import org.eclipse.scout.rt.ui.html.json.table.control.JsonTableControl;
import org.eclipse.scout.rt.ui.html.json.table.userfilter.JsonColumnUserFilter;
import org.eclipse.scout.rt.ui.html.json.table.userfilter.JsonDateColumnUserFilter;
import org.eclipse.scout.rt.ui.html.json.table.userfilter.JsonNumberColumnUserFilter;
import org.eclipse.scout.rt.ui.html.json.table.userfilter.JsonTableTextUserFilter;
import org.eclipse.scout.rt.ui.html.json.table.userfilter.JsonTextColumnUserFilter;
import org.eclipse.scout.rt.ui.html.json.tree.JsonTree;

@Bean
@Order(5500)
public class JsonObjectFactory extends AbstractJsonObjectFactory {

  @SuppressWarnings("unchecked")
  @Override
  public IJsonAdapter<?> createJsonAdapter(Object model, IUiSession session, String id, IJsonAdapter<?> parent) {
    // --- form fields ----
    if (model instanceof IGroupBox) {
      // we must distinct between normal group-boxes and group-boxes in tab-boxes
      // the use the same model, but we need different adapters
      IGroupBox groupBox = (IGroupBox) model;
      if (groupBox.getParentField() instanceof ITabBox) {
        return new JsonTabItem<IGroupBox>(groupBox, session, id, parent);
      }
      else {
        return new JsonGroupBox<IGroupBox>(groupBox, session, id, parent);
      }
    }
    if (model instanceof ISequenceBox) {
      return new JsonSequenceBox<ISequenceBox>((ISequenceBox) model, session, id, parent);
    }
    if (model instanceof ITabBox) {
      return new JsonTabBox<ITabBox>((ITabBox) model, session, id, parent);
    }
    if (model instanceof IBooleanField) {
      return new JsonCheckBoxField<IBooleanField>((IBooleanField) model, session, id, parent);
    }
    if (model instanceof ILabelField) {
      return new JsonLabelField<ILabelField>((ILabelField) model, session, id, parent);
    }
    if (model instanceof IImageField) {
      return new JsonImageField<IImageField>((IImageField) model, session, id, parent);
    }
    if (model instanceof ITableField<?>) {
      return new JsonTableField<ITableField<? extends ITable>>((ITableField<?>) model, session, id, parent);
    }
    if (model instanceof IListBox<?>) {
      return new JsonListBox((IListBox<?>) model, session, id, parent);
    }
    if (model instanceof ITreeField) {
      return new JsonTreeField<ITreeField>((ITreeField) model, session, id, parent);
    }
    if (model instanceof ITreeBox<?>) {
      return new JsonTreeBox<ITreeBox>((ITreeBox<?>) model, session, id, parent);
    }
    if (model instanceof IRadioButton<?>) {
      return new JsonRadioButton<IRadioButton>((IRadioButton<?>) model, session, id, parent);
    }
    if (model instanceof IRadioButtonGroup<?>) {
      return new JsonRadioButtonGroup<IRadioButtonGroup>((IRadioButtonGroup<?>) model, session, id, parent);
    }
    if (model instanceof IButton) {
      return new JsonButton<IButton>((IButton) model, session, id, parent);
    }
    if (model instanceof IStringField) {
      return new JsonStringField<IStringField>((IStringField) model, session, id, parent);
    }
    if (model instanceof INumberField<?>) {
      return new JsonNumberField<INumberField>((INumberField<?>) model, session, id, parent);
    }
    if (model instanceof IContentAssistField<?, ?>) {
      return new JsonSmartField((IContentAssistField<?, ?>) model, session, id, parent);
    }
    if (model instanceof IProposalChooser) {
      return new JsonProposalChooser<IProposalChooser>((IProposalChooser) model, session, id, parent);
    }
    if (model instanceof IDateField) {
      return new JsonDateField<IDateField>((IDateField) model, session, id, parent);
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
      return new JsonSplitBox<ISplitBox>((ISplitBox) model, session, id, parent);
    }
    if (model instanceof IPlaceholderField) {
      return new JsonPlaceholderField<IPlaceholderField>((IPlaceholderField) model, session, id, parent);
    }
    if (model instanceof IWizardProgressField) {
      return new JsonWizardProgressField<IWizardProgressField>((IWizardProgressField) model, session, id, parent);
    }
    if (model instanceof IHtmlField) {
      return new JsonHtmlField<IHtmlField>((IHtmlField) model, session, id, parent);
    }
    if (model instanceof IComposerField) {
      return new JsonComposerField<IComposerField>((IComposerField) model, session, id, parent);
    }
    if (model instanceof IBeanField) {
      return new JsonBeanField<IBeanField<?>>((IBeanField) model, session, id, parent);
    }
    if (model instanceof IFileChooserField) {
      return new JsonFileChooserField<IFileChooserField>((IFileChooserField) model, session, id, parent);
    }
    if (model instanceof IColorField) {
      return new JsonColorField<IColorField>((IColorField) model, session, id, parent);
    }
    if (model instanceof IBrowserField) {
      return new JsonBrowserField<IBrowserField>((IBrowserField) model, session, id, parent);
    }
    if (model instanceof IClipboardField) {
      return new JsonClipboardField<IClipboardField>((IClipboardField) model, session, id, parent);
    }

    // --- other model objects ---
    if (model instanceof IDesktop) {
      return new JsonDesktop<IDesktop>((IDesktop) model, session, id, parent);
    }
    if (model instanceof IContextMenu) {
      return new JsonContextMenu<IContextMenu>((IContextMenu) model, session, id, parent);
    }
    if (model instanceof IFormToolButton<?>) {
      return new JsonFormToolButton((IFormToolButton<?>) model, session, id, parent);
    }
    if (model instanceof IMenu) {
      return new JsonMenu<IMenu>((IMenu) model, session, id, parent);
    }
    if (model instanceof IKeyStroke) {
      return new JsonKeyStroke<IKeyStroke>((IKeyStroke) model, session, id, parent);
    }
    if (model instanceof ISearchForm) {
      return new JsonSearchForm<ISearchForm>((ISearchForm) model, session, id, parent);
    }
    if (model instanceof IForm) {
      return new JsonForm<IForm>((IForm) model, session, id, parent);
    }
    if (model instanceof IMessageBox) {
      return new JsonMessageBox<IMessageBox>((IMessageBox) model, session, id, parent);
    }
    if (model instanceof IOutlineViewButton) {
      return new JsonOutlineViewButton<IOutlineViewButton>((IOutlineViewButton) model, session, id, parent);
    }
    if (model instanceof IViewButton) {
      return new JsonViewButton<IViewButton>((IViewButton) model, session, id, parent);
    }
    if (model instanceof ISearchOutline) {
      return new JsonSearchOutline<ISearchOutline>((ISearchOutline) model, session, id, parent);
    }
    if (model instanceof IOutline) {
      return new JsonOutline<IOutline>((IOutline) model, session, id, parent);
    }
    if (model instanceof ITree) {
      return new JsonTree<ITree>((ITree) model, session, id, parent);
    }
    if (model instanceof ITable) {
      ITable table = (ITable) model;
      IPage page = (IPage) table.getProperty(JsonOutlineTable.PROP_PAGE);
      if (page != null) {
        return new JsonOutlineTable<ITable>(table, session, id, parent, page);
      }
      return new JsonTable<ITable>(table, session, id, parent);
    }
    if (model instanceof IClientSession) {
      return new JsonClientSession<IClientSession>((IClientSession) model, session, id, parent);
    }
    if (model instanceof ICalendar) {
      return new JsonCalendar<ICalendar>((ICalendar) model, session, id, parent);
    }
    if (model instanceof CalendarComponent) {
      return new JsonCalendarComponent<CalendarComponent>((CalendarComponent) model, session, id, parent);
    }
    if (model instanceof IPlanner<?, ?>) {
      return new JsonPlanner<IPlanner>((IPlanner<?, ?>) model, session, id, parent);
    }
    if (model instanceof IFileChooser) {
      return new JsonFileChooser<IFileChooser>((IFileChooser) model, session, id, parent);
    }
    if (model instanceof IAggregateTableControl) { // needs to be before ITableControl
      return new JsonAggregateTableControl<IAggregateTableControl>((IAggregateTableControl) model, session, id, parent);
    }
    if (model instanceof ITableControl) {
      return new JsonTableControl<ITableControl>((ITableControl) model, session, id, parent);
    }
    return null;
  }

  @SuppressWarnings("unchecked")
  @Override
  public IJsonObject createJsonObject(Object object) {
    if (object instanceof Date) {
      return new JsonDate((Date) object);
    }
    else if (object instanceof byte[]) {
      return new JsonByteArray((byte[]) object);
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
    if (object instanceof IStringColumn) {
      return new JsonStringColumn((IStringColumn) object);
    }
    if (object instanceof IBeanColumn<?>) {
      return new JsonBeanColumn((IBeanColumn<?>) object);
    }
    if (object instanceof IIconColumn) {
      return new JsonIconColumn((IIconColumn) object);
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
    return null;
  }
}
