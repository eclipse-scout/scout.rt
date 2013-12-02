package org.eclipse.scout.rt.ui.swt.ext;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;

/**
 * SWT's RadioButton and Checkbox don't support multiline in their labels.
 * A pure SWT Label however supports multiline.
 * Therefore this class was created, it is a composite of a button and a label.
 * 
 * @since 3.10.0-M4
 */
public abstract class MultilineButton extends Composite {

  protected Label m_label;
  protected ButtonEx m_btn;

  public MultilineButton(Composite parent, int style) {
    super(parent, style);
    createContent(this, style);
    createLayout();
  }

  protected void createContent(Composite parent, int style) {
    this.m_btn = new ButtonEx(parent, style);
    this.m_label = new Label(parent, 0);
  }

  protected void createLayout() {
    GridLayout containerLayout = new GridLayout(2, false);
    containerLayout.horizontalSpacing = 5; //space between button and label
    containerLayout.marginHeight = 0;
    containerLayout.marginWidth = 0;
    containerLayout.verticalSpacing = 0;

    GridData labelData = new GridData(SWT.FILL, SWT.FILL, false, false);
    labelData.verticalIndent = 3;
    m_label.setLayoutData(labelData);

    GridData buttonData = new GridData(SWT.FILL, SWT.TOP, false, false);
    buttonData.verticalIndent = 3;
    m_btn.setLayoutData(buttonData);

    setLayout(containerLayout);
  }

  public void setText(String text) {
    m_label.setText(text);
  }

  @Override
  public void setFont(org.eclipse.swt.graphics.Font font) {
    m_label.setFont(font);
    m_btn.setFont(font);
  }

  @Override
  public void setForeground(org.eclipse.swt.graphics.Color color) {
    super.setForeground(color);
    m_label.setForeground(color);
    m_btn.setForeground(color);
  }

  @Override
  public void setBackground(org.eclipse.swt.graphics.Color color) {
    super.setBackground(color);
    m_label.setBackground(color);
    m_btn.setBackground(color);
  }

  @Override
  public void removeListener(int eventType, Listener listener) {
    super.removeListener(eventType, listener);
    m_btn.removeListener(eventType, listener);
    m_label.removeListener(eventType, listener);
  }

  public void setSelection(boolean selected) {
    m_btn.setSelection(selected);
  }

  public boolean getSelection() {
    return m_btn.getSelection();
  }

  @Override
  public void addListener(int eventType, Listener listener) {
    super.addListener(eventType, listener);
    m_btn.addListener(eventType, listener);
    m_label.addListener(eventType, listener);
  }

  @Override
  public void setMenu(Menu menu) {
    super.setMenu(menu);
    m_btn.setMenu(menu);
    m_label.setMenu(menu);
  }

  @Override
  public void setToolTipText(String string) {
    super.setToolTipText(string);
    m_btn.setToolTipText(string);
    m_label.setToolTipText(string);
  }

  public void setImage(Image icon) {
    m_btn.setImage(icon);
  }
}
