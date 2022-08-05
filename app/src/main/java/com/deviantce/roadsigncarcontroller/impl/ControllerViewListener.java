package com.deviantce.roadsigncarcontroller.impl;

public interface ControllerViewListener {
    public void onSignboardBrighnessClicked(String brightness);
    public void onSignboardSpeedClicked(String speed);
    public void onSignboardTypeClicked(String type);

    public void onSirenVolumeButtonClicked(int volume);
    public void onBulletinBrighnessClicked(int brighness);

}
