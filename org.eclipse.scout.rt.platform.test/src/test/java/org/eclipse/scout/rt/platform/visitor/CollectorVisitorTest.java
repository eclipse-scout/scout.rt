/*******************************************************************************
 * Copyright (c) 2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.platform.visitor;

import static org.junit.Assert.assertEquals;

import org.eclipse.scout.rt.platform.util.CollectionUtility;
import org.eclipse.scout.rt.platform.visitor.CollectorVisitor;
import org.junit.Test;

public class CollectorVisitorTest {

  @Test
  public void test() {
    Object object1 = new Object();
    Object object2 = new Object();
    Object object3 = new Object();

    CollectorVisitor<Object> collectorVisitor = new CollectorVisitor<>();

    collectorVisitor.visit(object1);
    collectorVisitor.visit(object2);
    collectorVisitor.visit(object3);

    assertEquals(CollectionUtility.arrayList(object1, object2, object3), collectorVisitor.getElements());
  }
}
