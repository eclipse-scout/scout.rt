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
package org.eclipse.scout.rt.client.ui.form;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.scout.rt.client.ui.form.fields.AbstractFormField;
import org.eclipse.scout.rt.client.ui.form.fields.ICompositeField;
import org.eclipse.scout.rt.client.ui.form.fields.IFormField;
import org.eclipse.scout.rt.platform.Replace;
import org.eclipse.scout.rt.platform.extension.InjectFieldTo;
import org.eclipse.scout.rt.platform.util.collection.OrderedCollection;

/**
 * Thread local used to inject form fields using the {@link InjectFieldTo} and {@link Replace} annotations.
 * <p>
 * This thread local is used by {@link AbstractForm} and {@link AbstractFormField} to put its contributions.
 * <p>
 * The caller is responsible to call #push and #pop as in a stack manner using try...finally.
 *
 * @since 3.8.1
 */
public final class FormFieldInjectionThreadLocal {
  private static final ThreadLocal<FormFieldInjectionThreadLocal> THREAD_LOCAL = new ThreadLocal<FormFieldInjectionThreadLocal>() {
    @Override
    protected FormFieldInjectionThreadLocal initialValue() {
      return new FormFieldInjectionThreadLocal();
    }
  };

  /**
   * @param injection
   *          is an injection contributer fields to a container
   */
  public static void push(IFormFieldInjection injection) {
    THREAD_LOCAL.get().pushInternal(injection);
  }

  /**
   * @param injection
   *          is an injection contributer fields to a container
   */
  public static void pop(IFormFieldInjection injection) {
    THREAD_LOCAL.get().popInternal(injection);
    if (THREAD_LOCAL.get().isEmpty()) {
      THREAD_LOCAL.remove();
    }
  }

  /**
   * Keeps track of the current parent field path. All {@link ICompositeField}s supporting form field injection must
   * invoke this method before child fields are created. Further, the caller is responsible for invoking
   * {@link #popContainerField(ICompositeField)}. Use the following code snippet.
   * <p/>
   * <b>Code Snippet</b>:
   *
   * <pre>
   * try {
   *   FormFieldInjectionThreadLocal.pushContainerField(this);
   *   // create child fields
   * }
   * finally {
   *   FormFieldInjectionThreadLocal.popContainerField(this);
   * }
   * </pre>
   *
   * @param container
   * @since 4.1
   */
  public static void pushContainerField(ICompositeField container) {
    THREAD_LOCAL.get().pushContainerFieldInternal(container);
  }

  /**
   * @param container
   * @see #pushContainerField(ICompositeField)
   * @since 4.1
   */
  public static void popContainerField(ICompositeField container) {
    THREAD_LOCAL.get().popContainerFieldInternal(container);
    if (THREAD_LOCAL.get().isEmpty()) {
      THREAD_LOCAL.remove();
    }
  }

  /**
   * @return Returns the list of parent fields of the current form field injection. The list is not modifiable and never
   *         <code>null</code>.
   * @see #pushContainerField(ICompositeField)
   * @since 4.1
   */
  public static List<ICompositeField> getContainerFields() {
    return THREAD_LOCAL.get().getContainerFieldsInternal();
  }

  /**
   * @param container
   *          is the container field the given field classes are created for
   * @param fieldList
   *          live and mutable list of configured field classes (i.e. yet not instantiated)
   * @since 3.8.2
   */
  public static void filterFields(IFormField container, List<Class<? extends IFormField>> fieldList) {
    THREAD_LOCAL.get().filterFieldsInternal(container, fieldList);
  }

  /**
   * @param container
   *          is the container field that is being added potential injected fields
   * @param fields
   *          live and mutable collection of currently (configured) fields, not yet initialized or added to the
   *          container field
   */
  public static void injectFields(IFormField container, OrderedCollection<IFormField> fields) {
    THREAD_LOCAL.get().injectFieldsInternal(container, fields);
  }

  private final ArrayList<IFormFieldInjection> m_stack = new ArrayList<IFormFieldInjection>();

  private final List<ICompositeField> m_containerFields = new ArrayList<ICompositeField>();

  private FormFieldInjectionThreadLocal() {
  }

  private void pushInternal(IFormFieldInjection injection) {
    if (injection == null) {
      throw new IllegalArgumentException("injection is null");
    }
    m_stack.add(injection);
  }

  private void popInternal(IFormFieldInjection injection) {
    if (injection == null) {
      throw new IllegalArgumentException("injection is null");
    }
    if (m_stack.isEmpty()) {
      throw new IllegalArgumentException("push/pop asymmetry; expected nothing but got " + injection.getClass());
    }
    if (m_stack.isEmpty() || m_stack.get(m_stack.size() - 1) != injection) {
      throw new IllegalArgumentException("push/pop asymmetry; expected " + m_stack.get(m_stack.size() - 1).getClass() + " but got " + injection.getClass());
    }
    m_stack.remove(m_stack.size() - 1);
  }

  private void injectFieldsInternal(IFormField container, OrderedCollection<IFormField> fields) {
    if (m_stack.isEmpty()) {
      return;
    }
    for (IFormFieldInjection i : m_stack) {
      i.injectFields(container, fields);
    }
  }

  private void filterFieldsInternal(IFormField container, List<Class<? extends IFormField>> fieldList) {
    if (m_stack.isEmpty()) {
      return;
    }
    for (IFormFieldInjection i : m_stack) {
      i.filterFields(container, fieldList);
    }
  }

  private void pushContainerFieldInternal(ICompositeField container) {
    if (container == null) {
      throw new IllegalArgumentException("container is null");
    }
    m_containerFields.add(container);
  }

  private void popContainerFieldInternal(ICompositeField container) {
    if (container == null) {
      throw new IllegalArgumentException("container is null");
    }
    if (m_containerFields.isEmpty()) {
      throw new IllegalArgumentException("push/pop asymmetry; expected nothing but got " + container.getClass());
    }
    if (m_containerFields.isEmpty() || m_containerFields.get(m_containerFields.size() - 1) != container) {
      throw new IllegalArgumentException("push/pop asymmetry; expected " + m_containerFields.get(m_containerFields.size() - 1).getClass() + " but got " + container.getClass());
    }
    m_containerFields.remove(m_containerFields.size() - 1);
  }

  private List<ICompositeField> getContainerFieldsInternal() {
    return Collections.unmodifiableList(m_containerFields);
  }

  /**
   * @return if both stack and container field list are empty
   */
  private boolean isEmpty() {
    return (m_stack.isEmpty() && m_containerFields.isEmpty());
  }
}
