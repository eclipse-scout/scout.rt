# Migrate a Scout JS project from jQuery Promise/Deferred to native Promise

**Context:** This project builds on `@eclipse-scout/core` (Scout JS), which has already completed this same migration internally — its `Deferred` class, `ajax.*` helpers, `Call`/`AjaxCall`, `Widget.when()`, etc. are all native-Promise-based now. Your job is to bring this project's *own* code in line: eliminate `JQuery.Promise`/`JQuery.Deferred` types, `$.Deferred()`, and `.done()/.fail()/.always()` usage, replacing them with native `Promise`/`Deferred` (imported from `@eclipse-scout/core`) and `.then()/.catch()/.finally()`.

You cannot assume you can build or run tests in this environment — verify everything by reading code and cross-checking against the already-migrated base classes in `@eclipse-scout/core`, and report your changes clearly so a human can run the real test suite afterward.

## 1. Survey properly before estimating scope

Grep for **all** of these patterns, not just type annotations — a file can use `.done()/.fail()/.always()` on a promise with no `JQuery.Promise` type anywhere in sight, and a narrower grep will silently miss it (this happened during the reference migration: an initial pass using only `JQuery\.(Promise|Deferred)|\$\.Deferred\(` missed several real files with raw `.done()/.fail()`):

```
JQuery\.(Promise|Deferred)|\$\.Deferred\(|\.done\(|\.fail\(|\.always\(
```

Run this across every module before giving a file/effort estimate. Fix foundational/shared modules first (whatever other modules in this project depend on), then work outward.

## 2. Mechanical vs. manual changes — don't treat them the same

- **Type-only** (`JQuery.Promise<T>` → `Promise<T>`) is safe to batch with `sed` across a whole file, **except** the 2-argument jQuery form `JQuery.Promise<TResolve, TReject>` — drop the second type param entirely (native `Promise<T>` has no typed-rejection equivalent); handle those by hand.
- **Any real `$.Deferred()`, `.done()`, `.fail()`, `.always()` usage needs individual, careful review** — see the translation rules below. Do not regex-replace these.
- For every `protected override` method whose return type you're touching, verify the **exact** signature against the base class in `@eclipse-scout/core` (e.g. `Form._load()`, `Form._save()`, `PageWithTable._loadTableData()`, `ValueField._validateValue()`) — TS allows a narrower override, but confirm it, don't assume.

## 3. `.done()/.fail()/.always()` vs. `.then()/.catch()/.finally()` — the semantic differences

These are not interchangeable one-for-one. Know the actual differences before translating any call site:

**a) `.done()/.fail()` do not chain return values; `.then()` does.** Each `.done()` in a chain receives the *original* resolved value, not the previous `.done()`'s return value:
```js
// jQuery — every .done() gets the SAME original value:
promise.done(x => { console.log(x); return 123; })
       .done(x => console.log(x))
       .done(x => console.log(x));
// -> abc, abc, abc  (assuming promise resolves with 'abc')

// native — each .then() gets the PREVIOUS .then()'s return value:
promise.then(x => { console.log(x); return 123; })
       .then(x => console.log(x))
       .then(x => console.log(x));
// -> abc, 123, undefined
```
When migrating a `.done()` chain that relied on every callback seeing the same original value, either make each `.then()` explicitly `return x` to pass it along, or restructure so only the first callback needs the value.

**b) All `.fail()` handlers run; only the first `.catch()` does.** Chained `.fail()`s are independent registrations on the same deferred and all fire. A `.catch()`, if it doesn't rethrow, turns the chain back into a *resolved* one, so a second chained `.catch()` never sees anything to catch:
```js
$.Deferred().reject('abc')
  .fail(e => console.log('fail', e))
  .fail(e => console.log('fail2', e))
  .always(x => console.log('always', x));
// -> fail abc, fail2 abc, always abc

await Promise.reject('abc')
  .catch(e => console.log('catch', e))
  .catch(e => console.log('catch2', e))
  .finally(x => console.log('finally', x));
// -> catch abc, finally   (catch2 never runs)
```
If code depends on multiple independent failure handlers all seeing the rejection, don't chain `.catch()`s — attach them all directly to the *original* promise instead (`promise.catch(a); promise.catch(b);`), or consolidate into one handler.

**c) `.finally()` receives no arguments; `.always()` receives the resolved/rejected value.** This is easy to miss because both look like "run regardless of outcome" callbacks. Any `.always(fn)` where `fn` actually reads its argument needs that logic moved into `.then()`/`.catch()` — it cannot become `.finally(fn)` unchanged. Watch especially for a *bound* function passed to `.always()`, where the promise's value becomes an extra trailing argument:
```js
// original:
returnValue.always(this._resizeToFit.bind(this, column, maxWidth));
// _resizeToFit(column, maxWidth, calculatedSize) -- calculatedSize comes from .always()'s value.

// WRONG migration — calculatedSize would always be undefined:
returnValue.finally(this._resizeToFit.bind(this, column, maxWidth));

// correct: move the value-dependent logic into then()/catch(), or capture it explicitly:
returnValue.then(
  calculatedSize => this._resizeToFit(column, maxWidth, calculatedSize),
  calculatedSize => this._resizeToFit(column, maxWidth, calculatedSize)
);
```
So the rule for `.always(fn)` → `.finally(fn)` (from earlier migration work in this codebase) needs this extra condition: safe only if the result is returned/chained onward, `fn` never throws, **and `fn` doesn't need the resolved/rejected value**.

**d) A `.done()` callback throwing does not invoke `.fail()`/`.always()` — it's just an uncaught exception.** A `.then()` callback throwing correctly invokes the chain's `.catch()`. If migrated code seems to have swallowed errors that used to escape loudly (or vice versa), check whether the original relied on this asymmetry.

## 4. Multi-argument resolve/reject has no native equivalent

jQuery's `deferred.resolve(a, b, c)` supports multiple arguments; native `Promise` resolves with exactly one value. This affects several common patterns:

- `deferred.resolve(a, b, c)` → must resolve with a single value (wrap in an array/object), and every `.done((a, b, c) => ...)` consumer of that specific deferred needs updating to destructure it instead.
- `$.when(p1, p2, p3).done((a, b, c) => ...)` → `Promise.all([p1, p2, p3]).then(([a, b, c]) => ...)` — arguments move from separate parameters to a destructured array.
- The same applies to any `$.promiseAll()`-style helper — note the *arity* change, not just the function name:
  ```js
  // old:
  $.promiseAll([...]).then((...statusArr) => { ... });
  // new — statusArr is already the array, no rest-spread:
  $.promiseAll([...]).then(statusArr => { ... });
  ```
- Ajax success handlers used to receive `(data, textStatus, jqXHR)` as separate arguments — the native-Promise-based `ajax.*` helpers only ever resolve with the response body. If you need the status/jqXHR/error details, check whether the framework wraps them in something like `AjaxError` on the rejection path — use that instead of expecting extra success-handler arguments.

## 5. `promise.state()` / `deferred.state()` don't exist on native Promise

There is no synchronous way to query whether a native `Promise` is pending/resolved/rejected. If code relies on `.state()`, check whether the framework's own `Deferred` class (`promises.ts` in `@eclipse-scout/core`) exposes an equivalent wrapper (`state(): 'pending' | 'resolved' | 'rejected'`) — use that instead of a raw native Promise if you need to introspect state, or track the state yourself alongside the promise.

## 6. The trap that costs the most time: fake timers and microtasks

**First, the foundational fact — `jasmine.clock().tick()` never drains microtasks, only fake timers:**
```js
jasmine.clock().install();

// jQuery: $.resolvedPromise()'s .then() is scheduled via setTimeout, so it IS a fake timer:
$.resolvedPromise('abc').then(x => console.log(x));
jasmine.clock().tick(0);
console.log('after');
// -> 'abc', 'after'

// native: Promise.resolve()'s .then() is a microtask, tick() cannot touch it:
Promise.resolve('abc').then(x => console.log(x));
jasmine.clock().tick(0);
console.log('after');
// -> 'after', 'abc'   (the native .then() only fires once the current synchronous code
//                      finishes and the microtask queue is drained — e.g. after an `await`)
```
`jasmine.clock()` genuinely cannot be used to drive native-Promise-based code. Prefer awaiting something concrete (a returned promise, `widget.when('propertyChange:xyz')`) or `await sleep(...)`.

**Second, the subtler version of the same problem — jQuery's own `.then()` becomes a fake timer too:**
- `.done()/.fail()/.always()` fire **synchronously**, even on an already-settled deferred — this has never changed.
- `.then()/.catch()` on a jQuery Deferred (jQuery ≥ 3, Promises/A+ compliance) are **always deferred via `window.setTimeout`** — a real macrotask, not a microtask. (Verify in `node_modules/jquery/dist/jquery.js` if in doubt — search for `window.setTimeout( process )` inside `Deferred.then`.)

Consequence: once `jasmine.clock().install()` is active, any code that still calls `.then()/.catch()` on a raw jQuery Deferred/jqXHR has that reaction turned into a **fake timer**, invisible to `flushMicrotasks()` (which only drains microtasks). A single ajax response's full processing can require alternating fake-timer hops (jQuery's internal `.then()`) and native-microtask hops (the rest of your Promise chain), sometimes several rounds deep if success also schedules a *new* timer (e.g. a poller re-arming itself). Manually guessing `await flushMicrotasks(N); jasmine.clock().tick(M);` combinations is fragile and was the source of most of the flakiness in the reference migration.

**Fix: use `jasmine.clock().autoTick()`** (jasmine-core ≥ 5.7) instead of manual `tick()`/`flushMicrotasks()` pairs. Call it once, right after `install()`:
```ts
beforeEach(() => {
  jasmine.clock().install();
  jasmine.clock().autoTick();
});
afterEach(() => {
  jasmine.clock().uninstall(); // also cleanly stops the auto-tick loop
});
```
It continuously fires the next due fake timer and yields to a real macrotask in between (letting any native-Promise chain — and anything it schedules — run to completion) automatically, without you needing to know the hop count. It still fast-forwards long fake delays (e.g. a 30s retry interval) without the test actually waiting 30 real seconds. Combine it with real `await sleep(N)` (a small real wait, e.g. 50ms) at points where you just need to let things settle with no specific event to await, and with precise signals (`widget.when('propertyChange:xyz')`, a returned promise, a request-count check) wherever the code exposes one — always prefer a precise signal over a guessed wait when one is available.

When wrapping a raw `$.ajax()`/jqXHR directly, prefer this project's/framework's own `ajax.getJson()`/`ajax.postJson()`/`AjaxCall` (native-Promise, already migrated) over hand-rolling a new wrapper. If you must wrap a raw jqXHR yourself, use the **single two-argument** `jqXHR.then(onFulfilled, onRejected)`, not `jqXHR.then(onFulfilled).catch(onRejected)`. The chained form costs the rejection path *two* jQuery-internal scheduling hops instead of one — a real asymmetry that can flip the relative order in which two competing async operations (e.g. an aborted call vs. a fresh one) settle, compared to what the pre-migration code guaranteed.

**Audit your own test helpers for the same disease.** Any shared test utility that "flushes" queued ajax calls or their responses using `jasmine.clock().tick()` alone (implicitly assuming synchronous jQuery-Deferred resolution — e.g. a `sendQueuedAjaxCalls()`-style helper) will likely stop working once the production code it drives switches from `.done()` to `.then()`. `tick()` cannot flush the resulting native-Promise chain. Write an `async` equivalent that awaits a real completion signal instead (e.g. the framework's `session.whenRequestsDone()`), convert every caller from the sync helper to the async one, and mark those tests `async`.

## 7. The other big class of bug: fire-and-forget promises and unhandled rejections

Native Promises trigger a global "unhandled rejection" error when nothing ever attaches a `.catch()`/`.then(_, onRejected)`/`.finally()`-with-rethrow to them — Karma/Jasmine treats this as a failing test. jQuery has no such mechanism at all, so every place a Promise-returning call's result was neither returned, stored, nor awaited was **silently swallowed under jQuery** and is now a genuine, loud failure. This is not a new bug you're introducing — it's a pre-existing gap the migration makes visible. Concrete places this shows up:

- **Expected errors need explicit handling.** E.g. aborting an in-flight lookup/search call is expected to reject — make sure the abort path has a real `.catch()`, not just an assumption that nobody's listening (see any `LookupBox`/search-abort style code as an example of the pattern to check).
- **Stray timers firing after a test (or the object) is gone.** A `setTimeout` scheduled by, say, a debounced "send" method can fire after the test ends and hit a partially torn-down mock, producing an unhandled rejection in an unrelated later test's `afterAll`. Any `destroy()`-style method should explicitly `clearTimeout` whatever it scheduled (check the base framework's own `Session.destroy()`/`HybridManager.destroy()`-equivalents for the pattern, and make sure your test teardown actually calls `destroy()`/cancels those timers between tests).
- **A leftover ajax call executing after the test ends.** Make sure `jasmine.Ajax.install()`/`uninstall()` bracket every test that can trigger a request, so a request that fires later doesn't escape as a real network call.
- **For genuinely expected rejections, write `expectAsync(promise).toBeRejected()`** rather than letting the rejection go unhandled or wrapping it in a try/catch that swallows the assertion value. But note the timing trap: **the call itself is what attaches the handler**; the `await` of its result can happen later. If you call `expectAsync(...)` only at the end of a test, after the promise already rejected earlier in the test body, Karma has already flagged it as unhandled by the time you get there:
  ```ts
  let promise = doSomethingThatWillReject();
  let rejected = expectAsync(promise).toBeRejected(); // attach now
  // ... rest of the test, including whatever triggers the rejection ...
  await rejected; // await later
  ```

For everything else that's genuinely fire-and-forget by design (typical for UI event handlers where nobody awaits the result): add a no-op guard — `.catch(() => {})` or `.then(fn, fn)` — with a one-line comment saying it's intentional. Don't skip this; it's cheap and prevents console noise from masking real issues later.

## 8. Staleness/supersession checks: replace counters with identity checks

Watch for patterns like:
```ts
if (this._pendingCounter === 1) {
  this._onSuccess(value);
}
```
used to detect "is this the *last* of several overlapping async operations, ignore the superseded ones." This relied on old jQuery's `.done()/.fail()` firing **synchronously**, which made superseded (e.g. aborted) operations reliably settle before the current one. Under native Promise there's no such guarantee — whichever settles first depends on microtask/macrotask interleaving, which can differ per call site and even reverse under mocked-vs-real timing. Replace the counter check with an **identity check** against the most-recently-started operation:
```ts
this._pendingOperation = myPromise;
myPromise.then(
  value => { if (this._pendingOperation === myPromise) this._onSuccess(value); ... },
  error => { if (this._pendingOperation === myPromise) this._onFailure(error); ... }
);
```
This is correct regardless of settle order. If you find this bug, also check whether the same class (or a subclass, e.g. `SmartField` extends `ValueField`) shares the affected method — the fix usually applies broadly.

## 9. Test-only gotchas (recap)

- `FakeXMLHttpRequest already completed` from jasmine-ajax almost always means your synchronization assumption was wrong — some earlier async step you assumed had completed (e.g. a poller's next request being sent) actually hadn't yet, so `jasmine.Ajax.requests.mostRecent()` returns a stale, already-answered request. Fix by waiting for the actual effect (e.g. poll count increased), not a fixed tick/sleep amount.
- See §6 for `expectAsync` timing and §7 for the general unhandled-rejection checklist.

## 10. General workflow

1. Grep the whole codebase with the full pattern from §1; don't trust a narrower first pass.
2. Fix foundational modules first.
3. For each file: bulk-swap pure type annotations, then hand-review every `$.Deferred()`/`.done/.fail/.always` site using the rules in §3–§5 and §8.
4. Grep again after each module with the same full pattern to catch anything missed (do this even for modules you think are "done" — re-run the wider pattern, not just what caught your eye).
5. Report a clear diff summary; you likely can't run this project's test suite yourself, so flag anything you're less than fully confident about rather than asserting it's correct.
