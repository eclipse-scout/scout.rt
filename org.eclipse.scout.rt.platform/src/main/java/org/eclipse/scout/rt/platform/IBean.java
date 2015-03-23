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
package org.eclipse.scout.rt.platform;

import java.lang.annotation.Annotation;
import java.util.Map;

import org.eclipse.scout.commons.annotations.Internal;

/**
 * This is the registration for one {@link IBean} in the {@link IBeanContext}
 */
public interface IBean<T> {

  /**
   * To access all annotations of the bean.
   */
  Map<Class<? extends Annotation>, Annotation> getBeanAnnotations();

  <ANNOTATION extends Annotation> ANNOTATION getBeanAnnotation(Class<ANNOTATION> annotation);

  /**
   * @return
   */
  Class<? extends T> getBeanClazz();

  /**
   * Do not call this method directly, use {@link OBJ#get(Class)} instead!
   * <p>
   *
   * @return the initial instance of the bean, undecorated, not intercepted, may be null
   *         <p>
   *         used in {@link IBeanInstanceFactory}
   */
  @Internal
  T getInitialInstance();

  /**
   * Do not call this method directly, use {@link OBJ#get(Class)} instead!
   * <p>
   *
   * @return the (cached) runtime instance of the bean, undecorated, not intercepted, may be null
   *         <p>
   *         used in {@link IBeanInstanceFactory}
   */
  @Internal
  T createInstance();

}
