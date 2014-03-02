package com.peak.demo.helpers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Array;
import com.peak.demo.adt.MyRack;
import com.peak.demo.adt.RackSlotUnit;
import com.peak.demo.ess.Essentials;


public class RackHandler {

	public ArrayList<MyRack> racks;
	public ArrayList<MyRack> racksBackup;
	public Integer[] racksIdList;

	public Array<RackSlotUnit> rackSlotsFirstFloor, rackSlotsSecondFloor;
	public Array<RackSlotUnit> rackSlotsFirstFloorBackup, rackSlotsSecondFloorBackup;

	private int firstFloorCenterY;
	private int secondFloorCenterY;

	private int firstFloorBottomY;
	private int secondFloorBottomY;


	private float halfRackHeight;


	/**
	 * Refers to free slots for aligning the racks properly
	 */
	private int slotCountPerFloor;



	public RackHandler()
	{
		setup();

		randomizeRackIdList();
		initRacks();
		alignRacks();
	}

	public void randomizeRacks()
	{
		setup();

		randomizeRackIdList();
		initRacks();
		alignRacks();
	}

	public void orderSerially()
	{
		ArrayList<ArrayList<MyRack>> serials = new ArrayList<ArrayList<MyRack>>();

		// sorting with colors
		Collections.sort(racks,  new Comparator<MyRack>()
				{
			@Override
			public int compare(MyRack rack1, MyRack rack2)
			{
				if (MyRack.sameColor(rack1, rack2))
				{
					if (MyRack.sameNumber(rack1, rack2))
						return 0;

					return rack1.rackInfo.getNumber() - rack2.rackInfo.getNumber();
				}

				return rack1.rackInfo.getColorId() - rack2.rackInfo.getColorId();
			}
				});


		// collecting rack "1"s from each color.
		// keys represent color id
		// indexes can be up to 2 items at most, as there are 2 racks from each color
		HashMap<Integer, ArrayList<MyRack>> rack1s = new HashMap<Integer, ArrayList<MyRack>>();

		for (MyRack rack : racks)
		{
			if (rack.rackInfo.getNumber() == 1)
			{
				if (!rack1s.containsKey(rack.rackInfo.getColorId()))
				{
					rack1s.put(rack.rackInfo.getColorId(), new ArrayList<MyRack>(2));
				}

				rack1s.get(rack.rackInfo.getColorId()).add(rack);
			}
		}

		// making a list of used rack id list
		ArrayList<Integer> usedRackIdList = new ArrayList<Integer>();

		// creating serials first
		for(int i = 0; i < racks.size(); ++i)
		{
			// last 2 items cannot be a serial
			if (i >= racks.size() - 2)
				break;

			ArrayList<MyRack> serial = new ArrayList<MyRack>();
			MyRack pivotRack = racks.get(i);
			serial.add(pivotRack);
			int colorId = pivotRack.rackInfo.getColorId();
			int lastNumber = pivotRack.rackInfo.getNumber();
			for (int j = i+1; j < racks.size(); ++j)
			{
				MyRack nextRack = racks.get(j);

				if (lastNumber == 13)
				{
					// check if there is "1" from the same color which is not used
					if (rack1s.containsKey(colorId))
					{
						ArrayList<MyRack> rack1Items = rack1s.get(colorId);

						for (MyRack rack1Item : rack1Items)
						{
							if (!usedRackIdList.contains(rack1Item.rackId))
							{
								serial.add(rack1Item);
								break;
							}
						}
					}

					break;
				}

				if (!MyRack.sameColor(pivotRack, nextRack))
					break;

				if (nextRack.rackInfo.getNumber() == lastNumber)
					continue;


				if (nextRack.rackInfo.getNumber() != lastNumber +1)
					break;					


				serial.add(nextRack);
				++lastNumber;
			}


			if (serial.size() >= 3)
			{
				for (MyRack serialItem : serial)
					usedRackIdList.add(serialItem.rackId);

				serials.add(serial);
				i += lastNumber - pivotRack.rackInfo.getNumber();
			}


		}

		// checking the same numbers with different colors

		HashMap<Integer, ArrayList<MyRack>> sameNumbers = new HashMap<Integer, ArrayList<MyRack>>(Essentials.RACK_VAL_MAX - Essentials.RACK_VAL_MIN + 1);

		for (int i = Essentials.RACK_VAL_MIN; i <= Essentials.RACK_VAL_MAX; ++i)
		{
			sameNumbers.put(i, new ArrayList<MyRack>());
		}

		for (MyRack rack : racks)
		{
			if (usedRackIdList.contains(rack.rackId))
				continue;

			sameNumbers.get(rack.rackInfo.getNumber()).add(rack);
		}

		// now adding same numbers to our list if their count is more than 3
		for (int i = Essentials.RACK_VAL_MIN; i <= Essentials.RACK_VAL_MAX; ++i)
		{
			ArrayList<MyRack> sameNumberList = sameNumbers.get(i);

			// removing duplicate colors
			ArrayList<MyRack> duplicateRack = new ArrayList<MyRack>();
			ArrayList<Integer> occuredColorIdList = new ArrayList<Integer>();


			for (MyRack sameNumberRack : sameNumberList)
			{
				if (occuredColorIdList.contains(sameNumberRack.rackInfo.getColorId()))
				{
					duplicateRack.add(sameNumberRack);
					continue;
				}

				occuredColorIdList.add(sameNumberRack.rackInfo.getColorId());
			}

			for (MyRack removeRack : duplicateRack)
			{
				sameNumberList.remove(removeRack);
			}

			if (sameNumberList.size() >= 3)
			{
				for (MyRack sameNumberItem : sameNumberList)
					usedRackIdList.add(sameNumberItem.rackId);

				serials.add(sameNumberList);
			}
		}

		placeOnSlots(serials);
	}


	public void orderDually()
	{
		ArrayList<ArrayList<MyRack>> duals = new ArrayList<ArrayList<MyRack>>();


		Collections.sort(racks,  new Comparator<MyRack>()
				{
			@Override
			public int compare(MyRack rack1, MyRack rack2)
			{
				if (MyRack.sameNumber(rack1, rack2))
				{
					if (MyRack.sameColor(rack1, rack2))
						return 0;

					return rack1.rackInfo.getColorId() - rack2.rackInfo.getColorId();
				}

				return rack1.rackInfo.getNumber() - rack2.rackInfo.getNumber();
			}
				});

		for (int i = 0; i < racks.size(); ++i)
		{
			for (int j = i + 1; j < racks.size(); ++j)
			{
				if (MyRack.sameColorAndNumber(racks.get(i), racks.get(j)))
				{
					ArrayList<MyRack> tmpDual = new ArrayList<MyRack>(2);
					tmpDual.add(racks.get(i));
					tmpDual.add(racks.get(j));

					duals.add(tmpDual);
				}
			}
		}

		placeOnSlots(duals);
	}

	private void placeOnSlots(ArrayList<ArrayList<MyRack>> orderedSet)
	{
		int currSlotIndex = 0;

		boolean firstFloor = true;
		ArrayList<Integer> usedRackIdList = new ArrayList<Integer>();
		int groupItemCount = 0;

		for (ArrayList<MyRack> group : orderedSet)
		{
			groupItemCount += group.size();
			if (firstFloor && (currSlotIndex >= slotCountPerFloor || currSlotIndex + group.size() -1 >= slotCountPerFloor))
			{
				firstFloor = false;
				currSlotIndex = 0;
			}

			Array<RackSlotUnit> refSlot = firstFloor ? rackSlotsFirstFloor : rackSlotsSecondFloor;

			for (MyRack rack : group)
			{
				usedRackIdList.add(rack.rackId);
				rack.rect.x = refSlot.get(currSlotIndex).rect.x;
				rack.rect.y = refSlot.get(currSlotIndex).rect.y;
				refSlot.get(currSlotIndex).free = false;
				++currSlotIndex;
			}

			// leaving 1 space after a group
			++currSlotIndex;
		}

		int lastIndex = slotCountPerFloor - (racks.size() - groupItemCount);
		for (MyRack rack : racks)
		{
			if (usedRackIdList.contains(rack.rackId))
				continue;

			rack.rect.x = rackSlotsSecondFloor.get(lastIndex).rect.x;
			rack.rect.y = rackSlotsSecondFloor.get(lastIndex).rect.y;
			rackSlotsSecondFloor.get(lastIndex++).free = false;
		}
	}



	private void setup()
	{
		firstFloorCenterY = Essentials.BOARD_PIXEL_HEIGHT - Essentials.BOARD_FIRST_FLOOR_PIXEL_Y / 2;
		secondFloorCenterY = Essentials.BOARD_PIXEL_HEIGHT - Essentials.BOARD_FIRST_FLOOR_PIXEL_Y - Essentials.BOARD_SECOND_FLOOR_PIXEL_Y / 2;
		firstFloorBottomY = Essentials.BOARD_PIXEL_HEIGHT - Essentials.BOARD_FIRST_FLOOR_PIXEL_Y;
		secondFloorBottomY = Essentials.BOARD_PIXEL_HEIGHT - Essentials.BOARD_SECOND_FLOOR_PIXEL_Y;

		halfRackHeight = Essentials.RACK_PIXEL_HEIGHT / 2;

		racks = new ArrayList<MyRack>(Essentials.RACK_COUNT_PER_PLAYER);

		racksIdList = new Integer[Essentials.RACK_COUNT_PER_PLAYER];



		// generating free slot rectangles
		slotCountPerFloor = (Essentials.BOARD_PIXEL_WIDTH - 2 * Essentials.BOARD_SIDE_UNUSABLE_LENGTH) / Essentials.RACK_PIXEL_WIDTH;

		rackSlotsFirstFloor = new Array<RackSlotUnit>(slotCountPerFloor);
		rackSlotsSecondFloor = new Array<RackSlotUnit>(slotCountPerFloor);

		// generating floors' slots
		for (int i = 0; i < slotCountPerFloor; ++i)
		{
			Rectangle currRectFirst = new Rectangle();
			Rectangle currRectSecond = new Rectangle();

			currRectFirst.y = firstFloorBottomY;
			currRectSecond.y = secondFloorBottomY;
			currRectFirst.x = currRectSecond.x = Essentials.BOARD_SIDE_UNUSABLE_LENGTH + i * Essentials.RACK_PIXEL_WIDTH;
			currRectFirst.height = currRectSecond.height = Essentials.RACK_PIXEL_HEIGHT;
			currRectFirst.width = currRectSecond.width = Essentials.RACK_PIXEL_WIDTH;

			RackSlotUnit currRSUFirst = new RackSlotUnit();
			RackSlotUnit currRSUSecond = new RackSlotUnit();

			currRSUFirst.rect = currRectFirst;
			currRSUSecond.rect = currRectSecond;

			rackSlotsFirstFloor.add(currRSUFirst);
			rackSlotsSecondFloor.add(currRSUSecond);
		}


	}

	private void cleanRacksOnPlayer()
	{
		for (int i = 0; i < racksIdList.length; ++i)
			racksIdList[i] = -1;		
	}

	private boolean playerHasRack(int rackId)
	{
		boolean retval = false;
		for (int i : racksIdList)
			if (i == rackId)
				retval = true;

		return retval;
	}


	public void randomizeRackIdList()
	{
		cleanRacksOnPlayer();

		int randomRackId;
		int i = 0;
		while (i < racksIdList.length)
		{
			randomRackId = MathUtils.random(Essentials.RACK_VAL_MIN, Essentials.RACK_VAL_MAX);

			if (playerHasRack(randomRackId))
				continue;

			racksIdList[i++] = randomRackId; 
		}
	}

	/**
	 * Initializes racks on player with values in rack id list and sets default positions of each rack on the board.
	 */
	private void initRacks()
	{

		/**
		 * Sorting for ordered initialization.
		 */
		Arrays.sort(racksIdList, new Comparator<Integer>() {
					@Override
					public int compare(Integer x, Integer y)
					{
						int colorX = x / (Essentials.RACK_NUMBER_COUNT_PER_COLOR * Essentials.RACK_SET_COUNT_PER_COLOR);
						int colorY = y / (Essentials.RACK_NUMBER_COUNT_PER_COLOR * Essentials.RACK_SET_COUNT_PER_COLOR);
		
		
						int xMod = x % Essentials.RACK_NUMBER_COUNT_PER_COLOR;
						int yMod = y % Essentials.RACK_NUMBER_COUNT_PER_COLOR;
		
						xMod = xMod == 0 ? Essentials.RACK_NUMBER_COUNT_PER_COLOR : xMod;
						yMod = yMod == 0 ? Essentials.RACK_NUMBER_COUNT_PER_COLOR : yMod;
		
						if (xMod == yMod)
							return colorX - colorY;
		
						return xMod - yMod;
					}});



		for (int i = 0; i < Essentials.RACK_COUNT_PER_PLAYER; ++i)
		{
			MyRack rack = new MyRack(racksIdList[i]);

			rack.rect.x = rackSlotsFirstFloor.get(i).rect.x;
			rack.rect.y = rackSlotsFirstFloor.get(i).rect.y;

			racks.add(rack);
		}

		backupRacks();

	}

	private void backupRacks() {
		racksBackup = new ArrayList<MyRack>(racks.size());

		for (MyRack rack : racks)
		{
			racksBackup.add(new MyRack(rack));
		}

		rackSlotsFirstFloorBackup = new Array<RackSlotUnit>(slotCountPerFloor);

		for (RackSlotUnit rsu : rackSlotsFirstFloor)
		{
			rackSlotsFirstFloorBackup.add(new RackSlotUnit(rsu));
		}

		rackSlotsSecondFloorBackup = new Array<RackSlotUnit>(slotCountPerFloor);

		for (RackSlotUnit rsu : rackSlotsSecondFloor)
		{
			rackSlotsSecondFloorBackup.add(new RackSlotUnit(rsu));
		}

	}

	private void restoreFromBackup()
	{
		racks = racksBackup;
		rackSlotsFirstFloor = rackSlotsFirstFloorBackup;
		rackSlotsSecondFloor = rackSlotsSecondFloorBackup;
		backupRacks();
	}




	private void freeSlots(int rackIndex, int consecutiveRackCount)
	{

		MyRack refRack = racks.get(rackIndex);

		Array<RackSlotUnit> refFloor =  Math.abs(refRack.rect.y + halfRackHeight - firstFloorCenterY) < Math.abs(refRack.rect.y + halfRackHeight - secondFloorCenterY) 
				? rackSlotsFirstFloor : rackSlotsSecondFloor;

		int rightSlotIndex = getClosestSlotIndex(refRack, refFloor);

		try
		{
			for (int i = rightSlotIndex - consecutiveRackCount; i <= rightSlotIndex; ++i)
			{
				refFloor.get(i).free = true;
			}
		}
		catch (Exception e)
		{
			Gdx.app.log("freeSlot", "IMPORTANT!!!!!!!!!!! Exception caught");
		}

	}

	public boolean alignRacks()
	{
		return alignRacks(false, -1, -1);
	}

	public boolean alignRacks(boolean serialMove, int rackIndex, int consecutiveRackCount)
	{
		//		 checking if serial is placed to a free set of slots
		if (serialMove)
		{
			MyRack refRack = racks.get(rackIndex);

			Array<RackSlotUnit> refFloor =  Math.abs(refRack.rect.y + halfRackHeight - firstFloorCenterY) < Math.abs(refRack.rect.y + halfRackHeight - secondFloorCenterY) 
					? rackSlotsFirstFloor : rackSlotsSecondFloor;

			try
			{


				int startingSlotIndex = getClosestSlotIndex(refRack, refFloor) - consecutiveRackCount;

				startingSlotIndex = startingSlotIndex < 0 ? 0 : startingSlotIndex;


				// shifting from the end if needed
				if (startingSlotIndex + consecutiveRackCount >= refFloor.size - 1)
					startingSlotIndex = refFloor.size - 1 - consecutiveRackCount;



				// also checking if there are free slots to serials' each side
				for (int i = startingSlotIndex - 1; i <= startingSlotIndex + consecutiveRackCount + 1; ++i)
				{
					if (i < 0 || i >= slotCountPerFloor)
						continue;

					if (i >= refFloor.size)
						break;

					if (!refFloor.get(i).free)
					{
						restoreFromBackup();
						return false;
					}
				}


			}
			catch (Exception e)
			{
				Gdx.app.log(this.getClass().getName(), e.getMessage());

				restoreFromBackup();
				return false;

			}
		}

		for (RackSlotUnit slotUnit : rackSlotsFirstFloor)
			slotUnit.free = true;

		for (RackSlotUnit slotUnit : rackSlotsSecondFloor)
			slotUnit.free = true;

		// setting all racks' vertical position
		for (MyRack rack : racks)
		{
			rack.rect.y = (Math.abs(rack.rect.y + halfRackHeight - firstFloorCenterY) < Math.abs(rack.rect.y + halfRackHeight - secondFloorCenterY)) ? 
					firstFloorBottomY : secondFloorBottomY;			
		}



		// Sorting for proper use of aligning algorithm.

		Collections.sort(racks,  new Comparator<MyRack>()
				{
			@Override
			public int compare(MyRack rack1, MyRack rack2)
			{
				if (rack1.rect.y < rack2.rect.y)
					return 1;
				else if (rack1.rect.y > rack2.rect.y)
					return -1;
				else
					return (int) (rack1.rect.x - rack2.rect.x);
			}
				});





		for (int i = 0; i < racks.size(); ++i)
		{
			MyRack refRack = racks.get(i);
			boolean isOnFirstFloor = refRack.rect.y == firstFloorBottomY;
			Array<RackSlotUnit> refFloor = isOnFirstFloor ? rackSlotsFirstFloor : rackSlotsSecondFloor;

			if (!tryInsert(i, getClosestSlotIndex(refRack, refFloor), isOnFirstFloor))
			{
				restoreFromBackup();
				return false;
			}
		}

		backupRacks();

		return true;
	}



	/**
	 * Assuming all racks vertically aligned before calling this.
	 * This method will only calculate horizontal distance
	 * @param rack
	 * @param slots
	 * @return
	 */
	private int getClosestSlotIndex(MyRack rack, Array<RackSlotUnit> slots)
	{
		float minDist = Float.MAX_VALUE;
		int minIndex = -1;
		int currIndex = 0;
		for (RackSlotUnit rsu : slots)
		{
			float currDist = Math.abs(rack.rect.x - rsu.rect.x); 

			if (currDist < minDist)
			{
				minDist = currDist;
				minIndex = currIndex;
			}

			++currIndex;
		}

		return minIndex;
	}

	private boolean tryInsert(int rackIndex, int slotIndex, boolean firstFloor)
	{
		Array<RackSlotUnit> currSlots = firstFloor ? rackSlotsFirstFloor : rackSlotsSecondFloor;

		if (currSlots.get(slotIndex).free)
		{
			currSlots.get(slotIndex).free = false;
			racks.get(rackIndex).rect.x = currSlots.get(slotIndex).rect.x;
			racks.get(rackIndex).rect.y = currSlots.get(slotIndex).rect.y;
		}
		else
		{
			int nextFreeSlotIndex = -1;
			for (int i = slotIndex + 1; i < currSlots.size; ++i)
			{
				if (currSlots.get(i).free)
				{
					nextFreeSlotIndex = i;
					break;
				}
			}

			// if there is no free slot left on the floor
			if (nextFreeSlotIndex < 0)
				return false;

			currSlots.get(nextFreeSlotIndex).free = false;
			racks.get(rackIndex).rect.x = currSlots.get(nextFreeSlotIndex).rect.x;
			racks.get(rackIndex).rect.y = currSlots.get(nextFreeSlotIndex).rect.y;
		}

		return true;
	}


	/**
	 * Checks if touched rack is the right-most one.
	 * @param rackIndex Touched rack Index
	 * @return The amount of racks connected to the right-most rack of the serially moved racks. If the rack is not a part of a serial, the count will be less than 1.
	 */
	public int getSerialMoveLength(int rackIndex)
	{

		MyRack refRack = racks.get(rackIndex);
		Array<RackSlotUnit> refFloor = refRack.rect.y == firstFloorBottomY ? rackSlotsFirstFloor : rackSlotsSecondFloor;

		int slotIndex = getClosestSlotIndex(refRack, refFloor);

		// if the slot is the first, then it is not a serial
		if (slotIndex <= 0)
			return -1;

		if (slotIndex < refFloor.size - 1)
		{
			// check if it is not the right most one
			if (!refFloor.get(slotIndex + 1).free)
				return -1;	
		}


		int consecutiveRacksToLeft = 0;

		for (int i = slotIndex -1; i >= 0; --i)
		{
			if (refFloor.get(i).free)
				break;

			++consecutiveRacksToLeft;
		}

		return consecutiveRacksToLeft;

	}

	public void notifySerialMove(int activeRackIndex,
			int serialConsecutiveLength) {

		freeSlots(activeRackIndex, serialConsecutiveLength);
	}



}
