package de.fieben.wikichallenge.game;

import android.support.annotation.IntDef;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EBean;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Created by Felix on 09.07.2016.
 */
@EBean
public class Game {

    //<editor-fold desc="Private Constants">
    private static final String URL_RANDOM_ARTICLE = "https://en.m.wikipedia.org/wiki/Special:Random";
    private static final String PAGE_TITLE_ARTICLE_EXTENSION = " - Wikipedia, the free encyclopedia";
    private static final String URL_PREFIX = "https://en.m.wikipedia.org/wiki/";
    //</editor-fold>

    //<editor-fold desc="Fields">
    @Bean
    GameState mGameState;
    private String mTargetArticleName;
    private int mClickCounter;
    private int mRetryCounter;

    private WebView mWebView;
    private boolean mCustomLoad;

    private GameCallback mCallback;
    //</editor-fold>

    //<editor-fold desc="Public Methods">
    public void setCallback(final GameCallback callback) {
        mCallback = callback;
    }

    public void setWebView(final WebView webView) {
        // TODO: 18.06.2016 Loading bar
        mWebView = webView;

        // TODO: 09.07.2016 into WebViewController?
        mWebView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                if (mCustomLoad) {
                    return false;
                }
                if (mGameState.is(GameState.WON)) {
                    mCallback.onInvalidEvent(GameCallback.EVENT_GAME_ENDED);
                    return true;
                }
                if (mGameState.isNot(GameState.RUNNING)) {
                    mCallback.onInvalidEvent(GameCallback.EVENT_GAME_NOT_STARTED);
                    return true;
                }
                if (!url.contains(URL_PREFIX)) {
                    mCallback.onInvalidEvent(GameCallback.EVENT_INVALID_LINK);
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
                    mCallback.onNewStartArticle(mTargetArticleName);
                }
                if (mCustomLoad) {
                    mWebView.clearHistory();
                    mCustomLoad = false;
                    return;
                }
                if (mTargetArticleName.equalsIgnoreCase(title)) {
                    mGameState.set(GameState.WON);
                    mCallback.onGameWon(mClickCounter, mRetryCounter);
                } else {
                    mClickCounter++;
                    mCallback.onNewPage(mClickCounter);
                }
            }
        });
    }

    public void loadRandomStartArticle() {
        mCustomLoad = true;
        mWebView.loadUrl(URL_RANDOM_ARTICLE);
    }

    public void startGame() {
        mGameState.set(GameState.RUNNING);
        mClickCounter = 0;
        mRetryCounter = 0;

        mCallback.onGameStart(mTargetArticleName);
        loadRandomStartArticle();
    }

    public void onBackPressed() {
        if (mGameState.is(GameState.RUNNING)) {
            if (mWebView.canGoBack()) {
                mRetryCounter++;
                mCallback.onRetry(mRetryCounter);
                mWebView.goBack();
            } else {
                mCallback.onInvalidEvent(GameCallback.EVENT_CANT_GO_BACK);
            }
        } else {
            mCallback.onInvalidEvent(GameCallback.EVENT_GAME_NOT_STARTED);
        }
    }

    public boolean isRunning() {
        return mGameState.is(GameState.RUNNING);
    }
    //</editor-fold>

    public interface GameCallback {

        //<editor-fold desc="Public Constants">
        @IntDef({
                EVENT_GAME_NOT_STARTED,
                EVENT_INVALID_LINK,
                EVENT_GAME_ENDED,
                EVENT_CANT_GO_BACK
        })
        @Retention(RetentionPolicy.SOURCE)
        @interface InvalidEvent {
        }

        int EVENT_GAME_NOT_STARTED = 0;
        int EVENT_INVALID_LINK = 1;
        int EVENT_GAME_ENDED = 2;
        int EVENT_CANT_GO_BACK = 3;
        //</editor-fold>

        // TODO: 09.07.2016 unifiy onNewStartArticle and onGameStart?
        void onNewStartArticle(final String startArticleName);

        void onGameStart(final String startArticleName);

        void onNewPage(final int gameCount);

        void onRetry(final int retryCount);

        void onInvalidEvent(@InvalidEvent final int event);

        void onGameWon(int clickCounter, int retryCounter);
    }
}
