package sh.m.wristband;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.ToggleButton;

import java.util.ArrayList;
import java.util.List;

/**
 * A placeholder fragment containing a simple view.
 */
public class Wave extends Fragment implements SeekBar.OnSeekBarChangeListener, CompoundButton.OnCheckedChangeListener {
    /**
     * The fragment argument representing the section number for this
     * fragment.
     */
    private static final String ARG_SECTION_NUMBER = "section_number";
    private TextView tvAmp;
    private TextView tvOn;
    private TextView tvOff;

    /**
     * Returns a new instance of this fragment for the given section
     * number.
     */
    int waveAmp=127;
    int waveOff=100;
    int waveOn=100;
    boolean waveDirCV=true;
    private WaveThread wbThread=null;

    public static Wave newInstance(int sectionNumber) {
        Wave fragment = new Wave();
        Bundle args = new Bundle();
        args.putInt(ARG_SECTION_NUMBER, sectionNumber);
        fragment.setArguments(args);
        return fragment;
    }

    public Wave() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.frag_wave, container, false);
        tvAmp= (TextView) rootView.findViewById(R.id.textViewAmp);
        tvOn = (TextView) rootView.findViewById(R.id.textViewOn);
        tvOff = (TextView) rootView.findViewById(R.id.textViewOff);

        ((SeekBar)rootView.findViewById(R.id.seekBarAmp)).setOnSeekBarChangeListener(this);
        ((SeekBar)rootView.findViewById(R.id.seekBarOff)).setOnSeekBarChangeListener(this);
        ((SeekBar)rootView.findViewById(R.id.seekBarOn )).setOnSeekBarChangeListener(this);

        ((ToggleButton)rootView.findViewById(R.id.switchDir   )).setOnCheckedChangeListener(this);
        ((ToggleButton)rootView.findViewById(R.id.switchEnable)).setOnCheckedChangeListener(this);
        return rootView;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        //int m=seekBar.getId();
        //WristBand wb = ((MainActivity) getActivity()).getWb();
        /*
        if (wb!=null && boxes.get(m).isChecked())
            wb.setPWM(m, (progress+127));*/
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {}

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        switch (seekBar.getId()) {
            case R.id.seekBarAmp:
                waveAmp=seekBar.getProgress()+127;
                tvAmp.setText("Amplitude "+waveAmp);
                break;
            case R.id.seekBarOff:
                waveOff=seekBar.getProgress()+10;
                tvOff.setText("Off time "+waveOff);
                break;
            case R.id.seekBarOn:
                waveOn=seekBar.getProgress()+10;
                tvOn.setText("On time "+waveOn);
                break;
        }
        if (wbThread!=null)
            wbThread.setWaveParams(waveAmp,waveOff,waveOn,waveDirCV);
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if (buttonView.getId()==R.id.switchDir) {
            waveDirCV=isChecked;
            if (wbThread!=null)
                wbThread.setWaveParams(waveAmp,waveOff,waveOn,waveDirCV);
            Log.d("wave", "dir changed to "+isChecked);
        } else if (buttonView.getId()==R.id.switchEnable) {
            if (wbThread!=null && wbThread.isAlive())
                wbThread.interrupt();
            if (isChecked) {
                wbThread = new WaveThread((MainActivity) getActivity());
                wbThread.setWaveParams(waveAmp, waveOff, waveOn, waveDirCV);
                wbThread.start();

            }
        }
    }
}
