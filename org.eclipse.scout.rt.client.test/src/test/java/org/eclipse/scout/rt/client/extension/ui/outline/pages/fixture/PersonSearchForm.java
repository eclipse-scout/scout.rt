/*******************************************************************************
 * Copyright (c) 2010-2016 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.client.extension.ui.outline.pages.fixture;

import org.eclipse.scout.rt.client.extension.ui.outline.pages.fixture.PersonSearchForm.MainBox.BottomBox;
import org.eclipse.scout.rt.client.extension.ui.outline.pages.fixture.PersonSearchForm.MainBox.BottomBox.StreetField;
import org.eclipse.scout.rt.client.extension.ui.outline.pages.fixture.PersonSearchForm.MainBox.TopBox;
import org.eclipse.scout.rt.client.extension.ui.outline.pages.fixture.PersonSearchForm.MainBox.TopBox.NameField;
import org.eclipse.scout.rt.client.extension.ui.outline.pages.fixture.PersonSearchForm.MainBox.TopBox.SalutationField;
import org.eclipse.scout.rt.client.ui.desktop.outline.pages.AbstractSearchForm;
import org.eclipse.scout.rt.client.ui.form.fields.groupbox.AbstractGroupBox;
import org.eclipse.scout.rt.client.ui.form.fields.stringfield.AbstractStringField;
import org.eclipse.scout.rt.platform.Order;

/**
 * @since 6.0
 */
public class PersonSearchForm extends AbstractSearchForm {

  public MainBox getMainBox() {
    return (MainBox) getRootGroupBox();
  }

  public TopBox getTopBox() {
    return getFieldByClass(TopBox.class);
  }

  public SalutationField getSalutationField() {
    return getFieldByClass(SalutationField.class);
  }

  public NameField getNameField() {
    return getFieldByClass(NameField.class);
  }

  public BottomBox getBottomBox() {
    return getFieldByClass(BottomBox.class);
  }

  public StreetField getStreetField() {
    return getFieldByClass(StreetField.class);
  }

  public class MainBox extends AbstractGroupBox {

    @Order(10)
    public class TopBox extends AbstractGroupBox {

      @Order(0)
      public class SalutationField extends AbstractStringField {
      }

      @Order(5)
      public class NameField extends AbstractStringField {
      }
    }

    @Order(20)
    public class BottomBox extends AbstractGroupBox {

      @Order(10)
      public class StreetField extends AbstractStringField {
      }
    }
  }
}
