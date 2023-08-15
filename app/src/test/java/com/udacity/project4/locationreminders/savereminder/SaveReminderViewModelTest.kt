package com.udacity.project4.locationreminders.savereminder

import android.app.Application
import android.os.Build
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.udacity.project4.locationreminders.MainCoroutineRule
import com.udacity.project4.locationreminders.data.FakeDataSource
import com.udacity.project4.locationreminders.getOrAwaitValue
import com.udacity.project4.locationreminders.reminderslist.ReminderDataItem

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.pauseDispatcher
import kotlinx.coroutines.test.resumeDispatcher
import kotlinx.coroutines.test.runBlockingTest
import kotlinx.coroutines.test.runTest
import org.hamcrest.CoreMatchers.*
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.stopKoin
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(sdk = [Build.VERSION_CODES.O])
@ExperimentalCoroutinesApi
class SaveReminderViewModelTest {

    //Setup Rule
    @get:Rule
    var mainCoroutineRule = MainCoroutineRule()

    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    // Initialize the variables
    private lateinit var application: Application
    private lateinit var dataSource: FakeDataSource
    private lateinit var saveReminderViewModel: SaveReminderViewModel

    //Provide testing to the SaveReminderView and its live data objects
    @Before
    fun setupViewModel() {
        stopKoin()
        //Setup Application
        application = ApplicationProvider.getApplicationContext()
        //Setup Data Source
        dataSource = FakeDataSource()
        //Setup View Model
        saveReminderViewModel = SaveReminderViewModel(application,dataSource)
    }
    @Test
    fun validateAndSaveReminder_checkLoading() = mainCoroutineRule.runBlockingTest  {
            mainCoroutineRule.pauseDispatcher()
            // When adding a new Reminder
            saveReminderViewModel.validateAndSaveReminder(
                ReminderDataItem(
                    "Go Home",
                    "Go Home and Take Dinner",
                    "Home",
                    0.0,
                    0.0
                )
            )

        assertThat(saveReminderViewModel.showLoading.getOrAwaitValue(), `is`(true))
        mainCoroutineRule.resumeDispatcher()
        assertThat(saveReminderViewModel.showLoading.getOrAwaitValue(), `is`(false))
    }

    @Test
    fun validateAndSaveReminder_shouldReturnError() = mainCoroutineRule.runBlockingTest  {
        mainCoroutineRule.pauseDispatcher()
        // When adding a new Reminder
        saveReminderViewModel.validateAndSaveReminder(
            ReminderDataItem(
                null,
                "Go Home and Take Dinner",
                "Home",
                0.0,
                0.0
            )
        )
        assertThat(saveReminderViewModel.isReminderSaved.getOrAwaitValue(), `is`(false))
    }
}