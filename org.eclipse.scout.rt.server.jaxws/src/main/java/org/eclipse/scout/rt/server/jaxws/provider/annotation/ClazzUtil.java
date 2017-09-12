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
package org.eclipse.scout.rt.server.jaxws.provider.annotation;

import org.eclipse.scout.rt.platform.exception.PlatformException;
import org.eclipse.scout.rt.server.jaxws.provider.annotation.Clazz.NullClazz;

/**
 * Provides utility methods to resolve classes.
 *
 * @since 5.1
 */
public final class ClazzUtil {

  private ClazzUtil() {
  }

  /**
   * Resolves the concrete class as describes by the given <code>@Clazz</code> annotation.
   *
   * @param annotation
   *          meta-data for the class to be resolved.
   * @param expectedType
   *          the class type expected.
   * @param source
   *          used in case the resolution fails to give some more details about the origin.
   * @throws PlatformException
   *           is thrown if the class could not be resolved.
   */
  public static <T> Class<T> resolve(final Clazz annotation, final Class<T> expectedType, final String source) {
    final Class<?> clazz = annotation.value();
    final String qualifiedName = annotation.qualifiedName();

    if (qualifiedName.isEmpty() && NullClazz.class.equals(clazz)) {
      throw new PlatformException("No class specified for {}: missing 'value' or 'qualified name' attribute", source);
    }

    try {
      if (!qualifiedName.isEmpty()) {
        return assertClass(expectedType, Class.forName(qualifiedName), source);
      }
      else {
        return assertClass(expectedType, clazz, source);
      }
    }
    catch (final ReflectiveOperationException e) {
      throw new PlatformException("Failed to load class for {} [class={}, qualifiedName={}]", source, clazz, qualifiedName, e);
    }
  }

  @SuppressWarnings("unchecked")
  private static <T> Class<T> assertClass(final Class<T> expectedSuperClass, final Class<?> actualClass, final String source) {
    if (expectedSuperClass.isAssignableFrom(actualClass)) {
      return (Class<T>) actualClass;
    }
    else {
      throw new PlatformException("Class of wrong type for {} [expected={}, actual={}]", source, expectedSuperClass.getName(), actualClass.getName());
    }
  }
}
