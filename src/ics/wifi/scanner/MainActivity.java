package ics.wifi.scanner;

import ics.wifi.Location;
import ics.wifi.ScanResult;
import ics.wifi.WifiData;
import ics.wifi.WifiData.Pair;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;

public class MainActivity extends Activity implements OnTouchListener{
  private WifiData mData;
  private WifiScanReceiver mWifi;
  private RelativeLayout mFloorPlanLayout;
  private ArrayList<ImageView> mWifiDataImageList = new ArrayList<ImageView>();

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    setContentView(R.layout.activity_main);

    ImageView floorImage = (ImageView) findViewById(R.id.floorplan);
    floorImage.setOnTouchListener(new OnTouchListener(){
      @Override
      public boolean onTouch(View v, MotionEvent motionevent) {
        Log.i("Touch Event", "");
        if(motionevent.getAction() == MotionEvent.ACTION_DOWN) {
          Location loc = new Location(8, motionevent.getX(), motionevent.getY());
          mWifi.startSave(loc);
          return true;
        }
        return false;
      }
    });
    
    Button localizeButton = (Button) findViewById(R.id.localizeButton);
    localizeButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        mWifi.startLocalize();
      }
    });
    
    Button resetButton = (Button) findViewById(R.id.resetButton);
    resetButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        mData.clear();
        clearWifiDataImages();
      }
    });

    mWifi = new WifiScanReceiver(this, mData);

    mFloorPlanLayout = (RelativeLayout) findViewById(R.id.floorplanlayout);
    
    mData = WifiData.load();
    if(mData == null) {
      Log.e("Main", "cannot load wifi data");
      mData = new WifiData();
      return;
    }
    for(Pair<Location, List<ScanResult>> datum : mData) {
      addWifiDataImage(datum.first.x, datum.first.y);
    }
  }

  @Override
  public void onPause() {
    super.onPause();
    if(!mData.save()) {
      Log.e("Main", "cannot save wifi data");
    }
  }

  @Override
  public boolean onTouch(View view, MotionEvent motionevent) {
    return false;
  }

  private void addWifiDataImage(float x, float y) {
    ImageView datumImage = new ImageView(this);
    datumImage.setImageResource(R.drawable.wifi_blue);
    // datumImage.setImageBitmap(BitmapFactory.decodeResource(getResources(),R.drawable.wifi_blue));
    datumImage.setOnTouchListener(new OnTouchListener(){
      @Override
      public boolean onTouch(View view, MotionEvent event) {
        if(event.getAction() == MotionEvent.ACTION_DOWN) {
          mFloorPlanLayout.removeView(view);
          return true;
        }
        return false;
      }
    });
    
    RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(datumImage.getDrawable().getIntrinsicWidth(),
                                                                      datumImage.getDrawable().getIntrinsicHeight());
    lp.setMargins((int)(x - datumImage.getDrawable().getIntrinsicWidth()/2), 
        (int)(y - datumImage.getDrawable().getIntrinsicHeight()), 0, 0);
    mFloorPlanLayout.addView(datumImage, 1, lp);
    mWifiDataImageList.add(datumImage);
  }
  
  private void clearWifiDataImages() {
    for(ImageView img : mWifiDataImageList) {
      mFloorPlanLayout.removeView(img);
    }
  }
  
  
  class WifiScanReceiver extends BroadcastReceiver {
    private MainActivity mActivity;
    private WifiManager mWifiManager;
    
    private Location mCurrentLoc;
    private boolean isLocalizing = false;
    private boolean isScanning = false; 

    private ImageView mLocImage;

    WifiScanReceiver(MainActivity activity, WifiData data) {
      super();
      mActivity = activity;
      mWifiManager = (WifiManager) activity.getSystemService(Context.WIFI_SERVICE);
      mData = data;

      mLocImage = (ImageView) findViewById(R.id.current_location);
    }

    public void startSave(Location location) {
      if(!isScanning) {
        mCurrentLoc = location;
        isLocalizing = false;
        startScan();
      }
    }

    public void startLocalize() {
      isLocalizing = true;
      startScan();
    }

    private void startScan() {
      mActivity.registerReceiver(this, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
      mWifiManager.startScan();
      isScanning = true;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
      mActivity.unregisterReceiver(this);
      isScanning = false;
      if(isLocalizing) {
        isLocalizing = false;
        localize();
        return;
      } else {
        mData.add(mCurrentLoc, mWifiManager.getScanResults());
        mActivity.addWifiDataImage(mCurrentLoc.x, mCurrentLoc.y);
        return;
      }
    }

    private void localize() {
      mCurrentLoc = mData.localize(mWifiManager.getScanResults());
      if(mCurrentLoc == null) {
        mLocImage.setVisibility(View.GONE);
        return;
      }

      RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) mLocImage.getLayoutParams();

      lp.setMargins((int)(mCurrentLoc.x - mLocImage.getDrawable().getIntrinsicWidth() / 2), 
          (int)(mCurrentLoc.y - mLocImage.getDrawable().getIntrinsicHeight()), 0, 0);

      mLocImage.setLayoutParams(lp);
      mLocImage.setVisibility(View.VISIBLE);
      return;
    }
  }

}
