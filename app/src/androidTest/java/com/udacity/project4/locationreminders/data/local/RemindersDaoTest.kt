package com.udacity.project4.locationreminders.data.local

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.SmallTest;
import com.udacity.project4.locationreminders.data.dto.ReminderDTO

import org.junit.Before;
import org.junit.Rule;
import org.junit.runner.RunWith;

import kotlinx.coroutines.ExperimentalCoroutinesApi;
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.notNullValue
import org.hamcrest.MatcherAssert.assertThat
import org.junit.After
import org.junit.Test

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
//Unit test the DAO
@SmallTest
class RemindersDaoTest {
    //Add testing implementation to the RemindersDao.kt

    // Executes each task synchronously using Architecture Components.
    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    private lateinit var database: RemindersDatabase
    @Before fun initDb() {
    // Using an in-memory database so that the information stored here disappears when the
    // process is killed.
    database = Room.inMemoryDatabaseBuilder( getApplicationContext(), RemindersDatabase::class.java )
        .build()
    }
    @After
    fun closeDb() = database.close()


    @Test fun insertReminderAndGetById() = runBlockingTest {
        // GIVEN - Insert a reminder.
        val initreminder = ReminderDTO("title", "description", "Location", 0.0, 0.0)
        database.reminderDao().saveReminder(initreminder)
        val id = initreminder.id
        // WHEN - Get the reminder by id from the database.
        var loaded = database.reminderDao().getReminderById(initreminder.id)

        // THEN - The loaded data contains the expected values.
        assertThat<ReminderDTO>(loaded as ReminderDTO, notNullValue())
        assertThat(loaded.id, `is`(initreminder.id))
        assertThat(loaded.title, `is`(initreminder.title))
        assertThat(loaded.description, `is`(initreminder.description))
        assertThat(loaded.location, `is`(initreminder.location))
        assertThat(loaded.latitude, `is`(initreminder.latitude))
        assertThat(loaded.longitude, `is`(initreminder.longitude))

        loaded = null
        val updatedReminder = ReminderDTO("title1", "description1", "Location1", 0.1, 0.2,id)
        database.reminderDao().saveReminder(updatedReminder)
        loaded = database.reminderDao().getReminderById(id)
        assertThat<ReminderDTO>(loaded as ReminderDTO, notNullValue())
        assertThat(loaded.title, `is`(updatedReminder.title))
        assertThat(loaded.description, `is`(updatedReminder.description))
        assertThat(loaded.location, `is`(updatedReminder.location))
        assertThat(loaded.latitude, `is`(updatedReminder.latitude))
        assertThat(loaded.longitude, `is`(updatedReminder.longitude))

    }


}

