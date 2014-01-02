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
package org.eclipse.scout.rt.spec.client.gen.filter;

import org.eclipse.scout.commons.annotations.Doc;

/**
 * A filter for classes annotated with {@link Doc#ignore()}
 */
public class IgnoreDocFilter<T> implements IDocFilter<T> {

  /**
   * Accepts objects where the annotation value of {@link Doc#ignore()} is not <code>true</code>
   * 
   * @param o
   *          the object to filter
   */
  @Override
  public boolean accept(T o) {
    Doc docAnnotation = o.getClass().getAnnotation(Doc.class);
    if (docAnnotation != null) {
      return !docAnnotation.ignore();
    }
    return true;
  }

}
