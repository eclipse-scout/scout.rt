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

import java.util.Collection;
import java.util.List;

import jakarta.annotation.Generated;

import org.eclipse.scout.rt.dataobject.DoEntity;
import org.eclipse.scout.rt.dataobject.DoList;
import org.eclipse.scout.rt.dataobject.ScoutTypeVersions.Scout_25_2_001;
import org.eclipse.scout.rt.dataobject.TypeName;
import org.eclipse.scout.rt.dataobject.TypeVersion;

/**
 * Used to identify rows when restoring a bookmark's selection/drill down.
 */
@TypeName("scout.BookmarkTableRowIdentifier")
@TypeVersion(Scout_25_2_001.class)
public class BookmarkTableRowIdentifierDo extends DoEntity {

  public DoList<IBookmarkTableRowIdentifierComponentDo> keyComponents() {
    return doList("keyComponents");
  }

  /* **************************************************************************
   * GENERATED CONVENIENCE METHODS
   * *************************************************************************/

  @Generated("DoConvenienceMethodsGenerator")
  public BookmarkTableRowIdentifierDo withKeyComponents(Collection<? extends IBookmarkTableRowIdentifierComponentDo> keyComponents) {
    keyComponents().updateAll(keyComponents);
    return this;
  }

  @Generated("DoConvenienceMethodsGenerator")
  public BookmarkTableRowIdentifierDo withKeyComponents(IBookmarkTableRowIdentifierComponentDo... keyComponents) {
    keyComponents().updateAll(keyComponents);
    return this;
  }

  @Generated("DoConvenienceMethodsGenerator")
  public List<IBookmarkTableRowIdentifierComponentDo> getKeyComponents() {
    return keyComponents().get();
  }
}
