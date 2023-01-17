/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.client.ui.form.fields.sequencebox;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.scout.rt.client.extension.ui.form.fields.IFormFieldExtension;
import org.eclipse.scout.rt.client.extension.ui.form.fields.sequencebox.ISequenceBoxExtension;
import org.eclipse.scout.rt.client.extension.ui.form.fields.sequencebox.SequenceBoxChains.SequenceBoxCheckFromToChain;
import org.eclipse.scout.rt.client.extension.ui.form.fields.sequencebox.SequenceBoxChains.SequenceBoxCreateLabelSuffixChain;
import org.eclipse.scout.rt.client.extension.ui.form.fields.sequencebox.SequenceBoxChains.SequenceBoxIsLabelSuffixCandidateChain;
import org.eclipse.scout.rt.client.ui.form.fields.AbstractCompositeField;
import org.eclipse.scout.rt.client.ui.form.fields.AbstractFormField;
import org.eclipse.scout.rt.client.ui.form.fields.IFormField;
import org.eclipse.scout.rt.client.ui.form.fields.IValueField;
import org.eclipse.scout.rt.client.ui.form.fields.LogicalGridLayoutConfig;
import org.eclipse.scout.rt.client.ui.form.fields.booleanfield.IBooleanField;
import org.eclipse.scout.rt.client.ui.form.fields.button.IButton;
import org.eclipse.scout.rt.client.ui.form.fields.sequencebox.internal.SequenceBoxGrid;
import org.eclipse.scout.rt.platform.Order;
import org.eclipse.scout.rt.platform.annotations.ConfigOperation;
import org.eclipse.scout.rt.platform.annotations.ConfigProperty;
import org.eclipse.scout.rt.platform.classid.ClassId;
import org.eclipse.scout.rt.platform.exception.ProcessingException;
import org.eclipse.scout.rt.platform.holders.IHolder;
import org.eclipse.scout.rt.platform.status.IStatus;
import org.eclipse.scout.rt.platform.text.TEXTS;
import org.eclipse.scout.rt.platform.util.ObjectUtility;
import org.eclipse.scout.rt.platform.util.StringUtility;
import org.eclipse.scout.rt.platform.util.concurrent.OptimisticLock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A {@link IFormField} that contains an ordered sequence of {@link IFormField}s.<br>
 * E.g. a range with start and end date.
 * <p>
 * The Default implementation ensures that all fields in the range box have ascending values. Overwrite
 * {@link #execCheckFromTo(IValueField[], int)} to change that behaviour or disable any checks with
 * {@link #getConfiguredAutoCheckFromTo()}.
 * </p>
 */
@ClassId("e71e8b93-1168-4f5e-8781-4774f01eee26")
public abstract class AbstractSequenceBox extends AbstractCompositeField implements ISequenceBox {
  private static final Logger LOG = LoggerFactory.getLogger(AbstractSequenceBox.class);
  private static final String LABEL_VISIBLE_SEQUENCE = "LABEL_VISIBLE_SEQUENCE";

  private boolean m_autoCheckFromTo;
  private OptimisticLock m_labelCompositionLock;
  private SequenceBoxGrid m_grid;
  private String m_labelBase;
  private String m_labelSuffix;

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
   * @return true: A value change trigger ensures that all fields in the range box have consistent (ascending values) by
   *         calling {@link #execCheckFromTo(IValueField[], int)}
   *         <p>
   *         Examples:
   *
   *         <pre>
   * fromField.value &lt;= toField.value
   * minField.value &lt;= preferredField.value &lt;= maxField.value
   *         </pre>
   */
  @ConfigProperty(ConfigProperty.BOOLEAN)
  @Order(200)
  protected boolean getConfiguredAutoCheckFromTo() {
    return true;
  }

  /**
   * Configures the layout hints.
   * <p>
   * The hints are set to -1 by default which means the values will be set by the UI.
   */
  @ConfigProperty(ConfigProperty.INTEGER)
  @Order(220)
  protected LogicalGridLayoutConfig getConfiguredLayoutConfig() {
    return new LogicalGridLayoutConfig();
  }

  /**
   * Default implementation ensures that all fields in the range box have ascending values.<br>
   * Only active when {@link #isAutoCheckFromTo()} resp. {@link #getConfiguredAutoCheckFromTo()} is set to true
   *
   * @param valueFields
   *          all value fields in the range box that have the same {@link IHolder#getHolderType()} and are comparable
   * @param changedIndex
   *          the field that triggered the change
   */
  @ConfigOperation
  @Order(200)
  protected <T extends Comparable<T>> void execCheckFromTo(IValueField<T>[] valueFields, int changedIndex) {
    List<IValueField<T>> nonEmptyFields = new ArrayList<>();
    int nonEmptyIndex = -1;
    for (int i = 0; i < valueFields.length; i++) {
      if (valueFields[i].getValue() != null) {
        nonEmptyFields.add(valueFields[i]);
        if (i == changedIndex) {
          nonEmptyIndex = nonEmptyFields.size() - 1;
        }
      }
    }

    //check if the fields are comparable with same type
    if (nonEmptyIndex < 0 || !equalTypes(nonEmptyFields)) {
      clearSequenceErrors(Arrays.asList(valueFields));
    }
    else {
      checkNonEmptyFromTo(nonEmptyFields, nonEmptyIndex);
    }
  }

  private <T extends Comparable<T>> void checkNonEmptyFromTo(List<IValueField<T>> nonEmptyFields, int nonEmptyIndex) {
    // check changed field against its non-empty neighbours
    IValueField<T> v = nonEmptyFields.get(nonEmptyIndex);

    //check greater left neighbor
    IValueField<T> left = (nonEmptyIndex - 1 >= 0) ? nonEmptyFields.get(nonEmptyIndex - 1) : null;
    IStatus leftError = checkFromTo(left, v, false);
    if (leftError != null) {
      v.addErrorStatus(leftError);
    }
    else {
      //check right neighbor greater
      IValueField<T> right = (nonEmptyIndex + 1 < nonEmptyFields.size()) ? nonEmptyFields.get(nonEmptyIndex + 1) : null;
      IStatus rightError = checkFromTo(v, right, true);
      if (rightError != null) {
        v.addErrorStatus(rightError);
      }
      else {
        clearSequenceErrors(nonEmptyFields);
      }
    }
  }

  private <T extends Comparable<T>> IStatus checkFromTo(IValueField<T> from, IValueField<T> to, boolean lessMessage) {
    if (from != null && to != null && ObjectUtility.compareTo(from.getValue(), to.getValue()) > 0) {
      if (lessMessage) {
        return new InvalidSequenceStatus(TEXTS.get("XMustBeLessThanOrEqualY", from.getLabel(), to.getLabel()));
      }
      else {
        return new InvalidSequenceStatus(TEXTS.get("XMustBeGreaterThanOrEqualY", to.getLabel(), from.getLabel()));
      }
    }
    return null;
  }

  private <T extends Comparable<T>> boolean equalTypes(List<IValueField<T>> nonEmptyFields) {
    Set<Class> beanTypes = new HashSet<>();
    for (IValueField<T> f : nonEmptyFields) {
      beanTypes.add(f.getValue().getClass());
    }
    return beanTypes.size() == 1;
  }

  /**
   * Validate all fields including own and remove error status when it is an InvalidSequenceStatus
   */
  private <T extends Comparable<T>> void clearSequenceErrors(List<IValueField<T>> valueFields) {
    for (IValueField<T> v : valueFields) {
      v.removeErrorStatus(InvalidSequenceStatus.class);
    }
    removeErrorStatus(InvalidSequenceStatus.class);
  }

  @Override
  protected void initConfig() {
    m_labelCompositionLock = new OptimisticLock();
    setFieldGrid(new SequenceBoxGrid());
    super.initConfig();
    setAutoCheckFromTo(getConfiguredAutoCheckFromTo());
    setLayoutConfig(getConfiguredLayoutConfig());
    // when range box has visible label, suppress first field's label and append
    // to own label
    propertySupport.addPropertyChangeListener(
        e -> {
          if (e.getPropertyName().equals(IFormField.PROP_LABEL_VISIBLE) || e.getPropertyName().equals(IFormField.PROP_LABEL) || e.getPropertyName().equals(IFormField.PROP_VISIBLE)) {
            updateLabelComposition();
          }
        });
    // <bsh 2010-10-01>
    // If inner fields change their visibility dynamically, the label of the SequenceBox might change.
    for (IFormField field : getFields()) {
      field.addPropertyChangeListener(
          e -> {
            if (e.getPropertyName().equals(IFormField.PROP_LABEL_VISIBLE) || e.getPropertyName().equals(IFormField.PROP_LABEL) || e.getPropertyName().equals(IFormField.PROP_VISIBLE)) {
              updateLabelComposition();
            }
          });
    }
    updateLabelComposition();
    hideFieldStatusOfChildren();
    // attach change triggers
  }

  @Override
  protected void initConfigInternal() {
    super.initConfigInternal();
    // add listener after init-config has been executed to ensure no events are fired during init-config.
    attachCheckFromToListeners();
  }

  /**
   * Attach a property change listener to all {@link IValueField}s with the same holder type than the first comparable
   * value type {@link IValueField}.
   */
  private void attachCheckFromToListeners() {
    //fields with equal types
    ArrayList<IValueField> valueFieldList = getComparableValueFields();
    if (valueFieldList.size() >= 2) {
      final IValueField[] valueFields = valueFieldList.toArray(new IValueField[0]);
      for (int i = 0; i < valueFields.length; i++) {
        final int index = i;
        valueFields[index].addPropertyChangeListener(
            IValueField.PROP_VALUE,
            e -> {
              if (getForm() != null && isAutoCheckFromTo()) {
                checkFromTo(valueFields, index);
              }
            });
      }
    }
  }

  /**
   * @return Comparable {@link IValueField}s with the same holder type than the first one
   */
  private ArrayList<IValueField> getComparableValueFields() {
    ArrayList<IValueField> valueFieldList = new ArrayList<>();
    Class<?> sharedType = null;
    for (IFormField f : getFields()) {
      if (f instanceof IValueField) {
        IValueField v = (IValueField) f;
        Class<?> valueType = v.getHolderType();
        if (Comparable.class.isAssignableFrom(valueType)
            && (sharedType == null || valueType == sharedType)) {
          sharedType = valueType;
          valueFieldList.add(v);
        }
      }
    }
    return valueFieldList;
  }

  /*
   * Runtime
   */

  @Override
  public LogicalGridLayoutConfig getLayoutConfig() {
    return (LogicalGridLayoutConfig) propertySupport.getProperty(PROP_LAYOUT_CONFIG);
  }

  @Override
  public void setLayoutConfig(LogicalGridLayoutConfig layoutConfig) {
    propertySupport.setProperty(PROP_LAYOUT_CONFIG, layoutConfig);
  }

  @Override
  public void rebuildFieldGrid() {
    m_grid.validate(this);
    if (isInitConfigDone() && getForm() != null) {
      getForm().structureChanged(this);
    }
  }

  protected void setFieldGrid(SequenceBoxGrid grid) {
    m_grid = grid;
  }

  @Override
  public SequenceBoxGrid getFieldGrid() {
    return m_grid;
  }

  @Override
  protected void handleChildFieldVisibilityChanged() {
    super.handleChildFieldVisibilityChanged();
    if (isInitConfigDone()) {
      // box is only visible when it has at least one visible item
      rebuildFieldGrid();
    }
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
      interceptCheckFromTo(valueFields, changedIndex);
    }
    catch (ProcessingException e) {
      LOG.debug("Sequence Error", e);
      valueFields[changedIndex].addErrorStatus(e.getStatus());
    }
  }

  /**
   * Sets the status to invisible of every field.
   */
  protected void hideFieldStatusOfChildren() {
    List<IFormField> fields = getFields();
    for (IFormField field : fields) {
      field.setStatusVisible(false);
    }
  }

  private void updateLabelComposition() {
    if (!isLabelVisible()) {
      return;
    }

    try {
      if (m_labelCompositionLock.acquire()) {
        m_labelSuffix = interceptCreateLabelSuffix();

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
   * {@link #interceptIsLabelSuffixCandidate(IFormField)} returns true.
   * </p>
   */
  @Order(210)
  @ConfigOperation
  protected String execCreateLabelSuffix() {
    List<IFormField> fields = getFields();
    for (IFormField f : fields) {
      f.setLabelVisible(true, LABEL_VISIBLE_SEQUENCE);
    }

    for (IFormField formField : fields) {
      if (interceptIsLabelSuffixCandidate(formField)) {
        formField.setLabelVisible(false, LABEL_VISIBLE_SEQUENCE);
        return formField.getLabel();
      }
    }

    return null;
  }

  /**
   * <p>
   * Computes whether the given formField should be considered when creating the compound label in
   * {@link #interceptCreateLabelSuffix()}.
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
    StringBuilder b = new StringBuilder();
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

  protected final boolean interceptIsLabelSuffixCandidate(IFormField formField) {
    List<? extends IFormFieldExtension<? extends AbstractFormField>> extensions = getAllExtensions();
    SequenceBoxIsLabelSuffixCandidateChain chain = new SequenceBoxIsLabelSuffixCandidateChain(extensions);
    return chain.execIsLabelSuffixCandidate(formField);
  }

  protected final <T extends Comparable<T>> void interceptCheckFromTo(IValueField<T>[] valueFields, int changedIndex) {
    List<? extends IFormFieldExtension<? extends AbstractFormField>> extensions = getAllExtensions();
    SequenceBoxCheckFromToChain chain = new SequenceBoxCheckFromToChain(extensions);
    chain.execCheckFromTo(valueFields, changedIndex);
  }

  protected final String interceptCreateLabelSuffix() {
    List<? extends IFormFieldExtension<? extends AbstractFormField>> extensions = getAllExtensions();
    SequenceBoxCreateLabelSuffixChain chain = new SequenceBoxCreateLabelSuffixChain(extensions);
    return chain.execCreateLabelSuffix();
  }

  protected static class LocalSequenceBoxExtension<OWNER extends AbstractSequenceBox> extends LocalCompositeFieldExtension<OWNER> implements ISequenceBoxExtension<OWNER> {

    public LocalSequenceBoxExtension(OWNER owner) {
      super(owner);
    }

    @Override
    public boolean execIsLabelSuffixCandidate(SequenceBoxIsLabelSuffixCandidateChain chain, IFormField formField) {
      return getOwner().execIsLabelSuffixCandidate(formField);
    }

    @Override
    public <T extends Comparable<T>> void execCheckFromTo(SequenceBoxCheckFromToChain chain, IValueField<T>[] valueFields, int changedIndex) {
      getOwner().execCheckFromTo(valueFields, changedIndex);
    }

    @Override
    public String execCreateLabelSuffix(SequenceBoxCreateLabelSuffixChain chain) {
      return getOwner().execCreateLabelSuffix();
    }
  }

  @Override
  protected ISequenceBoxExtension<? extends AbstractSequenceBox> createLocalExtension() {
    return new LocalSequenceBoxExtension<>(this);
  }
}
