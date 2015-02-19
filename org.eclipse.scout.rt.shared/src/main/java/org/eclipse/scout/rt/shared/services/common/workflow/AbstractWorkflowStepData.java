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
package org.eclipse.scout.rt.shared.services.common.workflow;

import java.io.Serializable;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import org.eclipse.scout.commons.CollectionUtility;

public abstract class AbstractWorkflowStepData implements Serializable {
  private static final long serialVersionUID = 1L;

  /*
   * The following fields are used by the workflow framework
   */
  private long m_definitionNr;
  private String m_definitionText;
  private boolean m_definitionActive = true;

  private boolean m_suspendPossible;
  private boolean m_cancelPossible;
  private boolean m_finishPossible;

  private Date m_completionDate;
  private String m_comment;

  /*
   * The following fields are not diretcly used by the workflow framework
   */
  private long m_responsibleUserNr;
  private Date m_startDate;
  private Date m_dueDate;
  private long m_completionUserNr;

  private long m_stepNr;
  private List<String> m_documents;

  public long getDefinitionNr() {
    return m_definitionNr;
  }

  public void setDefinitionNr(long l) {
    m_definitionNr = l;
  }

  public String getDefinitionText() {
    return m_definitionText;
  }

  public void setDefinitionText(String s) {
    m_definitionText = s;
  }

  public boolean isDefinitionActive() {
    return m_definitionActive;
  }

  public void setDefinitionActive(boolean b) {
    m_definitionActive = b;
  }

  public long getStepNr() {
    return m_stepNr;
  }

  public void setStepNr(long l) {
    m_stepNr = l;
  }

  public long getResponsibleUserNr() {
    return m_responsibleUserNr;
  }

  public void setResponsibleUserNr(long l) {
    m_responsibleUserNr = l;
  }

  public Date getStartDate() {
    return m_startDate;
  }

  public void setStartDate(Date startDate) {
    m_startDate = startDate;
  }

  public Date getDueDate() {
    return m_dueDate;
  }

  public void setDueDate(Date d) {
    m_dueDate = d;
  }

  /**
   * @return true if the step is completed and the workflow may continue with
   *         the next step
   */
  public boolean isCompleted() {
    return m_completionDate != null;
  }

  public Date getCompletionDate() {
    return m_completionDate;
  }

  public void setCompletionDate(Date d) {
    m_completionDate = d;
  }

  public long getCompletionUserNr() {
    return m_completionUserNr;
  }

  public void setCompletionUserNr(long l) {
    m_completionUserNr = l;
  }

  public String getComment() {
    return m_comment;
  }

  public void setComment(String s) {
    m_comment = s;
  }

  public void addDocument(String s) {
    m_documents = CollectionUtility.appendList(m_documents, s);
  }

  public List<String> getDocuments() {
    return m_documents;
  }

  public void setDocuments(Collection<String> d) {
    m_documents = CollectionUtility.arrayList(d);
  }

  public boolean isSuspendPossible() {
    return m_suspendPossible;
  }

  public void setSuspendPossible(boolean b) {
    m_suspendPossible = b;
  }

  public boolean isCancelPossible() {
    return m_cancelPossible;
  }

  public void setCancelPossible(boolean b) {
    m_cancelPossible = b;
  }

  public boolean isFinishPossible() {
    return m_finishPossible;
  }

  public void setFinishPossible(boolean b) {
    m_finishPossible = b;
  }

}
