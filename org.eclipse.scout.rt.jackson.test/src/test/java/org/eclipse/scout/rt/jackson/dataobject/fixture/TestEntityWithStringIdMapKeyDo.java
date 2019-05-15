/*******************************************************************************
 * Copyright (c) 2019 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.jackson.dataobject.fixture;

import java.util.Map;

import javax.annotation.Generated;

import org.eclipse.scout.rt.platform.dataobject.DoEntity;
import org.eclipse.scout.rt.platform.dataobject.DoValue;
import org.eclipse.scout.rt.platform.dataobject.TypeName;

@TypeName("scout.TestEntityWithStringIdMapKey")
public class TestEntityWithStringIdMapKeyDo extends DoEntity {

  public DoValue<Map<FixtureStringId, String>> map() {
    return doValue("map");
  }

  /* **************************************************************************
   * GENERATED CONVENIENCE METHODS
   * *************************************************************************/

  @Generated("DoConvenienceMethodsGenerator")
  public TestEntityWithStringIdMapKeyDo withMap(Map<FixtureStringId, String> map) {
    map().set(map);
    return this;
  }

  @Generated("DoConvenienceMethodsGenerator")
  public Map<FixtureStringId, String> getMap() {
    return map().get();
  }
}
