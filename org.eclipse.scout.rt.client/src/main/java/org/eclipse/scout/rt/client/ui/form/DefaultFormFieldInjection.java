/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.client.ui.form;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.scout.rt.client.ui.form.fields.ICompositeField;
import org.eclipse.scout.rt.client.ui.form.fields.IFormField;
import org.eclipse.scout.rt.platform.Replace;
import org.eclipse.scout.rt.platform.extension.InjectFieldTo;
import org.eclipse.scout.rt.platform.reflect.ConfigurationUtility;
import org.eclipse.scout.rt.platform.util.BeanUtility;
import org.eclipse.scout.rt.platform.util.collection.OrderedCollection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Default implementation that inserts new fields and that replaces existing fields using the {@link Replace}
 * annotation.
 *
 * @since 3.8.2
 */
public class DefaultFormFieldInjection implements IFormFieldInjection {
  private static final Logger LOG = LoggerFactory.getLogger(DefaultFormFieldInjection.class);

  private final Map<IFormField, Set<Class<? extends IFormField>>> m_replacingFormFieldsByContainer;
  private final List<Class<? extends IFormField>> m_injectedFieldList;
  private final Object m_enclosingContext;

  private Set<Class<? extends IFormField>> m_injectingFields;
  private Set<Class<? extends IFormField>> m_replacingFields;
  private Map<Class<?>, Class<? extends IFormField>> m_replacementMapping;

  /**
   * Creates a new instance for the given enclosing context (i.e. an {@link IForm} or an {@link ICompositeField}).
   *
   * @param enclosingContext
   */
  public DefaultFormFieldInjection(Object enclosingContext) {
    m_replacingFormFieldsByContainer = new HashMap<>();
    m_injectedFieldList = new ArrayList<>();
    m_enclosingContext = enclosingContext;
  }

  public void addField(Class<? extends IFormField> fieldClass) {
    if (fieldClass == null) {
      throw new IllegalArgumentException("fieldClass must not be null");
    }
    Replace replace = fieldClass.getAnnotation(Replace.class);
    InjectFieldTo injectFieldTo = fieldClass.getAnnotation(InjectFieldTo.class);
    if (replace == null && injectFieldTo == null) {
      LOG.warn("Ignoring field [{}] since neither @{} nor @{} is declared.", fieldClass, InjectFieldTo.class.getSimpleName(), Replace.class.getSimpleName());
      return;
    }
    if (replace != null && injectFieldTo != null) {
      LOG.warn("@{} annotation is ignored since @{} is available as well on class [{}]. You should remove one of both annotations.", InjectFieldTo.class.getSimpleName(), Replace.class.getSimpleName(), fieldClass);
    }
    else if (injectFieldTo != null && !ICompositeField.class.isAssignableFrom(injectFieldTo.value())) {
      LOG.warn("Ignoring field [{}] since it is not injected into an {}, but @{}({})", fieldClass, ICompositeField.class.getSimpleName(), InjectFieldTo.class.getSimpleName(), injectFieldTo.value());
      return;
    }
    m_injectedFieldList.add(fieldClass);
    m_injectingFields = null;
    m_replacingFields = null;
  }

  public void addFields(List<Class<IFormField>> fieldClasses) {
    for (Class<IFormField> c : fieldClasses) {
      addField(c);
    }
  }

  /**
   * @return Returns the mapping of all replaced fields or <code>null</code>, if no field replacements were performed.
   */
  public Map<Class<?>, Class<? extends IFormField>> getReplacementMapping() {
    if (m_replacementMapping == null || m_replacementMapping.isEmpty()) {
      return null;
    }
    return m_replacementMapping;
  }

  private void ensurePrepared() {
    if (m_injectingFields == null || m_replacingFields == null) {
      prepare();
    }
  }

  private void prepare() {
    m_injectingFields = new HashSet<>();
    m_replacingFields = new HashSet<>();
    m_replacementMapping = new HashMap<>();

    // 1. separate injected fields by annotation
    for (Class<? extends IFormField> f : m_injectedFieldList) {
      if (f.isAnnotationPresent(Replace.class)) {
        m_replacingFields.add(f);
      }
      else if (f.isAnnotationPresent(InjectFieldTo.class)) {
        m_injectingFields.add(f);
      }
    }

    // 2. remove transitive replacements (i.e. compute replacing leaf classes)
    if (!m_replacingFields.isEmpty()) {
      m_replacementMapping = ConfigurationUtility.getReplacementMapping(new ArrayList<>(m_replacingFields));
      m_replacingFields = new HashSet<>(m_replacementMapping.values()); // values of all configured mappings correspond to the all leaf classes of replacements
    }

    // 3. remove injected fields that are replaced and treat those replacing fields as injected fields
    if (!m_injectingFields.isEmpty() && !m_replacingFields.isEmpty()) {
      Set<Class<? extends IFormField>> replacingInjectedFields = new HashSet<>();
      for (Class<? extends IFormField> replacingField : m_replacingFields) {
        for (Iterator<Class<? extends IFormField>> it = m_injectingFields.iterator(); it.hasNext();) {
          Class<? extends IFormField> injectedField = it.next();
          if (injectedField.isAssignableFrom(replacingField)) {
            it.remove();
            replacingInjectedFields.add(replacingField);
            // do not break since injectedField could extend another injected field
          }
        }
      }
      m_replacingFields.removeAll(replacingInjectedFields);
      m_injectingFields.addAll(replacingInjectedFields);
    }
  }

  @Override
  public void filterFields(IFormField container, List<Class<? extends IFormField>> fieldList) {
    if (container == null || fieldList == null || fieldList.isEmpty()) {
      return;
    }

    ensurePrepared();
    if (m_replacingFields.isEmpty()) {
      return;
    }

    Set<Class<? extends IFormField>> replacingFields = new HashSet<>();
    // remove all replaced classes
    for (Iterator<Class<? extends IFormField>> it = fieldList.iterator(); it.hasNext();) {
      Class<? extends IFormField> field = it.next();
      for (Class<? extends IFormField> replacingField : m_replacingFields) {
        if (field.isAssignableFrom(replacingField)) {
          replacingFields.add(replacingField);
          it.remove();
          break;
        }
      }
    }
    if (!replacingFields.isEmpty()) {
      m_replacingFormFieldsByContainer.put(container, replacingFields);
    }
  }

  @Override
  public void injectFields(IFormField container, OrderedCollection<IFormField> fields) {
    if (container == null || fields == null || m_injectedFieldList.isEmpty()) {
      return;
    }

    ensurePrepared();

    // 1. replace fields if required
    Set<Class<? extends IFormField>> replacingFields = m_replacingFormFieldsByContainer.remove(container);
    if (replacingFields != null) {
      for (Class<? extends IFormField> replacingField : replacingFields) {
        // create field
        createAndInsertField(container, fields, replacingField);
      }
    }

    // 2. insert injected fields
    for (Class<? extends IFormField> injectedField : m_injectingFields) {
      Class<?> tmpClass = injectedField;
      while (tmpClass.isAnnotationPresent(Replace.class)) {
        tmpClass = tmpClass.getSuperclass();
      }

      // insert field if 1) annotated inject-to class is this container OR 2) annotated inject-to class was a container which was replaced by this container
      Class<?> injectTo = tmpClass.getAnnotation(InjectFieldTo.class).value();
      if (injectTo == container.getClass() || m_replacementMapping.get(injectTo) == container.getClass()) {
        createAndInsertField(container, fields, injectedField);
      }
    }
  }

  /**
   * Adds the field f to the end of the list. If the field is already part of the given list, this method does nothing.
   */
  private void createAndInsertField(IFormField container, OrderedCollection<IFormField> fields, Class<? extends IFormField> fieldClass) {
    //check if list already contains the field
    for (IFormField f : fields) {
      if (f.getClass() == fieldClass) {
        return;
      }
    }

    IFormField f = newInnerInstance(container, fieldClass);
    fields.addOrdered(f);
  }

  /**
   * Creates a new form field instance within the given container.
   *
   * @param container
   * @param fieldClass
   */
  private <T> T newInnerInstance(IFormField container, Class<T> fieldClass) {
    try {
      // try to create the field using its container
      T t = BeanUtility.createInstance(fieldClass, m_enclosingContext, container);
      if (t != null) {
        return t;
      }
    }
    catch (Exception t) { // NOSONAR
    }
    return ConfigurationUtility.newInnerInstance(m_enclosingContext, fieldClass);
  }
}
