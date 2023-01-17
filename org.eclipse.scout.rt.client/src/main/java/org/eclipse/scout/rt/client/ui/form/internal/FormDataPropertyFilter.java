/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.client.ui.form.internal;

import java.io.Serializable;

import org.eclipse.scout.rt.platform.reflect.FastPropertyDescriptor;
import org.eclipse.scout.rt.platform.reflect.IPropertyFilter;
import org.eclipse.scout.rt.shared.data.form.fields.AbstractFormFieldData;
import org.eclipse.scout.rt.shared.data.form.properties.AbstractPropertyData;

public class FormDataPropertyFilter implements IPropertyFilter {

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
    //do NOT filter out AbstractFormData values
    if (AbstractFormFieldData.class.isAssignableFrom(propertyType)) {
      return false;
    }
    if (AbstractPropertyData.class.isAssignableFrom(propertyType)) {
      return false;
    }
    if (descriptor.getName().startsWith("configured")) {
      return false;
    }
    if ((!propertyType.isPrimitive()) && (!propertyType.isInterface()) && !Serializable.class.isAssignableFrom(propertyType)) {
      return false;
    }
    return true;
  }
}
