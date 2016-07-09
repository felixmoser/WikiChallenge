package de.fieben.wikichallenge.game;

import android.support.annotation.IntDef;

import org.androidannotations.annotations.EBean;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Created by Felix on 09.07.2016.
 */
@EBean
public class GameState {

    //<editor-fold desc="Public Constants">
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
    //</editor-fold>

    protected GameState() {
        // Packet wide constructor
    }

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
