package za.co.entelect.challenge;

import za.co.entelect.challenge.command.*;
import za.co.entelect.challenge.entities.*;
import za.co.entelect.challenge.enums.PowerUps;
import za.co.entelect.challenge.enums.Terrain;

import java.util.*;

import static java.lang.Math.max;
import static java.lang.Math.min;

import java.security.SecureRandom;

import java.util.Arrays;

public class Bot {

    private static int maxSpeed = 9;
    private List<Command> directionList = new ArrayList<>();
    private final Random random;

    private final static Command ACCELERATE = new AccelerateCommand();
    private final static Command LIZARD = new LizardCommand();
    private final static Command OIL = new OilCommand();
    private final static Command BOOST = new BoostCommand();
    private final static Command EMP = new EmpCommand();
    private final static Command FIX = new FixCommand();

    private final static Command TURN_RIGHT = new ChangeLaneCommand(1);
    private final static Command TURN_LEFT = new ChangeLaneCommand(-1);

    public Bot() {
        this.random = new SecureRandom();
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
        List<Object> blocks = getBlocksInFront(myCar.position.lane, myCar.position.block, gameState, myCar);

        if (myCar.damage >= 2) {
            return FIX;
        }

        if(truckExists || truckLeft || truckRight) {
            int result = doTurn(myCar, gameState);
            if(result > 5) {
                if(hasPowerUp(PowerUps.LIZARD, myCar.powerups)) {
                    return LIZARD;
                } else {
                    result -= 5;
                }
            }
            if(result == 1) {
                return TURN_RIGHT;
            } else if (result == 2) {
                return TURN_LEFT;
            }
        }

        if(myCar.speed > 8) {
            int result = doTurn(myCar, gameState);
            if(result > 5) {
                if(hasPowerUp(PowerUps.LIZARD, myCar.powerups)) {
                    return LIZARD;
                } else {
                    result -= 5;
                }
            }
            if(result == 1) {
                return TURN_RIGHT;
            } else if (result == 2) {
                return TURN_LEFT;
            }
        }

        if(myCar.speed <= 3 && blocks.contains(Terrain.MUD)) {
            int result = doTurn(myCar, gameState);
            if(result > 5) {
                if(hasPowerUp(PowerUps.LIZARD, myCar.powerups)) {
                    return LIZARD;
                } else {
                    result -= 5;
                }
            }
            if(result == 1) {
                return TURN_RIGHT;
            } else if (result == 2) {
                return TURN_LEFT;
            }
        }

        if (myCar.speed <= 3) {
            return ACCELERATE;
        }
/*        if (blocks.contains(Terrain.BOOST)) {
            return ACCELERATE;
        } */
        if (blocks.contains(Terrain.MUD)) {

                int result = doTurn(myCar, gameState);
            if(result > 5) {
                if(hasPowerUp(PowerUps.LIZARD, myCar.powerups)) {
                    return LIZARD;
                } else {
                    result -= 5;
                }
            }
                if(result == 1) {
                    return TURN_RIGHT;
                } else if (result == 2) {
                    return TURN_LEFT;
                }
        }
        if (blocks.contains(Terrain.WALL) || blocks.contains(Terrain.OIL_SPILL)) {
            int result = doTurn(myCar, gameState);
            if(result > 5) {
                if(hasPowerUp(PowerUps.LIZARD, myCar.powerups)) {
                    return LIZARD;
                } else {
                    result -= 5;
                }
            }
            if(result == 1) {
                return TURN_RIGHT;
            } else if (result == 2) {
                return TURN_LEFT;
            }
        }
        if (hasPowerUp(PowerUps.EMP, myCar.powerups) && (opponent.position.lane == myCar.position.lane || opponent.position.lane == myCar.position.lane - 1 || opponent.position.lane == myCar.position.lane + 1) && opponent.position.block >= myCar.position.block) {
            return EMP;
        }
        if (hasPowerUp(PowerUps.BOOST, myCar.powerups) && !myCar.boosting) {
            return BOOST;
        }
        if (myCar.speed > 8 && hasPowerUp(PowerUps.TWEET, myCar.powerups)) {
            if(!(opponent.position.lane == myCar.position.lane && opponent.position.block < myCar.position.block)) {
                return new TweetCommand(opponent.position.lane, opponent.position.block + opponent.speed + 5); }
        }
        if (myCar.damage >= 1) {
            return FIX;
        }
        if (myCar.speed > 8 && opponent.position.block < myCar.position.block && hasPowerUp(PowerUps.OIL, myCar.powerups)) {
            return OIL;
        }
        if (myCar.speed == 15 && hasPowerUp(PowerUps.EMP, myCar.powerups)) {
            return EMP;
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

    private Boolean hasPowerUp(PowerUps powerUpToCheck, PowerUps[] available) {
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
    private List<Object> getBlocksInFront(int lane, int block, GameState gameState, Car myCar) {
        List<Lane[]> map = gameState.lanes;
        List<Object> blocks = new ArrayList<>();
        int startBlock = map.get(0)[0].position.block;

        Lane[] laneList = map.get(lane - 1);
        for (int i = max(block - startBlock, 0); i <= block - startBlock + myCar.speed; i++) {
            if (laneList[i] == null || laneList[i].terrain == Terrain.FINISH) {
                break;
            }

            if(laneList[i].isOccupiedByCyberTruck) {
                blocks.add(100);
                continue;
            } else {
                blocks.add(laneList[i].terrain);
            }

        }

        return blocks;
    }

    private List<Object> getBlocksFrontRight(int lane, int block, GameState gameState, Car myCar) {
        List<Lane[]> map = gameState.lanes;
        List<Object> blocks = new ArrayList<>();
        int startBlock = map.get(0)[0].position.block;

        if(lane<=3){
            Lane[] laneList = map.get(lane);
            for (int i = max(block - startBlock, 0); i <= block - startBlock + myCar.speed; i++) {
                if (laneList[i] == null || laneList[i].terrain == Terrain.FINISH) {
                    break;
                }

                if(laneList[i].isOccupiedByCyberTruck) {
                    blocks.add(100);
                    continue;
                } else {
                    blocks.add(laneList[i].terrain);
                }
            }
        }


        return blocks;
    }

    private List<Object> getBlocksFrontLeft(int lane, int block, GameState gameState, Car myCar) {
        List<Lane[]> map = gameState.lanes;
        List<Object> blocks = new ArrayList<>();
        int startBlock = map.get(0)[0].position.block;

        if(lane-2>=0){
            Lane[] laneList = map.get(lane - 2);
            for (int i = max(block - startBlock, 0); i <= block - startBlock + myCar.speed; i++) {
                if (laneList[i] == null || laneList[i].terrain == Terrain.FINISH) {
                    break;
                }
                if(laneList[i].isOccupiedByCyberTruck) {
                    blocks.add(100);
                    continue;
                } else {
                    blocks.add(laneList[i].terrain);
                }

            }
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

    private int doTurn(Car myCar, GameState gameState){
        List<Object> blocksFront = getBlocksInFront(myCar.position.lane, myCar.position.block, gameState, myCar);
        List<Object> blocksLeft = getBlocksFrontLeft(myCar.position.lane, myCar.position.block, gameState, myCar);
        List<Object> blocksRight = getBlocksFrontRight(myCar.position.lane, myCar.position.block, gameState, myCar);
        List<Lane[]> map = gameState.lanes;
        Car opponent = gameState.opponent;

        boolean truckExists = checkTruck(myCar.position.lane, myCar.position.block, gameState, "front");
        boolean truckLeft = checkTruck(myCar.position.lane, myCar.position.block, gameState, "left");
        boolean truckRight = checkTruck(myCar.position.lane, myCar.position.block, gameState, "right");

        int LizardLeft = Collections.frequency(blocksLeft, Terrain.LIZARD);
        int LizardFront = Collections.frequency(blocksFront, Terrain.LIZARD);
        int LizardRight = Collections.frequency(blocksRight, Terrain.LIZARD);

        int TweetLeft = Collections.frequency(blocksLeft, Terrain.TWEET);
        int TweetFront = Collections.frequency(blocksFront, Terrain.TWEET);
        int TweetRight = Collections.frequency(blocksRight, Terrain.TWEET);

        int BOOSTLeft = Collections.frequency(blocksLeft, Terrain.BOOST);
        int BOOSTFront = Collections.frequency(blocksFront, Terrain.BOOST);
        int BOOSTRight = Collections.frequency(blocksRight, Terrain.BOOST);

        int WallLeft = Collections.frequency(blocksLeft, Terrain.WALL);
        int WallFront = Collections.frequency(blocksFront, Terrain.WALL);
        int WallRight = Collections.frequency(blocksRight, Terrain.WALL);

        int MudLeft = Collections.frequency(blocksLeft, Terrain.MUD);
        int MudFront = Collections.frequency(blocksFront, Terrain.MUD);
        int MudRight = Collections.frequency(blocksRight, Terrain.MUD);

        int OilLeft = Collections.frequency(blocksLeft, Terrain.OIL_SPILL);
        int OilFront = Collections.frequency(blocksFront, Terrain.OIL_SPILL);
        int OilRight = Collections.frequency(blocksRight, Terrain.OIL_SPILL);

        int TruckLeft = Collections.frequency(blocksLeft, 100);
        int TruckFront = Collections.frequency(blocksFront,100);
        int TruckRight = Collections.frequency(blocksRight, 100);

        double valueLeft = 0;
        double valueRight = 0;
        double valueFront = 0;

        if(truckExists) {
            valueFront -= 1500;
        }

        if(truckLeft) {
            valueLeft -= 1500;
        }

        if(truckRight) {
            valueRight -= 1500;
        }

        if((opponent.position.lane == myCar.position.lane) && opponent.position.block >= myCar.position.block && (opponent.position.block + opponent.speed) <= (myCar.position.block + myCar.speed) ) {
            valueFront -= 1000;
        }

        valueLeft += (LizardLeft * 1) + (BOOSTLeft * 2) + (TweetLeft * 0.5) - (100 * TruckLeft);
        valueRight += (LizardRight * 1)  + (BOOSTRight * 2) + (TweetRight * 0.5) - (100 * TruckRight);
        valueFront += (LizardFront * 1)  + (BOOSTFront * 2) + (TweetFront * 0.5) - (100 * TruckFront);

        valueLeft -= ((WallLeft * (3)) + (MudLeft * (1.5)) + (OilLeft * (1.5)));
        valueRight -= ((WallRight * (3)) + (MudRight * (1.5)) + (OilRight * (1.5)));
        valueFront -= ((WallFront * (3)) + (MudFront * (1.5)) + (OilFront * (1.5)));

        if(myCar.position.lane == 1) {
            if(valueRight > valueFront) {
                if(valueRight < 0) {
                    return 6;
                }
                return 1;
            } else {
                if(valueFront < 0) {
                    return 8;
                }
                return 3;
            }
        }

        if(myCar.position.lane == 4) {
            if(valueLeft > valueFront) {
                if(valueLeft < 0) {
                    return 7;
                }
                return 2;
            } else {
                if(valueFront < 0) {
                    return 8;
                }
                return 3;
            }
        }

        if(valueFront < 0 && valueLeft < 0 && valueRight < 0 ) {
            if(valueRight > valueLeft && valueRight > valueFront && myCar.position.lane != 4) {
                return 6;
            }

            if(valueLeft > valueRight && valueLeft > valueFront && myCar.position.lane != 1) {
                return 7;
            }

            if(valueFront > valueLeft && valueFront > valueRight) {
                return 8;
            }

            if(valueLeft == valueRight) {
                valueLeft += WallLeft * (-1) + BOOSTLeft * 1;
                valueRight += WallRight * (-1) + BOOSTRight * 1;
            }

            if(valueLeft == valueFront) {
                valueLeft += WallLeft * (-1) + BOOSTLeft * 1;
                valueFront += WallFront * (-1) + BOOSTFront * 1;
            }

            if(valueRight == valueFront) {
                valueRight += WallRight * (-1) + BOOSTRight * 1;
                valueFront += WallFront * (-1) + BOOSTFront * 1;
            }

            if(valueRight > valueLeft && valueRight > valueFront && myCar.position.lane != 4) {
                return 6;
            }

            if(valueLeft > valueRight && valueLeft > valueFront && myCar.position.lane != 1) {
                return 7;
            }

            if(valueFront > valueLeft && valueFront > valueRight) {
                return 8;
            }
            if(valueLeft == valueRight) {
                if(myCar.position.lane == 4) {
                    return 7;
                } else if (myCar.position.lane == 1) {
                    return 8;
                }
                return 7;
            }
        }

        if(valueRight > valueLeft && valueRight > valueFront && myCar.position.lane != 4) {
            return 1;
        }

        if(valueLeft > valueRight && valueLeft > valueFront && myCar.position.lane != 1) {
            return 2;
        }

        if(valueFront > valueLeft && valueFront > valueRight) {
            return 3;
        }

        if(valueLeft == valueRight) {
            valueLeft += WallLeft * (-1) + BOOSTLeft * 1;
            valueRight += WallRight * (-1) + BOOSTRight * 1;
        }

        if(valueLeft == valueFront) {
            valueLeft += WallLeft * (-1) + BOOSTLeft * 1;
            valueFront += WallFront * (-1) + BOOSTFront * 1;
        }

        if(valueRight == valueFront) {
            valueRight += WallRight * (-1) + BOOSTRight * 1;
            valueFront += WallFront * (-1) + BOOSTFront * 1;
        }

        if(valueRight > valueLeft && valueRight > valueFront && myCar.position.lane != 4) {
            if(valueRight < 0) {
                return 6;
            }
            return 1;
        }

        if(valueLeft > valueRight && valueLeft > valueFront && myCar.position.lane != 1) {
            if(valueLeft < 0) {
                return 7;
            }
            return 2;
        }

        if(valueFront > valueLeft && valueFront > valueRight) {
            if(valueFront < 0) {
                return 8;
            }
            return 3;
        }
        if(valueLeft == valueRight) {
            if(myCar.position.lane == 4) {
                return 2;
            } else if (myCar.position.lane == 1) {
                return 1;
            }
            return 2;
        }
        return 3;
    }
}
