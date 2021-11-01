/*
 * Copyright (c) 2010-2021 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.rt.ui.html.json;

import java.util.Arrays;
import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

import org.eclipse.scout.rt.platform.resource.BinaryResources;
import org.eclipse.scout.rt.platform.security.RejectedResourceException;
import org.eclipse.scout.rt.platform.util.HexUtility;
import org.eclipse.scout.rt.ui.html.res.IUploadable;
import org.junit.Assert;
import org.junit.Test;

public class UploadRequestHandlerTest {

  @Test
  public void testExtensionByName() {
    UploadRequestHandler h = new UploadRequestHandler();
    IUploadable uploadable = new IUploadable() {
      @Override
      public long getMaximumUploadSize() {
        return 0;
      }

      @Override
      public Collection<String> getAcceptedUploadFileExtensions() {
        return Arrays.asList("txt", "csv");
      }
    };
    Set<String> extSet = h.getValidFileExtensionsFor(uploadable, null);
    Assert.assertEquals("csv,txt", extSet.stream().sorted().collect(Collectors.joining(",")));
  }

  @Test
  public void testExtensionByMimeType() {
    UploadRequestHandler h = new UploadRequestHandler();
    IUploadable uploadable = new IUploadable() {
      @Override
      public long getMaximumUploadSize() {
        return 0;
      }

      @Override
      public Collection<String> getAcceptedUploadFileExtensions() {
        return Arrays.asList("text/plain", "text/foobar", "image/gif", "image/foobar", "image/jpeg");
      }
    };
    Set<String> extSet = h.getValidFileExtensionsFor(uploadable, null);
    Assert.assertEquals("gif,jfif,jpe,jpeg,jpg,pjp,pjpeg,txt", extSet.stream().sorted().collect(Collectors.joining(",")));
  }

  @Test
  public void testVerifyFileIntegrityWithEmptyNoname() {
    new UploadRequestHandler().verifyFileIntegrity(BinaryResources.create().withFilename("").withContent(HexUtility.decode("")).build());
  }

  @Test(expected = RejectedResourceException.class)
  public void testVerifyFileIntegrityWithEmptyGif() {
    new UploadRequestHandler().verifyFileIntegrity(BinaryResources.create().withFilename("x.jpg.gif").withContent(HexUtility.decode("")).build());
  }

  @Test
  public void testVerifyFileIntegrityWithGif() {
    new UploadRequestHandler().verifyFileIntegrity(BinaryResources.create().withFilename("x.jpg.gif").withContent(HexUtility.decode("474946383761")).build());
  }

  @Test(expected = RejectedResourceException.class)
  public void testVerifyFileIntegrityWithGifAsJpg() {
    new UploadRequestHandler().verifyFileIntegrity(BinaryResources.create().withFilename("x.jpg").withContent(HexUtility.decode("474946383761")).build());
  }

  @Test()
  public void testVerifyFileIntegrityWithUnknownExtAsGif() {
    new UploadRequestHandler().verifyFileIntegrity(BinaryResources.create().withFilename("x.foobar").withContent(HexUtility.decode("474946383761")).build());
  }

  @Test(expected = RejectedResourceException.class)
  public void testVerifyFileIntegrityWithNonImageExtAsGif() {
    new UploadRequestHandler().verifyFileIntegrity(BinaryResources.create().withFilename("x.foobar.mov").withContent(HexUtility.decode("474946383761")).build());
  }
}
