package za.co.entelect.challenge;

import za.co.entelect.challenge.command.*;
import za.co.entelect.challenge.entities.*;
import za.co.entelect.challenge.enums.PowerUps;
import za.co.entelect.challenge.enums.Terrain;

import java.util.*;

import static java.lang.Math.max;
import static java.lang.Math.min;

import java.security.SecureRandom;

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
        boolean truckExists = checkTruck(myCar.position.lane, myCar.position.block, gameState, "front", myCar);
        boolean truckLeft = checkTruck(myCar.position.lane, myCar.position.block, gameState, "left", myCar);
        boolean truckRight = checkTruck(myCar.position.lane, myCar.position.block, gameState, "right", myCar);
        //Basic fix logic
        List<Object> blocks = getBlocks(myCar.position.lane, myCar.position.block, gameState, myCar, "front");

        // Kalau damage >=2 difix karena terlalu lamban
        if (myCar.damage >= 2 && myCar.speed==maxSpeed) {
            return FIX;
        }

        // Cek apakah ada truck, kalau ada hindari atau loncati pakai lizard
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

        // Kalau kencang dan lane sebelah lebih hijau (ada powerup tanpa obstacle), pindah
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

        // Kalau lamban dan didepan ada lumpur, hindari
        if(myCar.speed <= 3 && (blocks.contains(Terrain.MUD) || blocks.contains(Terrain.WALL) || blocks.contains(Terrain.OIL_SPILL))) {
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

        // Kalau terlalu lamban, dipercepat
        if (myCar.speed <= 3) {
            if(hasPowerUp(PowerUps.BOOST, myCar.powerups) && myCar.damage == 0) {
                return BOOST;
            } else {
                return ACCELERATE;
            }
        }

        // Kalau didepan ada obstacle, hindarin
        if (blocks.contains(Terrain.MUD) || blocks.contains(Terrain.WALL) || blocks.contains(Terrain.OIL_SPILL)) {

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

        // Pakai EMP kalau kondisi oke
        if (hasPowerUp(PowerUps.EMP, myCar.powerups) && (opponent.position.lane == myCar.position.lane || opponent.position.lane == myCar.position.lane - 1 || opponent.position.lane == myCar.position.lane + 1) && opponent.position.block >= myCar.position.block && myCar.speed > 6) {
            return EMP;
        }
        if(myCar.speed == 9 && myCar.damage != 0) {
            return FIX;
        }
        // Pakai boost kalau punya dan ga lagi boost dan gak ada rintangan
        if (myCar.speed!= checkMax(myCar) && hasPowerUp(PowerUps.BOOST, myCar.powerups) && !myCar.boosting && !(blocks.contains(Terrain.MUD) || blocks.contains(Terrain.WALL) || blocks.contains(Terrain.OIL_SPILL))) {
            return BOOST;
        }

        // Kalau kencang dan punya tweet dan syarat kita didepan musuh, dan tidak dijalur sama, pakai tweet
        if (myCar.speed > 8 && hasPowerUp(PowerUps.TWEET, myCar.powerups)) {
            if(!(opponent.position.lane == myCar.position.lane && opponent.position.block < myCar.position.block)) {
//                return new TweetCommand(opponent.position.lane, opponent.position.block + opponent.speed + 5);
                return new TweetCommand(opponent.position.lane, opponent.position.block + opponent.speed);
            }
        }

        // Taruh oil
        if (myCar.speed == 15 && opponent.position.block < myCar.position.block && hasPowerUp(PowerUps.OIL, myCar.powerups)) {
            return OIL;
        }

        return ACCELERATE;
    }

    // Check max speed
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

    // Check punya powerup tertentu
    private Boolean hasPowerUp(PowerUps powerUpToCheck, PowerUps[] available) {
        for (PowerUps powerUp: available) {
            if (powerUp.equals(powerUpToCheck)) {
                return true;
            }
        }
        return false;
    }

    // Cek block di depan sesuai arah
    private List<Object> getBlocks(int lane, int block, GameState gameState, Car myCar, String direction) {
        List<Lane[]> map = gameState.lanes;
        List<Object> blocks = new ArrayList<>();
        int startBlock = map.get(0)[0].position.block;

        Lane[] laneList;

        if(direction.equals("right")){
            if(lane>3){
                return blocks;
            }
            laneList = map.get(lane);
        } else if(direction.equals("left")){
            if(lane-2<0){
                return blocks;
            }
            laneList = map.get(lane - 2);
        } else{
            laneList = map.get(lane - 1);
        }

        for (int i = max(block - startBlock, 0); i <= block - startBlock + myCar.speed + getAccelerate(myCar.speed, hasPowerUp(PowerUps.BOOST, myCar.powerups)); i++) {
            if (laneList[i] == null || laneList[i].terrain == Terrain.FINISH) {
                break;
            }
            blocks.add(laneList[i].terrain);
        }
        return blocks;
    }

    // Cek apakah ada cybertruck di arah direction
    private boolean checkTruck(int lane, int block, GameState gameState, String direction, Car myCar) {
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

        for (int i = max(block - startBlock -5, 0); i <= min(block - startBlock + myCar.speed + getAccelerate(myCar.speed, hasPowerUp(PowerUps.BOOST, myCar.powerups)), 1500); i++) {
            if (laneList[i] == null || laneList[i].terrain == Terrain.FINISH) {
                break;
            }

            if(laneList[i].isOccupiedByCyberTruck){
                return true;
            }

        }
        return false;
    }

    // Lakukan turn
    private int doTurn(Car myCar, GameState gameState){
        // Melakukan pembobotan lane depan, kiri, kanan untuk mendapatkan arah terbaik saat ini
        List<Object> blocksFront = getBlocks(myCar.position.lane, myCar.position.block, gameState, myCar, "front");
        List<Object> blocksLeft = getBlocks(myCar.position.lane, myCar.position.block, gameState, myCar, "left");
        List<Object> blocksRight = getBlocks(myCar.position.lane, myCar.position.block, gameState, myCar, "right");
        Car opponent = gameState.opponent;

        boolean truckExists = checkTruck(myCar.position.lane, myCar.position.block, gameState, "front", myCar);
        boolean truckLeft = checkTruck(myCar.position.lane, myCar.position.block, gameState, "left", myCar);
        boolean truckRight = checkTruck(myCar.position.lane, myCar.position.block, gameState, "right", myCar);

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

        if((opponent.position.lane == myCar.position.lane) && opponent.position.block >= myCar.position.block && (opponent.position.block + opponent.speed + getAccelerate(opponent.speed, false)) <= (myCar.position.block + myCar.speed + getAccelerate(myCar.speed, hasPowerUp(PowerUps.BOOST, myCar.powerups))) ) {
            valueFront -= 1000;
        }
        double wall = 0;
        double mud = 0;
        double oil = 0;
        if(myCar.boosting) {
            wall = 5;
            mud = 5;
            oil = 5;
        } else {
            wall = 3;
            mud = 1.5;
            oil = 1.5;
        }

        valueLeft += (LizardLeft * 1) + (BOOSTLeft * 2) + (TweetLeft * 0.5) - (100 * TruckLeft);
        valueRight += (LizardRight * 1)  + (BOOSTRight * 2) + (TweetRight * 0.5) - (100 * TruckRight);
        valueFront += (LizardFront * 1)  + (BOOSTFront * 2) + (TweetFront * 0.5) - (100 * TruckFront);

        valueLeft -= ((WallLeft * (wall)) + (MudLeft * (mud)) + (OilLeft * (oil)));
        valueRight -= ((WallRight * (wall)) + (MudRight * (mud)) + (OilRight * (oil)));
        valueFront -= ((WallFront * (wall)) + (MudFront * (mud)) + (OilFront * (oil)));

        return getNumber(valueFront, valueLeft, valueRight, WallLeft, WallRight, WallFront, myCar, BOOSTLeft, BOOSTRight, BOOSTFront);

    }

    // Return angka berdasarkan pembobotan method doTurn
    // Arti angka
    // 1. Belok kanan
    // 2. Belok kiri
    // 3. Lurus terus
    // 6. Jika punya lizard, pakai lizard. Jika tidak belok kanan
    // 7. Sama seperti 6 tapi belok kiri
    // 8. Sama seperti 6 tapi lurus
    // 4 dan 5 tidak ada karena pembuat phobia angka 4 dan 5. Semoga dengan begini tidak sial
    private int getNumber(double valueFront, double valueLeft, double valueRight, int WallLeft, int WallRight, int WallFront, Car myCar, int BOOSTLeft, int BOOSTRight, int BOOSTFront) {
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

            if(valueLeft == valueFront) {
                return 8;
            }

            if(valueRight == valueFront) {
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

    private int getAccelerate(int speedNow, boolean x) {
        if(speedNow == 0) {
            return 3;
        } else if (speedNow == 3 || speedNow == 6) {
            return 2;
        } else if (speedNow == 5 || speedNow == 8) {
            return 1;
        } else {
            if(speedNow == 9) {
                if(x) {
                    return 7;
                } else {
                    return 0;
                }
            } else {
                return 0;
            }
        }
    }
}