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

import org.eclipse.scout.commons.TypeCastUtility;

/**
 * @since 3.0
 */

public class LongArrayHolder extends Holder<Long[]> {
  private static final long serialVersionUID = 1L;

  public LongArrayHolder() {
    super(Long[].class);
  }

  public LongArrayHolder(Long[] value) {
    super(Long[].class, value);
  }

  public LongArrayHolder(Integer[] value) {
    super(Long[].class, (value != null ? TypeCastUtility.castValue(value, Long[].class) : null));
  }

}
