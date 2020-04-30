/*
 * Copyright (c) 2010-2020 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.rt.dataobject.fixture;

import javax.annotation.Generated;

import org.eclipse.scout.rt.dataobject.DoValue;

public class ProjectSubFixtureDo extends ProjectFixtureDo {

  public DoValue<Integer> count2() {
    return doValue("count2");
  }

  /* **************************************************************************
   * GENERATED CONVENIENCE METHODS
   * *************************************************************************/

  @Generated("DoConvenienceMethodsGenerator")
  public ProjectSubFixtureDo withCount2(Integer count2) {
    count2().set(count2);
    return this;
  }

  @Generated("DoConvenienceMethodsGenerator")
  public Integer getCount2() {
    return count2().get();
  }
}
