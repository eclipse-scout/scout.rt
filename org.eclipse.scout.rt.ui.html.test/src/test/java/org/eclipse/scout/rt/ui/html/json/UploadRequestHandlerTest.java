/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.ui.html.json;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.fileupload2.core.FileUploadException;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.BeanMetaData;
import org.eclipse.scout.rt.platform.IBean;
import org.eclipse.scout.rt.platform.resource.BinaryResource;
import org.eclipse.scout.rt.platform.resource.BinaryResources;
import org.eclipse.scout.rt.platform.security.MalwareScanner;
import org.eclipse.scout.rt.platform.security.RejectedResourceException;
import org.eclipse.scout.rt.platform.util.HexUtility;
import org.eclipse.scout.rt.server.commons.BufferedServletInputStream;
import org.eclipse.scout.rt.testing.platform.BeanTestingHelper;
import org.eclipse.scout.rt.ui.html.UiHtmlConfigProperties;
import org.eclipse.scout.rt.ui.html.res.IUploadable;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeBodyPart;
import jakarta.mail.internet.MimeMultipart;
import jakarta.servlet.http.HttpServletRequest;

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

  @Test
  public void testFileCount() throws IOException, FileUploadException, MessagingException {
    BeanTestingHelper testingHelper = BEANS.get(BeanTestingHelper.class);
    List<IBean<?>> mocked = new ArrayList<>();
    //limit upload file count to 10
    mocked.add(testingHelper.mockConfigProperty(UiHtmlConfigProperties.MaxUploadFileCountProperty.class, 10L));
    //disable malware scanner
    mocked.add(testingHelper.registerBean(new BeanMetaData(MalwareScanner.class, new MalwareScanner() {
      @Override
      public void scan(BinaryResource res) {
        //nop
      }
    })));
    IUploadable uploadable = () -> 1000000;
    Map<String, String> props = new HashMap<>();
    List<BinaryResource> result = new ArrayList<>();

    try {
      //0 files are ok
      new UploadRequestHandler().readUploadData(createUploadRequest(0), uploadable, props, result);
      //1 file is ok
      result.clear();
      new UploadRequestHandler().readUploadData(createUploadRequest(1), uploadable, props, result);
      //10 files are ok
      result.clear();
      new UploadRequestHandler().readUploadData(createUploadRequest(10), uploadable, props, result);
      //11 files fail
      result.clear();
      try {
        new UploadRequestHandler().readUploadData(createUploadRequest(11), uploadable, props, result);
        Assert.fail("must fail");
      }
      catch (RejectedResourceException e) {
        //expected
      }
    }
    finally {
      testingHelper.unregisterBeans(mocked);
    }
  }

  private static HttpServletRequest createUploadRequest(int fileCount) throws MessagingException, IOException {
    MimeMultipart multipart = new MimeMultipart("form-data");

    MimeBodyPart magicPart = new MimeBodyPart();
    magicPart.setDisposition("form-data; name=\"rowId\"");
    magicPart.setText("");
    multipart.addBodyPart(magicPart);

    for (int i = 1; i <= fileCount; i++) {
      MimeBodyPart filePart = new MimeBodyPart();
      filePart.setDisposition("form-data; name=\"files\"");
      filePart.setFileName("file" + i + ".txt");
      filePart.setText("Text " + i);
      multipart.addBodyPart(filePart);
    }

    ByteArrayOutputStream out = new ByteArrayOutputStream();
    multipart.writeTo(out);

    HttpServletRequest req = Mockito.mock(HttpServletRequest.class);
    Mockito.doReturn(multipart.getContentType()).when(req).getContentType();
    Mockito.doReturn(new BufferedServletInputStream(out.toByteArray())).when(req).getInputStream();
    return req;
  }
}
