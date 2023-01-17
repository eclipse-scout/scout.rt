/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.shared.data.form.fields.tablefield;

import org.eclipse.scout.rt.platform.reflect.FastPropertyDescriptor;
import org.eclipse.scout.rt.platform.reflect.IPropertyFilter;
import org.eclipse.scout.rt.platform.util.BeanUtility;

/**
 * Property filter class used by {@link BeanUtility} that accepts all column properties available on an
 * {@link AbstractTableFieldBeanData}.
 */
public class TableRowDataPropertyFilter implements IPropertyFilter {

  @Override
  public boolean accept(FastPropertyDescriptor descriptor) {
    Class<?> propertyType = descriptor.getPropertyType();
    if (propertyType == null) {
      return false;
    }
    if (descriptor.getReadMethod() == null) {
      return false;
    }
    if (descriptor.getWriteMethod() == null) {
      return false;
    }
    if ("rowState".equals(descriptor.getName())) {
      return false;
    }
    if ("customColumnValues".equals(descriptor.getName())) {
      return false;
    }
    return true;
  }
}
