/*
 * Copyright (c) 2010, 2026 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.api.data.table;

import jakarta.annotation.Generated;

import org.eclipse.scout.rt.dataobject.DoEntity;
import org.eclipse.scout.rt.dataobject.DoValue;
import org.eclipse.scout.rt.dataobject.TypeName;
import org.eclipse.scout.rt.platform.BEANS;

@TypeName("scout.TableRowDropTypes")
public class TableRowDropTypesDo extends DoEntity {

  public static TableRowDropTypesDo of(TableRowDropType dropType) {
    return BEANS.get(TableRowDropTypesDo.class)
        .withBefore(dropType)
        .withAfter(dropType)
        .withInside(dropType);
  }

  /**
   * Default is {@link TableRowDropType#ALLOWED}
   */
  public DoValue<TableRowDropType> before() {
    return doValue("before");
  }

  /**
   * Default is {@link TableRowDropType#ALLOWED}
   */
  public DoValue<TableRowDropType> after() {
    return doValue("after");
  }

  /**
   * Default is {@link TableRowDropType#NONE} for flat tables and {@link TableRowDropType#ALLOWED} for hierarchical tables
   */
  public DoValue<TableRowDropType> inside() {
    return doValue("inside");
  }

  /* **************************************************************************
   * GENERATED CONVENIENCE METHODS
   * *************************************************************************/

  /**
   * See {@link #before()}.
   */
  @Generated("DoConvenienceMethodsGenerator")
  public TableRowDropTypesDo withBefore(TableRowDropType before) {
    before().set(before);
    return this;
  }

  /**
   * See {@link #before()}.
   */
  @Generated("DoConvenienceMethodsGenerator")
  public TableRowDropType getBefore() {
    return before().get();
  }

  /**
   * See {@link #after()}.
   */
  @Generated("DoConvenienceMethodsGenerator")
  public TableRowDropTypesDo withAfter(TableRowDropType after) {
    after().set(after);
    return this;
  }

  /**
   * See {@link #after()}.
   */
  @Generated("DoConvenienceMethodsGenerator")
  public TableRowDropType getAfter() {
    return after().get();
  }

  /**
   * See {@link #inside()}.
   */
  @Generated("DoConvenienceMethodsGenerator")
  public TableRowDropTypesDo withInside(TableRowDropType inside) {
    inside().set(inside);
    return this;
  }

  /**
   * See {@link #inside()}.
   */
  @Generated("DoConvenienceMethodsGenerator")
  public TableRowDropType getInside() {
    return inside().get();
  }
}
