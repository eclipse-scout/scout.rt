/*******************************************************************************
 * Copyright (c) 2010-2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.client.ui.form.fields.smartfield2;

import java.beans.PropertyChangeListener;
import java.security.Permission;
import java.util.List;

import org.eclipse.scout.rt.client.ui.action.keystroke.IKeyStroke;
import org.eclipse.scout.rt.client.ui.action.menu.IMenu;
import org.eclipse.scout.rt.client.ui.action.menu.root.IValueFieldContextMenu;
import org.eclipse.scout.rt.client.ui.form.IForm;
import org.eclipse.scout.rt.client.ui.form.IFormFieldVisitor;
import org.eclipse.scout.rt.client.ui.form.fields.GridData;
import org.eclipse.scout.rt.client.ui.form.fields.ICompositeField;
import org.eclipse.scout.rt.client.ui.form.fields.IValidateContentDescriptor;
import org.eclipse.scout.rt.client.ui.form.fields.IValueField;
import org.eclipse.scout.rt.client.ui.form.fields.MasterListener;
import org.eclipse.scout.rt.client.ui.form.fields.groupbox.IGroupBox;
import org.eclipse.scout.rt.client.ui.form.fields.smartfield.ContentAssistFieldListener;
import org.eclipse.scout.rt.client.ui.form.fields.smartfield.IContentAssistField;
import org.eclipse.scout.rt.client.ui.form.fields.smartfield.IContentAssistFieldTable;
import org.eclipse.scout.rt.client.ui.form.fields.smartfield.IContentAssistFieldUIFacade;
import org.eclipse.scout.rt.client.ui.form.fields.smartfield.IContentAssistSearchParam;
import org.eclipse.scout.rt.client.ui.form.fields.smartfield.IProposalChooser;
import org.eclipse.scout.rt.client.ui.form.fields.smartfield.IProposalChooserProvider;
import org.eclipse.scout.rt.platform.classid.ClassId;
import org.eclipse.scout.rt.platform.job.IFuture;
import org.eclipse.scout.rt.platform.status.IMultiStatus;
import org.eclipse.scout.rt.platform.status.IStatus;
import org.eclipse.scout.rt.platform.util.TriState;
import org.eclipse.scout.rt.shared.data.basic.FontSpec;
import org.eclipse.scout.rt.shared.data.form.fields.AbstractFormFieldData;
import org.eclipse.scout.rt.shared.services.common.code.ICodeType;
import org.eclipse.scout.rt.shared.services.common.jdbc.SearchFilter;
import org.eclipse.scout.rt.shared.services.lookup.ILookupCall;
import org.eclipse.scout.rt.shared.services.lookup.ILookupRow;
import org.eclipse.scout.rt.shared.services.lookup.ILookupRowFetchedCallback;
import org.w3c.dom.Element;

// TODO [awe] 7.1 - SF2: remove this class after old smart-field has been deleted
/**
 * This adapter is used so we can use the old lookup/fetcher implementations which require the IContentAssistField
 * interface.
 *
 * @author awe
 */
@ClassId("67abbc75-74a4-4a2c-9c1c-3ab13bf2ceb2")
public class SmartField2ContentAssistAdapter<VALUE> implements IContentAssistField<VALUE, VALUE> {

  private final ISmartField2 m_field;

  public SmartField2ContentAssistAdapter(ISmartField2 field) {
    m_field = field;
  }

  @Override
  public void resetValue() {
    throw new UnsupportedOperationException();
  }

  @Override
  public void refreshDisplayText() {
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean isValueParsing() {
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean isValueChanging() {
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean isValueValidating() {
    throw new UnsupportedOperationException();
  }

  @Override
  public void addMasterListener(MasterListener listener) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void removeMasterListener(MasterListener listener) {
    throw new UnsupportedOperationException();
  }

  @Override
  public VALUE getInitValue() {
    throw new UnsupportedOperationException();
  }

  @Override
  public void setInitValue(VALUE initValue) {
    throw new UnsupportedOperationException();
  }

  @Override
  public VALUE getValue() {
    throw new UnsupportedOperationException();
  }

  @Override
  public void setValue(VALUE o) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void parseAndSetValue(String text) {
    throw new UnsupportedOperationException();
  }

  @Override
  public String getDisplayText() {
    throw new UnsupportedOperationException();
  }

  @Override
  public void setDisplayText(String s) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void fireValueChanged() {
    throw new UnsupportedOperationException();
  }

  @Override
  public IValueFieldContextMenu getContextMenu() {
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean isAutoAddDefaultMenus() {
    throw new UnsupportedOperationException();
  }

  @Override
  public void setAutoAddDefaultMenus(boolean b) {
    throw new UnsupportedOperationException();
  }

  @Override
  public IForm getForm() {
    throw new UnsupportedOperationException();
  }

  @Override
  public void addSubtreePropertyChangeListener(PropertyChangeListener listener) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void addSubtreePropertyChangeListener(String propName, PropertyChangeListener listener) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void removeSubtreePropertyChangeListener(PropertyChangeListener listener) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void removeSubtreePropertyChangeListener(String propName, PropertyChangeListener listener) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void setFormInternal(IForm form) {
    throw new UnsupportedOperationException();
  }

  @Override
  public IGroupBox getParentGroupBox() {
    throw new UnsupportedOperationException();
  }

  @Override
  public ICompositeField getParentField() {
    throw new UnsupportedOperationException();
  }

  @Override
  public void setParentFieldInternal(ICompositeField f) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void postInitConfig() {
    throw new UnsupportedOperationException();
  }

  @Override
  public void initField() {
    throw new UnsupportedOperationException();
  }

  @Override
  public void disposeField() {
    throw new UnsupportedOperationException();
  }

  @Override
  public void setView(boolean visible, boolean enabled, boolean mandatory) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void exportFormFieldData(AbstractFormFieldData target) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void importFormFieldData(AbstractFormFieldData source, boolean valueChangeTriggersEnabled) {
    throw new UnsupportedOperationException();
  }

  @Override
  public String storeToXmlString() {
    throw new UnsupportedOperationException();
  }

  @Override
  public void loadFromXmlString(String xml) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void storeToXml(Element x) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void loadFromXml(Element x) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void applySearch(SearchFilter search) {
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean hasProperty(String name) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void setFieldChanging(boolean b) {
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean isFieldChanging() {
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean isValueChangeTriggerEnabled() {
    throw new UnsupportedOperationException();
  }

  @Override
  public void setValueChangeTriggerEnabled(boolean b) {
    throw new UnsupportedOperationException();
  }

  @Override
  public String getFieldId() {
    throw new UnsupportedOperationException();
  }

  @Override
  public List<ICompositeField> getEnclosingFieldList() {
    throw new UnsupportedOperationException();
  }

  @Override
  public String getLabel() {
    throw new UnsupportedOperationException();
  }

  @Override
  public void setLabel(String name) {
    throw new UnsupportedOperationException();
  }

  @Override
  public String getInitialLabel() {
    throw new UnsupportedOperationException();
  }

  @Override
  public void setInitialLabel(String name) {
    throw new UnsupportedOperationException();
  }

  @Override
  public byte getLabelPosition() {
    throw new UnsupportedOperationException();
  }

  @Override
  public void setLabelPosition(byte pos) {
    throw new UnsupportedOperationException();
  }

  @Override
  public int getLabelWidthInPixel() {
    throw new UnsupportedOperationException();
  }

  @Override
  public void setLabelWidthInPixel(int w) {
    throw new UnsupportedOperationException();
  }

  @Override
  public byte getLabelHorizontalAlignment() {
    throw new UnsupportedOperationException();
  }

  @Override
  public void setLabelHorizontalAlignment(byte a) {
    throw new UnsupportedOperationException();
  }

  @Override
  public String getFullyQualifiedLabel(String separator) {
    throw new UnsupportedOperationException();
  }

  @Override
  public Object getProperty(String name) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void setProperty(String name, Object value) {
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean isInitialized() {
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean isMandatory() {
    throw new UnsupportedOperationException();
  }

  @Override
  public void setMandatory(boolean b) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void addErrorStatus(IStatus newStatus) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void addErrorStatus(String message) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void removeErrorStatus(Class<? extends IStatus> statusClazz) {
    throw new UnsupportedOperationException();
  }

  @Override
  public IMultiStatus getErrorStatus() {
    throw new UnsupportedOperationException();
  }

  @Override
  public void setErrorStatus(IMultiStatus status) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void clearErrorStatus() {
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean isContentValid() {
    throw new UnsupportedOperationException();
  }

  @Override
  public IValidateContentDescriptor validateContent() {
    throw new UnsupportedOperationException();
  }

  @Override
  public String getTooltipText() {
    throw new UnsupportedOperationException();
  }

  @Override
  public void setTooltipText(String text) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void updateKeyStrokes() {
    throw new UnsupportedOperationException();
  }

  @Override
  public List<IKeyStroke> getKeyStrokes() {
    throw new UnsupportedOperationException();
  }

  @Override
  public String getForegroundColor() {
    throw new UnsupportedOperationException();
  }

  @Override
  public void setForegroundColor(String c) {
    throw new UnsupportedOperationException();
  }

  @Override
  public String getBackgroundColor() {
    throw new UnsupportedOperationException();
  }

  @Override
  public void setBackgroundColor(String c) {
    throw new UnsupportedOperationException();
  }

  @Override
  public FontSpec getFont() {
    throw new UnsupportedOperationException();
  }

  @Override
  public void setFont(FontSpec f) {
    throw new UnsupportedOperationException();
  }

  @Override
  public String getLabelForegroundColor() {
    throw new UnsupportedOperationException();
  }

  @Override
  public void setLabelForegroundColor(String c) {
    throw new UnsupportedOperationException();
  }

  @Override
  public String getLabelBackgroundColor() {
    throw new UnsupportedOperationException();
  }

  @Override
  public void setLabelBackgroundColor(String c) {
    throw new UnsupportedOperationException();
  }

  @Override
  public FontSpec getLabelFont() {
    throw new UnsupportedOperationException();
  }

  @Override
  public void setLabelFont(FontSpec f) {
    throw new UnsupportedOperationException();
  }

  @Override
  public GridData getGridDataHints() {
    throw new UnsupportedOperationException();
  }

  @Override
  public void setGridDataHints(GridData data) {
    throw new UnsupportedOperationException();
  }

  @Override
  public GridData getGridData() {
    throw new UnsupportedOperationException();
  }

  @Override
  public void setGridDataInternal(GridData data) {
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean isSaveNeeded() {
    throw new UnsupportedOperationException();
  }

  @Override
  public void checkSaveNeeded() {
    throw new UnsupportedOperationException();
  }

  @Override
  public void touch() {
    throw new UnsupportedOperationException();
  }

  @Override
  public void markSaved() {
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean isEmpty() {
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean isPreventInitialFocus() {
    throw new UnsupportedOperationException();
  }

  @Override
  public void setPreventInitialFocus(boolean preventInitialFocus) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void requestFocus() {
    throw new UnsupportedOperationException();
  }

  @Override
  public IValueField getMasterField() {
    throw new UnsupportedOperationException();
  }

  @Override
  public void setMasterField(IValueField field) {
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean isMasterRequired() {
    throw new UnsupportedOperationException();
  }

  @Override
  public void setMasterRequired(boolean b) {
    throw new UnsupportedOperationException();
  }

  @Override
  public Object getMasterValue() {
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean isStatusVisible() {
    throw new UnsupportedOperationException();
  }

  @Override
  public void setStatusVisible(boolean statusVisible) {
    throw new UnsupportedOperationException();
  }

  @Override
  public String getStatusPosition() {
    throw new UnsupportedOperationException();
  }

  @Override
  public void setStatusPosition(String statusPosition) {
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean isMandatoryFulfilled() {
    throw new UnsupportedOperationException();
  }

  @Override
  public void setLoading(boolean loading) {
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean isLoading() {
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean isLabelVisible() {
    throw new UnsupportedOperationException();
  }

  @Override
  public void setLabelVisible(boolean b) {
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean isLabelVisible(String dimension) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void setLabelVisible(boolean visible, String dimension) {
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean isVisibleIncludingParents() {
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean isVisible() {
    throw new UnsupportedOperationException();
  }

  @Override
  public void setVisible(boolean b) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void setVisible(boolean visible, boolean updateParents) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void setVisible(boolean visible, boolean updateParents, boolean updateChildren) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void setVisiblePermission(Permission p) {
    throw new UnsupportedOperationException();
  }

  @Override
  public Permission getVisiblePermission() {
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean isVisibleGranted() {
    throw new UnsupportedOperationException();
  }

  @Override
  public void setVisibleGranted(boolean b) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void setVisibleGranted(boolean visible, boolean updateParents) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void setVisibleGranted(boolean visible, boolean updateParents, boolean updateChildren) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void setVisible(boolean visible, String dimension) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void setVisible(boolean visible, boolean updateParents, String dimension) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void setVisible(boolean visible, boolean updateParents, boolean updateChildren, String dimension) {
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean isEnabledIncludingParents() {
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean isEnabled() {
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean getEnabledProperty() {
    throw new UnsupportedOperationException();
  }

  @Override
  public void setEnabled(boolean b) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void setEnabled(boolean enabled, boolean updateParents) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void setEnabled(boolean enabled, boolean updateParents, boolean updateChildren) {
    throw new UnsupportedOperationException();
  }

  @Override
  public Permission getEnabledPermission() {
    throw new UnsupportedOperationException();
  }

  @Override
  public void setEnabledPermission(Permission p) {
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean isEnabledGranted() {
    throw new UnsupportedOperationException();
  }

  @Override
  public void setEnabledGranted(boolean b) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void setEnabledGranted(boolean enabled, boolean updateParents) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void setEnabledGranted(boolean enabled, boolean updateParents, boolean updateChildren) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void setEnabled(boolean enabled, String dimension) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void setEnabled(boolean enabled, boolean updateParents, String dimension) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void setEnabled(boolean enabled, boolean updateParents, boolean updateChildren, String dimension) {
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean acceptVisitor(IFormFieldVisitor visitor, int level, int fieldIndex, boolean includeThis) {
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean visitParents(IFormFieldVisitor visitor) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void setDisabledStyle(int disabledStyle) {
    throw new UnsupportedOperationException();
  }

  @Override
  public int getDisabledStyle() {
    throw new UnsupportedOperationException();
  }

  @Override
  public void addPropertyChangeListener(PropertyChangeListener listener) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void addPropertyChangeListener(String propertyName, PropertyChangeListener listener) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void removePropertyChangeListener(PropertyChangeListener listener) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void removePropertyChangeListener(String propertyName, PropertyChangeListener listener) {
    throw new UnsupportedOperationException();
  }

  @Override
  public String classId() {
    throw new UnsupportedOperationException();
  }

  @Override
  public double getOrder() {
    throw new UnsupportedOperationException();
  }

  @Override
  public void setOrder(double order) {
    throw new UnsupportedOperationException();
  }

  @Override
  public String getCssClass() {
    throw new UnsupportedOperationException();
  }

  @Override
  public void setCssClass(String cssClass) {
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean isVisible(String dimension) {
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean isEnabled(String dimension) {
    throw new UnsupportedOperationException();
  }

  @Override
  public Class<VALUE> getHolderType() {
    throw new UnsupportedOperationException();
  }

  @Override
  public List<IMenu> getMenus() {
    throw new UnsupportedOperationException();
  }

  @Override
  public <T extends IMenu> T getMenuByClass(Class<T> menuType) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void addSmartFieldListener(ContentAssistFieldListener listener) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void removeSmartFieldListener(ContentAssistFieldListener listener) {
    throw new UnsupportedOperationException();
  }

  @Override
  public IProposalChooser<?, VALUE> getProposalChooser() {
    throw new UnsupportedOperationException();
  }

  @Override
  public IProposalChooserProvider<VALUE> getProposalChooserProvider() {
    throw new UnsupportedOperationException();
  }

  @Override
  public void setProposalChooserProvider(IProposalChooserProvider<VALUE> provider) {
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean isActiveFilterEnabled() {
    throw new UnsupportedOperationException();
  }

  @Override
  public void setActiveFilterEnabled(boolean enabled) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void setActiveFilterLabel(TriState state, String label) {
    throw new UnsupportedOperationException();
  }

  @Override
  public String[] getActiveFilterLabels() {
    throw new UnsupportedOperationException();
  }

  @Override
  public TriState getActiveFilter() {
    throw new UnsupportedOperationException();
  }

  @Override
  public void setActiveFilter(TriState state) {
    throw new UnsupportedOperationException();
  }

  @Override
  public String getBrowseIconId() {
    throw new UnsupportedOperationException();
  }

  @Override
  public void setBrowseIconId(String s) {
    throw new UnsupportedOperationException();
  }

  @Override
  public int getBrowseMaxRowCount() {
    return m_field.getBrowseMaxRowCount();
  }

  @Override
  public void setBrowseMaxRowCount(int n) {
    throw new UnsupportedOperationException();
  }

  @Override
  public String getIconId() {
    throw new UnsupportedOperationException();
  }

  @Override
  public void setIconId(String s) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void setMultilineText(boolean b) {
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean isMultilineText() {
    throw new UnsupportedOperationException();
  }

  @Override
  public void applyLazyStyles() {
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean isBrowseAutoExpandAll() {
    throw new UnsupportedOperationException();
  }

  @Override
  public void setBrowseAutoExpandAll(boolean b) {
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean isBrowseHierarchy() {
    throw new UnsupportedOperationException();
  }

  @Override
  public void setBrowseHierarchy(boolean b) {
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean isBrowseLoadIncremental() {
    throw new UnsupportedOperationException();
  }

  @Override
  public void setBrowseLoadIncremental(boolean b) {
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean isLoadParentNodes() {
    throw new UnsupportedOperationException();
  }

  @Override
  public void setLoadParentNodes(boolean b) {
    throw new UnsupportedOperationException();
  }

  @Override
  public String getBrowseNewText() {
    throw new UnsupportedOperationException();
  }

  @Override
  public void setBrowseNewText(String s) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void doBrowseNew(String newText) {
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean acceptBrowseHierarchySelection(VALUE value, int level, boolean leaf) {
    throw new UnsupportedOperationException();
  }

  @Override
  public Class<? extends ICodeType<?, VALUE>> getCodeTypeClass() {
    throw new UnsupportedOperationException();
  }

  @Override
  public void setCodeTypeClass(Class<? extends ICodeType<?, VALUE>> codeType) {
    throw new UnsupportedOperationException();
  }

  @Override
  public ILookupCall<VALUE> getLookupCall() {
    throw new UnsupportedOperationException();
  }

  @Override
  public void setLookupCall(ILookupCall<VALUE> call) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void prepareKeyLookup(ILookupCall<VALUE> call, VALUE key) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void prepareTextLookup(ILookupCall<VALUE> call, String text) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void prepareBrowseLookup(ILookupCall<VALUE> call, String browseHint, TriState activeState) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void prepareRecLookup(ILookupCall<VALUE> call, VALUE parentKey, TriState activeState) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void setUniquelyDefinedValue(boolean background) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void clearProposal() {
    throw new UnsupportedOperationException();
  }

  @Override
  public void acceptProposal(ILookupRow<VALUE> row) {
    throw new UnsupportedOperationException();
  }

  @Override
  public IContentAssistFieldUIFacade getUIFacade() {
    throw new UnsupportedOperationException();
  }

  @Override
  public VALUE getValueAsLookupKey() {
    throw new UnsupportedOperationException();
  }

  @Override
  public void setProposalFormHeight(int proposalFormHeight) {
    throw new UnsupportedOperationException();
  }

  @Override
  public int getProposalFormHeight() {
    throw new UnsupportedOperationException();
  }

  @Override
  public Class<? extends IContentAssistFieldTable<VALUE>> getContentAssistFieldTableClass() {
    throw new UnsupportedOperationException();
  }

  @Override
  public void acceptProposal() {
    throw new UnsupportedOperationException();
  }

  @Override
  public void setWildcard(String wildcard) {
    throw new UnsupportedOperationException();
  }

  @Override
  public String getWildcard() {
    return m_field.getWildcard();
  }

  @Override
  public void doSearch(boolean selectCurrentValue, boolean synchronous) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void doSearch(String searchText, boolean selectCurrentValue, boolean synchronous) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void doSearch(IContentAssistSearchParam<VALUE> param, boolean synchronous) {
    throw new UnsupportedOperationException();
  }

  @Override
  public List<? extends ILookupRow<VALUE>> callKeyLookup(VALUE key) {
    throw new UnsupportedOperationException();
  }

  @Override
  @SuppressWarnings("unchecked")
  public List<? extends ILookupRow<VALUE>> callTextLookup(String text, int maxRowCount) {
    return m_field.callTextLookup(text, maxRowCount);
  }

  @Override
  @SuppressWarnings("unchecked")
  public List<? extends ILookupRow<VALUE>> callBrowseLookup(String browseHint, int maxRowCount) {
    return m_field.callBrowseLookup(browseHint, maxRowCount);
  }

  @Override
  public List<? extends ILookupRow<VALUE>> callBrowseLookup(String browseHint, int maxRowCount, TriState activeState) {
    throw new UnsupportedOperationException();
  }

  @Override
  public List<ILookupRow<VALUE>> callSubTreeLookup(VALUE parentKey) {
    throw new UnsupportedOperationException();
  }

  @Override
  public List<ILookupRow<VALUE>> callSubTreeLookup(VALUE parentKey, TriState activeState) {
    throw new UnsupportedOperationException();
  }

  @Override
  public IFuture<List<ILookupRow<VALUE>>> callKeyLookupInBackground(VALUE key, boolean cancelRunningJobs) {
    throw new UnsupportedOperationException();
  }

  @Override
  @SuppressWarnings("unchecked")
  public IFuture<List<ILookupRow<VALUE>>> callTextLookupInBackground(String text, boolean cancelRunningJobs) {
    return m_field.callTextLookupInBackground(text, cancelRunningJobs);
  }

  @Override
  @SuppressWarnings("unchecked")
  public IFuture<List<ILookupRow<VALUE>>> callBrowseLookupInBackground(boolean cancelRunningJobs) {
    return m_field.callBrowseLookupInBackground(cancelRunningJobs);
  }

  @Override
  public IFuture<List<ILookupRow<VALUE>>> callBrowseLookupInBackground(String browseHint, boolean cancelRunningJobs) {
    throw new UnsupportedOperationException();
  }

  @Override
  @SuppressWarnings("unchecked")
  public IFuture<List<ILookupRow<VALUE>>> callSubTreeLookupInBackground(VALUE parentKey, boolean cancelRunningJobs) {
    return m_field.callSubTreeLookupInBackground(parentKey, cancelRunningJobs);
  }

  @Override
  public IFuture<List<ILookupRow<VALUE>>> callSubTreeLookupInBackground(VALUE parentKey, TriState activeState, boolean cancelRunningJobs) {
    throw new UnsupportedOperationException();
  }

  @Override
  public IFuture<Void> callKeyLookupInBackground(VALUE key, ILookupRowFetchedCallback<VALUE> callback) {
    throw new UnsupportedOperationException();
  }

  @Override
  @SuppressWarnings("unchecked")
  public IFuture<Void> callTextLookupInBackground(String text, int maxRowCount, ILookupRowFetchedCallback<VALUE> callback) {
    return m_field.callTextLookupInBackground(text, maxRowCount, callback);
  }

  @Override
  @SuppressWarnings("unchecked")
  public IFuture<Void> callBrowseLookupInBackground(String browseHint, int maxRowCount, ILookupRowFetchedCallback<VALUE> callback) {
    return m_field.callBrowseLookupInBackground(browseHint, maxRowCount, callback);
  }

  @Override
  public IFuture<Void> callBrowseLookupInBackground(String browseHint, int maxRowCount, TriState activeState, ILookupRowFetchedCallback<VALUE> callback) {
    throw new UnsupportedOperationException();
  }

}
