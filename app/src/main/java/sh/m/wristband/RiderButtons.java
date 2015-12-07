package sh.m.wristband;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.SeekBar;

import java.nio.channels.NotYetConnectedException;
import java.util.ArrayList;
import java.util.List;

/**
 * A placeholder fragment containing a simple view.
 */
public class RiderButtons extends Fragment implements SeekBar.OnSeekBarChangeListener, CompoundButton.OnCheckedChangeListener, View.OnClickListener, View.OnTouchListener {
    /**
     * The fragment argument representing the section number for this
     * fragment.
     */
    private static final String ARG_SECTION_NUMBER = "section_number";
    private CheckBox cbr;
    private CheckBox cbb;
    private CheckBox cbl;
    private SeekBar sb;
    private ProgressBar progressBar;
    private byte rightId=1;
    private byte leftId=5;
    private Handler handler=new Handler();

    /**
     * Returns a new instance of this fragment for the given section
     * number.
     */



    public static RiderButtons newInstance(int sectionNumber) {
        RiderButtons fragment = new RiderButtons();
        Bundle args = new Bundle();
        args.putInt(ARG_SECTION_NUMBER, sectionNumber);
        fragment.setArguments(args);
        return fragment;
    }

    public RiderButtons() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.frag_rider, container, false);
        // toggle buttons
        rootView.findViewById(R.id.btnb1).setOnClickListener(this);
        rootView.findViewById(R.id.btnb2).setOnClickListener(this);
        rootView.findViewById(R.id.btnr1).setOnClickListener(this);
        rootView.findViewById(R.id.btnr2).setOnClickListener(this);
        rootView.findViewById(R.id.btnl1).setOnClickListener(this);
        rootView.findViewById(R.id.btnl2).setOnClickListener(this);
        // push buttons
        rootView.findViewById(R.id.btnb1ph).setOnTouchListener(this);
        rootView.findViewById(R.id.btnl1ph).setOnTouchListener(this);
        rootView.findViewById(R.id.btnr1ph).setOnTouchListener(this);
        // checkboxes
        cbb=((CheckBox) rootView.findViewById(R.id.cbb));
        cbb.setOnCheckedChangeListener(this);
        cbr=((CheckBox) rootView.findViewById(R.id.cbr));
        cbr.setOnCheckedChangeListener(this);
        cbl=((CheckBox) rootView.findViewById(R.id.cbl));
        cbl.setOnCheckedChangeListener(this);
        // strength
        sb=((SeekBar) rootView.findViewById(R.id.seekBar));
        sb.setOnSeekBarChangeListener(this);
        // feedback
        progressBar= (ProgressBar) rootView.findViewById(R.id.progressBar);
        progressBar.setVisibility(View.INVISIBLE);
        return rootView;
    }


    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);/*
        ((MainActivity) activity).onSectionAttached(
                getArguments().getInt(ARG_SECTION_NUMBER));*/
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        Log.d("bars","seekbar changed");
        if (cbl.isChecked())
            //((MainActivity) getActivity()).trySendingCommand(leftId, (byte) (progress*2));
            ((MainActivity) getActivity()).trySetVal(leftId, (float)progress/100f);

        if (cbr.isChecked())
            ((MainActivity) getActivity()).trySetVal(rightId, (float)progress/100f);
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {}

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {}

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        MainActivity ma = ((MainActivity) getActivity());
        int id=buttonView.getId();
        switch(id) {
            case R.id.cbb:
                cbl.setChecked(cbb.isChecked());
                cbr.setChecked(cbb.isChecked());
                break;
            case R.id.cbr:
                ma.trySetVal(rightId,
                        cbr.isChecked() ? ((float) sb.getProgress())/100f : 0);
                if (!isChecked && cbb.isChecked()) {
                    cbb.setChecked(false);
                    cbl.setChecked(true);
                }
                break;
            case R.id.cbl:
                ma.trySetVal(leftId,
                        cbl.isChecked() ? ((float) sb.getProgress())/100f : 0);
                if (!isChecked && cbb.isChecked()) {
                    cbb.setChecked(false);
                    cbr.setChecked(true);
                }
                break;
        }
    }

    @Override
    public void onClick(View v) {
        CheckBox c = null;
        int delay=2000;
        switch (v.getId()) {
            //both
            case R.id.btnb1:
                c=cbb;
                delay=2000;
                break;
            case R.id.btnb2:
                c=cbb;
                delay=4000;
                break;

            //left
            case R.id.btnl1:
                c=cbl;
                delay=2000;
                break;
            case R.id.btnl2:
                c=cbl;
                delay=250;
                break;

            //right
            case R.id.btnr1:
                c=cbr;
                delay=2000;
                break;
            case R.id.btnr2:
                c=cbr;
                delay=250;
                break;

        }
        if (c!=null) {
            c.setChecked(true);
            progressBar.setVisibility(View.VISIBLE);
            uncheckDelayed(c, delay);
        }
    }

    private void uncheckDelayed(final CheckBox cb, long i) {
        /*if (handler!=null)
            handler.removeCallbacksAndMessages(null);*/
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                cb.setChecked(false);
                progressBar.setVisibility(View.INVISIBLE);
            }
        },i);

    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        CheckBox c = null;
        switch (v.getId()) {
            case R.id.btnb1ph:
                c=cbb;
                break;

            //left
            case R.id.btnl1ph:
                c=cbl;
                break;

            //right
            case R.id.btnr1ph:
                c=cbr;
                break;
        }
        if(event.getAction() == MotionEvent.ACTION_DOWN) {
            c.setChecked(true);
        } else if (event.getAction() == MotionEvent.ACTION_UP) {
            c.setChecked(false);
        }

        if (c!=null) {
            if (event.getAction() == MotionEvent.ACTION_UP ||
                    event.getAction() == MotionEvent.ACTION_CANCEL) {
                c.setChecked(false);
            } else if (event.getAction() == MotionEvent.ACTION_UP) {
                c.setChecked(true);
            }
        }
        return false;
    }
}
