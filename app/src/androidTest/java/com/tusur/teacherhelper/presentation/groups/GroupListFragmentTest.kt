package com.tusur.teacherhelper.presentation.groups

import androidx.navigation.testing.TestNavHostController
import androidx.recyclerview.widget.RecyclerView
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.doesNotExist
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.filters.MediumTest
import com.tusur.teacherhelper.R
import com.tusur.teacherhelper.domain.model.Group
import com.tusur.teacherhelper.domain.repository.GroupRepository
import com.tusur.teacherhelper.launchFragmentInHiltContainer
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import org.hamcrest.CoreMatchers.not
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import javax.inject.Inject

@MediumTest
@HiltAndroidTest
class GroupListFragmentTest {

    @get:Rule
    val hiltRule = HiltAndroidRule(this)

    @Inject
    lateinit var repository: GroupRepository

    // NavController for input dialog test
    private lateinit var navController: TestNavHostController

    private val dispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        hiltRule.inject()
    }

    @Test
    fun showsList_whenGroupsExist() = runTest(dispatcher) {
        // GIVEN - One group already in the repository
        repository.add(Group(id = 1, number = "430-3"))

        // WHEN - On startup
        launchFragment()

        // THEN - Verify group is displayed on screen
        onView(withText("430-3")).check(matches(isDisplayed()))
    }

    @Test
    fun showsEmptyListLabel_whenGroupsDoesNotExist() {
        // GIVEN - Empty group list

        // WHEN - On startup
        launchFragment()

        // THEN - Verify label is displayed on screen
        onView(withText(R.string.empty_list)).check(matches(isDisplayed()))
    }

    @Test
    fun showsGroupInputDialog_whenButtonClicked() {
        // GIVEN - Empty group list

        launchFragment()

        // WHEN - Add button clicked
        onView(withId(R.id.add_button)).perform(click())

        // THEN - Verify destination is changed
        assertEquals(
            navController.currentDestination?.id,
            R.id.newGroupNumberInputBottomSheet
        )
    }

    @Test
    fun navigatesToGroupDetails_whenGroupClicked() = runTest(dispatcher) {
        // GIVEN - One group already in the repository
        repository.add(Group(id = 1, number = "430-3"))

        launchFragment()

        // WHEN - Group clicked
        onView(withText("430-3")).perform(click())

        // THEN - Verify destination is changed
        assertEquals(
            navController.currentDestination?.id,
            R.id.groupStudentsFragment
        )
    }

    @Test
    fun showsGroupDeleteButtons_whenBeginDelete() = runTest(dispatcher) {
        // GIVEN - One group in repository
        repository.add(Group(id = 1, number = "430-3"))

        launchFragment()

        // WHEN - Delete menu button is clicked
        onView(withId(R.id.remove)).perform(click())

        // THEN - Verify delete button of group is shown
        onView(withId(R.id.delete_button)).check(matches(isDisplayed()))
    }

    @Test
    fun removesAddButton_whenBeginSearch() {
        // GIVEN - Empty group list

        launchFragment()

        // WHEN - Search button clicked
        onView(withId(R.id.search_view)).perform(click())

        // THEN - Verify add button is gone
        onView(withId(R.id.add_button)).check(matches(not(isDisplayed())))
    }

    @Test
    fun showsAddButton_whenEndSearch() {
        // GIVEN - Empty group list

        launchFragment()

        // WHEN - Search button clicked and closed
        onView(withId(R.id.search_view)).perform(click())
        onView(withId(androidx.appcompat.R.id.search_close_btn)).perform(click())

        // THEN - Verify add button is shown
        onView(withId(R.id.add_button)).check(matches((isDisplayed())))
    }

    @Test
    fun showsDeleteMenuButton_whenEndSearch() {
        // GIVEN - Empty group list

        launchFragment()

        // WHEN - Search button clicked and closed
        onView(withId(R.id.search_view)).perform(click())
        onView(withId(androidx.appcompat.R.id.search_close_btn)).perform(click())

        // THEN - Verify delete menu button is shown
        onView(withId(R.id.remove)).check(matches(isDisplayed()))
    }

    @Test
    fun showsCancelDeleteMenuButton_whenBeginDelete() {
        // GIVEN - Empty group list

        launchFragment()

        // WHEN - Delete button clicked
        onView(withId(R.id.remove)).perform(click())

        // THEN - Verify cancel button is shown
        onView(withId(R.id.cancel)).check(matches(isDisplayed()))
    }


    @Test
    fun removesAddButton_whenBeginDelete() {
        // GIVEN - Empty group list

        launchFragment()

        // WHEN - Delete button clicked
        onView(withId(R.id.remove)).perform(click())

        // THEN - Verify add button is gone
        onView(withId(R.id.add_button)).check(matches(not(isDisplayed())))
    }

    @Test
    fun removesDeleteButtons_whenEndDelete() = runTest(dispatcher) {
        // GIVEN - One group in repository
        repository.add(Group(id = 1, number = "430-3"))

        launchFragment()

        // WHEN - Delete menu button clicked and then close clicked
        onView(withId(R.id.remove)).perform(click())
        onView(withId(R.id.cancel)).perform(click())

        // THEN - Verify group delete buttons are hidden
        onView(withId(R.id.delete_button)).check(doesNotExist())
    }

    @Test
    fun removesDeleteMenuButton_whenBeginSearch() {
        // GIVEN - Empty group list

        launchFragment()

        // WHEN - Search button clicked
        onView(withId(R.id.search_view)).perform(click())

        // THEN - Verify delete menu button is gone
        onView(withId(R.id.remove)).check(doesNotExist())
    }

    private fun launchFragment() {
        launchFragmentInHiltContainer<GroupListFragment>(
            themeResId = R.style.Theme_TeacherHelper,
            navController = {
                TestNavHostController(getApplicationContext())
                    .also {
                        it.setGraph(R.navigation.nav_graph)
                        it.setCurrentDestination(R.id.allGroupsFragment)
                        navController = it
                    }
            }
        ) {
            // Disable animations in RecyclerView
            requireView().findViewById<RecyclerView>(R.id.recycler_view).itemAnimator = null
        }
    }
}