package com.peak.demo.adt;

import com.badlogic.gdx.math.Rectangle;

public class RackSlotUnit {

	public RackSlotUnit()
	{
		rect = new Rectangle();
	}
	
	public RackSlotUnit(RackSlotUnit rsu) {
		rect = new Rectangle();
		rect.x = rsu.rect.x;
		rect.y = rsu.rect.y;
		rect.width = rsu.rect.width;
		rect.height = rsu.rect.height;
		free = rsu.free;
	}
	
	public boolean free = true;
	public Rectangle rect;
}