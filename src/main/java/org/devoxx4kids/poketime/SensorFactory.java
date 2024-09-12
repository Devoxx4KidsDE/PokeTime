package org.devoxx4kids.poketime;

import com.pi4j.Pi4J;
import com.pi4j.context.Context;
import com.pi4j.io.gpio.digital.DigitalInput;
import com.pi4j.io.gpio.digital.DigitalState;
import javafx.beans.property.BooleanProperty;

import static com.pi4j.io.gpio.digital.PullResistance.PULL_UP;

public class SensorFactory {

    private static SensorFactory factory;
    Context pi4j = Pi4J.newAutoContext();


    public SensorFactory() {

        if (PiSystem.isPiUnix) {
            Main.displayAndLog("is pi :)");
        }
    }

    public static SensorFactory create(){

        if (factory == null) {
            factory = new SensorFactory();
        }

        return factory;
    }


    public void createButton(Context pi4j) {

        final int PIN_BUTTON = 4; // PIN 7 = BCM 4

        var buttonConfig = DigitalInput.newConfigBuilder(pi4j)
            .id("button")
            .name("Pokeball")
            .address(PIN_BUTTON)
            .pull(PULL_UP)
            .debounce(3000L);

        var button = pi4j.create(buttonConfig);
        System.out.println("button created");
        System.out.println("button config "+buttonConfig.toString());
        System.out.println("button "+button.toString());


        button.addListener(e -> {
            if (e.state() == DigitalState.LOW) {
                System.out.println("pressed");
                Main.displayAndLog("Button pressed");
                Main.angreifen(30);
            }
        });

//        button.addListener(e -> {
//            boolean knopfGedrueckt = e.state().isLow();
//            if (knopfGedrueckt) {
//                Main.displayAndLog("Button pressed");
//                Main.angreifen(30);
//            }
//        });
    }


    public void createLightSensor(BooleanProperty nacht) {
        System.out.println("liiiight");
//            try {
//                I2CDevice device = bus.getDevice(0x39);
//                Tsl2561 lightSensor = new Tsl2561(device);
//                Timeline lightTimeline = new Timeline(new KeyFrame(Duration.seconds(10),
//                        actionEvent -> {
//                            try {
//                                double lux = lightSensor.getLux();
//                                Main.displayAndLog("lux = " + lux);
//                                // ToDo: Lass es Nacht werden!
//
//                            } catch (IOException e) {
//                                e.printStackTrace();
//                            }
//                        }));
//                lightTimeline.setCycleCount(Timeline.INDEFINITE);
//                lightTimeline.play();
//            } catch (IOException e) {
//                System.out.println("The light sensor should be connected properly... " + e.getMessage());
//            }
    }


    public void createAccelerometer() {
        System.out.println("pi goes brrrr");
//            try {
//                ADXL345 gyro = new ADXL345(bus);
//                gyro.init(gyro.X, 4);
//                lastGyroX = gyro.X.getRawValue();
//
//                Timeline gyroscopeTimeline = new Timeline(new KeyFrame(Duration.seconds(1),
//                        actionEvent -> {
//                            try {
//                                float x = gyro.X.getRawValue();
//                                System.out.println("gyro = " + Math.abs(x-lastGyroX));
//                                if (!Main.earthquake.getValue()) {
//                                    // Wenn der Sensor zu stark ausschlägt, erhöhe diesen Wert
//                                    if (Math.abs(x - lastGyroX) > 2000) {
//                                        Main.displayAndLog("Erdbeben!");
//                                        // ToDo: Lass es beben!
//
//                                    }
//                                }
//                                lastGyroX = x;
//                            } catch (IOException e) {
//                                e.printStackTrace();
//                            }
//                        }));
//                gyroscopeTimeline.setCycleCount(Timeline.INDEFINITE);
//                gyroscopeTimeline.play();
//            } catch (IOException e) {
//                System.out.println("The gyroscope should be connected properly... " + e.getMessage());
//            }
    }
}

// Pokemon angreifen!
//                Main.angreifen(3);

// Lass es Nacht werden!
//                    if (lux < 3) {
//                        nacht.setValue(true);
//                    } else {
//                        nacht.setValue(false);
//                    }

// Lass es beben!
//                            Main.erdbeben();
