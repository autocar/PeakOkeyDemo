package com.peak.demo.adt;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.peak.demo.ess.Essentials;

public class RackInfo {
	
	
	
	private Color color;
	private int colorId;
	private int number;
	

	
	
	public RackInfo(int rackId)
	{
		if (rackId < Essentials.RACK_VAL_MIN || rackId > Essentials.RACK_VAL_MAX)
			Gdx.app.log("ERROR", "Invalid Rack ID detected");
		
		
		
		color = new Color();
		
		colorId = (rackId - 1) / (Essentials.RACK_NUMBER_COUNT_PER_COLOR * Essentials.RACK_SET_COUNT_PER_COLOR);
		int colorOrder = colorId * 3;
		
		color.r = Essentials.RACK_ARR_COLOR_RGB[colorOrder++];
		color.g = Essentials.RACK_ARR_COLOR_RGB[colorOrder++];
		color.b = Essentials.RACK_ARR_COLOR_RGB[colorOrder++];
		color.a = 1.0f;
		
		int modRackId = rackId % Essentials.RACK_NUMBER_COUNT_PER_COLOR;
		number = modRackId == 0 ? Essentials.RACK_NUMBER_COUNT_PER_COLOR : modRackId;
		
		number = (rackId - Essentials.RACK_VAL_MIN) % Essentials.RACK_NUMBER_COUNT_PER_COLOR + 1;
	}
	
	public Color getColor()
	{
		return color;
	}
	
	public int getColorId()
	{
		return colorId;
	}
	
	public int getNumber()
	{
		return number;
	}
}
