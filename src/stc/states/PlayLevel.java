package stc.states;

import org.newdawn.slick.Color;
import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Input;
import stc.GameState;
import stc.Model;
import stc.Tile;
import stc.Slider;
import stc.TransitionManager;
import stc.UI.UILabel;
import stc.UI.UIRenderable;
import sun.audio.AudioPlayer;
import sun.audio.AudioStream;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;

/**
 * Created by Ollie on 20/05/2017.
 */
public class PlayLevel {

    private GameState gs;
    private TransitionManager tm;
    private Model m;

    private ArrayList<UIRenderable> staticUI;
    private ArrayList<UIRenderable> rotatingUI;

    private boolean escaping = false;

    public PlayLevel(GameState gameState, TransitionManager tm, GameContainer gc) {
        gs = gameState;
        this.tm = tm;
        m = gs.m;

        //Static UI
        staticUI = new ArrayList<>();
        UILabel tmpLabel = new UILabel(gc);
        tmpLabel.text = m.getProperty("message_left");
        tmpLabel.anchor.set(0.0f, 0.5f);
        tmpLabel.offset.set(0.11f, 0.0f);
        tmpLabel.color = Color.green.darker(0.4f);
        staticUI.add(tmpLabel.clone());
        tmpLabel.text = m.getProperty("message_right");
        tmpLabel.anchor.set(1.0f, 0.5f);
        tmpLabel.offset.set(-0.11f, 0.0f);
        tmpLabel.color = Color.green.darker(0.4f);
        staticUI.add(tmpLabel.clone());

        //Rotating UI
        rotatingUI = new ArrayList<>();
        tmpLabel = new UILabel("Best move count: " + m.getProperty("score"), gc);
        tmpLabel.anchor.set(0.5f, 0.5f);
        tmpLabel.offset.set(0f, -0.4f);
        tmpLabel.scale = m.getScale()/0.6f;
        rotatingUI.add(tmpLabel.clone());
        tmpLabel.text = "Your move count: " + m.score;
        tmpLabel.rotation = 180f;
        rotatingUI.add(tmpLabel.clone());
    }

    public void update(GameContainer gc) {
        m = gs.m;
        if(gc.getInput().isKeyDown(Input.KEY_ESCAPE) || escaping) {
            escaping = true;
            m.score = 0;
            if(m.getRotation() != 0) {
                tm.transitionFadeRotate(m, new Model(m.getProperty("filename"), 1.0f, 0f), GameState.State.LEVEL, -m.getRotation(), 0.3f);
            }
            if(!tm.isTransitioning()) {
                tm.transitionShrink(m, GameState.State.LEVEL_SELECT, 0.6f, 0.3f);
                escaping = false;
            }
        }
        if(gc.getInput().isKeyPressed(Input.KEY_RIGHT)) {
            m.score += 1;
            tm.transitionRotate(m, gs.currentState, 90, 0.2f);
        }
        else if(gc.getInput().isKeyPressed(Input.KEY_LEFT)) {
            m.score += 1;
            tm.transitionRotate(m, gs.currentState, -90, 0.2f);
        }
        if(gc.getInput().isKeyPressed(Input.KEY_SPACE) && m.getTileUnderBall().type != Tile.Type.BLUE && m.getTileUnderBall().type != Tile.Type.RED) {
            m.toggleRedBlue();
            try {
                String File = "res/sounds/Red_Blue_Switch.wav";
                InputStream in = new FileInputStream(File);
                AudioStream audioStream = new AudioStream(in);
                AudioPlayer.player.start(audioStream);
            }catch(Exception e){}
        }
        if(m.hasCompleted()) {
            if(m.score < Integer.parseInt(m.getProperty("score"))) {
                m.setProperty("score", String.valueOf(m.score)); //Could be saved to file too
            }
            tm.transitionFadeRotate(m, new Model(m.getProperty("next"), 1.0f, 0f), GameState.State.LEVEL, 90, 0.3f);
        }
        Tile t = m.getTileUnderBall();
        t.activate(m);
        for(Slider s: m.sliders){
            Tile ts = m.getTileUnderSlider(s);
            ts.activate(m);
        }
        for(UIRenderable r : staticUI) {
            r.update();
        }
        for(UIRenderable r : rotatingUI) {
            r.update();
        }
    }

    public void render(GameContainer gc, Graphics g) {
        gs.m.render(gc, g);
        renderText(g, gs.m);
    }

    public void renderText(Graphics g, Model m) {
        ((UILabel)rotatingUI.get(0)).text = "Best move count: " + m.getProperty("score");
        ((UILabel)rotatingUI.get(1)).text = "Your move count: " + m.score;
        ((UILabel)staticUI.get(0)).text = m.getProperty("message_left");
        ((UILabel)staticUI.get(1)).text = m.getProperty("message_right");
        if(gs.currentState == GameState.State.TRANSITION) {
            for(UIRenderable r : rotatingUI) {
                r.color.a = (m.getScale()-0.6f)*2.5f;
                r.scale = m.getScale();
                r.offsetRotation(m.getRotation());
                r.scaleOffset(m.getScale());
                r.render(g);
            }
            if(tm.getNewState() == GameState.State.LEVEL) {
                for(UIRenderable r : staticUI) {
                    r.scale = m.getScale();
                    r.scaleOffset(m.getScale());
                    r.color.a = m.getOpacity();
                    r.render(g);
                }
            } else {
                for(UIRenderable r : staticUI) {
                    r.scale = m.getScale();
                    r.scaleOffset(m.getScale());
                    r.color.a = (m.getScale()-0.6f)*2.5f;
                    r.render(g);
                }
            }
        } else {
            for(UIRenderable r : rotatingUI) {
                r.color.a = m.getOpacity();
                r.scale = m.getScale();
                r.offsetRotation(m.getRotation());
                r.scaleOffset(m.getScale());
                r.render(g);
            }
            for(UIRenderable r : staticUI) {
                r.scale = m.getScale();
                r.scaleOffset(m.getScale());
                r.color.a = (m.getScale()-0.6f)*2.5f;
                r.render(g);
            }
        }

    }

}
