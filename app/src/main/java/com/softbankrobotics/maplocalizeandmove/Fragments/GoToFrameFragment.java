package com.softbankrobotics.maplocalizeandmove.Fragments;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.airbnb.lottie.LottieAnimationView;
import com.aldebaran.qi.sdk.object.actuation.AttachedFrame;
import com.softbankrobotics.maplocalizeandmove.MainActivity;
import com.softbankrobotics.maplocalizeandmove.R;
import com.softbankrobotics.maplocalizeandmove.Utils.Popup;

import java.util.Map;
import java.util.concurrent.ExecutionException;

public class GoToFrameFragment extends Fragment {

    private static final String TAG = "GoToFrameFragment";
    public Popup goToPopup;
    public ImageView check;
    public TextView goto_text;
    public ImageView cross;
    public LottieAnimationView goto_loader;
    private MainActivity ma;
    private View localView;
    private Button goToRandomButton;
    private Button stopGoToRandomButton;

    /**
     * inflates the layout associated with this fragment
     * if an application theme is set it will be applied to this fragment.
     */

    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             Bundle savedInstanceState) {
        int fragmentId = R.layout.fragment_goto_frame;
        this.ma = (MainActivity) getActivity();
        if (ma != null) {
            Integer themeId = ma.getThemeId();
            if (themeId != null) {
                final Context contextThemeWrapper = new ContextThemeWrapper(ma, themeId);
                LayoutInflater localInflater = inflater.cloneInContext(contextThemeWrapper);
                return localInflater.inflate(fragmentId, container, false);
            } else {
                return inflater.inflate(fragmentId, container, false);
            }
        } else {
            Log.e(TAG, "could not get mainActivity, can't create fragment");
            return null;
        }
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        localView = view;
        view.findViewById(R.id.back_button).setOnClickListener((v) ->
                ma.setFragment(new ProductionFragment(), true));

        goToRandomButton = view.findViewById(R.id.goto_random);
        stopGoToRandomButton = view.findViewById(R.id.stop_goto_random);

        if (ma.goToRandomWasActivated) {
            gotoRandomButtonActivated();
        } else {
            stopGoToRandomButtonActivated();
        }

        goToRandomButton.setOnClickListener((v) -> {
            gotoRandomButtonActivated();
            ma.goToRandomLocation(true);

        });

        stopGoToRandomButton.setOnClickListener((v) -> {
            stopGoToRandomButtonActivated();
            ma.goToRandomLocation(false);
        });

        Switch goToMaxSpeedSwitch = view.findViewById(R.id.gotomaxspeed);
        goToMaxSpeedSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> ma.goToMaxSpeed = isChecked);

        Switch goToStraightSwitch = view.findViewById(R.id.gotostraight);
        goToStraightSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> ma.goToStraight = isChecked);
        displayLocations();
    }

    private void gotoRandomButtonActivated() {
        goToRandomButton.setEnabled(false);
        goToRandomButton.setAlpha((float) 0.5);
        stopGoToRandomButton.setEnabled(true);
        stopGoToRandomButton.setAlpha(1);
    }

    private void stopGoToRandomButtonActivated() {
        stopGoToRandomButton.setEnabled(false);
        stopGoToRandomButton.setAlpha((float) 0.5);
        goToRandomButton.setEnabled(true);
        goToRandomButton.setAlpha(1);
    }

    public void createGoToPopup() {
        goToPopup = new Popup(R.layout.popup_goto, this, ma);
        goto_loader = goToPopup.inflator.findViewById(R.id.goto_loader);
        check = goToPopup.inflator.findViewById(R.id.check);
        goto_text = goToPopup.inflator.findViewById(R.id.goto_text);
        cross = goToPopup.inflator.findViewById(R.id.cross);
        Button close_button = goToPopup.inflator.findViewById(R.id.close_button);
        close_button.setOnClickListener((v) -> {
            goToPopup.dialog.hide();
            ma.robotHelper.goToHelper.checkAndCancelCurrentGoto();
            Log.d(TAG, "GoToPopup: GoTo Action canceled by user");
        });
    }

    private void displayLocations() {
        Log.d(TAG, "displayLocations: started");
        if (ma.savedLocations.isEmpty()) {
            try {
                ma.loadLocations().get();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
        }

        if (ma.savedLocations.isEmpty()) noLocationsToLoad();
        else {

            LayoutInflater inflater = this.getLayoutInflater();
            final LinearLayout linearLayout = localView.findViewById(R.id.container);
            linearLayout.removeAllViews();

            //Adding mapFrame in List
            LinearLayout mapFrameLayout = (LinearLayout) inflater.inflate(R.layout.item_location_list_goto, null);
            TextView mapFrameName = (TextView) mapFrameLayout.getChildAt(1);
            mapFrameName.setText("MapFrame");
            ImageView separationList = new ImageView(this.getContext());
            separationList.setImageResource(R.drawable.ic_separation_list);
            Button goToFrameButton = (Button) mapFrameLayout.getChildAt(2);
            goToFrameButton.setOnClickListener((useless) -> {
                ma.goToLocation("mapFrame");
            });
            linearLayout.addView(mapFrameLayout);
            linearLayout.addView(separationList);

            //Adding all locations loaded in list
            for (Map.Entry<String, AttachedFrame> stringAttachedFrameEntry : ma.savedLocations.entrySet()) {
                Map.Entry pair = stringAttachedFrameEntry;
                LinearLayout v = (LinearLayout) inflater.inflate(R.layout.item_location_list_goto, null);
                TextView locationName = (TextView) v.getChildAt(1);
                locationName.setText(pair.getKey().toString());
                ImageView separationListbis = new ImageView(this.getContext());
                separationListbis.setImageResource(R.drawable.ic_separation_list);
                Button goToButton = (Button) v.getChildAt(2);
                goToButton.setOnClickListener((useless) -> {
                    //Log.d(TAG, "Goto Frame: " + pair.getKey().toString());
                    ma.goToLocation(pair.getKey().toString());
                });
                linearLayout.addView(v);
                linearLayout.addView(separationListbis);

            }
        }
        Log.d(TAG, "displayLocations: finished");
    }

    private void noLocationsToLoad() {
        Popup noLocationsPopup = new Popup(R.layout.popup_no_locations_to_load, this, ma);
        Button close = noLocationsPopup.inflator.findViewById(R.id.close_button);
        close.setOnClickListener((v) -> {
            noLocationsPopup.dialog.hide();
            ma.setFragment(new MainFragment(), true);
        });
        noLocationsPopup.dialog.show();
    }

}
