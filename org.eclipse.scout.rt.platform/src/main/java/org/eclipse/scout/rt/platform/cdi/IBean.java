/*******************************************************************************
 * Copyright (c) 2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.platform.cdi;

import java.lang.annotation.Annotation;
import java.util.Map;

/**
 *
 */
public interface IBean<T> {

  /**
   * @return
   */
  T get();

  /**
   * To access all annotations of the bean.
   */
  Map<Class<? extends Annotation>, Annotation> getBeanAnnotations();

  <ANNOTATION extends Annotation> ANNOTATION getBeanAnnotation(Class<ANNOTATION> annotation);

  /**
   * @return
   */
  Class<? extends T> getBeanClazz();

  boolean isIntercepted();

}
