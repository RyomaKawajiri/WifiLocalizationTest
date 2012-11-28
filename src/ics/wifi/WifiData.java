package ics.wifi;

import ics.wifi.WifiData.Pair;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import android.os.Environment;
import android.util.Log;

public class WifiData extends ArrayList<Pair<Location, List<ScanResult>>> {
  private static final long serialVersionUID = 2523377242664461905L;
  static final String TAG = "WifiData";
  static final String filename = "data";

  public class Pair<K, V> implements Serializable {
    private static final long serialVersionUID = -572322653137274974L;
    public Pair(K first, V second) {
      this.first = first;
      this.second = second;
    }
    public K first;
    public V second;
  }

  public WifiData() {
    super();
  }

  public void add(Location loc, List<android.net.wifi.ScanResult> results) {
    Log.i(TAG, "x = " + loc.x + ", y = "+ loc.y);
    this.add(new Pair<Location, List<ScanResult>>(loc, convertScanResultList(results)));
  }

  public Location localize(List<android.net.wifi.ScanResult> rawResults) {
    if(this.isEmpty())
      return null;

    List<ScanResult> results = convertScanResultList(rawResults); 

    Location loc = new Location(8, 0.0f, 0.0f);
    double minDistance = Double.MAX_VALUE;

    for(Pair<Location, List<ScanResult>> datum: this) {
      double dist = distance(datum.second, results);
      if(minDistance > dist) {
        minDistance = dist;
        loc = datum.first;
      }
    }

    Log.i(TAG, loc.x + ", " + loc.y);

    return loc;
  }

  private List<ScanResult> convertScanResultList(List<android.net.wifi.ScanResult> results) {
    ArrayList<ScanResult> ret = new ArrayList<ScanResult>();

    for(android.net.wifi.ScanResult r : results){
      ret.add(new ScanResult(r));
    }

    return ret;
  }

  private double distance(List<ScanResult> A, List<ScanResult> B) {
    sortResults(A);
    sortResults(B);

    double error_sum = 0;

    Iterator<ScanResult> ait = A.iterator();
    Iterator<ScanResult> bit = B.iterator();
    ScanResult a = ait.next();
    ScanResult b = bit.next();
    while(ait.hasNext() && bit.hasNext()) {
      if(a.BSSID.equals(b.BSSID)) {
        error_sum += Math.pow(a.level - b.level, 2);
        a = ait.next();
        b = bit.next();
      }else if(a.BSSID.compareTo(b.BSSID) > 0) {
        error_sum += Math.pow(a.level - (-100), 2);
        a = ait.next();
      }else{
        error_sum += Math.pow(b.level - (-100), 2);
        b = bit.next();
      }
    }

    while(ait.hasNext()) {
      error_sum += Math.pow(a.level - (-100), 2);
      a = ait.next();
    }

    while(bit.hasNext()) {
      error_sum += Math.pow(b.level - (-100), 2);
      b = bit.next();
    }

    return error_sum;
  }

  private void sortResults(List<ScanResult> r) {
    Comparator<ScanResult> comparator = new Comparator<ScanResult>() {
      @Override
      public int compare(ScanResult lhs, ScanResult rhs) {
        return lhs.BSSID.compareTo(rhs.BSSID);
      }
    };

    Collections.sort(r, comparator);
  }

  static public WifiData load() {
    FileInputStream inFile;
    try {
      inFile = new FileInputStream(getWifiFile());
      ObjectInputStream inObject = new ObjectInputStream(inFile);

      WifiData ret = (WifiData) inObject.readObject();

      inObject.close();
      inFile.close();

      return ret;

    } catch (Exception e) {
      e.printStackTrace();
      return null;
    }
  }


  public boolean save() {
    try {
      FileOutputStream outFile = new FileOutputStream(WifiData.getWifiFile());
      ObjectOutputStream outObject = new ObjectOutputStream(outFile);

      outObject.writeObject(this);

      outObject.close();
      outFile.close();      
    } catch (Exception e) {
      e.printStackTrace();
      return false;
    }

    return true;
  }

  static private File getWifiFile() {
    File file = new File(Environment.getExternalStorageDirectory(), filename);
    // file.delete();
    return file;
  }
}
