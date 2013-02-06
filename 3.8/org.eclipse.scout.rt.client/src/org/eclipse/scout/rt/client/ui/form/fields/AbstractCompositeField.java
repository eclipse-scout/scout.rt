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
package org.eclipse.scout.rt.client.ui.form.fields;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.eclipse.scout.commons.ConfigurationUtility;
import org.eclipse.scout.commons.annotations.InjectFieldTo;
import org.eclipse.scout.commons.annotations.Replace;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.commons.holders.Holder;
import org.eclipse.scout.rt.client.ui.form.AbstractForm;
import org.eclipse.scout.rt.client.ui.form.DefaultFormFieldInjection;
import org.eclipse.scout.rt.client.ui.form.FormFieldInjectionThreadLocal;
import org.eclipse.scout.rt.client.ui.form.IForm;
import org.eclipse.scout.rt.client.ui.form.IFormFieldVisitor;
import org.eclipse.scout.rt.client.ui.form.fields.groupbox.AbstractGroupBox;
import org.eclipse.scout.rt.client.ui.form.fields.wrappedform.IWrappedFormField;
import org.eclipse.scout.rt.shared.services.common.exceptionhandler.IExceptionHandlerService;
import org.eclipse.scout.service.SERVICES;

public abstract class AbstractCompositeField extends AbstractFormField implements ICompositeField {

  private IFormField[] m_fields;
  private Map<Class<?>, Class<? extends IFormField>> m_formFieldReplacements;

  public AbstractCompositeField() {
    this(true);
  }

  public AbstractCompositeField(boolean callInitializer) {
    super(callInitializer);
  }

  protected Class<? extends IFormField>[] getConfiguredFields() {
    Class[] dca = ConfigurationUtility.getDeclaredPublicClasses(getClass());
    return ConfigurationUtility.sortFilteredClassesByOrderAnnotation(dca, IFormField.class);
  }

  /**
   * Full override: disable
   */

  @Override
  protected boolean execCalculateVisible() {
    return true;
  }

  @Override
  protected void initConfig() {
    /*
     * call first super initConfig to ensure all properties are applied to the field only.
     * E.g. setEnabled(getConfiguredEnabled()) would enable/disable all children when called
     * after field creation. -> all fields would have the enabled state of the MainBox.
     */
    m_fields = new IFormField[0];
    super.initConfig();
    Class<? extends IFormField>[] fieldArray = getConfiguredFields();
    // prepare injected fields
    DefaultFormFieldInjection injectedFields = null;
    List<Class<? extends IFormField>> configuredFields = new ArrayList<Class<? extends IFormField>>();
    for (Class<? extends IFormField> clazz : fieldArray) {
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
    ArrayList<IFormField> fieldList = new ArrayList<IFormField>();
    try {
      if (injectedFields != null) {
        FormFieldInjectionThreadLocal.push(injectedFields);
      }
      //
      filterFieldsInternal(configuredFields);
      for (Class<? extends IFormField> clazz : configuredFields) {
        try {
          IFormField f = ConfigurationUtility.newInnerInstance(this, clazz);
          fieldList.add(f);
        }// end try
        catch (Throwable t) {
          SERVICES.getService(IExceptionHandlerService.class).handleException(new ProcessingException("field: " + clazz.getName(), t));
        }
      }
      injectFieldsInternal(fieldList);
    }
    finally {
      if (injectedFields != null) {
        m_formFieldReplacements = injectedFields.getReplacementMapping();
        FormFieldInjectionThreadLocal.pop(injectedFields);
      }
    }
    for (IFormField f : fieldList) {
      f.setParentFieldInternal(this);
    }
    m_fields = fieldList.toArray(new IFormField[fieldList.size()]);
    // attach a proxy controller to each child field in the group for: visible, saveNeeded
    for (IFormField f : m_fields) {
      f.addPropertyChangeListener(new P_FieldPropertyChangeListener());
    }
    handleFieldVisibilityChanged();
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
   * Used to manage field list and add/remove fields (see {@link AbstractGroupBox} with wizard buttons)
   * <p>
   * The default implementation checks for {@link InjectFieldTo} annotations in the enclosing (runtime) classes.
   * 
   * @param fieldList
   *          live and mutable list of configured fields, not yet initialized
   *          and added to composite field
   */
  protected void injectFieldsInternal(List<IFormField> fieldList) {
    FormFieldInjectionThreadLocal.injectFields(this, fieldList);
  }

  @Override
  public void setFormInternal(IForm form) {
    super.setFormInternal(form);
    if (form instanceof AbstractForm) {
      ((AbstractForm) form).registerFormFieldReplacementsInternal(m_formFieldReplacements);
    }
    IFormField[] a = m_fields;
    for (int i = 0; i < a.length; i++) {
      IFormField f = a[i];
      f.setFormInternal(form);
    }
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
  public int getFieldCount() {
    return m_fields.length;
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
  public <T extends IFormField> T getFieldById(final String id, final Class<T> type) {
    final Holder<T> found = new Holder<T>(type);
    IFormFieldVisitor v = new IFormFieldVisitor() {
      @Override
      @SuppressWarnings("unchecked")
      public boolean visitField(IFormField field, int level, int fieldIndex) {
        if (type.isAssignableFrom(field.getClass()) && field.getFieldId().equals(id)) {
          found.setValue((T) field);
        }
        return found.getValue() == null;
      }
    };
    visitFields(v, 0);
    return found.getValue();
  }

  @Override
  public <T extends IFormField> T getFieldByClass(Class<T> c) {
    final Class<? extends T> formFieldClass = getReplacingFieldClass(c);
    final Holder<T> found = new Holder<T>(c);
    IFormFieldVisitor v = new IFormFieldVisitor() {
      @Override
      @SuppressWarnings("unchecked")
      public boolean visitField(IFormField field, int level, int fieldIndex) {
        if (field.getClass() == formFieldClass) {
          found.setValue((T) field);
        }
        return found.getValue() == null;
      }
    };
    visitFields(v, 0);
    return found.getValue();
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
    return c;
  }

  @Override
  public IFormField[] getFields() {
    IFormField[] a = new IFormField[m_fields.length];
    System.arraycopy(m_fields, 0, a, 0, a.length);
    return a;
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
      else if (f[i] instanceof IWrappedFormField) {
        if (!((IWrappedFormField) f[i]).visitFields(visitor, startLevel + 1)) {
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

  @Override
  protected boolean execIsSaveNeeded() throws ProcessingException {
    for (IFormField f : m_fields) {
      if (f.isSaveNeeded()) {
        return true;
      }
    }
    return false;
  }

  @Override
  protected void execMarkSaved() throws ProcessingException {
    super.execMarkSaved();
    for (IFormField f : m_fields) {
      f.markSaved();
    }
  }

  @Override
  protected boolean execIsEmpty() throws ProcessingException {
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
   * do not broadcast this change to all children
   */
  @Override
  public void setVisible(boolean b) {
    super.setVisible(b);
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

  /**
   * when granting of visible property changes, do not broadcast and set this
   * property on all children that have no permission set
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

  // box is only visible when it has at least one visible item
  protected void handleFieldVisibilityChanged() {
    int visCount = 0;
    IFormField[] f = m_fields;
    for (int i = 0; i < f.length; i++) {
      if (f[i].isVisible()) {
        visCount++;
      }
    }
    setVisibleFieldCount(visCount);
    calculateVisibleInternal();
  }

  @Override
  public void rebuildFieldGrid() {
  }

  private void setVisibleFieldCount(int n) {
    propertySupport.setPropertyInt(PROP_VISIBLE_FIELD_COUNT, n);
  }

  protected int getVisibleFieldCount() {
    return propertySupport.getPropertyInt(PROP_VISIBLE_FIELD_COUNT);
  }

  /**
   * Implementation of PropertyChangeListener Proxy on all attached fields (not
   * groups)
   */
  private class P_FieldPropertyChangeListener implements PropertyChangeListener {
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
}
