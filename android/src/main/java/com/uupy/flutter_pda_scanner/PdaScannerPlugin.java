package com.uupy.flutter_pda_scanner;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;

import androidx.annotation.NonNull;

import io.flutter.plugin.common.EventChannel;
import io.flutter.plugin.common.PluginRegistry.Registrar;
import io.flutter.embedding.android.FlutterActivity;
import io.flutter.embedding.engine.plugins.FlutterPlugin;
import io.flutter.embedding.engine.plugins.FlutterPlugin.FlutterPluginBinding;
import io.flutter.embedding.engine.plugins.activity.ActivityAware;
import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding;

public class PdaScannerPlugin implements EventChannel.StreamHandler, FlutterPlugin, ActivityAware {
    private static final String CHANNEL = "com.uupy.flutter_pda_scanner/plugin";
    private static final String XM_SCAN_ACTION = "com.android.server.scannerservice.broadcast";
    private static final String SHINIOW_SCAN_ACTION = "com.android.server.scannerservice.shinow";
    private static final String IDATA_SCAN_ACTION = "android.intent.action.SCANRESULT";
    private static final String YBX_SCAN_ACTION = "android.intent.ACTION_DECODE_DATA";
    private static final String PL_SCAN_ACTION = "scan.rcv.message";
    private static final String BARCODE_DATA_ACTION = "com.ehsy.warehouse.action.BARCODE_DATA";
    private static final String HONEYWELL_SCAN_ACTION = "com.honeywell.decode.intent.action.EDIT_DATA";
    private static final String NL_SCAN_ACTION = "nlscan.action.SCANNER_RESULT";
    private static final String KAICOM_SCAN_ACTION = "com.android.receive_scan_action";

    private static EventChannel.EventSink eventSink;
    private static EventChannel channel;
    private static BinaryMessenger binaryMessenger;

    private static final BroadcastReceiver scanReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String actionName = intent.getAction();
            if (XM_SCAN_ACTION.equals(actionName) || SHINIOW_SCAN_ACTION.equals(actionName)) {
                eventSink.success(intent.getStringExtra("scannerdata"));
            } else if (IDATA_SCAN_ACTION.equals(actionName)) {
                eventSink.success(intent.getStringExtra("value"));
            } else if (YBX_SCAN_ACTION.equals(actionName)) {
                eventSink.success(intent.getStringExtra("barcode_string"));
            } else if (PL_SCAN_ACTION.equals(actionName)) {
                byte[] barcode = intent.getByteArrayExtra("barocode");
                int barcodelen = intent.getIntExtra("length", 0);
                String result = new String(barcode, 0, barcodelen);
                eventSink.success(result);
            } else if (HONEYWELL_SCAN_ACTION.equals(actionName) || BARCODE_DATA_ACTION.equals(actionName) || KAICOM_SCAN_ACTION.equals(actionName)) {
                eventSink.success(intent.getStringExtra("data"));
            } else if (NL_SCAN_ACTION.equals(actionName)) {
                eventSink.success(intent.getStringExtra("SCAN_BARCODE1"));
            } else {
                Log.i("PdaScannerPlugin", "NoSuchAction");
            }
        }
    };

    private PdaScannerPlugin(Activity activity) {
        IntentFilter xmIntentFilter = new IntentFilter();
        xmIntentFilter.addAction(XM_SCAN_ACTION);
        xmIntentFilter.setPriority(Integer.MAX_VALUE);
        activity.registerReceiver(scanReceiver, xmIntentFilter);

        IntentFilter shinowIntentFilter = new IntentFilter();
        shinowIntentFilter.addAction(SHINIOW_SCAN_ACTION);
        shinowIntentFilter.setPriority(Integer.MAX_VALUE);
        activity.registerReceiver(scanReceiver, shinowIntentFilter);

        IntentFilter iDataIntentFilter = new IntentFilter();
        iDataIntentFilter.addAction(IDATA_SCAN_ACTION);
        iDataIntentFilter.setPriority(Integer.MAX_VALUE);
        activity.registerReceiver(scanReceiver, iDataIntentFilter);

        IntentFilter yBoXunIntentFilter = new IntentFilter();
        yBoXunIntentFilter.addAction(YBX_SCAN_ACTION);
        yBoXunIntentFilter.setPriority(Integer.MAX_VALUE);
        activity.registerReceiver(scanReceiver, yBoXunIntentFilter);

        IntentFilter pLIntentFilter = new IntentFilter();
        pLIntentFilter.addAction(PL_SCAN_ACTION);
        pLIntentFilter.setPriority(Integer.MAX_VALUE);
        activity.registerReceiver(scanReceiver, pLIntentFilter);

        IntentFilter honeyIntentFilter = new IntentFilter();
        honeyIntentFilter.addAction(BARCODE_DATA_ACTION);
        honeyIntentFilter.setPriority(Integer.MAX_VALUE);
        activity.registerReceiver(scanReceiver, honeyIntentFilter);

        IntentFilter honeywellIntentFilter = new IntentFilter();
        honeywellIntentFilter.addAction(HONEYWELL_SCAN_ACTION);
        honeywellIntentFilter.setPriority(Integer.MAX_VALUE);
        activity.registerReceiver(scanReceiver, honeywellIntentFilter);

        // 新大陆
        IntentFilter newlandIntentFilter = new IntentFilter();
        newlandIntentFilter.addAction(NL_SCAN_ACTION);
        newlandIntentFilter.setPriority(Integer.MAX_VALUE);
        activity.registerReceiver(scanReceiver, newlandIntentFilter);

        // 凯立
        IntentFilter kaicomIntentFilter = new IntentFilter();
        kaicomIntentFilter.addAction(KAICOM_SCAN_ACTION);
        kaicomIntentFilter.setPriority(Integer.MAX_VALUE);
        activity.registerReceiver(scanReceiver, kaicomIntentFilter);
    }

    // This static method is only to remain compatible with apps that don’t use the v2 Android embedding.
    // @Deprecated()
    // @SuppressLint("Registrar")
    @SuppressWarnings("deprecation")
    public static void registerWith(Registrar registrar) {
        channel = new EventChannel(registrar.messenger(), CHANNEL);
        PdaScannerPlugin plugin = new PdaScannerPlugin(registrar.activity());
        channel.setStreamHandler(plugin);
    }

    @Override
    public void onListen(Object o, final EventChannel.EventSink eventSink) {
        PdaScannerPlugin.eventSink = eventSink;
    }

    @Override
    public void onCancel(Object o) {
        Log.i("PdaScannerPlugin", "PdaScannerPlugin:onCancel");
    }

    // The method that use v2 Android embedding.
    @Override
    public void onAttachedToEngine(FlutterPluginBinding binding) {
        binaryMessenger = binding.getBinaryMessenger();
    }

    @Override
    public void onDetachedFromEngine(FlutterPluginBinding binding) { 
        channel.setStreamHandler(null);
        channel = null;
    }

    @Override
    public void onAttachedToActivity(ActivityPluginBinding activityBinding) {        
        channel = new EventChannel(binaryMessenger, CHANNEL);
        PdaScannerPlugin plugin = new PdaScannerPlugin(activityBinding.getActivity());
        channel.setStreamHandler(plugin);
    }

    @Override
    public void onDetachedFromActivity() {
    }
}
