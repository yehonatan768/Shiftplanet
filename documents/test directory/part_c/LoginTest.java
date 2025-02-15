package com.example.shiftplanet;

import androidx.test.espresso.Espresso;
import androidx.test.espresso.intent.Intents;

import org.hamcrest.Matchers;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.rule.ActivityTestRule;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.*;
import static androidx.test.espresso.assertion.ViewAssertions.matches;

import static androidx.test.espresso.matcher.ViewMatchers.*;
import static androidx.test.espresso.intent.Intents.intended;
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent;

import org.junit.FixMethodOrder;
import org.junit.runners.MethodSorters;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)  // Make tests run in order based on method names
@RunWith(AndroidJUnit4.class)
public class LoginTest {

    @Rule
    public ActivityTestRule<Login> activityRule =
            new ActivityTestRule<>(Login.class, true, false);  // This rule is used to launch the activity

    @Before
    public void setUp() {
        Intents.init();  // Initialize Intents before each test
    }

    @After
    public void tearDown() {
        Intents.release();  // Release Intents after each test
    }

    @Test
    public void testLoginFieldsVisibility() throws InterruptedException {
        activityRule.launchActivity(null);
        Thread.sleep(1000);

        onView(withId(R.id.inputEmailLogin)).check(matches(isDisplayed()));
        onView(withId(R.id.inputPasswordLogin)).check(matches(isDisplayed()));
        onView(withId(R.id.createNewAccount)).check(matches(isDisplayed()));
        onView(withId(R.id.forgotPasswordLogin)).check(matches(isDisplayed()));
    }


    @Test
    public void testLoginValidCredentials() throws InterruptedException {
        activityRule.launchActivity(null);
            onView(withId(R.id.inputEmailLogin))
                    .perform(typeText("orgoren3146@gmail.com"), closeSoftKeyboard());
            onView(withId(R.id.inputPasswordLogin))
                    .perform(typeText("123456"), closeSoftKeyboard());
            onView(withId(R.id.btnLogin))
                    .perform(click());
            Thread.sleep(2000);
            // Verify that the navigation was to the ManagerHomePage
            intended(hasComponent(ManagerHomePage.class.getName()));

    }

    @Test
    public void testForgotPasswordNavigation() {
        activityRule.launchActivity(null);

        // Test navigation to forgot password page
        onView(withId(R.id.forgotPasswordLogin))
                .perform(click());

        // Verify that the navigation was to the ForgotPassword page
        intended(hasComponent(ForgotPassword.class.getName()));
    }

}
