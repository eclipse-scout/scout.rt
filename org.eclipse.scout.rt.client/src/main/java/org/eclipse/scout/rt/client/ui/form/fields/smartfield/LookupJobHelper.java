package org.eclipse.scout.rt.client.ui.form.fields.smartfield;

import org.eclipse.scout.rt.client.context.ClientRunContexts;
import org.eclipse.scout.rt.platform.job.IBlockingCondition;
import org.eclipse.scout.rt.platform.job.IFuture;
import org.eclipse.scout.rt.platform.job.Jobs;

/**
 * Job helper methods to be included later in the job framework.
 */
public final class LookupJobHelper {

  private LookupJobHelper() {
  }

  /**
   * await result while freeing model thread
   */
  public static <T> T await(IFuture<T> futureRes) {
    final IBlockingCondition bc = Jobs.newBlockingCondition(true);
    futureRes.whenDone(event -> bc.setBlocking(false), ClientRunContexts.copyCurrent());
    bc.waitFor();

    return futureRes.awaitDoneAndGet();
  }

  /**
   * await result while freeing model thread
   */
  public static <T> void awaitDone(IFuture<T> futureRes) {
    final IBlockingCondition bc = Jobs.newBlockingCondition(true);
    futureRes.whenDone(event -> bc.setBlocking(false), ClientRunContexts.copyCurrent());
    bc.waitFor();
    futureRes.awaitDone();
  }

}
