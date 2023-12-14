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

import jakarta.annotation.Generated;

import org.eclipse.scout.rt.dataobject.DoEntity;
import org.eclipse.scout.rt.dataobject.DoValue;
import org.eclipse.scout.rt.dataobject.TypeName;

@TypeName("TestBinary")
public class TestBinaryDo extends DoEntity {

  public DoValue<byte[]> content() {
    return doValue("content");
  }

  /* **************************************************************************
   * GENERATED CONVENIENCE METHODS
   * *************************************************************************/

  @Generated("DoConvenienceMethodsGenerator")
  public TestBinaryDo withContent(byte[] content) {
    content().set(content);
    return this;
  }

  @Generated("DoConvenienceMethodsGenerator")
  public byte[] getContent() {
    return content().get();
  }
}
