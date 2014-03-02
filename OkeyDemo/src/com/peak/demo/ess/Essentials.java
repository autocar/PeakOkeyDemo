package com.peak.demo.ess;

import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.BitmapFont.TextBounds;
import com.badlogic.gdx.utils.Array;
import com.peak.demo.adt.FontDetail;

public class Essentials {
	
	// the rgb values of red, yellow, black and blue; respectively
	public static final float[] RACK_ARR_COLOR_RGB = {1.0f, 0.0f, 0.0f, 1.0f, 0.5f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.8f, 1.0f};
	
	public static final int RACK_VAL_MIN = 1;
	public static final int RACK_VAL_MAX = 104;
	
	public static final int RACK_SET_COUNT_PER_COLOR = 2;
	public static final int RACK_NUMBER_COUNT_PER_COLOR = 13;
	public static final int RACK_COUNT = 104;
	
	public static final int RACK_COUNT_PER_PLAYER = 15;
	
	public static final int RACK_PIXEL_WIDTH = 71;
	public static final int RACK_PIXEL_HEIGHT = 99;
	public static final int BOARD_PIXEL_WIDTH = 1280;
	public static final int BOARD_PIXEL_HEIGHT = 225;
	
	/**
	 * The top-right area of the rack to trigger serial move.
	 */
	public static final double RACK_SERIAL_TRIGGER_RATIO = 0.35;
	
	/**
	 *  Unusable pixel length in board image from left and right sides.
	 */
	public static final int BOARD_SIDE_UNUSABLE_LENGTH = 26;
	
	/**
	 *  The bottom point of the board's first floor. The pixel value represents top-down length from the top of the board image.
	 */
	public static final int BOARD_FIRST_FLOOR_PIXEL_Y = 113;
	
	/**
	 *  The bottom point of the board's second floor. The pixel value represents top-down length from the top of the board image.
	 */
	public static final int BOARD_SECOND_FLOOR_PIXEL_Y = 217;
	
	/**
	 *  The bottom point of the rack's free space to draw number. The represents value shows the top-down length. 
	 */
	public static final int RACK_FONT_BOTTOM_OFFSET = 60;
	
	
	public static Array<FontDetail> FONT_SIZES;
	
	public static void initFontDetails(BitmapFont font)
	{
		FONT_SIZES = new Array<FontDetail>(RACK_NUMBER_COUNT_PER_COLOR);
		for (int i = RACK_VAL_MIN; i <= RACK_NUMBER_COUNT_PER_COLOR; ++i)
		{
			FontDetail tmp = new FontDetail();
			tmp.fontName = "default";
			tmp.str = String.valueOf(i);
			tmp.bounds = new TextBounds();
			tmp.bounds.set(font.getBounds(tmp.str)); // this is needed because the result object from getBounds() method is reused.
			
			FONT_SIZES.add(tmp);
		}
	}
	
	public static final String OPTION_NAME_RENEW = "Yenile";
	public static final String OPTION_NAME_SERIAL = "Seri Diz";
	public static final String OPTION_NAME_DUAL = "Cift Diz";
	
	public static final float OPTIONS_GAP_PIXEL = 50;
	
//	public static final long OPTIONS_TOUCH_LOCK_INTERVAL = 100000000;
}
