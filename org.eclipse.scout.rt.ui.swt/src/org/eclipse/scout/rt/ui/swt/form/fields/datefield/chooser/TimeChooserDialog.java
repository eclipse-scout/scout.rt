/*******************************************************************************
 * Copyright (c) 2010 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.ui.swt.form.fields.datefield.chooser;

import java.util.Calendar;
import java.util.Date;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.scout.rt.ui.swt.ISwtEnvironment;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ShellAdapter;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;

public class TimeChooserDialog extends Dialog {

  public static final int TYPE_BACK_YEAR = 1 << 0;
  public static final int TYPE_BACK_MONTH = 1 << 1;
  public static final int TYPE_FOREWARD_MONTH = 1 << 2;
  public static final int TYPE_FOREWARD_YEAR = 1 << 3;

  private TimeChooserContent m_timeChooserContent;
  private Date m_returnDate = null;
  private Date m_displayDate;
  private final ISwtEnvironment m_environment;

  @Override
  protected int getShellStyle() {
    return SWT.NONE;
  }

  public TimeChooserDialog(Shell parentShell, Date date, ISwtEnvironment environment) {
    super(parentShell);
    m_environment = environment;
    setDisplayDate(date);
    setBlockOnOpen(true);
  }

  public TimeChooserDialog(Shell parentShell, Number number, ISwtEnvironment environment) {
    super(parentShell);
    m_environment = environment;
    setDisplayDate((Double) number);
    setBlockOnOpen(true);
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

  public Date openDateChooser(Control c) {
    showDialogFor(c);
    return m_returnDate;

  }

  /**
   * @see ch.post.pf.gui.ocp.wt.ext.dialogs.OcpDialog#showDialogFor(org.eclipse.swt.widgets.Control)
   */
  public int showDialogFor(Control field) {
    create();
    getShell().addShellListener(new ShellAdapter() {
      @Override
      public void shellDeactivated(ShellEvent e) {
        close();
      }
    });
    // make sure that the popup fit into the application window.
    Rectangle appBounds = getEnvironment().getDisplay().getBounds();
    Point absPrefPos = field.toDisplay(field.getSize().x - getShell().getSize().x, field
        .getSize().y);
    Rectangle prefBounds = new Rectangle(absPrefPos.x, absPrefPos.y, getShell().getSize().x,
        getShell().getSize().y);
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

    Composite rootArea = new Composite(parent, SWT.NONE);
    m_timeChooserContent = new TimeChooserContent(rootArea, m_displayDate, getEnvironment());
    m_timeChooserContent.addTimeChangedListener(new AbstractDateSelectionListener() {
      @Override
      public void dateChanged(DateSelectionEvent e) {
        m_returnDate = (Date) e.getData();
        getShell().getDisplay().asyncExec(new Runnable() {
          @Override
          public void run() {
            close();
          }
        });
      }
    });
    m_timeChooserContent.setDate(m_displayDate);
    // layout
    rootArea.setLayout(new FormLayout());
    FormData data = new FormData();
    data.top = new FormAttachment(0, 0);
    data.left = new FormAttachment(0, 0);
    data.right = new FormAttachment(100, 0);
    data.bottom = new FormAttachment(100, 0);
    m_timeChooserContent.setLayoutData(data);
    return rootArea;
  }

  public ISwtEnvironment getEnvironment() {
    return m_environment;
  }

}
