package de.fieben.wikichallenge;

import android.support.annotation.IntDef;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.TextView;
import android.widget.Toast;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.ViewById;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@EActivity(R.layout.activity_main)
public class MainActivity extends AppCompatActivity {

    private static final String LOG_TAG = MainActivity.class.getSimpleName();

    public static final String URL_RANDOM_ARTICLE = "https://en.m.wikipedia.org/wiki/Special:Random";
    public static final String PAGE_TITLE_ARTICLE_EXTENSION = " - Wikipedia, the free encyclopedia";
    public static final String URL_PREFIX = "https://en.m.wikipedia.org/wiki/";

    @ViewById(R.id.activity_main_web_view)
    WebView mWebView;


    private GameState mGameState = new GameState();
    private String mTargetArticleName;
    private boolean mCustomLoad;
    private int mClickCounter;
    private int mRetryCounter;

    @AfterViews
    void init() {
        mWebView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                if (mCustomLoad) {
                    return false;
                }
                if (mGameState.is(GameState.WON)) {
                    eventText("The game is over, you won!", Toast.LENGTH_SHORT);
                    return true;
                }
                if (mGameState.isNot(GameState.RUNNING)) {
                    eventText("Game has not yet started.", Toast.LENGTH_SHORT);
                    return true;
                }
                if (!url.contains(URL_PREFIX)) {
                    eventText("This link is not allowed.", Toast.LENGTH_SHORT);
                    return true;
                }
                return false;
            }
        });

        mWebView.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onReceivedTitle(WebView view, String title) {
                title = title.replace(PAGE_TITLE_ARTICLE_EXTENSION, "");
                if (mGameState.isNot(GameState.RUNNING)) {
                    mTargetArticleName = title;
                }
                if (mCustomLoad) {
                    mWebView.clearHistory();
                    mCustomLoad = false;
                    return;
                }
                if (mTargetArticleName.equalsIgnoreCase(title)) {
                    mGameState.set(GameState.WON);
                    eventText("You won! It took you " + mClickCounter + " clicks and " + mRetryCounter + " retries.", Toast.LENGTH_LONG);
                } else {
                    mClickCounter++;
                    eventText("Click count: " + mClickCounter, Toast.LENGTH_SHORT);
                }
            }
        });

        showStartArticle();
    }

    @Override
    public void onBackPressed() {
        if (mGameState.is(GameState.WON)) {
            eventText("Hold back to start a new game.", Toast.LENGTH_SHORT);
            return;
        }
        if (mWebView.canGoBack()) {
            mRetryCounter++;
            mWebView.goBack();
            eventText("Retry count: " + mRetryCounter, Toast.LENGTH_SHORT);
        } else if (mGameState.is(GameState.RUNNING)) {
            eventText("You reached the start again.\nHold back to quit.", Toast.LENGTH_SHORT);
        } else {
            showStartArticle();
        }
    }

    @Override
    public boolean onKeyLongPress(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (mGameState.isNot(GameState.RUNNING)) {
                startGame();
            } else {
                super.onBackPressed();
            }
            return true;
        }
        return super.onKeyLongPress(keyCode, event);
    }


    private void showStartArticle() {
        loadRandomArticle();
        eventText("Hold back to start the challenge!\n\n(Or short press back for new start article.)", Toast.LENGTH_LONG);
    }

    private void startGame() {
        setTitle("Target article: " + mTargetArticleName);

        mGameState.set(GameState.RUNNING);
        mClickCounter = 0;
        mRetryCounter = 0;
        loadRandomArticle();

        eventText("The game has started.\nTarget article:\n\n\"" + mTargetArticleName + "\"", Toast.LENGTH_LONG);
    }

    private void loadRandomArticle() {
        mCustomLoad = true;
        mWebView.loadUrl(URL_RANDOM_ARTICLE);
    }

    private void eventText(String eventText, int length) {
        Log.d(LOG_TAG, eventText.replace("\n", " "));
        Toast toast = Toast.makeText(MainActivity.this, eventText, length);
        TextView textView = (TextView) toast.getView().findViewById(android.R.id.message);
        textView.setGravity(Gravity.CENTER);
        toast.show();
    }

    private static class GameState {

        @IntDef({
                IDLE,
                RUNNING,
                WON
        })
        @Retention(RetentionPolicy.SOURCE)
        public @interface State {
        }

        public static final int IDLE = 0;
        public static final int RUNNING = 1;
        public static final int WON = 2;

        @State
        private int mState = IDLE;

        public void set(@State final int state) {
            mState = state;
        }

        public boolean is(@State final int state) {
            return mState == state;
        }

        public boolean isNot(@State final int state) {
            return mState != state;
        }
    }
}
