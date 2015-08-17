/*******************************************************************************
 * Copyright (c) 2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.shared.services.common.code;

import org.eclipse.scout.commons.filter.IFilter;
import org.eclipse.scout.rt.platform.Bean;
import org.eclipse.scout.rt.platform.IgnoreBean;
import org.eclipse.scout.rt.platform.inventory.IClassInfo;

/**
 *
 */
@Bean
public class CodeTypeClassFilter implements IFilter<IClassInfo> {

  /**
   * Checks whether the given class is a CodeType class that should be registered
   *
   * @param ci
   *          the class to be checked
   * @return Returns <code>true</code> if the class is an accepted code type class. <code>false</code> otherwise.
   */
  @Override
  public boolean accept(IClassInfo ci) {
    return ci.isInstanciable() && !ci.hasAnnotation(IgnoreBean.class);
  }

}
