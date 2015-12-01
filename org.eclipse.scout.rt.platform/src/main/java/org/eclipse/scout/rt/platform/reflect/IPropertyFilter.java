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
package org.eclipse.scout.rt.platform.reflect;

public interface IPropertyFilter {

  /**
   * If the given descriptor should be analyzed.
   * 
   * @param descriptor
   * @return if accepts the given descriptor
   * @see AbstractForm.updateForm(...)
   */
  boolean accept(FastPropertyDescriptor descriptor);

}
