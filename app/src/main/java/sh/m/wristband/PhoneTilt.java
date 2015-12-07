package sh.m.wristband;

import android.app.Activity;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.ToggleButton;

/**
 * A placeholder fragment containing a simple view.
 */
public class PhoneTilt extends Fragment implements CompoundButton.OnCheckedChangeListener, SensorEventListener, RangeSeekBar.OnRangeSeekBarChangeListener<Integer> {
    /**
     * The fragment argument representing the section number for this
     * fragment.
     */
    private static final String ARG_SECTION_NUMBER = "section_number";

    /**
     * Returns a new instance of this fragment for the given section
     * number.
     */
    private WaveThread wbThread=null;

    private int tiltHA, tiltLF, tiltHT, tiltLA;
    private boolean tiltDirCV;
    private double ax;
    private TextView tv;

    public static PhoneTilt newInstance(int sectionNumber) {
        PhoneTilt fragment = new PhoneTilt();
        Bundle args = new Bundle();
        args.putInt(ARG_SECTION_NUMBER, sectionNumber);
        fragment.setArguments(args);
        return fragment;
    }

    public PhoneTilt() {
    }

    private void updateParams() {
        if (wbThread==null) return;

        double tfPulseK=(tiltLA - tiltHT)/(tiltHA - tiltLF);
        double tfPulseN=1;
        double tfAmpK=1;
        double tfAmpN=1;

        double x = Math.abs(ax);
        int waveAmp= (int) (tiltLA +x/90*(tiltHA - tiltLA));
        int waveOff=2;
        int waveFreq = (int) (tiltLF +x/90*(tiltHT - tiltLF));
        double waveDirCV= Math.signum(ax) * (tiltDirCV ? -1. : 1.);

        wbThread.setWaveParams(waveAmp, waveOff, (int)(1./(float)waveFreq/6), waveDirCV>0);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.frag_tilt, container, false);

        tv=((TextView) rootView.findViewById(R.id.tvSensor));

        ((ToggleButton)rootView.findViewById(R.id.switchDir   )).setOnCheckedChangeListener(this);
        ((ToggleButton)rootView.findViewById(R.id.switchEnable)).setOnCheckedChangeListener(this);

        RangeSeekBar<Integer> seekBarAmp = new RangeSeekBar<>(0, 127, getActivity());
        seekBarAmp.setOnRangeSeekBarChangeListener(this);
        ((LinearLayout)rootView).addView(seekBarAmp, 7);

        RangeSeekBar<Integer> seekBarFreq = new RangeSeekBar<>(500, 2000, getActivity());
        seekBarFreq.setOnRangeSeekBarChangeListener(this);
        ((LinearLayout)rootView).addView(seekBarFreq,9);

        return rootView;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        SensorManager sensorManager = (SensorManager) getActivity().getSystemService(getActivity().SENSOR_SERVICE);
        sensorManager.registerListener(this, sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_GAME);
    }



    @Override
    public void onRangeSeekBarValuesChanged(RangeSeekBar<?> bar, Integer minValue, Integer maxValue) {
        // handle changed range values
        Log.d("seek", "User selected new range values: MIN=" + minValue + ", MAX=" + maxValue);
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if (buttonView.getId()==R.id.switchDir) {
            tiltDirCV=isChecked;
        } else if (buttonView.getId()==R.id.switchEnable) {
            if (wbThread!=null && wbThread.isAlive())
                wbThread.interrupt();
            if (isChecked) {
                wbThread = new WaveThread((MainActivity) getActivity());
                updateParams();
                wbThread.start();
            }
        }
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType()==Sensor.TYPE_ACCELEROMETER){
            double accX = event.values[0];
            double accY = event.values[1];
            double accZ = event.values[2];
            ax = Math.atan2(accZ, Math.sqrt( Math.pow(accX, 2) + Math.pow(accY, 2))) * 180 / Math.PI;
            tv.setText(String.format("%2.2f  %2.2f  %2.2f = %5.2f",accX,accY,accZ,ax));
            updateParams();
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {}
}
