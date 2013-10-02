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
package org.eclipse.scout.rt.client.ui.form.fields.sequencebox;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.HashSet;

import org.eclipse.scout.commons.CompareUtility;
import org.eclipse.scout.commons.OptimisticLock;
import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.commons.annotations.ConfigOperation;
import org.eclipse.scout.commons.annotations.ConfigProperty;
import org.eclipse.scout.commons.annotations.Order;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.commons.exception.VetoException;
import org.eclipse.scout.commons.holders.IHolder;
import org.eclipse.scout.rt.client.ui.form.fields.AbstractCompositeField;
import org.eclipse.scout.rt.client.ui.form.fields.IFormField;
import org.eclipse.scout.rt.client.ui.form.fields.IValueField;
import org.eclipse.scout.rt.client.ui.form.fields.booleanfield.IBooleanField;
import org.eclipse.scout.rt.client.ui.form.fields.button.IButton;
import org.eclipse.scout.rt.client.ui.form.fields.sequencebox.internal.SequenceBoxGrid;
import org.eclipse.scout.rt.shared.ScoutTexts;

public abstract class AbstractSequenceBox extends AbstractCompositeField implements ISequenceBox {

  private boolean m_autoCheckFromTo;
  private OptimisticLock m_labelCompositionLock;
  private SequenceBoxGrid m_grid;
  private String m_labelBase;
  private String m_labelSuffix;
  private boolean m_equalColumnWidths;

  public AbstractSequenceBox() {
    this(true);
  }

  public AbstractSequenceBox(boolean callInitializer) {
    super(callInitializer);
  }

  /*
   * Configuration
   */

  /**
   * @return true: A value change trigger ensures that all fields in the range
   *         box have consistent (ascending values) by calling {@link #execCheckFromTo(IValueField)}
   *         <p>
   *         Examples:
   * 
   *         <pre>
   * fromField.value &lt;= toField.value
   * minField.value &lt;= preferredField.value &lt;= maxField.value
   * </pre>
   */
  @ConfigProperty(ConfigProperty.BOOLEAN)
  @Order(200)
  protected boolean getConfiguredAutoCheckFromTo() {
    return true;
  }

  @ConfigProperty(ConfigProperty.BOOLEAN)
  @Order(210)
  protected boolean getConfiguredEqualColumnWidths() {
    return false;
  }

  @Override
  protected boolean getConfiguredGridUseUiHeight() {
    return true;
  }

  /**
   * Default implementation ensures that all fields in the range box have
   * ascending values.<br>
   * Only active when {@link #isAutoCheckFromTo()} resp. {@link #getConfiguredAutoCheckFromTo()} is set to true
   * 
   * @param valueFields
   *          all value fields in the range box that have the same {@link IHolder#getHolderType()} and are comparable
   * @param changedIndex
   *          the field that triggered the change
   */
  @ConfigOperation
  @Order(200)
  protected <T extends Comparable<T>> void execCheckFromTo(IValueField<T>[] valueFields, int changedIndex) throws ProcessingException {
    ArrayList<IValueField<T>> nonEmptyFields = new ArrayList<IValueField<T>>();
    HashSet<Class> beanTypes = new HashSet<Class>();
    int nonEmptyIndex = -1;
    for (int i = 0; i < valueFields.length; i++) {
      if (valueFields[i].getValue() != null) {
        beanTypes.add(valueFields[i].getValue().getClass());
        nonEmptyFields.add(valueFields[i]);
        if (i == changedIndex) {
          nonEmptyIndex = nonEmptyFields.size() - 1;
        }
      }
    }
    //check if there are exactly two field and all of them are comparable with same type
    if (nonEmptyFields.size() != 2 || beanTypes.size() != 1) {
      clearInvalidSequenceStatus(valueFields);
      return;
    }
    // check changed field against its non-empty neighbours
    if (nonEmptyIndex >= 0) {
      IValueField<T> v = nonEmptyFields.get(nonEmptyIndex);
      if (nonEmptyIndex - 1 >= 0) {
        IValueField<T> left = nonEmptyFields.get(nonEmptyIndex - 1);
        if (CompareUtility.compareTo(left.getValue(), v.getValue()) > 0) {
          InvalidSequenceStatus errorStatus = new InvalidSequenceStatus(ScoutTexts.get("XMustBeGreaterThanOrEqualY", v.getLabel(), left.getLabel()));
          if (!v.isLabelSuppressed()) {
            v.setErrorStatus(errorStatus);
          }
          else {
            //first field's label is suppressed and error status updated on own label
            setErrorStatus(errorStatus);
          }
          return;
        }
      }
      if (nonEmptyIndex + 1 < nonEmptyFields.size()) {
        IValueField<T> right = nonEmptyFields.get(nonEmptyIndex + 1);
        if (CompareUtility.compareTo(v.getValue(), right.getValue()) > 0) {
          InvalidSequenceStatus errorStatus = new InvalidSequenceStatus(ScoutTexts.get("XMustBeLessThanOrEqualY", v.getLabel(), right.getLabel()));
          if (!v.isLabelSuppressed()) {
            v.setErrorStatus(errorStatus);
          }
          else {
            //first field's label is suppressed and error status updated on own label
            setErrorStatus(errorStatus);
          }
          return;
        }
      }
    }
    clearInvalidSequenceStatus(valueFields);
  }

  /**
   * Validate all fields including own and remove error status when it is an
   * InvalidSequenceStatus
   * 
   * @param valueFields
   */
  private void clearInvalidSequenceStatus(IValueField[] valueFields) {
    for (IValueField v : valueFields) {
      if (v.getErrorStatus() instanceof InvalidSequenceStatus) {
        v.clearErrorStatus();
      }
    }
    //remove error status on own
    if (getErrorStatus() instanceof InvalidSequenceStatus) {
      clearErrorStatus();
    }
  }

  @Override
  protected void initConfig() {
    m_labelCompositionLock = new OptimisticLock();
    m_grid = new SequenceBoxGrid(this);
    super.initConfig();
    setAutoCheckFromTo(getConfiguredAutoCheckFromTo());
    setEqualColumnWidths(getConfiguredEqualColumnWidths());
    // when range box has visible label, suppress first field's label and append
    // to own label
    propertySupport.addPropertyChangeListener(
        new PropertyChangeListener() {
          @Override
          public void propertyChange(PropertyChangeEvent e) {
            if (e.getPropertyName().equals(IFormField.PROP_LABEL_VISIBLE) || e.getPropertyName().equals(IFormField.PROP_LABEL) || e.getPropertyName().equals(IFormField.PROP_VISIBLE)) {
              updateLabelComposition();
            }
          }
        }
        );
    // <bsh 2010-10-01>
    // If inner fields change their visibility dynamically, the label of the SequenceBox might change.
    for (IFormField field : getFields()) {
      field.addPropertyChangeListener(
          new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent e) {
              if (e.getPropertyName().equals(IFormField.PROP_LABEL_VISIBLE) || e.getPropertyName().equals(IFormField.PROP_LABEL) || e.getPropertyName().equals(IFormField.PROP_VISIBLE)) {
                updateLabelComposition();
              }
            }
          }
          );
    }
    updateLabelComposition();
    // attach change triggers
    Class<?> sharedType = null;
    ArrayList<IValueField> valueFieldList = new ArrayList<IValueField>();
    for (IFormField f : getFields()) {
      if (f instanceof IValueField) {
        IValueField v = (IValueField) f;
        Class<?> valueType = v.getHolderType();
        if (Comparable.class.isAssignableFrom(valueType)) {
          if (sharedType == null || valueType == sharedType) {
            sharedType = valueType;
            valueFieldList.add(v);
          }
        }
      }
    }
    if (valueFieldList.size() >= 2) {
      final IValueField[] valueFields = valueFieldList.toArray(new IValueField[valueFieldList.size()]);
      for (int i = 0; i < valueFields.length; i++) {
        final int index = i;
        valueFields[index].addPropertyChangeListener(
            IValueField.PROP_VALUE,
            new PropertyChangeListener() {
              @Override
              public void propertyChange(PropertyChangeEvent e) {
                if (getForm() != null && isAutoCheckFromTo()) {
                  checkFromTo(valueFields, index);
                }
              }
            }
            );
      }
    }
  }

  /*
   * Runtime
   */

  @Override
  public boolean isEqualColumnWidths() {
    return m_equalColumnWidths;
  }

  @Override
  public void setEqualColumnWidths(boolean b) {
    m_equalColumnWidths = b;
  }

  @Override
  public void rebuildFieldGrid() {
    m_grid.validate();
    if (isInitialized()) {
      if (getForm() != null) {
        getForm().structureChanged(this);
      }
    }
  }

  // box is only visible when it has at least one visible item
  @Override
  protected void handleFieldVisibilityChanged() {
    super.handleFieldVisibilityChanged();
    if (isInitialized()) {
      rebuildFieldGrid();
    }
  }

  @Override
  public final int getGridColumnCount() {
    return m_grid.getGridColumnCount();
  }

  @Override
  public final int getGridRowCount() {
    return m_grid.getGridRowCount();
  }

  @Override
  public boolean isAutoCheckFromTo() {
    return m_autoCheckFromTo;
  }

  @Override
  public void setAutoCheckFromTo(boolean b) {
    m_autoCheckFromTo = b;
  }

  @SuppressWarnings("unchecked")
  private void checkFromTo(IValueField[] valueFields, int changedIndex) {
    try {
      execCheckFromTo(valueFields, changedIndex);
    }
    catch (VetoException e) {
      valueFields[changedIndex].setErrorStatus(e.getStatus());
    }
    catch (ProcessingException e) {
      valueFields[changedIndex].setErrorStatus(e.getStatus());
    }
  }

  private void updateLabelComposition() {
    if (!isLabelVisible()) {
      return;
    }

    try {
      if (m_labelCompositionLock.acquire()) {
        m_labelSuffix = execCreateLabelSuffix();

        computeCompoundLabel();
      }
    }
    finally {
      m_labelCompositionLock.release();
    }
  }

  /**
   * <p>
   * Returns a string which will be appended to the actual label of the sequence box.
   * </p>
   * <p>
   * As default it returns the label of the first field for which the function
   * {@link #execIsLabelSuffixCandidate(IFormField)} returns true.
   * </p>
   */
  @ConfigOperation
  @Order(210)
  protected String execCreateLabelSuffix() {
    for (IFormField f : getFields()) {
      f.setLabelSuppressed(false);
    }

    for (IFormField formField : getFields()) {
      if (execIsLabelSuffixCandidate(formField)) {
        formField.setLabelSuppressed(true);
        return formField.getLabel();
      }
    }

    return null;
  }

  /**
   * <p>
   * Computes whether the given formField should be considered when creating the compound label in
   * {@link #execCreateLabelSuffix()}.
   * </p>
   */
  @ConfigOperation
  @Order(211)
  protected boolean execIsLabelSuffixCandidate(IFormField formField) {
    if (!formField.isVisible()) {
      return false;
    }
    if (!formField.isLabelVisible()) {
      return false;
    }
    if (formField.getLabelPosition() == IFormField.LABEL_POSITION_ON_FIELD) {
      return false;
    }
    if (formField.getLabelPosition() == IFormField.LABEL_POSITION_TOP) {
      return false;
    }
    if (formField instanceof IBooleanField) {
      // Checkbox fields have their label on the right side of the checkbox so it is not necessary to use it for the compound label
      return false;
    }
    if (formField instanceof IButton) {
      // Inline buttons may be contained in the sequence box. The label is usually placed directly on the button itself.
      return false;
    }
    return true;
  }

  @Override
  public void setLabel(String name) {
    m_labelBase = name;
    computeCompoundLabel();
  }

  private void computeCompoundLabel() {
    if (StringUtility.hasText(m_labelBase) && StringUtility.hasText(m_labelSuffix)) {
      super.setLabel(m_labelBase + " " + m_labelSuffix);
    }
    else {
      super.setLabel(StringUtility.emptyIfNull(m_labelBase) + StringUtility.emptyIfNull(m_labelSuffix));
    }
  }

  @Override
  public String getFullyQualifiedLabel(String separator) {
    StringBuffer b = new StringBuffer();
    IFormField p = getParentField();
    if (p != null) {
      String s = p.getFullyQualifiedLabel(separator);
      if (s != null) {
        b.append(s);
      }
    }
    String s = m_labelBase;
    if (s != null) {
      if (b.length() > 0) {
        b.append(separator);
      }
      b.append(s);
    }
    return b.toString();
  }
}
