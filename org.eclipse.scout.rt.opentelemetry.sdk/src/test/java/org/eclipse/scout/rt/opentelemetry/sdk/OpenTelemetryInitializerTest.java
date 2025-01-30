/*
 * Copyright (c) 2010, 2024 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.opentelemetry.sdk;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.scout.rt.opentelemetry.sdk.OpenTelemetryInitializer.OpenTelemetryInitializerEnabledProperty;
import org.eclipse.scout.rt.opentelemetry.sdk.OpenTelemetryInitializerTest.OpenTelemetryInitializerPlatform;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.BeanMetaData;
import org.eclipse.scout.rt.platform.IBean;
import org.eclipse.scout.rt.platform.opentelemetry.IHistogramViewHintProvider;
import org.eclipse.scout.rt.platform.opentelemetry.IMetricProvider;
import org.eclipse.scout.rt.testing.platform.BeanTestingHelper;
import org.eclipse.scout.rt.testing.platform.TestingDefaultPlatform;
import org.eclipse.scout.rt.testing.platform.runner.PlatformTestRunner;
import org.eclipse.scout.rt.testing.platform.runner.RunWithNewPlatform;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;

import io.opentelemetry.api.GlobalOpenTelemetry;
import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.sdk.OpenTelemetrySdk;

@RunWith(PlatformTestRunner.class)
@RunWithNewPlatform(platform = OpenTelemetryInitializerPlatform.class)
public class OpenTelemetryInitializerTest {

  private final List<IBean<?>> m_beans = new ArrayList<>();

  private IMetricProvider m_metricProvider;
  private IHistogramViewHintProvider m_histogramViewHintProvider;

  @Before
  public void before() {
    GlobalOpenTelemetry.resetForTest();
    // OpenTelemetryInitializer is excluded in testing platform and initialized manually by test methods
    OpenTelemetryInitializer initializer = BEANS.opt(OpenTelemetryInitializer.class);
    assertNull(initializer);

    m_metricProvider = Mockito.mock(IMetricProvider.class);
    m_histogramViewHintProvider = Mockito.mock(IHistogramViewHintProvider.class);
    Mockito.when(m_histogramViewHintProvider.getInstrumentName()).thenReturn("test");
    m_beans.addAll(BeanTestingHelper.get().registerBeans(
        new BeanMetaData(IMetricProvider.class)
            .withApplicationScoped(true)
            .withInitialInstance(m_metricProvider),
        new BeanMetaData(IHistogramViewHintProvider.class)
            .withApplicationScoped(true)
            .withInitialInstance(m_histogramViewHintProvider),
        new BeanMetaData(OpenTelemetryInitializer.class)));
    initializer = BEANS.opt(OpenTelemetryInitializer.class);
    assertNotNull(initializer);
  }

  @After
  public void after() {
    BeanTestingHelper.get().unregisterBeans(m_beans);
    m_beans.clear();
  }

  @Test
  public void testInitializerEnabled() {
    m_beans.add(BeanTestingHelper.get().mockConfigProperty(OpenTelemetryInitializerEnabledProperty.class, Boolean.TRUE));

    OpenTelemetryInitializer initializer = BEANS.opt(OpenTelemetryInitializer.class);
    initializer.initOpenTelemetry();
    assertMetricProviderInvocations(1, 0);
    assertHistogramViewHintProviderInvocations(1);

    OpenTelemetrySdk scoutOpenTelemetry = initializer.m_openTelemetry;
    assertNotNull(scoutOpenTelemetry);
    OpenTelemetry globalOpenTelemetry = GlobalOpenTelemetry.get();
    assertNotNull(globalOpenTelemetry);
    assertSame(scoutOpenTelemetry.getPropagators(), globalOpenTelemetry.getPropagators());
    assertSame(scoutOpenTelemetry.getLogsBridge(), globalOpenTelemetry.getLogsBridge());
    assertSame(scoutOpenTelemetry.getMeterProvider(), globalOpenTelemetry.getMeterProvider());
    assertSame(scoutOpenTelemetry.getTracerProvider(), globalOpenTelemetry.getTracerProvider());

    initializer.shutdownOpenTelemetry();
    assertMetricProviderInvocations(1, 1);
    assertNull(initializer.m_openTelemetry);
  }

  @Test
  public void testInitializerDisabled() {
    m_beans.add(BeanTestingHelper.get().mockConfigProperty(OpenTelemetryInitializerEnabledProperty.class, Boolean.FALSE));

    OpenTelemetryInitializer initializer = BEANS.opt(OpenTelemetryInitializer.class);
    initializer.initOpenTelemetry();
    assertMetricProviderInvocations(0, 0);
    assertHistogramViewHintProviderInvocations(0);

    OpenTelemetrySdk scoutOpenTelemetry = initializer.m_openTelemetry;
    assertNull(scoutOpenTelemetry);
    OpenTelemetry globalOpenTelemetry = GlobalOpenTelemetry.get();
    assertSame(OpenTelemetry.noop(), globalOpenTelemetry);

    initializer.shutdownOpenTelemetry();
    assertMetricProviderInvocations(0, 0);
    assertNull(initializer.m_openTelemetry);
  }

  private void assertMetricProviderInvocations(int expectedRegisterInvocations, int expectedCloseInvocations) {
    Mockito.verify(m_metricProvider, Mockito.times(expectedRegisterInvocations)).register(Mockito.any());
    Mockito.verify(m_metricProvider, Mockito.times(expectedCloseInvocations)).close();
  }

  private void assertHistogramViewHintProviderInvocations(int expectedInvocations) {
    Mockito.verify(m_histogramViewHintProvider, Mockito.times(expectedInvocations)).getInstrumentName();
    Mockito.verify(m_histogramViewHintProvider, Mockito.times(expectedInvocations)).getExplicitBuckets();
  }

  public static class OpenTelemetryInitializerPlatform extends TestingDefaultPlatform {

    @Override
    protected boolean acceptBean(Class<?> bean) {
      if (OpenTelemetryInitializer.class.isAssignableFrom(bean)) {
        return false;
      }
      if (IMetricProvider.class.isAssignableFrom(bean)) {
        // do register default metrics during tests
        return false;
      }
      return super.acceptBean(bean);
    }
  }
}
