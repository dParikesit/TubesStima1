package za.co.entelect.challenge;

import za.co.entelect.challenge.command.*;
import za.co.entelect.challenge.entities.*;
import za.co.entelect.challenge.enums.PowerUps;
import za.co.entelect.challenge.enums.Terrain;

import java.util.*;

import static java.lang.Math.max;

import java.security.SecureRandom;

import java.util.Arrays;

public class Bot {

    private static final int maxSpeed = 9;
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

    public int[] arrTwt = {};
    public void ArrayTweet(int index) {
        this.arrTwt = Arrays.copyOf(this.arrTwt, this.arrTwt.length + 1);
        this.arrTwt[this.arrTwt.length - 1] = index;
    }

    public Bot() {
        this.random = new SecureRandom();
        directionList.add(TURN_LEFT);
        directionList.add(TURN_RIGHT);
    }

    public Command run(GameState gameState) {
        Car myCar = gameState.player;
        Car opponent = gameState.opponent;
        //Basic fix logic
        List<Object> blocks = getBlocksInFront(myCar.position.lane, myCar.position.block, gameState);
        List<Object> nextBlocks = blocks.subList(0,1);

        if(myCar.damage != 0) {
            return FIX;
        }

        if(!(blocks.contains(Terrain.MUD) || blocks.contains(Terrain.WALL) || blocks.contains(Terrain.OIL_SPILL)) && hasPowerUp(PowerUps.BOOST, myCar.powerups)){
            if(myCar.damage == 0 && myCar.speed < 7) {
                return BOOST;
            }
        }

        if(hasPowerUp(PowerUps.TWEET, myCar.powerups)){
            ArrayTweet(opponent.position.lane);
            return new TweetCommand(opponent.position.lane, opponent.position.block+1);
        }

        if(myCar.speed < 7) {
            return ACCELERATE;
        }

        if(Arrays.asList(arrTwt).contains(myCar.position.lane)) {
            if(myCar.position.lane == 1) {
                return TURN_RIGHT;
            }
            if(myCar.position.lane == 4) {
                return TURN_LEFT;
            }
            if(!Arrays.asList(arrTwt).contains(myCar.position.lane + 1)) {
                return TURN_RIGHT;
            }
            if(!Arrays.asList(arrTwt).contains(myCar.position.lane - 1)) {
                return TURN_LEFT;
            }
        }

        if ((blocks.contains(Terrain.WALL))) {
            if (hasPowerUp(PowerUps.LIZARD, myCar.powerups)) {
                return LIZARD;
            }
            if(myCar.position.lane == 1) {
                return TURN_RIGHT;
            }
            if(myCar.position.lane == 4) {
                return TURN_LEFT;
            }
            if(myCar.position.lane == 3) {
                return TURN_LEFT;
            }
            if(myCar.position.lane == 2) {
                return TURN_RIGHT;
            }
        }

        if(hasPowerUp(PowerUps.BOOST, myCar.powerups)) {
            return BOOST;
        }

        if ((blocks.contains(Terrain.MUD) || blocks.contains(Terrain.WALL))) {
            if (hasPowerUp(PowerUps.LIZARD, myCar.powerups)) {
                return LIZARD;
            }
            if(myCar.position.lane == 1) {
                return TURN_RIGHT;
            }
            if(myCar.position.lane == 4) {
                return TURN_LEFT;
            }
            if(myCar.position.lane == 3) {
                return TURN_LEFT;
            }
            if(myCar.position.lane == 2) {
                return TURN_RIGHT;
            }
        }

        return ACCELERATE;
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
    private List<Object> getBlocksInFront(int lane, int block, GameState gameState) {
        List<Lane[]> map = gameState.lanes;
        List<Object> blocks = new ArrayList<>();
        int startBlock = map.get(0)[0].position.block;

        Lane[] laneList = map.get(lane - 1);
        for (int i = max(block - startBlock, 0); i <= block - startBlock + Bot.maxSpeed; i++) {
            if (laneList[i] == null || laneList[i].terrain == Terrain.FINISH) {
                break;
            }

            blocks.add(laneList[i].terrain);

        }
        return blocks;
    }

}
