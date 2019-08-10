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

import java.util.Collection;
import java.util.List;

import javax.annotation.Generated;

import org.eclipse.scout.rt.dataobject.DoEntity;
import org.eclipse.scout.rt.dataobject.DoList;
import org.eclipse.scout.rt.dataobject.DoValue;
import org.eclipse.scout.rt.dataobject.TypeName;

@TypeName("TestGeneric")
@SuppressWarnings("unchecked")
public class TestGenericDo<T> extends DoEntity {

  public DoValue<T> genericAttribute() {
    return doValue("genericAttribute");
  }

  public DoList<T> genericListAttribute() {
    return doList("genericListAttribute");
  }

  /* **************************************************************************
   * GENERATED CONVENIENCE METHODS
   * *************************************************************************/

  @Generated("DoConvenienceMethodsGenerator")
  public TestGenericDo<T> withGenericAttribute(T genericAttribute) {
    genericAttribute().set(genericAttribute);
    return this;
  }

  @Generated("DoConvenienceMethodsGenerator")
  public T getGenericAttribute() {
    return genericAttribute().get();
  }

  @Generated("DoConvenienceMethodsGenerator")
  public TestGenericDo<T> withGenericListAttribute(Collection<? extends T> genericListAttribute) {
    genericListAttribute().updateAll(genericListAttribute);
    return this;
  }

  @Generated("DoConvenienceMethodsGenerator")
  public TestGenericDo<T> withGenericListAttribute(T... genericListAttribute) {
    genericListAttribute().updateAll(genericListAttribute);
    return this;
  }

  @Generated("DoConvenienceMethodsGenerator")
  public List<T> getGenericListAttribute() {
    return genericListAttribute().get();
  }
}
