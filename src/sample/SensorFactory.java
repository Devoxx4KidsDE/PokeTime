package sample;

import com.pi4j.component.gyroscope.analogdevices.ADXL345;
import com.pi4j.io.gpio.*;
import com.pi4j.io.gpio.event.GpioPinDigitalStateChangeEvent;
import com.pi4j.io.gpio.event.GpioPinListenerDigital;
import com.pi4j.io.i2c.I2CBus;
import com.pi4j.io.i2c.I2CDevice;
import com.pi4j.io.i2c.I2CFactory;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.beans.property.BooleanProperty;
import javafx.util.Duration;
import joachimeichborn.sensors.driver.Tsl2561;

import java.io.IOException;

/**
 * Created by Cassandra on 9/9/2016.
 */
public class SensorFactory {
    private static SensorFactory factory;
    private GpioController gpio;
    private I2CBus bus;
    private float letzterGyroX;

    public static SensorFactory create() throws IOException, I2CFactory.UnsupportedBusNumberException {
        if (factory == null) {
            factory = new SensorFactory();
        }
        return factory;
    }

    public SensorFactory() throws IOException, I2CFactory.UnsupportedBusNumberException {
        if (PiSystem.isPiUnix) {
            gpio = GpioFactory.getInstance();
            bus = I2CFactory.getInstance(I2CBus.BUS_1);
        }
    }

    public void createButton(SpriteView.PokeTrainer pokeTrainer) {
        if (PiSystem.isPiUnix) {
            final GpioPinDigitalInput myButton = gpio.provisionDigitalInputPin(RaspiPin.GPIO_07, PinPullResistance.PULL_UP);
            myButton.addListener(new GpioPinListenerDigital() {
                @Override
                public void handleGpioPinDigitalStateChangeEvent(GpioPinDigitalStateChangeEvent event) {
                    boolean knopfGedrueckt = event.getState().isLow();
                    if (knopfGedrueckt) Main.display("Knopf gedrückt.");
                    // Pokemon angreifen
                }
            });
        }
    }

    public void createLightSensor(BooleanProperty nacht) throws IOException {
        if (PiSystem.isPiUnix) {
            I2CDevice device = bus.getDevice(0x39);
            try {
                Tsl2561 lichtSensor = new Tsl2561(device);
                Timeline lichtZeitlinie = new Timeline(new KeyFrame(Duration.seconds(10), actionEvent -> {
                    try {
                        double lux lichtSensor.getLux();
                        Main.display("lux = " + lux);
                        // Es werde Nacht!
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }));
                lichtZeitlinie.setCycleCount(Timeline.INDEFINITE);
                lichtZeitlinie.play();
            } catch (IOException e) {
                System.out.println("Der Lichtsensor sollte ordentlich angeschlossen sein... " + e.getMessage());
            }
        }
    }

    public void createAccelerometer() {
        if (PiSystem.isPiUnix) {
            try {
                ADXL345 gyro = new ADXL345(bus);
                gyro.init(gyro.X, 4);
                letzterGyroX = gyro.X.getRawValue();
                Timeline geschwindigkeitsMesserZeitlinie = new Timeline(new KeyFrame(Duration.seconds(1), actionEvent -> {
                    try {
                        float x = gyro.X.getRawValue();
                        if (!Main.earthquake.getValue()) {
                            if (Math.abs(x - letzterGyroX) > 2000) {
                                Main.display("Erbeben!");
                                // Make an erdbeben!
                            }
                        }
                        letzterGyroX = x;
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }));
                geschwindigkeitsMesserZeitlinie.setCycleCount(Timeline.INDEFINITE);
                geschwindigkeitsMesserZeitlinie.play();
            } catch (IOException e) {
                System.out.println("Geschwindigkeitssensor sollte ordentlich angeschlossen sein... " + e.getMessage());
            }
        }
    }
}
// Pokemon angreifen!
//                Main.angriff(3);

// Es werde Nacht!
//                    if (lux < 3) {
//                        nacht.setValue(true);
//                    } else {
//                        nacht.setValue(false);
//                    }

// Mach ein Erdbeben!
//                            Main.erdbeben();
