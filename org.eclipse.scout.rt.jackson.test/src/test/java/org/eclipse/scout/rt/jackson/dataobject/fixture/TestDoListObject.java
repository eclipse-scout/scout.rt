/*
 * Copyright (c) 2010-2023 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
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
import org.eclipse.scout.rt.dataobject.TypeName;

@TypeName("TestDoList")
public class TestDoListObject extends DoEntity {

  public DoList<Object> objectList() {
    return doList("objectList");
  }

  /* **************************************************************************
   * GENERATED CONVENIENCE METHODS
   * *************************************************************************/

  @Generated("DoConvenienceMethodsGenerator")
  public TestDoListObject withObjectList(Collection<? extends Object> objectList) {
    objectList().updateAll(objectList);
    return this;
  }

  @Generated("DoConvenienceMethodsGenerator")
  public TestDoListObject withObjectList(Object... objectList) {
    objectList().updateAll(objectList);
    return this;
  }

  @Generated("DoConvenienceMethodsGenerator")
  public List<Object> getObjectList() {
    return objectList().get();
  }
}
