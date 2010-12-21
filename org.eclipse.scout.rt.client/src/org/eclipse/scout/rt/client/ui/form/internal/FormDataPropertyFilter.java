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
package org.eclipse.scout.rt.client.ui.form.internal;

import java.io.Serializable;

import org.eclipse.scout.commons.beans.FastPropertyDescriptor;
import org.eclipse.scout.commons.beans.IPropertyFilter;
import org.eclipse.scout.rt.shared.data.form.AbstractFormData;
import org.eclipse.scout.rt.shared.data.form.fields.AbstractFormFieldData;
import org.eclipse.scout.rt.shared.data.form.properties.AbstractPropertyData;

public class FormDataPropertyFilter implements IPropertyFilter {

  public FormDataPropertyFilter() {
  }

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
    if (AbstractFormData.class.isAssignableFrom(propertyType)) {
      return false;
    }
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
