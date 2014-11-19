/*******************************************************************************
 * Copyright (c) 2010 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.client.ui.desktop.outline.pages;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.List;

import org.eclipse.scout.commons.annotations.ClassId;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.rt.client.Activator;
import org.eclipse.scout.rt.shared.services.common.exceptionhandler.IExceptionHandlerService;
import org.eclipse.scout.rt.testing.shared.TestingUtility;
import org.eclipse.scout.testing.client.runner.ScoutClientTestRunner;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.osgi.framework.ServiceRegistration;

/**
 * JUnit tests for {@link AbstractPage}
 *
 * @since 3.10.0
 */
@RunWith(ScoutClientTestRunner.class)
public class PageTest {
  private static final String TEST_PAGE_CLASS_ID = "TEST_CLASS_ID";
  private List<ServiceRegistration> m_serviceRegs;
  private final int TEST_SERVICE_RANKING = 10000;

  @After
  public void tearDown() {
    TestingUtility.unregisterServices(m_serviceRegs);
  }

  @Test
  public void testClassIdAnnotatedPage() {
    PageWithClassId testPage = new PageWithClassId();
    assertEquals("classid should correspond to annotated id", TEST_PAGE_CLASS_ID, testPage.classId());
  }

  @Test
  public void testComputedClassId() {
    PageWithoutClassId testPageNoAnnotation = new PageWithoutClassId();
    String className = testPageNoAnnotation.getClass().getName();
    assertEquals("classid should correspond to annotated id", className, testPageNoAnnotation.classId());
  }

  /**
   * {@link AbstractPage#execPageActivated()}, if a ProcessingExeption is thrown
   */
  @Test
  public void testPageActivatedProcessingException() {
    IExceptionHandlerService service = createMockExceptionHandlerService();
    final IPage testPage = new PageExceptionOnActivated();
    testPage.pageActivatedNotify();
    verify(service, times(1)).handleException(any(ProcessingException.class));
  }

  /**
   * {@link AbstractPage#execPageActivated()} , if a Exeption is thrown
   */
  @Test
  public void testPageActivatedException() {
    IExceptionHandlerService service = createMockExceptionHandlerService();
    final IPage testPage = new PageRuntimeExceptionOnActivated();
    testPage.pageActivatedNotify();
    verify(service, times(1)).handleException(any(ProcessingException.class));
  }

  /**
   * {@link AbstractPage#execPageDeactivated()}, if a ProcessingExeption is thrown
   */
  @Test
  public void testPageDeactivatedProcessingException() {
    IExceptionHandlerService service = createMockExceptionHandlerService();
    final IPage testPage = new PageExceptionOnDeactivated();
    testPage.pageDeactivatedNotify();
    verify(service, times(1)).handleException(any(ProcessingException.class));
  }

  /**
   * {@link AbstractPage#execPageDeactivated()}, if a Exeption is thrown
   */
  @Test
  public void testPageDeactivatedException() {
    IExceptionHandlerService service = createMockExceptionHandlerService();
    final IPage testPage = new PageRuntimeExceptionOnDeactivated();
    testPage.pageDeactivatedNotify();
    verify(service, times(1)).handleException(any(ProcessingException.class));
  }

  private IExceptionHandlerService createMockExceptionHandlerService() {
    IExceptionHandlerService svc = Mockito.mock(IExceptionHandlerService.class);
    m_serviceRegs = TestingUtility.registerServices(Activator.getDefault().getBundle(), TEST_SERVICE_RANKING, svc);
    return svc;
  }

  @ClassId(TEST_PAGE_CLASS_ID)
  class PageWithClassId extends AbstractPage {
  }

  class PageWithoutClassId extends AbstractPage {
  }

  class PageExceptionOnActivated extends AbstractPage {
    @Override
    protected void execPageActivated() throws ProcessingException {
      throw new ProcessingException();
    }
  }

  class PageRuntimeExceptionOnActivated extends AbstractPage {
    @Override
    protected void execPageActivated() throws ProcessingException {
      throw new RuntimeException();
    }
  }

  class PageExceptionOnDeactivated extends AbstractPage {
    @Override
    protected void execPageDeactivated() throws ProcessingException {
      throw new ProcessingException();
    }
  }

  class PageRuntimeExceptionOnDeactivated extends AbstractPage {
    @Override
    protected void execPageDeactivated() throws ProcessingException {
      throw new RuntimeException();
    }
  }

}
