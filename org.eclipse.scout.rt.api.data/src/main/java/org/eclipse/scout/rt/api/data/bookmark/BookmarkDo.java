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

@TypeName("scout.Bookmark")
public class BookmarkDo extends DoEntity implements IBookmarkDo {

  @Override
  public DoValue<IBookmarkDefinitionDo> definition() {
    return doValue("definition");
  }

  public DoValue<String> id() {
    return doValue("id");
  }

  public DoValue<String> title() {
    return doValue("title");
  }

  public DoValue<String> description() {
    return doValue("description");
  }

  /* **************************************************************************
   * GENERATED CONVENIENCE METHODS
   * *************************************************************************/

  @Override
  @Generated("DoConvenienceMethodsGenerator")
  public BookmarkDo withDefinition(IBookmarkDefinitionDo definition) {
    definition().set(definition);
    return this;
  }

  @Override
  @Generated("DoConvenienceMethodsGenerator")
  public IBookmarkDefinitionDo getDefinition() {
    return definition().get();
  }

  @Generated("DoConvenienceMethodsGenerator")
  public BookmarkDo withId(String id) {
    id().set(id);
    return this;
  }

  @Generated("DoConvenienceMethodsGenerator")
  public String getId() {
    return id().get();
  }

  @Generated("DoConvenienceMethodsGenerator")
  public BookmarkDo withTitle(String title) {
    title().set(title);
    return this;
  }

  @Generated("DoConvenienceMethodsGenerator")
  public String getTitle() {
    return title().get();
  }
}
