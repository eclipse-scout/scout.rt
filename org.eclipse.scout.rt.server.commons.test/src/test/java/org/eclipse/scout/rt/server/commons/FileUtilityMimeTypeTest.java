/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.server.commons;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import jakarta.servlet.ServletContext;

import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.BeanMetaData;
import org.eclipse.scout.rt.platform.IBean;
import org.eclipse.scout.rt.platform.util.FileUtility;
import org.eclipse.scout.rt.testing.platform.BeanTestingHelper;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

/**
 * JUnit tests for {@link FileUtility#getMimeType(java.nio.file.Path)} using servlet context
 *
 * @since 5.2
 */
public class FileUtilityMimeTypeTest {

  private List<IBean<?>> beans = new ArrayList<>();

  @Before
  public void before() {
    ServletContext servletContext = Mockito.mock(ServletContext.class);
    Mockito.when(servletContext.getMimeType("file.xml")).thenReturn("application/xml");
    Mockito.when(servletContext.getMimeType("file.XML")).thenReturn("application/xml");
    Mockito.when(servletContext.getMimeType("file.m4v")).thenReturn("video/mp4");
    beans.add(BEANS.get(BeanTestingHelper.class).registerBean(new BeanMetaData(ServletContext.class, servletContext).withApplicationScoped(true)));
  }

  @After
  public void after() {
    BEANS.get(BeanTestingHelper.class).unregisterBeans(beans);
  }

  @Test
  public void testGetMimeType_xml() {
    assertEquals("text/xml", FileUtility.getMimeType("file.xml"));
  }

  @Test
  public void testGetMimeType_XML() {
    assertEquals("text/xml", FileUtility.getMimeType("file.XML"));
  }

  @Test
  public void testGetMimeType_m4v() {
    assertEquals("video/mp4", FileUtility.getMimeType("file.m4v"));
  }

  @Test
  public void testGetMimeType_invalidPath() {
    assertEquals("text/xml", FileUtility.getMimeType("*.xml"));
  }
}
