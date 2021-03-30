/*
 * Copyright (c) 2010-2021 BSI Business Systems Integration AG.
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
import java.util.Optional;

import javax.annotation.Generated;

import org.eclipse.scout.rt.dataobject.DoEntity;
import org.eclipse.scout.rt.dataobject.DoList;
import org.eclipse.scout.rt.dataobject.DoValue;
import org.eclipse.scout.rt.dataobject.TypeName;

@TypeName("TestOptional")
public class TestOptionalDo extends DoEntity {

  public DoValue<Optional<String>> optString() {
    return doValue("optString");
  }

  public DoList<Optional<String>> optStringList() {
    return doList("optStringList");
  }

  /* **************************************************************************
   * GENERATED CONVENIENCE METHODS
   * *************************************************************************/

  @Generated("DoConvenienceMethodsGenerator")
  public TestOptionalDo withOptString(Optional<String> optString) {
    optString().set(optString);
    return this;
  }

  @Generated("DoConvenienceMethodsGenerator")
  public Optional<String> getOptString() {
    return optString().get();
  }

  @Generated("DoConvenienceMethodsGenerator")
  public TestOptionalDo withOptStringList(Collection<? extends Optional<String>> optStringList) {
    optStringList().updateAll(optStringList);
    return this;
  }

  @Generated("DoConvenienceMethodsGenerator")
  public TestOptionalDo withOptStringList(Optional<String>... optStringList) {
    optStringList().updateAll(optStringList);
    return this;
  }

  @Generated("DoConvenienceMethodsGenerator")
  public List<Optional<String>> getOptStringList() {
    return optStringList().get();
  }
}
