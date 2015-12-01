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

import org.eclipse.scout.rt.platform.annotations.Internal;

/**
 * This is the registration for one bean in the {@link IBeanManager}.
 *
 * @since 5.1
 */
public interface IBean<T> {

  /**
   * Default order of a bean that is neither annotated with {@link Order} nor with {@link Replace}.
   */
  double DEFAULT_BEAN_ORDER = 5000;

  /**
   * Gets a {@link Map} holding all {@link Annotation}s of this {@link IBean}.
   *
   * @return A {@link Map} with the {@link Annotation} class as key and the {@link Annotation} instance as value.
   */
  Map<Class<? extends Annotation>, Annotation> getBeanAnnotations();

  /**
   * Gets the {@link Annotation} instance for the given {@link Annotation} {@link Class}.
   *
   * @param annotation
   *          The {@link Annotation} {@link Class} to search.
   * @return The {@link Annotation} instance if this annotation exists for this {@link IBean} or <code>null</code>
   *         otherwise.
   */
  <ANNOTATION extends Annotation> ANNOTATION getBeanAnnotation(Class<ANNOTATION> annotation);

  /**
   * @return The {@link Class} of this {@link IBean}.
   */
  Class<? extends T> getBeanClazz();

  /**
   * Do not call this method directly, use {@link BEANS#get(Class)} instead!
   * <p>
   *
   * @return the initial instance of the bean, undecorated, not intercepted, may be null
   *         <p>
   *         used in {@link IBeanDecorationFactory}
   */
  @Internal
  T getInitialInstance();

  /**
   * Gets and creates if necessary the instance for this {@link IBean}.<br>
   * The returned instance is created using the {@link IBeanInstanceProducer} and has been decorated using the
   * {@link IBeanDecorationFactory}.
   *
   * @return The instance of the bean
   */
  T getInstance();

  /**
   * Gets the {@link IBeanInstanceProducer} associated with this {@link IBean}.
   *
   * @return The {@link IBeanInstanceProducer} of this {@link IBean}.
   */
  IBeanInstanceProducer<T> getBeanInstanceProducer();

}
