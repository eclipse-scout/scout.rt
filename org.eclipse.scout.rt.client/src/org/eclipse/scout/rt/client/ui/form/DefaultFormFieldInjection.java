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
import org.eclipse.scout.commons.annotations.Order;
import org.eclipse.scout.rt.client.ui.form.fields.IFormField;

/**
 * Default implementation that inserts the fields at the right place based on their {@link Order} annotation.
 * 
 * @since 3.8.1
 */
public class DefaultFormFieldInjection implements IFormFieldInjection {
  private final ArrayList<IFormField> m_list = new ArrayList<IFormField>();

  public void addField(IFormField f) {
    m_list.add(f);
  }

  @Override
  public void injectFields(IFormField container, List<IFormField> fieldList) {
    if (container == null || fieldList == null || m_list.isEmpty()) {
      return;
    }
    Class<?> containerClazz = container.getClass();
    for (IFormField f : m_list) {
      InjectFieldTo ann = f.getClass().getAnnotation(InjectFieldTo.class);
      if (ann == null) {
        continue;
      }
      if (ann.value() != containerClazz) {
        continue;
      }
      insertField(fieldList, f);
    }
  }

  /**
   * add the field f to the list at the right place regarding the {@link Order} annotation
   */
  protected void insertField(List<IFormField> list, IFormField f) {
    //check if list already contains f
    if (list.contains(f)) {
      return;
    }
    Class<?> c = f.getClass();
    if (!c.isAnnotationPresent(Order.class)) {
      list.add(f);
      return;
    }
    double newOrder = c.getAnnotation(Order.class).value();
    for (int i = 0, n = list.size(); i < n; i++) {
      Class<?> existingClazz = list.get(i).getClass();
      if (existingClazz.isAnnotationPresent(Order.class)) {
        double existingOrder = existingClazz.getAnnotation(Order.class).value();
        if (newOrder < existingOrder) {
          list.add(i, f);
          return;
        }
      }
    }
    //default at end
    list.add(f);
  }
}
