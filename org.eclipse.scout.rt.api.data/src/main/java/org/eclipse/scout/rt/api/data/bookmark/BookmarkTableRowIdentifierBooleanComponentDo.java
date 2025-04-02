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
import org.eclipse.scout.rt.dataobject.ScoutTypeVersions.Scout_25_2_001;
import org.eclipse.scout.rt.dataobject.TypeName;
import org.eclipse.scout.rt.dataobject.TypeVersion;
import org.eclipse.scout.rt.platform.BEANS;

@TypeName("scout.BookmarkTableRowIdentifierBooleanComponent")
@TypeVersion(Scout_25_2_001.class)
public class BookmarkTableRowIdentifierBooleanComponentDo extends DoEntity implements IBookmarkTableRowIdentifierComponentDo {

  public DoValue<Boolean> key() {
    return doValue("key");
  }

  /* **************************************************************************
   * CUSTOM CONVENIENCE METHODS
   * *************************************************************************/

  public static BookmarkTableRowIdentifierBooleanComponentDo of(Boolean value) {
    return BEANS.get(BookmarkTableRowIdentifierBooleanComponentDo.class).withKey(value);
  }

  /* **************************************************************************
   * GENERATED CONVENIENCE METHODS
   * *************************************************************************/

  @Generated("DoConvenienceMethodsGenerator")
  public BookmarkTableRowIdentifierBooleanComponentDo withKey(Boolean key) {
    key().set(key);
    return this;
  }

  @Generated("DoConvenienceMethodsGenerator")
  public Boolean getKey() {
    return key().get();
  }

  @Generated("DoConvenienceMethodsGenerator")
  public boolean isKey() {
    return nvl(getKey());
  }
}
