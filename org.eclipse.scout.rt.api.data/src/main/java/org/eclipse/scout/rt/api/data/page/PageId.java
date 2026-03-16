/*
 * Copyright (c) 2010, 2025 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.api.data.page;

import java.io.Serial;

import org.eclipse.scout.rt.dataobject.id.AbstractStringId;
import org.eclipse.scout.rt.dataobject.id.IdTypeName;
import org.eclipse.scout.rt.platform.util.StringUtility;

/**
 * Page ID that can be used to identify an IPage.
 * <p>
 * Represents the class ID of a page ({@link org.eclipse.scout.rt.platform.classid.ITypeWithClassId#classId}).
 *
 * @since 23.1
 */
@IdTypeName("scout.PageId")
public final class PageId extends AbstractStringId {
  @Serial
  private static final long serialVersionUID = 1L;

  public static final PageId WILDCARD = PageId.of("*");

  private PageId(String id) {
    super(id);
  }

  public static PageId of(String id) {
    if (StringUtility.isNullOrEmpty(id)) {
      return null;
    }
    return new PageId(id);
  }
}
