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
package org.eclipse.scout.rt.dataobject;

import static org.junit.Assert.*;

import org.eclipse.scout.rt.dataobject.fixture.FirstSimpleContributionFixtureDo;
import org.eclipse.scout.rt.dataobject.fixture.SecondSimpleContributionFixtureDo;
import org.eclipse.scout.rt.dataobject.fixture.SimpleFixtureDo;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.util.Assertions.AssertionException;
import org.eclipse.scout.rt.testing.platform.runner.PlatformTestRunner;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(PlatformTestRunner.class)
public class DataObjectContributionTest {

  @Test
  public void testHasGetContribution() {
    SimpleFixtureDo doEntity = BEANS.get(SimpleFixtureDo.class);
    assertTrue(doEntity.getContributions().isEmpty());

    assertThrows(AssertionException.class, () -> doEntity.getContribution(null)); // contribution class is mandatory

    // has -> false, get -> null
    assertFalse(doEntity.hasContribution(FirstSimpleContributionFixtureDo.class));
    assertFalse(doEntity.hasContribution(SecondSimpleContributionFixtureDo.class));
    assertNull(doEntity.getContribution(FirstSimpleContributionFixtureDo.class));
    assertNull(doEntity.getContribution(SecondSimpleContributionFixtureDo.class));

    // add first contribution
    FirstSimpleContributionFixtureDo firstContribution = BEANS.get(FirstSimpleContributionFixtureDo.class);
    doEntity.putContribution(firstContribution);

    // check node availability and return values of has/get
    assertEquals(1, doEntity.getContributions().size());
    assertSame(firstContribution, doEntity.getContributions().iterator().next());
    assertTrue(doEntity.hasContribution(FirstSimpleContributionFixtureDo.class));
    assertSame(firstContribution, doEntity.getContribution(FirstSimpleContributionFixtureDo.class));

    // second contribution still not available
    assertFalse(doEntity.hasContribution(SecondSimpleContributionFixtureDo.class));
    assertNull(doEntity.getContribution(SecondSimpleContributionFixtureDo.class));
  }

  @Test
  public void testContribution() {
    SimpleFixtureDo doEntity = BEANS.get(SimpleFixtureDo.class);
    assertNull(doEntity.getContribution(FirstSimpleContributionFixtureDo.class));
    FirstSimpleContributionFixtureDo firstContribution = doEntity.contribution(FirstSimpleContributionFixtureDo.class);
    assertNotNull(firstContribution);
    assertSame(firstContribution, doEntity.contribution(FirstSimpleContributionFixtureDo.class)); // same instance if contribution is already available (via previous getOrCreate call)
    assertSame(firstContribution, doEntity.getContribution(FirstSimpleContributionFixtureDo.class)); // same instance for get call

    SecondSimpleContributionFixtureDo secondContribution = BEANS.get(SecondSimpleContributionFixtureDo.class);
    doEntity.putContribution(secondContribution);
    assertSame(secondContribution, doEntity.contribution(SecondSimpleContributionFixtureDo.class)); // same instance if contribution is already available (via putContribution)
  }

  @Test
  public void testPutContribution() {
    SimpleFixtureDo doEntity = BEANS.get(SimpleFixtureDo.class);
    FirstSimpleContributionFixtureDo firstContribution1 = BEANS.get(FirstSimpleContributionFixtureDo.class);
    doEntity.putContribution(firstContribution1);
    assertEquals(1, doEntity.getContributions().size());
    FirstSimpleContributionFixtureDo firstContribution2 = BEANS.get(FirstSimpleContributionFixtureDo.class);
    doEntity.putContribution(firstContribution2);
    assertEquals(1, doEntity.getContributions().size()); // size is still 1, first contribution was overridden

    assertSame(firstContribution2, doEntity.getContribution(FirstSimpleContributionFixtureDo.class)); // same as 2. instance
  }

  @Test
  public void testRemoveContribution() {
    SimpleFixtureDo doEntity = BEANS.get(SimpleFixtureDo.class);
    assertFalse(doEntity.removeContribution(FirstSimpleContributionFixtureDo.class)); // no effect
    assertFalse(doEntity.removeContribution(SecondSimpleContributionFixtureDo.class)); // no effect

    doEntity.putContribution(BEANS.get(FirstSimpleContributionFixtureDo.class));
    assertTrue(doEntity.removeContribution(FirstSimpleContributionFixtureDo.class));
  }

  @Test
  public void testEquality() {
    // Add contributions in order first -> second
    SimpleFixtureDo doEntity1 = BEANS.get(SimpleFixtureDo.class);
    doEntity1.putContribution(BEANS.get(FirstSimpleContributionFixtureDo.class));
    doEntity1.putContribution(BEANS.get(SecondSimpleContributionFixtureDo.class));

    // Add contributions in order second -> first
    SimpleFixtureDo doEntity2 = BEANS.get(SimpleFixtureDo.class);
    doEntity2.putContribution(BEANS.get(SecondSimpleContributionFixtureDo.class));
    doEntity2.putContribution(BEANS.get(FirstSimpleContributionFixtureDo.class));

    // Order of contributions must not be relevant for comparison
    assertEquals(doEntity1, doEntity2);
  }
}
