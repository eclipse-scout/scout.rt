package org.eclipse.scout.rt.ui.rap.ext;

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
  private static final long serialVersionUID = 1L;

  protected Label label;
  protected ButtonEx btn;

  public MultilineButton(Composite parent, int style) {
    super(parent, style);
    createContent(this, style);
    createLayout();
  }

  protected void createContent(Composite parent, int style) {
    this.btn = new ButtonEx(parent, style);
    this.label = new Label(parent, 0);
  }

  protected void createLayout() {
    GridLayout containerLayout = new GridLayout(2, false);
    containerLayout.horizontalSpacing = 5; //space between button and label
    containerLayout.marginHeight = 0;
    containerLayout.marginWidth = 0;
    containerLayout.verticalSpacing = 0;

    GridData labelData = new GridData(SWT.FILL, SWT.FILL, false, false);
    labelData.verticalIndent = 3;
    label.setLayoutData(labelData);

    GridData buttonData = new GridData(SWT.FILL, SWT.TOP, false, false);
    buttonData.verticalIndent = 3;
    btn.setLayoutData(buttonData);

    setLayout(containerLayout);
  }

  public void setText(String text) {
    label.setText(text);
  }

  @Override
  public void setFont(org.eclipse.swt.graphics.Font font) {
    label.setFont(font);
    btn.setFont(font);
  }

  @Override
  public void setForeground(org.eclipse.swt.graphics.Color color) {
    super.setForeground(color);
    label.setForeground(color);
    btn.setForeground(color);
  }

  @Override
  public void setBackground(org.eclipse.swt.graphics.Color color) {
    super.setBackground(color);
    label.setBackground(color);
    btn.setBackground(color);
  }

  @Override
  public void removeListener(int eventType, Listener listener) {
    super.removeListener(eventType, listener);
    btn.removeListener(eventType, listener);
    label.removeListener(eventType, listener);
  }

  public void setSelection(boolean selected) {
    btn.setSelection(selected);
  }

  public boolean getSelection() {
    return btn.getSelection();
  }

  @Override
  public void addListener(int eventType, Listener listener) {
    super.addListener(eventType, listener);
    btn.addListener(eventType, listener);
    label.addListener(eventType, listener);
  }

  @Override
  public void setMenu(Menu menu) {
    super.setMenu(menu);
    btn.setMenu(menu);
    label.setMenu(menu);
  }

  @Override
  public void setToolTipText(String string) {
    super.setToolTipText(string);
    btn.setToolTipText(string);
    label.setToolTipText(string);
  }

  public void setImage(Image icon) {
    btn.setImage(icon);
  }
}
