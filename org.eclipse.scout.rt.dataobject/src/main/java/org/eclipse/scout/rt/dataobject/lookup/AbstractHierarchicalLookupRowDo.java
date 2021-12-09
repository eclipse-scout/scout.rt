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
package org.eclipse.scout.rt.dataobject.lookup;

import javax.annotation.Generated;

import org.eclipse.scout.rt.dataobject.DoValue;

/**
 * Abstract base class for hierarchical lookup rows with generic key type T.
 *
 * @param <SELF>
 *          Type reference to concrete sub-class, used to implement with() methods returning concrete sub-class type
 * @param <ID>
 *          Lookup row id type
 */
public abstract class AbstractHierarchicalLookupRowDo<SELF extends AbstractHierarchicalLookupRowDo<SELF, ID>, ID> extends AbstractLookupRowDo<SELF, ID> {

  public static final String PARENT_ID = "parentId";

  /**
   * A subclass should implement this method to specify the concrete attribute type.
   *
   * @see AbstractHierarchicalLookupRowDo#createParentIdAttribute(AbstractHierarchicalLookupRowDo)
   */
  public abstract DoValue<ID> parentId();

  /* **************************************************************************
   * HELPER METHODS
   * *************************************************************************/

  protected static <ID> DoValue<ID> createParentIdAttribute(AbstractHierarchicalLookupRowDo<?, ID> self) {
    return self.doValue(PARENT_ID);
  }

  /* **************************************************************************
   * CUSTOM CONVENIENCE METHODS
   * *************************************************************************/

  public SELF withParentId(ID parentId) {
    parentId().set(parentId);
    return self();
  }

  /* **************************************************************************
   * GENERATED CONVENIENCE METHODS
   * *************************************************************************/

  @Generated("DoConvenienceMethodsGenerator")
  public ID getParentId() {
    return parentId().get();
  }
}
