package com.peak.demo.adt;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector3;
import com.peak.demo.ess.Essentials;

public class MyRack {

	public Rectangle rect;
	public RackInfo rackInfo;
	public int rackId;
	public int rackNumber;
	public String strRackNumber;


	/**
	 * Coordinates to draw font on the rack.
	 */
	public MyPoint fontCoor; 

	/**
	 * Construct rack with a given rackId.
	 * @param  rackId id of Rack, between 1 and 52
	 * 
	 */
	public MyRack(int rackId)
	{
		this.rackId = rackId;
		rackNumber = (rackId - Essentials.RACK_VAL_MIN) % Essentials.RACK_NUMBER_COUNT_PER_COLOR + 1;
		strRackNumber = String.valueOf(rackNumber);

		rect = new Rectangle();
		rackInfo = new RackInfo(rackId);
		fontCoor = new MyPoint();

		rect.x = 0;
		rect.y = 0;
		rect.width = Essentials.RACK_PIXEL_WIDTH;
		rect.height = Essentials.RACK_PIXEL_HEIGHT;



		updateFontCoor();
	}

	public MyRack(MyRack givenRack)
	{
		rackId = givenRack.rackId;
		rackNumber = (rackId - Essentials.RACK_VAL_MIN) % Essentials.RACK_NUMBER_COUNT_PER_COLOR + 1;
		strRackNumber = String.valueOf(rackNumber);

		rect = new Rectangle();
		rackInfo = new RackInfo(rackId);
		fontCoor = new MyPoint();

		rect.x = givenRack.rect.x;
		rect.y = givenRack.rect.y;
		rect.width = Essentials.RACK_PIXEL_WIDTH;
		rect.height = Essentials.RACK_PIXEL_HEIGHT;



		updateFontCoor();
	}

	public void updateFontCoor()
	{
		fontCoor.x = rect.x + Essentials.RACK_PIXEL_WIDTH / 2 - Essentials.FONT_SIZES.get(rackNumber - 1).bounds.width / 2;

		fontCoor.y = rect.y + Essentials.RACK_PIXEL_HEIGHT - Essentials.RACK_FONT_BOTTOM_OFFSET + (Essentials.RACK_FONT_BOTTOM_OFFSET - Essentials.FONT_SIZES.get(rackNumber - 1).bounds.height)/ 2 + Essentials.RACK_PIXEL_HEIGHT - Essentials.RACK_FONT_BOTTOM_OFFSET;

	}

	public String getNumberString()
	{
		return strRackNumber;
	}


	public void setCenterPoint(float x, float y)
	{
		rect.x = x - Essentials.RACK_PIXEL_WIDTH / 2;
		rect.y = y - Essentials.RACK_PIXEL_HEIGHT / 2;
	}



	public float getCenterX()
	{
		return rect.width + Essentials.RACK_PIXEL_WIDTH / 2;
	}

	public float getCenterY()
	{
		return rect.height + Essentials.RACK_PIXEL_HEIGHT / 2;
	}

	public boolean serialRegionTriggered(Vector3 touchPos)
	{
		Rectangle serialRegion = new Rectangle();

		serialRegion.width = (float) (Essentials.RACK_PIXEL_WIDTH * Essentials.RACK_SERIAL_TRIGGER_RATIO);
		serialRegion.height = (float) (Essentials.RACK_PIXEL_HEIGHT * Essentials.RACK_SERIAL_TRIGGER_RATIO);
		serialRegion.x = rect.x + (Essentials.RACK_PIXEL_WIDTH - serialRegion.width);
		serialRegion.y = rect.y + (Essentials.RACK_PIXEL_HEIGHT - serialRegion.height);

		return serialRegion.contains(touchPos.x, touchPos.y);
	}

	public static boolean sameNumber(MyRack rack1, MyRack rack2)
	{
		return rack1.rackInfo.getNumber() == rack2.rackInfo.getNumber();
	}

	public static boolean sameColor(MyRack rack1, MyRack rack2)
	{
		return rack1.rackInfo.getColorId() == rack2.rackInfo.getColorId();
	}

	public static boolean sameColorAndNumber(MyRack rack1, MyRack rack2)
	{
		return sameNumber(rack1, rack2) && sameColor(rack1, rack2);
	}
}
