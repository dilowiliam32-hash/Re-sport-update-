package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.AppDatabase
import com.example.data.Repository
import com.example.ui.ZeSportViewModel
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    private lateinit var db: AppDatabase
    private lateinit var repository: Repository
    private lateinit var viewModel: ZeSportViewModel

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        repository = Repository(db)
        viewModel = ZeSportViewModel(repository)
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun readStringFromContext() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("Ze Sport", appName)
    }

    @Test
    fun testMatchSimulationWorkflow() {
        val matchId = "norway_vs_england"
        val initialMatch = viewModel.matches.value.find { it.id == matchId }
        assertNotNull(initialMatch)
        assertEquals("Upcoming", initialMatch?.status)
        assertEquals("0", initialMatch?.homeScore)
        assertEquals("0", initialMatch?.awayScore)

        // 1. Upcoming -> Live (0 - 0)
        viewModel.advanceMatchStatus(matchId)
        val stage1 = viewModel.matches.value.find { it.id == matchId }
        assertEquals("Live", stage1?.status)
        assertEquals("0", stage1?.homeScore)
        assertEquals("0", stage1?.awayScore)

        // 2. Live (0 - 0) -> Live (1 - 0) [Haaland Goal]
        viewModel.advanceMatchStatus(matchId)
        val stage2 = viewModel.matches.value.find { it.id == matchId }
        assertEquals("Live", stage2?.status)
        assertEquals("1", stage2?.homeScore)
        assertEquals("0", stage2?.awayScore)

        // 3. Live (1 - 0) -> Live (1 - 1) [Bellingham Goal]
        viewModel.advanceMatchStatus(matchId)
        val stage3 = viewModel.matches.value.find { it.id == matchId }
        assertEquals("Live", stage3?.status)
        assertEquals("1", stage3?.homeScore)
        assertEquals("1", stage3?.awayScore)

        // 4. Live (1 - 1) -> Live (2 - 1) [Haaland Goal]
        viewModel.advanceMatchStatus(matchId)
        val stage4 = viewModel.matches.value.find { it.id == matchId }
        assertEquals("Live", stage4?.status)
        assertEquals("2", stage4?.homeScore)
        assertEquals("1", stage4?.awayScore)

        // 5. Live (2 - 1) -> Finished
        viewModel.advanceMatchStatus(matchId)
        val stage5 = viewModel.matches.value.find { it.id == matchId }
        assertEquals("Finished", stage5?.status)

        // 6. Finished -> Reset to Upcoming
        viewModel.advanceMatchStatus(matchId)
        val resetMatch = viewModel.matches.value.find { it.id == matchId }
        assertEquals("Upcoming", resetMatch?.status)
        assertEquals("0", resetMatch?.homeScore)
        assertEquals("0", resetMatch?.awayScore)
    }
}
