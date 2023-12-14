/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.jackson.dataobject.fixture;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import jakarta.annotation.Generated;

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
