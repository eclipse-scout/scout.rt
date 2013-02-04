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
package org.eclipse.scout.commons.serialization;

/**
 * Factory for creating new {@link IObjectSerializer} instances.
 * 
 * @since 3.8.2
 */
public interface IObjectSerializerFactory {

  /**
   * Creates a new instance of an {@link IObjectSerializer} using the given {@link IObjectReplacer}. Implementing
   * classes may share expensive objects like class loaders.
   * 
   * @param objectReplacer
   *          an {@link IObjectReplacer} instance of <code>null</code>
   * @return
   */
  IObjectSerializer createObjectSerializer(IObjectReplacer objectReplacer);

  /**
   * @return Returns an environment-dependent ClassLoader that is able to load all classes that are available in the
   *         running environment. The class loader should also be used by {@link IObjectSerializer}s returned by
   *         {@link #createObjectSerializer(IObjectReplacer)}. At least it has to support the same set of classes.
   */
  ClassLoader getClassLoader();
}
