package tests;


import android.content.Intent;
import android.widget.EditText;
import androidx.test.espresso.Espresso;
import androidx.test.espresso.IdlingResource;
import androidx.test.espresso.matcher.ViewMatchers;
import androidx.test.espresso.action.ViewActions;
import androidx.test.espresso.assertion.ViewAssertions;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.rule.ActivityTestRule;
import androidx.test.espresso.intent.Intents;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;


import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.rule.ActivityTestRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.*;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.*;

import static androidx.test.espresso.matcher.ViewMatchers.*;
import static androidx.test.espresso.action.ViewActions.*;
import static androidx.test.espresso.assertion.ViewAssertions.*;
import static androidx.test.espresso.intent.Intents.intended;
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent;

import com.example.shiftplanet.ForgotPassword;
import com.example.shiftplanet.Login;
import com.example.shiftplanet.ManagerHomePage;
import com.example.shiftplanet.R;
import com.example.shiftplanet.Registration;

@RunWith(AndroidJUnit4.class)
public class LoginTest {

    @Rule
    public ActivityTestRule<Login> activityRule = new ActivityTestRule<>(Login.class);

    @Before
    public void setUp() {
        // In this method you can set up any necessary mock data or preconditions if needed
    }

    @Test
    public void testLoginValidCredentials() {
        // הקלדת אימייל בתיבת הקלט
        Espresso.onView(withId(R.id.inputEmailLogin))
                .perform(typeText("orgoren3146@gmail.com"), closeSoftKeyboard());

        // הקלדת סיסמה בתיבת הקלט
        Espresso.onView(withId(R.id.inputPasswordLogin))
                .perform(typeText("123456"), closeSoftKeyboard());

        // לחיצה על כפתור ההתחברות
        Espresso.onView(withId(R.id.btnLogin))
                .perform(click());

        // בדיקת ניווט למסך הנכון לאחר התחברות
        intended(hasComponent(ManagerHomePage.class.getName())); // בדוק אם הניווט היה ל-ManagerHomePage
    }


    @Test
    public void testLoginEmptyEmail() {
        // הקלדת סיסמה תקינה
        Espresso.onView(withId(R.id.inputPasswordLogin))
                .perform(typeText("123456"), closeSoftKeyboard());

        // לחיצה על כפתור ההתחברות (ללא אימייל)
        Espresso.onView(withId(R.id.btnLogin))
                .perform(click());

        // בדוק אם הופיעה הודעת שגיאה
        Espresso.onView(withText("Please enter both email and password."))
                .check(matches(isDisplayed()));
    }

    @Test
    public void testLoginEmptyPassword() {
        // הקלדת אימייל תקין
        Espresso.onView(withId(R.id.inputEmailLogin))
                .perform(typeText("orgoren3146@gmail.com"), closeSoftKeyboard());

        // לחיצה על כפתור ההתחברות (ללא סיסמה)
        Espresso.onView(withId(R.id.btnLogin))
                .perform(click());

        // בדוק אם הופיעה הודעת שגיאה
        Espresso.onView(withText("Please enter both email and password."))
                .check(matches(isDisplayed()));
    }

    @Test
    public void testForgotPasswordNavigation() {
        // לחיצה על "שכחתי סיסמה"
        Espresso.onView(withId(R.id.forgotPasswordLogin))
                .perform(click());

        // בדוק שהניווט הוביל לעמוד המתאים (לדוג' ForgotPassword)
        intended(hasComponent(ForgotPassword.class.getName()));
    }

    @Test
    public void testCreateNewAccountNavigation() {
        // לחיצה על "צור חשבון חדש"
        Espresso.onView(withId(R.id.createNewAccount))
                .perform(click());

        // בדוק שהניווט הוביל לעמוד המתאים (לדוג' Registration)
        intended(hasComponent(Registration.class.getName()));
    }
}
