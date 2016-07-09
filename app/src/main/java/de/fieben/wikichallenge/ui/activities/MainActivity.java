package de.fieben.wikichallenge.ui.activities;

import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.TextView;
import android.widget.Toast;

import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.ViewById;

import de.fieben.wikichallenge.R;
import de.fieben.wikichallenge.game.Game;

@EActivity(R.layout.activity_main)
public class MainActivity extends AppCompatActivity implements Game.GameCallback {

    private static final String LOG_TAG = MainActivity.class.getSimpleName();

    @Bean
    Game mGame;

    @ViewById(R.id.activity_main_buttons_container)
    ViewGroup mButtonsContainer;

    @ViewById(R.id.activity_main_web_view)
    void initGame(final WebView webView) {
        mGame.setWebView(webView);
        mGame.setCallback(this);
        mGame.loadRandomStartArticle();
    }

    //<editor-fold desc="Game Controls">
    @Click(R.id.activity_main_button_start_game)
    void onStartGameClick() {
        mGame.startGame();

        mButtonsContainer.setVisibility(View.GONE);
    }

    @Click(R.id.activity_main_button_change_article)
    void onChangeArticleClick() {
        mGame.loadRandomStartArticle();
    }

    @Override
    public void onBackPressed() {
        if (mGame.isRunning()) {
            mGame.onBackPressed();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onKeyLongPress(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            super.onBackPressed();
            return true;
        }
        return super.onKeyLongPress(keyCode, event);
    }

    //<editor-fold desc="GameCallback">
    @Override
    public void onNewStartArticle(final String startArticleName) {
        setTitle("Target article: " + startArticleName);
    }

    @Override
    public void onGameStart(String startArticleName) {
        eventText("The game has started.\nTarget article:\n\n\"" + startArticleName + "\"", Toast.LENGTH_LONG);
    }

    @Override
    public void onNewPage(final int gameCount) {
        eventText("Click count: " + gameCount, Toast.LENGTH_SHORT);
    }

    @Override
    public void onRetry(final int retryCount) {
        eventText("Retry count: " + retryCount, Toast.LENGTH_SHORT);
    }

    @Override
    public void onInvalidEvent(@InvalidEvent final int event) {
        switch (event) {
            case Game.GameCallback.EVENT_GAME_NOT_STARTED:
                eventText("Game has not yet started.", Toast.LENGTH_LONG);
                break;
            case Game.GameCallback.EVENT_INVALID_LINK:
                eventText("This link is not allowed.", Toast.LENGTH_SHORT);
                break;
            case Game.GameCallback.EVENT_GAME_ENDED:
                eventText("The game is over, you already won!", Toast.LENGTH_SHORT);
                break;
            case Game.GameCallback.EVENT_CANT_GO_BACK:
                eventText("You're at the start.\n\nHold back to quit.", Toast.LENGTH_SHORT);
                break;
        }
    }

    @Override
    public void onGameWon(final int clickCounter, final int retryCounter) {
        eventText("You won! It took you " + clickCounter + " clicks and " + retryCounter + " retries.", Toast.LENGTH_LONG);
    }

    private void eventText(String eventText, int length) {
        Log.d(LOG_TAG, eventText.replace("\n", " "));
        Toast toast = Toast.makeText(MainActivity.this, eventText, length);
        TextView textView = (TextView) toast.getView().findViewById(android.R.id.message);
        textView.setGravity(Gravity.CENTER);
        toast.show();
    }
    //</editor-fold>
}
