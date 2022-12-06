/*
 * Copyright (c) 2010-2022 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.rt.client.ui.desktop.hybrid;

import org.eclipse.scout.rt.client.ui.desktop.hybrid.DummyForm.MainBox.GroupBox.DummyField;
import org.eclipse.scout.rt.client.ui.form.AbstractForm;
import org.eclipse.scout.rt.client.ui.form.fields.groupbox.AbstractGroupBox;
import org.eclipse.scout.rt.client.ui.form.fields.integerfield.AbstractIntegerField;
import org.eclipse.scout.rt.dataobject.mapping.AbstractDoEntityMapper;
import org.eclipse.scout.rt.dataobject.mapping.DoEntityMappings;
import org.eclipse.scout.rt.platform.ApplicationScoped;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.Order;
import org.eclipse.scout.rt.platform.classid.ClassId;

@ClassId("989880dd-f487-4316-b566-f6d4b185d985")
public class DummyForm extends AbstractForm {

  @Override
  protected String getConfiguredTitle() {
    return "Dummy";
  }

  public void exportData(DummyDo dummyDo) {
    BEANS.get(DummyFormMapper.class).toDo(this, dummyDo);
  }

  public void importData(DummyDo dummyDo) {
    BEANS.get(DummyFormMapper.class).fromDo(dummyDo, this);
  }

  public DummyField getDummyField() {
    return getFieldByClass(DummyField.class);
  }

  @Order(10)
  @ClassId("2810e557-12c2-428b-ad59-17b2bc76de44")
  public class MainBox extends AbstractGroupBox {

    @Order(10)
    @ClassId("dcaff435-4eff-43dc-b8d4-8683adce8b33")
    public class GroupBox extends AbstractGroupBox {

      @Order(10)
      @ClassId("d609ae93-c6cb-4978-9270-1257e1622f1d")
      public class DummyField extends AbstractIntegerField {

        @Override
        protected String getConfiguredLabel() {
          return "Dummy";
        }
      }
    }
  }

  @ApplicationScoped
  public static class DummyFormMapper extends AbstractDoEntityMapper<DummyDo, DummyForm> {
    @Override
    protected void initMappings(DoEntityMappings<DummyDo, DummyForm> mappings) {
      mappings
          .withHolder(DummyDo::dummy, DummyForm::getDummyField);
    }
  }
}
