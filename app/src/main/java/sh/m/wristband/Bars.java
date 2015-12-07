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

import java.util.ArrayList;
import java.util.List;

/**
 * A placeholder fragment containing a simple view.
 */
public class Bars extends Fragment implements SeekBar.OnSeekBarChangeListener, CompoundButton.OnCheckedChangeListener, View.OnClickListener {
    /**
     * The fragment argument representing the section number for this
     * fragment.
     */
    private static final String ARG_SECTION_NUMBER = "section_number";

    /**
     * Returns a new instance of this fragment for the given section
     * number.
     */


    protected List<SeekBar> bars=new ArrayList<>();
    protected List<CheckBox> boxes=new ArrayList<>();

    public static Bars newInstance(int sectionNumber) {
        Bars fragment = new Bars();
        Bundle args = new Bundle();
        args.putInt(ARG_SECTION_NUMBER, sectionNumber);
        fragment.setArguments(args);
        return fragment;
    }

    public Bars() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.frag_manual, container, false);
        LinearLayout ll= (LinearLayout) rootView.findViewById(R.id.ll_bars);
        rootView.findViewById(R.id.buttonDis).setOnClickListener(this);
        rootView.findViewById(R.id.buttonReset).setOnClickListener(this);
        rootView.findViewById(R.id.buttonEn).setOnClickListener(this);
        for (int i=0;i<6;i++) {
            View view=  getActivity().getLayoutInflater().inflate(R.layout.bar, null);
            //view.setVisibility(shouldBarBeVisible(i));
            SeekBar s= (SeekBar) view.findViewById(R.id.sbar);
            s.setId(i);
            s.setOnSeekBarChangeListener(this);
            bars.add(s);

            CheckBox cb= (CheckBox) view.findViewById(R.id.cbox);
            cb.setId(i);
            cb.setOnCheckedChangeListener(this);
            boxes.add(cb);

            ll.addView(view);
        }

        return rootView;
    }

    public int shouldBarBeVisible(int i) {
        return View.VISIBLE;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);/*
        ((MainActivity) activity).onSectionAttached(
                getArguments().getInt(ARG_SECTION_NUMBER));*/
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        int m=seekBar.getId();
        float x=(float)progress/100f;
        Log.d("bars","seekbar changed id "+m + " val "+ x);
        if (boxes.get(m).isChecked())
            //getMyActivity().trySendingCommand(m,127+progress);
            getMyActivity().trySetVal(m,x);
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {}

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {}

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        int id=buttonView.getId();
        /*getMyActivity().trySendingCommand(
                id,isChecked ? (127 + bars.get(id).getProgress()) : 127);*/
        getMyActivity().trySetVal(id,isChecked ? bars.get(id).getProgress()/100 : 0);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.buttonEn:
                //getMyActivity().trySendingCommand(VibroBrd.CMD_ENABLE,1);
                getMyActivity().trySetEnable(true);
                break;
            case R.id.buttonDis:
                //getMyActivity().trySendingCommand(VibroBrd.CMD_ENABLE,0);
                getMyActivity().trySetEnable(false);
                break;
            case R.id.buttonReset:
                for (SeekBar sb : bars) {
                    sb.setProgress(0);
                }
        }
    }

    public MainActivity getMyActivity() {
        return (MainActivity)getActivity();
    }
}
