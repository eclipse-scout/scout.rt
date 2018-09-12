/*******************************************************************************
 * Copyright (c) 2010-2017 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.platform.filter;

import java.util.function.Predicate;

import org.eclipse.scout.rt.platform.IgnoreBean;
import org.eclipse.scout.rt.platform.inventory.IClassInfo;

/**
 * Class filter for beans classes.
 */
public class BeanClassFilter implements Predicate<IClassInfo> {

  /**
   * Checks whether the given class can be registered as a bean
   *
   * @return Returns <code>true</code> if the class is instanciable and not ignored with {@link IgnoreBean},
   *         <code>false</code> otherwise.
   */
  @Override
  public boolean test(IClassInfo ci) {
    return ci.isInstanciable() && !ci.hasAnnotation(IgnoreBean.class);
  }

}
