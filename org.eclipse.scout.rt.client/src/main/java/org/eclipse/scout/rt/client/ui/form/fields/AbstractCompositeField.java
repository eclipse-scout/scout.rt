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
package org.eclipse.scout.rt.client.ui.form.fields;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.scout.rt.client.extension.ui.form.fields.ICompositeFieldExtension;
import org.eclipse.scout.rt.client.ui.form.AbstractForm;
import org.eclipse.scout.rt.client.ui.form.DefaultFormFieldInjection;
import org.eclipse.scout.rt.client.ui.form.FormFieldInjectionThreadLocal;
import org.eclipse.scout.rt.client.ui.form.IForm;
import org.eclipse.scout.rt.client.ui.form.IFormFieldVisitor;
import org.eclipse.scout.rt.client.ui.form.fields.groupbox.AbstractGroupBox;
import org.eclipse.scout.rt.client.ui.form.fields.sequencebox.AbstractSequenceBox;
import org.eclipse.scout.rt.client.ui.form.fields.splitbox.AbstractSplitBox;
import org.eclipse.scout.rt.client.ui.form.fields.tabbox.AbstractTabBox;
import org.eclipse.scout.rt.client.ui.form.fields.wrappedform.IWrappedFormField;
import org.eclipse.scout.rt.platform.Replace;
import org.eclipse.scout.rt.platform.classid.ClassId;
import org.eclipse.scout.rt.platform.extension.InjectFieldTo;
import org.eclipse.scout.rt.platform.reflect.ConfigurationUtility;
import org.eclipse.scout.rt.platform.util.CollectionUtility;
import org.eclipse.scout.rt.platform.util.CompareUtility;
import org.eclipse.scout.rt.platform.util.collection.OrderedCollection;

@ClassId("4a641cd4-801f-45d2-9f08-5798e20b03c4")
public abstract class AbstractCompositeField extends AbstractFormField implements ICompositeField {

  private List<IFormField> m_fields;
  private Map<Class<?>, Class<? extends IFormField>> m_formFieldReplacements;
  private Map<Class<? extends IFormField>, IFormField> m_movedFormFieldsByClass;

  public AbstractCompositeField() {
    this(true);
  }

  public AbstractCompositeField(boolean callInitializer) {
    super(callInitializer);
  }

  protected List<Class<IFormField>> getConfiguredFields() {
    Class[] dca = ConfigurationUtility.getDeclaredPublicClasses(getClass());
    return ConfigurationUtility.filterClasses(dca, IFormField.class);
  }

  /**
   * This will only set the property of the composite field. If you want to set the property on every child field, you
   * have to use {@link #setStatusVisible(boolean)} in {@link #execInitField()}.
   */
  @Override
  protected boolean getConfiguredStatusVisible() {
    return super.getConfiguredStatusVisible();
  }

  /**
   * A CompositeField is visible when it has visible child fields (note that menus aren't taken into account).
   * <p>
   * Overwrite this method to do further logic or return true to display an empty field
   * </p>
   */
  @Override
  protected boolean execCalculateVisible() {
    return getVisibleFieldCount() > 0;
  }

  @Override
  protected void initConfig() {
    /*
     * call first super initConfig to ensure all properties are applied to the field only.
     * E.g. setEnabled(getConfiguredEnabled()) would enable/disable all children when called
     * after field creation. -> all fields would have the enabled state of the MainBox.
     */
    m_fields = CollectionUtility.emptyArrayList();
    m_movedFormFieldsByClass = new HashMap<Class<? extends IFormField>, IFormField>();
    super.initConfig();
    // prepare injected fields
    DefaultFormFieldInjection injectedFields = null;
    List<Class<IFormField>> declaredFields = getConfiguredFields();
    List<Class<? extends IFormField>> configuredFields = new ArrayList<Class<? extends IFormField>>(declaredFields.size());
    for (Class<? extends IFormField> clazz : declaredFields) {
      if (ConfigurationUtility.isInjectFieldAnnotationPresent(clazz)) {
        if (injectedFields == null) {
          injectedFields = new DefaultFormFieldInjection(this);
        }
        injectedFields.addField(clazz);
      }
      else {
        configuredFields.add(clazz);
      }
    }

    try {
      List<IFormField> contributedFields = m_contributionHolder.getContributionsByClass(IFormField.class);
      if (injectedFields != null) {
        FormFieldInjectionThreadLocal.push(injectedFields);
      }
      FormFieldInjectionThreadLocal.pushContainerField(this);
      filterFieldsInternal(configuredFields);

      // create instances
      OrderedCollection<IFormField> fields = new OrderedCollection<IFormField>();
      for (Class<? extends IFormField> clazz : configuredFields) {
        IFormField f = ConfigurationUtility.newInnerInstance(this, clazz);
        fields.addOrdered(f);
      }

      // handle contributions
      fields.addAllOrdered(contributedFields);

      injectFieldsInternal(fields);

      // connect
      for (IFormField f : fields) {
        f.setParentFieldInternal(this);
      }

      m_fields = fields.getOrderedList();

      // attach a proxy controller to each child field in the group for: visible, saveNeeded
      for (IFormField f : m_fields) {
        f.addPropertyChangeListener(new P_FieldPropertyChangeListener());
      }
    }
    finally {
      if (injectedFields != null) {
        m_formFieldReplacements = injectedFields.getReplacementMapping();
        FormFieldInjectionThreadLocal.pop(injectedFields);
      }
      FormFieldInjectionThreadLocal.popContainerField(this);
    }
    handleFieldVisibilityChanged();
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

  /**
   * Filter list of configured fields before they are instantiated.
   * <p/>
   * The default implementation removes fields replaced by another field annotated with {@link Replace}.
   *
   * @param fieldList
   *          live and mutable list of configured field classes (i.e. yet not instantiated)
   * @since 3.8.2
   */
  protected void filterFieldsInternal(List<Class<? extends IFormField>> fieldList) {
    FormFieldInjectionThreadLocal.filterFields(this, fieldList);
  }

  /**
   * Override this internal method only in order to make use of dynamic fields<br>
   * To change the order or specify the insert position use {@link IFormField#setOrder(double)}. The default
   * implementation checks for {@link InjectFieldTo} annotations in the enclosing (runtime) classes.
   *
   * @param fields
   *          live and mutable collection of configured fields, not yet initialized and added to composite field
   */
  protected void injectFieldsInternal(OrderedCollection<IFormField> fields) {
    FormFieldInjectionThreadLocal.injectFields(this, fields);
  }

  @Override
  public void setFormInternal(IForm form) {
    super.setFormInternal(form);
    if (form instanceof AbstractForm && this == form.getRootGroupBox()) {
      // this is the root group box. Publish replacement map to form and keep local map for better performance (see getReplacingFieldClass)
      ((AbstractForm) form).registerFormFieldReplacementsInternal(m_formFieldReplacements);
    }
    for (IFormField field : m_fields) {
      field.setFormInternal(form);
    }
  }

  @Override
  public void setParentFieldInternal(ICompositeField parentField) {
    super.setParentFieldInternal(parentField);
    if (!(parentField instanceof AbstractCompositeField)) {
      return;
    }

    // check if this is a template field box
    if (isTemplateField()) {
      // do not publish replacement map for template field boxes
      return;
    }

    // publish replacement map to parent AbstractCompositeField and keep local map for better performance (see getReplacingFieldClass)
    ((AbstractCompositeField) parentField).registerFormFieldReplacements(m_formFieldReplacements);
  }

  /**
   * Returns <code>true</code> if this field is a template group box, i.e. an abstract box class containing other
   * {@link IFormField}s.
   * <p/>
   * This default implementation checks the path of super classes, starting by the most specific one and stopping by
   * this class or one of its well known direct sub classes (i.e {@link AbstractGroupBox}, {@link AbstractSequenceBox},
   * {@link AbstractSnapBox}, {@link AbstractSplitBox} and {@link AbstractTabBox}). If there exists an abstract class
   * containing {@link IFormField}, this method returns <code>true</code>. Subclasses may override this default
   * behavior.
   *
   * @since 4.0.1
   */
  public boolean isTemplateField() {
    Class<?> c = getClass();
    while (c.getSuperclass() != null) {
      c = c.getSuperclass();

      // non-abstract classes are not considered as template
      if (!Modifier.isAbstract(c.getModifiers())) {
        continue;
      }

      // quick check for well known Scout classes
      if (CompareUtility.isOneOf(c,
          AbstractCompositeField.class,
          AbstractGroupBox.class,
          AbstractSequenceBox.class,
          AbstractSplitBox.class,
          AbstractTabBox.class)) {
        return false;
      }

      // class is only a template if it contains fields
      for (Class<?> innerClass : c.getDeclaredClasses()) {
        int m = innerClass.getModifiers();
        if (Modifier.isPublic(m) && !Modifier.isAbstract(m) && IFormField.class.isAssignableFrom(innerClass)) {
          return true;
        }
      }
    }

    return false;
  }

  @Override
  public int getFieldIndex(IFormField f) {
    return m_fields.indexOf(f);
  }

  @Override
  public int getFieldCount() {
    return m_fields.size();
  }

  @Override
  public IFormField getFieldById(String id) {
    return CompositeFieldUtility.getFieldById(this, id);
  }

  @Override
  public <T extends IFormField> T getFieldById(String id, Class<T> type) {
    return CompositeFieldUtility.getFieldById(this, id, type);
  }

  @Override
  public <T extends IFormField> T getFieldByClass(Class<T> c) {
    return CompositeFieldUtility.getFieldByClass(this, getReplacingFieldClass(c));
  }

  /**
   * Registers the given form field replacements on this composite field.
   *
   * @param replacements
   *          Map having old field classes as key and replacing field classes as values.
   * @since 4.0.1
   */
  private void registerFormFieldReplacements(Map<Class<?>, Class<? extends IFormField>> replacements) {
    if (replacements == null || replacements.isEmpty()) {
      return;
    }
    if (m_formFieldReplacements == null) {
      m_formFieldReplacements = new HashMap<Class<?>, Class<? extends IFormField>>();
    }
    m_formFieldReplacements.putAll(replacements);
  }

  /**
   * Checks whether the form field with the given class has been replaced by another form field. If so, the replacing
   * form field's class is returned. Otherwise the given class itself.
   *
   * @param c
   * @return Returns the possibly available replacing field class for the given class.
   * @see Replace
   * @since 3.8.2
   */
  private <T extends IFormField> Class<? extends T> getReplacingFieldClass(Class<T> c) {
    // 1. check local replacements
    if (m_formFieldReplacements != null) {
      @SuppressWarnings("unchecked")
      Class<? extends T> replacementFieldClass = (Class<? extends T>) m_formFieldReplacements.get(c);
      if (replacementFieldClass != null) {
        return replacementFieldClass;
      }
    }
    // 2. check global replacements
    IForm form = getForm();
    if (form instanceof AbstractForm) {
      Map<Class<?>, Class<? extends IFormField>> mapping = ((AbstractForm) form).getFormFieldReplacementsInternal();
      if (mapping != null) {
        @SuppressWarnings("unchecked")
        Class<? extends T> replacementFieldClass = (Class<? extends T>) mapping.get(c);
        if (replacementFieldClass != null) {
          return replacementFieldClass;
        }
      }
    }
    // 3. check parent field replacements (used for templates only. It is less common and therefore checked after global replacements)
    ICompositeField parentField = getParentField();
    while (parentField != null) {
      if (parentField instanceof AbstractCompositeField) {
        Map<Class<?>, Class<? extends IFormField>> parentReplacements = ((AbstractCompositeField) parentField).m_formFieldReplacements;
        if (parentReplacements != null) {
          @SuppressWarnings("unchecked")
          Class<? extends T> replacementFieldClass = (Class<? extends T>) parentReplacements.get(c);
          if (replacementFieldClass != null) {
            return replacementFieldClass;
          }
        }
      }
      parentField = parentField.getParentField();
    }
    // 4. field is not replaced
    return c;
  }

  @Override
  public List<IFormField> getFields() {
    return CollectionUtility.arrayList(m_fields);
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
      else if (field instanceof IWrappedFormField) {
        if (!((IWrappedFormField) field).visitFields(visitor, startLevel + 1)) {
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

  @Override
  protected boolean execIsSaveNeeded() {
    for (IFormField f : m_fields) {
      if (f.isSaveNeeded()) {
        return true;
      }
    }
    return false;
  }

  @Override
  protected void execMarkSaved() {
    super.execMarkSaved();
    for (IFormField f : m_fields) {
      f.markSaved();
    }
  }

  @Override
  protected boolean execIsEmpty() {
    for (IFormField f : m_fields) {
      if (!f.isEmpty()) {
        return false;
      }
    }
    return true;
  }

  /**
   * broadcast this change to all children
   */
  @Override
  public void setMandatory(boolean b) {
    // recursively down all children
    for (IFormField f : m_fields) {
      f.setMandatory(b);
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

  /**
   * when granting of visible property changes, do not broadcast and set this property on all children that have no
   * permission set
   */
  @Override
  public void setVisibleGranted(boolean b) {
    super.setVisibleGranted(b);
  }

  /**
   * if initialized broadcast this change to all children.
   */
  @Override
  public void setEnabled(boolean b) {
    super.setEnabled(b);
    // recursively down all children only if initialized.
    if (isInitialized()) {
      for (IFormField f : m_fields) {
        f.setEnabled(b);
      }
    }
  }

  /**
   * Sets the property on the field and on every child.
   *
   * @see #getConfiguredStatusVisible()
   */
  @Override
  public void setStatusVisible(boolean statusVisible) {
    super.setStatusVisible(statusVisible);
    if (isInitialized()) {
      for (IFormField f : m_fields) {
        f.setStatusVisible(statusVisible);
      }
    }
  }

  // box is only visible when it has at least one visible item
  protected void handleFieldVisibilityChanged() {
    int visCount = 0;
    for (IFormField field : m_fields) {
      if (field.isVisible()) {
        visCount++;
      }
    }
    setVisibleFieldCount(visCount);
    calculateVisibleInternal();
  }

  @Override
  public void rebuildFieldGrid() {
  }

  protected void setVisibleFieldCount(int n) {
    propertySupport.setPropertyInt(PROP_VISIBLE_FIELD_COUNT, n);
  }

  protected int getVisibleFieldCount() {
    return propertySupport.getPropertyInt(PROP_VISIBLE_FIELD_COUNT);
  }

  /**
   * Implementation of PropertyChangeListener Proxy on all attached fields (not groups)
   */
  protected class P_FieldPropertyChangeListener implements PropertyChangeListener {
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
      // if a field is moved to another parent this listener unregisters itself
      else if (e.getPropertyName().equals(IFormField.PROP_PARENT_FIELD)) {
        IFormField field = (IFormField) e.getSource();
        if (!isDirectChildOfComposite(field)) {
          field.removePropertyChangeListener(this);
        }
      }
    }

    /**
     * @return <code>true</code>, if the field is a direct child inside this listener's composite field instance,
     *         <code>false</code> otherwise.
     */
    protected boolean isDirectChildOfComposite(IFormField field) {
      return field.getParentField() == AbstractCompositeField.this;
    }

  }

  protected static class LocalCompositeFieldExtension<OWNER extends AbstractCompositeField> extends LocalFormFieldExtension<OWNER> implements ICompositeFieldExtension<OWNER> {

    public LocalCompositeFieldExtension(OWNER owner) {
      super(owner);
    }
  }

  @Override
  protected ICompositeFieldExtension<? extends AbstractCompositeField> createLocalExtension() {
    return new LocalCompositeFieldExtension<AbstractCompositeField>(this);
  }
}
