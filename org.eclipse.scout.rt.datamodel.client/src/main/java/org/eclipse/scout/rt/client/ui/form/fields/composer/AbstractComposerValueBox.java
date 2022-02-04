/*
 * Copyright (c) 2010-2018 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.rt.client.ui.form.fields.composer;

import java.beans.PropertyChangeListener;
import java.math.BigDecimal;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.scout.rt.client.dto.FormData;
import org.eclipse.scout.rt.client.dto.FormData.DefaultSubtypeSdkCommand;
import org.eclipse.scout.rt.client.extension.ui.form.fields.IFormFieldExtension;
import org.eclipse.scout.rt.client.extension.ui.form.fields.composer.ComposerValueBoxChains.ComposerValueBoxChangedValueChain;
import org.eclipse.scout.rt.client.extension.ui.form.fields.composer.ComposerValueBoxChains.ComposerValueBoxInitOperatorToFieldMapChain;
import org.eclipse.scout.rt.client.extension.ui.form.fields.composer.IComposerValueBoxExtension;
import org.eclipse.scout.rt.client.ui.basic.tree.ITreeNode;
import org.eclipse.scout.rt.client.ui.form.fields.AbstractFormField;
import org.eclipse.scout.rt.client.ui.form.fields.ICompositeField;
import org.eclipse.scout.rt.client.ui.form.fields.IFormField;
import org.eclipse.scout.rt.client.ui.form.fields.IValueField;
import org.eclipse.scout.rt.client.ui.form.fields.bigdecimalfield.AbstractBigDecimalField;
import org.eclipse.scout.rt.client.ui.form.fields.datefield.AbstractDateField;
import org.eclipse.scout.rt.client.ui.form.fields.datefield.AbstractTimeField;
import org.eclipse.scout.rt.client.ui.form.fields.groupbox.AbstractGroupBox;
import org.eclipse.scout.rt.client.ui.form.fields.integerfield.AbstractIntegerField;
import org.eclipse.scout.rt.client.ui.form.fields.listbox.AbstractListBox;
import org.eclipse.scout.rt.client.ui.form.fields.longfield.AbstractLongField;
import org.eclipse.scout.rt.client.ui.form.fields.sequencebox.AbstractSequenceBox;
import org.eclipse.scout.rt.client.ui.form.fields.sequencebox.ISequenceBox;
import org.eclipse.scout.rt.client.ui.form.fields.smartfield.AbstractSmartField;
import org.eclipse.scout.rt.client.ui.form.fields.stringfield.AbstractStringField;
import org.eclipse.scout.rt.client.ui.form.fields.treebox.AbstractTreeBox;
import org.eclipse.scout.rt.platform.Order;
import org.eclipse.scout.rt.platform.annotations.ConfigOperation;
import org.eclipse.scout.rt.platform.classid.ClassId;
import org.eclipse.scout.rt.platform.text.TEXTS;
import org.eclipse.scout.rt.platform.util.CollectionUtility;
import org.eclipse.scout.rt.platform.util.TriState;
import org.eclipse.scout.rt.shared.data.model.DataModelConstants;
import org.eclipse.scout.rt.shared.data.model.IDataModelAttribute;
import org.eclipse.scout.rt.shared.data.model.IDataModelAttributeOp;
import org.eclipse.scout.rt.shared.services.common.jdbc.SearchFilter;
import org.eclipse.scout.rt.shared.services.lookup.ILookupCall;
import org.eclipse.scout.rt.shared.services.lookup.LocalLookupCall;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Template box containing all composer values.
 * <p>
 */
@ClassId("2d8065cf-eeb3-4d64-8753-adb36cf852b8")
public abstract class AbstractComposerValueBox extends AbstractGroupBox implements IComposerValueBox {
  private static final Logger LOG = LoggerFactory.getLogger(AbstractComposerValueBox.class);

  private Map<Integer /* operator */, Map<Integer /* field type */, IComposerValueField>> m_operatorTypeToFieldMap;
  //selection context
  private IDataModelAttribute m_attribute;
  private IComposerValueField m_selectedField;
  private PropertyChangeListener m_valueChangedListener;

  public AbstractComposerValueBox() {
    this(true);
  }

  public AbstractComposerValueBox(boolean callInitializer) {
    super(callInitializer);
  }

  @Override
  protected boolean getConfiguredBorderVisible() {
    return false;
  }

  @Override
  protected int getConfiguredGridColumnCount() {
    return 1;
  }

  @Override
  protected int getConfiguredGridW() {
    return 1;
  }

  /**
   * This callback is invoked whenever the active value field changed its value.
   */
  @ConfigOperation
  @Order(50)
  protected void execChangedValue() {
  }

  @Override
  protected void execAddSearchTerms(SearchFilter search) {
    //nop
  }

  @Override
  protected void initConfig() {
    super.initConfig();

    Map<Integer, Map<Integer, IComposerValueField>> operatorTypeToFieldMap = new HashMap<>();
    interceptInitOperatorToFieldMap(operatorTypeToFieldMap);
    m_operatorTypeToFieldMap = operatorTypeToFieldMap;

    m_valueChangedListener = e -> {
      if (IValueField.PROP_VALUE.equals(e.getPropertyName())) {
        try {
          interceptChangedValue();
        }
        catch (Exception ex) {
          LOG.error("fire value change on {}", e.getSource(), ex);
        }
      }
    };

    for (IFormField f : getFields()) {
      f.setLabelVisible(false);
      f.setLabel(TEXTS.get("Value"));
      f.setVisible(false);
      if (f instanceof ISequenceBox) {
        List<IFormField> sequenceBoxChildFields = ((ICompositeField) f).getFields();
        if (CollectionUtility.hasElements(sequenceBoxChildFields)) {
          IFormField firstField = CollectionUtility.firstElement(sequenceBoxChildFields);
          firstField.setLabelVisible(false);
          if (sequenceBoxChildFields.size() > 1) {
            IFormField secondField = CollectionUtility.getElement(sequenceBoxChildFields, 1);
            secondField.setLabel(TEXTS.get("and"));
          }
        }
      }
    }
  }

  /**
   * Initializes the mapping from operator to field.
   */
  protected void execInitOperatorToFieldMap(Map<Integer /* operator */, Map<Integer /* field type */, IComposerValueField>> operatorTypeToFieldMap) {
    // specific operators
    Map<Integer, IComposerValueField> betweenMap = new HashMap<>();
    betweenMap.put(IDataModelAttribute.TYPE_DATE, getFieldByClass(BetweenDateField.class));
    betweenMap.put(IDataModelAttribute.TYPE_DATE_TIME, getFieldByClass(BetweenDateTimeField.class));
    betweenMap.put(IDataModelAttribute.TYPE_BIG_DECIMAL, getFieldByClass(BetweenBigDecimalField.class));
    betweenMap.put(IDataModelAttribute.TYPE_AGGREGATE_COUNT, getFieldByClass(BetweenIntegerField.class));
    betweenMap.put(IDataModelAttribute.TYPE_INTEGER, getFieldByClass(BetweenIntegerField.class));
    betweenMap.put(IDataModelAttribute.TYPE_LONG, getFieldByClass(BetweenLongField.class));
    betweenMap.put(IDataModelAttribute.TYPE_PERCENT, getFieldByClass(BetweenBigDecimalField.class));
    betweenMap.put(IDataModelAttribute.TYPE_PLAIN_BIG_DECIMAL, getFieldByClass(BetweenBigDecimalField.class));
    betweenMap.put(IDataModelAttribute.TYPE_PLAIN_INTEGER, getFieldByClass(BetweenIntegerField.class));
    betweenMap.put(IDataModelAttribute.TYPE_PLAIN_LONG, getFieldByClass(BetweenLongField.class));
    betweenMap.put(IDataModelAttribute.TYPE_TIME, getFieldByClass(BetweenTimeField.class));

    operatorTypeToFieldMap.put(DataModelConstants.OPERATOR_BETWEEN, betweenMap);
    operatorTypeToFieldMap.put(DataModelConstants.OPERATOR_DATE_BETWEEN, betweenMap);
    operatorTypeToFieldMap.put(DataModelConstants.OPERATOR_DATE_TIME_BETWEEN, betweenMap);

    // type defaults
    Map<Integer, IComposerValueField> defaultMap = new HashMap<>();
    defaultMap.put(IDataModelAttribute.TYPE_DATE, getFieldByClass(DateField.class));
    defaultMap.put(IDataModelAttribute.TYPE_DATE_TIME, getFieldByClass(DateTimeField.class));
    defaultMap.put(IDataModelAttribute.TYPE_BIG_DECIMAL, getFieldByClass(BigDecimalField.class));
    defaultMap.put(IDataModelAttribute.TYPE_AGGREGATE_COUNT, getFieldByClass(IntegerField.class));
    defaultMap.put(IDataModelAttribute.TYPE_INTEGER, getFieldByClass(IntegerField.class));
    defaultMap.put(IDataModelAttribute.TYPE_LIST, getFieldByClass(ListBoxField.class));
    defaultMap.put(IDataModelAttribute.TYPE_TREE, getFieldByClass(TreeBoxField.class));
    defaultMap.put(IDataModelAttribute.TYPE_LONG, getFieldByClass(LongField.class));
    defaultMap.put(IDataModelAttribute.TYPE_PERCENT, getFieldByClass(BigDecimalField.class));
    defaultMap.put(IDataModelAttribute.TYPE_PLAIN_BIG_DECIMAL, getFieldByClass(BigDecimalField.class));
    defaultMap.put(IDataModelAttribute.TYPE_PLAIN_INTEGER, getFieldByClass(IntegerField.class));
    defaultMap.put(IDataModelAttribute.TYPE_PLAIN_LONG, getFieldByClass(LongField.class));
    defaultMap.put(IDataModelAttribute.TYPE_STRING, getFieldByClass(StringField.class));
    defaultMap.put(IDataModelAttribute.TYPE_FULL_TEXT, getFieldByClass(StringField.class));
    defaultMap.put(IDataModelAttribute.TYPE_RICH_TEXT, getFieldByClass(StringField.class));
    defaultMap.put(IDataModelAttribute.TYPE_HTML, getFieldByClass(StringField.class));
    defaultMap.put(IDataModelAttribute.TYPE_SMART, getFieldByClass(SmartField.class));
    defaultMap.put(IDataModelAttribute.TYPE_TIME, getFieldByClass(TimeField.class));

    operatorTypeToFieldMap.put(0, defaultMap);
  }

  /**
   * @return the selected field (may be a sequence box) or null if no field is selected
   */
  public IComposerValueField getSelectedField() {
    return m_selectedField;
  }

  public void setSelectionContext(IDataModelAttribute attribute, IDataModelAttributeOp op, List<?> values) {
    if (op == null) {
      return;
    }
    m_attribute = attribute;
    //
    int dataType = op.getType();
    if (dataType == IDataModelAttribute.TYPE_INHERITED) {
      dataType = attribute.getType();
    }
    Map<Integer, IComposerValueField> typeToFieldMap = m_operatorTypeToFieldMap.get(op.getOperator());
    if (typeToFieldMap == null) {
      //default
      typeToFieldMap = m_operatorTypeToFieldMap.get(0);
    }
    IComposerValueField valueField = typeToFieldMap.get(dataType);
    if (values == null && m_selectedField != null && valueField != null && m_selectedField.getClass().equals(valueField.getClass())) {
      values = m_selectedField.getValues();
    }
    //clear old
    if (m_selectedField != null) {
      m_selectedField.removeValueChangeListenerFromTarget(m_valueChangedListener);
      m_selectedField.clearSelectionContext();
    }
    //set new
    m_selectedField = valueField;
    if (m_selectedField != null) {
      m_selectedField.addValueChangeListenerToTarget(m_valueChangedListener);
      m_selectedField.setSelectionContext(m_attribute, dataType, op, values);
    }
    for (IFormField f : getFields()) {
      f.setVisible(f == m_selectedField);
    }
  }

  public void clearSelectionContext() {
    //clear old
    if (m_selectedField != null) {
      m_selectedField.clearSelectionContext();
    }
    //set new
    m_selectedField = null;
    for (IFormField f : getFields()) {
      f.setVisible(false);
    }
  }

  @Order(10)
  @ClassId("93c506dc-737d-4e12-89a2-2a3a21e49fbe")
  @FormData(defaultSubtypeSdkCommand = DefaultSubtypeSdkCommand.IGNORE)
  public class ListBoxField extends AbstractListBox<Object> implements IComposerValueField {

    @Override
    protected boolean getConfiguredAutoLoad() {
      return false;
    }

    @Override
    protected int getConfiguredGridH() {
      return 4;
    }

    @Override
    protected void execPrepareLookup(ILookupCall<Object> call) {
      if (m_attribute != null) {
        // if isFilterActiveRows() is true, do not use a filter to load rows
        // the list box is only populated one time and filtering for active/inactive is done afterwards
        call.setActive(isFilterActiveRows() ? null : TriState.TRUE);
        m_attribute.prepareLookup(call);
      }
    }

    @Override
    public void addValueChangeListenerToTarget(PropertyChangeListener listener) {
      this.addPropertyChangeListener(listener);
    }

    @Override
    public void removeValueChangeListenerFromTarget(PropertyChangeListener listener) {
      this.removePropertyChangeListener(listener);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void setSelectionContext(IDataModelAttribute attribute, int dataType, IDataModelAttributeOp op, List<?> values) {
      setFilterActiveRowsValue(TriState.TRUE);
      setFilterActiveRows(attribute.isActiveFilterEnabled());
      ILookupCall<Object> newCall = attribute.getLookupCall();
      if (getLookupCall() != newCall) {
        setLookupCall(newCall);
        try {
          loadListBoxData();
        }
        catch (Exception e) {
          LOG.warn("Exception while loading list box data [{}]", getClass().getName(), e);
          // nop
        }
      }
      try {
        Object firstElement = CollectionUtility.firstElement(values);
        if (firstElement instanceof Collection) {
          setValue(CollectionUtility.hashSet((Collection) firstElement));
        }
        else if (firstElement instanceof Object[]) {
          setValue(CollectionUtility.hashSet((Object[]) firstElement));
        }
        else {
          setValue(null);
        }
      }
      catch (Exception e) { // NOSONAR
        // nop
        setValue(null);
      }
    }

    @Override
    public void clearSelectionContext() {
      setValue(null);
    }

    @Override
    public List<Object> getValues() {
      if (getCheckedKeyCount() > 0) {
        return CollectionUtility.arrayList((Object) getValue());
      }
      else {
        return null;
      }
    }

    @Override
    public List<String> getTexts() {
      return CollectionUtility.arrayList(getDisplayText());
    }
  }

  @Order(10)
  @ClassId("32cf9165-ff77-46a5-afb2-1a2b1aff42c8")
  @FormData(defaultSubtypeSdkCommand = DefaultSubtypeSdkCommand.IGNORE)
  public class TreeBoxField extends AbstractTreeBox<Object> implements IComposerValueField {

    @Override
    protected boolean getConfiguredAutoLoad() {
      return false;
    }

    @Override
    protected boolean getConfiguredAutoCheckChildNodes() {
      return true;
    }

    @Override
    protected int getConfiguredGridH() {
      return 4;
    }

    @Override
    protected void execPrepareLookup(ILookupCall<Object> call, ITreeNode parent) {
      if (m_attribute != null) {
        // if isFilterActiveNodes() is true, do not use a filter to load nodes
        // the tree box is only populated one time and filtering for active/inactive is done afterwards
        call.setActive(isFilterActiveNodes() ? null : TriState.TRUE);
        m_attribute.prepareLookup(call);
      }
    }

    @Override
    public void addValueChangeListenerToTarget(PropertyChangeListener listener) {
      this.addPropertyChangeListener(listener);
    }

    @Override
    public void removeValueChangeListenerFromTarget(PropertyChangeListener listener) {
      this.removePropertyChangeListener(listener);
    }

    @Override
    @SuppressWarnings("unchecked")
    public void setSelectionContext(IDataModelAttribute attribute, int dataType, IDataModelAttributeOp op, List<?> values0) {
      setFilterActiveNodesValue(TriState.TRUE);
      setFilterActiveNodes(attribute.isActiveFilterEnabled());
      ILookupCall<Object> newCall = attribute.getLookupCall();
      if (getLookupCall() != newCall) {
        setLookupCall(newCall);
        try {
          loadRootNode();
          getTree().setNodeExpanded(getTree().getRootNode(), true);
        }
        catch (Exception e) {
          LOG.warn("Exception while loading and expanding root node [{}]", getClass().getName(), e);
          // nop
        }
      }
      try {
        Object firstElement = CollectionUtility.firstElement(values0);
        if (firstElement instanceof Collection) {
          setValue(CollectionUtility.hashSet((Collection) firstElement));
        }
        else if (firstElement instanceof Object[]) {
          setValue(CollectionUtility.hashSet((Object[]) firstElement));
        }
        else {
          setValue(null);
        }
      }
      catch (Exception e) { // NOSONAR
        // nop
        setValue(null);
      }
    }

    @Override
    public void clearSelectionContext() {
      setValue(null);
    }

    @Override
    public List<Object> getValues() {
      Set<Object> value = getValue();
      if (value == null) {
        return null;
      }
      return CollectionUtility.arrayList((Object) value);
    }

    @Override
    public List<String> getTexts() {
      return CollectionUtility.arrayList(getDisplayText());
    }
  }

  @Order(10)
  @ClassId("5e26128b-2d65-4ed7-88cc-c23a466b6e88")
  @FormData(defaultSubtypeSdkCommand = DefaultSubtypeSdkCommand.IGNORE)
  public class DateField extends AbstractDateField implements IComposerValueField {
    @Override
    public void addValueChangeListenerToTarget(PropertyChangeListener listener) {
      this.addPropertyChangeListener(listener);
    }

    @Override
    public void removeValueChangeListenerFromTarget(PropertyChangeListener listener) {
      this.removePropertyChangeListener(listener);
    }

    @Override
    public void setSelectionContext(IDataModelAttribute attribute, int dataType, IDataModelAttributeOp op, List values) {
      try {
        @SuppressWarnings("unchecked")
        Object firstElement = CollectionUtility.firstElement(values);
        if (firstElement instanceof Date) {
          setValue((Date) firstElement);
        }
        else {
          setValue(null);
        }
      }
      catch (Exception e) { // NOSONAR
        // nop
        setValue(null);
      }
    }

    @Override
    public void clearSelectionContext() {
      setValue(null);
    }

    @Override
    public List<Object> getValues() {
      if (getValue() != null) {
        return Collections.singletonList(getValue());
      }
      else {
        return null;
      }
    }

    @Override
    public List<String> getTexts() {
      return CollectionUtility.arrayList(getDisplayText());
    }
  }

  @Order(10)
  @ClassId("0ea204ce-bc8b-472d-9535-d9de4d117acb")
  @FormData(defaultSubtypeSdkCommand = DefaultSubtypeSdkCommand.IGNORE)
  public class TimeField extends AbstractTimeField implements IComposerValueField {
    @Override
    public void addValueChangeListenerToTarget(PropertyChangeListener listener) {
      this.addPropertyChangeListener(listener);
    }

    @Override
    public void removeValueChangeListenerFromTarget(PropertyChangeListener listener) {
      this.removePropertyChangeListener(listener);
    }

    @Override
    public void setSelectionContext(IDataModelAttribute attribute, int dataType, IDataModelAttributeOp op, List values) {

      try {

        if (values != null && values.size() == 1) {
          @SuppressWarnings("unchecked")
          Object firstElement = CollectionUtility.firstElement(values);
          if (firstElement instanceof Double) {
            setTimeValue((Double) firstElement);
          }
          else if (firstElement instanceof Date) {
            setValue((Date) firstElement);
          }
          else {
            setValue(null);
          }
        }
        else {
          setValue(null);
        }
      }
      catch (Exception e) { // NOSONAR
        // nop
        setValue(null);
      }
    }

    @Override
    public void clearSelectionContext() {
      setValue(null);
    }

    @Override
    public List<Object> getValues() {
      if (getValue() == null) {
        return null;
      }
      return Collections.singletonList(getValue());
    }

    @Override
    public List<String> getTexts() {
      return CollectionUtility.arrayList(getDisplayText());
    }
  }

  @Order(10)
  @ClassId("0e51fb86-c4f7-471d-8cb1-6329fc8c31a6")
  @FormData(defaultSubtypeSdkCommand = DefaultSubtypeSdkCommand.IGNORE)
  public class DateTimeField extends AbstractDateField implements IComposerValueField {

    @Override
    protected boolean getConfiguredHasTime() {
      return true;
    }

    @Override
    public void addValueChangeListenerToTarget(PropertyChangeListener listener) {
      this.addPropertyChangeListener(listener);
    }

    @Override
    public void removeValueChangeListenerFromTarget(PropertyChangeListener listener) {
      this.removePropertyChangeListener(listener);
    }

    @Override
    public void setSelectionContext(IDataModelAttribute attribute, int dataType, IDataModelAttributeOp op, List values) {
      try {
        @SuppressWarnings("unchecked")
        Object firstElement = CollectionUtility.firstElement(values);
        if (firstElement instanceof Date) {
          setValue((Date) firstElement);
        }
        else {
          setValue(null);
        }
      }
      catch (Exception e) { // NOSONAR
        // nop
        setValue(null);
      }
    }

    @Override
    public void clearSelectionContext() {
      setValue(null);
    }

    @Override
    public List<Object> getValues() {
      if (getValue() == null) {
        return null;
      }
      return Collections.singletonList(getValue());
    }

    @Override
    public List<String> getTexts() {
      return CollectionUtility.arrayList(getDisplayText());
    }
  }

  @Order(10)
  @ClassId("462688c6-9246-49be-adf6-2d21abbff066")
  @FormData(defaultSubtypeSdkCommand = DefaultSubtypeSdkCommand.IGNORE)
  public class IntegerField extends AbstractIntegerField implements IComposerValueField {
    @Override
    public void addValueChangeListenerToTarget(PropertyChangeListener listener) {
      this.addPropertyChangeListener(listener);
    }

    @Override
    public void removeValueChangeListenerFromTarget(PropertyChangeListener listener) {
      this.removePropertyChangeListener(listener);
    }

    @Override
    public void setSelectionContext(IDataModelAttribute attribute, int dataType, IDataModelAttributeOp op, List values) {
      switch (dataType) {
        case IDataModelAttribute.TYPE_INTEGER: {
          setGroupingUsed(true);
          break;
        }
        case IDataModelAttribute.TYPE_PLAIN_INTEGER: {
          setGroupingUsed(false);
          break;
        }
      }
      try {
        @SuppressWarnings("unchecked")
        Object firstElement = CollectionUtility.firstElement(values);
        if (firstElement instanceof Integer) {
          setValue((Integer) firstElement);
        }
        else {
          setValue(null);
        }
      }
      catch (Exception e) { // NOSONAR
        // nop
        setValue(null);
      }
    }

    @Override
    public void clearSelectionContext() {
      setValue(null);
    }

    @Override
    public List<Object> getValues() {
      if (getValue() == null) {
        return null;
      }
      return Collections.singletonList(getValue());
    }

    @Override
    public List<String> getTexts() {
      return CollectionUtility.arrayList(getDisplayText());
    }
  }

  @Order(10)
  @ClassId("c0b921cd-59f2-46cf-9108-533aa8612c8d")
  @FormData(defaultSubtypeSdkCommand = DefaultSubtypeSdkCommand.IGNORE)
  public class LongField extends AbstractLongField implements IComposerValueField {
    @Override
    public void addValueChangeListenerToTarget(PropertyChangeListener listener) {
      this.addPropertyChangeListener(listener);
    }

    @Override
    public void removeValueChangeListenerFromTarget(PropertyChangeListener listener) {
      this.removePropertyChangeListener(listener);
    }

    @Override
    public void setSelectionContext(IDataModelAttribute attribute, int dataType, IDataModelAttributeOp op, List values) {
      switch (dataType) {
        case IDataModelAttribute.TYPE_LONG: {
          setGroupingUsed(true);
          break;
        }
        case IDataModelAttribute.TYPE_PLAIN_LONG: {
          setGroupingUsed(false);
          break;
        }
      }
      try {
        @SuppressWarnings("unchecked")
        Object firstElement = CollectionUtility.firstElement(values);
        if (firstElement instanceof Long) {
          setValue((Long) firstElement);
        }
        else {
          setValue(null);
        }
      }
      catch (Exception e) { // NOSONAR
        // nop
        setValue(null);
      }
    }

    @Override
    public void clearSelectionContext() {
      setValue(null);
    }

    @Override
    public List<Object> getValues() {
      if (getValue() == null) {
        return null;
      }
      return Collections.singletonList(getValue());
    }

    @Override
    public List<String> getTexts() {
      return CollectionUtility.arrayList(getDisplayText());
    }
  }

  @Order(10)
  @ClassId("6cd73fce-6b42-4e38-9f77-3cd1b73e799c")
  @FormData(defaultSubtypeSdkCommand = DefaultSubtypeSdkCommand.IGNORE)
  public class BigDecimalField extends AbstractBigDecimalField implements IComposerValueField {
    @Override
    public void addValueChangeListenerToTarget(PropertyChangeListener listener) {
      this.addPropertyChangeListener(listener);
    }

    @Override
    public void removeValueChangeListenerFromTarget(PropertyChangeListener listener) {
      this.removePropertyChangeListener(listener);
    }

    @Override
    public void setSelectionContext(IDataModelAttribute attribute, int dataType, IDataModelAttributeOp op, List values) {
      switch (dataType) {
        case IDataModelAttribute.TYPE_BIG_DECIMAL: {
          setGroupingUsed(true);
          setPercent(false);
          break;
        }
        case IDataModelAttribute.TYPE_PERCENT: {
          setGroupingUsed(true);
          setPercent(true);
          break;
        }
        case IDataModelAttribute.TYPE_PLAIN_BIG_DECIMAL: {
          setGroupingUsed(false);
          setPercent(false);
          break;
        }
      }
      try {
        @SuppressWarnings("unchecked")
        Object firstElement = CollectionUtility.firstElement(values);
        if (firstElement instanceof BigDecimal) {
          setValue((BigDecimal) firstElement);
        }
        else {
          setValue(null);
        }
      }
      catch (Exception e) { // NOSONAR
        // nop
        setValue(null);
      }
    }

    @Override
    public void clearSelectionContext() {
      setValue(null);
    }

    @Override
    public List<Object> getValues() {
      if (getValue() == null) {
        return null;
      }
      return Collections.singletonList(getValue());
    }

    @Override
    public List<String> getTexts() {
      return CollectionUtility.arrayList(getDisplayText());
    }
  }

  @Order(10)
  @ClassId("055381df-86e7-4971-95bb-8d9f1074e409")
  @FormData(defaultSubtypeSdkCommand = DefaultSubtypeSdkCommand.IGNORE)
  public class StringField extends AbstractStringField implements IComposerValueField {
    @Override
    public void addValueChangeListenerToTarget(PropertyChangeListener listener) {
      this.addPropertyChangeListener(listener);
    }

    @Override
    public void removeValueChangeListenerFromTarget(PropertyChangeListener listener) {
      this.removePropertyChangeListener(listener);
    }

    @Override
    public void setSelectionContext(IDataModelAttribute attribute, int dataType, IDataModelAttributeOp op, List values) {
      try {
        @SuppressWarnings("unchecked")
        Object firstElement = CollectionUtility.firstElement(values);
        if (firstElement instanceof String) {
          setValue((String) firstElement);
        }
        else {
          setValue(null);
        }
      }
      catch (Exception e) { // NOSONAR
        // nop
        setValue(null);
      }
    }

    @Override
    public void clearSelectionContext() {
      setValue(null);
    }

    @Override
    public List<Object> getValues() {
      if (getValue() == null) {
        return null;
      }
      return Collections.singletonList(getValue());
    }

    @Override
    public List<String> getTexts() {
      return CollectionUtility.arrayList(getDisplayText());
    }
  }

  @Order(10)
  @ClassId("4707c050-3f4b-4ce3-a8c9-0452522becdc")
  @FormData(defaultSubtypeSdkCommand = DefaultSubtypeSdkCommand.IGNORE)
  public class SmartField extends AbstractSmartField<Object> implements IComposerValueField {

    @Override
    protected void execPrepareLookup(ILookupCall<Object> call) {
      if (m_attribute != null) {
        call.setActive(isActiveFilterEnabled() ? getActiveFilter() : TriState.TRUE);
        m_attribute.prepareLookup(call);
      }
    }

    @Override
    public void addValueChangeListenerToTarget(PropertyChangeListener listener) {
      this.addPropertyChangeListener(listener);
    }

    @Override
    public void removeValueChangeListenerFromTarget(PropertyChangeListener listener) {
      this.removePropertyChangeListener(listener);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void setSelectionContext(IDataModelAttribute attribute, int dataType, IDataModelAttributeOp op, List values) {
      setActiveFilterEnabled(attribute.isActiveFilterEnabled());
      ILookupCall<Object> newCall = (ILookupCall<Object>) op.getLookupCall();
      if (newCall == null) {
        newCall = attribute.getLookupCall();
      }
      if (getLookupCall() != newCall) {
        setLookupCall(newCall);
      }
      setSearchRequired(attribute.isSearchRequired());
      try {
        setValue(CollectionUtility.firstElement(values));
      }
      catch (Exception e) { // NOSONAR
        // nop
        setValue(null);
      }
      setBrowseHierarchy();
    }

    protected void setBrowseHierarchy() {
      if (LocalLookupCall.class.isAssignableFrom(getLookupCall().getClass()) &&
          ((LocalLookupCall<?>) getLookupCall()).isHierarchicalLookup()) {
        setBrowseHierarchy(true);
      }
      else {
        setBrowseHierarchy(false);
      }
    }

    @Override
    public void clearSelectionContext() {
      setValue(null);
    }

    @Override
    public List<Object> getValues() {
      if (getValue() == null) {
        return null;
      }
      return Collections.singletonList(getValue());
    }

    @Override
    public List<String> getTexts() {
      return CollectionUtility.arrayList(getDisplayText());
    }
  }

  @Order(10)
  @ClassId("83ead684-0cb1-43b3-8545-7815db2facb3")
  @FormData(defaultSubtypeSdkCommand = DefaultSubtypeSdkCommand.IGNORE)
  public class BetweenDateField extends AbstractSequenceBox implements IComposerValueField {
    @Order(10)
    @ClassId("4be8320d-841e-46c7-990f-47dc66294f9e")
    public class DateFromField extends AbstractDateField {
    }

    @Order(20)
    @ClassId("5a031971-bb60-4366-bbfc-3ce906e022f9")
    public class DateToField extends AbstractDateField {
    }

    @Override
    public void addValueChangeListenerToTarget(PropertyChangeListener listener) {
      for (IFormField f : getFields()) {
        f.addPropertyChangeListener(listener);
      }
    }

    @Override
    public void removeValueChangeListenerFromTarget(PropertyChangeListener listener) {
      for (IFormField f : getFields()) {
        f.removePropertyChangeListener(listener);
      }
    }

    @Override
    public void setSelectionContext(IDataModelAttribute attribute, int dataType, IDataModelAttributeOp op, List<?> values) {
      try {
        getFieldByClass(DateFromField.class).setValue(null);
        getFieldByClass(DateToField.class).setValue(null);

        if (values != null && values.size() == 2) {
          if (values.get(0) instanceof Date) {
            getFieldByClass(DateFromField.class).setValue((Date) values.get(0));
          }
          if (values.get(1) instanceof Date) {
            getFieldByClass(DateToField.class).setValue((Date) values.get(1));
          }
        }
      }
      catch (Exception e) { // NOSONAR
        // nop
      }
    }

    @Override
    public void clearSelectionContext() {
      getFieldByClass(DateFromField.class).setValue(null);
      getFieldByClass(DateToField.class).setValue(null);
    }

    @Override
    public List<Object> getValues() {
      Object a = getFieldByClass(DateFromField.class).getValue();
      Object b = getFieldByClass(DateToField.class).getValue();
      if (a == null && b == null) {
        return null;
      }
      return CollectionUtility.arrayList(a, b);
    }

    @Override
    public List<String> getTexts() {
      String a = getFieldByClass(DateFromField.class).getDisplayText();
      String b = getFieldByClass(DateToField.class).getDisplayText();
      return CollectionUtility.arrayList(a, b);
    }
  }

  @Order(10)
  @ClassId("85010cb1-d6fa-4879-a5d2-7247685b95b7")
  @FormData(defaultSubtypeSdkCommand = DefaultSubtypeSdkCommand.IGNORE)
  public class BetweenTimeField extends AbstractSequenceBox implements IComposerValueField {
    @Order(10)
    @ClassId("0a86c867-8907-4395-bf67-79c117c9c54c")
    public class TimeFromField extends AbstractTimeField {
    }

    @Order(20)
    @ClassId("0fb0159c-c781-4243-b1f4-169eb489692b")
    public class TimeToField extends AbstractTimeField {
    }

    @Override
    public void addValueChangeListenerToTarget(PropertyChangeListener listener) {
      for (IFormField f : getFields()) {
        f.addPropertyChangeListener(listener);
      }
    }

    @Override
    public void removeValueChangeListenerFromTarget(PropertyChangeListener listener) {
      for (IFormField f : getFields()) {
        f.removePropertyChangeListener(listener);
      }
    }

    @Override
    public void setSelectionContext(IDataModelAttribute attribute, int dataType, IDataModelAttributeOp op, List values) {
      try {
        getFieldByClass(TimeFromField.class).setValue(null);
        getFieldByClass(TimeToField.class).setValue(null);
        if (values != null && values.size() == 2) {
          if (values.get(0) instanceof Double) {
            getFieldByClass(TimeFromField.class).setTimeValue((Double) values.get(0));
          }
          else if (values.get(0) instanceof Date) {
            getFieldByClass(TimeFromField.class).setValue((Date) values.get(0));
          }
          if (values.get(1) instanceof Double) {
            getFieldByClass(TimeToField.class).setTimeValue((Double) values.get(1));
          }
          else if (values.get(1) instanceof Date) {
            getFieldByClass(TimeToField.class).setValue((Date) values.get(1));
          }
        }
      }
      catch (Exception e) { // NOSONAR
        // nop
      }
    }

    @Override
    public void clearSelectionContext() {
      getFieldByClass(TimeFromField.class).setValue(null);
      getFieldByClass(TimeToField.class).setValue(null);
    }

    @Override
    public List<Object> getValues() {
      Object a = getFieldByClass(TimeFromField.class).getValue();
      Object b = getFieldByClass(TimeToField.class).getValue();
      if (a == null && b == null) {
        return null;
      }
      return CollectionUtility.arrayList(a, b);
    }

    @Override
    public List<String> getTexts() {
      String a = getFieldByClass(TimeFromField.class).getDisplayText();
      String b = getFieldByClass(TimeToField.class).getDisplayText();
      return CollectionUtility.arrayList(a, b);
    }
  }

  @Order(10)
  @ClassId("cb94b834-2f10-4d81-b22b-c433bd065963")
  @FormData(defaultSubtypeSdkCommand = DefaultSubtypeSdkCommand.IGNORE)
  public class BetweenDateTimeField extends AbstractSequenceBox implements IComposerValueField {
    @Order(10)
    @ClassId("c94de66d-5d6d-44f4-9418-0fc8fc7c679c")
    public class DateTimeFromField extends AbstractDateField {
      @Override
      protected boolean getConfiguredHasTime() {
        return true;
      }
    }

    @Order(20)
    @ClassId("6e1d5025-5e29-498f-8027-75c3843c1d9e")
    public class DateTimeToField extends AbstractDateField {
      @Override
      protected boolean getConfiguredHasTime() {
        return true;
      }
    }

    @Override
    public void addValueChangeListenerToTarget(PropertyChangeListener listener) {
      for (IFormField f : getFields()) {
        f.addPropertyChangeListener(listener);
      }
    }

    @Override
    public void removeValueChangeListenerFromTarget(PropertyChangeListener listener) {
      for (IFormField f : getFields()) {
        f.removePropertyChangeListener(listener);
      }
    }

    @Override
    public void setSelectionContext(IDataModelAttribute attribute, int dataType, IDataModelAttributeOp op, List values) {
      try {
        getFieldByClass(DateTimeFromField.class).setValue(null);
        getFieldByClass(DateTimeToField.class).setValue(null);
        if (values != null && values.size() == 2) {
          if (values.get(0) instanceof Date) {
            getFieldByClass(DateTimeFromField.class).setValue((Date) values.get(0));
          }
          if (values.get(1) instanceof Date) {
            getFieldByClass(DateTimeToField.class).setValue((Date) values.get(1));
          }
        }
      }
      catch (Exception e) { // NOSONAR
        // nop
      }
    }

    @Override
    public void clearSelectionContext() {
      getFieldByClass(DateTimeFromField.class).setValue(null);
      getFieldByClass(DateTimeToField.class).setValue(null);
    }

    @Override
    public List<Object> getValues() {
      Object a = getFieldByClass(DateTimeFromField.class).getValue();
      Object b = getFieldByClass(DateTimeToField.class).getValue();
      if (a == null && b == null) {
        return null;
      }
      return CollectionUtility.arrayList(a, b);
    }

    @Override
    public List<String> getTexts() {
      String a = getFieldByClass(DateTimeFromField.class).getDisplayText();
      String b = getFieldByClass(DateTimeToField.class).getDisplayText();
      return CollectionUtility.arrayList(a, b);
    }
  }

  @Order(10)
  @ClassId("4d0ae72b-dffd-4a09-945b-4e50d4037d16")
  @FormData(defaultSubtypeSdkCommand = DefaultSubtypeSdkCommand.IGNORE)
  public class BetweenIntegerField extends AbstractSequenceBox implements IComposerValueField {
    @Order(10)
    @ClassId("f17a7e6d-1df2-484e-9e96-8b3d32c7d095")
    public class IntegerFromField extends AbstractIntegerField {
    }

    @Order(20)
    @ClassId("9cdddae5-de01-41d6-9cfc-5ae7c0ad3663")
    public class IntegerToField extends AbstractIntegerField {
    }

    @Override
    public void addValueChangeListenerToTarget(PropertyChangeListener listener) {
      for (IFormField f : getFields()) {
        f.addPropertyChangeListener(listener);
      }
    }

    @Override
    public void removeValueChangeListenerFromTarget(PropertyChangeListener listener) {
      for (IFormField f : getFields()) {
        f.removePropertyChangeListener(listener);
      }
    }

    @Override
    public void setSelectionContext(IDataModelAttribute attribute, int dataType, IDataModelAttributeOp op, List values) {
      try {
        getFieldByClass(IntegerFromField.class).setValue(null);
        getFieldByClass(IntegerToField.class).setValue(null);
        if (values != null && values.size() == 2) {
          if (values.get(0) instanceof Integer) {
            getFieldByClass(IntegerFromField.class).setValue((Integer) values.get(0));
          }
          if (values.get(1) instanceof Integer) {
            getFieldByClass(IntegerToField.class).setValue((Integer) values.get(1));
          }
        }
      }
      catch (Exception e) { // NOSONAR
        // nop
      }
    }

    @Override
    public void clearSelectionContext() {
      getFieldByClass(IntegerFromField.class).setValue(null);
      getFieldByClass(IntegerToField.class).setValue(null);
    }

    @Override
    public List<Object> getValues() {
      Object a = getFieldByClass(IntegerFromField.class).getValue();
      Object b = getFieldByClass(IntegerToField.class).getValue();
      if (a == null && b == null) {
        return null;
      }
      return CollectionUtility.arrayList(a, b);
    }

    @Override
    public List<String> getTexts() {
      String a = getFieldByClass(IntegerFromField.class).getDisplayText();
      String b = getFieldByClass(IntegerToField.class).getDisplayText();
      return CollectionUtility.arrayList(a, b);
    }
  }

  @Order(10)
  @ClassId("91376e0a-af80-4deb-b089-2675c3870dbc")
  @FormData(defaultSubtypeSdkCommand = DefaultSubtypeSdkCommand.IGNORE)
  public class BetweenLongField extends AbstractSequenceBox implements IComposerValueField {
    @Order(10)
    @ClassId("c084e230-ccc8-4672-bcfd-09538630e9e7")
    public class LongFromField extends AbstractLongField {
    }

    @Order(20)
    @ClassId("e94e9352-4690-4dcd-8be4-d73135636d27")
    public class LongToField extends AbstractLongField {
    }

    @Override
    public void addValueChangeListenerToTarget(PropertyChangeListener listener) {
      for (IFormField f : getFields()) {
        f.addPropertyChangeListener(listener);
      }
    }

    @Override
    public void removeValueChangeListenerFromTarget(PropertyChangeListener listener) {
      for (IFormField f : getFields()) {
        f.removePropertyChangeListener(listener);
      }
    }

    @Override
    public void setSelectionContext(IDataModelAttribute attribute, int dataType, IDataModelAttributeOp op, List values) {
      try {
        getFieldByClass(LongFromField.class).setValue(null);
        getFieldByClass(LongToField.class).setValue(null);
        if (values != null && values.size() == 2) {
          if (values.get(0) instanceof Long) {
            getFieldByClass(LongFromField.class).setValue((Long) values.get(0));
          }
          if (values.get(1) instanceof Long) {
            getFieldByClass(LongToField.class).setValue((Long) values.get(1));
          }
        }
      }
      catch (Exception e) { // NOSONAR
        // nop
      }
    }

    @Override
    public void clearSelectionContext() {
      getFieldByClass(LongFromField.class).setValue(null);
      getFieldByClass(LongToField.class).setValue(null);
    }

    @Override
    public List<Object> getValues() {
      Object a = getFieldByClass(LongFromField.class).getValue();
      Object b = getFieldByClass(LongToField.class).getValue();
      if (a == null && b == null) {
        return null;
      }
      return CollectionUtility.arrayList(a, b);
    }

    @Override
    public List<String> getTexts() {
      String a = getFieldByClass(LongFromField.class).getDisplayText();
      String b = getFieldByClass(LongToField.class).getDisplayText();
      return CollectionUtility.arrayList(a, b);
    }
  }

  @Order(10)
  @ClassId("1eb9aa40-936f-4cd0-b34c-c345c7486685")
  @FormData(defaultSubtypeSdkCommand = DefaultSubtypeSdkCommand.IGNORE)
  public class BetweenBigDecimalField extends AbstractSequenceBox implements IComposerValueField {
    @Order(10)
    @ClassId("07c002ce-e3df-45d5-8f5e-5acdd4fb1ad1")
    public class BigDecimalFromField extends AbstractBigDecimalField {
    }

    @Order(20)
    @ClassId("3df14616-fdbc-4854-8b83-6feb08518047")
    public class BigDecimalToField extends AbstractBigDecimalField {
    }

    @Override
    public void addValueChangeListenerToTarget(PropertyChangeListener listener) {
      for (IFormField f : getFields()) {
        f.addPropertyChangeListener(listener);
      }
    }

    @Override
    public void removeValueChangeListenerFromTarget(PropertyChangeListener listener) {
      for (IFormField f : getFields()) {
        f.removePropertyChangeListener(listener);
      }
    }

    @Override
    public void setSelectionContext(IDataModelAttribute attribute, int dataType, IDataModelAttributeOp op, List values) {
      try {
        getFieldByClass(BigDecimalFromField.class).setValue(null);
        getFieldByClass(BigDecimalToField.class).setValue(null);
        if (values != null && values.size() == 2) {
          if (values.get(0) instanceof BigDecimal) {
            getFieldByClass(BigDecimalFromField.class).setValue((BigDecimal) values.get(0));
          }
          if (values.get(1) instanceof BigDecimal) {
            getFieldByClass(BigDecimalToField.class).setValue((BigDecimal) values.get(1));
          }
        }
      }
      catch (Exception e) { // NOSONAR
        // nop
      }
    }

    @Override
    public void clearSelectionContext() {
      getFieldByClass(BigDecimalFromField.class).setValue(null);
      getFieldByClass(BigDecimalToField.class).setValue(null);
    }

    @Override
    public List<Object> getValues() {
      Object a = getFieldByClass(BigDecimalFromField.class).getValue();
      Object b = getFieldByClass(BigDecimalToField.class).getValue();
      if (a == null && b == null) {
        return null;
      }
      return CollectionUtility.arrayList(a, b);
    }

    @Override
    public List<String> getTexts() {
      String a = getFieldByClass(BigDecimalFromField.class).getDisplayText();
      String b = getFieldByClass(BigDecimalToField.class).getDisplayText();
      return CollectionUtility.arrayList(a, b);
    }
  }

  protected final void interceptChangedValue() {
    List<? extends IFormFieldExtension<? extends AbstractFormField>> extensions = getAllExtensions();
    ComposerValueBoxChangedValueChain chain = new ComposerValueBoxChangedValueChain(extensions);
    chain.execChangedValue();
  }

  protected final void interceptInitOperatorToFieldMap(Map<Integer /* operator */, Map<Integer /* field type */, IComposerValueField>> operatorTypeToFieldMap) {
    List<? extends IFormFieldExtension<? extends AbstractFormField>> extensions = getAllExtensions();
    ComposerValueBoxInitOperatorToFieldMapChain chain = new ComposerValueBoxInitOperatorToFieldMapChain(extensions);
    chain.execInitOperatorToFieldMap(operatorTypeToFieldMap);
  }

  protected static class LocalComposerValueBoxExtension<OWNER extends AbstractComposerValueBox> extends LocalGroupBoxExtension<OWNER> implements IComposerValueBoxExtension<OWNER> {

    public LocalComposerValueBoxExtension(OWNER owner) {
      super(owner);
    }

    @Override
    public void execChangedValue(ComposerValueBoxChangedValueChain chain) {
      getOwner().execChangedValue();
    }

    @Override
    public void execInitOperatorToFieldMap(ComposerValueBoxInitOperatorToFieldMapChain chain, Map<Integer, Map<Integer, IComposerValueField>> operatorTypeToFieldMap) {
      getOwner().execInitOperatorToFieldMap(operatorTypeToFieldMap);
    }
  }

  @Override
  protected IComposerValueBoxExtension<? extends AbstractComposerValueBox> createLocalExtension() {
    return new LocalComposerValueBoxExtension<>(this);
  }
}
