/*******************************************************************************
 * Copyright (c) 2012 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.extension.client;

/**
 * Classes implementing this interface are wrapping another object which can be retrieved by invoking
 * {@link #getWrappedObject()}.
 * 
 * @since 3.9.0
 */
public interface IWrappedObject<T> {

  /**
   * @return Returns the object wrapped by this object. Implementors must ensure that this method never returns
   *         <code>null</code>.
   */
  T getWrappedObject();
}
