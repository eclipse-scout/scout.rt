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
package org.eclipse.scout.rt.ui.swt.util;

import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.scout.rt.client.ui.basic.table.ITable;
import org.eclipse.scout.rt.client.ui.form.fields.IFormField;
import org.eclipse.scout.rt.ui.swt.ISwtEnvironment;
import org.eclipse.scout.rt.ui.swt.basic.comp.CLabelEx;
import org.eclipse.scout.rt.ui.swt.basic.comp.HyperlinkEx;
import org.eclipse.scout.rt.ui.swt.ext.ButtonEx;
import org.eclipse.scout.rt.ui.swt.ext.ImageViewer;
import org.eclipse.scout.rt.ui.swt.ext.MultilineCheckbox;
import org.eclipse.scout.rt.ui.swt.ext.MultilineRadioButton;
import org.eclipse.scout.rt.ui.swt.ext.ScrolledFormEx;
import org.eclipse.scout.rt.ui.swt.ext.SectionContent;
import org.eclipse.scout.rt.ui.swt.ext.SnapButtonMaximized;
import org.eclipse.scout.rt.ui.swt.ext.StatusLabelEx;
import org.eclipse.scout.rt.ui.swt.ext.StatusLabelTop;
import org.eclipse.scout.rt.ui.swt.ext.StyledTextEx;
import org.eclipse.scout.rt.ui.swt.ext.TextEx;
import org.eclipse.scout.rt.ui.swt.ext.table.TableEx;
import org.eclipse.scout.rt.ui.swt.ext.tree.TreeEx;
import org.eclipse.scout.rt.ui.swt.extension.UiDecorationExtensionPoint;
import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.IFormColors;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;

public class ScoutFormToolkit extends WrappedFormToolkit {

  public ScoutFormToolkit(FormToolkit kit) {
    super(kit);
  }

  public ScrolledFormEx createScrolledFormEx(Composite parent, int style) {
    ScrolledFormEx form = new ScrolledFormEx(parent, style);
    adapt(form, false, false);
    if (form.getBody() != null) {
      adapt(form.getBody(), false, false);
    }
    form.setBackground(kit.getColors().getBackground());
    form.setForeground(kit.getColors().getColor(IFormColors.TITLE));
    return form;
  }

  @Override
  public ScrolledForm createScrolledForm(Composite parent) {
    ScrolledForm form = new ScrolledForm(parent, SWT.V_SCROLL | SWT.WRAP | kit.getOrientation());
    // form.setExpandHorizontal(true);
    // form.setExpandVertical(true);
    adapt(form, false, false);
    form.setBackground(kit.getColors().getBackground());
    form.setForeground(kit.getColors().getColor(IFormColors.TITLE));
    form.setFont(JFaceResources.getHeaderFont());
    return form;
  }

  @Override
  public TreeEx createTree(Composite parent, int style) {
    TreeEx tree = new TreeEx(parent, style);
    adapt(tree, false, false);
    return tree;
  }

  @Override
  public TableEx createTable(Composite parent, int style) {
    return createTable(parent, style, null);
  }

  public TableEx createTable(Composite parent, int style, ITable scoutTable) {
    TableEx table = new TableEx(parent, style, scoutTable);
    adapt(table, false, false);
    return table;

  }

  public int computeSwtLabelHorizontalAlignment(int scoutAlign) {
    switch (scoutAlign) {
      case -1: {
        return SWT.LEFT;
      }
      case 0: {
        return SWT.CENTER;
      }
      case 1: {
        return SWT.RIGHT;
      }
      default: {
        return UiDecorationExtensionPoint.getLookAndFeel().getFormFieldLabelAlignment();
      }
    }
  }

  protected int computeSwtLabelStyle(IFormField scoutObject) {
    if (scoutObject == null) {
      return SWT.NONE;
    }

    return computeSwtLabelHorizontalAlignment(scoutObject.getLabelHorizontalAlignment());
  }

  public StatusLabelEx createStatusLabel(Composite parent, ISwtEnvironment environment, IFormField scoutObject) {
    int labelStyle = computeSwtLabelStyle(scoutObject);

    return createStatusLabel(parent, environment, scoutObject, labelStyle);
  }

  public StatusLabelEx createStatusLabel(Composite parent, ISwtEnvironment environment, IFormField scoutObject, int style) {
    StatusLabelEx label = null;
    if (scoutObject != null && scoutObject.getLabelPosition() == IFormField.LABEL_POSITION_TOP) {
      label = new StatusLabelTop(parent, style, environment);
    }
    else {
      label = new StatusLabelEx(parent, style, environment);
    }
    adapt(label, false, false);

    return label;
  }

  public StyledText createStyledText(Composite parent, int style) {
    StyledText text = new StyledTextEx(parent, style);
    adapt(text, false, false);
    // correction to look like a normal text
    text.setIndent(2);
    return text;
  }

  public CTabFolder createTabFolder(Composite parent) {
    CTabFolder folder = new CTabFolder(parent, SWT.TOP);
    adapt(folder, false, false);
    return folder;
  }

  public Text createText(Composite container, int style) {
    Text t = new TextEx(container, style);
    adapt(t, false, false);
    return t;
  }

  public SectionContent createSectionClient(Composite parent) {
    SectionContent client = new SectionContent(parent, SWT.NONE);
    adapt(client, false, false);
    return client;
  }

  public ButtonEx createButtonEx(Composite parent, int style) {
    ButtonEx button = new ButtonEx(parent, style | SWT.FLAT);
    adapt(button, false, false);
    return button;
  }

  /**
   * @since 3.10.0-M4
   */
  public MultilineRadioButton createMultilineRadioButton(Composite container) {
    MultilineRadioButton button = new MultilineRadioButton(container, SWT.LEFT);
    adapt(button, false, false);
    return button;
  }

  /**
   * @since 3.10.0-M4
   */
  public MultilineCheckbox createMultilineCheckBox(Composite container) {
    MultilineCheckbox button = new MultilineCheckbox(container, SWT.CHECK);
    adapt(button, false, false);
    return button;
  }

  public Canvas createCanvas(Composite container) {
    Canvas c = new Canvas(container, SWT.NONE);
    adapt(c, false, false);
    return c;
  }

  public Browser createBrowser(Composite container, int style) {
    Browser browser = new Browser(container, style);
    adapt(browser, false, false);
    return browser;
  }

  public SnapButtonMaximized createSnapButtonMaximized(Composite parent) {
    SnapButtonMaximized button = new SnapButtonMaximized(parent, SWT.NONE);
    adapt(button);
    return button;
  }

  public SashForm createSashForm(Composite parent, int style) {
    SashForm button = new SashForm(parent, style);
    adapt(button);
    return button;
  }

  public CLabelEx createCLabel(Composite parent, String text) {
    return createCLabel(parent, text, SWT.NONE);
  }

  /**
   * Creates a clabel as a part of the form.
   * 
   * @param parent
   *          the label parent
   * @param text
   *          the label text
   * @param style
   *          the label style
   * @return the label widget
   */
  public CLabelEx createCLabel(Composite parent, String text, int style) {
    CLabelEx label = new CLabelEx(parent, style | getOrientation());
    if (text != null) {
      label.setText(text);
    }
    adapt(label, false, false);
    return label;
  }

  public ImageViewer createImageViewer(Composite container) {
    ImageViewer viewer = new ImageViewer(container);
    adapt(viewer, false, false);
    return viewer;
  }

  @Override
  public HyperlinkEx createHyperlink(Composite parent, String text, int style) {
    HyperlinkEx hyperlink = new HyperlinkEx(parent, style | getOrientation());
    if (text != null) {
      hyperlink.setText(text);
    }
    getHyperlinkGroup().add(hyperlink);
    adapt(hyperlink);
    hyperlink.addListener(SWT.KeyDown, new P_HyperlinkKeyListener());
    return hyperlink;
  }

  private class P_HyperlinkKeyListener implements Listener {
    @Override
    public void handleEvent(Event event) {
      if (event.type == SWT.KeyDown) {
        switch (event.keyCode) {
          case SWT.TAB:
            int traversal = SWT.TRAVERSE_TAB_NEXT;
            if ((event.stateMask & SWT.SHIFT) != 0) {
              traversal = SWT.TRAVERSE_TAB_PREVIOUS;
            }
            ((Control) event.widget).traverse(traversal);
            break;
        }
      }
    }
  }
}
