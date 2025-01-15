/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.client.ui.form.fields;

import static org.eclipse.scout.rt.client.ui.form.fields.CompositeFieldUtility.connectFields;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.scout.rt.client.extension.ui.form.fields.ICompositeFieldExtension;
import org.eclipse.scout.rt.client.ui.IWidget;
import org.eclipse.scout.rt.client.ui.form.AbstractForm;
import org.eclipse.scout.rt.client.ui.form.DefaultFormFieldInjection;
import org.eclipse.scout.rt.client.ui.form.FormFieldInjectionThreadLocal;
import org.eclipse.scout.rt.client.ui.form.IForm;
import org.eclipse.scout.rt.client.ui.form.fields.groupbox.AbstractGroupBox;
import org.eclipse.scout.rt.client.ui.form.fields.sequencebox.AbstractSequenceBox;
import org.eclipse.scout.rt.client.ui.form.fields.splitbox.AbstractSplitBox;
import org.eclipse.scout.rt.client.ui.form.fields.tabbox.AbstractTabBox;
import org.eclipse.scout.rt.platform.Replace;
import org.eclipse.scout.rt.platform.classid.ClassId;
import org.eclipse.scout.rt.platform.extension.InjectFieldTo;
import org.eclipse.scout.rt.platform.holders.Holder;
import org.eclipse.scout.rt.platform.reflect.ConfigurationUtility;
import org.eclipse.scout.rt.platform.util.CollectionUtility;
import org.eclipse.scout.rt.platform.util.ObjectUtility;
import org.eclipse.scout.rt.platform.util.collection.OrderedCollection;
import org.eclipse.scout.rt.platform.util.visitor.TreeVisitResult;

@ClassId("4a641cd4-801f-45d2-9f08-5798e20b03c4")
public abstract class AbstractCompositeField extends AbstractFormField implements ICompositeField {

  private Map<Class<?>, Class<? extends IFormField>> m_formFieldReplacements;
  private Map<Class<? extends IFormField>, IFormField> m_movedFormFieldsByClass;
  private static final ThreadLocal<Boolean> REPLACEMENT_LOOKUP_DONE = ThreadLocal.withInitial(() -> false);

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
  @SuppressWarnings("squid:S1185")
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
    return hasVisibleFieldsInternal();
  }

  @Override
  protected void initConfig() {
    /*
     * call first super initConfig to ensure all properties are applied to the field only.
     * E.g. setEnabled(getConfiguredEnabled()) would enable/disable all children when called
     * after field creation. -> all fields would have the enabled state of the MainBox.
     */

    setFieldsInternal(CollectionUtility.emptyArrayList());
    m_movedFormFieldsByClass = new HashMap<>();
    super.initConfig();
    // prepare injected fields
    DefaultFormFieldInjection injectedFields = null;
    List<Class<IFormField>> declaredFields = getConfiguredFields();
    List<Class<? extends IFormField>> configuredFields = new ArrayList<>(declaredFields.size());
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
      OrderedCollection<IFormField> fields = new OrderedCollection<>();
      for (Class<? extends IFormField> clazz : configuredFields) {
        IFormField f = ConfigurationUtility.newInnerInstance(this, clazz);
        fields.addOrdered(f);
      }

      // handle contributions
      fields.addAllOrdered(contributedFields);

      injectFieldsInternal(fields);

      // connect
      for (IFormField f : fields) {
        connectFields(f, this);
      }

      setFieldsInternal(fields.getOrderedList());
    }
    finally {
      if (injectedFields != null) {
        m_formFieldReplacements = injectedFields.getReplacementMapping();
        FormFieldInjectionThreadLocal.pop(injectedFields);
      }
      FormFieldInjectionThreadLocal.popContainerField(this);
    }
  }

  @Override
  public void addField(IFormField f) {
    CompositeFieldUtility.addField(f, this, getFieldsInternal());
    addChildFieldPropertyChangeListener(f);
    handleFieldsChanged();
  }

  @Override
  public void removeField(IFormField f) {
    CompositeFieldUtility.removeField(f, this, getFieldsInternal());
    removeChildFieldPropertyChangeListener(f);
    handleFieldsChanged();
  }

  @Override
  public void moveFieldTo(IFormField f, ICompositeField newContainer) {
    CompositeFieldUtility.moveFieldTo(f, this, newContainer);
    m_movedFormFieldsByClass.put(f.getClass(), f);
  }

  @Override
  public Map<Class<? extends IFormField>, IFormField> getMovedFields() {
    return Collections.unmodifiableMap(m_movedFormFieldsByClass);
  }

  /**
   * Updates this composite field's state after a child field has been added or removed.
   */
  protected void handleFieldsChanged() {
    handleChildFieldVisibilityChanged();
    checkSaveNeeded();
    checkEmpty();
    propertySupport.setPropertyAlwaysFire(PROP_FIELDS, getFieldsInternal());
  }

  /**
   * Filter list of configured fields before they are instantiated.
   * <p/>
   * The default implementation removes fields replaced by another field annotated with {@link Replace}.
   *
   * @param fieldList
   *     live and mutable list of configured field classes (i.e. yet not instantiated)
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
   *     live and mutable collection of configured fields, not yet initialized and added to composite field
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
  }

  @Override
  public boolean setParentInternal(IWidget parentField) {
    boolean result = super.setParentInternal(parentField);
    if (!(parentField instanceof AbstractCompositeField)) {
      return result;
    }

    // check if this is a template field box
    if (isTemplateField()) {
      // do not publish replacement map for template field boxes
      return result;
    }

    // publish replacement map to parent AbstractCompositeField and keep local map for better performance (see getReplacingFieldClass)
    ((AbstractCompositeField) parentField).registerFormFieldReplacements(m_formFieldReplacements);
    return result;
  }

  /**
   * Returns <code>true</code> if this field is a template group box, i.e. an abstract box class containing other
   * {@link IFormField}s.
   * <p/>
   * This default implementation checks the path of super classes, starting by the most specific one and stopping by
   * this class or one of its well known direct sub classes (i.e {@link AbstractGroupBox}, {@link AbstractSequenceBox},
   * {@link AbstractSplitBox} and {@link AbstractTabBox}). If there exists an abstract class containing
   * {@link IFormField}, this method returns <code>true</code>. Subclasses may override this default behavior.
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
      if (ObjectUtility.isOneOf(c,
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
    return getFieldsInternal().indexOf(f);
  }

  @Override
  public int getFieldCount() {
    return getFieldsInternal().size();
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
  public <T extends IWidget> TreeVisitResult getWidgetByClassInternal(Holder<T> result, Class<T> widgetClassToFind) {
    // Compared to the default implementation of AbstractWidget, this implementation allows the retrieval of a form field which extends another form field in the same group box.
    // With the default implementation the retrieval would depend on the order of the form field in the group box.
    // Since this is a very rare use case and something we would actually like to drop, it is only supported for form fields to support backward compatibility.
    Class<T> classToFind = widgetClassToFind;
    boolean replacementLookupDone = false;
    if (!REPLACEMENT_LOOKUP_DONE.get()) {
      // Do the replacement lookup only the first time a composite field is visited. The getReplacingFieldClass checks the parent fields anyway.
      classToFind = getReplacingFieldClass(widgetClassToFind);
      replacementLookupDone = true;
      REPLACEMENT_LOOKUP_DONE.set(true);
    }
    try {
      // Now use the the new classTofind to first check the composite field itself and then its children
      TreeVisitResult visitResult = super.getWidgetByClassInternal(result, classToFind);
      if (visitResult == TreeVisitResult.SKIP_SUBTREE || visitResult == TreeVisitResult.TERMINATE) {
        return visitResult;
      }
      for (IWidget child : getChildren()) {
        T widget = child.getWidgetByClass(classToFind);
        if (widget != null) {
          result.setValue(widget);
          return TreeVisitResult.TERMINATE;
        }
      }
    }
    finally {
      if (replacementLookupDone) {
        REPLACEMENT_LOOKUP_DONE.remove();
      }
    }
    return TreeVisitResult.SKIP_SUBTREE;
  }

  @Override
  public <T extends IFormField> T getFieldByClass(Class<T> fieldToFind) {
    return getWidgetByClass(fieldToFind);
  }

  /**
   * Registers the given form field replacements on this composite field.
   *
   * @param replacements
   *     Map having old field classes as key and replacing field classes as values.
   * @since 4.0.1
   */
  private void registerFormFieldReplacements(Map<Class<?>, Class<? extends IFormField>> replacements) {
    if (replacements == null || replacements.isEmpty()) {
      return;
    }
    if (m_formFieldReplacements == null) {
      m_formFieldReplacements = new HashMap<>();
    }
    m_formFieldReplacements.putAll(replacements);
  }

  /**
   * Checks whether the form field with the given class has been replaced by another form field. If so, the replacing
   * form field's class is returned. Otherwise the given class itself.
   *
   * @return Returns the possibly available replacing field class for the given class.
   * @see Replace
   * @since 3.8.2
   */
  private <T extends IWidget> Class<T> getReplacingFieldClass(Class<T> c) {
    // 1. check local replacements
    if (m_formFieldReplacements != null) {
      @SuppressWarnings("unchecked")
      Class<T> replacementFieldClass = (Class<T>) m_formFieldReplacements.get(c);
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
        Class<T> replacementFieldClass = (Class<T>) mapping.get(c);
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
          Class<T> replacementFieldClass = (Class<T>) parentReplacements.get(c);
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
  public void setFields(List<IFormField> fields) {
    setFieldsInternal(CollectionUtility.arrayList(fields));
  }

  protected void setFieldsInternal(List<IFormField> fields) {
    propertySupport.setPropertyList(PROP_FIELDS, fields);
  }

  @Override
  public List<IFormField> getFields() {
    return CollectionUtility.arrayList(getFieldsInternal());
  }

  protected List<IFormField> getFieldsInternal() {
    return propertySupport.getPropertyList(PROP_FIELDS);
  }

  @Override
  public List<? extends IWidget> getChildren() {
    return CollectionUtility.flatten(super.getChildren(), getFieldsInternal());
  }

  @Override
  protected void handleChildFieldVisibilityChanged() {
    super.handleChildFieldVisibilityChanged();

    // box is only visible when it has at least one visible item
    setHasVisibleFieldsInternal(calcHasVisibleFieldsInternal());
    calculateVisibleInternal();
  }

  protected boolean calcHasVisibleFieldsInternal() {
    if (CollectionUtility.isEmpty(getFieldsInternal())) {
      return false;
    }
    for (IFormField field : getFieldsInternal()) {
      if (field.isVisible()) {
        return true;
      }
    }
    return false;
  }

  @Override
  public void rebuildFieldGrid() {
  }

  protected void setHasVisibleFieldsInternal(boolean hasVisibleFields) {
    propertySupport.setPropertyBool(PROP_HAS_VISIBLE_FIELDS, hasVisibleFields);
  }

  protected boolean hasVisibleFieldsInternal() {
    return propertySupport.getPropertyBool(PROP_HAS_VISIBLE_FIELDS);
  }

  protected static class LocalCompositeFieldExtension<OWNER extends AbstractCompositeField> extends LocalFormFieldExtension<OWNER> implements ICompositeFieldExtension<OWNER> {

    public LocalCompositeFieldExtension(OWNER owner) {
      super(owner);
    }
  }

  @Override
  protected ICompositeFieldExtension<? extends AbstractCompositeField> createLocalExtension() {
    return new LocalCompositeFieldExtension<>(this);
  }
}
