package wispywasp;
import battlecode.common.*;

public strictfp class RobotPlayer {
  // START declare instance variables
   static RobotController rc;
   static Direction NORTHWEST;
   static Direction SOUTHWEST;
   static Direction NORTHEAST;
   static Direction SOUTHEAST;
   static Direction gDir;
   static Direction sDir;
   static Team enemy;
   // END

   @SuppressWarnings("unused")
   public static void run(RobotController rc) throws GameActionException {
     // START initialize instance variables
      RobotPlayer.rc = rc;   
      NORTHWEST = new Direction((float)Math.PI * 3/4);
      SOUTHWEST = new Direction((float)Math.PI * -3/4);
      NORTHEAST = SOUTHWEST.opposite();
      SOUTHEAST = NORTHWEST.opposite();
      gDir = NORTHEAST;
      sDir = SOUTHEAST;
      enemy = rc.getTeam().opponent();
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
            // START declare (& initialize?) local variables
            Direction dir = randomDirection();
            //END
            if (rc.canHireGardener(dir) && Math.random() < .5) {
               rc.hireGardener(dir);
            }
            tryMove(randomDirection());
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
            // START declare (& initialize?) local variables
            RobotInfo[] friends = rc.senseNearbyRobots(-1, rc.getTeam());
            Direction away;
            // END
            for (int i = 0; i < friends.length; i++) {
               if (friends[i].getType() == RobotType.ARCHON) {
                  away = rc.getLocation().directionTo(friends[i].getLocation()).opposite();
                  tryMove(away);
               }
            }

            if (rc.canMove(gDir)) {
               tryMove(gDir);
            } 
            else {
               gDir = gDir.opposite();
            }
         
            if (rc.canBuildRobot(RobotType.SOLDIER, SOUTHEAST) && Math.random() < .9) {
               rc.buildRobot(RobotType.SOLDIER, SOUTHEAST);
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
      
      while (true) {
         try {
            // START declare (& initialize?) local variables
            MapLocation myLocation = rc.getLocation();
            RobotInfo[] robots = rc.senseNearbyRobots(-1, enemy);
            // END
            if (robots.length > 0) {
               if (rc.canFireTriadShot()) {
                  rc.fireTriadShot(rc.getLocation().directionTo(robots[0].location));
               }
            } 
            else {
               if (rc.canMove(sDir)) {
                  tryMove(sDir);
               } 
               else if (Math.random() < 0.5) {
                  sDir = sDir.rotateLeftDegrees(90 + (float)Math.random() * 10);
                  tryMove(sDir);
               } 
               else {
                  sDir = sDir.rotateRightDegrees(90 + (float)Math.random() * 10);
                  tryMove(sDir);
               }
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
