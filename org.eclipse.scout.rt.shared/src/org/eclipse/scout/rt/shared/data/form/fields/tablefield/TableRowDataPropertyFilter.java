package org.eclipse.scout.rt.shared.data.form.fields.tablefield;

import org.eclipse.scout.commons.BeanUtility;
import org.eclipse.scout.commons.beans.FastPropertyDescriptor;
import org.eclipse.scout.commons.beans.IPropertyFilter;

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