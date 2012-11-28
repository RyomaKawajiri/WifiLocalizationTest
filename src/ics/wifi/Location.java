package ics.wifi;

import java.io.Serializable;

public class Location implements Serializable {
  private static final long serialVersionUID = 7217191987117737904L;
  public int floor;
  public float x;
  public float y;
  public Location(int _floor, float _x, float _y){
    floor = _floor;
    x = _x;
    y = _y;
  };
}