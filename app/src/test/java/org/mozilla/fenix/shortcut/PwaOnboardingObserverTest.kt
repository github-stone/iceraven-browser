/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.mozilla.fenix.shortcut

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.navigation.NavController
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestCoroutineDispatcher
import mozilla.components.browser.state.action.ContentAction
import mozilla.components.browser.state.state.BrowserState
import mozilla.components.browser.state.state.createTab
import mozilla.components.browser.state.store.BrowserStore
import mozilla.components.feature.pwa.WebAppUseCases
import mozilla.components.support.test.ext.joinBlocking
import mozilla.components.support.test.rule.MainCoroutineRule
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mozilla.fenix.R
import org.mozilla.fenix.browser.BrowserFragmentDirections
import org.mozilla.fenix.ext.nav
import org.mozilla.fenix.utils.Settings

@ExperimentalCoroutinesApi
class PwaOnboardingObserverTest {

    private lateinit var store: BrowserStore
    private lateinit var lifecycleOwner: MockedLifecycleOwner
    private lateinit var pwaOnboardingObserver: PwaOnboardingObserver
    private lateinit var navigationController: NavController
    private lateinit var settings: Settings
    private lateinit var webAppUseCases: WebAppUseCases

    private val testDispatcher = TestCoroutineDispatcher()

    @get:Rule
    val coroutinesTestRule = MainCoroutineRule(testDispatcher)

    @Before
    fun setUp() {
        store = BrowserStore(
            BrowserState(
                tabs = listOf(
                    createTab(url = "https://firefox.com", id = "1")
                ), selectedTabId = "1"
            )
        )
        lifecycleOwner = MockedLifecycleOwner(Lifecycle.State.STARTED)
        navigationController = mockk(relaxed = true)
        settings = mockk(relaxed = true)
        webAppUseCases = mockk(relaxed = true)

        pwaOnboardingObserver = PwaOnboardingObserver(
            store = store,
            lifecycleOwner = lifecycleOwner,
            navController = navigationController,
            settings = settings,
            webAppUseCases = webAppUseCases
        )
    }

    @Test
    fun `GIVEN cfr should not yet be shown WHEN installable page is loaded THEN counter is incremented`() {
        pwaOnboardingObserver.start()
        every { webAppUseCases.isInstallable() } returns true

        store.dispatch(ContentAction.UpdateWebAppManifestAction("1", mockk())).joinBlocking()
        verify { settings.incrementVisitedInstallableCount() }
        verify(exactly = 0) { navigationController.nav(
            R.id.browserFragment,
            BrowserFragmentDirections.actionBrowserFragmentToPwaOnboardingDialogFragment())
        }
    }

    @Test
    fun `GIVEN cfr should be shown WHEN installable page is loaded THEN we navigate to onboarding fragment`() {
        pwaOnboardingObserver.start()
        every { webAppUseCases.isInstallable() } returns true
        every { settings.shouldShowPwaCfr } returns true

        store.dispatch(ContentAction.UpdateWebAppManifestAction("1", mockk())).joinBlocking()
        verify { settings.incrementVisitedInstallableCount() }
        verify { navigationController.nav(
            R.id.browserFragment,
            BrowserFragmentDirections.actionBrowserFragmentToPwaOnboardingDialogFragment())
        }
    }

    @Test
    fun `GIVEN web app is not installable WHEN page with manifest is loaded THEN nothing happens`() {
        pwaOnboardingObserver.start()
        every { webAppUseCases.isInstallable() } returns false

        store.dispatch(ContentAction.UpdateWebAppManifestAction("1", mockk())).joinBlocking()
        verify(exactly = 0) { settings.incrementVisitedInstallableCount() }
        verify(exactly = 0) { navigationController.nav(
            R.id.browserFragment,
            BrowserFragmentDirections.actionBrowserFragmentToPwaOnboardingDialogFragment())
        }
    }

    internal class MockedLifecycleOwner(initialState: Lifecycle.State) : LifecycleOwner {
        val lifecycleRegistry = LifecycleRegistry(this).apply {
            currentState = initialState
        }

        override fun getLifecycle(): Lifecycle = lifecycleRegistry
    }
}
