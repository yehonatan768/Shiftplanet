package com.example.shiftplanet;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static androidx.test.espresso.intent.Intents.intended;
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent;

import androidx.test.espresso.intent.Intents;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class ManagerDialogRequestDetailsTest {

    @Rule
    public ActivityScenarioRule<ManagerDialogRequestDetails> activityRule =
            new ActivityScenarioRule<>(ManagerDialogRequestDetails.class);

    @Test
    public void testApproveButtonUpdatesRequestStatus() {
        // Start monitoring Intents to verify navigation or behavior
        Intents.init();

        // Simulate click on Approve button
        onView(withId(R.id.approve_button)).perform(click());

        // Verify Toast message
        onView(withText("Request Approved")).inRoot(new ToastMatcher())
                .check(matches(withText("Request Approved")));

        // Verify navigation back to ManagerRequestPage
        intended(hasComponent(ManagerRequestPage.class.getName()));

        // End monitoring Intents
        Intents.release();
    }

    @Test
    public void testDenyButtonUpdatesRequestStatus() {
        // Start monitoring Intents to verify navigation or behavior
        Intents.init();

        // Simulate click on Deny button
        onView(withId(R.id.deny_button)).perform(click());

        // Verify Toast message
        onView(withText("Request Denied")).inRoot(new ToastMatcher())
                .check(matches(withText("Request Denied")));

        // Verify navigation back to ManagerRequestPage
        intended(hasComponent(ManagerRequestPage.class.getName()));

        // End monitoring Intents
        Intents.release();
    }

    @Test
    public void testBackButtonNavigatesToManagerRequestPage() {
        // Start monitoring Intents to verify navigation
        Intents.init();

        // Simulate click on Back button
        onView(withId(R.id.btnBackDialog)).perform(click());

        // Verify navigation to ManagerRequestPage
        intended(hasComponent(ManagerRequestPage.class.getName()));

        // End monitoring Intents
        Intents.release();
    }
}
