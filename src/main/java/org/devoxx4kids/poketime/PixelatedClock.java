package org.devoxx4kids.poketime;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.scene.control.Label;
import javafx.util.Duration;

import java.time.Clock;
import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.time.temporal.ChronoField;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjuster;

public class PixelatedClock extends Label {
    private DateTimeFormatter clockFormat = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT);
    public BooleanProperty isNight = new SimpleBooleanProperty(false);
    public long multiplier = 1;

    public Clock getClock() {
        return Clock.systemDefaultZone();
    }

    public PixelatedClock() {
        setFont(Main.pixelated);
        setLayoutX((double) Main.CELL_SIZE / 4);
        Timeline clockTimeline = new Timeline(new KeyFrame(Duration.millis(1), actionEvent -> {
            LocalDateTime date = LocalDateTime.now(getClock());
            setText(date.format(clockFormat));
        }));
        clockTimeline.setCycleCount(Timeline.INDEFINITE);
        clockTimeline.play();
    }
}
