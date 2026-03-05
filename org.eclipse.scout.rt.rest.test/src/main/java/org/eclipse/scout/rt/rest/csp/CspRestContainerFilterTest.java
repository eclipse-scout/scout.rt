/*
 * Copyright (c) 2010, 2026 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.rest.csp;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import jakarta.ws.rs.container.ContainerResponseContext;

import org.junit.Assert;
import org.junit.Test;

public class CspRestContainerFilterTest {
  @Test
  public void testIsAttachment() {
    assertIsAttachment(true, "attachment");
    assertIsAttachment(true, "attachment; filename=\"file name.jpg\"");
    assertIsAttachment(true, "attachment; filename*=UTF-8''file%20name.jpg");
    assertIsAttachment(true, "filename*=UTF-8''file%20name.jpg; attachment");
    assertIsAttachment(false, "filename*=UTF-8''file%20name.jpg; attachment-");
    assertIsAttachment(false, "inline");
    assertIsAttachment(false, "");
    assertIsAttachment(false, "  ");
    assertIsAttachment(false, null);
  }

  protected void assertIsAttachment(boolean expectation, String header) {
    ContainerResponseContext context = mock(ContainerResponseContext.class);
    when(context.getHeaderString(eq("Content-Disposition"))).thenReturn(header);
    Assert.assertEquals(expectation, new CspRestContainerFilter().isAttachment(context));
  }
}
