package sh.m.wristband;

import android.util.Log;

/**
 * Created by m on 21/01/15.
 */
public class WaveThread extends Thread {
    private MainActivity ma=null;
    private float amp=0;
    private int tOff=100;
    private int tOn=100;
    private boolean dir = false;
    private long t_now =0;
    private int mState=0;
    private long t_change =0;
    private int mId=0;
    /*
    private boolean shouldRun=true;

    @Override
    public void interrupt() {
        shouldRun=false;
    }

    @Override
    public boolean isInterrupted() {
        return shouldRun;
    }
    */
    public WaveThread(MainActivity ma) {
        this.ma=ma;
    }
    public synchronized void setWaveParams(int amp, int tOff, int tOn, boolean direction) {
        this.amp=amp;
        this.tOff=tOff;
        this.tOn=tOn;
        this.dir=direction;
        Log.d("waveThread","param update");
    }
    @Override
    public void run() {
        Log.d("waveThread", "started");
        if (!ma.trySetEnable(true))
            return;
        boolean res=true;
        while (!this.isInterrupted()) {
            try {
                sleep(10);
            } catch (InterruptedException e) {
                e.printStackTrace();
                break;
            }
            t_now = System.currentTimeMillis();

            if (this.mState>0 && (t_now - t_change)>tOn) {
                // finish state ON
                this.mState = 0;
                res=ma.trySetVal(this.mId, 0);
                t_change = t_now;
            }
            //#print "turn off %d %2.2f" % (mId,t_now-t0)

            else if (mState==0 && t_now - t_change >tOff) {
                //#finish state OFF
                //#increment motor id
                if (dir)
                    mId = mId < 5 ? mId+1 : 0;
                else
                    mId = mId > 0 ? mId-1 : 5;

                mState = 1;
                res=ma.trySetVal(mId, amp/100f);
                t_change = t_now;
            }
            if (!res) break;
        }
        ma.trySetEnable(false);
        Log.d("waveThread", "while loop finished");
    }
}
