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
package org.eclipse.scout.rt.platform.util;

/**
 * Adaptable objects allows to be converted into other objects without having to implement the particular interface.
 * This is kind of a dynamic extension on object level, and lets clients to query whether an object has a particular
 * extension.
 * <p>
 * For example,
 *
 * <pre>
 *     IAdaptable a = [some adaptable];
 *     IFoo x = (IFoo)a.getAdapter(IFoo.class);
 *     if (x != null)
 *         [do IFoo things with x]
 * </pre>
 *
 * @since 5.1
 */
public interface IAdaptable {

  /**
   * Asks the adaptable object to provide a representation of the given type.
   *
   * @param type
   *          the requested type.
   * @return the object requested, or <code>null</code> if not supported by the adaptable object.
   */
  <T> T getAdapter(Class<T> type);
}
