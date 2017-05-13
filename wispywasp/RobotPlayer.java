package wispywasp;
import battlecode.common.*;

public strictfp class RobotPlayer {

   // START declare instance variables
   static RobotController rc;
   static Direction gDir;
   
   // END

   @SuppressWarnings("unused")
   public static void run(RobotController rc) throws GameActionException {
   
      // START initialize instance variables
      RobotPlayer.rc = rc;  
      
      // END
         
      switch (rc.getType()) {
         case ARCHON:
            runArchon();
            break;
         case GARDENER:
            runGardener();
            break;
         case SOLDIER:
            runSoldier();
            break;
      }
   }
   
   static void runArchon() throws GameActionException {
      while (true) {
         try {
            Direction dir = randomDirection();
            if (rc.canHireGardener(dir) && Math.random() < .1) {
               rc.hireGardener(dir);
            }
            Direction northWest = new Direction((float)Math.PI * 3/4);
            tryMove(northWest);
            Clock.yield();    
         }
         catch (Exception e) {
            System.out.println("Archon Exception");
            e.printStackTrace();
         }
      }
   }
   
   static void runGardener() throws GameActionException {
      while (true) {
         try {
            RobotInfo[] friends = rc.senseNearbyRobots(-1, rc.getTeam());
            for (int i = 0; i < friends.length; i++) {
               if (friends[i].getType() == RobotType.ARCHON) {
                  Direction away = rc.getLocation().directionTo(friends[i].getLocation()).opposite();
                  tryMove(away);
               }
            }
            if (gDir == null){
               gDir = new Direction((float)Math.PI -3 / 4);
            }
            if (rc.canMove(gDir)) {
               tryMove(gDir);
            } else {
               gDir = gDir.opposite();
            }   
            Direction dir = randomDirection();
            if (rc.canBuildRobot(RobotType.SOLDIER, dir) && Math.random() < .3) {
               rc.buildRobot(RobotType.SOLDIER, dir);
            }
            Clock.yield();    
         }
         catch (Exception e) {
            System.out.println("Gardener Exception");
            e.printStackTrace();
         }
      }
   }
   static void runSoldier() throws GameActionException {
      Team enemy = rc.getTeam().opponent();
      while (true) {
         try {
            MapLocation myLocation = rc.getLocation();
            RobotInfo[] robots = rc.senseNearbyRobots(-1, enemy);
            if (robots.length > 0) {
               if (rc.canFireSingleShot()) {
                  rc.fireSingleShot(rc.getLocation().directionTo(robots[0].location));
               }
            } else {
               Direction myDir = new Direction((float)Math.PI / -4);
               if (rc.canMove(myDir)) tryMove(myDir);
            }
            Clock.yield();
         }
         catch (Exception e) {
            System.out.println("Soldier Exception");
            e.printStackTrace();
         }
      }
   }
   
   /**
    * Returns a random Direction
    * @return a random Direction
    */
   static Direction randomDirection() {
      return new Direction((float)Math.random() * 2 * (float)Math.PI);
   }
   /**
    * Attempts to move in a given direction, while avoiding small obstacles directly in the path.
    *
    * @param dir The intended direction of movement
    * @return true if a move was performed
    * @throws GameActionException
    */
   static boolean tryMove(Direction dir) throws GameActionException {
      return tryMove(dir,20,3);
   }
   /**
    * Attempts to move in a given direction, while avoiding small obstacles direction in the path.
    *
    * @param dir The intended direction of movement
    * @param degreeOffset Spacing between checked directions (degrees)
    * @param checksPerSide Number of extra directions checked on each side, if intended direction was unavailable
    * @return true if a move was performed
    * @throws GameActionException
    */
   static boolean tryMove(Direction dir, float degreeOffset, int checksPerSide) throws GameActionException {
   
      // First, try intended direction
      if (rc.canMove(dir)) {
         rc.move(dir);
         return true;
      }
   
      // Now try a bunch of similar angles
      boolean moved = false;
      int currentCheck = 1;
   
      while(currentCheck<=checksPerSide) {
         // Try the offset of the left side
         if(rc.canMove(dir.rotateLeftDegrees(degreeOffset*currentCheck))) {
            rc.move(dir.rotateLeftDegrees(degreeOffset*currentCheck));
            return true;
         }
         // Try the offset on the right side
         if(rc.canMove(dir.rotateRightDegrees(degreeOffset*currentCheck))) {
            rc.move(dir.rotateRightDegrees(degreeOffset*currentCheck));
            return true;
         }
         // No move performed, try slightly further
         currentCheck++;
      }
   
      // A move never happened, so return false.
      return false;
   }
}
