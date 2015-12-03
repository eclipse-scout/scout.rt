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
package org.eclipse.scout.rt.shared.data.form.fixture;

import org.eclipse.scout.rt.shared.data.form.AbstractFormData;
import org.eclipse.scout.rt.shared.data.form.fields.AbstractValueFieldData;

public class TestFormData extends AbstractFormData {
  private static final long serialVersionUID = 1L;

  public TestFormData() {
  }

  public G1Box getG1Box() {
    return getFieldByClass(G1Box.class);
  }

  public G2Box getG2Box() {
    return getFieldByClass(G2Box.class);
  }

  public Text1 getText1() {
    return getFieldByClass(Text1.class);
  }

  public Text2 getText2() {
    return getFieldByClass(Text2.class);
  }

  public Text3 getText3() {
    return getFieldByClass(Text3.class);
  }

  public Text4 getText4() {
    return getFieldByClass(Text4.class);
  }

  public static class G1Box extends AbstractTestGroupBoxData {
    private static final long serialVersionUID = 1L;

    public G1Box() {
    }
  }

  public static class G2Box extends AbstractTestGroupBoxData {
    private static final long serialVersionUID = 1L;

    public G2Box() {
    }
  }

  public static class Text1 extends AbstractValueFieldData<String> {
    private static final long serialVersionUID = 1L;

    public Text1() {
    }
  }

  public static class Text2 extends AbstractValueFieldData<String> {
    private static final long serialVersionUID = 1L;

    public Text2() {
    }
  }

  public static class Text3 extends AbstractValueFieldData<String> {
    private static final long serialVersionUID = 1L;

    public Text3() {
    }
  }

  public static class Text4 extends AbstractValueFieldData<String> {
    private static final long serialVersionUID = 1L;

    public Text4() {
    }

    @Override
    public String getFieldId() {
      return "customId";
    }
  }
}
