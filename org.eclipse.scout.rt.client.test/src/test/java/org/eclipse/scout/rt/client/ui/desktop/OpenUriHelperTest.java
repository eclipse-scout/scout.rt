/*
 * Copyright (c) 2010, 2026 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.client.ui.desktop;

import static org.junit.Assert.assertEquals;

import org.eclipse.scout.rt.client.testenvironment.TestEnvironmentClientSession;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.resource.BinaryResource;
import org.eclipse.scout.rt.platform.resource.BinaryResources;
import org.eclipse.scout.rt.platform.resource.MimeType;
import org.eclipse.scout.rt.testing.client.runner.ClientTestRunner;
import org.eclipse.scout.rt.testing.client.runner.RunWithClientSession;
import org.eclipse.scout.rt.testing.platform.runner.RunWithSubject;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(ClientTestRunner.class)
@RunWithSubject("default")
@RunWithClientSession(TestEnvironmentClientSession.class)
public class OpenUriHelperTest {
  protected OpenUriHelper m_openUriHelper = BEANS.get(OpenUriHelper.class);

  @Test
  public void testGetOpenUriActionForResource() {
    Assert.assertEquals(OpenUriAction.NEW_WINDOW, m_openUriHelper.getOpenUriActionForResource(createBinaryResourcesWithMimeType(MimeType.PNG)));
    assertEquals(OpenUriAction.NEW_WINDOW, m_openUriHelper.getOpenUriActionForResource(createBinaryResourcesWithMimeType(MimeType.JPG)));
    assertEquals(OpenUriAction.NEW_WINDOW, m_openUriHelper.getOpenUriActionForResource(createBinaryResourcesWithMimeType(MimeType.JPEG)));
    assertEquals(OpenUriAction.NEW_WINDOW, m_openUriHelper.getOpenUriActionForResource(createBinaryResourcesWithMimeType(MimeType.GIF)));
    assertEquals(OpenUriAction.NEW_WINDOW, m_openUriHelper.getOpenUriActionForResource(createBinaryResourcesWithMimeType(MimeType.BMP)));
    assertEquals(OpenUriAction.NEW_WINDOW, m_openUriHelper.getOpenUriActionForResource(createBinaryResourcesWithMimeType(MimeType.AVI)));
    assertEquals(OpenUriAction.NEW_WINDOW, m_openUriHelper.getOpenUriActionForResource(createBinaryResourcesWithMimeType(MimeType.AVIF)));
    assertEquals(OpenUriAction.NEW_WINDOW, m_openUriHelper.getOpenUriActionForResource(createBinaryResourcesWithMimeType(MimeType.MP4)));
    assertEquals(OpenUriAction.NEW_WINDOW, m_openUriHelper.getOpenUriActionForResource(createBinaryResourcesWithMimeType(MimeType.M2V)));
    assertEquals(OpenUriAction.DOWNLOAD, m_openUriHelper.getOpenUriActionForResource(createBinaryResourcesWithMimeType(MimeType.ZIP)));
    assertEquals(OpenUriAction.DOWNLOAD, m_openUriHelper.getOpenUriActionForResource(createBinaryResourcesWithMimeType(MimeType.PDF)));

    // Must be 'DOWNLOAD' for security reasons!
    assertEquals(OpenUriAction.DOWNLOAD, m_openUriHelper.getOpenUriActionForResource(createBinaryResourcesWithMimeType(MimeType.SVG)));
    assertEquals(OpenUriAction.DOWNLOAD, m_openUriHelper.getOpenUriActionForResource(createBinaryResourcesWithMimeType(MimeType.HTM)));
    assertEquals(OpenUriAction.DOWNLOAD, m_openUriHelper.getOpenUriActionForResource(createBinaryResourcesWithMimeType(MimeType.HTML)));
    assertEquals(OpenUriAction.DOWNLOAD, m_openUriHelper.getOpenUriActionForResource(createBinaryResourcesWithMimeType(MimeType.JS)));
  }

  protected BinaryResource createBinaryResourcesWithMimeType(MimeType mimeType) {
    return BinaryResources.create()
        .withFilename("mock." + mimeType.getFileExtension())
        .withContentType(mimeType.getType())
        .build();
  }
}

