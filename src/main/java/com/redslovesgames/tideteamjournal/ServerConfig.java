/* RECONSTRUCTED SOURCE BASELINE */
package com.redslovesgames.tideteamjournal;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.util.Identifier;

public final class ServerConfig {
   private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
   private static volatile Values values = Values.defaults();
   private ServerConfig() {}
   public static Values get(){return values;}
   public static boolean load(){
      Values previous=values; Path path=path();
      try { if(!Files.exists(path)){Files.createDirectories(path.getParent());Files.writeString(path,GSON.toJson(Values.defaults()));}
         Values parsed=GSON.fromJson(Files.readString(path),Values.class); values=validate(parsed); return true;
      } catch(IOException|JsonParseException|IllegalArgumentException e){values=previous;TideTeamJournal.LOGGER.error("Could not load {}; retaining the last valid settings",path,e);return false;}
   }
   private static Path path(){return FabricLoader.getInstance().getConfigDir().resolve("tide_team_journal-server.json");}
   static Values validate(Values raw){
      if(raw==null) throw new IllegalArgumentException("Configuration is empty");
      raw.historyLimit=Math.max(0,Math.min(10000,raw.historyLimit));
      raw.visibleMetrics=raw.visibleMetrics!=null&&!raw.visibleMetrics.isEmpty()?raw.visibleMetrics.stream().filter(m->List.of("catches","species","record_events","active_records","fish_score").contains(m)).distinct().toList():List.of("catches","species","record_events","active_records","fish_score");
      raw.repairMinimumRank="owner".equalsIgnoreCase(raw.repairMinimumRank)?"owner":"officer";
      raw.claimAllMinimumRank="officer".equalsIgnoreCase(raw.claimAllMinimumRank)?"officer":"owner";
      raw.bobberBonuses=raw.bobberBonuses==null?new LinkedHashMap<>():raw.bobberBonuses;
      migrateLegacyBobberDefaults(raw.bobberBonuses);
      Map<String,BobberBonuses.Bonus> validated=new LinkedHashMap<>();
      raw.bobberBonuses.forEach((id,b)->{if(Identifier.tryParse(id)!=null&&b!=null)validated.put(id,clamp(b));else TideTeamJournal.LOGGER.warn("Ignoring invalid bobber bonus entry {}",id);});
      raw.bobberBonuses=validated; raw.fallbackBobberBonus=clamp(raw.fallbackBobberBonus==null?new BobberBonuses.Bonus(0,1):raw.fallbackBobberBonus);
      tideborneEnsureFishScoreMetric(raw); return raw;
   }
   private static void migrateLegacyBobberDefaults(Map<String,BobberBonuses.Bonus> map){
      migrate(map,"tide:golden_apple_bobber",1,1,2,2); migrate(map,"tide:enchanted_golden_apple_bobber",2,2,5,0);
      migrate(map,"tide:iron_bobber",0,2,0,0); migrate(map,"tide:diamond_bobber",1,2,0,0); migrate(map,"tide:netherite_bobber",2,2,0,1);
      migrate(map,"tide:amethyst_bobber",2,0,0,1); migrate(map,"tide:echo_bobber",0,3,0,1); migrate(map,"tide:chorus_bobber",1,1,0,3);
      migrate(map,"tide:feather_bobber",0,3,0,2); migrate(map,"tide:lichen_bobber",0,2,0,1); migrate(map,"tide:nautilus_bobber",2,0,1,0);
      migrate(map,"tide:heart_bobber",3,0,0,0); migrate(map,"tide:heart_bobber",1,2,0,0); migrate(map,"tide:grassy_bobber",1,1,0,0); migrate(map,"tide:duck_bobber",0,2,0,0);
   }
   private static void migrate(Map<String,BobberBonuses.Bonus> map,String id,int oldLuck,int oldLure,int newLuck,int newLure){BobberBonuses.Bonus b=map.get(id);if(b!=null&&b.luck()==oldLuck&&b.lureSpeed()==oldLure)map.put(id,new BobberBonuses.Bonus(newLuck,newLure));}
   private static BobberBonuses.Bonus clamp(BobberBonuses.Bonus b){return new BobberBonuses.Bonus(Math.max(0,Math.min(10,b.luck())),Math.max(0,Math.min(10,b.lureSpeed())));}
   private static void tideborneEnsureFishScoreMetric(Values c){ArrayList<String> m=new ArrayList<>(c.visibleMetrics);if(!m.contains("fish_score"))m.add("fish_score");c.visibleMetrics=m;}

   public static final class Values {
      public boolean leaderboardEnabled=true,historyEnabled=true,contributionTracking=true,announcementsEnabled=true,recordBadgesEnabled=true,recordTooltipsEnabled=true,operatorBypass=true,membersMayClaimWithExactFish=true;
      public int historyLimit=200; public String repairMinimumRank="officer",claimAllMinimumRank="owner";
      public List<String> visibleMetrics=List.of("catches","species","record_events","active_records","fish_score");
      public boolean trackDiscoveries=true,trackLargestRecords=true,trackSmallestRecords=true,trackRepairs=true,bobberBonusesEnabled=true;
      public BobberBonuses.Bonus fallbackBobberBonus=new BobberBonuses.Bonus(0,1); public Map<String,BobberBonuses.Bonus> bobberBonuses=defaultBobbers();
      static Values defaults(){return new Values();}
      private static Map<String,BobberBonuses.Bonus> defaultBobbers(){
         Map<String,BobberBonuses.Bonus> result=new LinkedHashMap<>();
         for(var id:com.redslovesgames.tideborne.fishing.v2.FishingGearRegistry.supportedBobberIds()){
            var gear=com.redslovesgames.tideborne.fishing.v2.FishingGearRegistry.bobberModifiers(id).orElseThrow();
            result.put(id.toString(),new BobberBonuses.Bonus((int)gear.fishingLuck(),(int)gear.namedAdditiveModifier(com.redslovesgames.tideborne.fishing.v2.FishingGearEffects.LURE_BONUS)));
         }
         return result;
      }
   }
}