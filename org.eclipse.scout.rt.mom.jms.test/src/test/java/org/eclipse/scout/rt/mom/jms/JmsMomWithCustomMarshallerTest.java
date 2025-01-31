/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.mom.jms;

import static org.junit.Assert.assertEquals;

import java.util.Map;
import java.util.Objects;

import org.eclipse.scout.rt.mom.api.IDestination;
import org.eclipse.scout.rt.mom.api.IDestination.DestinationType;
import org.eclipse.scout.rt.mom.api.IDestination.ResolveMethod;
import org.eclipse.scout.rt.mom.api.IMomImplementor;
import org.eclipse.scout.rt.mom.api.MOM;
import org.eclipse.scout.rt.mom.api.marshaller.IMarshaller;
import org.eclipse.scout.rt.mom.api.marshaller.ObjectMarshaller;
import org.eclipse.scout.rt.mom.api.marshaller.TextMarshaller;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.IgnoreBean;
import org.eclipse.scout.rt.platform.Replace;
import org.eclipse.scout.rt.platform.exception.PlatformException;
import org.eclipse.scout.rt.platform.holders.StringHolder;
import org.junit.Test;

public class JmsMomWithCustomMarshallerTest extends AbstractJmsMomTest {

  public JmsMomWithCustomMarshallerTest(AbstractJmsMomTestParameter parameter) {
    super(parameter);
  }

  @Test
  public void testMomEnvironmentWithCustomDefaultMarshaller() throws InterruptedException {
    installMom(FixtureMomWithTextMarshaller.class);
    final Capturer<String> capturer1 = new Capturer<>();
    final Capturer<Object> capturer2 = new Capturer<>();

    IDestination<String> queueString = MOM.newDestination("test/mom/testPublishStringData", DestinationType.QUEUE, ResolveMethod.DEFINE, null);
    IDestination<Object> queueObject = MOM.newDestination("test/mom/testPublishObjectData", DestinationType.QUEUE, ResolveMethod.DEFINE, null);
    m_disposables.add(MOM.registerMarshaller(FixtureMom.class, queueObject, BEANS.get(ObjectMarshaller.class)));

    MOM.publish(FixtureMom.class, queueString, "Hello MOM!");
    MOM.publish(FixtureMom.class, queueObject, new StringHolder("Hello MOM! (holder)"));
    m_disposables.add(MOM.subscribe(FixtureMom.class, queueString, message -> capturer1.set(message.getTransferObject())));
    m_disposables.add(MOM.subscribe(FixtureMom.class, queueObject, message -> capturer2.set(message.getTransferObject())));

    // Verify
    String received1 = capturer1.get();
    Object received2 = capturer2.get();
    assertEquals("Hello MOM!", received1);
    assertEquals("Hello MOM! (holder)", Objects.toString(received2));
  }

  @Test
  public void testMomEnvironmentWithConfiguredDefaultMarshaller() throws InterruptedException {
    installMom(FixtureMomWithConfiguredTextMarshaller.class);
    final Capturer<String> capturer = new Capturer<>();

    IDestination<String> queueString = MOM.newDestination("test/mom/testPublishStringData", DestinationType.QUEUE, ResolveMethod.DEFINE, null);

    MOM.publish(FixtureMom.class, queueString, "Hello MOM!");
    m_disposables.add(MOM.subscribe(FixtureMom.class, queueString, message -> capturer.set(message.getTransferObject())));

    // Verify
    String received = capturer.get();
    assertEquals("!MOM olleH", received);
  }

  @Test(expected = PlatformException.class)
  public void testMomEnvironmentWithInvalidMarshaller() {
    installMom(FixtureMomWithInvalidMarshaller.class);
    IDestination<String> queueString = MOM.newDestination("test/mom/testPublishStringData", DestinationType.QUEUE, ResolveMethod.DEFINE, null);
    MOM.publish(FixtureMom.class, queueString, "Hello MOM!");
  }

  @IgnoreBean
  @Replace
  public static class FixtureMomWithTextMarshaller extends FixtureMom {

    public FixtureMomWithTextMarshaller(AbstractJmsMomTestParameter parameter) {
      super(parameter);
    }

    @Override
    protected Map<String, String> getConfiguredEnvironment() {
      final Map<String, String> env = super.getConfiguredEnvironment();
      env.put(IMomImplementor.MARSHALLER, TextMarshaller.class.getName());
      env.put(JmsMomImplementor.JMS_CLIENT_ID, "junit_mom_client");
      return env;
    }
  }

  @IgnoreBean
  @Replace
  public static class FixtureMomWithConfiguredTextMarshaller extends FixtureMom {

    public FixtureMomWithConfiguredTextMarshaller(AbstractJmsMomTestParameter parameter) {
      super(parameter);
    }

    @Override
    protected IMarshaller getConfiguredDefaultMarshaller() {
      return new TextMarshaller() {
        @Override
        public Object unmarshall(Object data, Map<String, String> context) {
          return new StringBuilder((String) data).reverse().toString();
        }
      };
    }
  }

  @IgnoreBean
  @Replace
  public static class FixtureMomWithInvalidMarshaller extends FixtureMom {

    public FixtureMomWithInvalidMarshaller(AbstractJmsMomTestParameter parameter) {
      super(parameter);
    }

    @Override
    protected Map<String, String> getConfiguredEnvironment() {
      final Map<String, String> env = super.getConfiguredEnvironment();
      env.put(IMomImplementor.MARSHALLER, "Invalid Class Name");
      return env;
    }
  }
}
