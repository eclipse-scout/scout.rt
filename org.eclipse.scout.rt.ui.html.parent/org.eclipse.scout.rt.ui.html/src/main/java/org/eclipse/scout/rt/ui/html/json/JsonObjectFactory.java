/*******************************************************************************
 * Copyright (c) 2010 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.ui.html.json;

import org.eclipse.scout.rt.client.IClientSession;
import org.eclipse.scout.rt.client.ui.action.keystroke.IKeyStroke;
import org.eclipse.scout.rt.client.ui.action.menu.IMenu;
import org.eclipse.scout.rt.client.ui.action.menu.root.IContextMenu;
import org.eclipse.scout.rt.client.ui.basic.activitymap.IActivityMap;
import org.eclipse.scout.rt.client.ui.basic.calendar.ICalendar;
import org.eclipse.scout.rt.client.ui.basic.table.ITable;
import org.eclipse.scout.rt.client.ui.basic.table.columns.IColumn;
import org.eclipse.scout.rt.client.ui.basic.table.columns.IDateColumn;
import org.eclipse.scout.rt.client.ui.basic.table.columns.INumberColumn;
import org.eclipse.scout.rt.client.ui.basic.table.control.IAnalysisTableControl;
import org.eclipse.scout.rt.client.ui.basic.table.control.IChartTableControl;
import org.eclipse.scout.rt.client.ui.basic.table.control.IGraphTableControl;
import org.eclipse.scout.rt.client.ui.basic.table.control.IMapTableControl;
import org.eclipse.scout.rt.client.ui.basic.table.control.ITableControl;
import org.eclipse.scout.rt.client.ui.basic.table.menus.TableOrganizeMenu;
import org.eclipse.scout.rt.client.ui.basic.tree.ITree;
import org.eclipse.scout.rt.client.ui.desktop.IDesktop;
import org.eclipse.scout.rt.client.ui.desktop.outline.IFormToolButton;
import org.eclipse.scout.rt.client.ui.desktop.outline.IOutline;
import org.eclipse.scout.rt.client.ui.desktop.outline.IOutlineViewButton;
import org.eclipse.scout.rt.client.ui.desktop.outline.ISearchOutline;
import org.eclipse.scout.rt.client.ui.form.IForm;
import org.eclipse.scout.rt.client.ui.form.fields.IFormField;
import org.eclipse.scout.rt.client.ui.form.fields.booleanfield.IBooleanField;
import org.eclipse.scout.rt.client.ui.form.fields.button.IButton;
import org.eclipse.scout.rt.client.ui.form.fields.button.IRadioButton;
import org.eclipse.scout.rt.client.ui.form.fields.calendarfield.ICalendarField;
import org.eclipse.scout.rt.client.ui.form.fields.datefield.IDateField;
import org.eclipse.scout.rt.client.ui.form.fields.groupbox.IGroupBox;
import org.eclipse.scout.rt.client.ui.form.fields.imagebox.IImageField;
import org.eclipse.scout.rt.client.ui.form.fields.labelfield.ILabelField;
import org.eclipse.scout.rt.client.ui.form.fields.listbox.IListBox;
import org.eclipse.scout.rt.client.ui.form.fields.numberfield.INumberField;
import org.eclipse.scout.rt.client.ui.form.fields.placeholder.IPlaceholderField;
import org.eclipse.scout.rt.client.ui.form.fields.plannerfield.IPlannerField;
import org.eclipse.scout.rt.client.ui.form.fields.radiobuttongroup.IRadioButtonGroup;
import org.eclipse.scout.rt.client.ui.form.fields.richtextfield.IRichTextField;
import org.eclipse.scout.rt.client.ui.form.fields.sequencebox.ISequenceBox;
import org.eclipse.scout.rt.client.ui.form.fields.smartfield.IContentAssistField;
import org.eclipse.scout.rt.client.ui.form.fields.smartfield.IProposalChooser;
import org.eclipse.scout.rt.client.ui.form.fields.splitbox.ISplitBox;
import org.eclipse.scout.rt.client.ui.form.fields.stringfield.IStringField;
import org.eclipse.scout.rt.client.ui.form.fields.tabbox.ITabBox;
import org.eclipse.scout.rt.client.ui.form.fields.tablefield.ITableField;
import org.eclipse.scout.rt.client.ui.form.fields.tagcloudfield.ITagCloudField;
import org.eclipse.scout.rt.client.ui.form.fields.treebox.ITreeBox;
import org.eclipse.scout.rt.client.ui.form.fields.treefield.ITreeField;
import org.eclipse.scout.rt.client.ui.form.fields.wrappedform.IWrappedFormField;
import org.eclipse.scout.rt.client.ui.messagebox.IMessageBox;
import org.eclipse.scout.rt.shared.data.model.IDataModel;
import org.eclipse.scout.rt.ui.html.json.action.keystroke.JsonKeyStroke;
import org.eclipse.scout.rt.ui.html.json.basic.activitymap.JsonActivityMap;
import org.eclipse.scout.rt.ui.html.json.calendar.JsonCalendar;
import org.eclipse.scout.rt.ui.html.json.desktop.JsonDesktop;
import org.eclipse.scout.rt.ui.html.json.desktop.JsonFormToolButton;
import org.eclipse.scout.rt.ui.html.json.desktop.JsonOutline;
import org.eclipse.scout.rt.ui.html.json.desktop.JsonOutlineViewButton;
import org.eclipse.scout.rt.ui.html.json.desktop.JsonSearchOutline;
import org.eclipse.scout.rt.ui.html.json.form.JsonForm;
import org.eclipse.scout.rt.ui.html.json.form.fields.JsonDateField;
import org.eclipse.scout.rt.ui.html.json.form.fields.JsonFormField;
import org.eclipse.scout.rt.ui.html.json.form.fields.button.JsonButton;
import org.eclipse.scout.rt.ui.html.json.form.fields.calendar.JsonCalendarField;
import org.eclipse.scout.rt.ui.html.json.form.fields.checkbox.JsonCheckBoxField;
import org.eclipse.scout.rt.ui.html.json.form.fields.groupbox.JsonGroupBox;
import org.eclipse.scout.rt.ui.html.json.form.fields.imagefield.JsonImageField;
import org.eclipse.scout.rt.ui.html.json.form.fields.labelfield.JsonLabelField;
import org.eclipse.scout.rt.ui.html.json.form.fields.listbox.JsonListBox;
import org.eclipse.scout.rt.ui.html.json.form.fields.numberfield.JsonNumberField;
import org.eclipse.scout.rt.ui.html.json.form.fields.placeholder.JsonPlaceholderField;
import org.eclipse.scout.rt.ui.html.json.form.fields.plannerfield.JsonPlannerField;
import org.eclipse.scout.rt.ui.html.json.form.fields.radiobutton.JsonRadioButton;
import org.eclipse.scout.rt.ui.html.json.form.fields.radiobutton.JsonRadioButtonGroup;
import org.eclipse.scout.rt.ui.html.json.form.fields.richtextfield.JsonRichTextField;
import org.eclipse.scout.rt.ui.html.json.form.fields.sequencebox.JsonSequenceBox;
import org.eclipse.scout.rt.ui.html.json.form.fields.smartfield.JsonProposalChooser;
import org.eclipse.scout.rt.ui.html.json.form.fields.smartfield.JsonSmartField;
import org.eclipse.scout.rt.ui.html.json.form.fields.splitbox.JsonSplitBox;
import org.eclipse.scout.rt.ui.html.json.form.fields.stringfield.JsonStringField;
import org.eclipse.scout.rt.ui.html.json.form.fields.tabbox.JsonTabBox;
import org.eclipse.scout.rt.ui.html.json.form.fields.tabbox.JsonTabItem;
import org.eclipse.scout.rt.ui.html.json.form.fields.tablefield.JsonTableField;
import org.eclipse.scout.rt.ui.html.json.form.fields.tagcloudfield.JsonTagCloudField;
import org.eclipse.scout.rt.ui.html.json.form.fields.treebox.JsonTreeBox;
import org.eclipse.scout.rt.ui.html.json.form.fields.treefield.JsonTreeField;
import org.eclipse.scout.rt.ui.html.json.form.fields.wrappedform.JsonWrappedFormField;
import org.eclipse.scout.rt.ui.html.json.menu.JsonContextMenu;
import org.eclipse.scout.rt.ui.html.json.menu.JsonMenu;
import org.eclipse.scout.rt.ui.html.json.messagebox.JsonMessageBox;
import org.eclipse.scout.rt.ui.html.json.table.JsonColumn;
import org.eclipse.scout.rt.ui.html.json.table.JsonDateColumn;
import org.eclipse.scout.rt.ui.html.json.table.JsonNumberColumn;
import org.eclipse.scout.rt.ui.html.json.table.JsonTable;
import org.eclipse.scout.rt.ui.html.json.table.JsonTableOrganizeMenu;
import org.eclipse.scout.rt.ui.html.json.table.control.JsonAnalysisTableControl;
import org.eclipse.scout.rt.ui.html.json.table.control.JsonChartTableControl;
import org.eclipse.scout.rt.ui.html.json.table.control.JsonGraphTableControl;
import org.eclipse.scout.rt.ui.html.json.table.control.JsonMapTableControl;
import org.eclipse.scout.rt.ui.html.json.table.control.JsonTableControl;
import org.eclipse.scout.rt.ui.html.json.tree.JsonTree;

/**
 * This factory creates IJsonAdapter instances for a given model object. You must call the <code>init()</code> method
 * on the return value from <code>createJsonAdapter()</code>.
 */
public class JsonObjectFactory implements IJsonObjectFactory {

  @Override
  @SuppressWarnings("unchecked")
  public IJsonObject createJsonObject(Object model, IJsonSession session, String id, IJsonAdapter<?> parent) {
    // --- form fields ----
    if (model instanceof IGroupBox) {
      // we must distinct between normal group-boxes and group-boxes in tab-boxes
      // the use the same model, but we need different adapters
      IGroupBox groupBox = (IGroupBox) model;
      if (groupBox.getParentField() instanceof ITabBox) {
        return new JsonTabItem(groupBox, session, id, parent);
      }
      else {
        return new JsonGroupBox(groupBox, session, id, parent);
      }
    }
    if (model instanceof ISequenceBox) {
      return new JsonSequenceBox((ISequenceBox) model, session, id, parent);
    }
    if (model instanceof ITabBox) {
      return new JsonTabBox((ITabBox) model, session, id, parent);
    }
    if (model instanceof IBooleanField) {
      return new JsonCheckBoxField((IBooleanField) model, session, id, parent);
    }
    if (model instanceof ILabelField) {
      return new JsonLabelField((ILabelField) model, session, id, parent);
    }
    if (model instanceof IImageField) {
      return new JsonImageField((IImageField) model, session, id, parent);
    }
    if (model instanceof ITableField<?>) {
      return new JsonTableField((ITableField<?>) model, session, id, parent);
    }
    if (model instanceof IListBox<?>) {
      return new JsonListBox((IListBox<?>) model, session, id, parent);
    }
    if (model instanceof ITreeField) {
      return new JsonTreeField((ITreeField) model, session, id, parent);
    }
    if (model instanceof ITreeBox<?>) {
      return new JsonTreeBox((ITreeBox<?>) model, session, id, parent);
    }
    if (model instanceof IRadioButton<?>) {
      return new JsonRadioButton((IRadioButton<?>) model, session, id, parent);
    }
    if (model instanceof IRadioButtonGroup<?>) {
      return new JsonRadioButtonGroup((IRadioButtonGroup<?>) model, session, id, parent);
    }
    if (model instanceof IButton) {
      return new JsonButton((IButton) model, session, id, parent);
    }
    if (model instanceof IRichTextField) {
      return new JsonRichTextField((IRichTextField) model, session, id, parent);
    }
    if (model instanceof ITagCloudField) {
      return new JsonTagCloudField((ITagCloudField) model, session, id, parent);
    }
    if (model instanceof IStringField) {
      return new JsonStringField((IStringField) model, session, id, parent);
    }
    if (model instanceof INumberField<?>) {
      return new JsonNumberField((INumberField<?>) model, session, id, parent);
    }
    if (model instanceof IContentAssistField<?, ?>) {
      return new JsonSmartField((IContentAssistField<?, ?>) model, session, id, parent);
    }
    if (model instanceof IProposalChooser) {
      return new JsonProposalChooser((IProposalChooser) model, session, id, parent);
    }
    if (model instanceof IDateField) {
      return new JsonDateField((IDateField) model, session, id, parent);
    }
    if (model instanceof ICalendarField<?>) {
      return new JsonCalendarField((ICalendarField<?>) model, session, id, parent);
    }
    if (model instanceof IPlannerField<?, ?, ?, ?>) {
      return new JsonPlannerField((IPlannerField<?, ?, ?, ?>) model, session, id, parent);
    }
    if (model instanceof IWrappedFormField<?>) {
      return new JsonWrappedFormField((IWrappedFormField<?>) model, session, id, parent);
    }
    if (model instanceof ISplitBox) {
      return new JsonSplitBox((ISplitBox) model, session, id, parent);
    }
    if (model instanceof IPlaceholderField) {
      return new JsonPlaceholderField((IPlaceholderField) model, session, id, parent);
    }

    // Fallback for all other form fields (will generate a 'not implemented' placeholder). Note: insert more form fields _before_ this line!
    if (model instanceof IFormField) {
      return new JsonFormField((IFormField) model, session, id, parent);
    }

    // --- other model objects ---
    if (model instanceof IDesktop) {
      return new JsonDesktop((IDesktop) model, session, id, parent);
    }
    if (model instanceof IContextMenu) {
      return new JsonContextMenu((IContextMenu) model, session, id, parent);
    }
    if (model instanceof TableOrganizeMenu) { // needs to be before IMenu
      return new JsonTableOrganizeMenu((TableOrganizeMenu) model, session, id, parent);
    }
    if (model instanceof IMenu) {
      return new JsonMenu((IMenu) model, session, id, parent);
    }
    if (model instanceof IKeyStroke) {
      return new JsonKeyStroke((IKeyStroke) model, session, id, parent);
    }
    if (model instanceof IForm) {
      return new JsonForm((IForm) model, session, id, parent);
    }
    if (model instanceof IMessageBox) {
      return new JsonMessageBox((IMessageBox) model, session, id, parent);
    }
    if (model instanceof IOutlineViewButton) {
      return new JsonOutlineViewButton((IOutlineViewButton) model, session, id, parent);
    }
    if (model instanceof IFormToolButton<?>) {
      return new JsonFormToolButton((IFormToolButton<?>) model, session, id, parent);
    }
    if (model instanceof ISearchOutline) {
      return new JsonSearchOutline((ISearchOutline) model, session, id, parent);
    }
    if (model instanceof IOutline) {
      return new JsonOutline((IOutline) model, session, id, parent);
    }
    if (model instanceof ITree) {
      return new JsonTree((ITree) model, session, id, parent);
    }
    if (model instanceof ITable) {
      return new JsonTable((ITable) model, session, id, parent);
    }
    if (model instanceof IClientSession) {
      return new JsonClientSession((IClientSession) model, session, id, parent);
    }
    if (model instanceof IDataModel) {
      return new JsonDataModel((IDataModel) model, session, id, parent);
    }
    if (model instanceof ICalendar) {
      return new JsonCalendar((ICalendar) model, session, id, parent);
    }
    if (model instanceof IActivityMap<?, ?>) {
      return new JsonActivityMap((IActivityMap<?, ?>) model, session, id, parent);
    }

    if (model instanceof INumberColumn<?>) {
      return new JsonNumberColumn((INumberColumn<?>) model, session);
    }
    if (model instanceof IDateColumn) {
      return new JsonDateColumn((IDateColumn) model, session);
    }
    if (model instanceof IColumn<?>) {
      return new JsonColumn((IColumn<?>) model, session);
    }

    if (model instanceof IChartTableControl) { // needs to be before ITableControl
      return new JsonChartTableControl((IChartTableControl) model, session, id, parent);
    }
    if (model instanceof IGraphTableControl) { // needs to be before ITableControl
      return new JsonGraphTableControl((IGraphTableControl) model, session, id, parent);
    }
    if (model instanceof IMapTableControl) { // needs to be before ITableControl
      return new JsonMapTableControl((IMapTableControl) model, session, id, parent);
    }
    if (model instanceof IAnalysisTableControl) { // needs to be before ITableControl
      return new JsonAnalysisTableControl((IAnalysisTableControl) model, session, id, parent);
    }
    if (model instanceof ITableControl) {
      return new JsonTableControl((ITableControl) model, session, id, parent);
    }

    throw new IllegalArgumentException("Cannot create JSON adapter for model-object " + model);
  }
}
