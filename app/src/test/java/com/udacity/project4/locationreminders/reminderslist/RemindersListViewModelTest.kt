package com.udacity.project4.locationreminders.reminderslist

import android.app.Application
import android.os.Build
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.udacity.project4.locationreminders.MainCoroutineRule
import com.udacity.project4.locationreminders.data.FakeDataSource
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.getOrAwaitValue
import com.udacity.project4.locationreminders.savereminder.SaveReminderViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.pauseDispatcher
import kotlinx.coroutines.test.resumeDispatcher
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.CoreMatchers.*
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.stopKoin
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(sdk = [Build.VERSION_CODES.O])
@ExperimentalCoroutinesApi
class RemindersListViewModelTest {

    //Provide testing to the RemindersListViewModel and its live data objects

    @get:Rule
    var mainCoroutineRule = MainCoroutineRule()

    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    // Initialize the variables
    private lateinit var application: Application
    private lateinit var dataSource: FakeDataSource
    private lateinit var remindersListViewModel: RemindersListViewModel

    //Provide testing to the RemindersList and its live data objects
    @Before
    fun setupViewModel() {
        stopKoin()
        //Setup Application
        application = ApplicationProvider.getApplicationContext()
        //Setup Data Source
        dataSource = FakeDataSource()
        //Setup View Model
        remindersListViewModel = RemindersListViewModel(application,dataSource)
    }
    @Test
    fun loadReminders_check_loading() = mainCoroutineRule.runBlockingTest  {
        // When adding a new Reminder
        val reminder = ReminderDTO("Test", "TestDescription", "TestLocation", 0.0, 0.0)
        dataSource.saveReminder(reminder)

        mainCoroutineRule.pauseDispatcher()
        remindersListViewModel.loadReminders()
        assertThat(remindersListViewModel.showLoading.getOrAwaitValue(), `is`(true))

        mainCoroutineRule.resumeDispatcher()
        assertThat(remindersListViewModel.showLoading.getOrAwaitValue(), `is`(false))
    }

    @Test
    fun loadReminders_shouldReturnError() = mainCoroutineRule.runBlockingTest {

        val reminder = ReminderDTO("Test", "TestDescription", "TestLocation", 0.0, 0.0)
        dataSource.saveReminder(reminder)

        dataSource.setReturnError(true)

        //Trigger Refresh
        remindersListViewModel.loadReminders()
        assertThat(remindersListViewModel.showSnackBar.getOrAwaitValue(), `is`(notNullValue()))

        //Have to understand how to test Live Dataset
        //assertThat(remindersListViewModel.remindersList.getOrAwaitValue(), `is`(false))
        //assertThat(remindersListViewModel.selectedReminder.getOrAwaitValue(), `is`(false))
    }


}