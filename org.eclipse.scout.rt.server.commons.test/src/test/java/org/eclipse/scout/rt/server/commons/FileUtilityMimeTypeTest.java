package org.eclipse.scout.rt.server.commons;

import static org.junit.Assert.assertEquals;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletContext;

import org.eclipse.scout.rt.platform.BeanMetaData;
import org.eclipse.scout.rt.platform.IBean;
import org.eclipse.scout.rt.platform.util.FileUtility;
import org.eclipse.scout.rt.testing.shared.TestingUtility;
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
    beans.add(TestingUtility.registerBean(new BeanMetaData(ServletContext.class, servletContext).withApplicationScoped(true)));
  }

  @After
  public void after() {
    TestingUtility.unregisterBeans(beans);
  }

  @Test
  public void testGetMimeType_xml() {
    assertEquals("text/xml", FileUtility.getMimeType(Paths.get("file.xml")));
  }

  @Test
  public void testGetMimeType_XML() {
    assertEquals("text/xml", FileUtility.getMimeType(Paths.get("file.XML")));
  }

  @Test
  public void testGetMimeType_m4v() {
    assertEquals("video/mp4", FileUtility.getMimeType(Paths.get("file.m4v")));
  }
}
