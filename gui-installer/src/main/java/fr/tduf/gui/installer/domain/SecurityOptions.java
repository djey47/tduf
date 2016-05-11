package fr.tduf.gui.installer.domain;

/**
 * Class describing security options 1&2 for a vehicle slot
 */
public class SecurityOptions {
    public static final float NOT_INSTALLED = 1;
    public static final float INSTALLED = 100;

    private final float optionOne;
    private int optionTwo;

    private SecurityOptions(float one, int two) {
        optionOne = one;
        optionTwo = two;
    }

    static SecurityOptions fromValues(float one, int two) {
        return new SecurityOptions(one, two);
    }

    public float getOptionOne() {
        return optionOne;
    }

    public int getOptionTwo() {
        return optionTwo;
    }
}
