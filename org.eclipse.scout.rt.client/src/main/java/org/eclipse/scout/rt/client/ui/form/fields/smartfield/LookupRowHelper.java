package org.eclipse.scout.rt.client.ui.form.fields.smartfield;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

import org.eclipse.scout.rt.client.context.ClientRunContexts;
import org.eclipse.scout.rt.client.job.ModelJobs;
import org.eclipse.scout.rt.platform.Bean;
import org.eclipse.scout.rt.platform.job.IFuture;
import org.eclipse.scout.rt.platform.job.Jobs;
import org.eclipse.scout.rt.platform.util.CollectionUtility;
import org.eclipse.scout.rt.platform.util.concurrent.IRunnable;
import org.eclipse.scout.rt.shared.services.lookup.ILookupCall;
import org.eclipse.scout.rt.shared.services.lookup.ILookupRow;
import org.eclipse.scout.rt.shared.services.lookup.LookupCall;

/**
 * Utility methods for smartfield lookup using {@link ILookupRowProvider} and {@link LookupCall}.
 *
 * @since 5.2
 */
@Bean
public class LookupRowHelper {

  //TODO test exception cases (before, after, provide)

  /**
   * Synchronous lookup
   *
   * @return list of resulting {@link ILookupRow}s
   */
  public <T> List<? extends ILookupRow<T>> lookup(final ILookupRowProvider<T> provider, final ILookupCall<T> lookupCall) {
    if (lookupCall == null) {
      return CollectionUtility.emptyArrayList();
    }

    beforeProvide(provider, lookupCall);
    List<? extends ILookupRow<T>> result = provider.provide(lookupCall);
    return afterProvide(provider, lookupCall, result);
  }

  /**
   * Asynchronous lookup
   *
   * @return list of resulting {@link ILookupRow}s
   */
  public <T> IFuture<List<? extends ILookupRow<T>>> scheduleLookup(final ILookupRowProvider<T> provider, final ILookupCall<T> lookupCall) {
    return Jobs.schedule(new Callable<List<? extends ILookupRow<T>>>() {

      @Override
      public List<? extends ILookupRow<T>> call() throws Exception {
        if (lookupCall == null) {
          return CollectionUtility.emptyArrayList();
        }
        scheduleBeforeProvide(provider, lookupCall);
        final List<? extends ILookupRow<T>> lookupRes = provider.provide(lookupCall);
        return scheduleAfterProvide(provider, lookupCall, lookupRes);
      }

    }, Jobs.newInput()
        .withRunContext(ClientRunContexts.copyCurrent())
        .withName("Fetching lookup data [lookupCall={}]", lookupCall != null ? lookupCall.getClass().getName() : "")
        .withExceptionHandling(null, false));
  }

  private <T> void scheduleBeforeProvide(final ILookupRowProvider<T> provider, final ILookupCall<T> lookupCall) {
    ModelJobs.schedule(new IRunnable() {

      @Override
      public void run() throws Exception {
        beforeProvide(provider, lookupCall);
      }
    }, ModelJobs.newInput(ClientRunContexts.copyCurrent())
        .withName("Before lookup [lookupCall={}]", lookupCall.getClass().getName()))
        .awaitDone();
  }

  protected <T> void beforeProvide(final ILookupRowProvider<T> provider, final ILookupCall<T> lookupCall) {
    provider.beforeProvide(lookupCall);
  }

  private <T> List<? extends ILookupRow<T>> scheduleAfterProvide(final ILookupRowProvider<T> provider, final ILookupCall<T> lookupCall, final List<? extends ILookupRow<T>> lookupRes) {
    return ModelJobs.schedule(new Callable<List<? extends ILookupRow<T>>>() {

      @Override
      public List<? extends ILookupRow<T>> call() throws Exception {
        return afterProvide(provider, lookupCall, lookupRes);
      }
    },
        ModelJobs.newInput(ClientRunContexts.copyCurrent())
            .withName("After lookup [lookupCall={}]", lookupCall.getClass().getName()))
        .awaitDoneAndGet();
  }

  protected <T> List<? extends ILookupRow<T>> afterProvide(final ILookupRowProvider<T> provider, final ILookupCall<T> lookupCall, final List<? extends ILookupRow<T>> lookupRes) {
    List<ILookupRow<T>> postProcessingList = new ArrayList<>();
    postProcessingList.addAll(lookupRes);
    provider.afterProvide(lookupCall, postProcessingList);
    return postProcessingList;
  }

}
