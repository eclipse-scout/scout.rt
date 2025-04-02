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

@TypeName("scout.BookmarkTableRowIdentifierStringComponent")
@TypeVersion(Scout_25_2_001.class)
public class BookmarkTableRowIdentifierStringComponentDo extends DoEntity implements IBookmarkTableRowIdentifierComponentDo {

  public DoValue<String> key() {
    return doValue("key");
  }

  /* **************************************************************************
   * CUSTOM CONVENIENCE METHODS
   * *************************************************************************/

  public static BookmarkTableRowIdentifierStringComponentDo of(String value) {
    return BEANS.get(BookmarkTableRowIdentifierStringComponentDo.class).withKey(value);
  }

  /* **************************************************************************
   * GENERATED CONVENIENCE METHODS
   * *************************************************************************/

  @Generated("DoConvenienceMethodsGenerator")
  public BookmarkTableRowIdentifierStringComponentDo withKey(String key) {
    key().set(key);
    return this;
  }

  @Generated("DoConvenienceMethodsGenerator")
  public String getKey() {
    return key().get();
  }
}
