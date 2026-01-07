/*
 * Copyright (c) 2010, 2026 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.client.ui.form.fields.browserfield;

import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.eclipse.scout.rt.client.dto.FormData;
import org.eclipse.scout.rt.client.dto.FormData.DefaultSubtypeSdkCommand;
import org.eclipse.scout.rt.client.dto.FormData.SdkCommand;
import org.eclipse.scout.rt.client.testenvironment.TestEnvironmentClientSession;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.resource.BinaryResource;
import org.eclipse.scout.rt.platform.util.CollectionUtility;
import org.eclipse.scout.rt.security.csp.BlockAllContentSecurityPolicy;
import org.eclipse.scout.rt.security.csp.ContentSecurityPolicy;
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
    formData.setLocation("https://www.example.org");
    importFormFieldData(formData, false);

    Assert.assertEquals("https://www.example.org", getLocation());
    Assert.assertNull(getBinaryResource());
    Assert.assertTrue(CollectionUtility.isEmpty(getAttachments()));

    String fileName = "document.txt";
    final BinaryResource resource = new BinaryResource(fileName, "Welcome".getBytes(StandardCharsets.UTF_8));

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

    // resource and attachment with CSP
    formData = new AbstractBrowserFieldTestData();
    formData.setBinaryResource(resource);
    formData.setAttachments(attachments);
    ContentSecurityPolicy defaultPolicy = BEANS.get(BlockAllContentSecurityPolicy.class);
    formData.putContentSecurityPolicy(null /* default policy */, defaultPolicy);
    ContentSecurityPolicy documentPolicy = BEANS.get(BlockAllContentSecurityPolicy.class).withStyleSrc(ContentSecurityPolicy.EXPRESSION_SELF);
    formData.putContentSecurityPolicy(fileName, documentPolicy);
    importFormFieldData(formData, false);

    Assert.assertNull(getLocation());
    Assert.assertEquals(resource, getBinaryResource());
    Assert.assertEquals(attachments, getAttachments());
    Assert.assertSame(defaultPolicy, getContentSecurityPolicy(null));
    Assert.assertSame(documentPolicy, getContentSecurityPolicy(fileName));

    // set all three (not a valid case, but import form data will import what is provided)
    formData = new AbstractBrowserFieldTestData();
    formData.setLocation("https://www.example.org");
    formData.setBinaryResource(resource);
    formData.setAttachments(attachments);
    importFormFieldData(formData, false);

    Assert.assertEquals("https://www.example.org", getLocation());
    Assert.assertEquals(resource, getBinaryResource());
    Assert.assertEquals(attachments, getAttachments());
  }

  @Test
  public void testContentSecurityPolicy() {
    BlockAllContentSecurityPolicy defaultPolicy = BEANS.get(BlockAllContentSecurityPolicy.class);
    ContentSecurityPolicy documentPolicy = BEANS.get(BlockAllContentSecurityPolicy.class).withStyleSrc(ContentSecurityPolicy.EXPRESSION_SELF);
    String documentFileName = "document.html";
    String oldFileName = "old.html";

    setContentSecurityPolicy(defaultPolicy);
    putContentSecurityPolicy(documentFileName, documentPolicy);
    putContentSecurityPolicy(oldFileName, null);

    Map<String, ContentSecurityPolicy> policies = getContentSecurityPolicies();
    Assert.assertEquals(3, policies.size());
    Assert.assertSame(documentPolicy, policies.get(documentFileName));
    Assert.assertSame(defaultPolicy, policies.get(null));
    Assert.assertNull(policies.get(oldFileName));
    Assert.assertSame(documentPolicy, getContentSecurityPolicy(documentFileName));
    Assert.assertSame(defaultPolicy, getContentSecurityPolicy(null));
  }

  @Test
  public void testContentSecurityPolicyEmpty() {
    AbstractBrowserField field = new AbstractBrowserField() {
    };
    field.setContentSecurityPolicy(BEANS.get(BlockAllContentSecurityPolicy.class));
    Assert.assertNotNull(field.getContentSecurityPoliciesInternal());

    field.setContentSecurityPolicy(null);
    Assert.assertNull(field.getContentSecurityPoliciesInternal());

    field.setContentSecurityPolicy(BEANS.get(BlockAllContentSecurityPolicy.class));
    field.putContentSecurityPolicy("doc.html", BEANS.get(BlockAllContentSecurityPolicy.class));
    Assert.assertNotNull(field.getContentSecurityPoliciesInternal());

    field.setContentSecurityPolicy(null);
    Assert.assertNotNull(field.getContentSecurityPoliciesInternal());
    field.putContentSecurityPolicy("doc.html", null); // last policy removed
    Assert.assertNull(field.getContentSecurityPoliciesInternal());
  }
}
