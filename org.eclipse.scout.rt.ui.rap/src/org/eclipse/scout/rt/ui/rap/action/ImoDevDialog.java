/*******************************************************************************
 * Copyright (c) 2011 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 *******************************************************************************/
package org.eclipse.scout.rt.ui.rap.action;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.scout.rt.ui.rap.ext.ButtonEx;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

/**
 * <h3>ImoDevDialog</h3> ...
 * 
 * @author imo
 * @since 3.7.0 June 2011
 */
public class ImoDevDialog extends Dialog {
  private static final long serialVersionUID = 1L;

  private Listener m_anyListener = new Listener() {
    private static final long serialVersionUID = 1L;

    @Override
    public void handleEvent(Event e) {
      System.out.println("Event " + e.widget + " " + toRWTConstant(e.type) + " x=" + e.x + " y=" + e.y + " keyCode=" + e.keyCode + " char=" + e.character + " time=" + e.time);
    }
  };

  public ImoDevDialog() {
    super(new Shell());
  }

  public static String toRWTConstant(int i) {
    try {
      for (Field f : SWT.class.getFields()) {
        int m = Modifier.PUBLIC | Modifier.STATIC | Modifier.FINAL;
        if ((f.getModifiers() & m) == m && f.getType() == int.class && new Integer(i).equals(f.get(null))) {
          return f.getName();
        }
      }
    }
    catch (Throwable t) {
      //
    }
    return "type=" + i;
  }

  protected void addListeners(Control c) {
    c.addListener(SWT.MouseDown, m_anyListener);
    c.addListener(SWT.MouseUp, m_anyListener);
    c.addListener(SWT.MouseDoubleClick, m_anyListener);
    c.addListener(SWT.KeyDown, m_anyListener);
    c.addListener(SWT.KeyUp, m_anyListener);
    c.addListener(SWT.Traverse, m_anyListener);
    c.addListener(SWT.FocusIn, m_anyListener);
    c.addListener(SWT.FocusOut, m_anyListener);
    c.addListener(SWT.Selection, m_anyListener);
  }

  @Override
  public void create() {
    setShellStyle(SWT.DIALOG_TRIM | SWT.RESIZE | SWT.MAX | SWT.APPLICATION_MODAL);
    super.create();
  }

  @Override
  protected Control createContents(Composite parent) {
    super.createContents(parent);
    //
    Text text1 = new Text(parent, SWT.BORDER);
    addListeners(text1);
    text1.setText("value 1");
    Text text2 = new Text(parent, SWT.BORDER);
    addListeners(text2);
    text2.setText("value 2");
    Button button1 = new Button(parent, SWT.BORDER);
    button1.setText("Button");
    addListeners(button1);
    ButtonEx button2 = new ButtonEx(parent, SWT.BORDER);
    button2.setText("ButtonEx");
    addListeners(button2);
    return parent;
  }

}
