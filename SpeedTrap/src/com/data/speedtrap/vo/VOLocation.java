package com.data.speedtrap.vo;

import android.location.Location;

public class VOLocation {
  private Location location;
  private String camType;
  private String direction;
  private String passedTheLocation;
  
  public Location getLocation() {
    return location;
  }
  public void setLocation(Location location) {
    this.location = location;
  }  
  public String getCamType() {
    return camType;
  }
  public void setCamType(String camType) {
    this.camType = camType;
  }  
  public String getPassedTheLocation() {
    return passedTheLocation;
  }
  public void setPassedTheLocation(String passedTheLocation) {
    this.passedTheLocation = passedTheLocation;
  }  
  public String getDirection() {
    return direction;
  }
  public void setDirection(String direction) {
    this.direction = direction;
  }  
}
