package org.devoxx4kids.poketime;

import javafx.scene.image.Image;

class Pokemon extends SpriteView.RandomWalker {

    private String name;
    private final Image front;
    private final Image back;

    Pokemon(String name, Location loc, double speed, boolean avoid, boolean sleepy) {

        super(loadImage("/images/" + name.toLowerCase() + ".png", 4, 4), loc, 4, 4, speed);
        front = new Image("file:/images/" + name + "-front.png");
        back = new Image("file:/images/" + name + "-back.png");
        this.name = Character.toUpperCase(name.charAt(0)) + name.substring(1);
        arrivalHandler =
                e -> {
                    if (Main.pokeTrainer.location.get().equals(location.get())) {
                        Main.battle(this);
                    }
                };
        if (avoid) {this.avoid = Main.pokeTrainer;};
        if (sleepy) {sleepy();};
    }

    public String getName() {

        return name;
    }

    public Image getFront() {

        return front;
    }

    public Image getBack() {

        return back;
    }
}
