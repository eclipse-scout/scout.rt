package org.eclipse.scout.rt.ui.html.selenium.junit;

/**
 * A category used to mark tests that are unstable on the CI build system but are successful when run locally. When the
 * CI build executes tests, this category is excluded. So you should only apply this category when there's no way to fix
 * the test for CI build - usually there's some sort of timing problem, when a test fails on CI build.
 */
public class UnstableCiTest implements UiTestCategory {

}
