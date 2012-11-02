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
package org.eclipse.scout.rt.client.ui.form.fields.composer;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.scout.commons.TriState;
import org.eclipse.scout.commons.annotations.ConfigOperation;
import org.eclipse.scout.commons.annotations.FormData;
import org.eclipse.scout.commons.annotations.FormData.DefaultSubtypeSdkCommand;
import org.eclipse.scout.commons.annotations.Order;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.client.ui.basic.tree.ITreeNode;
import org.eclipse.scout.rt.client.ui.form.fields.IFormField;
import org.eclipse.scout.rt.client.ui.form.fields.IValueField;
import org.eclipse.scout.rt.client.ui.form.fields.datefield.AbstractDateField;
import org.eclipse.scout.rt.client.ui.form.fields.datefield.AbstractTimeField;
import org.eclipse.scout.rt.client.ui.form.fields.doublefield.AbstractDoubleField;
import org.eclipse.scout.rt.client.ui.form.fields.groupbox.AbstractGroupBox;
import org.eclipse.scout.rt.client.ui.form.fields.integerfield.AbstractIntegerField;
import org.eclipse.scout.rt.client.ui.form.fields.listbox.AbstractListBox;
import org.eclipse.scout.rt.client.ui.form.fields.longfield.AbstractLongField;
import org.eclipse.scout.rt.client.ui.form.fields.sequencebox.AbstractSequenceBox;
import org.eclipse.scout.rt.client.ui.form.fields.sequencebox.ISequenceBox;
import org.eclipse.scout.rt.client.ui.form.fields.smartfield.AbstractSmartField;
import org.eclipse.scout.rt.client.ui.form.fields.stringfield.AbstractStringField;
import org.eclipse.scout.rt.client.ui.form.fields.treebox.AbstractTreeBox;
import org.eclipse.scout.rt.shared.AbstractIcons;
import org.eclipse.scout.rt.shared.ScoutTexts;
import org.eclipse.scout.rt.shared.data.model.DataModelConstants;
import org.eclipse.scout.rt.shared.data.model.IDataModelAttribute;
import org.eclipse.scout.rt.shared.data.model.IDataModelAttributeOp;
import org.eclipse.scout.rt.shared.services.common.jdbc.SearchFilter;
import org.eclipse.scout.rt.shared.services.lookup.LookupCall;

/**
 * Template box containing all composer values.
 * <p>
 */
public abstract class AbstractComposerValueBox extends AbstractGroupBox {
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(AbstractComposerValueBox.class);

  private Map<Integer/*operator*/, Map<Integer/*dataType*/, IComposerValueField>> m_opTypeToFieldMap;
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
  protected void execChangedValue() throws ProcessingException {
  }

  @Override
  protected void execAddSearchTerms(SearchFilter search) {
    //nop
  }

  @Override
  protected void initConfig() {
    super.initConfig();
    m_opTypeToFieldMap = new HashMap<Integer, Map<Integer, IComposerValueField>>();
    //specific operators
    HashMap<Integer, IComposerValueField> betweenMap = new HashMap<Integer, IComposerValueField>();
    betweenMap.put(IDataModelAttribute.TYPE_DATE, getFieldByClass(BetweenDateField.class));
    betweenMap.put(IDataModelAttribute.TYPE_DATE_TIME, getFieldByClass(BetweenDateTimeField.class));
    betweenMap.put(IDataModelAttribute.TYPE_DOUBLE, getFieldByClass(BetweenDoubleField.class));
    betweenMap.put(IDataModelAttribute.TYPE_AGGREGATE_COUNT, getFieldByClass(BetweenIntegerField.class));
    betweenMap.put(IDataModelAttribute.TYPE_INTEGER, getFieldByClass(BetweenIntegerField.class));
    betweenMap.put(IDataModelAttribute.TYPE_LONG, getFieldByClass(BetweenLongField.class));
    betweenMap.put(IDataModelAttribute.TYPE_PERCENT, getFieldByClass(BetweenDoubleField.class));
    betweenMap.put(IDataModelAttribute.TYPE_PLAIN_DOUBLE, getFieldByClass(BetweenDoubleField.class));
    betweenMap.put(IDataModelAttribute.TYPE_PLAIN_INTEGER, getFieldByClass(BetweenIntegerField.class));
    betweenMap.put(IDataModelAttribute.TYPE_PLAIN_LONG, getFieldByClass(BetweenLongField.class));
    betweenMap.put(IDataModelAttribute.TYPE_TIME, getFieldByClass(BetweenTimeField.class));
    m_opTypeToFieldMap.put(DataModelConstants.OPERATOR_BETWEEN, betweenMap);
    m_opTypeToFieldMap.put(DataModelConstants.OPERATOR_DATE_BETWEEN, betweenMap);
    m_opTypeToFieldMap.put(DataModelConstants.OPERATOR_DATE_TIME_BETWEEN, betweenMap);
    //type defaults
    HashMap<Integer, IComposerValueField> defaultMap = new HashMap<Integer, IComposerValueField>();
    defaultMap.put(IDataModelAttribute.TYPE_DATE, getFieldByClass(DateField.class));
    defaultMap.put(IDataModelAttribute.TYPE_DATE_TIME, getFieldByClass(DateTimeField.class));
    defaultMap.put(IDataModelAttribute.TYPE_DOUBLE, getFieldByClass(DoubleField.class));
    defaultMap.put(IDataModelAttribute.TYPE_AGGREGATE_COUNT, getFieldByClass(IntegerField.class));
    defaultMap.put(IDataModelAttribute.TYPE_INTEGER, getFieldByClass(IntegerField.class));
    defaultMap.put(IDataModelAttribute.TYPE_NUMBER_LIST, getFieldByClass(ListBoxField.class));
    defaultMap.put(IDataModelAttribute.TYPE_NUMBER_TREE, getFieldByClass(TreeBoxField.class));
    defaultMap.put(IDataModelAttribute.TYPE_CODE_LIST, getFieldByClass(ListBoxField.class));
    defaultMap.put(IDataModelAttribute.TYPE_CODE_TREE, getFieldByClass(TreeBoxField.class));
    defaultMap.put(IDataModelAttribute.TYPE_LONG, getFieldByClass(LongField.class));
    defaultMap.put(IDataModelAttribute.TYPE_PERCENT, getFieldByClass(DoubleField.class));
    defaultMap.put(IDataModelAttribute.TYPE_PLAIN_DOUBLE, getFieldByClass(DoubleField.class));
    defaultMap.put(IDataModelAttribute.TYPE_PLAIN_INTEGER, getFieldByClass(IntegerField.class));
    defaultMap.put(IDataModelAttribute.TYPE_PLAIN_LONG, getFieldByClass(LongField.class));
    defaultMap.put(IDataModelAttribute.TYPE_STRING, getFieldByClass(StringField.class));
    defaultMap.put(IDataModelAttribute.TYPE_FULL_TEXT, getFieldByClass(StringField.class));
    defaultMap.put(IDataModelAttribute.TYPE_SMART, getFieldByClass(SmartField.class));
    defaultMap.put(IDataModelAttribute.TYPE_TIME, getFieldByClass(TimeField.class));
    m_opTypeToFieldMap.put(0, defaultMap);
    //
    m_valueChangedListener = new PropertyChangeListener() {
      @Override
      public void propertyChange(PropertyChangeEvent e) {
        if (IValueField.PROP_VALUE.equals(e.getPropertyName())) {
          try {
            execChangedValue();
          }
          catch (Throwable t) {
            LOG.error("fire value change on " + e.getSource(), t);
          }
        }
      }
    };
    for (IFormField f : getFields()) {
      f.setLabelVisible(false);
      f.setLabel(ScoutTexts.get("Value"));
      f.setVisible(false);
      if (f instanceof ISequenceBox) {
        IFormField[] sequenceBoxChildFields = ((ISequenceBox) f).getFields();
        if (sequenceBoxChildFields.length > 0) {
          sequenceBoxChildFields[0].setLabelVisible(false);
        }
        if (sequenceBoxChildFields.length > 1 && sequenceBoxChildFields[1].getLabel() == null) {
          sequenceBoxChildFields[1].setLabel(ScoutTexts.get("and"));
        }
      }
    }
  }

  /**
   * @return the selected field (may be a sequence box) or null if no field is selected
   */
  public IComposerValueField getSelectedField() {
    return m_selectedField;
  }

  public void setSelectionContext(IDataModelAttribute attribute, IDataModelAttributeOp op, Object[] values) {
    if (op == null) {
      return;
    }
    if (values == null && getSelectedField() != null) {
      values = getSelectedField().getValues();
    }
    m_attribute = attribute;
    //
    int dataType = op.getType();
    if (dataType == IDataModelAttribute.TYPE_INHERITED) {
      dataType = attribute.getType();
    }
    Map<Integer, IComposerValueField> typeToFieldMap = m_opTypeToFieldMap.get(op.getOperator());
    if (typeToFieldMap == null) {
      //default
      typeToFieldMap = m_opTypeToFieldMap.get(0);
    }
    IComposerValueField valueField = typeToFieldMap.get(dataType);
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
      if (f == m_selectedField) {
        f.setVisible(true);
      }
      else {
        f.setVisible(false);
      }
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
    protected void execPrepareLookup(LookupCall call) throws ProcessingException {
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

    @Override
    public void setSelectionContext(IDataModelAttribute attribute, int dataType, IDataModelAttributeOp op, Object[] values) {
      setFilterActiveRows(attribute.isActiveFilterEnabled());
      LookupCall newCall = attribute.getLookupCall();
      if (getLookupCall() != newCall) {
        setLookupCall(attribute.getLookupCall());
        try {
          loadListBoxData();
        }
        catch (Exception e) {
          LOG.warn(null, e);
          // nop
        }
      }
      try {
        if (values != null && values.length == 1 && values[0] instanceof Object[]) {
          setValue((Object[]) values[0]);
        }
        else {
          setValue(null);
        }
      }
      catch (Exception e) {
        // nop
        setValue(null);
      }
    }

    @Override
    public void clearSelectionContext() {
      setValue(null);
    }

    @Override
    public Object[] getValues() {
      if (getCheckedKeyCount() > 0) {
        return new Object[]{getValue()};
      }
      else {
        return null;
      }
    }

    @Override
    public String[] getTexts() {
      return new String[]{getDisplayText()};
    }
  }

  @Order(10)
  @FormData(defaultSubtypeSdkCommand = DefaultSubtypeSdkCommand.IGNORE)
  public class TreeBoxField extends AbstractTreeBox<Object> implements IComposerValueField {

    @Override
    protected boolean getConfiguredAutoLoad() {
      return false;
    }

    @Override
    protected int getConfiguredGridH() {
      return 4;
    }

    @Override
    protected void execPrepareLookup(LookupCall call, ITreeNode parent) throws ProcessingException {
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
    public void setSelectionContext(IDataModelAttribute attribute, int dataType, IDataModelAttributeOp op, Object[] values) {
      setFilterActiveNodes(attribute.isActiveFilterEnabled());
      LookupCall newCall = attribute.getLookupCall();
      if (getLookupCall() != newCall) {
        setLookupCall(newCall);
        try {
          loadRootNode();
          getTree().setNodeExpanded(getTree().getRootNode(), true);
        }
        catch (Exception e) {
          LOG.warn(null, e);
          // nop
        }
      }
      try {
        if (values != null && values.length == 1 && values[0] instanceof Object[]) {
          setValue((Object[]) values[0]);
        }
        else {
          setValue(null);
        }
      }
      catch (Exception e) {
        // nop
        setValue(null);
      }
    }

    @Override
    public void clearSelectionContext() {
      setValue(null);
    }

    @Override
    public Object[] getValues() {
      if (getCheckedKeyCount() > 0) {
        return new Object[]{getValue()};
      }
      else {
        return null;
      }
    }

    @Override
    public String[] getTexts() {
      return new String[]{getDisplayText()};
    }
  }

  @Order(10)
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
    public void setSelectionContext(IDataModelAttribute attribute, int dataType, IDataModelAttributeOp op, Object[] values) {
      try {
        if (values != null && values.length == 1 && values[0] instanceof Date) {
          setValue((Date) values[0]);
        }
        else {
          setValue(null);
        }
      }
      catch (Exception e) {
        // nop
        setValue(null);
      }
    }

    @Override
    public void clearSelectionContext() {
      setValue(null);
    }

    @Override
    public Object[] getValues() {
      if (getValue() != null) {
        return new Object[]{getValue()};
      }
      else {
        return null;
      }
    }

    @Override
    public String[] getTexts() {
      return new String[]{getDisplayText()};
    }
  }

  @Order(10)
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
    public void setSelectionContext(IDataModelAttribute attribute, int dataType, IDataModelAttributeOp op, Object[] values) {
      try {
        if (values != null && values.length == 1) {
          if (values[0] instanceof Double) {
            setTimeValue((Double) values[0]);
          }
          else if (values[0] instanceof Date) {
            setValue((Date) values[0]);
          }
          else {
            setValue(null);
          }
        }
        else {
          setValue(null);
        }
      }
      catch (Exception e) {
        // nop
        setValue(null);
      }
    }

    @Override
    public void clearSelectionContext() {
      setValue(null);
    }

    @Override
    public Object[] getValues() {
      if (getValue() != null) {
        return new Object[]{getValue()};
      }
      else {
        return null;
      }
    }

    @Override
    public String[] getTexts() {
      return new String[]{getDisplayText()};
    }
  }

  @Order(10)
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
    public void setSelectionContext(IDataModelAttribute attribute, int dataType, IDataModelAttributeOp op, Object[] values) {
      try {
        if (values != null && values.length == 1 && values[0] instanceof Date) {
          setValue((Date) values[0]);
        }
        else {
          setValue(null);
        }
      }
      catch (Exception e) {
        // nop
        setValue(null);
      }
    }

    @Override
    public void clearSelectionContext() {
      setValue(null);
    }

    @Override
    public Object[] getValues() {
      if (getValue() != null) {
        return new Object[]{getValue()};
      }
      else {
        return null;
      }
    }

    @Override
    public String[] getTexts() {
      return new String[]{getDisplayText()};
    }
  }

  @Order(10)
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
    public void setSelectionContext(IDataModelAttribute attribute, int dataType, IDataModelAttributeOp op, Object[] values) {
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
        if (values != null && values.length == 1 && values[0] instanceof Integer) {
          setValue((Integer) values[0]);
        }
        else {
          setValue(null);
        }
      }
      catch (Exception e) {
        // nop
        setValue(null);
      }
    }

    @Override
    public void clearSelectionContext() {
      setValue(null);
    }

    @Override
    public Object[] getValues() {
      if (getValue() != null) {
        return new Object[]{getValue()};
      }
      else {
        return null;
      }
    }

    @Override
    public String[] getTexts() {
      return new String[]{getDisplayText()};
    }
  }

  @Order(10)
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
    public void setSelectionContext(IDataModelAttribute attribute, int dataType, IDataModelAttributeOp op, Object[] values) {
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
        if (values != null && values.length == 1 && values[0] instanceof Long) {
          setValue((Long) values[0]);
        }
        else {
          setValue(null);
        }
      }
      catch (Exception e) {
        // nop
        setValue(null);
      }
    }

    @Override
    public void clearSelectionContext() {
      setValue(null);
    }

    @Override
    public Object[] getValues() {
      if (getValue() != null) {
        return new Object[]{getValue()};
      }
      else {
        return null;
      }
    }

    @Override
    public String[] getTexts() {
      return new String[]{getDisplayText()};
    }
  }

  @Order(10)
  @FormData(defaultSubtypeSdkCommand = DefaultSubtypeSdkCommand.IGNORE)
  public class DoubleField extends AbstractDoubleField implements IComposerValueField {
    @Override
    public void addValueChangeListenerToTarget(PropertyChangeListener listener) {
      this.addPropertyChangeListener(listener);
    }

    @Override
    public void removeValueChangeListenerFromTarget(PropertyChangeListener listener) {
      this.removePropertyChangeListener(listener);
    }

    @Override
    public void setSelectionContext(IDataModelAttribute attribute, int dataType, IDataModelAttributeOp op, Object[] values) {
      switch (dataType) {
        case IDataModelAttribute.TYPE_DOUBLE: {
          setGroupingUsed(true);
          setPercent(false);
          break;
        }
        case IDataModelAttribute.TYPE_PERCENT: {
          setGroupingUsed(true);
          setPercent(true);
          break;
        }
        case IDataModelAttribute.TYPE_PLAIN_DOUBLE: {
          setGroupingUsed(false);
          setPercent(false);
          break;
        }
      }
      try {
        if (values != null && values.length == 1 && values[0] instanceof Double) {
          setValue((Double) values[0]);
        }
        else {
          setValue(null);
        }
      }
      catch (Exception e) {
        // nop
        setValue(null);
      }
    }

    @Override
    public void clearSelectionContext() {
      setValue(null);
    }

    @Override
    public Object[] getValues() {
      if (getValue() != null) {
        return new Object[]{getValue()};
      }
      else {
        return null;
      }
    }

    @Override
    public String[] getTexts() {
      return new String[]{getDisplayText()};
    }
  }

  @Order(10)
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
    public void setSelectionContext(IDataModelAttribute attribute, int dataType, IDataModelAttributeOp op, Object[] values) {
      try {
        if (values != null && values.length == 1 && values[0] instanceof String) {
          setValue((String) values[0]);
        }
        else {
          setValue(null);
        }
      }
      catch (Exception e) {
        // nop
        setValue(null);
      }
    }

    @Override
    public void clearSelectionContext() {
      setValue(null);
    }

    @Override
    public Object[] getValues() {
      if (getValue() != null) {
        return new Object[]{getValue()};
      }
      else {
        return null;
      }
    }

    @Override
    public String[] getTexts() {
      return new String[]{getDisplayText()};
    }
  }

  @Order(10)
  @FormData(defaultSubtypeSdkCommand = DefaultSubtypeSdkCommand.IGNORE)
  public class SmartField extends AbstractSmartField<Object> implements IComposerValueField {

    @Override
    protected String getConfiguredBrowseIconId() {
      return AbstractIcons.Empty;
    }

    @Override
    protected void execPrepareLookup(LookupCall call) throws ProcessingException {
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

    @Override
    public void setSelectionContext(IDataModelAttribute attribute, int dataType, IDataModelAttributeOp op, Object[] values) {
      setActiveFilterEnabled(attribute.isActiveFilterEnabled());
      LookupCall newCall = attribute.getLookupCall();
      if (getLookupCall() != newCall) {
        setLookupCall(newCall);
      }
      try {
        if (values != null && values.length == 1) {
          setValue(values[0]);
        }
        else {
          setValue(null);
        }
      }
      catch (Exception e) {
        // nop
        setValue(null);
      }
    }

    @Override
    public void clearSelectionContext() {
      setValue(null);
    }

    @Override
    public Object[] getValues() {
      if (getValue() != null) {
        return new Object[]{getValue()};
      }
      else {
        return null;
      }
    }

    @Override
    public String[] getTexts() {
      return new String[]{getDisplayText()};
    }
  }

  //XXXXXXXXXXXXXXXXXXXXXXX

  @Order(10)
  @FormData(defaultSubtypeSdkCommand = DefaultSubtypeSdkCommand.IGNORE)
  public class BetweenDateField extends AbstractSequenceBox implements IComposerValueField {
    @Order(10)
    public class DateFromField extends AbstractDateField {
    }

    @Order(20)
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
    public void setSelectionContext(IDataModelAttribute attribute, int dataType, IDataModelAttributeOp op, Object[] values) {
      try {
        getFieldByClass(BetweenDateField.DateFromField.class).setValue(null);
        getFieldByClass(BetweenDateField.DateToField.class).setValue(null);
        if (values != null && values.length == 2) {
          if (values[0] instanceof Date) {
            getFieldByClass(BetweenDateField.DateFromField.class).setValue((Date) values[0]);
          }
          if (values[1] instanceof Date) {
            getFieldByClass(BetweenDateField.DateToField.class).setValue((Date) values[1]);
          }
        }
      }
      catch (Exception e) {
        // nop
      }
    }

    @Override
    public void clearSelectionContext() {
      getFieldByClass(BetweenDateField.DateFromField.class).setValue(null);
      getFieldByClass(BetweenDateField.DateToField.class).setValue(null);
    }

    @Override
    public Object[] getValues() {
      Object a = getFieldByClass(BetweenDateField.DateFromField.class).getValue();
      Object b = getFieldByClass(BetweenDateField.DateToField.class).getValue();
      if (a != null || b != null) {
        return new Object[]{a, b};
      }
      else {
        return null;
      }
    }

    @Override
    public String[] getTexts() {
      String a = getFieldByClass(BetweenDateField.DateFromField.class).getDisplayText();
      String b = getFieldByClass(BetweenDateField.DateToField.class).getDisplayText();
      return new String[]{a, b};
    }
  }

  @Order(10)
  @FormData(defaultSubtypeSdkCommand = DefaultSubtypeSdkCommand.IGNORE)
  public class BetweenTimeField extends AbstractSequenceBox implements IComposerValueField {
    @Order(10)
    public class TimeFromField extends AbstractTimeField {
    }

    @Order(20)
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
    public void setSelectionContext(IDataModelAttribute attribute, int dataType, IDataModelAttributeOp op, Object[] values) {
      try {
        getFieldByClass(BetweenTimeField.TimeFromField.class).setValue(null);
        getFieldByClass(BetweenTimeField.TimeToField.class).setValue(null);
        if (values != null && values.length == 2) {
          if (values[0] instanceof Double) {
            getFieldByClass(BetweenTimeField.TimeFromField.class).setTimeValue((Double) values[0]);
          }
          else if (values[0] instanceof Date) {
            getFieldByClass(BetweenTimeField.TimeFromField.class).setValue((Date) values[0]);
          }
          if (values[1] instanceof Double) {
            getFieldByClass(BetweenTimeField.TimeToField.class).setTimeValue((Double) values[1]);
          }
          else if (values[1] instanceof Date) {
            getFieldByClass(BetweenTimeField.TimeToField.class).setValue((Date) values[1]);
          }
        }
      }
      catch (Exception e) {
        // nop
      }
    }

    @Override
    public void clearSelectionContext() {
      getFieldByClass(BetweenTimeField.TimeFromField.class).setValue(null);
      getFieldByClass(BetweenTimeField.TimeToField.class).setValue(null);
    }

    @Override
    public Object[] getValues() {
      Object a = getFieldByClass(BetweenTimeField.TimeFromField.class).getValue();
      Object b = getFieldByClass(BetweenTimeField.TimeToField.class).getValue();
      if (a != null || b != null) {
        return new Object[]{a, b};
      }
      else {
        return null;
      }
    }

    @Override
    public String[] getTexts() {
      String a = getFieldByClass(BetweenTimeField.TimeFromField.class).getDisplayText();
      String b = getFieldByClass(BetweenTimeField.TimeToField.class).getDisplayText();
      return new String[]{a, b};
    }
  }

  @Order(10)
  @FormData(defaultSubtypeSdkCommand = DefaultSubtypeSdkCommand.IGNORE)
  public class BetweenDateTimeField extends AbstractSequenceBox implements IComposerValueField {
    @Order(10)
    public class DateTimeFromField extends AbstractDateField {
      @Override
      protected boolean getConfiguredHasTime() {
        return true;
      }
    }

    @Order(20)
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
    public void setSelectionContext(IDataModelAttribute attribute, int dataType, IDataModelAttributeOp op, Object[] values) {
      try {
        getFieldByClass(BetweenDateTimeField.DateTimeFromField.class).setValue(null);
        getFieldByClass(BetweenDateTimeField.DateTimeToField.class).setValue(null);
        if (values != null && values.length == 2) {
          if (values[0] instanceof Date) {
            getFieldByClass(BetweenDateTimeField.DateTimeFromField.class).setValue((Date) values[0]);
          }
          if (values[1] instanceof Date) {
            getFieldByClass(BetweenDateTimeField.DateTimeToField.class).setValue((Date) values[1]);
          }
        }
      }
      catch (Exception e) {
        // nop
      }
    }

    @Override
    public void clearSelectionContext() {
      getFieldByClass(BetweenDateTimeField.DateTimeFromField.class).setValue(null);
      getFieldByClass(BetweenDateTimeField.DateTimeToField.class).setValue(null);
    }

    @Override
    public Object[] getValues() {
      Object a = getFieldByClass(BetweenDateTimeField.DateTimeFromField.class).getValue();
      Object b = getFieldByClass(BetweenDateTimeField.DateTimeToField.class).getValue();
      if (a != null || b != null) {
        return new Object[]{a, b};
      }
      else {
        return null;
      }
    }

    @Override
    public String[] getTexts() {
      String a = getFieldByClass(BetweenDateTimeField.DateTimeFromField.class).getDisplayText();
      String b = getFieldByClass(BetweenDateTimeField.DateTimeToField.class).getDisplayText();
      return new String[]{a, b};
    }
  }

  @Order(10)
  @FormData(defaultSubtypeSdkCommand = DefaultSubtypeSdkCommand.IGNORE)
  public class BetweenIntegerField extends AbstractSequenceBox implements IComposerValueField {
    @Order(10)
    public class IntegerFromField extends AbstractIntegerField {
    }

    @Order(20)
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
    public void setSelectionContext(IDataModelAttribute attribute, int dataType, IDataModelAttributeOp op, Object[] values) {
      try {
        getFieldByClass(BetweenIntegerField.IntegerFromField.class).setValue(null);
        getFieldByClass(BetweenIntegerField.IntegerToField.class).setValue(null);
        if (values != null && values.length == 2) {
          if (values[0] instanceof Integer) {
            getFieldByClass(BetweenIntegerField.IntegerFromField.class).setValue((Integer) values[0]);
          }
          if (values[1] instanceof Integer) {
            getFieldByClass(BetweenIntegerField.IntegerToField.class).setValue((Integer) values[1]);
          }
        }
      }
      catch (Exception e) {
        // nop
      }
    }

    @Override
    public void clearSelectionContext() {
      getFieldByClass(BetweenIntegerField.IntegerFromField.class).setValue(null);
      getFieldByClass(BetweenIntegerField.IntegerToField.class).setValue(null);
    }

    @Override
    public Object[] getValues() {
      Object a = getFieldByClass(BetweenIntegerField.IntegerFromField.class).getValue();
      Object b = getFieldByClass(BetweenIntegerField.IntegerToField.class).getValue();
      if (a != null || b != null) {
        return new Object[]{a, b};
      }
      else {
        return null;
      }
    }

    @Override
    public String[] getTexts() {
      String a = getFieldByClass(BetweenIntegerField.IntegerFromField.class).getDisplayText();
      String b = getFieldByClass(BetweenIntegerField.IntegerToField.class).getDisplayText();
      return new String[]{a, b};
    }
  }

  @Order(10)
  @FormData(defaultSubtypeSdkCommand = DefaultSubtypeSdkCommand.IGNORE)
  public class BetweenLongField extends AbstractSequenceBox implements IComposerValueField {
    @Order(10)
    public class LongFromField extends AbstractLongField {
    }

    @Order(20)
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
    public void setSelectionContext(IDataModelAttribute attribute, int dataType, IDataModelAttributeOp op, Object[] values) {
      try {
        getFieldByClass(BetweenLongField.LongFromField.class).setValue(null);
        getFieldByClass(BetweenLongField.LongToField.class).setValue(null);
        if (values != null && values.length == 2) {
          if (values[0] instanceof Long) {
            getFieldByClass(BetweenLongField.LongFromField.class).setValue((Long) values[0]);
          }
          if (values[1] instanceof Long) {
            getFieldByClass(BetweenLongField.LongToField.class).setValue((Long) values[1]);
          }
        }
      }
      catch (Exception e) {
        // nop
      }
    }

    @Override
    public void clearSelectionContext() {
      getFieldByClass(BetweenLongField.LongFromField.class).setValue(null);
      getFieldByClass(BetweenLongField.LongToField.class).setValue(null);
    }

    @Override
    public Object[] getValues() {
      Object a = getFieldByClass(BetweenLongField.LongFromField.class).getValue();
      Object b = getFieldByClass(BetweenLongField.LongToField.class).getValue();
      if (a != null || b != null) {
        return new Object[]{a, b};
      }
      else {
        return null;
      }
    }

    @Override
    public String[] getTexts() {
      String a = getFieldByClass(BetweenLongField.LongFromField.class).getDisplayText();
      String b = getFieldByClass(BetweenLongField.LongToField.class).getDisplayText();
      return new String[]{a, b};
    }
  }

  @Order(10)
  @FormData(defaultSubtypeSdkCommand = DefaultSubtypeSdkCommand.IGNORE)
  public class BetweenDoubleField extends AbstractSequenceBox implements IComposerValueField {
    @Order(10)
    public class DoubleFromField extends AbstractDoubleField {
    }

    @Order(20)
    public class DoubleToField extends AbstractDoubleField {
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
    public void setSelectionContext(IDataModelAttribute attribute, int dataType, IDataModelAttributeOp op, Object[] values) {
      try {
        getFieldByClass(BetweenDoubleField.DoubleFromField.class).setValue(null);
        getFieldByClass(BetweenDoubleField.DoubleToField.class).setValue(null);
        if (values != null && values.length == 2) {
          if (values[0] instanceof Double) {
            getFieldByClass(BetweenDoubleField.DoubleFromField.class).setValue((Double) values[0]);
          }
          if (values[1] instanceof Double) {
            getFieldByClass(BetweenDoubleField.DoubleToField.class).setValue((Double) values[1]);
          }
        }
      }
      catch (Exception e) {
        // nop
      }
    }

    @Override
    public void clearSelectionContext() {
      getFieldByClass(BetweenDoubleField.DoubleFromField.class).setValue(null);
      getFieldByClass(BetweenDoubleField.DoubleToField.class).setValue(null);
    }

    @Override
    public Object[] getValues() {
      Object a = getFieldByClass(BetweenDoubleField.DoubleFromField.class).getValue();
      Object b = getFieldByClass(BetweenDoubleField.DoubleToField.class).getValue();
      if (a != null || b != null) {
        return new Object[]{a, b};
      }
      else {
        return null;
      }
    }

    @Override
    public String[] getTexts() {
      String a = getFieldByClass(BetweenDoubleField.DoubleFromField.class).getDisplayText();
      String b = getFieldByClass(BetweenDoubleField.DoubleToField.class).getDisplayText();
      return new String[]{a, b};
    }
  }

}
