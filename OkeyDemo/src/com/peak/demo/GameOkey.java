package com.peak.demo;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.BitmapFont.TextBounds;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector3;
import com.peak.demo.adt.MyRack;
import com.peak.demo.ess.Essentials;
import com.peak.demo.helpers.RackHandler;


public class GameOkey implements ApplicationListener {

	private static int SCREEN_WIDTH = 1280;
	private static int SCREEN_HEIGHT = 768;
	private OrthographicCamera camera;
	private SpriteBatch batch;

	private Texture imgRack;
	private Texture imgBoard;
	private static String PATH_RACK = "tile-rack.png";
	private static String PATH_BOARD = "tile-board.png";

	private BitmapFont font;

	private boolean rackInteraction = false;
	private boolean lockInteraction = false;
	private boolean serialMove = false;
	private int serialConsecutiveLength = -1;

	private MyRack activeRack;
	private int activeRackIndex = -1;

	private Vector3 touchPos;

	private Music themeMusic;
	private Sound sfxPick;
	private Sound sfxPut;
	private Sound sfxError;
	private Sound sfxSuccess;

	private RackHandler rh;

	private Rectangle optionRectRenew, optionRectSerial, optionRectDual;
	private Color optionColor;

	@Override
	public void create() {

		// initializing font sizes
		font = new BitmapFont(Gdx.files.internal("data/default.fnt"), Gdx.files.internal("data/default.png"), false);
		font.getRegion().getTexture().setFilter(TextureFilter.Linear, TextureFilter.Linear);
		font.scale(2);

		Essentials.initFontDetails(font);

		// creating menu rectangles
		TextBounds tmpBound;
		optionRectRenew = new Rectangle();
		tmpBound = font.getBounds(Essentials.OPTION_NAME_RENEW);
		optionRectRenew.width = tmpBound.width;
		optionRectRenew.height = tmpBound.height;
		optionRectRenew.y = SCREEN_HEIGHT - Essentials.OPTIONS_GAP_PIXEL - optionRectRenew.height;
//		optionRectRenew.x = SCREEN_WIDTH / 2 - optionRectRenew.width / 2;
		optionRectRenew.x = SCREEN_WIDTH - optionRectRenew.width - Essentials.BOARD_SIDE_UNUSABLE_LENGTH;

		optionRectSerial = new Rectangle();
		tmpBound = font.getBounds(Essentials.OPTION_NAME_SERIAL);
		optionRectSerial.width = tmpBound.width;
		optionRectSerial.height = tmpBound.height;
		optionRectSerial.y = optionRectRenew.y - Essentials.OPTIONS_GAP_PIXEL * 2 - optionRectSerial.height;
//		optionRectSerial.x = SCREEN_WIDTH / 2 - optionRectSerial.width / 2;
		optionRectSerial.x = SCREEN_WIDTH - optionRectSerial.width - Essentials.BOARD_SIDE_UNUSABLE_LENGTH;

		optionRectDual = new Rectangle();
		tmpBound = font.getBounds(Essentials.OPTION_NAME_DUAL);
		optionRectDual.width = tmpBound.width;
		optionRectDual.height = tmpBound.height;
		optionRectDual.y = optionRectSerial.y - Essentials.OPTIONS_GAP_PIXEL * 2 - optionRectDual.height;
//		optionRectDual.x = SCREEN_WIDTH / 2 - optionRectDual.width / 2;
		optionRectDual.x = SCREEN_WIDTH - optionRectDual.width - Essentials.BOARD_SIDE_UNUSABLE_LENGTH;

		optionColor = new Color(0.9f, 0.9f, 0.9f, 1.0f);

		Texture.setEnforcePotImages(false);	
		imgRack = new Texture(Gdx.files.internal(PATH_RACK));
		imgBoard = new Texture(Gdx.files.internal(PATH_BOARD));

		rh = new RackHandler();

		touchPos = new Vector3();

		themeMusic = Gdx.audio.newMusic(Gdx.files.internal("ff8_shuffle_or_boogie.mp3"));
		themeMusic.play();
		themeMusic.setLooping(true);

		sfxPick = Gdx.audio.newSound(Gdx.files.internal("sfx_pick_16bit.wav"));
		sfxPut = Gdx.audio.newSound(Gdx.files.internal("sfx_put_16bit.wav"));
		sfxError = Gdx.audio.newSound(Gdx.files.internal("sfx_error_16bit.wav"));
		sfxSuccess = Gdx.audio.newSound(Gdx.files.internal("sfx_success_16bit.wav"));

		batch = new SpriteBatch();

		camera = new OrthographicCamera();
		camera.setToOrtho(false, SCREEN_WIDTH, SCREEN_HEIGHT);
	}


	@Override
	public void dispose() {
		batch.dispose();
		font.dispose();
		imgBoard.dispose();
		imgRack.dispose();
		themeMusic.dispose();
		sfxPick.dispose();
		sfxPut.dispose();
		sfxError.dispose();
		sfxSuccess.dispose();
	}

	@Override
	public void render() {


		Gdx.gl.glClearColor(0, 0, 0.2f, 1);
		Gdx.gl.glClear(GL10.GL_COLOR_BUFFER_BIT);
		
		camera.update();
		
		batch.setProjectionMatrix(camera.combined);
		batch.begin();
		
		// draw board
		batch.draw(imgBoard, 0, 0);

		// drawing racks
		for (MyRack rack : rh.racks)
		{
			if (rackInteraction && rack.equals(activeRack))
				continue;

			// draw rack
			batch.draw(imgRack, rack.rect.x, rack.rect.y);

			// draw number on rack
			rack.updateFontCoor();
			font.setColor(rack.rackInfo.getColor());
			font.draw(batch, rack.getNumberString(), rack.fontCoor.x, rack.fontCoor.y); 
		}

		if (rackInteraction)
		{
			batch.draw(imgRack, activeRack.rect.x, activeRack.rect.y);

			if (serialMove)
			{
				// this is to ensure serially moved racks are drawn above
				for (int i = activeRackIndex- serialConsecutiveLength; i < activeRackIndex; ++i)
				{
					MyRack tmpRack =  rh.racks.get(i);
					batch.draw(imgRack, tmpRack.rect.x, tmpRack.rect.y);
					font.setColor(tmpRack.rackInfo.getColor());
					font.draw(batch, tmpRack.getNumberString(), tmpRack.fontCoor.x, tmpRack.fontCoor.y); 
				}
			}

			// draw number on rack
			activeRack.updateFontCoor();
			font.setColor(activeRack.rackInfo.getColor());
			font.draw(batch, activeRack.getNumberString(), activeRack.fontCoor.x, activeRack.fontCoor.y); 
		}

		// draw options
		font.setColor(optionColor);

		font.draw(batch, Essentials.OPTION_NAME_RENEW, optionRectRenew.x, optionRectRenew.y + optionRectRenew.height);
		font.draw(batch, Essentials.OPTION_NAME_SERIAL, optionRectSerial.x,optionRectSerial.y + optionRectSerial.height);
		font.draw(batch, Essentials.OPTION_NAME_DUAL, optionRectDual.x, optionRectDual.y + optionRectDual.height);

		batch.end();

		camera.update();

		if (!lockInteraction)
		{
			if (Gdx.input.isTouched())
			{    
				touchPos.set(Gdx.input.getX(), Gdx.input.getY(), 0);
				camera.unproject(touchPos);

				// this code-block means that user didn't finish his action
				if (rackInteraction)
				{

					activeRack.setCenterPoint(touchPos.x, touchPos.y);

					if (serialMove)
					{
						for (int i = 1; i <= serialConsecutiveLength; ++i)
						{
							rh.racks.get(activeRackIndex - i).setCenterPoint(touchPos.x - (Essentials.RACK_PIXEL_WIDTH * i), touchPos.y);
						}
					}
				}
				else
				{ 
					activeRack = null;
					activeRackIndex = -1;
					for (MyRack rack : rh.racks)
					{
						++activeRackIndex;
						if (rack.rect.contains(touchPos.x, touchPos.y))
						{
							activeRack = rack;
							break;

						}
					}

					// this means a rack is touched
					if (activeRack != null)
					{
						sfxPick.play();

						rackInteraction = true; 	
						serialMove = false;

						if (activeRack.serialRegionTriggered(touchPos))
						{
							serialConsecutiveLength = rh.getSerialMoveLength(activeRackIndex);

							if (serialConsecutiveLength > 0)
							{
								rh.notifySerialMove(activeRackIndex, serialConsecutiveLength);
								serialMove = true;
							}
						}
					}
					else // this means a rack is NOT touched
					{
						if (optionRectRenew.contains(touchPos.x, touchPos.y))
						{
							rh = new RackHandler();
							System.gc();

							lockInteraction = true;
							
							sfxSuccess.play();
						}
						else if (optionRectSerial.contains(touchPos.x, touchPos.y))
						{
							rh.orderSerially();
							rh.alignRacks();

							lockInteraction = true;
							
							sfxSuccess.play();
						}
						else if (optionRectDual.contains(touchPos.x, touchPos.y))
						{
							rh.orderDually();
							rh.alignRacks();

							lockInteraction = true;
							
							sfxSuccess.play();
						}
					}
				}
			}
			else
			{
				// this code-block means that user has just stopped interacting with screen
				if (rackInteraction)
				{  
					if (rh.alignRacks(serialMove, activeRackIndex, serialConsecutiveLength))
						sfxPut.play();
					else
						sfxError.play();
				}


				rackInteraction = false;
				serialMove = false;
				lockInteraction = false;
			}
		}

		if (lockInteraction)
			lockInteraction = Gdx.input.isTouched();
	}


	@Override
	public void resize(int width, int height) {
	}

	@Override
	public void pause() {
	}

	@Override
	public void resume() {
	}
}
