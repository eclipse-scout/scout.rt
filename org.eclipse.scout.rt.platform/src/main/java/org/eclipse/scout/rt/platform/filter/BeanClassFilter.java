/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
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
   * @return Returns <code>true</code> if the class is instantiable and not ignored with {@link IgnoreBean},
   *         <code>false</code> otherwise.
   */
  @Override
  public boolean test(IClassInfo ci) {
    return ci.isInstanciable() && !ci.hasAnnotation(IgnoreBean.class);
  }

}
