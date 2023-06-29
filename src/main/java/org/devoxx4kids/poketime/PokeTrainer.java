package org.devoxx4kids.poketime;

import javafx.animation.Animation;
import javafx.animation.RotateTransition;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;

public class PokeTrainer extends SpriteView.RandomWalker {

    static final Image playerImage = loadImage("/images/black-girl.png");

    public PokeTrainer(Location loc) {

        super(playerImage, loc);
        arrivalHandler = e -> {
            for (SpriteView s : Main.sprites) {
                if (s instanceof Pokemon) {
                    Pokemon p = (Pokemon) s;
                    if (s.location.get().equals(location.get())) {
                        Main.battle(p);
                    }
                }
            }
        };
    }

    @Override
    public void move(Direction direction) {

        if (walking != null && walking.getStatus().equals(Animation.Status.RUNNING))
            return;

        if (!inBounds(direction))
            return;

        moveTo(location.getValue().offset(direction.getXOffset(), direction.getYOffset()));
    }

    public void gameover() {

        Main.gameover = true;

        RotateTransition rotate = new RotateTransition(Duration.seconds(3), org.devoxx4kids.poketime.PokeTrainer.this);
        rotate.byAngleProperty().set(1080);
        rotate.setOnFinished(actionEvent -> Main.root.getChildren().remove(org.devoxx4kids.poketime.PokeTrainer.this));
        rotate.play();
        Main.sprites.remove(this);
        Main.root.getChildren().add(new Rectangle(Main.BOARD_WIDTH, Main.BOARD_HEIGHT, Color.color(0, 0, 0, .4)));

        Label label = new Label("GAME OVER");
        label.setTextFill(Color.WHITESMOKE);
        label.setAlignment(Pos.BASELINE_CENTER);
        label.setFont(Main.pixelated);
        label.setPrefHeight(Main.BOARD_HEIGHT);
        label.setPrefWidth(Main.BOARD_WIDTH);
        Main.root.getChildren().add(label);
    }
}
