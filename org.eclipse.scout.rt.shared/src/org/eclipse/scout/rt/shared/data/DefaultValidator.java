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
package org.eclipse.scout.rt.shared.data;

import java.io.IOException;

import org.eclipse.scout.rt.shared.data.form.AbstractFormData;
import org.eclipse.scout.rt.shared.data.form.DefaultFormDataValidator;
import org.eclipse.scout.rt.shared.data.form.ValidationStrategy;
import org.eclipse.scout.rt.shared.util.ValidationUtility;

/**
 * Does input/output validation of arbitrary serializable data.
 * <p>
 * This default traverses all objects of the arguments map in the complete data structure by writing the object to a
 * void stream.
 * <p>
 * This default delegates {@link AbstractFormData} to a {@link DefaultFormDataValidator} and does nothing otherwise.
 * <p>
 * Default maxLength is checked in {@link #checkMaxLenghtDefault(Object)}
 */
public class DefaultValidator implements IValidator {
  private int m_validationStrategy;
  private int m_defaultMaxLengthForString = 250;
  private int m_defaultMaxLengthForClob = 64000000;
  private int m_defaultMaxLengthForBlob = 64000000;
  private int m_defaultMaxLengthForArray = 10000;

  public DefaultValidator(int validationStrategy) {
    m_validationStrategy = validationStrategy;
  }

  @Override
  public int getValidationStrategy() {
    return m_validationStrategy;
  }

  @Override
  public void setValidationStrategy(int strategy) {
    m_validationStrategy = strategy;
  }

  @Override
  public int getDefaultMaxLengthForString() {
    return m_defaultMaxLengthForString;
  }

  @Override
  public void setDefaultMaxLengthForString(int defaultMaxLengthForString) {
    m_defaultMaxLengthForString = defaultMaxLengthForString;
  }

  @Override
  public int getDefaultMaxLengthForClob() {
    return m_defaultMaxLengthForClob;
  }

  @Override
  public void setDefaultMaxLengthForClob(int defaultMaxLengthForClob) {
    m_defaultMaxLengthForClob = defaultMaxLengthForClob;
  }

  @Override
  public int getDefaultMaxLengthForBlob() {
    return m_defaultMaxLengthForBlob;
  }

  @Override
  public void setDefaultMaxLengthForBlob(int defaultMaxLengthForBlob) {
    m_defaultMaxLengthForBlob = defaultMaxLengthForBlob;
  }

  @Override
  public int getDefaultMaxLengthForArray() {
    return m_defaultMaxLengthForArray;
  }

  @Override
  public void setDefaultMaxLengthForArray(int defaultMaxLengthForArray) {
    m_defaultMaxLengthForArray = defaultMaxLengthForArray;
  }

  @Override
  public void validate(Object obj) throws Exception {
    if (getValidationStrategy() == ValidationStrategy.NO_CHECK) {
      return;
    }
    try {
      if (obj == null) {
        visitObject(obj);
        return;
      }
      ValidationUtility.ObjectTreeVisitor v = new ValidationUtility.ObjectTreeVisitor() {
        @Override
        protected boolean visitObject(Object o) throws Exception {
          return visitObject(o);
        }
      };
      v.writeObject(obj);
    }
    catch (IOException ioe) {
      throw (Exception) ioe.getCause();
    }
  }

  /**
   * validate an object in the arguments object hierarchy tree
   * <p>
   * The default delegates form data objects to {@link DefaultFormDataValidator}
   * <p>
   * Other objects are checked by {@link #checkDefaults(Object)}
   * 
   * @return true to continue visiting the subtree of this object or false to skip visiting the suptree of this
   *         object.
   */
  protected boolean visitObject(Object obj) throws Exception {
    if (obj == null) {
      return false;
    }
    if (obj instanceof AbstractFormData) {
      new DefaultFormDataValidator(this, getValidationStrategy()).validate((AbstractFormData) obj);
      //form data may do deeper checks by its own decision and by calling this.validate() again
      return false;
    }
    checkMaxLenghtDefault(obj);
    return true;
  }

  private static final String STRING_NAME = "String";
  private static final String CLOB_NAME = "Clob";
  private static final String BLOB_NAME = "Blob";
  private static final String ARRAY_NAME = "Array";

  /**
   * The default does a max length check on string (250 chars) blob/clob(64MB) and array (10'000)
   */
  @Override
  public void checkMaxLenghtDefault(Object obj) throws Exception {
    if (obj == null) {
      return;
    }
    //maxLength check
    if (obj instanceof String) {
      ValidationUtility.checkMaxLength(STRING_NAME, obj, getDefaultMaxLengthForString());
      return;
    }
    Class<?> c = obj.getClass();
    if (c.isArray()) {
      if (c == char[].class) {
        ValidationUtility.checkMaxLength(CLOB_NAME, obj, getDefaultMaxLengthForClob());
      }
      else if (c == byte[].class) {
        ValidationUtility.checkMaxLength(BLOB_NAME, obj, getDefaultMaxLengthForBlob());
      }
      else {
        ValidationUtility.checkMaxLength(ARRAY_NAME, obj, getDefaultMaxLengthForArray());
      }
    }
  }
}
