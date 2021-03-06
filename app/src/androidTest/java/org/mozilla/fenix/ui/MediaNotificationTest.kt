/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.mozilla.fenix.ui

import androidx.test.uiautomator.UiSelector
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Before
import org.junit.Ignore
import org.junit.Rule
import org.junit.Test
import org.mozilla.fenix.helpers.AndroidAssetDispatcher
import org.mozilla.fenix.helpers.HomeActivityTestRule
import org.mozilla.fenix.helpers.TestAssetHelper
import org.mozilla.fenix.ui.robots.browserScreen
import org.mozilla.fenix.ui.robots.homeScreen
import org.mozilla.fenix.ui.robots.mDevice
import org.mozilla.fenix.ui.robots.navigationToolbar
import org.mozilla.fenix.ui.robots.notificationShade

/**
 *  Tests for verifying basic functionality of media notifications:
 *  - video and audio playback system notifications appear and can pause/play the media content
 *  - a media notification icon is displayed on the homescreen for the tab playing media content
 *  Note: this test only verifies media notifications, not media itself
 */
class MediaNotificationTest {
    /* ktlint-disable no-blank-line-before-rbrace */ // This imposes unreadable grouping.

    private lateinit var mockWebServer: MockWebServer

    @get:Rule
    val activityTestRule = HomeActivityTestRule()

    @Before
    fun setUp() {
        mockWebServer = MockWebServer().apply {
            dispatcher = AndroidAssetDispatcher()
            start()
        }
    }

    @After
    fun tearDown() {
        mockWebServer.shutdown()
        // verify if the notification tray is expanded and should be closed before the next test
        val notificationShade =
            mDevice.findObject(UiSelector().resourceId("com.android.systemui:id/notification_stack_scroller"))

        if (notificationShade.exists())
            mDevice.pressBack()
    }

    @Test
    @Ignore("https://github.com/mozilla-mobile/fenix/issues/15754")
    fun videoPlaybackSystemNotificationTest() {
        val videoTestPage = TestAssetHelper.getVideoPageAsset(mockWebServer)

        navigationToolbar {
        }.enterURLAndEnterToBrowser(videoTestPage.url) {
            mDevice.waitForIdle()
            clickMediaPlayerPlayButton()
            waitForPlaybackToStart()
        }.openNotificationShade {
            verifySystemNotificationExists(videoTestPage.title)
            clickMediaSystemNotificationControlButton("Pause")
            verifyMediaSystemNotificationButtonState("Play")
        }

        mDevice.pressBack()

        browserScreen {
            verifyMediaIsPaused()
        }.openTabDrawer {
            closeTab()
        }

        mDevice.openNotification()

        notificationShade {
            verifySystemNotificationGone(videoTestPage.title)
        }

        // close notification shade before the next test
        mDevice.pressBack()
    }

    @Test
    @Ignore("https://github.com/mozilla-mobile/fenix/issues/15754")
    fun audioPlaybackSystemNotificationTest() {
        val audioTestPage = TestAssetHelper.getAudioPageAsset(mockWebServer)

        navigationToolbar {
        }.enterURLAndEnterToBrowser(audioTestPage.url) {
            mDevice.waitForIdle()
            clickMediaPlayerPlayButton()
            waitForPlaybackToStart()
        }.openNotificationShade {
            verifySystemNotificationExists(audioTestPage.title)
            clickMediaSystemNotificationControlButton("Pause")
            verifyMediaSystemNotificationButtonState("Play")
        }

        mDevice.pressBack()

        browserScreen {
            verifyMediaIsPaused()
        }.openTabDrawer {
            closeTab()
        }

        mDevice.openNotification()

        notificationShade {
            verifySystemNotificationGone(audioTestPage.title)
        }

        // close notification shade before the next test
        mDevice.pressBack()
    }

    @Test
    @Ignore("https://github.com/mozilla-mobile/fenix/issues/15754")
    fun tabMediaControlButtonTest() {
        val audioTestPage = TestAssetHelper.getAudioPageAsset(mockWebServer)

        navigationToolbar {
        }.enterURLAndEnterToBrowser(audioTestPage.url) {
            mDevice.waitForIdle()
            clickMediaPlayerPlayButton()
            waitForPlaybackToStart()
        }.openTabDrawer {
            verifyTabMediaControlButtonState("Pause")
            clickTabMediaControlButton()
            verifyTabMediaControlButtonState("Play")
        }.openTab(audioTestPage.title) {
            verifyMediaIsPaused()
        }
    }

    @Test
    @Ignore("https://github.com/mozilla-mobile/fenix/issues/15754")
    fun mediaSystemNotificationInPrivateModeTest() {
        val audioTestPage = TestAssetHelper.getAudioPageAsset(mockWebServer)

        homeScreen { }.togglePrivateBrowsingMode()

        navigationToolbar {
        }.enterURLAndEnterToBrowser(audioTestPage.url) {
            mDevice.waitForIdle()
            clickMediaPlayerPlayButton()
            waitForPlaybackToStart()
        }.openNotificationShade {
            verifySystemNotificationExists("A site is playing media")
            clickMediaSystemNotificationControlButton("Pause")
            verifyMediaSystemNotificationButtonState("Play")
        }

        mDevice.pressBack()

        browserScreen {
            verifyMediaIsPaused()
        }.openTabDrawer {
            closeTab()
            verifySnackBarText("Private tab closed")
        }

        mDevice.openNotification()

        notificationShade {
            verifySystemNotificationGone("A site is playing media")
        }

        // close notification shade before and go back to regular mode before the next test
        mDevice.pressBack()
        homeScreen { }.togglePrivateBrowsingMode()
    }
}
