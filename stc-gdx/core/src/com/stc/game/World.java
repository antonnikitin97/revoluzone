package com.stc.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Align;
import com.stc.game.levels.LevelInstance;
import com.stc.game.levels.LevelObject;

import java.util.ArrayList;

public class World
{
	
	private float scale = 1.0f;
	private float rotation = 0.0f;
	private float opacity = 1.0f;
	private float textOpacity = 1.0f;
	
	private ShapeRenderer renderer;
	private SpriteBatch sb;
    private BitmapFont font;
    private GlyphLayout layout;
	
	private Interpolator scaleLerp = new Interpolator(1.0f, 1.0f, 0.0f);
	private Interpolator rotationLerp = new Interpolator(0.0f, 0.0f, 0.0f);
	private Interpolator opacityLerp = new Interpolator(1.0f, 1.0f, 0.0f);
	private Interpolator textOpacityLerp = new Interpolator(1.0f, 1.0f, 0.0f);
	
	public World() {
		renderer = Renderer.shapeRenderer();
		sb = Renderer.spriteBatch();
        font = Renderer.gameFont();
		font.setColor(Globals.COLOR_TEXT);
	}
	
	public World(float scale) {
		this.scale = scale;
		renderer = Renderer.shapeRenderer();
		sb = Renderer.spriteBatch();
        font = Renderer.gameFont();
		font.setColor(Globals.COLOR_TEXT);
	}
	
	public World(float scale, float rotation) {
		this(scale);
		this.rotation = rotation;
	}
	
	public World(float scale, float rotation, float opacity) {
		this(scale, rotation);
		this.opacity = opacity;
		this.textOpacity = opacity;
	}
	
	public void update(float delta) {
		scaleLerp.update(delta);
		rotationLerp.update(delta);
		opacityLerp.update(delta);
		textOpacityLerp.update(delta);
		
		scale = scaleLerp.lerp();
		rotation = rotationLerp.lerp();
		rotation = rotation < 0 ? rotation + 360 : rotation;
		rotation = rotation % 360;
		opacity = opacityLerp.lerp();
		textOpacity = textOpacityLerp.lerp();
	}
	
	public void render(LevelInstance level) {
		renderer.begin(ShapeRenderer.ShapeType.Filled);
		renderer.identity();

		float scaleFactor = Gdx.graphics.getHeight() / (1.414f * level.getSize());
		if(Globals.orientation.equals("portrait"))
			scaleFactor = Gdx.graphics.getWidth() / (1.414f * level.getSize());

		renderer.translate(Gdx.graphics.getWidth()/2, Gdx.graphics.getHeight()/2, 0);
		renderer.rotate(0,0,1,rotation);
		renderer.scale(scale*scaleFactor, scale*scaleFactor, 1);
		renderer.translate(-(float)level.getSize()/2.0f, -(float)level.getSize()/2.0f, 0);
		
		renderFloor(level.getSize());
		
		ArrayList<LevelObject> objects = level.getLevelObjects();
		
		for(LevelObject o : objects) {
			o.renderFloor(renderer, opacity);
		}
		
		Vector2 t = new Vector2(Globals.SHADOW_OFFSET, Globals.SHADOW_OFFSET);
		t.rotate(-rotation);
		renderer.translate(t.x, t.y, 0.0f);
		
		for(LevelObject o : objects) {
			o.renderShadow(renderer, opacity);
		}
		
		renderer.translate(-t.x, -t.y, 0.0f);
		
		for(LevelObject o : objects) {
			o.renderObject(renderer, opacity);
		}
		
		renderer.end();
	}
	
	private void renderFloor(int size) {
		Color floorColor = new Color(Globals.COLOR_FLOOR);
		floorColor.a *= opacity;
		renderer.setColor(floorColor);
		
		float x = 0, y = 0;
		for(int i = 0; i < size*size; i++) {
			x = i % size;
			y = i / size;
			renderer.rect(x, y, 1, 1);
		}
	}
	
	public void drawString(float x, float y, String text, float inRotation) {
		layout = new GlyphLayout(font, text);
		
		float scaleFactor = Gdx.graphics.getHeight() / 2.828f;
		if(Globals.orientation.equals("portrait"))
			scaleFactor = Gdx.graphics.getWidth() / 2.828f;
		
		sb.begin();
		
		Matrix4 m = new Matrix4().idt();
		m.translate(Gdx.graphics.getWidth()/2, Gdx.graphics.getHeight()/2, 0);
		m.rotate(0, 0, 1, rotation);
		m.scale(scale, scale, 1);
		m.rotate(0, 0, 1, inRotation);
		m.translate(x, y * scaleFactor, 0);
		
		sb.setTransformMatrix(m);
		Color c = new Color(Globals.COLOR_TEXT);
		c.a *= textOpacity;
		font.setColor(c);
        font.draw(sb, text, -layout.width/2, layout.height/2.0f, layout.width, Align.center, false);
        sb.end();

        // Fix the blend state back (sb.end() resets it)
        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
	}
	
	public void scale(float target, float duration) {
		scaleLerp.begin(scale, target, duration);
	}
	
	public void rotate(float degrees, float duration) {
		rotationLerp.begin(rotation, rotation + degrees, duration);
	}
	
	public void rotate(float degrees) {
		rotation += degrees;
		rotationLerp.clear(rotation);
	}
	
	public void fade(float target, float duration) {
		opacityLerp.begin(opacity, target, duration);
	}
	
	public void fadeText(float target, float duration) {
		textOpacityLerp.begin(textOpacity, target, duration);
	}
	
	public void setupScaleLerp(float from, float to, float duration) {
		scale = from;
		scaleLerp.begin(from, to, duration);
	}
	
	public void setupRotationLerp(float from, float to, float duration) {
		rotation = from;
		rotationLerp.begin(from, to, duration);
	}
	
	public void setupOpacityLerp(float from, float to, float duration) {
		opacity = from;
		opacityLerp.begin(from, to, duration);
	}
	
	public void setupTextOpacityLerp(float from, float to, float duration) {
		textOpacity = from;
		textOpacityLerp.begin(from, to, duration);
	}
	
	public void setRotation(float rotation) {
		this.rotation = rotation;
		rotationLerp.begin(rotation, rotation, 0);
	}
	
	public float getRotation() {
		return rotation;
	}
	
	public void setScale(float scale) {
		this.scale = scale;
		scaleLerp.begin(scale, scale, 0);
	}

	public float getScale() {
		return scale;
	}
	
	public void setOpacity(float opacity) {
		this.opacity = opacity;
		opacityLerp.begin(opacity, opacity, 0);
	}
	
	public float getOpacity() {
		return opacity;
	}
	
	public void setTextOpacity(float opacity) {
		textOpacity = opacity;
		textOpacityLerp.begin(opacity, opacity, 0);
	}
	
	public float getTextOpacity() {
		return textOpacity;
	}
	
	public boolean changing() {
		return scaleLerp.active() || rotationLerp.active() || opacityLerp.active() || textOpacityLerp.active();
	}
	
}
