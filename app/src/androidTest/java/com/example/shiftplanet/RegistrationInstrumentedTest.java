package com.example.shiftplanet;

import android.view.View;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.rule.ActivityTestRule;
import androidx.test.espresso.intent.Intents;
import org.junit.Before;
import org.junit.After;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.*;
import static androidx.test.espresso.assertion.ViewAssertions.*;
import static androidx.test.espresso.matcher.RootMatchers.isPlatformPopup;
import static androidx.test.espresso.intent.Intents.intended;
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent;
import static androidx.test.espresso.matcher.ViewMatchers.*;

import static org.hamcrest.Matchers.not;

import org.junit.FixMethodOrder;
import org.junit.runners.MethodSorters;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@RunWith(AndroidJUnit4.class)
public class RegistrationInstrumentedTest {

    @Rule
    public ActivityTestRule<Registration> activityRule =
            new ActivityTestRule<>(Registration.class, true, false);

    @Before
    public void setUp() {
        Intents.init();
    }

    @After
    public void tearDown() {
        Intents.release();
    }

    @Test
    public void test1ManagerTypeFieldsVisibility() {
        activityRule.launchActivity(null);

        onView(withId(R.id.autoCompleteUserType)).perform(click());
        onView(withText("Manager")).inRoot(isPlatformPopup()).perform(click());

        onView(withId(R.id.inputEmail)).check(matches(isDisplayed()));
        onView(withId(R.id.inputPassword)).check(matches(isDisplayed()));
        onView(withId(R.id.inputConfirmPassword)).check(matches(isDisplayed()));
        onView(withId(R.id.input_full_name)).check(matches(isDisplayed()));
        onView(withId(R.id.inputPhoneNumber)).check(matches(isDisplayed()));
        onView(withId(R.id.input_id)).check(matches(isDisplayed()));
        onView(withId(R.id.inputBusinessCode)).check(matches(isDisplayed()));
        onView(withId(R.id.input_manager_email)).check(matches(not(isDisplayed())));
    }

    @Test
    public void test2EmployeeTypeFieldsVisibility() {
        activityRule.launchActivity(null);

        onView(withId(R.id.autoCompleteUserType)).perform(click());
        onView(withText("Employee")).inRoot(isPlatformPopup()).perform(click());

        onView(withId(R.id.inputEmail)).check(matches(isDisplayed()));
        onView(withId(R.id.inputPassword)).check(matches(isDisplayed()));
        onView(withId(R.id.inputConfirmPassword)).check(matches(isDisplayed()));
        onView(withId(R.id.input_full_name)).check(matches(isDisplayed()));
        onView(withId(R.id.inputPhoneNumber)).check(matches(isDisplayed()));
        onView(withId(R.id.input_id)).check(matches(isDisplayed()));
        onView(withId(R.id.inputBusinessCode)).check(matches(isDisplayed()));
        onView(withId(R.id.input_manager_email)).check(matches(isDisplayed()));
    }

    @Test
    public void test3SignUpWithExampleInfo() throws InterruptedException {
        activityRule.launchActivity(null);

        onView(withId(R.id.autoCompleteUserType)).perform(click());
        onView(withText("Employee")).inRoot(isPlatformPopup()).perform(click());

        onView(withId(R.id.inputEmail)).perform(typeText("ytfakerman@gmail.com"), closeSoftKeyboard());
        onView(withId(R.id.inputPassword)).perform(typeText("123qwe"), closeSoftKeyboard());
        onView(withId(R.id.inputConfirmPassword)).perform(typeText("123qwe"), closeSoftKeyboard());
        onView(withId(R.id.input_full_name)).perform(typeText("Moshe Luke"), closeSoftKeyboard());
        onView(withId(R.id.inputPhoneNumber)).perform(typeText("0507402456"), closeSoftKeyboard());
        onView(withId(R.id.input_id)).perform(typeText("449521443"), closeSoftKeyboard());
        onView(withId(R.id.inputBusinessCode)).perform(typeText("1"), closeSoftKeyboard());
        onView(withId(R.id.input_manager_email)).perform(typeText("yehonatan768@gmail.com"), closeSoftKeyboard());

        onView(withId(R.id.btnRegister)).perform(click());
        Thread.sleep(2000);
        // Verify the Intent to Login activity is triggered
        intended(hasComponent(Login.class.getName()));
    }
}
