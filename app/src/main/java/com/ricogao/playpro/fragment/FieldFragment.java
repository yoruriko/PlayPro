package com.ricogao.playpro.fragment;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.ricogao.playpro.R;
import com.ricogao.playpro.activity.FieldListActivity;
import com.ricogao.playpro.model.Event;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindColor;
import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by ricogao on 2017/4/5.
 */

public class FieldFragment extends EventFragment {

    @BindView(R.id.state_pie_chart)
    PieChart statePieChart;
    @BindView(R.id.position_centre_pie_chart)
    PieChart positionCentreChart;
    @BindView(R.id.position_font_pie_chart)
    PieChart positionFontChart;

    @BindColor(R.color.background_light)
    int backgroundLight;

    private List<Entry> stateEntries, positionEntries1, positionEntries2;
    private List<String> stateLabel, positionLabel1, positionLabel2;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.field_fragment_layout, container, false);
        ButterKnife.bind(this, view);

        if (event.getAnalysis() == null) {
            showDialog();
        }

        return view;
    }

    @Override
    public void setEvent(Event event) {
        super.setEvent(event);
        processData();
        if(positionFontChart!=null){
            showData();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        showData();
    }

    private void processData() {
        if (event.getAnalysis() == null) {
            return;
        }
        processState();
        processPositionCentre();
        processPositionFont();

    }

    private void showFieldSelectPage() {
        Intent it = new Intent(this.getContext(), FieldListActivity.class);
        it.putExtra("eventId", event.getId());
        startActivity(it);
    }

    private void showDialog() {
        new AlertDialog.Builder(this.getContext())
                .setTitle("Do you want to associate field with this session now?")
                .setMessage("This session have not been associated with any field yet, some information will not be displayed.")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        showFieldSelectPage();
                    }
                })
                .setNegativeButton("Later", null)
                .setCancelable(false)
                .show();
    }

    private void showData() {
        if (event.getAnalysis() != null) {
            showPieChar(stateEntries, stateLabel, statePieChart);
            showPieChar(positionEntries1, positionLabel1, positionCentreChart);
            showPieChar(positionEntries2, positionLabel2, positionFontChart);
        }
    }

    private void processState() {
        int stand = event.getAnalysis().getStandCount();
        int walk = event.getAnalysis().getWalkCount();
        int run = event.getAnalysis().getRunCount();
        float total = stand + walk + run;

        stateEntries = new ArrayList<>();
        stateEntries.add(new Entry(stand / total * 100f, 0));
        stateEntries.add(new Entry(walk / total * 100f, 1));
        stateEntries.add(new Entry(run / total * 100f, 2));

        stateLabel = new ArrayList<>();
        stateLabel.add("Stand");
        stateLabel.add("Walk");
        stateLabel.add("Run");
    }

    private void processPositionCentre() {
        int centre = event.getAnalysis().getCentreCount();
        int wing = event.getAnalysis().getWingCount();
        float total = centre + wing;

        positionEntries1 = new ArrayList<>();
        positionEntries1.add(new Entry(centre / total * 100f, 0));
        positionEntries1.add(new Entry(wing / total * 100f, 1));

        positionLabel1 = new ArrayList<>();
        positionLabel1.add("Centre");
        positionLabel1.add("Wing");
    }

    private void processPositionFont() {
        int font = event.getAnalysis().getFontCount();
        int midField = event.getAnalysis().getMidFieldCount();
        int back = event.getAnalysis().getBackCount();
        float total = font + midField + back;

        positionEntries2 = new ArrayList<>();
        positionEntries2.add(new Entry(font / total * 100f, 0));
        positionEntries2.add(new Entry(midField / total * 100f, 1));
        positionEntries2.add(new Entry(back / total * 100f, 2));

        positionLabel2 = new ArrayList<>();
        positionLabel2.add("Font");
        positionLabel2.add("Midfield");
        positionLabel2.add("Back");
    }

    private void showPieChar(List<Entry> entries, List<String> label, PieChart pieChart) {
        PieDataSet dataSet = new PieDataSet(entries, "");

        dataSet.setColors(ColorTemplate.COLORFUL_COLORS);
        PieData data = new PieData(label, dataSet);

        pieChart.setHoleColor(backgroundLight);
        pieChart.setDescription("");
        pieChart.setData(data);
        pieChart.getLegend().setTextColor(Color.WHITE);
    }

}
