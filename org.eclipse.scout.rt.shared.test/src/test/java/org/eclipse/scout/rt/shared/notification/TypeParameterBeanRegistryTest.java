/*******************************************************************************
 * Copyright (c) 2010-2017 BSI Business Systems Integration AG.
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
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.eclipse.scout.rt.platform.TypeParameterBeanRegistry;
import org.eclipse.scout.rt.platform.util.IRegistrationHandle;
import org.junit.Test;

/**
 * Tests for {@link TypeParameterBeanRegistry}
 */
public class TypeParameterBeanRegistryTest {

  @Test
  public void testEmptyRegistry() throws Exception {
    final TypeParameterBeanRegistry<ITestHandler> reg = new TypeParameterBeanRegistry<>(ITestHandler.class);
    assertTrue(reg.getBeans(String.class).isEmpty());
  }

  @Test
  public void testEmptyRegistration() {
    final TypeParameterBeanRegistry<ITestHandler> reg = new TypeParameterBeanRegistry<>(ITestHandler.class);
    final List<ITestHandler> l = new ArrayList<>();
    reg.registerBeans(l);
    assertTrue(reg.getBeans(String.class).isEmpty());
  }

  @Test
  public void testStringHandler() {
    final TypeParameterBeanRegistry<ITestHandler> registry = new TypeParameterBeanRegistry<>(ITestHandler.class);
    final List<ITestHandler> l = new ArrayList<>();
    l.add(new String1Handler());
    l.add(new LongHandler());
    l.add(new String2Handler());
    l.add(new CharSequenceHandler());

    IRegistrationHandle registration = registry.registerBeans(l);
    final List<ITestHandler> stringResult = registry.getBeans(String.class);
    assertEquals(3, stringResult.size());
    assertEquals(l.get(0), stringResult.get(0));
    assertEquals(l.get(2), stringResult.get(1));
    assertEquals(l.get(3), stringResult.get(2));

    List<ITestHandler> charSequenceResult = registry.getBeans(CharSequence.class);
    assertEquals(1, charSequenceResult.size());
    assertEquals(l.get(3), charSequenceResult.get(0));

    registration.dispose();

    assertEquals(0, registry.getBeans(String.class).size());
    assertEquals(0, registry.getBeans(CharSequence.class).size());
  }

  @Test
  public void testStringHandler1() {
    String1Handler sh1 = new String1Handler();
    String2Handler sh2 = new String2Handler();
    CharSequenceHandler ch1 = new CharSequenceHandler();

    final TypeParameterBeanRegistry<ITestHandler> registry = new TypeParameterBeanRegistry<>(ITestHandler.class);

    registry.registerBean(sh1);
    assertEquals(Arrays.asList(sh1), registry.getBeans(String.class));
    assertEquals(Collections.emptyList(), registry.getBeans(CharSequence.class));

    registry.registerBean(sh2);
    assertEquals(Arrays.asList(sh1, sh2), registry.getBeans(String.class));
    assertEquals(Collections.emptyList(), registry.getBeans(CharSequence.class));

    registry.registerBean(ch1);
    assertEquals(Arrays.asList(sh1, sh2, ch1), registry.getBeans(String.class));
    assertEquals(Arrays.asList(ch1), registry.getBeans(CharSequence.class));
  }

  @Test
  public void testCachedCharSeqHandler() {
    final TypeParameterBeanRegistry<ITestHandler> reg = new TypeParameterBeanRegistry<>(ITestHandler.class);
    final List<ITestHandler> l = new ArrayList<>();
    l.add(new String1Handler());
    l.add(new LongHandler());
    l.add(new String2Handler());
    l.add(new CharSequenceHandler());
    reg.registerBeans(l);
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
