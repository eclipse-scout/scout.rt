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
package org.eclipse.scout.rt.platform.exception;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.UndeclaredThrowableException;
import java.util.concurrent.ExecutionException;

import org.eclipse.scout.rt.platform.ApplicationScoped;
import org.eclipse.scout.rt.platform.util.StringUtility;

/**
 * Used to translate an exception into a {@link RuntimeException}.
 * <p>
 * If an exception is of the type {@link RuntimeException}, it is not translated and returned as given. But, a checked
 * exception is translated into {@link ProcessingException}, with the given checked exception as its cause. Thereby,
 * wrapper exceptions like {@link UndeclaredThrowableException}, {@link InvocationTargetException} or
 * {@link ExecutionException} are unwrapped and their cause translated accordingly.
 * <p>
 * If an exception is of the type {@link Error}, it is not translated but re-throw instead. That is because an
 * {@link Error} indicates a serious problem due to a abnormal condition.
 */
@ApplicationScoped
public class RuntimeExceptionTranslator implements IThrowableTranslator<RuntimeException> {

  @Override
  public RuntimeException translate(final Throwable t) {
    if (t instanceof Error) {
      throw (Error) t;
    }
    else if (t instanceof UndeclaredThrowableException && t.getCause() != null) {
      return translate(t.getCause());
    }
    else if (t instanceof InvocationTargetException && t.getCause() != null) {
      return translate(t.getCause());
    }
    else if (t instanceof ExecutionException && t.getCause() != null) {
      return translate(t.getCause());
    }
    else if (t instanceof RuntimeException) {
      return (RuntimeException) t;
    }
    else {
      return new ProcessingException(StringUtility.nvl(t.getMessage(), t.getClass().getSimpleName()), t);
    }
  }
}
