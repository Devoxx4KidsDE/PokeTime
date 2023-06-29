package org.devoxx4kids.poketime;

public enum Direction {

    DOWN(0),
    LEFT(1),
    RIGHT(2),
    UP(3);

    private final int offset;

    Direction(int offset) {

        this.offset = offset;
    }

    public int getOffset() {

        return offset;
    }


    public int getXOffset() {

        switch (this) {
            case LEFT:
                return -1;

            case RIGHT:
                return 1;

            default:
                return 0;
        }
    }


    public int getYOffset() {

        switch (this) {
            case UP:
                return -1;

            case DOWN:
                return 1;

            default:
                return 0;
        }
    }


    public static Direction random() {

        switch ((int) (4 * Math.random())) {
            case 0:
                return DOWN;

            case 1:
                return LEFT;

            case 2:
                return RIGHT;

            default:
                return UP;
        }
    }
}
