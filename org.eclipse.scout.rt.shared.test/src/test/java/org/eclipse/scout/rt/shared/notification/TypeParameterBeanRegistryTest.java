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
package org.eclipse.scout.rt.shared.notification;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

/**
 * Tests for {@link TypeParameterBeanRegistry}
 */
public class TypeParameterBeanRegistryTest {

  @Test
  public void testEmptyRegistry() throws Exception {
    final TypeParameterBeanRegistry<ITestHandler> reg = new TypeParameterBeanRegistry<>();
    assertTrue(reg.getBeans(String.class).isEmpty());
  }

  @Test
  public void testEmptyRegistration() {
    final TypeParameterBeanRegistry<ITestHandler> reg = new TypeParameterBeanRegistry<>();
    final List<ITestHandler> l = new ArrayList<>();
    reg.registerBeans(ITestHandler.class, l);
    assertTrue(reg.getBeans(String.class).isEmpty());
  }

  @Test
  public void testStringHandler() {
    final TypeParameterBeanRegistry<ITestHandler> reg = new TypeParameterBeanRegistry<>();
    final List<ITestHandler> l = new ArrayList<>();
    l.add(new String1Handler());
    l.add(new LongHandler());
    l.add(new String2Handler());
    l.add(new CharSequenceHandler());
    reg.registerBeans(ITestHandler.class, l);
    final List<ITestHandler> res = reg.getBeans(String.class);
    assertEquals(3, res.size());
    assertEquals(l.get(0), res.get(0));
    assertEquals(l.get(2), res.get(1));
    assertEquals(l.get(3), res.get(2));
  }

  @Test
  public void testCahedCharSeqHandler() {
    final TypeParameterBeanRegistry<ITestHandler> reg = new TypeParameterBeanRegistry<>();
    final List<ITestHandler> l = new ArrayList<>();
    l.add(new String1Handler());
    l.add(new LongHandler());
    l.add(new String2Handler());
    l.add(new CharSequenceHandler());
    reg.registerBeans(ITestHandler.class, l);
    //first lookup
    reg.getBeans(CharSequence.class);
    //get cached result
    final List<ITestHandler> res = reg.getBeans(CharSequence.class);
    assertEquals(1, res.size());
    assertEquals(l.get(3), res.get(0));
  }

  interface ITestHandler<T> {
  }

  class String1Handler implements ITestHandler<String> {
  }

  class String2Handler implements ITestHandler<String> {
  }

  class CharSequenceHandler implements ITestHandler<CharSequence> {
  }

  class LongHandler implements ITestHandler<Long> {
  }

}
