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
package org.eclipse.scout.rt.ui.swing.ext.calendar;

import java.awt.Color;
import java.awt.Font;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.Locale;

import javax.swing.InputVerifier;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import javax.swing.event.MouseInputAdapter;

import org.eclipse.scout.rt.ui.swing.ext.BorderLayoutEx;
import org.eclipse.scout.rt.ui.swing.ext.FlowLayoutEx;
import org.eclipse.scout.rt.ui.swing.ext.JHyperlink;
import org.eclipse.scout.rt.ui.swing.ext.JPanelEx;

/**
 * New analog/digital clock version of the time chooser widget.
 * 
 * @author imo
 */
public class TimeChooser {
  /**
   * command sent when time was changed by {@link #setTimeInMinutes(int)}
   */
  public static final String ACTION_SET_TIME = "setTime";
  /**
   * command sent when time was changed by clicking on clock
   */
  public static final String ACTION_CLOCK_CLICKED = "clockClicked";
  /**
   * command sent when time was changed by double clicking on clock
   */
  public static final String ACTION_CLOCK_DOUBLE_CLICKED = "clockDoubleClicked";
  /**
   * command sent when time was changed by entering digital time text
   */
  public static final String ACTION_TEXT_TYPED = "textTyped";
  /**
   * command sent when time was changed by entering digital time text
   */
  public static final String ACTION_AM_PM_SWITCHED = "amPmSwitched";

  private final Collection<ActionListener> listeners = new ArrayList<ActionListener>();
  private JPanel m_container;
  private JClock m_analogClock;
  private JTextField m_digitalClock;
  private JLabel m_amLabel;
  private JLabel m_pmLabel;
  private Font m_selectedFont;
  private Font m_defaultFont;
  /**
   * time in minutes
   */
  private int m_time;
  private Calendar m_referenceCal;
  //locale format
  private final boolean m_h24Mode;
  private final DateFormat m_timeFormat;

  public TimeChooser() {
    this(Locale.getDefault());
  }

  public TimeChooser(Locale locale) {
    DateFormat fmt = DateFormat.getTimeInstance(DateFormat.SHORT, locale);
    if (fmt instanceof SimpleDateFormat && ((SimpleDateFormat) fmt).toPattern().indexOf('a') >= 0) {
      String pat = ((SimpleDateFormat) fmt).toPattern();
      m_h24Mode = false;
      m_timeFormat = new SimpleDateFormat(pat.replace('a', ' ').trim());
    }
    else {
      m_h24Mode = true;
      m_timeFormat = fmt;
    }
    m_container = new JPanel();
    m_container.setBackground(UIManager.getColor("Calendar.date.background"));
    m_container.setLayout(new BorderLayoutEx());
    m_analogClock = new JClock(m_h24Mode);
    JPanel clockPanel = new JPanel(new FlowLayoutEx(FlowLayoutEx.CENTER, 0, 0));
    clockPanel.setBorder(new EmptyBorder(8, 8, 8, 8));
    clockPanel.add(m_analogClock);
    m_container.add(clockPanel, BorderLayoutEx.CENTER);
    MouseInputAdapter mouseHandler = new MouseInputAdapter() {
      @Override
      public void mouseMoved(MouseEvent e) {
        m_analogClock.setTemporaryTime(m_analogClock.getTimeAt(e.getPoint()));
      }

      @Override
      public void mouseDragged(MouseEvent e) {
        m_analogClock.setTemporaryTime(m_analogClock.getTimeAt(e.getPoint()));
      }

      @Override
      public void mouseReleased(MouseEvent e) {
        if (e.getButton() == MouseEvent.BUTTON1) {
          Rectangle r = m_analogClock.getBounds();
          r.x = 0;
          r.y = 0;
          if (r.contains(e.getPoint())) {
            setTimeInternalNoActionEvent(m_analogClock.getTemporaryTime() != null ? m_analogClock.getTemporaryTime() : m_analogClock.getTime());
            if (e.getClickCount() == 1) {
              fireActionEvent(ACTION_CLOCK_CLICKED);
            }
            else if (e.getClickCount() == 2) {
              fireActionEvent(ACTION_CLOCK_DOUBLE_CLICKED);
            }
          }
        }
      }

      @Override
      public void mouseExited(MouseEvent e) {
        m_analogClock.setTemporaryTime(null);
      }
    };
    m_analogClock.addMouseListener(mouseHandler);
    m_analogClock.addMouseMotionListener(mouseHandler);
    //
    m_digitalClock = new JTextField(4);
    m_digitalClock.setInputVerifier(new InputVerifier() {
      @Override
      public boolean verify(JComponent input) {
        try {
          m_digitalClock.setForeground(null);
          Integer time = parseTime(m_digitalClock.getText());
          if (time != null) {
            if (setTimeInternalNoActionEvent(time.intValue())) {
              fireActionEvent(ACTION_TEXT_TYPED);
            }
          }
        }
        catch (Exception pe) {
          m_digitalClock.setForeground(Color.red);
        }
        return true;
      }
    });
    //
    m_amLabel = new JHyperlink("AM");
    m_amLabel.setRequestFocusEnabled(false);
    if (isH24Mode()) {
      m_amLabel.setToolTipText("00:00 - 11:59");
    }
    else {
      m_amLabel.setToolTipText("0:00 AM - 11:59 AM");
    }
    m_amLabel.addMouseListener(new MouseAdapter() {
      @Override
      public void mouseReleased(MouseEvent e) {
        if (setTimeInternalNoActionEvent(m_time % (12 * 60))) {
          fireActionEvent(ACTION_AM_PM_SWITCHED);
        }
      }
    });
    m_pmLabel = new JHyperlink("PM");
    m_pmLabel.setRequestFocusEnabled(false);
    if (isH24Mode()) {
      m_pmLabel.setToolTipText("12:00 - 23:59");
    }
    else {
      m_pmLabel.setToolTipText("12:00 PM - 11:59 PM");
    }
    m_pmLabel.addMouseListener(new MouseAdapter() {
      @Override
      public void mouseReleased(MouseEvent e) {
        if (setTimeInternalNoActionEvent((m_time % (12 * 60)) + (12 * 60))) {
          fireActionEvent(ACTION_AM_PM_SWITCHED);
        }
      }
    });
    Font f = m_amLabel.getFont();
    m_selectedFont = new Font(f.getFamily(), Font.BOLD, f.getSize());
    m_defaultFont = new Font(f.getFamily(), Font.PLAIN, f.getSize());
    JPanelEx buttonPanel = new JPanelEx(new FlowLayoutEx(FlowLayoutEx.CENTER, 4, 0));
    buttonPanel.add(m_digitalClock);
    buttonPanel.add(m_amLabel);
    buttonPanel.add(m_pmLabel);
    m_container.add(buttonPanel, BorderLayoutEx.SOUTH);
    //
    //synth
    m_container.setName("Synth.TimeChooser");
    //initial value
    Calendar cal = Calendar.getInstance();
    setTimeInternalNoActionEvent(cal.get(Calendar.HOUR_OF_DAY) * 60 + cal.get(Calendar.MINUTE));
  }

  public void addActionListener(ActionListener l) {
    listeners.add(l);
  }

  public void removeActionListener(ActionListener l) {
    listeners.remove(l);
  }

  public JPanel getContainer() {
    return m_container;
  }

  public JLabel getAmLabel() {
    return m_amLabel;
  }

  public JLabel getPmLabel() {
    return m_pmLabel;
  }

  public JClock getClock() {
    return m_analogClock;
  }

  private void fireActionEvent(String command) {
    ActionEvent e = new ActionEvent(this, ActionEvent.ACTION_PERFORMED, command);
    for (ActionListener l : listeners) {
      l.actionPerformed(e);
    }
  }

  public int getTimeInMinutes() {
    return m_time;
  }

  public void setTimeInMinutes(int time) {
    if (setTimeInternalNoActionEvent(time)) {
      fireActionEvent(ACTION_SET_TIME);
    }
  }

  private boolean/*changed*/setTimeInternalNoActionEvent(int time) {
    int newTime = (time / 15) * 15;
    if (m_time != newTime) {
      m_time = newTime;
      updateAnalogClock(m_time);
      updateDigitalClock(m_time);
      updateAmPmLabels();
      return true;
    }
    return false;
  }

  public void finishEditing() {
    if (m_digitalClock.isFocusOwner()) {
      m_digitalClock.getInputVerifier().shouldYieldFocus(m_digitalClock);
    }
  }

  public void setDate(Date d) {
    Calendar c = Calendar.getInstance();
    if (d != null) {
      c.setTime(d);
    }
    m_referenceCal = c;
    setTimeInMinutes(c.get(Calendar.HOUR_OF_DAY) * 60 + c.get(Calendar.MINUTE));
  }

  public Date getDate() {
    Calendar c = m_referenceCal;
    if (m_referenceCal == null) {
      c = Calendar.getInstance();
    }
    int t = getTimeInMinutes();
    c.set(Calendar.HOUR_OF_DAY, (t / 60) % 24);
    c.set(Calendar.MINUTE, t % 60);
    c.set(Calendar.SECOND, 0);
    c.set(Calendar.MILLISECOND, 0);
    return c.getTime();
  }

  private boolean isH24Mode() {
    return m_h24Mode;
  }

  private Integer parseTime(String s) throws ParseException {
    if (s == null) return null;
    s = s.trim().toLowerCase();
    if (s.length() == 0) return null;
    boolean amSet = false;
    boolean pmSet = false;
    int h;
    int m;
    if (s.indexOf("am") >= 0) {
      amSet = true;
    }
    if (s.indexOf("pm") >= 0) {
      pmSet = true;
    }
    s = s.replace("am", "");
    s = s.replace("pm", "");
    s = s.trim();
    if (!s.matches("[0-9.:]+")) return null;
    String[] a = s.split("[:.]");
    if (a.length == 1) {
      if (!a[0].matches("[0-9]+")) return null;
      int i = Integer.parseInt(a[0]);
      if (a[0].length() <= 2) {
        h = i;
        m = 0;
      }
      else if (a[0].length() <= 4) {
        h = i / 100;
        m = i % 100;
      }
      else {
        return null;
      }
    }
    else if (a.length == 2) {
      if (!a[0].matches("[0-9]+")) return null;
      if (!a[1].matches("[0-9]+")) return null;
      h = Integer.parseInt(a[0]);
      m = Integer.parseInt(a[1]);
    }
    else {
      return null;
    }
    //
    if (h < 0 || h > 24) return null;
    if (m < 0 || m >= 60) return null;
    if (h == 24) h = 0;
    //
    if (!amSet && !pmSet) {
      if (h >= 13) {
        pmSet = true;
      }
      else if (isH24Mode()) {
        if (h >= 13) {
          pmSet = true;
        }
        else {
          amSet = true;
        }
      }
      else {
        if (m_analogClock.isAM()) {
          amSet = true;
        }
        else {
          pmSet = true;
        }
      }
    }
    //
    if (amSet) {
      return (h % 12) * 60 + m;
    }
    else {
      return ((h % 12) + 12) * 60 + m;
    }
  }

  private void updateAmPmLabels() {
    m_amLabel.setFont(m_analogClock.isAM() ? m_selectedFont : m_defaultFont);
    m_pmLabel.setFont(m_analogClock.isAM() ? m_defaultFont : m_selectedFont);
  }

  private void updateAnalogClock(int time) {
    m_analogClock.setAM(time < 12 * 60);
    m_analogClock.setTime(time);
  }

  private void updateDigitalClock(int time) {
    Calendar c = Calendar.getInstance();
    c.set(2000, 1, 1, time / 60, time % 60, 0);
    m_digitalClock.setText(m_timeFormat.format(c.getTime()));
  }
}
