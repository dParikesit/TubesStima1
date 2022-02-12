package za.co.entelect.challenge;

import za.co.entelect.challenge.command.*;
import za.co.entelect.challenge.entities.*;
import za.co.entelect.challenge.enums.PowerUps;
import za.co.entelect.challenge.enums.Terrain;

import java.util.*;

import static java.lang.Math.max;

import java.security.SecureRandom;

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

        //Fix first if too damaged to move
        if(myCar.damage == 5 || (myCar.damage>0 && myCar.speed==maxSpeed)) {
            return FIX;
        }

        //Accelerate first if going to slow
        if(myCar.speed <= 3) {
            return ACCELERATE;
        }

        if ((blocks.contains(Terrain.MUD) || blocks.contains(Terrain.WALL) || blocks.contains(Terrain.OIL_SPILL))) {
            if (hasPowerUp(PowerUps.LIZARD, myCar.powerups)) {
                return LIZARD;
            }

            return doTurn(myCar, gameState);
        }

        if(hasPowerUp(PowerUps.TWEET, myCar.powerups)){
            return new TweetCommand(opponent.position.lane, opponent.position.block+opponent.speed);
        }

        if(hasPowerUp(PowerUps.EMP, myCar.powerups) && myCar.position.block < opponent.position.block &&inCone(myCar, opponent)){
            return EMP;
        }

        if(opponent.position.lane==myCar.position.lane && myCar.position.block>opponent.position.block &&myCar.position.block <= opponent.position.block+opponent.speed){
            return OIL;
        }

        if(!(blocks.contains(Terrain.MUD) || blocks.contains(Terrain.WALL) || blocks.contains(Terrain.OIL_SPILL)) && hasPowerUp(PowerUps.BOOST, myCar.powerups)){
            return BOOST;
        }

        return ACCELERATE;
    }
    private Command doTurn(Car myCar, GameState gameState){
        List<Object> blocksLeft = getBlocksFrontLeft(myCar.position.lane, myCar.position.block, gameState);
        List<Object> blocksRight = getBlocksFrontRight(myCar.position.lane, myCar.position.block, gameState);
        if(myCar.position.lane!=1 && !(blocksLeft.contains(Terrain.MUD) || blocksLeft.contains(Terrain.OIL_SPILL) || blocksLeft.contains(Terrain.WALL))){
            return TURN_LEFT;
        }
        if(myCar.position.lane!=4 && !(blocksLeft.contains(Terrain.MUD) || blocksRight.contains(Terrain.OIL_SPILL) || blocksRight.contains(Terrain.WALL))){
            return TURN_RIGHT;
        }

        if(myCar.position.lane!=1 && !blocksLeft.contains(Terrain.WALL)){
            return TURN_LEFT;
        }
        if(myCar.position.lane!=4 && !blocksRight.contains(Terrain.WALL)){
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
    private List<Object> getBlocksFrontRight(int lane, int block, GameState gameState) {
        List<Lane[]> map = gameState.lanes;
        List<Object> blocks = new ArrayList<>();
        int startBlock = map.get(0)[0].position.block;

        if(lane<=3){
            Lane[] laneList = map.get(lane);
            for (int i = max(block - startBlock, 0); i <= block - startBlock + Bot.maxSpeed; i++) {
                if (laneList[i] == null || laneList[i].terrain == Terrain.FINISH) {
                    break;
                }

                blocks.add(laneList[i].terrain);

            }
        }
        return blocks;
    }

    private List<Object> getBlocksFrontLeft(int lane, int block, GameState gameState) {
        List<Lane[]> map = gameState.lanes;
        List<Object> blocks = new ArrayList<>();
        int startBlock = map.get(0)[0].position.block;

        if(lane-2>=0){
            Lane[] laneList = map.get(lane - 2);
            for (int i = max(block - startBlock, 0); i <= block - startBlock + Bot.maxSpeed; i++) {
                if (laneList[i] == null || laneList[i].terrain == Terrain.FINISH) {
                    break;
                }

                blocks.add(laneList[i].terrain);

            }
        }
        return blocks;
    }

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
