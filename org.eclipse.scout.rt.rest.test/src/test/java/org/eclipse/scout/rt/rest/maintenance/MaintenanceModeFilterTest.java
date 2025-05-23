/*
 * Copyright (c) 2010, 2025 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.rest.maintenance;

import static org.mockito.Mockito.*;

import java.io.IOException;

import jakarta.ws.rs.container.ContainerRequestContext;

import org.eclipse.scout.rt.testing.platform.runner.PlatformTestRunner;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Test for {@link MaintenanceModeFilter}
 */
@RunWith(PlatformTestRunner.class)
public class MaintenanceModeFilterTest {

  private MaintenanceModeFilter m_filter;

  @Before
  public void initializeFilter() throws IOException {
    m_filter = mock(MaintenanceModeFilter.class);
    doCallRealMethod().when(m_filter).filter(any());
  }

  @Test
  public void testMaintenanceModeRegular() throws IOException {
    testFilterInternal(true, false, true);
  }

  @Test
  public void testMaintenanceModeOverrideByPermission() throws IOException {
    testFilterInternal(true, true, false);
  }

  @Test
  public void testNoMaintenanceMode() throws IOException {
    testFilterInternal(false, false, false);
    testFilterInternal(false, true, false);
  }

  protected void testFilterInternal(boolean isMaintenanceMode, boolean isMaintenanceModeLoginPermissionGranted, boolean expectCancel) throws IOException {
    doReturn(isMaintenanceMode).when(m_filter).isMaintenanceMode();
    doReturn(isMaintenanceModeLoginPermissionGranted).when(m_filter).isMaintenanceModeLoginPermissionGranted();

    ContainerRequestContext requestContext = mock(ContainerRequestContext.class);

    m_filter.filter(requestContext);

    if (expectCancel) {
      verify(requestContext, times(1)).abortWith(any());
    }
    else {
      verifyNoInteractions(requestContext);
    }
  }
}
