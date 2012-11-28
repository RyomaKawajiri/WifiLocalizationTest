package ics.wifi;

import java.io.Serializable;

public class ScanResult implements Serializable {
  private static final long serialVersionUID = 6108550788442747674L;
  
  public ScanResult(android.net.wifi.ScanResult result) {
    this.BSSID = result.BSSID;
    this.SSID = result.SSID;
    this.capabilities = result.capabilities;
    this.frequency = result.frequency;
    this.level = result.level;
  }
  
  public String BSSID;
  public String SSID;
  public String capabilities;
  public int frequency;
  public int level;
}
