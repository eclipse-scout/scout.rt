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
package org.eclipse.scout.rt.ui.swing.form.fields.filechooserfield;

import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;

import org.eclipse.scout.commons.CollectionUtility;
import org.eclipse.scout.commons.CompareUtility;
import org.eclipse.scout.commons.holders.Holder;
import org.eclipse.scout.commons.job.JobEx;
import org.eclipse.scout.rt.client.ui.action.menu.IMenu;
import org.eclipse.scout.rt.client.ui.action.menu.MenuUtility;
import org.eclipse.scout.rt.client.ui.basic.filechooser.IFileChooser;
import org.eclipse.scout.rt.client.ui.form.fields.filechooserfield.IFileChooserField;
import org.eclipse.scout.rt.ui.swing.LogicalGridLayout;
import org.eclipse.scout.rt.ui.swing.SwingUtility;
import org.eclipse.scout.rt.ui.swing.action.menu.SwingScoutContextMenu;
import org.eclipse.scout.rt.ui.swing.basic.IconGroup;
import org.eclipse.scout.rt.ui.swing.ext.JPanelEx;
import org.eclipse.scout.rt.ui.swing.ext.JStatusLabelEx;
import org.eclipse.scout.rt.ui.swing.ext.decoration.ContextMenuDecorationItem;
import org.eclipse.scout.rt.ui.swing.ext.decoration.DecorationGroup;
import org.eclipse.scout.rt.ui.swing.ext.decoration.DropDownDecorationItem;
import org.eclipse.scout.rt.ui.swing.ext.decoration.IDecorationGroup;
import org.eclipse.scout.rt.ui.swing.ext.decoration.JTextFieldWithDecorationIcons;
import org.eclipse.scout.rt.ui.swing.form.fields.SwingScoutValueFieldComposite;

public class SwingScoutFileChooserField extends SwingScoutValueFieldComposite<IFileChooserField> implements ISwingScoutFileChooserField {
  private static final long serialVersionUID = 1L;
  private ContextMenuDecorationItem m_contextMenuMarker;
  private SwingScoutContextMenu m_contextMenu;
  private DropDownDecorationItem m_dropdownIcon;
  private PropertyChangeListener m_contextMenuVisibilityListener;

  @Override
  protected void initializeSwing() {
    JPanelEx container = new JPanelEx();
    container.setOpaque(false);
    JStatusLabelEx label = getSwingEnvironment().createStatusLabel(getScoutObject());
    container.add(label);
    //
    JTextComponent textField = createTextField(container);
    Document doc = textField.getDocument();
    doc.addDocumentListener(new DocumentListener() {
      @Override
      public void removeUpdate(DocumentEvent e) {
        setInputDirty(true);
      }

      @Override
      public void insertUpdate(DocumentEvent e) {
        setInputDirty(true);
      }

      @Override
      public void changedUpdate(DocumentEvent e) {
        setInputDirty(true);
      }
    });
    // key mappings
    InputMap inputMap = textField.getInputMap(JTextField.WHEN_FOCUSED);
    inputMap.put(SwingUtility.createKeystroke("F2"), "fileChooser");
    ActionMap actionMap = textField.getActionMap();
    actionMap.put("fileChooser", new P_SwingFileChooserAction());
    //
    setSwingContainer(container);
    setSwingLabel(label);
    setSwingField(textField);
    // layout
    getSwingContainer().setLayout(new LogicalGridLayout(getSwingEnvironment(), 1, 0));
  }

  /**
   * Create and add the text field to the container.
   * <p>
   * May add additional components to the container.
   */
  protected JTextComponent createTextField(JComponent container) {
    JTextFieldWithDecorationIcons textField = new JTextFieldWithDecorationIcons();
    container.add(textField);
    IDecorationGroup decorationGroup = new DecorationGroup(textField, getSwingEnvironment());
    // context menu marker
    m_contextMenuMarker = new ContextMenuDecorationItem(getScoutObject().getContextMenu(), textField, getSwingEnvironment());
    m_contextMenuMarker.addMouseListener(new MouseAdapter() {
      @Override
      public void mouseClicked(MouseEvent e) {
        m_contextMenu.showSwingPopup(e.getX(), e.getY(), false);
      }
    });
    decorationGroup.addDecoration(m_contextMenuMarker);

    // smart chooser decoration
    m_dropdownIcon = new DropDownDecorationItem(textField, getSwingEnvironment());
    m_dropdownIcon.addMouseListener(new MouseAdapter() {
      @Override
      public void mouseClicked(MouseEvent e) {
        if (e.getButton() == MouseEvent.BUTTON3) {
          m_contextMenu.showSwingPopup(e.getX(), e.getY(), false);
        }
        else {
          getSwingTextField().requestFocus();
          handleSwingFileChooserAction();
        }
      }
    });
    decorationGroup.addDecoration(m_dropdownIcon);

    textField.setDecorationIcon(decorationGroup);
    return textField;
//    JTextFieldWithDropDownButton textField = new JTextFieldWithDropDownButton(getSwingEnvironment());
//    container.add(textField);
//    textField.addDropDownButtonListener(new IDropDownButtonListener() {
//      @Override
//      public void iconClicked(Object source) {
//        getSwingTextField().requestFocus();
//        handleSwingFileChooserAction();
//      }
//
//      @Override
//      public void menuClicked(Object source) {
//        handleSwingPopup((JComponent) source);
//      }
//    });
//    return textField;
  }

  protected void installContextMenu() {
    m_contextMenuVisibilityListener = new PropertyChangeListener() {
      @Override
      public void propertyChange(PropertyChangeEvent evt) {
        if (IMenu.PROP_VISIBLE.equals(evt.getPropertyName())) {
          m_contextMenuMarker.setMarkerVisible(getScoutObject().getContextMenu().isVisible());
        }
      }
    };
    getScoutObject().getContextMenu().addPropertyChangeListener(m_contextMenuVisibilityListener);
    m_contextMenuMarker.setMarkerVisible(getScoutObject().getContextMenu().isVisible());
    m_contextMenu = SwingScoutContextMenu.installContextMenuWithSystemMenus(getSwingTextField(), getScoutObject().getContextMenu(), getSwingEnvironment());
  }

  @Override
  public JTextComponent getSwingTextField() {
    return (JTextComponent) getSwingField();
  }

  protected void uninstallContextMenu() {
    if (m_contextMenuVisibilityListener != null) {
      getScoutObject().getContextMenu().removePropertyChangeListener(m_contextMenuVisibilityListener);
      m_contextMenuVisibilityListener = null;
    }
  }

  @Override
  protected void attachScout() {
    super.attachScout();
    IFileChooserField f = getScoutObject();
    setFileIconIdFromScout(f.getFileIconId());
    installContextMenu();
  }

  @Override
  protected void detachScout() {
    if (m_contextMenuMarker != null) {
      m_contextMenuMarker.destroy();
    }
    uninstallContextMenu();
    super.detachScout();
  }

  @Override
  protected void setDisplayTextFromScout(String s) {
    JTextComponent swingField = getSwingTextField();
    swingField.setText(s);
  }

  private boolean calculateDropDownButtonEnabled() {
    final Holder<List<IMenu>> menuHolder = new Holder<List<IMenu>>();
    Runnable t = new Runnable() {
      @Override
      public void run() {
        menuHolder.setValue(getScoutObject().getContextMenu().getChildActions());
      }
    };
    JobEx job = getSwingEnvironment().invokeScoutLater(t, 1200);
    try {
      job.join(1200);
    }
    catch (InterruptedException ex) {
      //nop
    }
    return MenuUtility.consolidateMenus(menuHolder.getValue()).size() > 0;
  }

  @Override
  protected void setHorizontalAlignmentFromScout(int scoutAlign) {
    if (getSwingTextField() instanceof JTextField) {
      int swingAlign = SwingUtility.createHorizontalAlignment(scoutAlign);
      ((JTextField) getSwingTextField()).setHorizontalAlignment(swingAlign);
    }
  }

  @Override
  protected void setEnabledFromScout(boolean b) {
    super.setEnabledFromScout(b);
    m_dropdownIcon.setEnabled(b);
//    if (getSwingField() instanceof JTextFieldWithDropDownButton) {
//      ((JTextFieldWithDropDownButton) getSwingTextField()).setDropDownButtonEnabled(b);
//    }
  }

  protected void setFileIconIdFromScout(String s) {
    m_dropdownIcon.setIconGroup(new IconGroup(getSwingEnvironment(), s));
  }

  @Override
  protected boolean handleSwingInputVerifier() {
    final String text = getSwingTextField().getText();
    // only handle if text has changed
    if (CompareUtility.equals(text, getScoutObject().getDisplayText()) && getScoutObject().getErrorStatus() == null) {
      return true;
    }
    final Holder<Boolean> result = new Holder<Boolean>(Boolean.class, false);
    // notify Scout
    Runnable t = new Runnable() {
      @Override
      public void run() {
        boolean b = getScoutObject().getUIFacade().setTextFromUI(text);
        result.setValue(b);
      }
    };
    JobEx job = getSwingEnvironment().invokeScoutLater(t, 0);
    try {
      job.join(2345);
    }
    catch (InterruptedException e) {
      //nop
    }
    // end notify
    getSwingEnvironment().dispatchImmediateSwingJobs();
    return true;// continue always
  }

  @Override
  protected void handleSwingFocusGained() {
    super.handleSwingFocusGained();
    JTextComponent swingField = getSwingTextField();
    if (!isMenuOpened() && swingField.getDocument().getLength() > 0) {
      swingField.setCaretPosition(swingField.getDocument().getLength());
      swingField.moveCaretPosition(0);
    }
    setMenuOpened(false);
  }

  protected boolean isFileChooserEnabled() {
    return m_dropdownIcon.isEnabled();
//    if (getSwingField() instanceof JTextFieldWithDropDownButton) {
//      return (((JTextFieldWithDropDownButton) getSwingTextField()).isDropDownButtonEnabled());
//    }
//    return false;
  }

  protected void handleSwingFileChooserAction() {
    if (isFileChooserEnabled()) {
      Runnable scoutJob = new Runnable() {
        @Override
        public void run() {
          IFileChooser fc = getScoutObject().getFileChooser();
          final List<File> files = fc.startChooser();

          Runnable swingJob = new Runnable() {
            @Override
            public void run() {
              if (CollectionUtility.hasElements(files)) {
                getSwingTextField().setText(CollectionUtility.firstElement(files).getAbsolutePath());
                handleSwingInputVerifier();
              }
            }
          };
          if (getSwingEnvironment() != null) {
            getSwingEnvironment().invokeSwingLater(swingJob);
          }
        }
      };
      getSwingEnvironment().invokeScoutLater(scoutJob, 0);
    }
  }

//  protected void handleSwingPopup(final JComponent target) {
//    if (getScoutObject().getContextMenu().hasChildActions()) {
//      // notify Scout
//
//      // <bsh 2010-10-08>
//      // The default implementation positions the popup menu on the left side of the
//      // "target" component. This is no longer correct in Rayo. So we use the target's
//      // width and subtract a certain amount.
//      int x = 0;
//      if (target instanceof JTextFieldWithDropDownButton) {
//        JTextFieldWithDropDownButton tf = (JTextFieldWithDropDownButton) target;
//        x = tf.getWidth() - tf.getMargin().right;
//      }
//      final Point point = new Point(x, target.getHeight());
//      // </bsh>
//
//      Runnable t = new Runnable() {
//        @Override
//        public void run() {
//          List<IMenu> scoutMenus = ActionUtility.visibleNormalizedActions(getScoutObject().getContextMenu().getChildActions());
//          // call swing menu
//          new SwingPopupWorker(getSwingEnvironment(), target, point, scoutMenus).enqueue();
//        }
//      };
//      getSwingEnvironment().invokeScoutLater(t, 5678);
//      // end notify
//    }
//  }

  @Override
  protected void handleScoutPropertyChange(String name, Object newValue) {
    super.handleScoutPropertyChange(name, newValue);
    if (name.equals(IFileChooserField.PROP_FILE_ICON_ID)) {
      setFileIconIdFromScout((String) newValue);
    }
  }

  /**
   * Swing action
   */
  private class P_SwingFileChooserAction extends AbstractAction {
    private static final long serialVersionUID = 1L;

    @Override
    public void actionPerformed(ActionEvent e) {
      handleSwingFileChooserAction();
    }
  }// end private class

}
