package za.co.entelect.challenge;

import za.co.entelect.challenge.command.*;
import za.co.entelect.challenge.entities.*;
import za.co.entelect.challenge.enums.PowerUps;
import za.co.entelect.challenge.enums.Terrain;

import java.util.*;

import static java.lang.Math.max;
import static java.lang.Math.min;


public class Bot {
    private static int maxSpeed = 9;
    private List<Command> directionList = new ArrayList<>();

    private final static Command ACCELERATE = new AccelerateCommand();
    private final static Command LIZARD = new LizardCommand();
    private final static Command OIL = new OilCommand();
    private final static Command BOOST = new BoostCommand();
    private final static Command EMP = new EmpCommand();
    private final static Command FIX = new FixCommand();

    private final static Command TURN_RIGHT = new ChangeLaneCommand(1);
    private final static Command TURN_LEFT = new ChangeLaneCommand(-1);

    public Bot() {
        directionList.add(TURN_LEFT);
        directionList.add(TURN_RIGHT);
    }

    public Command run(GameState gameState) {
        Car myCar = gameState.player;
        Car opponent = gameState.opponent;
        maxSpeed = checkMax(myCar);
        boolean truckExists = checkTruck(myCar.position.lane, myCar.position.block, gameState, "front");
        boolean truckLeft = checkTruck(myCar.position.lane, myCar.position.block, gameState, "left");
        boolean truckRight = checkTruck(myCar.position.lane, myCar.position.block, gameState, "right");

        //Basic fix logic
        List<Object> blocks = getBlocks(myCar.position.lane, myCar.position.block, gameState, "front");
        List<Object> nextBlocks = blocks;
        if(blocks.size()>5){
            nextBlocks = blocks.subList(5, min(6+myCar.speed+1, blocks.size()));
        }

        //Fix first if too damaged to move
        if(myCar.damage == 5 || (myCar.damage>=2 && myCar.speed==maxSpeed)) {
            return FIX;
        }

        //Accelerate first if going to slow
        if(myCar.speed <= 3) {
            return ACCELERATE;
        }

        if ((truckExists || nextBlocks.contains(Terrain.MUD) || nextBlocks.contains(Terrain.WALL) || nextBlocks.contains(Terrain.OIL_SPILL))) {
            if (hasPowerUp(PowerUps.LIZARD, myCar.powerups)) {
                return LIZARD;
            }

            return doEvade(myCar, gameState, truckLeft, truckRight);
        }

        if(hasPowerUp(PowerUps.TWEET, myCar.powerups)){
            return new TweetCommand(opponent.position.lane, opponent.position.block+opponent.speed);
        }

        if(hasPowerUp(PowerUps.EMP, myCar.powerups) && myCar.position.block < opponent.position.block &&inCone(myCar, opponent)){
            return EMP;
        }

        if(hasPowerUp(PowerUps.BOOST, myCar.powerups) &&!(nextBlocks.contains(Terrain.MUD) || nextBlocks.contains(Terrain.WALL) || nextBlocks.contains(Terrain.OIL_SPILL))){
            return BOOST;
        }

        if(hasPowerUp(PowerUps.OIL, myCar.powerups) && myCar.position.block>opponent.position.block && !(nextBlocks.contains(Terrain.MUD) || nextBlocks.contains(Terrain.WALL) || nextBlocks.contains(Terrain.OIL_SPILL))){
            return OIL;
        }

        if(advantageous(myCar, gameState, "left", truckLeft)){
            return TURN_LEFT;
        }

        if(advantageous(myCar, gameState, "right", truckRight)){
            return TURN_RIGHT;
        }

        return ACCELERATE;
    }

    private int checkMax(Car myCar){
        switch (myCar.damage){
            case 0:
                return 15;
            case 1:
                return 9;
            case 2:
                return 8;
            case 3:
                return 6;
            case 4:
                return 3;
            case 5:
                return 0;
            default:
                return 9;
        }
    }

    private boolean advantageous(Car myCar, GameState gameState, String direction, boolean truck){
        List<Object> blocks = getBlocks(myCar.position.lane, myCar.position.block, gameState, direction);
        if(blocks.size()>5){
            blocks = blocks.subList(5, min(6+myCar.speed+1, blocks.size()));
        } else{
            return false;
        }

        if((blocks.contains(Terrain.BOOST) || blocks.contains(Terrain.EMP) || blocks.contains(Terrain.LIZARD) || blocks.contains(Terrain.TWEET)) && !(truck || blocks.contains(Terrain.MUD) || blocks.contains(Terrain.WALL) || blocks.contains(Terrain.OIL_SPILL))){
            return true;
        }

        return false;
    }

    private Command doEvade(Car myCar, GameState gameState, boolean truckLeft, boolean truckRight){
        List<Object> blocksLeft = getBlocks(myCar.position.lane, myCar.position.block, gameState, "left");
        List<Object> blocksRight = getBlocks(myCar.position.lane, myCar.position.block, gameState, "right");

        if(blocksLeft.size()>5){
            blocksLeft=blocksLeft.subList(5, min(6+myCar.speed+1, blocksLeft.size()));
        }
        if(blocksRight.size()>5){
            blocksRight=blocksRight.subList(5, min(6+myCar.speed+1, blocksRight.size()));
        }

        if(blocksLeft.size()>0 && !(truckLeft || blocksLeft.contains(Terrain.MUD) || blocksLeft.contains(Terrain.OIL_SPILL) || blocksLeft.contains(Terrain.WALL))){
            return TURN_LEFT;
        }
        if(blocksRight.size()>0 && !(truckRight || blocksRight.contains(Terrain.MUD) || blocksRight.contains(Terrain.OIL_SPILL) || blocksRight.contains(Terrain.WALL))){
            return TURN_RIGHT;
        }

        if(blocksLeft.size()>0 && !blocksLeft.contains(Terrain.WALL) && !blocksLeft.contains(true) && !truckLeft){
            return TURN_LEFT;
        }
        if(blocksRight.size()>0 && !blocksRight.contains(Terrain.WALL) && !blocksRight.contains(true) && !truckRight){
            return TURN_RIGHT;
        }

        return ACCELERATE;
    }
    private boolean inCone(Car myCar, Car opponent){
        return opponent.position.lane==myCar.position.lane || opponent.position.lane==myCar.position.lane-1 || opponent.position.lane==myCar.position.lane+1;
    }

    private boolean hasPowerUp(PowerUps powerUpToCheck, PowerUps[] available) {
        for (PowerUps powerUp: available) {
            if (powerUp.equals(powerUpToCheck)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns map of blocks and the objects in the for the current lanes, returns
     * the amount of blocks that can be traversed at max speed.
     **/
    private List<Object> getBlocks(int lane, int block, GameState gameState, String direction) {
        List<Lane[]> map = gameState.lanes;
        List<Object> blocks = new ArrayList<>();
        int startBlock = map.get(0)[0].position.block;

        Lane[] laneList = map.get(lane - 1);

        if(direction.equals("left")){
            if(lane-2<0){
                return blocks;
            }
            laneList = map.get(lane - 2);
        } else if(direction.equals("right")){
            if(lane>3){
                return blocks;
            }
            laneList = map.get(lane);
        }

        for (int i = max(block - startBlock -5, 0); i <= min(block - startBlock + 20, 1500); i++) {
            if (laneList[i] == null || laneList[i].terrain == Terrain.FINISH) {
                break;
            }

            blocks.add(laneList[i].terrain);
        }
        return blocks;
    }

    private boolean checkTruck(int lane, int block, GameState gameState, String direction) {
        List<Lane[]> map = gameState.lanes;
        int startBlock = map.get(0)[0].position.block;

        Lane[] laneList;

        if(direction.equals("left")){
            if(lane-2<0){
                return false;
            }
            laneList = map.get(lane - 2);
        } else if(direction.equals("right")){
            if(lane>3){
                return false;
            }
            laneList = map.get(lane);
        } else{
            laneList = map.get(lane - 1);
        }

        for (int i = max(block - startBlock -5, 0); i <= min(block - startBlock + 20, 1500); i++) {
            if (laneList[i] == null || laneList[i].terrain == Terrain.FINISH) {
                break;
            }

            if(laneList[i].isOccupiedByCyberTruck){
                return true;
            }

        }
        return false;
    }

}