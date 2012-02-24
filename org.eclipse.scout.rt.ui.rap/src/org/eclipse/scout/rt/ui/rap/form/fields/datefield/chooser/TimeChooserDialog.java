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
package org.eclipse.scout.rt.ui.rap.form.fields.datefield.chooser;

import java.util.Calendar;
import java.util.Date;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.rwt.lifecycle.WidgetUtil;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;

public class TimeChooserDialog extends Dialog {
  private static final long serialVersionUID = 1L;

  private static final String TIMECHOOSER_DIALOG_CUSTOM_VARIANT = "timechooser-dialog";

  public static final int TYPE_BACK_YEAR = 1 << 0;
  public static final int TYPE_BACK_MONTH = 1 << 1;
  public static final int TYPE_FOREWARD_MONTH = 1 << 2;
  public static final int TYPE_FOREWARD_YEAR = 1 << 3;

  private TimeChooser m_timeChooser;
  private Date m_returnTime = null;
  private Date m_displayDate;

  @Override
  protected int getShellStyle() {
    return SWT.NONE;
  }

  public TimeChooserDialog(Shell parentShell, Date date) {
    super(parentShell);
    setDisplayDate(date);
    setBlockOnOpen(false);
    create();
  }

  public TimeChooserDialog(Shell parentShell, Number number) {
    super(parentShell);
    setDisplayDate((Double) number);
    setBlockOnOpen(false);
    create();
  }

  private void setDisplayDate(Date date) {
    m_displayDate = date;
  }

  private void setDisplayDate(Double number) {
    if (number == null) {
      return;
    }

    // transform time of day in fractional representation (0.5 = noon)
    // into normal hour/minute representation
    Calendar c = Calendar.getInstance();
    int hours = (int) (number * 24);
    int mins = (int) Math.round((number * 24 - hours) * 60);
    c.set(0, 0, 0, hours, mins);
    // set time as date
    m_displayDate = c.getTime();
  }

  public void openTimeChooser(Control c) {
    showDialogFor(c);
  }

  public Date getReturnTime() {
    return m_returnTime;
  }

  public int showDialogFor(Control field) {
    // make sure that the popup fit into the application window.
    Rectangle appBounds = field.getDisplay().getBounds();
    Point absPrefPos = field.toDisplay(field.getSize().x - getShell().getSize().x, field.getSize().y);
    Rectangle prefBounds = new Rectangle(absPrefPos.x, absPrefPos.y, getShell().getSize().x, getShell().getSize().y);
    // horizontal correction
    if (prefBounds.x + prefBounds.width > appBounds.width) {
      prefBounds.x = appBounds.width - prefBounds.width;
    }
    // vertical correciton
    if (prefBounds.y + prefBounds.height > appBounds.height) {
      prefBounds.y = appBounds.height - prefBounds.height;
    }
    getShell().setLocation(prefBounds.x, prefBounds.y);
    int ret = this.open();
    return ret;
  }

  @Override
  protected Control createContents(Composite parent) {
    parent.setData(WidgetUtil.CUSTOM_VARIANT, TIMECHOOSER_DIALOG_CUSTOM_VARIANT);

    Composite rootArea = new Composite(parent, SWT.NONE);
    rootArea.setData(WidgetUtil.CUSTOM_VARIANT, TIMECHOOSER_DIALOG_CUSTOM_VARIANT);

    m_timeChooser = new TimeChooser(rootArea);
    m_timeChooser.addChangeListener(new ChangeListener() {
      @Override
      public void stateChanged(ChangeEvent e) {
        m_returnTime = m_timeChooser.getTime();
        getShell().getDisplay().asyncExec(new Runnable() {
          @Override
          public void run() {
            close();
          }
        });
      }
    });
    m_timeChooser.setTime(m_displayDate);

    // layout
    rootArea.setLayout(new GridLayout());
    GridData data = new GridData(SWT.FILL, SWT.FILL, true, true);
    rootArea.setLayoutData(data);
    return rootArea;
  }
}
