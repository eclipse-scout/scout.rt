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
package org.eclipse.scout.commons.holders;

/**
 * @since 3.0
 */

public class ObjectArrayHolder extends Holder<Object[]> {
  private static final long serialVersionUID = 1L;

  public ObjectArrayHolder() {
    super(Object[].class);
  }

  public ObjectArrayHolder(Object[] value) {
    super(Object[].class, value);
  }

}
