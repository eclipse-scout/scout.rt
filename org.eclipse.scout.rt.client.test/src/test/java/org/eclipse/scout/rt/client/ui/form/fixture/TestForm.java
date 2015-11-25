/*******************************************************************************
 * Copyright (c) 2013 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.client.ui.form.fixture;

import org.eclipse.scout.commons.annotations.FormData;
import org.eclipse.scout.commons.annotations.FormData.SdkCommand;
import org.eclipse.scout.commons.annotations.Order;
import org.eclipse.scout.rt.client.ui.form.AbstractForm;
import org.eclipse.scout.rt.client.ui.form.AbstractFormHandler;
import org.eclipse.scout.rt.client.ui.form.fields.button.AbstractCancelButton;
import org.eclipse.scout.rt.client.ui.form.fields.button.AbstractOkButton;
import org.eclipse.scout.rt.client.ui.form.fields.groupbox.AbstractGroupBox;
import org.eclipse.scout.rt.client.ui.form.fields.stringfield.AbstractStringField;
import org.eclipse.scout.rt.client.ui.form.fixture.TestForm.MainBox.CancelButton;
import org.eclipse.scout.rt.client.ui.form.fixture.TestForm.MainBox.G1Box;
import org.eclipse.scout.rt.client.ui.form.fixture.TestForm.MainBox.G2Box;
import org.eclipse.scout.rt.client.ui.form.fixture.TestForm.MainBox.G2Box.Text3Field;
import org.eclipse.scout.rt.client.ui.form.fixture.TestForm.MainBox.G3Box;
import org.eclipse.scout.rt.client.ui.form.fixture.TestForm.MainBox.G3Box.G4Box;
import org.eclipse.scout.rt.client.ui.form.fixture.TestForm.MainBox.OkButton;
import org.eclipse.scout.rt.client.ui.form.fixture.TestForm.MainBox.Text1Field;
import org.eclipse.scout.rt.client.ui.form.fixture.TestForm.MainBox.Text4Field;
import org.eclipse.scout.rt.shared.data.form.fixture.TestFormData;

@FormData(value = TestFormData.class, sdkCommand = SdkCommand.USE)
public class TestForm extends AbstractForm {

  public TestForm() {
    super();
  }

  public CancelButton getCancelButton() {
    return getFieldByClass(CancelButton.class);
  }

  public void startNew() {
    startInternal(new NewHandler());
  }

  public G1Box getG1Box() {
    return getFieldByClass(G1Box.class);
  }

  public G2Box getG2Box() {
    return getFieldByClass(G2Box.class);
  }

  public G3Box getG3Box() {
    return getFieldByClass(G3Box.class);
  }

  public G4Box getG4Box() {
    return getFieldByClass(G4Box.class);
  }

  public MainBox getMainBox() {
    return getFieldByClass(MainBox.class);
  }

  public OkButton getOkButton() {
    return getFieldByClass(OkButton.class);
  }

  public Text3Field getText3Field() {
    return getFieldByClass(Text3Field.class);
  }

  public Text1Field getText1Field() {
    return getFieldByClass(Text1Field.class);
  }

  public Text4Field getText4Field() {
    return getFieldByClass(Text4Field.class);
  }

  public G3Box.G4Box.Text2Field getG3G4Text2Field() {
    return getFieldByClass(G3Box.G4Box.Text2Field.class);
  }

  @Order(10)
  public class MainBox extends AbstractGroupBox {

    @Order(10)
    public class Text4Field extends AbstractStringField {
      @Override
      public String getFieldId() {
        return "customId";
      }
    }

    @Order(10)
    public class Text1Field extends AbstractStringField {
    }

    @Order(10)
    public class G1Box extends AbstractTestGroupBox {
    }

    @Order(20)
    public class G2Box extends AbstractTestGroupBox {

      @Order(10)
      public class Text3Field extends AbstractStringField {
      }
    }

    @Order(25)
    public class G3Box extends AbstractGroupBox {

      @Order(20)
      public class G4Box extends AbstractGroupBox {

        @Order(20)
        public class Text2Field extends AbstractStringField {
        }

      }
    }

    @Order(30)
    public class OkButton extends AbstractOkButton {
    }

    @Order(40)
    public class CancelButton extends AbstractCancelButton {
    }
  }

  public class NewHandler extends AbstractFormHandler {
  }
}
