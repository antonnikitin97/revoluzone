package proto;

import org.newdawn.slick.Color;
import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.geom.Circle;
import org.newdawn.slick.geom.Shape;
import org.newdawn.slick.geom.Transform;
import org.newdawn.slick.geom.Vector2f;

/**
 * Created by steppers on 2/12/17.
 */
public class Ball extends Renderable {

    private static final float G = 90f;

    public float x, y;

    private boolean moving = false;
    private float velX = 0, velY = 0;
    private float accelX = 0, accelY = 0;
    private float destX = 1, destY = 1;

    public Ball() {
        x = 1;
        y = 1;
    }

    public Ball(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public void update(float delta) {
        if(moving) {
            velX += accelX * delta;
            velY += accelY * delta;
            x += velX * delta;
            y += velY * delta;

            if(velX > 0) {
                if(x > destX) {
                    stopMoving();
                }
            } else if(velX < 0) {
                if(x < destX) {
                    stopMoving();
                }
            } else if(velY > 0) {
                if(y > destY) {
                    stopMoving();
                }
            }else if(velY < 0) {
                if(y < destY) {
                    stopMoving();
                }
            }
        }
    }

    public void move(int destX, int destY) {
        if(destX != (int)x || destY != (int)y) {
            if (moving = true) {
                this.destX = destX;
                this.destY = destY;

                float dx, dy;
                dx = destX - x;
                dy = destY - y;

                if(dx != 0)
                    accelX = (dx / Math.abs(dx))*G;
                if(dy != 0)
                    accelY = (dy / Math.abs(dy))*G;
            }
        }
    }

    private void stopMoving() {
        moving = false;
        x = destX;
        y = destY;
        velX = 0;
        velY = 0;
        accelX = 0;
        accelY = 0;
    }

    public boolean isMoving() {
        return moving;
    }

    @Override
    public void renderBackPlane(GameContainer gc, Graphics g) {

    }

    @Override
    public void renderFloorPlane(GameContainer gc, Graphics g) {

    }

    public void renderShadow(GameContainer gc, Graphics g, Model m) {
        float SCALE = ((Math.min(gc.getHeight(), gc.getWidth()) * 0.70f) / m.gridSize) * m.getScale();
        float offset = - (m.gridSize / 2) + 0.5f;
        Vector2f screenOffset = new Vector2f(gc.getWidth()/2, gc.getHeight()/2);

        Vector2f shadow = new Vector2f(0.07f, 0.07f).sub(m.getRotation() + 25).add(new Vector2f(offset, offset));
        g.setColor(Color.white.darker(0.8f).multiply(new Color(1,1,1,m.getOpacity()))); //Shadow color
        Vector2f pos = new Vector2f(x, y).add(shadow);
        pos.sub(-m.getRotation());
        pos.scale(SCALE);
        pos.add(screenOffset);

        Circle c = new Circle(0, 0, SCALE / 2);
        Shape circ = c.transform(Transform.createRotateTransform((float) (m.getRotation() * Math.PI) / 180));
        circ.setLocation(pos.x, pos.y);
        g.fill(circ);
    }

    public void renderObject(GameContainer gc, Graphics g, Model m) {
        float SCALE = ((Math.min(gc.getHeight(), gc.getWidth()) * 0.70f) / m.gridSize) * m.getScale();
        float offset = - (m.gridSize / 2) + 0.5f;
        Vector2f screenOffset = new Vector2f(gc.getWidth()/2, gc.getHeight()/2);

        Vector2f pos = new Vector2f(offset + x, offset + y);
        pos.sub(-m.getRotation());
        pos.scale(SCALE);
        pos.add(screenOffset);

        Circle c = new Circle(0, 0, SCALE/2.2f);
        Shape circ = c.transform(Transform.createRotateTransform((float)(m.getRotation()*Math.PI)/180));
        circ.setLocation(pos.x, pos.y);
        g.setColor(Color.cyan.multiply(new Color(1,1,1,m.getOpacity())));
        g.fill(circ);
    }
}