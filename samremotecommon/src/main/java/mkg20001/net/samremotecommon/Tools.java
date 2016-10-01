package mkg20001.net.samremotecommon;

import android.os.Build;
import android.util.Base64;
import android.util.Log;

import net.nodestyle.helper.Array;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class Tools {
    public static String base64(String s) {
        if (s==null) s=""; //eq to null
        return Base64.encodeToString(s.getBytes(), Base64.DEFAULT);
    }
    public static int base64len(String s) {
        return base64(s).length();
    }
    public static String chr(int c) {
        return Character.toString((char)c);
    }
    public static String getDeviceName() {
        String manufacturer = Build.MANUFACTURER;
        String model = Build.MODEL;
        if (model.startsWith(manufacturer)) {
            return capitalize(model);
        } else {
            return capitalize(manufacturer) + " " + model;
        }
    }

    public static void log(String s) {
        // do something for a debug build
        //if (BuildConfig.DEBUG)
        Log.d("SamRemote",s);
    }

    public static void log2(String s) {
        // always print - like error or key send
        System.out.println(s);
    }


    private static String capitalize(String s) {
        if (s == null || s.length() == 0) {
            return "";
        }
        char first = s.charAt(0);
        if (Character.isUpperCase(first)) {
            return s;
        } else {
            return Character.toUpperCase(first) + s.substring(1);
        }
    }

    private static final int NB_THREADS = 25;
    public static Array ips=new Array();

    public static String[] doScan() {
        ips.clear();
        String LOG_TAG="loggg";
        Log.i(LOG_TAG, "Start scanning");

        ExecutorService executor = Executors.newFixedThreadPool(NB_THREADS);
        for(int dest=0; dest<255; dest++) {
            String host = "192.168.178." + dest;
            executor.execute(pingRunnable(host));
        }

        Log.i(LOG_TAG, "Waiting for executor to terminate...");
        executor.shutdown();
        try { executor.awaitTermination(60*1000, TimeUnit.MILLISECONDS); } catch (InterruptedException ignored) { }

        Log.i(LOG_TAG, "Scan finished");

        String[] res=new String[ips.length];

        Integer i=0;

        for (java.lang.Object o:ips.getItems()) {
            res[i]=(String) o;
            i++;
        }

        return res;
    }

    private static Runnable pingRunnable(final String host) {
        return new Runnable() {
            public void run() {
                Tools.log( "Pinging " + host + "...");
                try {
                    InetAddress inet = InetAddress.getByName(host);
                    boolean reachable = inet.isReachable(1000);
                    if (reachable) ips.push(host);
                    Tools.log( "=> Result: " + (reachable ? "reachable" : "not reachable"));
                } catch (UnknownHostException e) {
                    Log.e("SamRemote", "Not found", e);
                } catch (IOException e) {
                    Log.e("SamRemote", "IO Error", e);
                }
            }
        };
    }
}