/*
 * Copyright (c) 2010, 2026 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.platform.util.concurrent;

import static org.junit.Assert.*;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

import org.eclipse.scout.rt.platform.context.RunContexts;
import org.eclipse.scout.rt.platform.job.IFuture;
import org.eclipse.scout.rt.platform.job.Jobs;
import org.eclipse.scout.rt.platform.util.Assertions.AssertionException;
import org.eclipse.scout.rt.platform.util.FinalValue;
import org.eclipse.scout.rt.testing.platform.runner.PlatformTestRunner;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(PlatformTestRunner.class)
public class CompletableTest {

  @Test
  public void testWrap() {
    assertThrows(AssertionException.class, () -> Completable.wrap(null));

    assertNotNull(Completable.wrap(CompletableFuture.completedStage("test")));
  }

  @Test
  public void testCompleted() {
    Completable<Object> c = Completable.completed(null);
    assertNotNull(c);
    assertTrue(c.isDone());
    assertFalse(c.isCancelled());
    assertNull(c.getNow("absent value is not required"));
    assertNull(c.awaitDoneAndGet());

    Completable<String> c1 = Completable.completed("completed");
    assertNotNull(c1);
    assertTrue(c1.isDone());
    assertFalse(c1.isCancelled());
    assertEquals("completed", c1.getNow("absent value is not required"));
    assertEquals("completed", c1.awaitDoneAndGet());
  }

  @Test
  public void testAllOf_empty() {
    Completable<Void> c = Completable.allOf(Stream.empty());
    assertNotNull(c);
    assertTrue(c.isDone());
    assertFalse(c.isCancelled());
    assertNull(c.getNow(null));
    assertNull(c.awaitDoneAndGet());
  }

  @Test
  public void testAllOf_singleElement() {
    Completable<Void> c = Completable.allOf(Stream.of(Completable.completed("done")));
    assertNotNull(c);
    assertTrue(c.isDone());
    assertFalse(c.isCancelled());
    assertNull(c.getNow(null));
    assertNull(c.awaitDoneAndGet());
  }

  @Test
  public void testAllOf_multipleElement() {
    CompletableFuture<String> f = new CompletableFuture<>();
    Completable<Void> c = Completable.allOf(Stream.of(Completable.wrap(f), Completable.completed("done")));
    assertNotNull(c);
    assertFalse(c.isDone());
    assertFalse(c.isCancelled());

    f.complete("completed");
    assertTrue(c.isDone());
    assertFalse(c.isCancelled());
  }

  @Test
  public void testUnwrap() {
    CompletableFuture<String> f = new CompletableFuture<>();
    Completable<String> cf = Completable.wrap(f);
    assertNotNull(cf);
    assertSame(f, cf.unwrap());

    CompletionStage<String> s = CompletableFuture.completedStage("completed");
    Completable<String> cs = Completable.wrap(s);
    assertNotNull(cs);
    assertSame(s, cs.unwrap());
  }

  @Test
  public void testToCompletableFuture() {
    CompletableFuture<String> f = new CompletableFuture<>();
    Completable<String> cf = Completable.wrap(f);
    assertNotNull(cf);
    assertSame(f, cf.toCompletableFuture());

    CompletionStage<String> s = CompletableFuture.completedStage("completed");
    Completable<String> cs = Completable.wrap(s);
    assertNotNull(cs);
    assertNotSame(s, cs.toCompletableFuture());
    assertEquals("completed", cs.toCompletableFuture().getNow("valueIfAbsent"));
  }

  // --- non-blocking api methods (like CompletionStage) ----------------------

  @Test
  public void testThenApply() {
    CompletableFuture<String> f = new CompletableFuture<>();
    Completable<String> c = Completable.wrap(f);

    Completable<String> c1 = c.thenApply(r -> r + " thenApply");

    f.complete("completed");
    assertEquals("completed", c.getNow("valueIfAbsent"));
    assertEquals("completed thenApply", c1.getNow("valueIfAbsent"));
  }

  @Test
  public void testThenAccept() {
    CompletableFuture<String> f = new CompletableFuture<>();
    Completable<String> c = Completable.wrap(f);

    FinalValue<String> result = new FinalValue<>();
    assertFalse(result.isSet());

    Completable<Void> c1 = c.thenAccept(result::set);
    assertFalse(result.isSet());
    assertFalse(c1.isDone());

    f.complete("completed");
    assertEquals("completed", c.getNow("valueIfAbsent"));
    assertTrue(c1.isDone());
    assertTrue(result.isSet());
    assertEquals("completed", result.get());
  }

  @Test
  public void testThenRun() {
    CompletableFuture<String> f = new CompletableFuture<>();
    Completable<String> c = Completable.wrap(f);

    FinalValue<Boolean> result = new FinalValue<>();
    assertFalse(result.isSet());

    Completable<Void> c1 = c.thenRun(() -> result.set(true));
    assertFalse(result.isSet());
    assertFalse(c1.isDone());

    f.complete("completed");
    assertEquals("completed", c.getNow("valueIfAbsent"));
    assertTrue(c1.isDone());
    assertTrue(result.isSet());
    assertTrue(result.get());
  }

  @Test
  public void testHandle() {
    CompletableFuture<String> f = new CompletableFuture<>();
    Completable<String> c = Completable.wrap(f);

    Completable<String> c1 = c.handle((r, e) -> e == null ? r + " handle" : e.getMessage());

    f.complete("completed");
    assertEquals("completed", c.getNow("valueIfAbsent"));
    assertEquals("completed handle", c1.getNow("valueIfAbsent"));
  }

  @Test
  public void testHandle_exception() {
    CompletableFuture<String> f = new CompletableFuture<>();
    Completable<String> c = Completable.wrap(f);

    Completable<String> c1 = c.handle((r, e) -> e == null ? r + " handle" : e.getMessage());

    f.completeExceptionally(new RuntimeException("exception"));
    assertThrows(RuntimeException.class, () -> c.getNow("valueIfAbsent"));
    assertEquals("exception", c1.getNow("valueIfAbsent"));
  }

  @Test
  public void testWhenComplete() {
    CompletableFuture<String> f = new CompletableFuture<>();
    Completable<String> c = Completable.wrap(f);

    FinalValue<String> result = new FinalValue<>();
    assertFalse(result.isSet());

    Completable<String> c1 = c.whenComplete((r, e) -> result.set(e == null ? r + " whenComplete" : e.getMessage()));
    assertFalse(result.isSet());
    assertFalse(c1.isDone());

    f.complete("completed");

    assertTrue(c1.isDone());
    assertEquals("completed", c.getNow("valueIfAbsent"));
    assertTrue(result.isSet());
    assertEquals("completed whenComplete", result.get());
    assertEquals("completed", c1.getNow("valueIfAbsent"));
  }

  @Test
  public void testWhenComplete_exception() {
    CompletableFuture<String> f = new CompletableFuture<>();
    Completable<String> c = Completable.wrap(f);

    FinalValue<String> result = new FinalValue<>();
    assertFalse(result.isSet());

    Completable<String> c1 = c.whenComplete((r, e) -> result.set(e == null ? r + " whenComplete" : e.getMessage()));
    assertFalse(result.isSet());
    assertFalse(c1.isDone());

    f.completeExceptionally(new RuntimeException("exception"));

    assertTrue(c1.isDone());
    assertThrows(RuntimeException.class, () -> c.getNow("valueIfAbsent"));
    assertTrue(result.isSet());
    assertEquals("exception", result.get());
    assertThrows(RuntimeException.class, () -> c1.getNow("valueIfAbsent"));
  }

  @Test
  public void testExceptionally() {
    CompletableFuture<String> f = new CompletableFuture<>();
    Completable<String> c = Completable.wrap(f);

    Completable<String> c1 = c.exceptionally(e -> e.getMessage() + " exceptionally");

    f.complete("completed");
    assertEquals("completed", c.getNow("valueIfAbsent"));
    assertEquals("completed", c1.getNow("valueIfAbsent"));
  }

  @Test
  public void testExceptionally_exception() {
    CompletableFuture<String> f = new CompletableFuture<>();
    Completable<String> c = Completable.wrap(f);

    Completable<String> c1 = c.exceptionally(e -> e.getMessage() + " exceptionally");

    f.completeExceptionally(new RuntimeException("exception"));
    assertThrows(RuntimeException.class, () -> c.getNow("valueIfAbsent"));
    assertEquals("exception exceptionally", c1.getNow("valueIfAbsent"));
  }

  // --- blocking api methods (like IFuture) ----------------------------------

  @Test
  public void testIsDone_complete() {
    CompletableFuture<String> f = new CompletableFuture<>();
    Completable<String> c = Completable.wrap(f);

    assertFalse(c.isDone());
    assertFalse(c.isCancelled());

    f.complete("completed");
    assertTrue(c.isDone());
    assertFalse(c.isCancelled());
  }

  @Test
  public void testIsDone_completeExceptionally() {
    CompletableFuture<String> f = new CompletableFuture<>();
    Completable<String> c = Completable.wrap(f);

    assertFalse(c.isDone());
    assertFalse(c.isCancelled());

    f.completeExceptionally(new UnsupportedOperationException());
    assertTrue(c.isDone());
    assertFalse(c.isCancelled());
  }

  @Test
  public void testIsCancelled() {
    CompletableFuture<String> f = new CompletableFuture<>();
    Completable<String> c = Completable.wrap(f);

    assertFalse(c.isCancelled());
    assertFalse(c.isDone());

    f.cancel(false);
    assertTrue(c.isCancelled());
    assertTrue(c.isDone());
  }

  @Test
  public void testGetNow() {
    CompletableFuture<String> f = new CompletableFuture<>();
    Completable<String> c = Completable.wrap(f);

    assertEquals("valueIfAbsent", c.getNow("valueIfAbsent"));

    f.complete("completed");
    assertEquals("completed", c.getNow("valueIfAbsent"));
  }

  @Test
  public void testGetNow_cancelled() {
    CompletableFuture<String> f = new CompletableFuture<>();
    Completable<String> c = Completable.wrap(f);

    assertEquals("valueIfAbsent", c.getNow("valueIfAbsent"));

    f.cancel(true);
    assertThrows(FutureCancelledError.class, () -> c.getNow("valueIfAbsent"));
  }

  @Test
  public void testGetNow_exceptionallyCompleted() {
    CompletableFuture<String> f = new CompletableFuture<>();
    Completable<String> c = Completable.wrap(f);

    assertEquals("valueIfAbsent", c.getNow("valueIfAbsent"));

    RuntimeException rt = new RuntimeException("rt");
    f.completeExceptionally(rt);
    RuntimeException thrownException = assertThrows(RuntimeException.class, () -> c.getNow("valueIfAbsent"));
    assertSame(rt, thrownException);
  }

  @Test
  public void testAwaitDone() throws Exception {
    CompletableFuture<String> f = new CompletableFuture<>();
    Completable<String> c = Completable.wrap(f);

    CountDownLatch jobScheduledLatch = new CountDownLatch(1);

    IFuture<String> jobFuture = Jobs.schedule(() -> {
      assertFalse(c.isDone());
      jobScheduledLatch.countDown();
      c.awaitDone();
      return "done";
    }, Jobs.newInput().withRunContext(RunContexts.copyCurrent()));

    assertTrue(jobScheduledLatch.await(5, TimeUnit.SECONDS));
    f.complete("completed");
    assertEquals("done", jobFuture.awaitDoneAndGet(5, TimeUnit.SECONDS));
  }

  @Test(timeout = 5000)
  public void testAwaitDone_cancelled() {
    CompletableFuture<String> f = new CompletableFuture<>();
    Completable<String> c = Completable.wrap(f);

    f.cancel(false);
    c.awaitDone();
  }

  @Test
  public void testAwaitDone_exceptionallyCompleted() throws Exception {
    CompletableFuture<String> f = new CompletableFuture<>();
    Completable<String> c = Completable.wrap(f);

    CountDownLatch jobScheduledLatch = new CountDownLatch(1);

    IFuture<String> jobFuture = Jobs.schedule(() -> {
      assertFalse(c.isDone());
      jobScheduledLatch.countDown();
      c.awaitDone();
      return "done (exception is irrelevant)";
    }, Jobs.newInput().withRunContext(RunContexts.copyCurrent()));

    assertTrue(jobScheduledLatch.await(5, TimeUnit.SECONDS));
    f.completeExceptionally(new RuntimeException("exception"));
    assertEquals("done (exception is irrelevant)", jobFuture.awaitDoneAndGet(5, TimeUnit.SECONDS));
  }
}
