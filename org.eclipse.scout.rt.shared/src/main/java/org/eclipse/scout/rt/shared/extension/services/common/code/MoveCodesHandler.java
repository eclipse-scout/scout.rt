/*******************************************************************************
 * Copyright (c) 2010-2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.shared.extension.services.common.code;

import java.util.LinkedList;
import java.util.List;

import org.eclipse.scout.rt.platform.OrderedComparator;
import org.eclipse.scout.rt.platform.util.collection.OrderedCollection;
import org.eclipse.scout.rt.shared.extension.AbstractMoveModelObjectHandler;
import org.eclipse.scout.rt.shared.services.common.code.ICode;

public class MoveCodesHandler<CODE_ID, CODE extends ICode<CODE_ID>> extends AbstractMoveModelObjectHandler<CODE> {

  public MoveCodesHandler(OrderedCollection<CODE> rootModelObjects) {
    super("code", rootModelObjects);
  }

  @Override
  @SuppressWarnings("unchecked")
  protected CODE getParent(CODE child) {
    return (CODE) child.getParentCode();
  }

  @Override
  protected void removeChild(CODE parent, CODE child) {
    parent.removeChildCodeInternal(child.getId());
  }

  @Override
  protected void addChild(CODE parent, CODE child) {
    parent.addChildCodeInternal(-1, child);
    sortChildren(parent);
  }

  @Override
  protected void sortChildren(CODE parent) {
    List<? extends ICode<CODE_ID>> childCodes = parent.getChildCodes(false);
    childCodes.sort(new OrderedComparator());
    int index = 0;
    for (ICode<CODE_ID> code : childCodes) {
      parent.addChildCodeInternal(index, code);
      index++;
    }
  }

  @Override
  protected List<CODE> collectAllModelObjects() {
    List<CODE> allCodes = new LinkedList<>();
    collectAllCodes(getRootModelObjects(), allCodes);
    return allCodes;
  }

  private void collectAllCodes(Iterable<? extends CODE> codes, List<CODE> allCodes) {
    if (codes == null) {
      return;
    }
    for (CODE code : codes) {
      allCodes.add(code);
      @SuppressWarnings("unchecked")
      List<? extends CODE> childCodes = (List<? extends CODE>) code.getChildCodes(false);
      collectAllCodes(childCodes, allCodes);
    }
  }
}
