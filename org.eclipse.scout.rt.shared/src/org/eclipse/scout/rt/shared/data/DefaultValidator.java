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
import java.io.ObjectOutputStream;
import java.io.OutputStream;

import org.eclipse.scout.rt.shared.data.form.AbstractFormData;
import org.eclipse.scout.rt.shared.data.form.DefaultFormDataValidator;
import org.eclipse.scout.rt.shared.data.form.ValidationStrategy;
import org.eclipse.scout.rt.shared.util.ValidationUtility;

/**
 * Does input/output validation of arbitrary serializable data.
 * <p>
 * This default traverses all objects of the arguments map in the complete data structure by writing the object to a
 * void stream and calling {@link #validateObject()} on every traversed Object in the hierarchy.
 * <p>
 * This default delegates {@link AbstractFormData} to a {@link DefaultFormDataValidator} and does nothing otherwise.
 */
public class DefaultValidator {
  private int m_validationStrategy;
  private final Object[] m_args;

  public DefaultValidator(int validationStrategy, Object[] args) {
    m_validationStrategy = validationStrategy;
    m_args = args;
  }

  public Object[] getArgs() {
    return m_args;
  }

  public int getValidationStrategy() {
    return m_validationStrategy;
  }

  public void setValidationStrategy(int strategy) {
    m_validationStrategy = strategy;
  }

  public void validate() throws Exception {
    if (getValidationStrategy() == ValidationStrategy.NO_CHECK) {
      return;
    }
    Object[] args = getArgs();
    if (args == null || args.length == 0) {
      return;
    }
    for (int i = 0; i < args.length; i++) {
      validateRootObject(i, args[i]);
    }
    new ObjectTreeVisitor() {
      @Override
      void visitObject(Object obj) throws Exception {
        validateSubTreeObject(obj);
      }
    }.writeObject(args);
  }

  /**
   * validate an argument of the service
   * <p>
   * The default does a max length check on string (250 chars) and array (50MB)
   */
  protected void validateRootObject(int index, Object value) throws Exception {
    //maxLength check
    if (value == null) {
      return;
    }
    if (value instanceof String) {
      ValidationUtility.checkMaxLength(null, value, 250);
    }
    else if (value.getClass().isArray()) {
      //50MB
      ValidationUtility.checkMaxLength(null, value, 50000000);
    }
  }

  /**
   * validate an object in the arguments object hierarchy tree
   * <p>
   * The default delegates form data objects to {@link DefaultFormDataValidator}
   */
  protected void validateSubTreeObject(Object obj) throws Exception {
    if (obj instanceof AbstractFormData) {
      new DefaultFormDataValidator(getValidationStrategy(), (AbstractFormData) obj).validate();
    }
  }

  public static abstract class ObjectTreeVisitor extends ObjectOutputStream {
    public ObjectTreeVisitor() throws IOException {
      super(new OutputStream() {
        @Override
        public void write(int b) throws IOException {
          //nop
        }
      });
      enableReplaceObject(true);
    }

    @Override
    protected Object replaceObject(Object obj) throws IOException {
      try {
        visitObject(obj);
      }
      catch (IOException ioe) {
        throw ioe;
      }
      catch (Exception e) {
        throw new IOException(e.getMessage());
      }
      return obj;
    }

    abstract void visitObject(Object obj) throws Exception;

  }

}
