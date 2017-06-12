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
package org.eclipse.scout.rt.platform.inventory;

import java.lang.annotation.ElementType;
import java.lang.annotation.Target;
import java.util.Set;

public interface IClassInventory {

  /**
   * Returns all known subclasses that are assignable to the given queryClass, including interfaces. Use
   * {@link IClassInfo#isInstanciable()} to check if it is an instantiable bean.
   *
   * @param queryClassInfo
   *          is a {@link IClassInfo} that is part of this {@link IClassInventory}
   * @return all known subclasses, including interfaces and private types.
   */
  Set<IClassInfo> getAllKnownSubClasses(IClassInfo queryClassInfo);

  /**
   * Returns all known subclasses that are assignable to the given queryClass, including interfaces. Use
   * {@link IClassInfo#isInstanciable()} to check if it is an instantiable bean.
   *
   * @param clazz
   * @return all known subclasses, including interfaces and private types.
   */
  Set<IClassInfo> getAllKnownSubClasses(Class<?> queryClazz);

  /**
   * {@link Target} {@link ElementType#TYPE} {@link ElementType#ANNOTATION_TYPE}
   *
   * @param annotationInfo
   *          is a {@link IClassInfo} that is part of this {@link IClassInventory}
   * @return all registered types annotated with the annotation
   */
  Set<IClassInfo> getKnownAnnotatedTypes(IClassInfo annotationInfo);

  /**
   * {@link Target} {@link ElementType#TYPE} {@link ElementType#ANNOTATION_TYPE}
   *
   * @param annotation
   * @return all registered types annotated with the annotation
   */
  Set<IClassInfo> getKnownAnnotatedTypes(Class<?> annotation);
}
