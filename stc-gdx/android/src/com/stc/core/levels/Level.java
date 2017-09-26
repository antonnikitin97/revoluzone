package com.stc.core.levels;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import com.badlogic.gdx.files.*;
import org.json.*;
import com.stc.core.levels.moveables.*;
import com.stc.core.levels.statics.*;
import java.util.*;
import com.badlogic.gdx.math.*;

/**
 * Created by steppers on 8/2/17.
 */

public class Level {
	
	private class Link {
		public int sx, sy, tx, ty;
	}

    private boolean loaded;
    private String raw = "";

    // Level info
    private String fileName;
    private String levelName;
    private String prevLevelName;
    private String nextLevelName;
	private int[] levelData;
	private ArrayList<Link> links;
	private Rail[][] rails;
    private int size;

    /*
     * Construct a level object from the file provided.
     * This will not lead to a usable object yet. Use load()
     * before the level is required.
     */
    public Level(FileHandle file) {
        this.loaded = false;
        this.fileName = file.name();
		links = new ArrayList<Link>();

        raw = file.readString();

        parseData();
    }

	/*
	 * Loads member variables from the raw JSON
	 */
    private void parseData() {
        JSONObject obj = new JSONObject(raw);
		
        JSONObject info = obj.getJSONObject("info");
        this.levelName = info.getString("levelName");
        this.nextLevelName = info.getString("nextLevelName");
        this.prevLevelName = info.getString("prevLevelName");
        this.size = info.getInt("size");
		
		levelData = new int[this.size*this.size];
		JSONArray data = obj.getJSONArray("data");
		for(int i = 0; i < data.length(); i++) {
			levelData[i] = data.get(i);
		}
		
		if(obj.has("links")) {
			JSONArray linkData = obj.getJSONArray("links");
			for(int i = 0; i < linkData.length(); i++) {
				JSONObject link = linkData.getJSONObject(i);
				Link l = new Link();
				l.sx = link.getInt("sx");
				l.sy = link.getInt("sy");
				l.tx = link.getInt("tx");
				l.ty = link.getInt("ty");
				links.add(l);
			}
		}
		
		if(obj.has("rails")) {
			rails = new Rail[this.size][this.size];
			JSONArray railData = obj.getJSONArray("rails");
			Link l = new Link();
			for(int i = 0; i < railData.length(); i++) {
				JSONObject rail = railData.getJSONObject(i);
				l.sx = rail.getInt("sx");
				l.sy = rail.getInt("sy");
				l.tx = rail.getInt("tx");
				l.ty = rail.getInt("ty");
				int dx = l.tx - l.sx;
				int dy = l.ty - l.sy;
				if(dx != 0) {
					dx = dx / Math.abs(dx);
					for(int x = l.sx; x != l.tx+dx; x += dx) {
						if(rails[x][l.sy] == null)
							rails[x][l.sy] = new Rail(x, l.sy);
					}
				} else if(dy != 0) {
					dy = dy / Math.abs(dy);
					for(int y = l.sy; y != l.ty+dy; y += dy) {
						if(rails[l.sx][y] == null)
							rails[l.sx][y] = new Rail(l.sx, y);
					}
				}
			}
		}
    }

    /*
     * Scans the data for errors
     */
    public boolean verify() {
        return true;
    }
	
	public LevelInstance getInstance() {
		LevelInstance instance = new LevelInstance(levelName, nextLevelName, prevLevelName);
		
		Tile[] tiles = new Tile[size*size];
		int x, y, index;
		for(int i = 0; i < size*size; i++) {
			int id = levelData[i];
			x = i % size;
			y = size - 1 - (i / size);
			index = y*size + x;
			
			tiles[index] = new Tile(x, y, TileType.EMPTY); // Default
			switch(id) {
				case 1:
					tiles[index] = new Tile(x, y, TileType.WALL);
					break;
				case 5:
					instance.addMoveable(new Ball(x, y));
					instance.addStatic(new StartPad(x, y));
					break;
				case 6:
					instance.addStatic(new FinishHole(x, y));
					break;
				case 2:
					tiles[index] = new Tile(x, y, TileType.RED);
					break;
				case 3:
					tiles[index] = new Tile(x, y, TileType.BLUE);
					break;
				case 7:
					instance.addStatic(new Switch(x, y));
					break;
				case 9:
					instance.addStatic(new LockedFinishHole(x, y));
					break;
				case 4:
					instance.addStatic(new KillZone(x, y));
					break;
				case 10:
					instance.addMoveable(new Slider(x, y));
					break;
				default: break;
			}
		}
		
		for(Link l : links) {
			instance.getStaticAt(l.sx, l.sy).addLink(instance.getStaticAt(l.tx, l.ty));
		}
		
		if(rails != null) {
			for(x = 0; x < this.size; x++) {
				for(y = 0; y < this.size; y++) {
					if(rails[x][y] != null) {
						Rail r = rails[x][y];
					
						if(x > 0)
							if(rails[x-1][y] != null)
								r.addLink(rails[x-1][y]);
							
						if(x < this.size-1)
							if(rails[x+1][y] != null)
								r.addLink(rails[x+1][y]);
							
						if(y > 0)
							if(rails[x][y-1] != null)
								r.addLink(rails[x][y-1]);
							
						if(y < this.size-1)
							if(rails[x][y+1] != null)
								r.addLink(rails[x][y+1]);
							
						instance.addStatic(r);
					}
				}
			}
		}
		
		instance.setTiles(tiles, size);
		instance.triggerUpdate(0);
		return instance;
	}
	
	public void setNextLevelName(String name) {
		nextLevelName = name;
	}
	
	public void setPrevLevelName(String name) {
		prevLevelName = name;
	}

    public String getNextLevelName() {
        return nextLevelName;
    }

    public String getPrevLevelName() {
        return prevLevelName;
    }

	public String getLevelName() {
		return levelName;
	}
	
}
