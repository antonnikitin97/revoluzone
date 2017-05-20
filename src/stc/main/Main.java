package stc.main;

import org.newdawn.slick.AppGameContainer;

/**
 * A game made for the Soton GameJam by:
 * Anton, Ollie and Alistair
 */

public class Main {

    private static final String GAME_NAME = "Squaring the Circle";

    public static void main(String[] args) {

        AppGameContainer gc;
        try{
            gc = new AppGameContainer(new StateManager(GAME_NAME));
//            gc.setDisplayMode(gc.getScreenWidth()-100, gc.getScreenHeight()-80, false);
//            gc.setDisplayMode(1920, 1080, true);
            gc.setDisplayMode(1280, 720, false);
//            gc.setDisplayMode(gc.getScreenWidth(), gc.getScreenHeight(), true);
            gc.setVSync(true);
            gc.setSmoothDeltas(true);
            gc.setMultiSample(4);
            gc.setShowFPS(false);
            gc.start();
        }catch (Exception e){
            System.out.println(e.getMessage());
        }
    }
}
