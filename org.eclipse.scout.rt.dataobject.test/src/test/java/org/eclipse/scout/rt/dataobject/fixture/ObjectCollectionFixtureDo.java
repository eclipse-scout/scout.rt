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
package org.eclipse.scout.rt.dataobject.fixture;

import java.util.Collection;
import java.util.Set;

import javax.annotation.Generated;

import org.eclipse.scout.rt.dataobject.DoEntity;
import org.eclipse.scout.rt.dataobject.DoSet;
import org.eclipse.scout.rt.dataobject.TypeName;

@TypeName("scout.ObjectCollectionFixture")
public class ObjectCollectionFixtureDo extends DoEntity {

  public DoSet<Object> objectSet() {
    return doSet("objectSet");
  }

  /* **************************************************************************
   * GENERATED CONVENIENCE METHODS
   * *************************************************************************/

  @Generated("DoConvenienceMethodsGenerator")
  public ObjectCollectionFixtureDo withObjectSet(Collection<? extends Object> objectSet) {
    objectSet().updateAll(objectSet);
    return this;
  }

  @Generated("DoConvenienceMethodsGenerator")
  public ObjectCollectionFixtureDo withObjectSet(Object... objectSet) {
    objectSet().updateAll(objectSet);
    return this;
  }

  @Generated("DoConvenienceMethodsGenerator")
  public Set<Object> getObjectSet() {
    return objectSet().get();
  }
}
