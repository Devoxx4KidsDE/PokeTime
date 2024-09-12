//package joachimeichborn.sensors.driver;
//
//import java.io.IOException;
//import java.nio.ByteBuffer;
//import java.nio.ByteOrder;
//
//
//import com.pi4j.io.i2c.I2CDevice;
//
///**
// * @author Joachim von Eichborn (Java implementation inspired by a C++ implementation from Adafruit Industries)
// *
// *         <p>
// *         <b>LICENSE</b>
// *         </p>
// *         <p>
// *         Software License Agreement (BSD License)
// *         </p>
// *         <p>
// *         Copyright (c) 2015, Joachim von Eichborn
// *         </p>
// *         <p>
// *         Redistribution and use in source and binary forms, with or without modification, are permitted provided that the
// *         following conditions are met: 1. Redistributions of source code must retain the above copyright notice, this list
// *         of conditions and the following disclaimer. 2. Redistributions in binary form must reproduce the above copyright
// *         notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided
// *         with the distribution. 3. Neither the name of the copyright holders nor the names of its contributors may be used
// *         to endorse or promote products derived from this software without specific prior written permission.
// *         </p>
// *         <p>
// *         THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS ''AS IS'' AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT
// *         NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN
// *         NO EVENT SHALL THE COPYRIGHT HOLDER BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
// *         CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
// *         DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
// *         STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE,
// *         EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
// *         <p>
// *         <p>
// *         <b>License specifications from the Adafruit C++ implementation:</b>
// *         </p>
// *         <p>
// *         Author: K. Townsend (Adafruit Industries)
// *         </p>
// *         <p>
// *         LICENSE
// *         </p>
// *         <p>
// *         Software License Agreement (BSD License)
// *         </p>
// *         <p>
// *         Copyright (c) 2013, Adafruit Industries All rights reserved.
// *         </p>
// *         <p>
// *         Redistribution and use in source and binary forms, with or without modification, are permitted provided that the
// *         following conditions are met: 1. Redistributions of source code must retain the above copyright notice, this list
// *         of conditions and the following disclaimer. 2. Redistributions in binary form must reproduce the above copyright
// *         notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided
// *         with the distribution. 3. Neither the name of the copyright holders nor the names of its contributors may be used
// *         to endorse or promote products derived from this software without specific prior written permission.
// *         </p>
// *         <p>
// *         THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS ''AS IS'' AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT
// *         NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN
// *         NO EVENT SHALL THE COPYRIGHT HOLDER BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
// *         CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
// *         DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
// *         STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE,
// *         EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
// *         </p>
// */
//public class Tsl2561 {
//    /**
//     * Helper class to store the measured raw brightness values and calculate SI lux from them
//     */
//    static class Luminosity {
//        private final static int LUX_SCALE = 14; // Scale by 2^14
//        private final static int RATIO_SCALE = 9; // Scale ratio by 2^9
//        private final static int CHSCALE = 10; // Scale channel values by 2^10
//        private final static int INITIAL_SCALE_13MS = 0x7517; // 322/11 *2^CHSCALE
//        private final static int INITIAL_SCALE_101MS = 0x0FE7; // 322/81 *2^CHSCALE
//        private final static int INITIAL_SCALE_402MS = (1 << CHSCALE);
//
//        /**
//         * To calculate lux values from the measured broadband and infrared values, one has to do some computation. They rely
//         * on several factors that were obtained experimentally. Which factors has to be used depend on the ratio of the
//         * measured broadband and infrared values
//         * <p>
//         * <b>The factors used here apply only to the CL, FN and T package. If you use the CS package, please look into the
//         * TSL2561 documentation for the factors you have to use</b>
//         * </p>
//         */
//        private enum Factors {
//            LEVEL_1(0x0040, 0x01f2, 0x01be), // 0.125 * 2^RATIO_SCALE, 0.0304 * 2^LUX_SCALE, 0.0272 * 2^LUX_SCALE
//            LEVEL_2(0x0080, 0x0214, 0x02d1), // 0.250 * 2^RATIO_SCALE, 0.0325 * 2^LUX_SCALE, 0.0440 * 2^LUX_SCALE
//            LEVEL_3(0x00c0, 0x023f, 0x037b), // 0.375 * 2^RATIO_SCALE, 0.0351 * 2^LUX_SCALE, 0.0544 * 2^LUX_SCALE
//            LEVEL_4(0x0100, 0x0270, 0x03fe), // 0.50 * 2^RATIO_SCALE, 0.0381 * 2^LUX_SCALE, 0.0624 * 2^LUX_SCALE
//            LEVEL_5(0x0138, 0x016f, 0x01fc), // 0.61 * 2^RATIO_SCALE, 0.0224 * 2^LUX_SCALE, 0.0310 * 2^LUX_SCALE
//            LEVEL_6(0x019a, 0x00d2, 0x00fb), // 0.80 * 2^RATIO_SCALE, 0.0128 * 2^LUX_SCALE, 0.0153 * 2^LUX_SCALE
//            LEVEL_7(0x029a, 0x0018, 0x0012); // 1.3 * 2^RATIO_SCALE, 0.00146 * 2^LUX_SCALE, 0.00112 * 2^LUX_SCALE
//
//            /**
//             * Threshold of the ratio between broadband value and infrared value up to which the level is used
//             */
//            private final int mRatioThreshold;
//            /**
//             * ratio-dependent factor for the broadband value
//             */
//            private final int mBroadbandFactor;
//            /**
//             * ration-dependent factor for the infrared value
//             */
//            private final int mInfraredFactor;
//
//            Factors(final int aRatioThreshold, final int aBroadbandFactor, final int aInfraredFactor) {
//                mRatioThreshold = aRatioThreshold;
//                mBroadbandFactor = aBroadbandFactor;
//                mInfraredFactor = aInfraredFactor;
//            }
//
//            public int getBroadbandFactor() {
//                return mBroadbandFactor;
//            }
//
//            public int getInfraredFactor() {
//                return mInfraredFactor;
//            }
//
//            /**
//             * Return the correct factors for the given ratio
//             *
//             * @param aRatio
//             *            the ratio between the measured broadband and infrared values
//             * @return the entry matching the specified ratio
//             */
//            public static Factors getFactorsByRatio(final long aRatio) {
//                for (final Factors factors : values()) {
//                    if (aRatio < factors.mRatioThreshold) {
//                        return factors;
//                    }
//                }
//
//                return null;
//            }
//        }
//
//        final int mBroadband;
//        final int mInfrared;
//        final IntegrationTime mIntegrationTime;
//        final Gain mGain;
//
//        /**
//         * Constructor, the measured raw brightness values are given as byte arrays in little endian order
//         *
//         * @param aBroadband
//         * @param aInfrared
//         * @param mGain
//         * @param mIntegrationTime
//         */
//        public Luminosity(final byte[] aBroadband, final byte[] aInfrared, final IntegrationTime aIntegrationTime,
//                          final Gain aGain) {
//            final ByteBuffer broadbandBuffer = ByteBuffer.wrap(aBroadband);
//            broadbandBuffer.order(ByteOrder.LITTLE_ENDIAN);
//            mBroadband = broadbandBuffer.getShort() & 0xFFFF;
//
//            final ByteBuffer infraredBuffer = ByteBuffer.wrap(aInfrared);
//            infraredBuffer.order(ByteOrder.LITTLE_ENDIAN);
//            mInfrared = infraredBuffer.getShort() & 0xFFFF;
//
//            mIntegrationTime = aIntegrationTime;
//            mGain = aGain;
//        }
//
//        public String toString() {
//            return "Luminosity broadband=" + mBroadband + ", infrared=" + mInfrared + ", gain=" + mGain + ", time="
//                    + mIntegrationTime;
//        }
//
//        public int getBroadband() {
//            return mBroadband;
//        }
//
//        public int getInfrared() {
//            return mInfrared;
//        }
//
//        /**
//         * Converts the raw sensor values to the standard SI lux equivalent.
//         *
//         * @return {@link Long#MAX_VALUE} if the sensor is saturated such that the values are unreliable, 0 if at least one
//         *         of the measured raw values is zero and the calculated lux value otherwise
//         */
//        public double calculateLux() {
//            long channelScale;
//
//            if (getBroadband() > mIntegrationTime.getClipping() || getInfrared() > mIntegrationTime.getClipping()) {
//                return Long.MAX_VALUE;
//            }
//            else if (getBroadband() == 0 || getInfrared() == 0) {
//                return 0;
//            }
//
//            // Initialize scale depending on the integration time
//            switch (mIntegrationTime) {
//                case MS_13:
//                    channelScale = INITIAL_SCALE_13MS;
//                    break;
//                case MS_101:
//                    channelScale = INITIAL_SCALE_101MS;
//                    break;
//                case MS_402:
//                    channelScale = INITIAL_SCALE_402MS;
//                    break;
//                default:
//                    throw new IllegalArgumentException("Integration time " + mIntegrationTime + " cannot be handled");
//            }
//
//            // Modify scale based on the gain
//            if (mGain == Gain.X1) {
//                channelScale = channelScale << 4;
//            }
//
//            final long scaledBroadband = (getBroadband() * channelScale) >> CHSCALE;
//            final long scaledInfrared = (getInfrared() * channelScale) >> CHSCALE;
//
//            final long ratio;
//            if (scaledBroadband == 0) {
//                ratio = 0;
//            }
//            else {
//                // round during bit shifting
//                ratio = (((scaledInfrared << (RATIO_SCALE + 1)) / scaledBroadband) + 1) >> 1;
//            }
//
//            long temp = 0;
//
//            if (ratio > 0) {
//                final Factors factors = Factors.getFactorsByRatio(ratio);
//                if (factors != null) {
//                    temp = ((scaledBroadband * factors.getBroadbandFactor()) - (scaledInfrared * factors.getInfraredFactor()));
//                }
//            }
//
//            final double lux;
//            if (temp >= 0) {
//                lux = (temp >> (LUX_SCALE - 1)) / 10.0;
//            }
//            else {
//                lux = 0;
//            }
//
//            return lux;
//        }
//    }
//
//    /**
//     * Codes to switch the sensor on or off
//     */
//    enum Power {
//        ON(0x03), //
//        OFF(0x00);
//
//        private int mFieldValue;
//
//        Power(final int aFieldValue) {
//            mFieldValue = aFieldValue;
//        }
//
//        public int getFieldValue() {
//            return mFieldValue;
//        }
//    }
//
//    /**
//     * Registers of the light sensor that are used
//     */
//    enum Register {
//        CONTROL(0x00), //
//        TIMING(0x01), //
//        BROADBAND(0x0C), //
//        INFRARED(0x0E);
//
//        private int mRegister;
//
//        Register(final int aRegister) {
//            mRegister = aRegister;
//        }
//
//        public int getRegister() {
//            return mRegister;
//        }
//    }
//
//    /**
//     * Integration time options that are supported by the sensor
//     */
//    enum IntegrationTime {
//        MS_13(0x00, 15, 100, 4850, 4900), //
//        MS_101(0x01, 120, 200, 36000, 37000), //
//        MS_402(0x02, 450, 500, 63000, 65000);
//
//        /**
//         * Value to write to the timing register to set the integration time
//         */
//        private int mFieldValue;
//        /**
//         * Integration time in milliseconds
//         */
//        private int mTime;
//        /**
//         * Lower threshold used in auto gain estimation
//         */
//        private int mLowThreshold;
//        /**
//         * Upper threshold used in auto gain estimation
//         */
//        private int mHighThreshold;
//        /**
//         * Value that indicates that the sensor is saturated such that we can not trust the values anymore
//         */
//        private int mClipping;
//
//        IntegrationTime(final int aFieldValue, final int aTime, final int aLowThreshold, final int aHighThreshold,
//                        final int aClipping) {
//            mFieldValue = aFieldValue;
//            mTime = aTime;
//            mLowThreshold = aLowThreshold;
//            mHighThreshold = aHighThreshold;
//            mClipping = aClipping;
//        }
//
//        public int getFieldValue() {
//            return mFieldValue;
//        }
//
//        public int getTime() {
//            return mTime;
//        }
//
//        public int getLowThreshold() {
//            return mLowThreshold;
//        }
//
//        public int getHighThreshold() {
//            return mHighThreshold;
//        }
//
//        public int getClipping() {
//            return mClipping;
//        }
//    }
//
//    /**
//     * Enum holding the gain options that are supported by the sensor
//     */
//    enum Gain {
//        X1(0x00), //
//        X16(0x10); //
//
//        private int mFieldValue;
//
//        Gain(final int aFieldValue) {
//            mFieldValue = aFieldValue;
//        }
//
//        public int getFieldValue() {
//            return mFieldValue;
//        }
//    }
//
//    /**
//     * Bit indicating command mode to the sensor
//     */
//    private final int COMMAND_BIT = 0x80;
//
//    /**
//     * Bit indicating that a word should be read/written instead of a byte
//     */
//    private final int WORD_BIT = 0x20;
//
//    /**
//     * The light sensor
//     */
//    private final I2CDevice mSensor;
//
//    /**
//     * If this is set, gain is adjusted automatically to improve the measurement
//     */
//    private boolean mAutoGain;
//
//    private IntegrationTime mIntegrationTime;
//    private Gain mGain;
//
//    /**
//     * Writes an 8 bit value to the specified register
//     *
//     * @throws IOException
//     */
//    private void writeByte(final int aReg, final int aValue) throws IOException {
//        mSensor.write((byte) aReg, (byte) aValue);
//    }
//
//    /**
//     * Reads a 16 bit value from the specified register
//     *
//     * @throws IOException
//     */
//    private byte[] readWord(final int aReg) throws IOException {
//        final byte[] result = new byte[2];
//        mSensor.read((byte) aReg, result, 0, 2);
//
//
//        return result;
//    }
//
//    /**
//     * Turn the light sensor on or off
//     *
//     * @throws IOException
//     */
//    private void setPower(final Power aPower) throws IOException {
//        writeByte(COMMAND_BIT | Register.CONTROL.getRegister(), aPower.getFieldValue());
//    }
//
//    /**
//     * @throws IOException
//     */
//    private Luminosity getData() throws IOException {
//        setPower(Power.ON);
//
//        try {
//            // wait for measurement
//            Thread.sleep(mIntegrationTime.getTime());
//        }
//        catch (InterruptedException e) {
//            System.err.println("Waiting for integration was interrupted");
//        }
//
//        final byte[] broadband = readWord(COMMAND_BIT | WORD_BIT | Register.BROADBAND.getRegister());
//        final byte[] infrared = readWord(COMMAND_BIT | WORD_BIT | Register.INFRARED.getRegister());
//
//        final Luminosity luminosity = new Luminosity(broadband, infrared, mIntegrationTime, mGain);
//
//        setPower(Power.OFF);
//
//        return luminosity;
//    }
//
//    /**
//     * Constructor, configures the sensor
//     *
//     * @throws IOException
//     */
//    public Tsl2561(final I2CDevice aSensorDevice) throws IOException {
//        mSensor = aSensorDevice;
//
//        mAutoGain = true;
//        mIntegrationTime = IntegrationTime.MS_402;
//        mGain = Gain.X16;
//
//        setIntegrationTime(mIntegrationTime);
//        setGain(mGain);
//
//        setPower(Power.OFF);
//    }
//
//    /**
//     * Enables or disables trying to automatically improve results by adjusting the gain setting
//     */
//    public void setAutoGain(final boolean aAutoGain) {
//        mAutoGain = aAutoGain;
//    }
//
//    public void setIntegrationTime(final IntegrationTime aIntegrationTime) {
//        try {
//            writeByte(COMMAND_BIT | Register.TIMING.getRegister(), aIntegrationTime.getFieldValue() | mGain.getFieldValue());
//
//            mIntegrationTime = aIntegrationTime;
//        }
//        catch (IOException e) {
//            System.err.println("Could not set integration time to " + aIntegrationTime + ": " + e.getMessage());
//        }
//    }
//
//    public void setGain(final Gain aGain) {
//        try {
//            writeByte(COMMAND_BIT | Register.TIMING.getRegister(), mIntegrationTime.getFieldValue() | aGain.getFieldValue());
//
//            mGain = aGain;
//        }
//        catch (IOException e) {
//            System.err.println("Could not set gain to " + aGain + ": " + e.getMessage());
//        }
//    }
//
//    /**
//     * @throws IOException
//     */
//    private Luminosity getLuminosity() throws IOException {
//        Luminosity luminosity = getData();
//
//        if (mAutoGain) {
//            System.out.println("Auto gain is active");
//            if ((luminosity.getBroadband() < mIntegrationTime.getLowThreshold()) && (mGain == Gain.X1)) {
//                System.out.println("broadband value too low, increasing gain");
//                setGain(Gain.X16);
//                luminosity = getData();
//            }
//            else if ((luminosity.getBroadband() > mIntegrationTime.getHighThreshold()) && (mGain == Gain.X16)) {
//                System.out.println("broadband value too high, decreasing gain");
//                setGain(Gain.X1);
//                luminosity = getData();
//            }
//        }
//        else {
//            System.out.println("Auto gain is deactivated");
//        }
//
//        return luminosity;
//    }
//
//    public double getLux() throws IOException {
//        Luminosity luminosity = getLuminosity();
//
//        if (luminosity.getBroadband() == 0 && luminosity.getInfrared() == 0) {
//            luminosity = getLuminosity();
//        }
//
//        final double lux = luminosity.calculateLux();
//
//        System.out.println("Calculated " + lux + " lux from raw values " + luminosity);
//
//        return lux;
//    }
//}
