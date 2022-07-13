package com.deviantce.roadsigncarcontroller.impl;

public interface ControllerViewListener {
    public void onSirenVolumeButtonClicked(int volume);
    public void onBulletinBrighnessClicked(int brighness);

    public void onSignboardBrighnessClicked(int brightness);
    public void onSignboardSpeedClicked(int speed);
    public void onSignboardTypeClicked(int type);
}
