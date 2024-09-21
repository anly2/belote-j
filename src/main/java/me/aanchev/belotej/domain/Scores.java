package me.aanchev.belotej.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Scores {
    private int us = 0;
    private int them = 0;



    public Scores add(int points, RelPlayer player) {
        return switch (player) {
            case n, s -> addToUs(points);
            case w, e -> addToThem(points);
        };
    }
    public Scores add(int points, Team team) {
        return switch (team) {
            case US -> addToUs(points);
            case THEM -> addToThem(points);
        };
    }
    public Scores addToUs(int points) {
        us += points;
        return this;
    }
    public Scores addToThem(int points) {
        them += points;
        return this;
    }
    public Scores add(int us, int them) {
        this.us += us;
        this.them += them;
        return this;
    }


    public void reset() {
        us = 0;
        them = 0;
    }


    public static Scores scores() {
        return new Scores();
    }
    public static Scores scores(int us, int them) {
        return new Scores(us, them);
    }
}
