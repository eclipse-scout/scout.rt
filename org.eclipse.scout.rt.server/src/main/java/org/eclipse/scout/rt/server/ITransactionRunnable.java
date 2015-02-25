package org.eclipse.scout.rt.server;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.scout.commons.exception.ProcessingException;

/**
 * Runnable for ServerJobs
 * 
 * @since 4.2
 */
public interface ITransactionRunnable {

  /**
   * @param monitor
   *          for progress tracking and cancelation
   * @return execution status
   */
  IStatus run(IProgressMonitor monitor) throws ProcessingException;

}
