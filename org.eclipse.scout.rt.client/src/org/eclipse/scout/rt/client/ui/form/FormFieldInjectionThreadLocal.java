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
package org.eclipse.scout.rt.client.ui.form;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.scout.commons.annotations.InjectFieldTo;
import org.eclipse.scout.rt.client.ui.form.fields.AbstractFormField;
import org.eclipse.scout.rt.client.ui.form.fields.IFormField;

/**
 * Thread local used to inject form fields using the {@link InjectFieldTo} annotation
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
  }

  /**
   * @param container
   *          is the container field that is being added potential injected fields
   * @param fieldList
   *          live and mutable list of currently (configured) fields, not yet initialized
   *          or added to the container field
   */
  public static void injectFields(IFormField container, List<IFormField> fieldList) {
    THREAD_LOCAL.get().injectFieldsInternal(container, fieldList);
  }

  private final ArrayList<IFormFieldInjection> m_stack = new ArrayList<IFormFieldInjection>();

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

  private void injectFieldsInternal(IFormField container, List<IFormField> fieldList) {
    if (m_stack.isEmpty()) {
      return;
    }
    for (IFormFieldInjection i : m_stack) {
      i.injectFields(container, fieldList);
    }
  }
}
