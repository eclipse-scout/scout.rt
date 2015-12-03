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
package org.eclipse.scout.rt.client.ui.form.fields;

import org.eclipse.scout.rt.platform.OrderedComparator;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

/**
 * @since 4.1
 */
public class OrderedComparatorTest {

  @Test
  public void testCompare() {
    OrderedComparator comparator = new OrderedComparator();
    IFormField f1 = Mockito.mock(IFormField.class);
    IFormField f2 = Mockito.mock(IFormField.class);
    Mockito.when(f1.getOrder()).thenReturn(10d);
    Mockito.when(f2.getOrder()).thenReturn(20d);

    Assert.assertEquals(0, comparator.compare(null, null));
    Assert.assertEquals(-1, comparator.compare(null, f1));
    Assert.assertEquals(1, comparator.compare(f1, null));

    Assert.assertEquals(-1, comparator.compare(f1, f2));
    Assert.assertEquals(1, comparator.compare(f2, f1));

    Assert.assertEquals(0, comparator.compare(f1, f1));
  }
}
