/*******************************************************************************
 * Copyright (c) 2010-2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.client.ui.form.fields.integerfield;

import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.scout.rt.client.dto.FormData;
import org.eclipse.scout.rt.client.dto.FormData.DefaultSubtypeSdkCommand;
import org.eclipse.scout.rt.client.dto.FormData.SdkCommand;
import org.eclipse.scout.rt.client.testenvironment.TestEnvironmentClientSession;
import org.eclipse.scout.rt.client.ui.form.fields.browserfield.AbstractBrowserField;
import org.eclipse.scout.rt.platform.resource.BinaryResource;
import org.eclipse.scout.rt.platform.util.CollectionUtility;
import org.eclipse.scout.rt.testing.client.runner.ClientTestRunner;
import org.eclipse.scout.rt.testing.client.runner.RunWithClientSession;
import org.eclipse.scout.rt.testing.platform.runner.RunWithSubject;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(ClientTestRunner.class)
@RunWithSubject("default")
@RunWithClientSession(TestEnvironmentClientSession.class)
@FormData(value = AbstractBrowserFieldTestData.class, defaultSubtypeSdkCommand = DefaultSubtypeSdkCommand.CREATE, sdkCommand = SdkCommand.CREATE)
public class AbstractBrowserFieldTest extends AbstractBrowserField {

  @Test
  public void testImportFormData() {
    // location only
    AbstractBrowserFieldTestData formData = new AbstractBrowserFieldTestData();
    formData.setLocation("http://www.example.org");
    importFormFieldData(formData, false);

    Assert.assertEquals("http://www.example.org", getLocation());
    Assert.assertNull(getBinaryResource());
    Assert.assertTrue(CollectionUtility.isEmpty(getAttachments()));

    final BinaryResource resource = new BinaryResource("document.txt", "Welcome".getBytes(StandardCharsets.UTF_8));

    // resource only
    formData = new AbstractBrowserFieldTestData();
    formData.setBinaryResource(resource);
    importFormFieldData(formData, false);

    Assert.assertNull(getLocation());
    Assert.assertEquals(resource, getBinaryResource());
    Assert.assertTrue(CollectionUtility.isEmpty(getAttachments()));

    final Set<BinaryResource> attachments = new HashSet<>();
    attachments.add(new BinaryResource("attachment.txt", "Content".getBytes(StandardCharsets.UTF_8)));
    attachments.add(new BinaryResource("image.png", new byte[]{12, 43, 12, 71, 73, 12, 51}));

    // resource and attachment
    formData = new AbstractBrowserFieldTestData();
    formData.setBinaryResource(resource);
    formData.setAttachments(attachments);
    importFormFieldData(formData, false);

    Assert.assertNull(getLocation());
    Assert.assertEquals(resource, getBinaryResource());
    Assert.assertEquals(attachments, getAttachments());

    // set all three (not a valid case, but import form data will import was is provided)
    formData = new AbstractBrowserFieldTestData();
    formData.setLocation("http://www.example.org");
    formData.setBinaryResource(resource);
    formData.setAttachments(attachments);
    importFormFieldData(formData, false);

    Assert.assertEquals("http://www.example.org", getLocation());
    Assert.assertEquals(resource, getBinaryResource());
    Assert.assertEquals(attachments, getAttachments());
  }
}
