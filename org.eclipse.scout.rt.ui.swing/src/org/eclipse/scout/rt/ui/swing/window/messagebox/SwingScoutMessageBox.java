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
package org.eclipse.scout.rt.ui.swing.window.messagebox;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.Icon;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.basic.BasicHTML;
import javax.swing.text.View;

import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.rt.client.ui.messagebox.IMessageBox;
import org.eclipse.scout.rt.client.ui.messagebox.MessageBoxEvent;
import org.eclipse.scout.rt.client.ui.messagebox.MessageBoxListener;
import org.eclipse.scout.rt.ui.swing.SwingUtility;
import org.eclipse.scout.rt.ui.swing.basic.SwingScoutComposite;
import org.eclipse.scout.rt.ui.swing.ext.BorderLayoutEx;
import org.eclipse.scout.rt.ui.swing.ext.FlowLayoutEx;
import org.eclipse.scout.rt.ui.swing.ext.JDialogEx;
import org.eclipse.scout.rt.ui.swing.ext.JLabelEx;
import org.eclipse.scout.rt.ui.swing.ext.JPanelEx;
import org.eclipse.scout.rt.ui.swing.form.fields.AbstractLayoutManager2;

public class SwingScoutMessageBox extends SwingScoutComposite<IMessageBox> implements ISwingScoutMessageBox {

  private static final int MAX_WIDTH = 800;
  private static final int MAX_HEIGHT = 600;

  private static final int VERTICAL_PADDING = 12;
  private static final int HORIZONTAL_PADDING = 12;
  private static final int EMPTY_BORDER_PADDING = 12;

  private static final int COLOR_LIGHT_GRAY = 0xf2f2f2;

  private P_ScoutMessageBoxListener m_scoutMessageBoxListener;
  private Window m_swingParent;
  private JDialogEx m_swingDialog;
  private JButton m_swingButtonYes;
  private JButton m_swingButtonNo;
  private JButton m_swingButtonCancel;
  private JButton m_swingButtonCopy;

  public SwingScoutMessageBox(Window swingParent) {
    super();
    m_swingParent = swingParent;
    while (swingParent != null && !(swingParent instanceof Dialog || swingParent instanceof Frame)) {
      m_swingParent = SwingUtilities.getWindowAncestor(swingParent);
    }
  }

  @Override
  protected void initializeSwing() {
    if (m_swingParent instanceof Dialog) {
      m_swingDialog = new JDialogEx((Dialog) m_swingParent);
    }
    else {
      m_swingDialog = new JDialogEx((Frame) m_swingParent);
    }
    m_swingDialog.setModal(true);
    String title = getScoutMessageBox().getTitle();
    m_swingDialog.setTitle(title);
    /**
     * WORKAROUND AWT doesn't show a dialog icon if dialog is not resizeable
     */
    m_swingDialog.setResizable(true);
    m_swingDialog.addWindowListener(new P_SwingWindowListener());
    // content
    JPanel contentPane = (JPanel) m_swingDialog.getContentPane();
    contentPane.setLayout(new P_Layout());
    contentPane.setBackground(new Color(COLOR_LIGHT_GRAY));
    if (getScoutMessageBox().getIntroText() != null) {
      String s = getScoutMessageBox().getIntroText();
      JPanel labelPanel = new JPanelEx(new FlowLayoutEx(FlowLayoutEx.LEFT));
      labelPanel.setBorder(new EmptyBorder(VERTICAL_PADDING, HORIZONTAL_PADDING, VERTICAL_PADDING, HORIZONTAL_PADDING));
      labelPanel.setBackground(Color.white);
      labelPanel.setOpaque(true);
      JLabelEx label = new JLabelEx();
      s = processTextOnLabel(s, label, getScoutMessageBox().isHtmlEnabled());
      label.setText(s);
      ensureProperDimension(label, s);
      labelPanel.add(label);
      contentPane.add(BorderLayout.NORTH, labelPanel);
    }
    if (getScoutMessageBox().getActionText() != null) {
      String s = getScoutMessageBox().getActionText();
      JPanel labelPanel = new JPanelEx(new FlowLayoutEx(FlowLayoutEx.LEFT));
      labelPanel.setBorder(new CompoundBorder(new P_TopSeparatorBorder(), new EmptyBorder(VERTICAL_PADDING, HORIZONTAL_PADDING, VERTICAL_PADDING, HORIZONTAL_PADDING)));
      labelPanel.setOpaque(false);
      JLabelEx label = new JLabelEx();
      s = processTextOnLabel(s, label, getScoutMessageBox().isHtmlEnabled());
      label.setText(s);
      ensureProperDimension(label, s);
      labelPanel.add(label);
      contentPane.add(BorderLayout.CENTER, labelPanel);
    }
    // buttons
    JPanel buttonPanel = new JPanelEx(new FlowLayoutEx(FlowLayoutEx.RIGHT));
    buttonPanel.setBorder(new CompoundBorder(new P_TopSeparatorBorder(), new EmptyBorder(EMPTY_BORDER_PADDING, EMPTY_BORDER_PADDING, EMPTY_BORDER_PADDING, EMPTY_BORDER_PADDING)));
    buttonPanel.setOpaque(false);
    JButton defaultButton = null;
    if (getScoutMessageBox().getYesButtonText() != null) {
      m_swingButtonYes = createButton(getScoutMessageBox().getYesButtonText(), UIManager.getIcon("SystemButton.yesIcon"));
      buttonPanel.add(m_swingButtonYes);
      if (defaultButton == null) {
        defaultButton = m_swingButtonYes;
      }
    }
    if (getScoutMessageBox().getNoButtonText() != null) {
      m_swingButtonNo = createButton(getScoutMessageBox().getNoButtonText(), UIManager.getIcon("SystemButton.noIcon"));
      buttonPanel.add(m_swingButtonNo);
      if (defaultButton == null) {
        defaultButton = m_swingButtonNo;
      }
    }
    if (getScoutMessageBox().getCancelButtonText() != null) {
      m_swingButtonCancel = createButton(getScoutMessageBox().getCancelButtonText(), UIManager.getIcon("SystemButton.cancelIcon"));
      buttonPanel.add(m_swingButtonCancel);
      if (defaultButton == null) {
        defaultButton = m_swingButtonCancel;
      }
    }
    // set ENTER key
    if (m_swingButtonYes != null) {
      addButtonKeyStroke(m_swingButtonYes, "ENTER");
    }
    else if (m_swingButtonNo != null) {
      addButtonKeyStroke(m_swingButtonNo, "ENTER");
    }
    else if (m_swingButtonCancel != null) {
      addButtonKeyStroke(m_swingButtonCancel, "ENTER");
    }
    // set ESCAPE key
    if (m_swingButtonCancel != null) {
      addButtonKeyStroke(m_swingButtonCancel, "ESCAPE");
    }
    else if (m_swingButtonNo != null) {
      addButtonKeyStroke(m_swingButtonNo, "ESCAPE");
    }
    else if (m_swingButtonYes != null) {
      addButtonKeyStroke(m_swingButtonYes, "ESCAPE");
    }
    // copy/paste button
    if (getScoutMessageBox().getHiddenText() != null) {
      m_swingButtonCopy = createButton(SwingUtility.getNlsText("Copy"), null);
      buttonPanel.add(m_swingButtonCopy);
      addButtonKeyStroke(m_swingButtonCopy, "control C");
    }
    contentPane.add(BorderLayout.SOUTH, buttonPanel);
    if (defaultButton != null) {
      buttonPanel.getRootPane().setDefaultButton(defaultButton);
    }
    // init layout
    m_swingDialog.pack();
  }

  public IMessageBox getScoutMessageBox() {
    return getScoutObject();
  }

  @Override
  public JDialog getSwingDialog() {
    return m_swingDialog;
  }

  public JButton getSwingYesButton() {
    return m_swingButtonYes;
  }

  public JButton getSwingNoButton() {
    return m_swingButtonNo;
  }

  public JButton getSwingCancelButton() {
    return m_swingButtonCancel;
  }

  public JButton getSwingCopyButton() {
    return m_swingButtonCopy;
  }

  /**
   * start GUI process by presenting a dialog use this method to show the
   * dialog, not m_swingDialog().setVisible()
   */
  @Override
  public void showSwingMessageBox() {
    setOptimizedDialogBounds();
    m_swingDialog.setVisible(true);
  }

  private JButton createButton(String text, Icon icon) {
    final JButton b = new JButton(StringUtility.removeMnemonic(text));
    b.setMnemonic(StringUtility.getMnemonic(text));
    if (icon != null) {
      b.setIcon(icon);
    }
    b.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        handleSwingButtonAction(b);
      }
    });
    return b;
  }

  private void addButtonKeyStroke(JButton b, String keyStrokeText) {
    if (keyStrokeText != null) {
      KeyStroke pressedKs = KeyStroke.getKeyStroke(keyStrokeText);
      KeyStroke releasedKs = KeyStroke.getKeyStroke(pressedKs.getKeyCode(), pressedKs.getModifiers(), true);
      InputMap map = b.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
      map.put(pressedKs, "pressed");
      map.put(releasedKs, "released");
    }
  }

  private void handleSwingButtonAction(JButton b) {
    if (b == m_swingButtonCopy) {
      // copy message to clipboard
      Clipboard clip = Toolkit.getDefaultToolkit().getSystemClipboard();
      clip.setContents(new StringSelection(getScoutMessageBox().getHiddenText()), null);
    }
    else {
      int resultOption = -1;
      if (b == m_swingButtonYes) {
        resultOption = IMessageBox.YES_OPTION;
      }
      else if (b == m_swingButtonNo) {
        resultOption = IMessageBox.NO_OPTION;
      }
      else if (b == m_swingButtonCancel) {
        resultOption = IMessageBox.CANCEL_OPTION;
      }
      if (resultOption != -1) {
        final int fOption = resultOption;
        Runnable t = new Runnable() {
          @Override
          public void run() {
            getScoutMessageBox().getUIFacade().setResultFromUI(fOption);
          }
        };

        getSwingEnvironment().invokeScoutLater(t, 0);
      }
    }
  }

  private void setOptimizedDialogBounds() {
    // adjustLocation
    Window owner = m_swingParent;
    Dimension ownerSize = owner.getSize();
    if (ownerSize.width >= 100 && ownerSize.height >= 100) {
      getSwingDialog().setLocationRelativeTo(owner);
    }
    else {
      getSwingDialog().setLocationRelativeTo(null);
    }
  }

  @Override
  protected void attachScout() {
    super.attachScout();
    if (m_scoutMessageBoxListener == null) {
      m_scoutMessageBoxListener = new P_ScoutMessageBoxListener();
      getScoutMessageBox().addMessageBoxListener(m_scoutMessageBoxListener);
    }
  }

  @Override
  protected void detachScout() {
    super.detachScout();
    if (m_scoutMessageBoxListener != null) {
      getScoutMessageBox().removeMessageBoxListener(m_scoutMessageBoxListener);
      m_scoutMessageBoxListener = null;
    }
  }

  /*
   * event handlers
   */
  protected void handleScoutMessageBoxClosed(MessageBoxEvent e) {
    // dialog model detach
    disconnectFromScout();
    m_swingDialog.setVisible(false);
    m_swingDialog.dispose();
  }

  protected void handleSwingWindowClosed(WindowEvent e) {
    // notify Scout
    Runnable t = new Runnable() {
      @Override
      public void run() {
        getScoutMessageBox().getUIFacade().setResultFromUI(IMessageBox.CANCEL_OPTION);
      }
    };

    getSwingEnvironment().invokeScoutLater(t, 0);
    // end notify
  }

  @Override
  public void setName(String name) {
    m_swingDialog.getRootPane().setName(name);
  }

  /*
   * other observers
   */
  private class P_ScoutMessageBoxListener implements MessageBoxListener {
    @Override
    public void messageBoxChanged(final MessageBoxEvent e) {
      switch (e.getType()) {
        case MessageBoxEvent.TYPE_CLOSED: {
          Runnable t = new Runnable() {
            @Override
            public void run() {
              switch (e.getType()) {
                case MessageBoxEvent.TYPE_CLOSED: {
                  handleScoutMessageBoxClosed(e);
                  break;
                }
                default:
                  break;
              }
            }
          };
          getSwingEnvironment().invokeSwingLater(t);
          break;
        }
        default:
          break;
      }
    }
  }// end private class

  private class P_SwingWindowListener extends WindowAdapter {
    @Override
    public void windowClosed(WindowEvent e) {
      handleSwingWindowClosed(e);
    }
  }// end private class

  private static class P_TopSeparatorBorder implements Border {

    @Override
    public Insets getBorderInsets(Component c) {
      return new Insets(2, 0, 0, 0);
    }

    @Override
    public boolean isBorderOpaque() {
      return false;
    }

    @Override
    public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
      // top
      g.setColor(Color.lightGray);
      g.drawLine(x, y, x + width, y);
      g.setColor(Color.white);
      g.drawLine(x, y + 1, x + width, y + 1);
    }
  }

  private static class P_Layout extends BorderLayoutEx {
    private static final long serialVersionUID = 1L;

    @Override
    protected Dimension getLayoutSize(Container parent, int sizeflag) {
      // min/pref size is at most MAX_WIDTH x MAX_HEIGHT
      Dimension d = super.getLayoutSize(parent, sizeflag);
      switch (sizeflag) {
        case AbstractLayoutManager2.MIN_SIZE:
        case AbstractLayoutManager2.PREF_SIZE: {
          d.width = Math.min(d.width, MAX_WIDTH);
          d.height = Math.min(d.height, MAX_HEIGHT);
          break;
        }
        case AbstractLayoutManager2.MAX_SIZE: {
          break;
        }
      }
      return d;
    }
  }

  /**
   * Ensures proper text wrapping
   *
   * @param label
   *          the label component with its initial text set
   * @param text
   *          the text that should be displayed
   */
  private void ensureProperDimension(final JLabel label, String text) {
    final int maxLabelWidth = MAX_WIDTH - 2 * HORIZONTAL_PADDING;
    if (label.getPreferredSize().width <= maxLabelWidth) {
      return;
    }

    // convert text to HTML to enable text wrapping
    label.setText(SwingUtility.createHtmlLabelText(text, true));
    // register listener to correct dimension
    label.addPropertyChangeListener("ancestor", new PropertyChangeListener() {

      @Override
      public void propertyChange(PropertyChangeEvent e) {
        JComponent component = (JComponent) e.getSource();
        component.removePropertyChangeListener("ancestor", this);

        final View view = (View) component.getClientProperty("html");
        if (view == null) {
          return;
        }

        final float initialHorizontalSpan = view.getPreferredSpan(View.X_AXIS);
        final float initialVerticalSpan = view.getPreferredSpan(View.Y_AXIS);

        // narrow horizontal span to allow proper height calculation. This precedes initial size calculation.
        view.setSize(Math.min(maxLabelWidth, initialHorizontalSpan), initialVerticalSpan);

        /*
         * Restore initial span to allow the label to be resized wider than the preferred span specified.
         * Otherwise, when resizing the component, the text will not reflow to adapt the new width.
         * To prevent the label from flickering when being displayed, the size is only adjusted if component is resized to a width wider than the preferred width.
         */
        label.addComponentListener(new ComponentAdapter() {
          @Override
          public void componentResized(ComponentEvent e1) {
            JPanel contentPane = (JPanel) m_swingDialog.getContentPane();
            if (contentPane.getWidth() > MAX_WIDTH) {
              label.removeComponentListener(this);
              view.setSize(initialHorizontalSpan, initialVerticalSpan);
            }
          }
        });
      }
    });
  }

  protected String processTextOnLabel(String text, JLabelEx label, boolean htmlEnabled) {
    if (!htmlEnabled && BasicHTML.isHTMLString(text)) {
      if (SwingUtility.isMultilineLabelText(text)) {
        String body = getSwingEnvironment().getHtmlValidator().escape(text, getScoutMessageBox());
        text = "<html>" + StringUtility.convertPlainTextNewLinesToHtml(body, false) + "</html>";
      }
      else {
        text = getSwingEnvironment().getHtmlValidator().escape(text, getScoutMessageBox());
      }
    }
    return text;
  }
}
