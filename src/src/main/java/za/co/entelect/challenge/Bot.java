package za.co.entelect.challenge;

import za.co.entelect.challenge.command.*;
import za.co.entelect.challenge.entities.*;
import za.co.entelect.challenge.enums.PowerUps;
import za.co.entelect.challenge.enums.Terrain;

import java.util.*;

import static java.lang.Math.max;

public class Bot {

    private static final int maxSpeed = 9;
    private List<Command> directionList = new ArrayList<>();

    private Random random;
    private GameState gameState;
    private Car opponent;
    private Car myCar;
    private int maxRound;
    private int currentRound;
    private final static Command FIX = new FixCommand();
    private final static Command ACCELERATE = new AccelerateCommand();
    private final static Command LIZARD = new LizardCommand();
    private final static Command OIL = new OilCommand();
    private final static Command BOOST = new BoostCommand();
    private final static Command EMP = new EmpCommand();

    private final static Command TURN_RIGHT = new ChangeLaneCommand(1);
    private final static Command TURN_LEFT = new ChangeLaneCommand(-1);

    public Bot(Random random, GameState gameState) {
        this.random = new Random();
        this.gameState = gameState;
        this.myCar = gameState.player;
        this.opponent = gameState.opponent;

        // this.maxRound = gameState.maxRounds;
        // this.currentRound = gameState.currentRound;

        directionList.add(TURN_RIGHT);
        directionList.add(TURN_LEFT);
    }

    public Command run() {
        List<Object> blocks = getBlocksInFront(myCar.position.lane, myCar.position.block);
        List<Object> nextBlocks = blocks.subList(0, 1);
        if (myCar.damage >= 2) {
            return FIX;
        }
        if (myCar.speed <= 3) {
            return ACCELERATE;
        }
        if (blocks.contains(Terrain.BOOST)) {
            return ACCELERATE;
        }
        if (blocks.contains(Terrain.MUD)) {
            if (hasPowerUp(PowerUps.LIZARD, myCar.powerups)) {
                return LIZARD;
            } else {
                return chooseLane(myCar.position.lane, directionList);
            }
        }
        if (blocks.contains(Terrain.WALL) || blocks.contains(Terrain.OIL_SPILL)) {
            return chooseLane(myCar.position.lane, directionList);
        }
        if (hasPowerUp(PowerUps.BOOST, myCar.powerups)) {
            return BOOST;
        }
        if (hasPowerUp(PowerUps.EMP, myCar.powerups) && opponent.position.lane == myCar.position.lane && opponent.position.block >= myCar.position.block) {
            return EMP;
        }
        if (myCar.damage >= 1) {
            return FIX;
       }
        if (myCar.speed > 8 && hasPowerUp(PowerUps.TWEET, myCar.powerups)) {
            return new TweetCommand(opponent.position.lane, opponent.position.block + 10);
        }
        if (myCar.speed > 8 && opponent.position.block < myCar.position.block && hasPowerUp(PowerUps.OIL, myCar.powerups)) {
            return OIL;
        }
        if (myCar.speed == 15 && hasPowerUp(PowerUps.EMP, myCar.powerups)) {
            return EMP;
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
     * Returns map of blocks and the objects in the for the current lanes, returns the amount of blocks that can be
     * traversed at max speed.
     **/
    private List<Object> getBlocksInFront(int lane, int block) {
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

    private Command chooseLane(int lane, List<Command> directionList) {
        if (lane == 1) {
            return TURN_RIGHT;
        } else if (lane == 4) {
            return TURN_LEFT;
        } else {
            int i = random.nextInt(directionList.size());
            return directionList.get(i);
        }
    }
}
