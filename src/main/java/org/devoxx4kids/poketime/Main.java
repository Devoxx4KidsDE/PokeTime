package org.devoxx4kids.poketime;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.ScaleTransition;
import javafx.animation.SequentialTransition;
import javafx.animation.Timeline;
import javafx.animation.TranslateTransition;

import javafx.application.Application;
import javafx.application.Platform;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;

import javafx.fxml.FXMLLoader;

import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Parent;
import javafx.scene.Scene;

import javafx.scene.control.Label;

import javafx.scene.effect.ColorAdjust;

import javafx.scene.image.ImageView;

import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;

import javafx.scene.layout.StackPane;

import javafx.scene.paint.Color;

import javafx.scene.shape.Rectangle;
import javafx.scene.shape.StrokeType;

import javafx.scene.text.Font;

import javafx.stage.Stage;

import javafx.util.Duration;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.IntStream;


public class Main extends Application {

    static final int SCALE = 2;
    static final int SPRITE_SIZE = 32;
    static final int CELL_SIZE = SPRITE_SIZE * SCALE;
    static final int HORIZONTAL_CELLS = 12;
    static final int VERTICAL_CELLS = 7;
    static final int BOARD_WIDTH = HORIZONTAL_CELLS * CELL_SIZE;
    static final int BOARD_HEIGHT = VERTICAL_CELLS * CELL_SIZE;
    public static boolean gameover = false;
    public static List<SpriteView> sprites = new ArrayList<>();
    public static PixelatedClock pixelatedClock;
    public static Group root;
    public static Font pixelated = Font.loadFont(Main.class.getResourceAsStream("/fonts/pixelated.ttf"),
            Main.CELL_SIZE);
    public static BooleanProperty nacht = new SimpleBooleanProperty(false);
    public static Group spriteGroup = new Group();
    public static PokeTrainer pokeTrainer;
    public static BooleanProperty earthquake = new SimpleBooleanProperty(false);
    private static Label messageDisplay;
    private static Label pokemonCounter;
    private static int pokemonCaught = 0;
    private static Timeline clearMessageDisplay = new Timeline(new KeyFrame(Duration.seconds(5)));
    private static Parent battleScene;
    private static BattleSceneController battleSceneController;

    private static Timeline battle = new Timeline(new KeyFrame(Duration.seconds(5),
                actionEvent -> {
                    int health = Integer.parseInt(battleSceneController.playerHealth.getText());
                    health = Math.max(health - 5, 0);

                    if (health == 0) {
                        endBattle("Pokemontrainer ist geflohen!");
                        pokeTrainer.gameover();
                    }

                    battleSceneController.playerHealth.setText(String.valueOf(health));

                    ScaleTransition grow = new ScaleTransition(Duration.seconds(.5));
                    grow.setToX(1.4);
                    grow.setToY(1.4);

                    ScaleTransition shrink = new ScaleTransition(Duration.seconds(.5));
                    shrink.setToX(1);
                    shrink.setToY(1);

                    SequentialTransition attackTransition = new SequentialTransition(
                            battleSceneController.pokemonPicture, grow, shrink);
                    ColorAdjust colorAdjust = new ColorAdjust();
                    battleSceneController.trainerPokemon.setEffect(colorAdjust);

                    Timeline colorChange = new Timeline(
                            new KeyFrame(Duration.seconds(.1), new KeyValue(colorAdjust.hueProperty(), 1)),
                            new KeyFrame(Duration.seconds(.2), new KeyValue(colorAdjust.hueProperty(), 0)));
                    colorChange.setCycleCount(5);
                    attackTransition.setOnFinished((val) -> colorChange.play());
                    attackTransition.play();
                }));
    private static boolean inBattle = false;
    private static Pokemon enemy;

    private ImageView background;

    @Override
    public void start(Stage primaryStage) throws Exception {

        primaryStage.setTitle("PokeTime");

        StackPane wrapper = new StackPane();
        root = new Group();
        wrapper.getChildren().add(root);

        Scene scene = new Scene(wrapper, BOARD_WIDTH, BOARD_HEIGHT, Color.BLACK);
        primaryStage.setScene(scene);
        populateBackground(root);
        pixelatedClock = new PixelatedClock();
        messageDisplay = new Label();
        pokemonCounter = new Label();

        pokeTrainer = new PokeTrainer(new Location(0, 3));

        // ToDo: Neue Pokemon hinzufügen, Namen müssen Dateinamen in resources/images entsprechen
        spriteGroup.getChildren().add(new Pokemon("Rattfratz", new Location(8, 2), 3, true, false));
        spriteGroup.getChildren().add(new Pokemon("Griffel", new Location(9, 4), 2, true, true));
        spriteGroup.getChildren().add(new Pokemon("Bidiza", new Location(7, 6), .5, true, true));
        spriteGroup.getChildren().add(new Pokemon("Krebscorps", new Location(5, 4), 1.2, true, false));
        spriteGroup.getChildren().add(new Pokemon("Larvitar", new Location(6, 5), 1.2, true, true));
        spriteGroup.getChildren().add(new Pokemon("Mampfaxo", new Location(3, 5), .5, false, true));

        populateCells(root, pokeTrainer);
        root.getChildren().add(spriteGroup);
        spriteGroup.getChildren().add(pokeTrainer);
        addKeyHandler(scene, pokeTrainer);
        pokeTrainer.idle = true;

        ColorAdjust colorAdjustment = new ColorAdjust();
        background.setEffect(colorAdjustment);
        pixelatedClock.isNight.addListener((o, ov, nv) -> {
            new Timeline(
                new KeyFrame(Duration.seconds(2), new KeyValue(colorAdjustment.brightnessProperty(), nv ? -0.6 : 0)))
                .play();
        });
        root.getChildren().add(messageDisplay);
        messageDisplay.setFont(pixelated);
        messageDisplay.setLayoutX(CELL_SIZE / 4);
        messageDisplay.setLayoutY(BOARD_HEIGHT - CELL_SIZE * 1.7);
        messageDisplay.setTextFill(Color.WHITESMOKE);
        clearMessageDisplay.setOnFinished((ae) -> messageDisplay.setText(""));

        root.getChildren().add(pokemonCounter);
        pokemonCounter.setFont(pixelated);
        pokemonCounter.setLayoutX((double) CELL_SIZE / 4);
        pokemonCounter.setLayoutY((double) CELL_SIZE / 4);
        pokemonCounter.setTextFill(Color.DARKGREEN);

        FXMLLoader fxmlLoader = new FXMLLoader(Main.class.getResource("/BattleScene.fxml"));
        battleScene = fxmlLoader.load();
        battleSceneController = fxmlLoader.getController();
        wrapper.getChildren().add(battleScene);
        battleScene.setVisible(false);

        SensorFactory sensorFactory = SensorFactory.create();
        sensorFactory.createButton();
        Main.pixelatedClock.isNight.bind(nacht);
        sensorFactory.createLightSensor(nacht);
        sensorFactory.createAccelerometer();

        primaryStage.show();
    }


    private void populateBackground(Group root) {

        background = new ImageView(Objects.requireNonNull(getClass().getResource("/images/backgrounds/forest.png")).toString());
        background.setFitHeight(BOARD_HEIGHT);
        background.setFitWidth(BOARD_WIDTH);
        root.getChildren().add(background);
    }


    private void populateCells(Group root, final SpriteView mainCharacter) {

        // Gratuitous use of lambdas to do nested iteration!
        Group cells = new Group();
        IntStream.range(0, HORIZONTAL_CELLS).mapToObj(i ->
                    IntStream.range(0, VERTICAL_CELLS).mapToObj(j -> {
                        Rectangle rect = new Rectangle(i * CELL_SIZE, j * CELL_SIZE, CELL_SIZE, CELL_SIZE);
                        rect.setFill(Color.rgb(0, 0, 0, 0));
                        rect.setStrokeType(StrokeType.INSIDE);
                        rect.setStroke(Color.BLACK);
                        rect.setOnMousePressed(e ->
                                mainCharacter.move(mainCharacter.location.get().directionTo(new Location(i, j))));

                        return rect;
                    })).flatMap(s -> s).forEach(cells.getChildren()::add);
        root.getChildren().add(cells);
    }


    private void addKeyHandler(Scene scene, SpriteView player) {

        scene.addEventHandler(KeyEvent.KEY_PRESSED,
            key -> {
                KeyCode keyCode = key.getCode();

                switch (keyCode) {
                    case W:
                    case UP:
                        player.move(Direction.UP);
                        break;

                    case A:
                    case LEFT:
                        player.move(Direction.LEFT);
                        break;

                    case S:
                    case DOWN:
                        player.move(Direction.DOWN);
                        break;

                    case D:
                    case RIGHT:
                        player.move(Direction.RIGHT);
                        break;

                    case J:
                        if (key.isControlDown() && key.isShiftDown())
                            angreifen(3);
                        break;

                    case K:
                        if (key.isControlDown() && key.isShiftDown())
                            nacht.setValue(!nacht.getValue());
                        break;

                    case L:
                        if (key.isControlDown() && key.isShiftDown())
                            erdbeben();
                        break;

                    case ESCAPE:
                        Platform.exit();
                }
            });
    }


    public static void main(String[] args) {

        launch(args);
    }


    public static void angreifen(int damage) {

        Platform.runLater(() -> attackImpl(damage));
    }


    public static void attackImpl(int damage) {

        if (!inBattle || gameover)
            return;

        TranslateTransition right = new TranslateTransition(Duration.seconds(.2),
                battleSceneController.trainerPokemon);
        right.setToX(10);

        TranslateTransition left = new TranslateTransition(Duration.seconds(.2), battleSceneController.trainerPokemon);
        left.setToX(0);

        SequentialTransition attackTransition = new SequentialTransition(right, left);
        ColorAdjust colorAdjust = new ColorAdjust();
        battleSceneController.pokemonPicture.setEffect(colorAdjust);

        Timeline colorChange = new Timeline(new KeyFrame(Duration.seconds(.1),
                    new KeyValue(colorAdjust.hueProperty(), 1)),
                new KeyFrame(Duration.seconds(.2), new KeyValue(colorAdjust.hueProperty(), 0)));
        colorChange.setCycleCount(5);
        attackTransition.setOnFinished((val) -> colorChange.play());
        attackTransition.play();

        int health = Integer.parseInt(battleSceneController.enemyHealth.getText());
        health = Math.max(health - damage, 0);

        if (health == 0) {
            endBattle("Pokemon gefangen!");
            spriteGroup.getChildren().remove(enemy);
            enemy.stop();
            enemy.arrivalHandler = null;
            sprites.remove(enemy);
            pokemonCounter.setText("Pokemon gefangen: " + ++pokemonCaught);

            if (sprites.size() == 1) {
                win();
            }
            // capture the pokemon
        }

        battleSceneController.enemyHealth.setText(String.valueOf(health));
    }

        public static void win() {

            Main.root.getChildren().add(new Rectangle(Main.BOARD_WIDTH, Main.BOARD_HEIGHT, Color.color(0, 0, 0, .4)));

            Label label = new Label("DU HAST GEWONNEN!!!");
            label.setTextFill(Color.LIGHTGREEN);
            label.setAlignment(Pos.BASELINE_CENTER);
            label.setFont(Main.pixelated);
            label.setPrefHeight(Main.BOARD_HEIGHT);
            label.setPrefWidth(Main.BOARD_WIDTH);
            Main.root.getChildren().add(label);
        }


    public static void battle(Pokemon pokemon) {

        if (inBattle || gameover)
            return;

        enemy = pokemon;
        System.out.println("Ein wildes " + pokemon.getName() + " ist erschienen.");
        battleSceneController.playerHealth.setText("35");
        battleSceneController.enemyHealth.setText("35");
        battleSceneController.pokemonName.setText(pokemon.getName());
        battleSceneController.pokemonPicture.setImage(pokemon.getFront());
        battleScene.setVisible(true);
        inBattle = true;
        battle.setCycleCount(Animation.INDEFINITE);
        battle.play();
    }


    public static void endBattle(String message) {

        System.out.println(message);
        battleScene.setVisible(false);
        inBattle = false;
        battle.stop();
    }


    public static void erdbeben() {

        earthquake.setValue(true);

        Timeline quakeTimeline = new Timeline();

        for (int i = 1; i < 100; i++) {
            quakeTimeline.getKeyFrames()
                .add(new KeyFrame(Duration.seconds(.1 * i),
                        new KeyValue(spriteGroup.translateXProperty(), i % 2 == 0 ? -10 : 10)));
        }

        quakeTimeline.getKeyFrames()
            .add(new KeyFrame(Duration.seconds(10), new KeyValue(spriteGroup.translateXProperty(), 0)));
        quakeTimeline.setOnFinished((o) -> earthquake.setValue(false));
        quakeTimeline.play();
    }


    public static void displayAndLog(String message) {

        System.out.println(message);
        Platform.runLater(() -> {
            messageDisplay.setText(message);
            clearMessageDisplay.playFromStart();
        });
    }

}
