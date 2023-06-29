package org.devoxx4kids.poketime;

import com.pi4j.component.gyroscope.analogdevices.ADXL345;
import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.GpioPinDigitalInput;
import com.pi4j.io.gpio.PinPullResistance;
import com.pi4j.io.gpio.RaspiPin;
import com.pi4j.io.gpio.event.GpioPinListenerDigital;
import com.pi4j.io.i2c.I2CBus;
import com.pi4j.io.i2c.I2CDevice;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.beans.property.BooleanProperty;
import javafx.util.Duration;
import joachimeichborn.sensors.driver.Tsl2561;

import java.io.IOException;

import static com.pi4j.io.i2c.I2CFactory.*;

public class SensorFactory {

    private static SensorFactory factory;
    private GpioController gpio;
    private I2CBus bus;
    private float lastGyroX;

    public SensorFactory() throws IOException, UnsupportedBusNumberException {

        if (PiSystem.isPiUnix) {
            gpio = GpioFactory.getInstance();
            bus = getInstance(I2CBus.BUS_1);
        }
    }

    public static SensorFactory create() throws IOException, UnsupportedBusNumberException {

        if (factory == null) {
            factory = new SensorFactory();
        }

        return factory;
    }


    public void createButton() {

        if (PiSystem.isPiUnix) {
            final GpioPinDigitalInput myButton = gpio.provisionDigitalInputPin(RaspiPin.GPIO_07,
                    PinPullResistance.PULL_UP);

            myButton.addListener((GpioPinListenerDigital) event -> {
                boolean knopfGedrueckt = event.getState().isLow();
                if (knopfGedrueckt)
                    Main.displayAndLog("Knopf gedrueckt.");
                // ToDo: Pokemon angreifen!
            });
        }
    }


    public void createLightSensor(BooleanProperty nacht) {

        if (PiSystem.isPiUnix) {
            try {
                I2CDevice device = bus.getDevice(0x39);
                Tsl2561 lightSensor = new Tsl2561(device);
                Timeline lightTimeline = new Timeline(new KeyFrame(Duration.seconds(10),
                        actionEvent -> {
                            try {
                                double lux = lightSensor.getLux();
                                Main.displayAndLog("lux = " + lux);
                                // ToDo: Lass es Nacht werden!

                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }));
                lightTimeline.setCycleCount(Timeline.INDEFINITE);
                lightTimeline.play();
            } catch (IOException e) {
                System.out.println("The light sensor should be connected properly... " + e.getMessage());
            }
        }
    }


    public void createAccelerometer() {

        if (PiSystem.isPiUnix) {
            try {
                ADXL345 gyro = new ADXL345(bus);
                gyro.init(gyro.X, 4);
                lastGyroX = gyro.X.getRawValue();

                Timeline gyroscopeTimeline = new Timeline(new KeyFrame(Duration.seconds(1),
                        actionEvent -> {
                            try {
                                float x = gyro.X.getRawValue();
                                System.out.println("gyro = " + Math.abs(x-lastGyroX));
                                if (!Main.earthquake.getValue()) {
                                    // Wenn der Sensor zu stark ausschlägt, erhöhe diesen Wert
                                    if (Math.abs(x - lastGyroX) > 2000) {
                                        Main.displayAndLog("Erdbeben!");
                                        // ToDo: Lass es beben!

                                    }
                                }
                                lastGyroX = x;
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }));
                gyroscopeTimeline.setCycleCount(Timeline.INDEFINITE);
                gyroscopeTimeline.play();
            } catch (IOException e) {
                System.out.println("The gyroscope should be connected properly... " + e.getMessage());
            }
        }
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
