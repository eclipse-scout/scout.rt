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
package org.eclipse.scout.rt.shared.validate;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Collection;

/**
 * Does input/output validation of arbitrary data.
 */
public interface IValidator {

  /**
   * validate the object and its complete substructure tree
   */
  void validateParameter(Object parameter, Collection<Annotation> parameterAnnotations) throws Exception;

  /**
   * validate the objects and the complete substructure tree based on the parameter annotations of the method (and its
   * super methods)
   */
  void validateMethodCall(Method m, Object[] parameters) throws Exception;
}
