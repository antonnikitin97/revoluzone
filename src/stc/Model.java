package stc;

import org.newdawn.slick.Color;
import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.geom.*;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by steppers on 2/12/17.
 */
public class Model extends Renderable {

    private static final float ROT_VEL = 360f;

    public int gridSize;
    public int score = 0;

    private HashMap<String, String> properties;

    public Tile[][] tiles;
    public Ball ball;
    public List<Slider> sliders = new ArrayList<>();

    private float rotation = 0;
    private float scale = 1;
    private float opacity = 1;
    private float textOpacity = 1;
    public boolean redEnabled = false;

    private Color opCol = new Color(1,1,1,1);

    public Model(String fileName, float scale) {
        properties = new HashMap<>();
        loadFromFile(fileName);
        setOpacity(1);
        setScale(scale);
        reset();
        recalcAll();

    }

    public Model(String fileName, float scale, float opacity) {
        properties = new HashMap<>();
        loadFromFile(fileName);
        setOpacity(opacity);
        setScale(scale);
        reset();
        recalcAll();
    }

    public void update(float delta) {
        //Update our rotation here
        ball.update(delta, this);
        for(int i = 0; i < sliders.size(); i++) {
            sliders.get(i).update(delta, this);
        }
    }

    public Tile getTileUnderBall() {
        return tiles[(int)ball.x][(int)ball.y];
    }

    public Tile getTileUnderSlider(Slider s){return tiles[(int)s.x][(int)s.y];}

    public void reset() {
        sliders.forEach(Slider::reset);
        for(int x = 0; x < gridSize; x++) {
            for (int y = 0; y < gridSize; y++) {
                if(tiles[x][y].type == Tile.Type.START)
                    ball = new Ball(x, y);
                tiles[x][y].reset(true);
            }
        }
    }

    public void toggleRedBlue() {
        for(Slider s : sliders) {
            if(tiles[(int)s.destX][(int)s.destY].type == Tile.Type.RED || tiles[(int)s.destX][(int)s.destY].type == Tile.Type.BLUE)
                return;
        }
        for(int x = 0; x < gridSize; x++) {
            for (int y = 0; y < gridSize; y++) {
                if(tiles[x][y].type == Tile.Type.RED || tiles[x][y].type == Tile.Type.BLUE) {
                    tiles[x][y].active = !tiles[x][y].active;
                }
            }
        }
        redEnabled = !redEnabled;
        recalcAll();
    }

    public void addSlider(int x, int y) {
        sliders.add(new Slider(x,y));
    }

    public boolean isWaiting() {
        if (ball.isMoving()) {
            return true;
        }
        for (int i = 0; i < sliders.size(); i++) {
            if (sliders.get(i).isMoving()) {
                return true;
            }
        }
        return false;
    }

    public void recalcBall() {
        int r = (int)rotation % 360;
        while(r < 0)
            r += 360;
        int y = (int)(ball.destY);
        int x = (int)(ball.destX);
        switch(r) {
            case 0:
                for(y = y+1; y < gridSize; y++) {
                    if(tiles[x][y].isSolid(this)) {
                        ball.move(x, y-1);
                        return;
                    }
                }
                break;
            case 90:
                for(x = x+1; x < gridSize; x++) {
                    if(tiles[x][y].isSolid(this)) {
                        ball.move(x-1, y);
                        return;
                    }
                }
                break;
            case 180:
                for(y = y-1; y >= 0; y--) {
                    if(tiles[x][y].isSolid(this)) {
                        ball.move(x, y+1);
                        return;
                    }
                }
                break;
            case 270:
                for(x = x-1; x >= 0; x--) {
                    if(tiles[x][y].isSolid(this)) {
                        ball.move(x+1, y);
                        return;
                    }
                }
                break;
        }
    }

    public void recalcAll() {
        recalcBall();
        for (int i = 0; i < sliders.size(); i++) {
            recalcSlider(sliders.get(i));
        }
        //Calculate in reverse as well in case we miss one that can move
        for (int i = sliders.size()-1; i >= 0; i--) {
            recalcSlider(sliders.get(i));
        }
        recalcBall();
    }

    public void recalcSlider(Slider s) {
        int r = (int) rotation % 360;
        while (r < 0) {
            r += 360;
        }

        int y = (int) (s.destY);
        int x = (int) (s.destX);

        switch (r) {
            case 0:
                for (y = y + 1; y < gridSize; y++) {
                    if ((tiles[x][y].isSolid(this) && tiles[x][y].isRail) || !tiles[x][y].isRail || (ball.destX == x && ball.destY == y)) {
                        s.move(x, y - 1);
                        break;
                    }
                }
                break;
            case 90:
                for (x = x + 1; x < gridSize; x++) {
                    if ((tiles[x][y].isSolid(this) && tiles[x][y].isRail) || !tiles[x][y].isRail || (ball.destX == x && ball.destY == y)) {
                        s.move(x - 1, y);
                        break;
                    }
                }
                break;
            case 180:
                for (y = y - 1; y >= 0; y--) {
                    if ((tiles[x][y].isSolid(this) && tiles[x][y].isRail) || !tiles[x][y].isRail || (ball.destX == x && ball.destY == y)) {
                        s.move(x, y + 1);
                        break;
                    }
                }
                break;
            case 270:
                for (x = x - 1; x >= 0; x--) {
                    if ((tiles[x][y].isSolid(this) && tiles[x][y].isRail) || !tiles[x][y].isRail || (ball.destX == x && ball.destY == y)) {
                        s.move(x + 1, y);
                        break;
                    }
                }
                break;
        }
    }

    public void render(GameContainer gc, Graphics g) {
        renderBackPlane(gc, g);
        renderFloorPlane(gc, g);
        renderShadow(gc, g);
        renderObject(gc, g);
    }

    @Override
    public void renderBackPlane(GameContainer gc, Graphics g) {
        float SCALE = ((Math.min(gc.getHeight(), gc.getWidth()) * 0.70f) / gridSize) * scale;
        float offset = - ((float)gridSize / 2) + 0.5f;

        Rectangle rect = new Rectangle(-SCALE/2, -SCALE/2, SCALE, SCALE);
        Shape tile = rect.transform(Transform.createRotateTransform((float)(rotation*Math.PI)/180));

        Vector2f screenOffset = new Vector2f(gc.getWidth()/2, gc.getHeight()/2);

        g.setColor(Color.white.darker(0.2f).multiply(opCol)); //Floor color
        for(int x = 0; x < gridSize; x++) {
            for (int y = 0; y < gridSize; y++) {
                Vector2f pos = new Vector2f(offset + x, offset + y);
                pos.sub(-rotation);
                pos.scale(SCALE);
                pos.add(screenOffset);
                tile.setLocation(pos.x, pos.y);
                g.fill(tile);
            }
        }
    }

    @Override
    public void renderFloorPlane(GameContainer gc, Graphics g) {
        float SCALE = ((Math.min(gc.getHeight(), gc.getWidth()) * 0.70f) / gridSize) * scale;
        Tile t;
        float offset = - ((float)gridSize / 2) + 0.5f;

        //Full floor tile
        Rectangle rect = new Rectangle(-SCALE/2, -SCALE/2, SCALE, SCALE);
        Shape tile = rect.transform(Transform.createRotateTransform((float)(rotation*Math.PI)/180));
        Rectangle stripeRect = new Rectangle(-SCALE/2, -SCALE/2, SCALE, SCALE);
        Shape stripe = stripeRect.transform(Transform.createScaleTransform(0.2f, 0.7f));
        Shape cross1, cross2;
        cross1 = stripe.transform(Transform.createRotateTransform((float)(rotation*Math.PI)/180 + (float)Math.PI/4));
        cross2 = stripe.transform(Transform.createRotateTransform((float)(rotation*Math.PI)/180 - (float)Math.PI/4));
        //Switch rect
        rect = new Rectangle(-(SCALE/2)*0.6f, -(SCALE/2)*0.6f, SCALE*0.6f, SCALE*0.6f);
        Shape switch1 = rect.transform(Transform.createRotateTransform((float)(rotation*Math.PI)/180));
        Circle cicleSwitch = new Circle(0,0, SCALE*0.5f*0.4f);

        //Start/finish
        Circle circleLarge = new Circle(0,0, SCALE*0.5f*0.8f);
        Circle circleSmall = new Circle(0,0, SCALE*0.5f*0.7f);

        //Rails
        Rectangle rail = new Rectangle(-SCALE/2f, -SCALE/10, SCALE, SCALE/5);
        Shape railX = rail.transform(Transform.createRotateTransform((float)(rotation*Math.PI)/180));
        Shape railY = rail.transform(Transform.createRotateTransform((float)(((rotation*Math.PI)/180)+Math.PI/2)));

        Rectangle railStop = new Rectangle(0, -SCALE/10, SCALE/2, SCALE/5);
        Shape railStopX1 = railStop.transform(Transform.createRotateTransform((float)(rotation*Math.PI)/180));
        Shape railStopY1 = railStop.transform(Transform.createRotateTransform((float)(((rotation*Math.PI)/180)+Math.PI/2)));
        Shape railStopX2 = railStop.transform(Transform.createRotateTransform((float)(((rotation*Math.PI)/180)+Math.PI)));
        Shape railStopY2 = railStop.transform(Transform.createRotateTransform((float)(((rotation*Math.PI)/180)+3*Math.PI/2)));

        Circle dot = new Circle(0, 0, SCALE / 6f);
        Shape railDot = dot.transform(Transform.createRotateTransform((float)(rotation*Math.PI)/180));

        Vector2f screenOffset = new Vector2f(gc.getWidth()/2, gc.getHeight()/2);

        for(int x = 0; x < gridSize; x++) {
            for (int y = 0; y < gridSize; y++) {
                Vector2f pos = new Vector2f(offset + x, offset + y);
                pos.sub(-rotation);
                pos.scale(SCALE);
                pos.add(screenOffset);

                railX.setLocation(pos.x, pos.y);
                railY.setLocation(pos.x, pos.y);
                railStopX1.setLocation(pos.x, pos.y);
                railStopX2.setLocation(pos.x, pos.y);
                railStopY1.setLocation(pos.x, pos.y);
                railStopY2.setLocation(pos.x, pos.y);
                railDot.setLocation(pos.x, pos.y);

                t = tiles[x][y];
                switch(t.type) {
                    case KILL:
                        g.setColor(Color.yellow.multiply(opCol));
                        tile.setLocation(pos.x, pos.y);
                        g.fill(tile);
                        g.setColor(Color.black.multiply(opCol));
                        cross1.setLocation(pos.x, pos.y);
                        cross2.setLocation(pos.x, pos.y);
                        g.fill(cross1);
                        g.fill(cross2);
                        break;
                    case RED:
                        if(t.active)
                            break;
                        g.setColor(Color.red.multiply(opCol).multiply(new Color(1, 1, 1, 0.3f)));
                        tile.setLocation(pos.x, pos.y);
                        g.fill(tile);
                        break;
                    case BLUE:
                        if(t.active)
                            break;
                        g.setColor(Color.blue.multiply(opCol).multiply(new Color(1, 1, 1, 0.3f)));
                        tile.setLocation(pos.x, pos.y);
                        g.fill(tile);
                        break;
                    case START:
                        g.setColor(Color.green.darker().multiply(opCol));
                        circleLarge.setCenterX(pos.x);
                        circleLarge.setCenterY(pos.y);
                        g.fill(circleLarge);
                        g.setColor(Color.green.multiply(opCol));
                        circleSmall.setCenterX(pos.x);
                        circleSmall.setCenterY(pos.y);
                        g.fill(circleSmall);
                        break;
                    case FINISH:
                        g.setColor(Color.darkGray.multiply(opCol));
                        circleLarge.setCenterX(pos.x);
                        circleLarge.setCenterY(pos.y);
                        g.fill(circleLarge);
                        g.setColor(Color.black.multiply(opCol));
                        circleSmall.setCenterX(pos.x);
                        circleSmall.setCenterY(pos.y);
                        g.fill(circleSmall);
                        break;
                    case SWITCH:
                        g.setColor(Color.darkGray.multiply(opCol));
                        switch1.setLocation(pos.x, pos.y);
                        g.fill(switch1);
                        if(t.active)
                            g.setColor(Color.green.multiply(opCol));
                        else
                            g.setColor(Color.red.multiply(opCol));
                        cicleSwitch.setCenterX(pos.x);
                        cicleSwitch.setCenterY(pos.y);
                        g.fill(cicleSwitch);
                        break;
                    case LOCKED_FINISH:
                        g.setColor(Color.darkGray.multiply(opCol));
                        circleLarge.setCenterX(pos.x);
                        circleLarge.setCenterY(pos.y);
                        g.fill(circleLarge);
                        g.setColor(Color.red.multiply(opCol));
                        circleSmall.setCenterX(pos.x);
                        circleSmall.setCenterY(pos.y);
                        g.fill(circleSmall);
                        break;
                }
                if(tiles[x][y].isRail){
                    g.setColor(Color.black.multiply(opCol));
                    if(tiles[x+1][y].isRail && tiles[x-1][y].isRail){
                        g.fill(railX);
                    }if(tiles[x][y+1].isRail && tiles[x][y-1].isRail){
                        g.fill(railY);
                    }if(tiles[x+1][y].isRail && !tiles[x-1][y].isRail){
                        g.fill(railStopX1);
                        g.fill(railDot);
                    }if(!tiles[x+1][y].isRail && tiles[x-1][y].isRail){
                        g.fill(railStopX2);
                        g.fill(railDot);
                    }if(tiles[x][y+1].isRail && !tiles[x][y-1].isRail){
                        g.fill(railStopY1);
                        g.fill(railDot);
                    }if(!tiles[x][y+1].isRail && tiles[x][y-1].isRail){
                        g.fill(railStopY2);
                        g.fill(railDot);
                    }if(!tiles[x][y+1].isRail && !tiles[x][y-1].isRail && !tiles[x+1][y].isRail && !tiles[x-1][y].isRail){
                        g.fill(railDot);
                    }
                }
            }
        }

    }

    @Override
    public void renderShadow(GameContainer gc, Graphics g) {
        float SCALE = ((Math.min(gc.getHeight(), gc.getWidth()) * 0.70f) / gridSize) * scale;
        float offset = - ((float)gridSize / 2) + 0.5f;

        Rectangle rect = new Rectangle(-SCALE/2, -SCALE/2, SCALE, SCALE);
        Shape tile = rect.transform(Transform.createRotateTransform((float)(rotation*Math.PI)/180));

        Vector2f screenOffset = new Vector2f(gc.getWidth()/2, gc.getHeight()/2);
        
        Vector2f shadow = new Vector2f(0.07f, 0.07f).sub(rotation + 25).add(new Vector2f(offset, offset));
        g.setColor(Color.white.darker(0.8f).multiply(opCol).multiply(new Color(1,1,1,0.7f))); //Shadow color
        for (int x = 0; x < gridSize; x++) {
            for (int y = 0; y < gridSize; y++) {
                if(tiles[x][y].type == Tile.Type.LOCKED_FINISH)
                    continue;
                if (tiles[x][y].isSolid(this) && (tiles[x][y].hasSlider(this) == null)) {
                    Vector2f pos = new Vector2f(shadow.x + x, shadow.y + y);
                    pos.sub(-rotation);
                    pos.scale(SCALE);
                    pos.add(screenOffset);
                    tile.setLocation(pos.x, pos.y);
                    g.fill(tile);
                }
            }
        }

        //Render Ball shadow
        ball.renderShadow(gc, g, this);
        for (int i = 0; i < sliders.size(); i++) {
            sliders.get(i).renderShadow(gc, g, this);
        }
    }

    @Override
    public void renderObject(GameContainer gc, Graphics g) {
        float SCALE = ((Math.min(gc.getHeight(), gc.getWidth()) * 0.70f) / gridSize) * scale;
        float offset = - ((float)gridSize / 2) + 0.5f;

        Rectangle rect = new Rectangle(-SCALE/2, -SCALE/2, SCALE, SCALE);
        Shape tile = rect.transform(Transform.createRotateTransform((float)(rotation*Math.PI)/180));

        Vector2f screenOffset = new Vector2f(gc.getWidth()/2, gc.getHeight()/2);

        for(int x = 0; x < gridSize; x++) {
            for(int y = 0; y < gridSize; y++) {
                Vector2f pos = new Vector2f(offset + x, offset + y);
                pos.sub(-rotation);
                pos.scale(SCALE);
                pos.add(screenOffset);
                tile.setLocation(pos.x, pos.y);

                switch (tiles[x][y].type) {
                    case FIXED:
                        g.setColor(Color.white.darker(0.4f).multiply(opCol));
                        g.fill(tile);
                        break;
                    case RED:
                        if(!tiles[x][y].active)
                            break;
                        g.setColor(Color.red.multiply(opCol));
                        g.fill(tile);
                        break;
                    case BLUE:
                        if(!tiles[x][y].active)
                            break;
                        g.setColor(Color.blue.multiply(opCol));
                        g.fill(tile);
                        break;
                    default:
                        break;
                }
            }
        }
        ball.renderObject(gc, g, this);
        for(int i = 0; i < sliders.size(); i++) {
            sliders.get(i).renderObject(gc, g, this);
        }
    }

    public void drawLink(Line l, GameContainer gc, Graphics g) {
        float SCALE = ((Math.min(gc.getHeight(), gc.getWidth()) * 0.70f) / gridSize) * scale;
        float offset = - ((float)gridSize / 2) + 0.5f;
        Vector2f screenOffset = new Vector2f(gc.getWidth()/2, gc.getHeight()/2);

        Circle dot = new Circle(0, 0, SCALE / 8f);
        Shape linkDot = dot.transform(Transform.createRotateTransform((float)(rotation*Math.PI)/180));

        Vector2f posSrc = new Vector2f(offset + l.getX1(), offset + l.getY1());
        posSrc.sub(-rotation);
        posSrc.scale(SCALE);
        posSrc.add(screenOffset);

        Vector2f posDst = new Vector2f(offset + l.getX2(), offset + l.getY2());
        posDst.sub(-rotation);
        posDst.scale(SCALE);
        posDst.add(screenOffset);

        g.draw(new Line(posSrc, posDst));

        linkDot.setLocation(posSrc);
        g.fill(linkDot);
        linkDot.setLocation(posDst);
        g.fill(linkDot);

        Vector2f dir = posDst.sub(posSrc);
        Vector2f normal = dir.getPerpendicular().normalise();
        Vector2f center = dir.scale(0.5f);
        center  = center.add(posSrc);
        Vector2f arrow1 = new Vector2f();
        Vector2f arrow2 = new Vector2f();
        arrow1.set(normal);
        arrow2.set(normal.negate());
        Transform t = Transform.createRotateTransform((float)(60f*Math.PI)/180);
        arrow1 = t.transform(arrow1).scale(30f);
        t = Transform.createRotateTransform((float)(-60f*Math.PI)/180);
        arrow2 = t.transform(arrow2).scale(30f);
        arrow1.add(center);
        arrow2.add(center);

        g.draw(new Line(center, arrow1));
        g.draw(new Line(center, arrow2));

        g.resetTransform();
    }

    public void setRotation(float rotation) {
        this.rotation = rotation;
    }

    public float getRotation() {
        return rotation;
    }

    public void setScale(float scale) {
        this.scale = scale;
    }

    public float getScale() {
        return scale;
    }

    public void setOpacity(float opacity) {
        this.opacity = opacity;
        opCol = new Color(1,1,1,opacity);
    }

    public float getOpacity() {
        return opacity;
    }

    public void setTextOpacity(float opacity) {
        this.textOpacity = opacity;
    }

    public float getTextOpacity() {
        return textOpacity;
    }

    public String getProperty(String key) {
        return properties.get(key);
    }

    public void setProperty(String key, String val) {
        properties.put(key, val);
    }

    public boolean hasCompleted() {
        return tiles[(int)ball.x][(int)ball.y].type == Tile.Type.FINISH;
    }

    private void loadFromFile(String path) {
        File file = new File("res/levels/" + path);
        if(!file.exists()) {
            System.out.println("File not found: " + path);
            System.exit(1);
        }

        try {
            BufferedReader br = new BufferedReader(new FileReader(file));
            String line;

            ball = new Ball(3, 3);

            //Finds the tile array
            int y = 1;
            while((line = br.readLine()).startsWith("{")) {
                String[] ids = line.substring(1, line.length()-1).split(",");
                if(tiles == null)
                    initTiles(ids.length);
                for(int i = 0; i < ids.length; i++) {
                    tiles[i+1][y] = new Tile(Integer.parseInt(ids[i]));
                    tiles[i+1][y].x = i+1;
                    tiles[i+1][y].y = y;
                    if(ids[i].equals("5")) {
                        ball = new Ball(i+1, y);
                    }
                }
                y++;
            }
            if(line.length() != 1)
                processPropertyLine(line);

            //Process modifiers
            while((line = br.readLine()) != null) {
                processPropertyLine(line);
            }

        } catch (Exception e) {
            System.err.println("Error reading file: " + path);
            e.printStackTrace();
            System.exit(2);
        }
        properties.put("filename", path);
    }

    private void processPropertyLine(String line) {
        String[] parts = line.split("=");
        if(parts.length == 1)
            return;
        String type = parts[0].trim();
        String data = parts[1].trim();
        StringBuilder s = new StringBuilder();
        String[] lines = data.split("\\\\n");
        switch (type) {
            case "name":
                properties.put("name", data);
                break;
            case "next":
                properties.put("next", data);
                break;
            case "prev":
                properties.put("prev", data);
                break;
            case "score":
                properties.put("score", data);
                break;
            case "message_left":
                for(int i = 0; i < lines.length; i++) {
                    s.append(lines[i]);
                    if(i != lines.length-1)
                        s.append("\n");
                }
                properties.put("message_left", s.toString());
                break;
            case "message_right":
                for(int i = 0; i < lines.length; i++) {
                    s.append(lines[i]);
                    if(i != lines.length-1)
                        s.append("\n");
                }
                properties.put("message_right", s.toString());
                break;
            case "link"://Links switches, teleporters and sliders
                String firstLink = data.split("->")[0];
                String secondLink = data.split("->")[1];
                int L1x, L1y, L2x, L2y;
                L1x = Integer.parseInt(firstLink.split(",")[0]);
                L1y = Integer.parseInt(firstLink.split(",")[1]);
                L2x = Integer.parseInt(secondLink.split(",")[0]);
                L2y = Integer.parseInt(secondLink.split(",")[1]);
                tiles[L1x][L1y].links.add(tiles[L2x][L2y]);
                break;
            case "slider"://Slider initial positions
                String slider = data.trim();
                int sx, sy;
                sx = Integer.parseInt(slider.split(",")[0]);
                sy = Integer.parseInt(slider.split(",")[1]);
                addSlider(sx, sy);
                break;
            case "rail":
                String firstRail = data.split("->")[0];
                String secondRail = data.split("->")[1];
                int R1x, R1y, R2x, R2y;
                R1x = Integer.parseInt(firstRail.split(",")[0]);
                R1y = Integer.parseInt(firstRail.split(",")[1]);
                R2x = Integer.parseInt(secondRail.split(",")[0]);
                R2y = Integer.parseInt(secondRail.split(",")[1]);
                for(int x = Math.min(R1x, R2x); x <= Math.max(R1x, R2x); x++) {
                    for(int y = Math.min(R1y, R2y); y <= Math.max(R1y, R2y); y++) {
                        tiles[x][y].isRail = true;
                    }
                }
                break;
            default:
                properties.put(type, data);
        }
    }

    public void saveToFile(String filename, String name) {
        FileWriter fw = null;
        BufferedWriter bw = null;
        try {
            fw = new FileWriter("res/levels/" + filename);
            bw = new BufferedWriter(fw);

            StringBuilder data = new StringBuilder();

            for(int y = 1; y < tiles.length-1; y++) {
                data.append("{");
                for (int x = 1; x < tiles.length - 1; x++) {
                    data.append(tiles[x][y].resetType.ordinal() + ((x < tiles.length-2) ? "," : ""));
                }
                data.append("}\n");
            }
            data.append("name=" + name + "\n");
            data.append("next=" + (getProperty("next") == null ? "user_levels/null.txt" : getProperty("next")) + "\n");
            data.append("prev=" + (getProperty("prev") == null ? "user_levels/null.txt" : getProperty("prev")) + "\n");
            data.append("score=" + (getProperty("score") == null ? "999" : getProperty("score")) + "\n");
            data.append("message_left=\n");
            data.append("message_right=\n");

            for(int y = 1; y < tiles.length-1; y++) {
                for (int x = 1; x < tiles.length - 1; x++) {
                    for(Tile to : tiles[x][y].links) {
                        data.append("link=" + x + "," + y + "->" + to.x + "," + to.y + "\n");
                    }
                }
            }

            Tile t;
            for(int y = 1; y < tiles.length-1; y++) {
                for (int x = 1; x < tiles.length - 1; x++) {
                    t = tiles[x][y];
                    if(t.isRail) {
                        data.append("rail=" + x + "," + y + "->" + x + "," + y + "\n");
                    }
                }
            }

            for(Slider s : sliders) {
                data.append("slider=" + (int)s.resetX + "," + (int)s.resetY + "\n");
            }

            bw.write(data.toString());

        } catch (IOException e) {
            System.err.println("Error writing file: " + filename);
            e.printStackTrace();
            System.exit(3);
        } finally {
            try {
                if (bw != null)
                    bw.close();

                if (fw != null)
                    fw.close();

            } catch (IOException ex) {
                System.err.println("Error writing file: " + filename);
                ex.printStackTrace();
                System.exit(3);
            }

        }
    }

    public void resize(int newSize) {
        Tile[][] newTiles = new Tile[newSize+2][newSize+2];
        int newGridSize = newSize+2;

        for(int x = 0; x < newSize+2; x++) {
            for(int y = 0; y < newSize+2; y++) {
                if(x == 0 || y == 0 || x == newSize+1 || y == newSize+1) {
                    newTiles[x][y] = new Tile(Tile.Type.FIXED.ordinal());
                } else {
                    newTiles[x][y] = new Tile(Tile.Type.EMPTY.ordinal());
                }
                newTiles[x][y].x = x;
                newTiles[x][y].y = y;
            }
        }

        int smaller = Math.min(newGridSize, gridSize);
        for(int x = 1; x < smaller-1; x++) {
            for(int y = 1; y < smaller-1; y++) {
                newTiles[x][y] = tiles[x][y];
            }
        }
        tiles = newTiles;
        gridSize = newGridSize;

        reset();
        recalcAll();
    }

    private void initTiles(int size) {
        tiles = new Tile[size+2][size+2];
        gridSize = size+2;

        for(int x = 0; x < size+2; x++) {
            for(int y = 0; y < size+2; y++) {
                if(x == 0 || y == 0 || x == size+1 || y == size+1) {
                    tiles[x][y] = new Tile(Tile.Type.FIXED.ordinal());
                } else {
                    tiles[x][y] = new Tile(Tile.Type.EMPTY.ordinal());
                }
            }
        }
    }

    public Tile getTileFromMousePos(GameContainer gc) {
        float SCALE = ((Math.min(gc.getHeight(), gc.getWidth()) * 0.70f) / gridSize) * scale;

        float offset = - ((float)gridSize / 2);

        Vector2f screenOffset = new Vector2f(gc.getWidth()/2, gc.getHeight()/2);

        Vector2f mouse = new Vector2f(gc.getInput().getMouseX(), gc.getInput().getMouseY());
        mouse = mouse.sub(screenOffset);
        mouse = mouse.scale(1/SCALE);
        mouse = mouse.sub(rotation);
        mouse = mouse.sub(new Vector2f(offset, offset));

        int x, y;
        x = (int)mouse.x;
        y = (int)mouse.y;

        if(x >= 0 && x < gridSize && y >= 0 && y < gridSize) {
            return tiles[x][y];
        }
        return null;
    }

    public Vector2f getWorldCoordOfTile(Vector2f tileCoord, GameContainer gc) {
        float SCALE = ((Math.min(gc.getHeight(), gc.getWidth()) * 0.70f) / gridSize) * scale;

        float offset = - ((float)gridSize / 2);

        Vector2f screenOffset = new Vector2f(gc.getWidth()/2, gc.getHeight()/2);

        tileCoord = tileCoord.add(new Vector2f(offset, offset));
        tileCoord = tileCoord.add(rotation);
        tileCoord = tileCoord.scale(SCALE);
        tileCoord = tileCoord.add(screenOffset);

        return tileCoord;
    }

}
