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
package org.eclipse.scout.rt.client.ui.form.fields.radiobuttongroup;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.scout.commons.CompareUtility;
import org.eclipse.scout.commons.ConfigurationUtility;
import org.eclipse.scout.commons.TriState;
import org.eclipse.scout.commons.TypeCastUtility;
import org.eclipse.scout.commons.annotations.ConfigOperation;
import org.eclipse.scout.commons.annotations.ConfigProperty;
import org.eclipse.scout.commons.annotations.ConfigPropertyValue;
import org.eclipse.scout.commons.annotations.Order;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.commons.holders.Holder;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.client.services.lookup.FormFieldProvisioningContext;
import org.eclipse.scout.rt.client.services.lookup.ILookupCallProvisioningService;
import org.eclipse.scout.rt.client.ui.form.IForm;
import org.eclipse.scout.rt.client.ui.form.IFormFieldVisitor;
import org.eclipse.scout.rt.client.ui.form.fields.AbstractValueField;
import org.eclipse.scout.rt.client.ui.form.fields.ICompositeField;
import org.eclipse.scout.rt.client.ui.form.fields.IFormField;
import org.eclipse.scout.rt.client.ui.form.fields.button.AbstractButton;
import org.eclipse.scout.rt.client.ui.form.fields.button.IButton;
import org.eclipse.scout.rt.client.ui.form.fields.groupbox.AbstractGroupBox;
import org.eclipse.scout.rt.client.ui.form.fields.radiobuttongroup.internal.RadioButtonGroupGrid;
import org.eclipse.scout.rt.shared.data.form.ValidationRule;
import org.eclipse.scout.rt.shared.services.common.code.CODES;
import org.eclipse.scout.rt.shared.services.common.code.ICodeType;
import org.eclipse.scout.rt.shared.services.common.exceptionhandler.IExceptionHandlerService;
import org.eclipse.scout.rt.shared.services.lookup.CodeLookupCall;
import org.eclipse.scout.rt.shared.services.lookup.LookupCall;
import org.eclipse.scout.rt.shared.services.lookup.LookupRow;
import org.eclipse.scout.service.SERVICES;

/**
 * The listbox value is a Object[] where the Object[] is the set of selected
 * keys of the listbox<br>
 * the inner table shows those rows as selected which have the key value as a
 * part of the listbox value (Object[])
 * <p>
 * Note, that the listbox might not necessarily show all selected rows since the value of the listbox might contain
 * inactive keys that are not reflected in the listbox<br>
 * Therefore an empty listbox table is not the same as a listbox with an empty value (null)
 */
public abstract class AbstractRadioButtonGroup<T> extends AbstractValueField<T> implements IRadioButtonGroup<T>, ICompositeField {
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(AbstractRadioButtonGroup.class);

  private boolean m_valueAndSelectionMediatorActive;
  private LookupCall m_lookupCall;
  private Class<? extends ICodeType> m_codeTypeClass;
  private RadioButtonGroupGrid m_grid;
  private IFormField[] m_fields;
  private IButton[] m_radioButtons;

  public AbstractRadioButtonGroup() {
    this(true);
  }

  public AbstractRadioButtonGroup(boolean callInitializer) {
    super(callInitializer);
  }

  /*
   * Configuration
   */

  /**
   * Use a {@link LookupCall} which is able to handle {@link LookupCall#getDataByAll()}
   */
  @ConfigProperty(ConfigProperty.LOOKUP_CALL)
  @Order(240)
  @ConfigPropertyValue("null")
  @ValidationRule(ValidationRule.LOOKUP_CALL)
  protected Class<? extends LookupCall> getConfiguredLookupCall() {
    return null;
  }

  /**
   * All codes returned by the configured {@link ICodeType} are used as radio
   * buttons.<br>
   * However, the {@link #execFilterLookupResult(LookupCall, List)} can be used
   * to manipulate the codes which shall be used.
   */
  @ConfigProperty(ConfigProperty.CODE_TYPE)
  @Order(250)
  @ConfigPropertyValue("null")
  @ValidationRule(ValidationRule.CODE_TYPE)
  protected Class<? extends ICodeType> getConfiguredCodeType() {
    return null;
  }

  /**
   * Called before any lookup is performed
   */
  @ConfigOperation
  @Order(260)
  protected void execPrepareLookup(LookupCall call) {
  }

  /**
   * @param call
   *          that produced this result
   * @param result
   *          live list containing the result rows. Add, remove, set, replace
   *          and clear of entries in this list is supported
   */
  @ConfigOperation
  @Order(270)
  protected void execFilterLookupResult(LookupCall call, List<LookupRow> result) throws ProcessingException {
  }

  @Override
  protected void initConfig() {
    m_fields = new IFormField[0];
    m_grid = new RadioButtonGroupGrid(this);
    super.initConfig();
    // Configured CodeType
    if (getConfiguredCodeType() != null) {
      setCodeTypeClass(getConfiguredCodeType());
    }
    // Configured LookupCall
    Class<? extends LookupCall> lookupCallClass = getConfiguredLookupCall();
    if (lookupCallClass != null) {
      try {
        LookupCall call = lookupCallClass.newInstance();
        setLookupCall(call);
      }
      catch (Exception e) {
        SERVICES.getService(IExceptionHandlerService.class).handleException(new ProcessingException(this.getClass().getSimpleName(), e));
      }
    }
    // add fields
    ArrayList<IFormField> fieldList = new ArrayList<IFormField>();
    Class<? extends IFormField>[] fieldArray = getConfiguredFields();
    for (int i = 0; i < fieldArray.length; i++) {
      IFormField f;
      try {
        f = ConfigurationUtility.newInnerInstance(this, fieldArray[i]);
        fieldList.add(f);
      }// end try
      catch (Throwable t) {
        SERVICES.getService(IExceptionHandlerService.class).handleException(new ProcessingException("field: " + fieldArray[i].getName(), t));
      }
    }
    injectFieldsInternal(fieldList);
    for (IFormField f : fieldList) {
      f.setParentFieldInternal(this);
    }
    m_fields = fieldList.toArray(new IFormField[0]);
    //attach a proxy controller to each child field in the group for: visible, saveNeeded
    for (IFormField f : m_fields) {
      f.addPropertyChangeListener(new P_FieldPropertyChangeListenerEx());
    }
    //extract buttons from field subtree
    ArrayList<IButton> buttonList = new ArrayList<IButton>();
    for (IFormField f : m_fields) {
      IButton b = findFirstButtonInFieldTree(f);
      if (b != null) {
        buttonList.add(b);
      }
    }
    m_radioButtons = buttonList.toArray(new IButton[buttonList.size()]);
    //decorate radiobuttons
    for (IButton b : m_radioButtons) {
      b.setDisplayStyleInternal(IButton.DISPLAY_STYLE_RADIO);
      b.addPropertyChangeListener(new P_ButtonPropertyChangeListener());
    }
    handleFieldVisibilityChanged();
  }

  private IButton findFirstButtonInFieldTree(IFormField f) {
    if (f instanceof IButton) {
      return (IButton) f;
    }
    else if (f instanceof ICompositeField) {
      for (IFormField sub : ((ICompositeField) f).getFields()) {
        IButton b = findFirstButtonInFieldTree(sub);
        if (b != null) {
          return b;
        }
      }
    }
    return null;
  }

  /**
   * do not use this internal method<br>
   * Used to manage field list and add/remove fields (see {@link AbstractGroupBox} with wizard buttons)
   * 
   * @param fieldList
   *          live and mutable list of configured fields, not yet initialized
   *          and added to composite field
   */
  protected void injectFieldsInternal(List<IFormField> fieldList) {
    if (getLookupCall() != null) {
      // Use the LookupCall
      try {
        LookupRow[] lookupRows = getLookupRows();
        for (int i = 0; i < lookupRows.length; i++) {
          RadioButton radioButton = new RadioButton();
          radioButton.setEnabled(lookupRows[i].isEnabled());
          radioButton.setLabel(lookupRows[i].getText());
          radioButton.setRadioValue(lookupRows[i].getKey());
          radioButton.setTooltipText(lookupRows[i].getTooltipText());
          radioButton.setBackgroundColor(lookupRows[i].getBackgroundColor());
          radioButton.setForegroundColor(lookupRows[i].getForegroundColor());
          radioButton.setFont(lookupRows[i].getFont());
          fieldList.add(radioButton);
        }
      }
      catch (ProcessingException e) {
        SERVICES.getService(IExceptionHandlerService.class).handleException(new ProcessingException(this.getClass().getSimpleName(), e));
      }
    }
  }

  public LookupCall getLookupCall() {
    return m_lookupCall;
  }

  public void setLookupCall(LookupCall call) {
    m_lookupCall = call;
  }

  public Class<? extends ICodeType> getCodeTypeClass() {
    return m_codeTypeClass;
  }

  public void setCodeTypeClass(Class<? extends ICodeType> codeTypeClass) {
    m_codeTypeClass = codeTypeClass;
    // create lookup service call
    m_lookupCall = null;
    if (m_codeTypeClass != null) {
      m_lookupCall = CodeLookupCall.newInstanceByService(m_codeTypeClass);
    }

    // If no label is configured use the code type's text
    if (getConfiguredLabel() == null) {
      setLabel(CODES.getCodeType(codeTypeClass).getText());
    }
  }

  protected Class<? extends IFormField>[] getConfiguredFields() {
    Class[] dca = ConfigurationUtility.getDeclaredPublicClasses(getClass());
    return ConfigurationUtility.sortFilteredClassesByOrderAnnotation(dca, IFormField.class);
  }

  /*
   * Runtime
   */
  @Override
  protected void initFieldInternal() throws ProcessingException {
    // special case: a button represents null
    IButton b = getButtonFor(null);
    if (b != null) {
      syncValueToButtons();
    }
    super.initFieldInternal();
  }

  /**
   * Returns the LookupRows using {@link #getLookupCall()}, {@link #prepareLookupCall(LookupCall)} and
   * {@link #filterLookup(LookupCall, List)}.
   * 
   * @return
   * @throws ProcessingException
   */
  private LookupRow[] getLookupRows() throws ProcessingException {
    LookupRow[] data;
    LookupCall call = null;
    // Get the data
    if (getLookupCall() != null) {
      call = SERVICES.getService(ILookupCallProvisioningService.class).newClonedInstance(getLookupCall(), new FormFieldProvisioningContext(AbstractRadioButtonGroup.this));
      prepareLookupCall(call);
      data = call.getDataByAll();
    }
    else {
      data = new LookupRow[0];
    }

    // Filter the result
    ArrayList<LookupRow> result;
    if (data != null) {
      result = new ArrayList<LookupRow>(Arrays.asList(data));
    }
    else {
      result = new ArrayList<LookupRow>();
    }
    filterLookup(call, result);

    return result.toArray(new LookupRow[result.size()]);
  }

  private void prepareLookupCall(LookupCall call) {
    prepareLookupCallInternal(call);
    execPrepareLookup(call);
  }

  private void prepareLookupCallInternal(LookupCall call) {
    call.setActive(TriState.UNDEFINED);
    //when there is a master value defined in the original call, don't set it to null when no master value is available
    if (getMasterValue() != null || getLookupCall() == null || getLookupCall().getMaster() == null) {
      call.setMaster(getMasterValue());
    }
  }

  private void filterLookup(LookupCall call, List<LookupRow> result) throws ProcessingException {
    execFilterLookupResult(call, result);
  }

  @Override
  public void rebuildFieldGrid() {
    if (m_grid != null) {
      m_grid.validate();
      if (isInitialized()) {
        if (getParentField() != null) {
          getParentField().rebuildFieldGrid();
        }
        if (getForm() != null) {
          getForm().structureChanged(this);
        }
      }
    }
  }

  protected void handleFieldVisibilityChanged() {
    if (isInitialized()) {
      rebuildFieldGrid();
    }
  }

  @Override
  public final int getGridColumnCount() {
    return m_grid != null ? m_grid.getGridColumnCount() : 1;
  }

  @Override
  public final int getGridRowCount() {
    return m_grid != null ? m_grid.getGridRowCount() : 1;
  }

  @Override
  public void setFormInternal(IForm form) {
    super.setFormInternal(form);
    for (IFormField f : getFields()) {
      f.setFormInternal(form);
    }
  }

  @Override
  protected void valueChangedInternal() {
    super.valueChangedInternal();
    syncValueToButtons();
  }

  @Override
  protected T validateValueInternal(T rawValue) throws ProcessingException {
    T validValue;
    if (rawValue == null) {
      validValue = null;
    }
    else {
      T t = TypeCastUtility.castValue(rawValue, getHolderType());
      IButton b = getButtonFor(t);
      if (b != null) {
        validValue = t;
      }
      else {
        throw new ProcessingException("Illegal radio value: " + rawValue);
      }
    }
    return validValue;
  }

  @Override
  @ConfigPropertyValue("0")
  protected double getConfiguredGridWeightY() {
    return 0;
  }

  @Override
  public T getSelectedKey() {
    return getValue();
  }

  @Override
  public IButton getButtonFor(T value) {
    for (IButton b : getButtons()) {
      T radioValue = TypeCastUtility.castValue(b.getRadioValue(), getHolderType());
      if (CompareUtility.equals(radioValue, value)) {
        return b;
      }
    }
    return null;
  }

  @Override
  public IButton getSelectedButton() {
    return getButtonFor(getSelectedKey());
  }

  @Override
  public void selectKey(T key) {
    setValue(key);
  }

  @Override
  public void selectButton(IButton button) {
    for (IButton b : getButtons()) {
      if (b == button) {
        button.setSelected(true);
        break;
      }
    }
  }

  /*
   * Implementation of ICompositeField
   */

  /**
   * broadcast this change to all children
   */
  @Override
  public void setEnabled(boolean b) {
    super.setEnabled(b);
    // recursively down all children
    for (IFormField f : m_fields) {
      f.setEnabled(b);
    }
  }

  /**
   * when granting of enabled property changes, broadcast and set this property
   * on all children that have no permission set
   */
  @Override
  public void setEnabledGranted(boolean b) {
    super.setEnabledGranted(b);
    for (IFormField f : getFields()) {
      if (f.getEnabledPermission() == null) {
        f.setEnabledGranted(b);
      }
    }
  }

  @Override
  @SuppressWarnings("unchecked")
  public <F extends IFormField> F getFieldByClass(final Class<F> c) {
    final Holder<IFormField> found = new Holder<IFormField>(IFormField.class);
    IFormFieldVisitor v = new IFormFieldVisitor() {
      @Override
      public boolean visitField(IFormField field, int level, int fieldIndex) {
        if (field.getClass() == c) {
          found.setValue(field);
        }
        return found.getValue() == null;
      }
    };
    visitFields(v, 0);
    return (F) found.getValue();
  }

  @Override
  public IFormField getFieldById(final String id) {
    final Holder<IFormField> found = new Holder<IFormField>(IFormField.class);
    IFormFieldVisitor v = new IFormFieldVisitor() {
      @Override
      public boolean visitField(IFormField field, int level, int fieldIndex) {
        if (field.getFieldId().equals(id)) {
          found.setValue(field);
        }
        return found.getValue() == null;
      }
    };
    visitFields(v, 0);
    return found.getValue();
  }

  @Override
  public <X extends IFormField> X getFieldById(final String id, final Class<X> type) {
    final Holder<X> found = new Holder<X>(type);
    IFormFieldVisitor v = new IFormFieldVisitor() {
      @Override
      @SuppressWarnings("unchecked")
      public boolean visitField(IFormField field, int level, int fieldIndex) {
        if (type.isAssignableFrom(field.getClass()) && field.getFieldId().equals(id)) {
          found.setValue((X) field);
        }
        return found.getValue() == null;
      }
    };
    visitFields(v, 0);
    return found.getValue();
  }

  @Override
  public int getFieldCount() {
    return m_fields.length;
  }

  @Override
  public int getFieldIndex(IFormField f) {
    for (int i = 0; i < m_fields.length; i++) {
      if (m_fields[i] == f) {
        return i;
      }
    }
    return -1;
  }

  @Override
  public IFormField[] getFields() {
    IFormField[] a = new IFormField[m_fields.length];
    System.arraycopy(m_fields, 0, a, 0, a.length);
    return a;
  }

  @Override
  public IButton[] getButtons() {
    return m_radioButtons;
  }

  @Override
  public boolean visitFields(IFormFieldVisitor visitor, int startLevel) {
    // myself
    if (!visitor.visitField(this, startLevel, 0)) {
      return false;
    }
    // children
    int index = 0;
    IFormField[] f = m_fields;
    for (int i = 0; i < f.length; i++) {
      if (f[i] instanceof ICompositeField) {
        if (!((ICompositeField) f[i]).visitFields(visitor, startLevel + 1)) {
          return false;
        }
      }
      else {
        if (!visitor.visitField(f[i], startLevel, index)) {
          return false;
        }
      }
      index++;
    }
    return true;
  }

  private void syncValueToButtons() {
    if (m_valueAndSelectionMediatorActive) {
      return;
    }
    try {
      m_valueAndSelectionMediatorActive = true;
      //
      T selectedKey = getSelectedKey();
      IButton selectedButton = getButtonFor(selectedKey);
      for (IButton b : getButtons()) {
        b.setSelected(b == selectedButton);
      }
    }
    finally {
      m_valueAndSelectionMediatorActive = false;
    }
  }

  private void syncButtonsToValue(IButton selectedButton) {
    if (m_valueAndSelectionMediatorActive) {
      return;
    }
    try {
      m_valueAndSelectionMediatorActive = true;
      //
      for (IButton b : getButtons()) {
        b.setSelected(b == selectedButton);
      }
      T radioValue = TypeCastUtility.castValue(selectedButton.getRadioValue(), getHolderType());
      selectKey(radioValue);
    }
    finally {
      m_valueAndSelectionMediatorActive = false;
    }
  }

  /**
   * Implementation of PropertyChangeListener Proxy on all attached fields (not groups)
   */
  private class P_FieldPropertyChangeListenerEx implements PropertyChangeListener {
    @Override
    public void propertyChange(PropertyChangeEvent e) {
      if (e.getPropertyName().equals(IFormField.PROP_VISIBLE)) {
        // fire group box visibility
        handleFieldVisibilityChanged();
      }
      else if (e.getPropertyName().equals(IFormField.PROP_SAVE_NEEDED)) {
        checkSaveNeeded();
      }
      else if (e.getPropertyName().equals(IFormField.PROP_EMPTY)) {
        checkEmpty();
      }
    }
  }// end private class

  /**
   * Implementation of PropertyChangeListener Proxy on all attached fields (not groups)
   */
  private class P_ButtonPropertyChangeListener implements PropertyChangeListener {
    @Override
    public void propertyChange(PropertyChangeEvent e) {
      if (e.getPropertyName().equals(IButton.PROP_SELECTED)) {
        if (((IButton) e.getSource()).isSelected()) {
          syncButtonsToValue((IButton) e.getSource());
        }
      }
    }
  }// end private class

  /**
   * A dynamic unconfigured radio button
   */
  private final class RadioButton extends AbstractButton {
    @Override
    protected void initConfig() {
      super.initConfig();
    }
  }

}
