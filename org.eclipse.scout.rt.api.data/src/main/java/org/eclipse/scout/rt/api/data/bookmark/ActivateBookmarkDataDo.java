/*
 * Copyright (c) 2010, 2025 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.api.data.bookmark;

import jakarta.annotation.Generated;

import org.eclipse.scout.rt.dataobject.DoEntity;
import org.eclipse.scout.rt.dataobject.DoValue;
import org.eclipse.scout.rt.dataobject.TypeName;

@TypeName("scout.ActivateBookmarkData")
public class ActivateBookmarkDataDo extends DoEntity {

  public DoValue<OutlineBookmarkDefinitionDo> bookmarkDefinition() {
    return doValue("bookmarkDefinition");
  }

  /* **************************************************************************
   * GENERATED CONVENIENCE METHODS
   * *************************************************************************/

  @Generated("DoConvenienceMethodsGenerator")
  public ActivateBookmarkDataDo withBookmarkDefinition(OutlineBookmarkDefinitionDo bookmarkDefinition) {
    bookmarkDefinition().set(bookmarkDefinition);
    return this;
  }

  @Generated("DoConvenienceMethodsGenerator")
  public OutlineBookmarkDefinitionDo getBookmarkDefinition() {
    return bookmarkDefinition().get();
  }
}
