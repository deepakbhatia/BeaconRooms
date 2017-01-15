package com.optimalcities.beaconrooms;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.RemoteException;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import org.altbeacon.beacon.Beacon;
import org.altbeacon.beacon.BeaconConsumer;
import org.altbeacon.beacon.BeaconManager;
import org.altbeacon.beacon.BeaconParser;
import org.altbeacon.beacon.Identifier;
import org.altbeacon.beacon.MonitorNotifier;
import org.altbeacon.beacon.RangeNotifier;
import org.altbeacon.beacon.Region;
import org.altbeacon.beacon.utils.UrlBeaconUrlCompressor;

import java.util.Collection;

public class MainActivity extends AppCompatActivity implements BeaconConsumer, MonitorNotifier,RangeNotifier, View.OnClickListener {
    protected static final String TAG = "MonitoringActivity";
    private static final int PERMISSION_REQUEST_COARSE_LOCATION = 1;
    private static  int JOINED_ROOM = 1;
    private static  String CURRENT_ROOM = null;
    private static  double CURRENT_ROOM_DISTANCE = 1000;

    private android.support.v7.app.AlertDialog alertDialog;

    private BeaconManager beaconManager;

    Button joinRoom;
    private MainActivity activity;

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        joinRoom = (Button)findViewById(R.id.joinRoom);

        joinRoom.setOnClickListener(this);
        verifyBluetooth();
        logToDisplay("Application just launched");


        activity = this;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // Android M Permission check
            if (this.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                final AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("This app needs location access");
                builder.setMessage("Please grant location access so this app can detect beacons in the background.");
                builder.setPositiveButton(android.R.string.ok, null);
                builder.setOnDismissListener(new DialogInterface.OnDismissListener() {

                    @TargetApi(23)
                    @Override
                    public void onDismiss(DialogInterface dialog) {
                        requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                                PERMISSION_REQUEST_COARSE_LOCATION);
                    }

                });
                builder.show();
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_REQUEST_COARSE_LOCATION: {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.d(TAG, "coarse location permission granted");
                } else {
                    final AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setTitle("Functionality limited");
                    builder.setMessage("Since location access has not been granted, this app will not be able to discover beacons when in the background.");
                    builder.setPositiveButton(android.R.string.ok, null);
                    builder.setOnDismissListener(new DialogInterface.OnDismissListener() {

                        @Override
                        public void onDismiss(DialogInterface dialog) {
                        }

                    });
                    builder.show();
                }
                return;
            }
        }
    }

    public void onRangingClicked(View view) {

    }

    @Override
    public void onResume() {
        super.onResume();
        //((BeaconReferenceApplication) this.getApplicationContext()).setMonitoringActivity(this);
        beaconManager = BeaconManager.getInstanceForApplication(this.getApplicationContext());
// Detect the main identifier (UID) frame:
        beaconManager.getBeaconParsers().add(new BeaconParser().
                setBeaconLayout(BeaconParser.EDDYSTONE_UID_LAYOUT));
// Detect the telemetry (TLM) frame:
        beaconManager.getBeaconParsers().add(new BeaconParser().
                setBeaconLayout(BeaconParser.EDDYSTONE_TLM_LAYOUT));
// Detect the URL frame:
        beaconManager.getBeaconParsers().add(new BeaconParser().
                setBeaconLayout(BeaconParser.EDDYSTONE_URL_LAYOUT));
        beaconManager.bind(this);
    }


    private void verifyBluetooth() {

        try {
            if (!BeaconManager.getInstanceForApplication(this).checkAvailability()) {
                final AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("Bluetooth not enabled");
                builder.setMessage("Please enable bluetooth in settings and restart this application.");
                builder.setPositiveButton(android.R.string.ok, null);
                builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialog) {
                        finish();
                        System.exit(0);
                    }
                });
                builder.show();
            }
        }
        catch (RuntimeException e) {
            final AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Bluetooth LE not available");
            builder.setMessage("Sorry, this device does not support Bluetooth LE.");
            builder.setPositiveButton(android.R.string.ok, null);
            builder.setOnDismissListener(new DialogInterface.OnDismissListener() {

                @Override
                public void onDismiss(DialogInterface dialog) {
                    finish();
                    System.exit(0);
                }

            });
            builder.show();

        }

    }

    public void logToDisplay(final String line) {

    }

    @Override
    public void onBeaconServiceConnect() {
        // Set the two identifiers below to null to detect any beacon regardless of identifiers
        Identifier myBeaconNamespaceId = Identifier.parse("0x2f234454f4911ba9ffa6");
        Identifier myBeaconInstanceId = Identifier.parse("0x000000000001");
        Region region = new Region("my-beacon-region", myBeaconNamespaceId, myBeaconInstanceId, null);
        Region urlregion = new Region("all-beacons-region", null, null, null);

        beaconManager.addMonitorNotifier(this);
        beaconManager.addRangeNotifier(this);
        try {
            beaconManager.startMonitoringBeaconsInRegion(region);
            beaconManager.startRangingBeaconsInRegion(urlregion);

        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    public void didEnterRegion(Region region) {
        Log.d(TAG, "I detected a beacon in the region with namespace id " + region.getId1() +
                " and instance id: " + region.getUniqueId());
    }

    public void didExitRegion(Region region) {

        Log.d(TAG, "Beacon exited region with namespace id " + region.getId1() +
                " and instance id: " + region.getId2());
    }

    public void didDetermineStateForRegion(int state, Region region) {
    }
    @Override
    public void didRangeBeaconsInRegion(Collection<Beacon> collection, Region region) {

        for (Beacon beacon: collection) {
            if (beacon.getServiceUuid() == 0xfeaa && beacon.getBeaconTypeCode() == 0x10) {
                // This is a Eddystone-URL frame
                String url = UrlBeaconUrlCompressor.uncompress(beacon.getId1().toByteArray());


                String roomName = url.split("room=")[1];
                //Log.d(TAG,"roomname = " + roomName+":"+beacon.getDistance() +":"+CURRENT_ROOM_DISTANCE);
                if(roomName!=null && JOINED_ROOM==1 && !roomName.equals(CURRENT_ROOM) ){


                    Log.d(TAG, " I see a beacon transmitting a url: " + url +
                            " approximately " + beacon.getDistance() + " meters away.");

                    CURRENT_ROOM = roomName;

                    CURRENT_ROOM_DISTANCE = beacon.getDistance();

                    activity.runOnUiThread(new Runnable() {


                        @Override
                        public void run() {

                            joinRoom.setText("Join " + CURRENT_ROOM);
                            joinRoom.setEnabled(true);
                        }
                    });

                }
        }
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        beaconManager.unbind(this);
    }


    /*
     * Creates an connect UI dialog
     */
    private void showConnectDialog(String url) {


        alertDialog = Dialog.createConnectVideoDialog(new TextView(this),url,connectClickListener(url),cancelConnectDialogClickListener(),this);



        alertDialog.show();

    }

    private DialogInterface.OnClickListener connectClickListener(final String url) {
        return new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {

                JOINED_ROOM = -1;
                Intent intent = new Intent(getApplicationContext(),RoomActivity.class);

                intent.putExtra("room_url",url);

                startActivity(intent);
            }
        };
    }


    private DialogInterface.OnClickListener cancelConnectDialogClickListener() {
        return new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                alertDialog.dismiss();
                JOINED_ROOM = 1;

            }
        };
    }

    @Override
    public void onClick(View view) {

        if(view.getId() == R.id.joinRoom)
        showConnectDialog(CURRENT_ROOM);

    }
}