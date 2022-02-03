/*
 * Copyright (c) 2010-2018 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.rt.client.ui.form.fields;

import org.eclipse.scout.rt.client.testenvironment.TestEnvironmentClientSession;
import org.eclipse.scout.rt.client.ui.basic.tree.ITree;
import org.eclipse.scout.rt.client.ui.form.fields.DataModelFormFieldEnabledTest.P_BoxWithComposer.ComposerField;
import org.eclipse.scout.rt.client.ui.form.fields.composer.AbstractComposerField;
import org.eclipse.scout.rt.client.ui.form.fields.groupbox.AbstractGroupBox;
import org.eclipse.scout.rt.testing.client.runner.ClientTestRunner;
import org.eclipse.scout.rt.testing.client.runner.RunWithClientSession;
import org.eclipse.scout.rt.testing.platform.runner.RunWithSubject;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(ClientTestRunner.class)
@RunWithSubject("default")
@RunWithClientSession(TestEnvironmentClientSession.class)
public class DataModelFormFieldEnabledTest {

  @Test
  public void testComposerInheritance() {
    P_BoxWithComposer box = new P_BoxWithComposer();
    ComposerField composerField = box.getFieldByClass(ComposerField.class);
    ITree tree = composerField.getTree();
    box.setEnabled(false);

    Assert.assertFalse(box.isEnabled());
    Assert.assertFalse(composerField.isEnabledIncludingParents());
    Assert.assertTrue(composerField.isEnabled());
    Assert.assertTrue(tree.isEnabled());
    Assert.assertFalse(tree.isEnabledIncludingParents());
  }

  public static class P_BoxWithComposer extends AbstractGroupBox {
    public class ComposerField extends AbstractComposerField {
    }
  }
}
