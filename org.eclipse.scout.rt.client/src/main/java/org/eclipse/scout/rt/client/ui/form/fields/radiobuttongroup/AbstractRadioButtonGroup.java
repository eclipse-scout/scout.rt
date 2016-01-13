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
package org.eclipse.scout.rt.client.ui.form.fields.radiobuttongroup;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.scout.rt.client.extension.ui.form.fields.IFormFieldExtension;
import org.eclipse.scout.rt.client.extension.ui.form.fields.radiobuttongroup.IRadioButtonGroupExtension;
import org.eclipse.scout.rt.client.extension.ui.form.fields.radiobuttongroup.RadioButtonGroupChains.RadioButtonGroupFilterLookupResultChain;
import org.eclipse.scout.rt.client.extension.ui.form.fields.radiobuttongroup.RadioButtonGroupChains.RadioButtonGroupPrepareLookupChain;
import org.eclipse.scout.rt.client.services.lookup.FormFieldProvisioningContext;
import org.eclipse.scout.rt.client.services.lookup.ILookupCallProvisioningService;
import org.eclipse.scout.rt.client.ui.form.IForm;
import org.eclipse.scout.rt.client.ui.form.IFormFieldVisitor;
import org.eclipse.scout.rt.client.ui.form.fields.AbstractFormField;
import org.eclipse.scout.rt.client.ui.form.fields.AbstractValueField;
import org.eclipse.scout.rt.client.ui.form.fields.CompositeFieldUtility;
import org.eclipse.scout.rt.client.ui.form.fields.ICompositeField;
import org.eclipse.scout.rt.client.ui.form.fields.ICompositeFieldGrid;
import org.eclipse.scout.rt.client.ui.form.fields.IFormField;
import org.eclipse.scout.rt.client.ui.form.fields.button.AbstractRadioButton;
import org.eclipse.scout.rt.client.ui.form.fields.button.IButton;
import org.eclipse.scout.rt.client.ui.form.fields.button.IRadioButton;
import org.eclipse.scout.rt.client.ui.form.fields.radiobuttongroup.internal.RadioButtonGroupGrid;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.Order;
import org.eclipse.scout.rt.platform.annotations.ConfigOperation;
import org.eclipse.scout.rt.platform.annotations.ConfigProperty;
import org.eclipse.scout.rt.platform.classid.ClassId;
import org.eclipse.scout.rt.platform.exception.ProcessingException;
import org.eclipse.scout.rt.platform.reflect.ConfigurationUtility;
import org.eclipse.scout.rt.platform.util.CollectionUtility;
import org.eclipse.scout.rt.platform.util.CompareUtility;
import org.eclipse.scout.rt.platform.util.TriState;
import org.eclipse.scout.rt.platform.util.collection.OrderedCollection;
import org.eclipse.scout.rt.shared.services.common.code.ICodeType;
import org.eclipse.scout.rt.shared.services.lookup.CodeLookupCall;
import org.eclipse.scout.rt.shared.services.lookup.ILookupCall;
import org.eclipse.scout.rt.shared.services.lookup.ILookupRow;
import org.eclipse.scout.rt.shared.services.lookup.LookupCall;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * AbstractRadioButtonGroup contains a set of {@link IRadioButton} and can also contain other {@link IFormField}s at the
 * same time. In an AbstractRadioButtonGroup only 1 RadioButton can be selected at a time.
 */
@ClassId("20dd4412-e677-4996-afcc-13c43b9dcae8")
public abstract class AbstractRadioButtonGroup<T> extends AbstractValueField<T> implements IRadioButtonGroup<T>, ICompositeField {
  private static final Logger LOG = LoggerFactory.getLogger(AbstractRadioButtonGroup.class);

  private boolean m_valueAndSelectionMediatorActive;
  private ILookupCall<T> m_lookupCall;
  private Class<? extends ICodeType<?, T>> m_codeTypeClass;
  private ICompositeFieldGrid<ICompositeField> m_grid;
  private List<IFormField> m_fields;
  private List<IRadioButton<T>> m_radioButtons;
  private Map<Class<? extends IFormField>, IFormField> m_movedFormFieldsByClass;

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
  protected Class<? extends ILookupCall<T>> getConfiguredLookupCall() {
    return null;
  }

  /**
   * All codes returned by the configured {@link ICodeType} are used as radio buttons.<br>
   * However, the {@link #interceptFilterLookupResult(LookupCall, List)} can be used to manipulate the codes which shall
   * be used.
   */
  @ConfigProperty(ConfigProperty.CODE_TYPE)
  @Order(250)
  protected Class<? extends ICodeType<?, T>> getConfiguredCodeType() {
    return null;
  }

  @Override
  @Order(210)
  @ConfigProperty(ConfigProperty.BOOLEAN)
  protected boolean getConfiguredAutoAddDefaultMenus() {
    return false;
  }

  /**
   * Called before any lookup is performed
   */
  @ConfigOperation
  @Order(260)
  protected void execPrepareLookup(ILookupCall<T> call) {
  }

  /**
   * @param call
   *          that produced this result
   * @param result
   *          live list containing the result rows. Add, remove, set, replace and clear of entries in this list is
   *          supported
   */
  @ConfigOperation
  @Order(270)
  protected void execFilterLookupResult(ILookupCall<T> call, List<ILookupRow<T>> result) {
  }

  @Override
  protected void initConfig() {
    m_fields = CollectionUtility.emptyArrayList();
    m_movedFormFieldsByClass = new HashMap<Class<? extends IFormField>, IFormField>();
    m_grid = createGrid();
    super.initConfig();
    // Configured CodeType
    if (getConfiguredCodeType() != null) {
      setCodeTypeClass(getConfiguredCodeType());
    }
    // Configured LookupCall
    Class<? extends ILookupCall<T>> lookupCallClass = getConfiguredLookupCall();
    if (lookupCallClass != null) {
      ILookupCall<T> call = BEANS.get(lookupCallClass);
      setLookupCall(call);
    }
    // add fields
    List<Class<IFormField>> configuredFields = getConfiguredFields();
    List<IFormField> contributedFields = m_contributionHolder.getContributionsByClass(IFormField.class);
    OrderedCollection<IFormField> fields = new OrderedCollection<IFormField>();
    for (Class<? extends IFormField> fieldClazz : configuredFields) {
      fields.addOrdered(ConfigurationUtility.newInnerInstance(this, fieldClazz));
    }
    fields.addAllOrdered(contributedFields);
    injectFieldsInternal(fields);
    for (IFormField f : fields) {
      f.setParentFieldInternal(this);
    }
    m_fields = fields.getOrderedList();
    //attach a proxy controller to each child field in the group for: visible, saveNeeded
    for (IFormField f : m_fields) {
      f.addPropertyChangeListener(new P_FieldPropertyChangeListenerEx());
    }
    //extract buttons from field subtree
    List<IRadioButton<T>> buttonList = new ArrayList<IRadioButton<T>>();
    for (IFormField f : m_fields) {
      IRadioButton<T> b = findFirstButtonInFieldTree(f);
      if (b != null) {
        buttonList.add(b);
      }
    }
    m_radioButtons = buttonList;
    //decorate radio buttons
    for (IRadioButton b : m_radioButtons) {
      b.addPropertyChangeListener(new P_ButtonPropertyChangeListener());
    }
    handleFieldVisibilityChanged();
  }

  @SuppressWarnings("unchecked")
  private IRadioButton<T> findFirstButtonInFieldTree(IFormField f) {
    if (f instanceof IRadioButton) {
      return (IRadioButton) f;
    }
    else if (f instanceof ICompositeField) {
      for (IFormField sub : ((ICompositeField) f).getFields()) {
        IRadioButton<T> b = findFirstButtonInFieldTree(sub);
        if (b != null) {
          return b;
        }
      }
    }
    return null;
  }

  /**
   * Override this internal method only in order to make use of dynamic fields<br>
   * Used to add and/or remove fields<br>
   * To change the order or specify the insert position use {@link IFormField#setOrder(double)}.
   *
   * @param fields
   *          live and mutable collection of configured fields, yet not initialized
   */
  protected void injectFieldsInternal(OrderedCollection<IFormField> fields) {
    if (getLookupCall() != null) {
      // Use the LookupCall
      List<ILookupRow<T>> lookupRows = getLookupRows();
      for (ILookupRow<T> row : lookupRows) {
        IRadioButton<T> radioButton = createEmptyRadioButtonForLookupRow();
        radioButton.setEnabled(row.isEnabled());
        radioButton.setLabel(row.getText());
        radioButton.setRadioValue(row.getKey());
        radioButton.setTooltipText(row.getTooltipText());
        radioButton.setBackgroundColor(row.getBackgroundColor());
        radioButton.setForegroundColor(row.getForegroundColor());
        radioButton.setFont(row.getFont());
        radioButton.setEnabled(row.isActive());
        fields.addLast(radioButton);
      }
    }
  }

  /**
   * Returns a new instance of an empty radio button to be used when creating radio buttons dynamically from lookup
   * rows. Subclasses may override this method to use custom radio buttons. Please note that all lookup row values (e.g.
   * text, foreground color etc.) are applied to the returned instance afterwards. This method must <b>not</b> return
   * <code>null</code>!
   */
  protected IRadioButton<T> createEmptyRadioButtonForLookupRow() {
    return new RadioButton();
  }

  /**
   * Returns an instance of {@link ICompositeFieldGrid} which arranges the fields of this group box by setting a
   * GridData object on each field. The default implementation returns a {@link RadioButtonGroupGrid} instance. Override
   * this method when you have special requirements for displaying radio buttons within the group.
   */
  protected ICompositeFieldGrid<ICompositeField> createGrid() {
    return new RadioButtonGroupGrid();
  }

  @Override
  public void addField(IFormField f) {
    CompositeFieldUtility.addField(f, this, m_fields);
  }

  @Override
  public void removeField(IFormField f) {
    CompositeFieldUtility.removeField(f, this, m_fields);
  }

  @Override
  public void moveFieldTo(IFormField f, ICompositeField newContainer) {
    CompositeFieldUtility.moveFieldTo(f, this, newContainer, m_movedFormFieldsByClass);
  }

  @Override
  public Map<Class<? extends IFormField>, IFormField> getMovedFields() {
    return Collections.unmodifiableMap(m_movedFormFieldsByClass);
  }

  public ILookupCall<T> getLookupCall() {
    return m_lookupCall;
  }

  public void setLookupCall(ILookupCall<T> call) {
    m_lookupCall = call;
  }

  public Class<? extends ICodeType<?, T>> getCodeTypeClass() {
    return m_codeTypeClass;
  }

  public void setCodeTypeClass(Class<? extends ICodeType<?, T>> codeTypeClass) {
    m_codeTypeClass = codeTypeClass;
    // create lookup service call
    m_lookupCall = null;
    if (m_codeTypeClass != null) {
      m_lookupCall = CodeLookupCall.newInstanceByService(m_codeTypeClass);
    }

    // If no label is configured use the code type's text
    if (getConfiguredLabel() == null) {
      setLabel(BEANS.get(codeTypeClass).getText());
    }
  }

  protected List<Class<IFormField>> getConfiguredFields() {
    Class[] dca = ConfigurationUtility.getDeclaredPublicClasses(getClass());
    return ConfigurationUtility.filterClasses(dca, IFormField.class);
  }

  /*
   * Runtime
   */
  @Override
  protected void initFieldInternal() {
    // special case: a button represents null
    IRadioButton b = getButtonFor(null);
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
   */
  protected List<ILookupRow<T>> getLookupRows() {
    List<ILookupRow<T>> data;
    ILookupCall<T> call = getLookupCall();
    // Get the data
    if (call != null) {
      call = BEANS.get(ILookupCallProvisioningService.class).newClonedInstance(call, new FormFieldProvisioningContext(AbstractRadioButtonGroup.this));
      prepareLookupCall(call);
      data = CollectionUtility.arrayList(call.getDataByAll());
    }
    else {
      data = CollectionUtility.emptyArrayList();
    }

    // Filter the result
    filterLookup(call, data);

    return data;
  }

  protected void prepareLookupCall(ILookupCall<T> call) {
    prepareLookupCallInternal(call);
    interceptPrepareLookup(call);
  }

  private void prepareLookupCallInternal(ILookupCall<T> call) {
    call.setActive(TriState.UNDEFINED);
    //when there is a master value defined in the original call, don't set it to null when no master value is available
    if (getMasterValue() != null || getLookupCall() == null || getLookupCall().getMaster() == null) {
      call.setMaster(getMasterValue());
    }
  }

  protected void filterLookup(ILookupCall<T> call, List<ILookupRow<T>> result) {
    interceptFilterLookupResult(call, result);

    // filter invalid rows
    Iterator<ILookupRow<T>> resultIt = result.iterator();
    while (resultIt.hasNext()) {
      ILookupRow<T> row = resultIt.next();
      if (row == null) {
        resultIt.remove();
      }
      else if (row.getKey() == null) {
        LOG.warn("The key of a lookup row may not be null. Row has been removed for radio button group '{}'.", getClass().getName());
        resultIt.remove();
      }
    }
  }

  @Override
  public void rebuildFieldGrid() {
    if (m_grid != null) {
      m_grid.validate(this);
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
  protected T validateValueInternal(T rawValue) {
    T validValue;
    if (rawValue == null) {
      validValue = null;
    }
    else {
      IRadioButton b = getButtonFor(rawValue);
      if (b != null) {
        validValue = rawValue;
      }
      else {
        throw new ProcessingException("Illegal radio value: " + rawValue);
      }
    }
    return validValue;
  }

  @Override
  protected double getConfiguredGridWeightY() {
    return 0;
  }

  @Override
  public T getSelectedKey() {
    return getValue();
  }

  @Override
  public IRadioButton<T> getButtonFor(T value) {
    for (IRadioButton<T> b : getButtons()) {
      T radioValue = b.getRadioValue();
      if (CompareUtility.equals(radioValue, value)) {
        return b;
      }
    }
    return null;
  }

  @Override
  public IRadioButton<T> getSelectedButton() {
    return getButtonFor(getSelectedKey());
  }

  @Override
  public void selectKey(T key) {
    setValue(key);
  }

  /**
   * use the label of the {@link AbstractRadioButton} contained in this field as display text instead of relying on
   * {@link Object#toString()} of validValue
   */
  @Override
  protected String formatValueInternal(T validValue) {
    if (validValue == null || getButtonFor(validValue) == null) {
      return "";
    }
    return getButtonFor(validValue).getLabel();
  }

  @Override
  public void selectButton(IRadioButton button) {
    for (IRadioButton b : getButtons()) {
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
   * when granting of enabled property changes, broadcast and set this property on all children that have no permission
   * set
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
  public <F extends IFormField> F getFieldByClass(Class<F> c) {
    return CompositeFieldUtility.getFieldByClass(this, c);
  }

  @Override
  public IFormField getFieldById(String id) {
    return CompositeFieldUtility.getFieldById(this, id);
  }

  @Override
  public <X extends IFormField> X getFieldById(String id, Class<X> type) {
    return CompositeFieldUtility.getFieldById(this, id, type);
  }

  @Override
  public int getFieldCount() {
    return m_fields.size();
  }

  @Override
  public int getFieldIndex(IFormField f) {
    return m_fields.indexOf(f);
  }

  @Override
  public List<IFormField> getFields() {
    return CollectionUtility.arrayList(m_fields);
  }

  @Override
  public List<IRadioButton<T>> getButtons() {
    if (m_radioButtons == null) {
      return CollectionUtility.emptyArrayList();
    }
    return CollectionUtility.arrayList(m_radioButtons);
  }

  @Override
  public boolean visitFields(IFormFieldVisitor visitor, int startLevel) {
    // myself
    if (!visitor.visitField(this, startLevel, 0)) {
      return false;
    }
    // children
    int index = 0;
    for (IFormField field : m_fields) {
      if (field instanceof ICompositeField) {
        if (!((ICompositeField) field).visitFields(visitor, startLevel + 1)) {
          return false;
        }
      }
      else {
        if (!visitor.visitField(field, startLevel, index)) {
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
      IRadioButton selectedButton = getButtonFor(selectedKey);
      for (IRadioButton b : getButtons()) {
        b.setSelected(b == selectedButton);
      }
    }
    finally {
      m_valueAndSelectionMediatorActive = false;
    }
  }

  private void syncButtonsToValue(IRadioButton<T> selectedButton) {
    if (m_valueAndSelectionMediatorActive) {
      return;
    }
    try {
      m_valueAndSelectionMediatorActive = true;
      //
      for (IRadioButton<T> b : getButtons()) {
        b.setSelected(b == selectedButton);
      }
      selectKey(selectedButton.getRadioValue());
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
    @SuppressWarnings("unchecked")
    @Override
    public void propertyChange(PropertyChangeEvent e) {
      if (e.getPropertyName().equals(IButton.PROP_SELECTED)) {
        if (((IRadioButton) e.getSource()).isSelected()) {
          syncButtonsToValue((IRadioButton<T>) e.getSource());
        }
      }
    }
  }// end private class

  /**
   * A dynamic unconfigured radio button
   */
  private final class RadioButton extends AbstractRadioButton<T> {
  }

  protected final void interceptPrepareLookup(ILookupCall<T> call) {
    List<? extends IFormFieldExtension<? extends AbstractFormField>> extensions = getAllExtensions();
    RadioButtonGroupPrepareLookupChain<T> chain = new RadioButtonGroupPrepareLookupChain<T>(extensions);
    chain.execPrepareLookup(call);
  }

  protected final void interceptFilterLookupResult(ILookupCall<T> call, List<ILookupRow<T>> result) {
    List<? extends IFormFieldExtension<? extends AbstractFormField>> extensions = getAllExtensions();
    RadioButtonGroupFilterLookupResultChain<T> chain = new RadioButtonGroupFilterLookupResultChain<T>(extensions);
    chain.execFilterLookupResult(call, result);
  }

  protected static class LocalRadioButtonGroupExtension<T, OWNER extends AbstractRadioButtonGroup<T>> extends LocalValueFieldExtension<T, OWNER> implements IRadioButtonGroupExtension<T, OWNER> {

    public LocalRadioButtonGroupExtension(OWNER owner) {
      super(owner);
    }

    @Override
    public void execPrepareLookup(RadioButtonGroupPrepareLookupChain<T> chain, ILookupCall<T> call) {
      getOwner().execPrepareLookup(call);
    }

    @Override
    public void execFilterLookupResult(RadioButtonGroupFilterLookupResultChain<T> chain, ILookupCall<T> call, List<ILookupRow<T>> result) {
      getOwner().execFilterLookupResult(call, result);
    }
  }

  @Override
  protected IRadioButtonGroupExtension<T, ? extends AbstractRadioButtonGroup<T>> createLocalExtension() {
    return new LocalRadioButtonGroupExtension<T, AbstractRadioButtonGroup<T>>(this);
  }
}
