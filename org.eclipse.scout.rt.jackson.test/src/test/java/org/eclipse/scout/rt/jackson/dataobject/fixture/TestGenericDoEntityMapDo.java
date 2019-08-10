/*
 * Copyright (c) 2010-2018 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.rt.jackson.dataobject.fixture;

import java.util.Map;

import javax.annotation.Generated;

import org.eclipse.scout.rt.dataobject.DoEntity;
import org.eclipse.scout.rt.dataobject.DoValue;
import org.eclipse.scout.rt.dataobject.IDoEntity;
import org.eclipse.scout.rt.dataobject.TypeName;

@TypeName("TestGenericDoEntityMap")
public class TestGenericDoEntityMapDo<T extends Map<String, ? extends IDoEntity>> extends DoEntity {

  public DoValue<T> genericMapAttribute() {
    return doValue("genericMapAttribute");
  }

  /* **************************************************************************
   * GENERATED CONVENIENCE METHODS
   * *************************************************************************/

  @Generated("DoConvenienceMethodsGenerator")
  public TestGenericDoEntityMapDo<T> withGenericMapAttribute(T genericMapAttribute) {
    genericMapAttribute().set(genericMapAttribute);
    return this;
  }

  @Generated("DoConvenienceMethodsGenerator")
  public T getGenericMapAttribute() {
    return genericMapAttribute().get();
  }
}
