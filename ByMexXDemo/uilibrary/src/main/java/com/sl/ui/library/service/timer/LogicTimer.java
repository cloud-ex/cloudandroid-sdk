package com.sl.ui.library.service.timer;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by zhoujing on 2017/10/19.
 */

public class LogicTimer {

    public interface ITimerListener {
        void onTimer(int times);
    }

    private static LogicTimer instance = null;

    public static LogicTimer getInstance() {
        if (null == instance)
            instance = new LogicTimer();
        return instance;
    }

    private Timer mTimer;
    private int mTimes = 0;
    private int mRate = 5;
    private List<ITimerListener> mListeners = new ArrayList<>();

    private LogicTimer() {
    }

    public void init() {
        mTimer = new Timer();
        mTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                mTimes++;
                if (mTimes >= 100) {
                    mTimes = 0;
                }
                refresh(mTimes);
            }
        }, 1000 * 5, 1000 * mRate);
    }

    public void destroy() {
        if (mTimer != null) {
            mTimer.cancel();
            mTimer = null;
            mListeners.clear();
            instance = null;
        }
    }

    public void registerListener(ITimerListener listener) {

        if (listener == null) return;

        int iCount;
        for (iCount = 0; iCount < mListeners.size(); iCount++) {
            if (listener.equals(mListeners.get(iCount)))
                break;
        }

        if (iCount >= mListeners.size())
            mListeners.add(listener);
    }


    public void unRegisterListener(ITimerListener listener) {

        if (listener == null) return;

        int iCount;
        for (iCount = 0; iCount < mListeners.size(); iCount++) {
            if (listener.equals(mListeners.get(iCount))) {
                mListeners.remove(mListeners.get(iCount));
                return;
            }
        }
    }

    public void refresh(int times) {
        for (int i = 0; i < mListeners.size(); i++) {
            if (mListeners.get(i) != null) {
                mListeners.get(i).onTimer(times);
            }
        }
    }

}
