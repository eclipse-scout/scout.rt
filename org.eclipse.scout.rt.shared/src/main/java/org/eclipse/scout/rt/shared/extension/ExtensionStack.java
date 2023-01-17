/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.shared.extension;

import java.util.ArrayList;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;

public class ExtensionStack {

  private final Deque<List<? extends IExtension<?>>> m_extensions;

  public ExtensionStack() {
    this(null);
  }

  public ExtensionStack(List<List<? extends IExtension<?>>> extensions) {
    m_extensions = new LinkedList<>();
    if (extensions != null) {
      m_extensions.addAll(extensions);
    }
  }

  public void pushExtensions(List<? extends IExtension<?>> extensions) {
    if (extensions == null || extensions.isEmpty()) {
      throw new IllegalArgumentException("extensions is null or empty");
    }
    m_extensions.push(extensions);
  }

  public void popExtensions(List<? extends IExtension<?>> extensions) {
    if (extensions == null) {
      throw new IllegalArgumentException("extensions is null");
    }
    if (m_extensions.isEmpty()) {
      throw new IllegalArgumentException("push/pop asymmetry; expected nothing but got " + extensions);
    }
    List<? extends IExtension<?>> topOfStack = m_extensions.peek();
    if (m_extensions.isEmpty() || topOfStack != extensions) {
      throw new IllegalArgumentException("push/pop asymmetry; expected " + topOfStack.getClass() + " but got " + extensions);
    }
    m_extensions.pop();
  }

  public boolean isEmpty() {
    return m_extensions.isEmpty();
  }

  /**
   * @param declaringClass
   * @return
   */
  public Object findContextObjectByClass(Class<?> declaringClass) {
    for (List<? extends IExtension<?>> segment : m_extensions) {
      for (IExtension<?> modelExtension : segment) {
        if (declaringClass.isInstance(modelExtension)) {
          return modelExtension;
        }
      }
    }
    return null;
  }

  public List<List<? extends IExtension<?>>> snapshot() {
    return new ArrayList<>(m_extensions);
  }
}
