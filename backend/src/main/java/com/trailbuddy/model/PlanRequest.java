package com.trailbuddy.model;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

public class PlanRequest {
    @Size(max = 120)
    private String place = "a nearby park or trail";

    @Size(max = 30)
    private String activity = "hiking";

    @Min(30)
    @Max(480)
    private Integer duration = 120;

    @Size(max = 30)
    private String level = "beginner";

    @Size(max = 180)
    private String interests = "nature and a screen-free break";

    public String getPlace() { return place; }
    public void setPlace(String place) { this.place = place; }
    public String getActivity() { return activity; }
    public void setActivity(String activity) { this.activity = activity; }
    public Integer getDuration() { return duration; }
    public void setDuration(Integer duration) { this.duration = duration; }
    public String getLevel() { return level; }
    public void setLevel(String level) { this.level = level; }
    public String getInterests() { return interests; }
    public void setInterests(String interests) { this.interests = interests; }
}
