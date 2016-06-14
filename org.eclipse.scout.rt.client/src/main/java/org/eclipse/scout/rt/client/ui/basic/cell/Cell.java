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
package org.eclipse.scout.rt.client.ui.basic.cell;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.scout.rt.client.ui.IHtmlCapable;
import org.eclipse.scout.rt.client.ui.IStyleable;
import org.eclipse.scout.rt.client.ui.form.fields.DefaultFieldStatus;
import org.eclipse.scout.rt.client.ui.form.fields.ValidationFailedStatus;
import org.eclipse.scout.rt.platform.exception.ProcessingException;
import org.eclipse.scout.rt.platform.status.IMultiStatus;
import org.eclipse.scout.rt.platform.status.IStatus;
import org.eclipse.scout.rt.platform.status.MultiStatus;
import org.eclipse.scout.rt.platform.util.CompareUtility;
import org.eclipse.scout.rt.platform.util.StringUtility;
import org.eclipse.scout.rt.shared.data.basic.FontSpec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A Cell represents model properties of a tree node or table cell.
 * <p>
 * This implementation shares graphical aspects with other cell instances and uses a {@link CellExtension} to store
 * rarely used properties.
 * </p>
 */
public class Cell implements ICell, IStyleable, IHtmlCapable {

  private static final Logger LOG = LoggerFactory.getLogger(Cell.class);

  private static final Map<CellStyle, CellStyle> SHARED_STYLE_STORE;
  private static final Object SHARED_STYLE_STORE_LOCK = new Object();
  private static final CellStyle DEFAULT_CELL_STYLE;

  static {
    SHARED_STYLE_STORE = new HashMap<CellStyle, CellStyle>();
    DEFAULT_CELL_STYLE = new CellStyle();
    SHARED_STYLE_STORE.put(DEFAULT_CELL_STYLE, DEFAULT_CELL_STYLE);
  }

  private ICellObserver m_observer;
  private Object m_value;
  private String m_text;
  private ICellSpecialization m_cellSpecialization = DEFAULT_CELL_STYLE;

  private IMultiStatus m_errorStatus = null;

  public Cell() {
  }

  public Cell(ICell c) {
    super();
    try {
      updateFrom(c);
    }
    catch (RuntimeException e) {
      //should never happen
      LOG.error("Unexpected", e);
    }
  }

  public Cell(ICellObserver observer) {
    super();
    setObserver(observer);
  }

  public Cell(ICellObserver observer, ICell c) {
    super();
    updateFrom(c);
    setObserver(observer);
  }

  public void updateFrom(ICell c) {
    if (c != null) {
      setCssClass(c.getCssClass());
      setFont(c.getFont());
      setEditable(c.isEditable());
      setForegroundColor(c.getForegroundColor());
      setBackgroundColor(c.getBackgroundColor());
      setHorizontalAlignment(c.getHorizontalAlignment());
      setTooltipText(c.getTooltipText());
      setIconId(c.getIconId());
      setText(c.getText());
      setValue(c.getValue());
      setMandatory(c.isMandatory());
      setErrorStatusInternal(c.getErrorStatus());
      setHtmlEnabled(c.isHtmlEnabled());
      //do not reset observer
    }
  }

  @Override
  public Object getValue() {
    return m_value;
  }

  /**
   * @return true if the value has in fact changed
   */
  public boolean setValue(Object value) {
    if (getObserver() != null) {
      try {
        value = getObserver().validateValue(this, value);
      }
      catch (ProcessingException e) {
        addErrorStatus(new ValidationFailedStatus<Object>(e, value));
      }
    }
    if (CompareUtility.equals(m_value, value)) {
      return false;
    }
    else {
      m_value = value;
      notifyObserver(VALUE_BIT);
      return true;
    }
  }

  @Override
  public String getText() {
    return m_text;
  }

  public void setText(String s) {
    if (CompareUtility.notEquals(m_text, s)) {
      m_text = s;
      notifyObserver(TEXT_BIT);
    }
  }

  @Override
  public String getCssClass() {
    return m_cellSpecialization.getCssClass();
  }

  @Override
  public void setCssClass(String cssClass) {
    if (CompareUtility.notEquals(m_cellSpecialization.getCssClass(), cssClass)) {
      ICellSpecialization newStyle = m_cellSpecialization.copy();
      newStyle.setCssClass(cssClass);
      setValueInternal(CSS_CLASS_BIT, newStyle);
    }
  }

  @Override
  public String getIconId() {
    return m_cellSpecialization.getIconId();
  }

  public void setIconId(String s) {
    if (CompareUtility.notEquals(m_cellSpecialization.getIconId(), s)) {
      ICellSpecialization newStyle = m_cellSpecialization.copy();
      newStyle.setIconId(s);
      setValueInternal(ICON_ID_BIT, newStyle);
    }
  }

  @Override
  public String getTooltipText() {
    return m_cellSpecialization.getTooltipText();
  }

  public void setTooltipText(String s) {
    if (m_cellSpecialization instanceof CellStyle) {
      if (!StringUtility.isNullOrEmpty(s)) {
        ICellSpecialization newStyle = new CellExtension(m_cellSpecialization);
        newStyle.setTooltipText(s);
        setValueInternal(TOOLTIP_BIT, newStyle);
      }
    }
    else if (CompareUtility.notEquals(m_cellSpecialization.getIconId(), s)) {
      ICellSpecialization newStyle = m_cellSpecialization.copy();
      newStyle.setTooltipText(s);
      setValueInternal(TOOLTIP_BIT, newStyle);
    }
  }

  @Override
  public boolean isMandatory() {
    return m_cellSpecialization.isMandatory();
  }

  public void setMandatory(boolean mandatory) {
    if (m_cellSpecialization instanceof CellStyle) {
      if (mandatory) {
        ICellSpecialization newStyle = new CellExtension(m_cellSpecialization);
        newStyle.setMandatory(mandatory);
        setValueInternal(MANDATORY_BIT, newStyle);
      }
    }
    else if (CompareUtility.notEquals(m_cellSpecialization.isMandatory(), mandatory)) {
      ICellSpecialization newStyle = m_cellSpecialization.copy();
      newStyle.setMandatory(mandatory);
      setValueInternal(MANDATORY_BIT, newStyle);
    }
  }

  @Override
  public int getHorizontalAlignment() {
    return m_cellSpecialization.getHorizontalAlignment();
  }

  public void setHorizontalAlignment(int a) {
    if (m_cellSpecialization.getHorizontalAlignment() != a) {
      ICellSpecialization newStyle = m_cellSpecialization.copy();
      newStyle.setHorizontalAlignment(a);
      setValueInternal(H_ALIGN_BIT, newStyle);
    }
  }

  @Override
  public String getBackgroundColor() {
    return m_cellSpecialization.getBackgroundColor();
  }

  public void setBackgroundColor(String c) {
    if (CompareUtility.notEquals(m_cellSpecialization.getBackgroundColor(), c)) {
      ICellSpecialization newStyle = m_cellSpecialization.copy();
      newStyle.setBackgroundColor(c);
      setValueInternal(BG_COLOR_BIT, newStyle);
    }
  }

  @Override
  public String getForegroundColor() {
    return m_cellSpecialization.getForegroundColor();
  }

  public void setForegroundColor(String c) {
    if (CompareUtility.notEquals(m_cellSpecialization.getForegroundColor(), c)) {
      ICellSpecialization newStyle = m_cellSpecialization.copy();
      newStyle.setForegroundColor(c);
      setValueInternal(FG_COLOR_BIT, newStyle);
    }
  }

  @Override
  public FontSpec getFont() {
    return m_cellSpecialization.getFont();
  }

  public void setFont(FontSpec f) {
    if (m_cellSpecialization.getFont() == null && f != null || m_cellSpecialization.getFont() != null && f == null || CompareUtility.notEquals(m_cellSpecialization.getFont(), f)) {
      ICellSpecialization newStyle = m_cellSpecialization.copy();
      newStyle.setFont(f);
      setValueInternal(FONT_BIT, newStyle);
    }
  }

  @Override
  public boolean isEditable() {
    return m_cellSpecialization.isEditable();
  }

  public void setEditable(boolean b) {
    if (m_cellSpecialization instanceof CellStyle) {
      if (b) {
        ICellSpecialization newStyle = new CellExtension(m_cellSpecialization);
        newStyle.setEditable(b);
        setValueInternal(EDITABLE_BIT, newStyle);
      }
    }
    else if (m_cellSpecialization.isEditable() != b) {
      ICellSpecialization newStyle = m_cellSpecialization.copy();
      newStyle.setEditable(b);
      setValueInternal(EDITABLE_BIT, newStyle);
    }
  }

  @Override
  public void setHtmlEnabled(boolean b) {
    if (m_cellSpecialization.isHtmlEnabled() != b) {
      ICellSpecialization newStyle = m_cellSpecialization.copy();
      newStyle.setHtmlEnabled(b);
      setValueInternal(HTML_ENABLED_BIT, newStyle);
    }
  }

  @Override
  public boolean isHtmlEnabled() {
    return m_cellSpecialization.isHtmlEnabled();
  }

  @Override
  public ICellObserver getObserver() {
    return m_observer;
  }

  public void setObserver(ICellObserver observer) {
    m_observer = observer;
  }

  private void setValueInternal(int bitPos, ICellSpecialization cellSpecialization) {
    synchronized (SHARED_STYLE_STORE_LOCK) {
      CellStyle newStyle = cellSpecialization.getCellStyle();
      CellStyle sharedStyle = SHARED_STYLE_STORE.get(newStyle);
      if (sharedStyle == null) {
        SHARED_STYLE_STORE.put(newStyle, newStyle);
        m_cellSpecialization = cellSpecialization.reconcile(newStyle);
      }
      else {
        m_cellSpecialization = cellSpecialization.reconcile(sharedStyle);
      }
    }
    notifyObserver(bitPos);
  }

  @Override
  public IMultiStatus getErrorStatus() {
    return m_errorStatus;
  }

  /**
   * @deprecated use {@link #addErrorStatus(String)}, will be removed in Scout 6.1
   */
  @Deprecated
  public void setErrorStatus(String message) {
    addErrorStatus(new DefaultFieldStatus(message));
  }

  /**
   * Set the error status of the cell or <code>null</code> in case of no error.
   *
   * @deprecated use {@link #addErrorStatus(IStatus)}, will be removed in Scout 6.1
   **/
  @Deprecated
  public void setErrorStatus(IStatus status) {
    setErrorStatusInternal(ensureMultiStatus(status));
  }

  public void clearErrorStatus() {
    setErrorStatusInternal(null);
  }

  public void addErrorStatus(String message) {
    addErrorStatus(new DefaultFieldStatus(message));
  }

  /**
   * Adds an error status
   */
  public void addErrorStatus(IStatus newStatus) {
    final MultiStatus status = ensureMultiStatus(getErrorStatusInternal());
    status.add(newStatus);
    setErrorStatusInternal(status);
    notifyObserver(ERROR_STATUS_BIT);
  }

  /**
   * Adds an error status
   */
  public void addErrorStatuses(List<IStatus> newStatus) {
    final MultiStatus status = ensureMultiStatus(getErrorStatusInternal());
    status.addAll(newStatus);
    setErrorStatusInternal(status);
    notifyObserver(ERROR_STATUS_BIT);
  }

  /**
   * Remove IStatus of a specific type
   */
  public void removeErrorStatus(Class<? extends IStatus> statusClazz) {
    final IMultiStatus ms = getErrorStatusInternal();
    if (ms != null) {
      ms.removeAll(statusClazz);
      if (ms.getChildren().isEmpty()) {
        clearErrorStatus();
      }
      notifyObserver(ERROR_STATUS_BIT);
    }
  }

  private MultiStatus ensureMultiStatus(IStatus s) {
    if (s instanceof MultiStatus) {
      return (MultiStatus) s;
    }
    final MultiStatus ms = new MultiStatus();
    if (s != null) {
      ms.add(s);
    }
    return ms;
  }

  private IMultiStatus getErrorStatusInternal() {
    return m_errorStatus;
  }

  public void setErrorStatusInternal(IMultiStatus status) {
    m_errorStatus = status;
  }

  private void notifyObserver(int changedBit) {
    if (getObserver() != null) {
      getObserver().cellChanged(this, changedBit);
    }
  }

  /**
   * @return true, if it contains an error status with severity &gt;= IStatus.ERROR
   */
  public boolean hasError() {
    IStatus errorStatus = getErrorStatus();
    return errorStatus != null && (errorStatus.getSeverity() >= IStatus.ERROR);
  }

  @Override
  public boolean isContentValid() {
    return !hasError() && isMandatoryFulfilled();
  }

  @Override
  public boolean isMandatoryFulfilled() {
    return !isMandatory() || getValue() != null;
  }

  @Override
  public String toString() {
    String s = getText();
    if (s == null) {
      s = StringUtility.emptyIfNull(getValue());
    }
    return s;
  }

}
