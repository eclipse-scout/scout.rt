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

/**
 * This translator simply returns the given {@link Throwable}, unless being a wrapper exceptions like
 * {@link UndeclaredThrowableException}, {@link InvocationTargetException} or {@link ExecutionException}. Those are
 * unwrapped and their cause translated accordingly.
 */
@ApplicationScoped
public class ThrowableTranslator implements IThrowableTranslator<Throwable> {

  /**
   * Returns the given {@link Throwable}, unless being a wrapper exceptions like {@link UndeclaredThrowableException},
   * {@link InvocationTargetException} or {@link ExecutionException}. Those are unwrapped and their cause translated
   * accordingly.
   */
  @Override
  public Throwable translate(final Throwable t) {
    if (t instanceof UndeclaredThrowableException && t.getCause() != null) {
      return translate(t.getCause());
    }
    else if (t instanceof InvocationTargetException && t.getCause() != null) {
      return translate(t.getCause());
    }
    else if (t instanceof ExecutionException && t.getCause() != null) {
      return translate(t.getCause());
    }

    return t;
  }
}
