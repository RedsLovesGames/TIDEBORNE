package com.redslovesgames.tideborne.fishing.simulation;

import com.redslovesgames.tideborne.fishing.*;
import com.redslovesgames.tideborne.fishing.gear.*;
import com.redslovesgames.tideborne.fishing.specimen.*;
import com.redslovesgames.tideborne.fishing.tide.TideFishingLineModifiers;
import com.redslovesgames.tideborne.fishing.gametest.GearArchetypeCases;
import com.redslovesgames.tideborne.fishing.gear.*;
import com.redslovesgames.tideborne.fishing.tide.TideFishingLineModifiers;
import java.nio.file.*;
import java.util.*;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import static com.redslovesgames.tideborne.fishing.gear.FishingGearRegistry.GearProfile.*;

/** Explicit aggregate report job. Never part of ordinary test/build. */
@Tag("gear-balance-report")
class PostGearBalanceReport {
    private static final int N=100_000;
    private final StringBuilder out=new StringBuilder();
    private final FishingSimulator simulator=new FishingSimulator();
    private final SpeciesSelectionService selection=new SpeciesSelectionService();
    private final FightProfileService fights=new FightProfileService();
    private void row(Object... values) { out.append("| ");for(Object v:values)out.append(v instanceof Double?String.format(Locale.ROOT,"%.5f",v):v).append(" | ");out.append('\n'); }
    private void table(String title,String header) {out.append("\n## ").append(title).append("\n\n").append(header).append('\n');row(Arrays.stream(header.split("\\|")).filter(x->!x.isBlank()).map(x->"---").toArray());}
    private double exactShare(List<SpeciesProfile> pool,PostGearBalanceScenarios.Scenario s,String role) {
        double total=0,matched=0;
        for(var p:pool){double w=selection.adjustedWeight(p,s.nativeLuck(),FishingEnvironment.empty(),s.gear());total+=w;if(p.targetTags().contains(role))matched+=w;}
        return matched/total;
    }
    @Test void writeAggregateReport() throws Exception {
        out.append("# Generated deterministic gear measurements\n\nSeeds: ").append(Arrays.toString(PostGearBalanceScenarios.SEEDS)).append("; 100000 catches per seed/run; 200000 per scenario. Rates are fractions.\n");
        var builds=PostGearBalanceScenarios.builds();var scenarios=new ArrayList<>(builds);scenarios.addAll(PostGearBalanceScenarios.ablations());
        table("Encounter results: ordinary controlled pool","| Scenario | 4-5 star share | Heavy | Very small | Warm | Deep | Heavy seed spread | Natural mean | Max decile deviation from .1 | Score mean | Score min/max |");
        for(var s:scenarios){
            var results=new ArrayList<FishingSimulator.Result>();
            for(long seed:PostGearBalanceScenarios.SEEDS)results.add(simulator.simulate(PostGearBalanceScenarios.pool(false),FishingEnvironment.empty(),PostGearBalanceScenarios.config(seed,N,s.nativeLuck(),0),s.gear()));
            double[] heavy=results.stream().mapToDouble(r->PostGearBalanceTest.share(r,"heavy")).toArray();
            for(double h:heavy)assertEquals(exactShare(PostGearBalanceScenarios.pool(false),s,"heavy"),h,.006,s.name());
            double decile=0;for(var r:results)for(long bucket:r.naturalPercentiles().buckets())decile=Math.max(decile,Math.abs(bucket/(double)N-.1));
            row(s.name(),results.stream().mapToDouble(r->r.rate(r.rarityCounts().get(CanonicalRarity.FOUR_STAR)+r.rarityCounts().get(CanonicalRarity.FIVE_STAR))).average().orElseThrow(),
                    meanShare(results,"heavy"),meanShare(results,"very_small"),meanShare(results,"warm"),meanShare(results,"deep"),Math.abs(heavy[0]-heavy[1]),
                    results.stream().mapToDouble(r->r.naturalPercentiles().mean()).average().orElseThrow(),decile,
                    results.stream().mapToDouble(r->r.fishScores().mean()).average().orElseThrow(),results.stream().mapToInt(r->r.fishScores().min()).min().orElseThrow()+"/"+results.stream().mapToInt(r->r.fishScores().max()).max().orElseThrow());
            assertTrue(decile<.006,s.name());
        }
        table("Trait isolation: identical fixed three-star species/seeds","| Scenario | Perfect Catch input | Body event | Condition event | Pigment event | Perfect specimen | Score mean | Natural mean | Exact paired natural histogram? |");
        var fixed=List.of(PostGearBalanceScenarios.species("fixed",CanonicalRarity.THREE_STAR,Set.of(),1,1,1));
        for(double pc:new double[]{0,.1}) {
            var reference=simulator.simulate(fixed,FishingEnvironment.empty(),PostGearBalanceScenarios.config(PostGearBalanceScenarios.SEEDS[0],N,0,pc));
            for(var s:builds){var rs=new ArrayList<FishingSimulator.Result>();
                for(long seed:PostGearBalanceScenarios.SEEDS){var r=simulator.simulate(fixed,FishingEnvironment.empty(),PostGearBalanceScenarios.config(seed,N,s.nativeLuck(),pc),s.gear());rs.add(r);
                    if(seed==PostGearBalanceScenarios.SEEDS[0])assertEquals(reference.naturalPercentiles(),r.naturalPercentiles(),s.name());}
                row(s.name(),pc,rs.stream().mapToDouble(r->1-r.rate(r.bodyTypeCounts().get(SpecimenData.BodyType.NORMAL))).average().orElseThrow(),
                        rs.stream().mapToDouble(r->1-r.rate(r.conditionCounts().get(SpecimenData.Condition.NORMAL))).average().orElseThrow(),
                        rs.stream().mapToDouble(r->1-r.rate(r.pigmentationCounts().get(SpecimenData.Pigmentation.NORMAL))).average().orElseThrow(),
                        rs.stream().mapToDouble(r->r.rate(r.qualityCounts().get(SpecimenData.SpecimenQuality.PERFECT_SPECIMEN))).average().orElseThrow(),
                        rs.stream().mapToDouble(r->r.fishScores().mean()).average().orElseThrow(),reference.naturalPercentiles().mean(),"yes");
            }
        }
        table("Boss sensitivity: exact probabilities, not shipped encounters","| Scenario | Shipped boss share | Hypothetical boss share | Boss gear ratio | Non-target ordinary 1-star gear ratio | FL | TL | S | T | P |");
        for(var s:scenarios)if(Set.of("Baseline","Kujira + Shark","Leviathan bait only","Leviathan").contains(s.name())) {
            var boss=PostGearBalanceScenarios.pool(true).stream().filter(p->p.targetTags().contains("boss")).findFirst().orElseThrow();
            row(s.name(),0,exactShare(PostGearBalanceScenarios.pool(true),s,"boss"),FishingGearEffects.speciesWeightMultiplier(boss,FishingEnvironment.empty(),s.gear(),s.nativeLuck()),
                    FishingGearEffects.speciesWeightMultiplier(PostGearBalanceScenarios.pool(false).getFirst(),FishingEnvironment.empty(),s.gear(),s.nativeLuck()),FishingGearEffects.fishingLuck(s.gear()),FishingGearEffects.traitLuck(s.gear()),FishingGearEffects.strengthMultiplier(s.gear()),FishingGearEffects.tempoMultiplier(s.gear()),FishingGearEffects.catchLossPreventionChance(s.gear()));
        }
        fightGrid(builds);safety();timing();caps(builds);dominance(builds);crateAndBait();
        Path path=Path.of(System.getProperty("tideborne.balance.output","build/gear-balance-results.md"));Files.createDirectories(path.toAbsolutePath().getParent());Files.writeString(path,out);
    }
    private double meanShare(List<FishingSimulator.Result> rs,String role){return rs.stream().mapToDouble(r->PostGearBalanceTest.share(r,role)).average().orElseThrow();}
    private void fightGrid(List<PostGearBalanceScenarios.Scenario> builds){
        table("Fight grid: means over 54 fixed specimens per build","| Build | S effective | T effective | Zone projected | Speed projected | Strength surcharge relieved | Specimen/score mutations |");
        for(var s:builds){double strength=0,tempo=0,zone=0,speed=0,relief=0;int n=0;
            for(double speciesStrength:new double[]{.5,1,2})for(double percentile:new double[]{10,50,99})for(var body:SpecimenData.BodyType.values())for(var quality:SpecimenData.SpecimenQuality.values()){
                var p=PostGearBalanceScenarios.species("fight",CanonicalRarity.THREE_STAR,Set.of(),1,speciesStrength,speciesStrength);
                double length=p.sizeDistribution().quantile(percentile/100);
                var base=new SpecimenData(p.speciesId(),2,1,773,percentile,length,length,percentile,SpecimenData.BodyType.NORMAL,SpecimenData.Condition.NORMAL,SpecimenData.Pigmentation.NORMAL,quality,false,OptionalDouble.empty(),OptionalInt.empty(),SpecimenData.Provenance.generated());
                var specimen=new BodyTypeGenerator().applyPhysicalSize(p,base,body);var before=specimen.toString();var score=new FishScoreV2Service().calculate(p.rarity(),specimen);
                var raw=fights.create(p,specimen);var relieved=fights.applyTrophyRelief(fights.speciesBaseline(p),raw,s.gear());
                assertEquals(raw.strength()-Math.max(0,raw.strength()-fights.speciesBaseline(p).strength())*FishingGearEffects.trophyFightRelief(s.gear()),relieved.strength(),1e-12);
                var fight=fights.create(p,specimen,s.gear());var projected=fights.projectMinigame(p,specimen,s.gear(),1);
                assertEquals(before,specimen.toString());assertEquals(score,new FishScoreV2Service().calculate(p.rarity(),specimen));assertTrue(score.fishScore()>=1&&score.fishScore()<=3000);
                strength+=fight.strength();tempo+=fight.tempo();zone+=projected.catchZoneArea();speed+=projected.speed();relief+=raw.strength()-relieved.strength();n++;
            }row(s.name(),strength/n,tempo/n,zone/n,speed/n,relief/n,0);
        }
    }
    private void safety(){
        table("Leader curve: isolated versus Netherite/Copper/Netherite safety chassis","| Leader | Isolated P | Isolated Z | Isolated V | Chassis P | Chassis Z | Chassis V | 200000 threat trials prevented |");
        for(var tier:LeaderTier.values()){
            var leader=LeaderGearModifiers.forTier(tier,true);var chassis=FishingGearModifiers.compose(NETHERITE_ROD.rodModifiers(),TideFishingLineModifiers.forProfile(TIDE_COPPER_LINE),FishingGearRegistry.bobberModifiers(net.minecraft.util.Identifier.of("tide","netherite_bobber")).orElseThrow(),leader);
            int prevented=0;for(long seed:PostGearBalanceScenarios.SEEDS){var random=new SplittableRandom(seed);for(int i=0;i<N;i++)if(FishingGearEffects.preventsCatchLoss(chassis,random::nextDouble))prevented++;}
            double rate=prevented/(2.0*N);assertEquals(FishingGearEffects.catchLossPreventionChance(chassis),rate,.005);
            row(tier,FishingGearEffects.catchLossPreventionChance(leader),FishingGearEffects.catchZoneAreaMultiplier(leader),FishingGearEffects.minigameSpeedMultiplier(leader),FishingGearEffects.catchLossPreventionChance(chassis),FishingGearEffects.catchZoneAreaMultiplier(chassis),FishingGearEffects.minigameSpeedMultiplier(chassis),rate);
        }
    }
    private void timing(){
        table("Water timing: exact wait enumeration, illustrative fixed-fight cycles","| Setup | Lure | Wait ticks | Wait ratio vs baseline | Cycle seconds (10s fight) | Throughput ratio (10s fight) | Throughput ratio (30s fight) |");
        String[] names={"Baseline","Swift only","Chorus replaces Red","Normal bait + Red","Full Fast"};int[] lures={1,1,3,3,5};
        double base=PostGearBalanceScenarios.expectedWait(1)+50;
        for(int i=0;i<names.length;i++){double wait=PostGearBalanceScenarios.expectedWait(lures[i]);row(names[i],lures[i],wait,wait/PostGearBalanceScenarios.expectedWait(1),(wait+50)/20+10,(base+200)/(wait+50+200),(base+600)/(wait+50+600));}
    }
    private void caps(List<PostGearBalanceScenarios.Scenario> builds){
        table("Clamp saturation: fixed build axes and ordinary candidate weights","| Build | FL8 | TL3 | S min/max | T min/max | Z min/max | V min/max | P .95 | Candidate cap count/25 | Selection probability at weight cap |");
        for(var s:builds){var g=s.gear();var v=GearArchetypeCases.values(g);int count=0;double cap=0,total=0;
            for(var p:PostGearBalanceScenarios.pool(false)){double weight=selection.adjustedWeight(p,s.nativeLuck(),FishingEnvironment.empty(),g);total+=weight;if(FishingGearEffects.speciesWeightMultiplier(p,FishingEnvironment.empty(),g,s.nativeLuck())>=3-1e-12){count++;cap+=weight;}}
            row(s.name(),v[0]>=8?"100%":"0%",v[1]>=3?"100%":"0%",edge(v[2],.7,1.35),edge(v[3],.8,1.3),edge(v[4],.7,1.4),edge(v[5],.8,1.25),v[6]>=.95?"100%":"0%",count,cap/total);
        }
    }
    private String edge(double v,double low,double high){return (v<=low?"100%":"0%")+" / "+(v>=high?"100%":"0%");}
    private double[] utility(FishingGearModifiers g,double nativeLuck){return new double[]{FishingGearEffects.fishingLuck(g)+nativeLuck,FishingGearEffects.traitLuck(g),g.namedAdditiveModifier(FishingGearEffects.LURE_BONUS),-FishingGearEffects.strengthMultiplier(g),-FishingGearEffects.tempoMultiplier(g),FishingGearEffects.catchZoneAreaMultiplier(g),-FishingGearEffects.minigameSpeedMultiplier(g),FishingGearEffects.catchLossPreventionChance(g),FishingGearEffects.trophyFightRelief(g),FishingGearEffects.crateWeightMultiplier(g),g.namedMultiplierModifier(FishingGearEffects.TARGET_WEIGHT_PREFIX+"heavy"),g.namedMultiplierModifier(FishingGearEffects.TARGET_WEIGHT_PREFIX+"very_small"),g.namedMultiplierModifier(FishingGearEffects.TARGET_WEIGHT_PREFIX+"kujira_target"),g.namedMultiplierModifier(FishingGearEffects.TARGET_WEIGHT_PREFIX+"legendary"),g.namedMultiplierModifier(FishingGearEffects.TARGET_WEIGHT_PREFIX+"boss")};}
    private boolean dominates(double[] a,double[] b){boolean better=false;for(int i=0;i<a.length;i++){if(a[i]<b[i]-1e-12)return false;if(a[i]>b[i]+1e-12)better=true;}return better;}
    private void dominance(List<PostGearBalanceScenarios.Scenario> builds){
        table("Representative full-loadout dominance scan","| Build | Strictly dominates (all measured modifier objectives) |");
        for(var a:builds){var names=new ArrayList<String>();for(var b:builds)if(a!=b&&dominates(utility(a.gear(),a.nativeLuck()),utility(b.gear(),b.nativeLuck())))names.add(b.name());row(a.name(),names.isEmpty()?"none":String.join(", ",names));assertTrue(names.size()<builds.size()-1);}
        table("Single-slot scan: nondominated canonical choices","| Slot | Nondominated choices | Dominated choices |");
        var groups=new LinkedHashMap<String,Map<String,FishingGearModifiers>>();
        var rods=new LinkedHashMap<String,FishingGearModifiers>();for(var p:List.of(WOOD_ROD,IRON_ROD,GOLD_ROD,DIAMOND_ROD,NETHERITE_ROD,KUJIRA_BONE_FISHING_ROD))rods.put(p.name(),p.rodModifiers());groups.put("Rods",rods);
        var lines=new LinkedHashMap<String,FishingGearModifiers>();for(var p:List.of(TIDE_BASE_LINE,TIDE_COPPER_LINE,TIDE_IRON_LINE,TIDE_GOLDEN_LINE,TIDE_DIAMOND_LINE))lines.put(p.name(),TideFishingLineModifiers.forProfile(p));lines.put("Swift",TideborneFishingGearModifiers.swiftLine(PostGearBalanceScenarios.CONFIG));lines.put("Tentacle",TideborneFishingGearModifiers.tentacleLine(PostGearBalanceScenarios.CONFIG));groups.put("Lines",lines);
        var bobbers=new TreeMap<String,FishingGearModifiers>();for(var id:FishingGearRegistry.supportedBobberIds())bobbers.put(id.getPath(),FishingGearRegistry.bobberModifiers(id).orElseThrow());groups.put("Bobbers",bobbers);
        var leaders=new LinkedHashMap<String,FishingGearModifiers>();for(var t:LeaderTier.values())leaders.put(t.name(),LeaderGearModifiers.forTier(t,true));groups.put("Leaders",leaders);
        for(var group:groups.entrySet()){var good=new ArrayList<String>();var bad=new ArrayList<String>();for(var a:group.getValue().entrySet()){boolean dominated=false;for(var b:group.getValue().entrySet())if(a!=b&&dominates(utility(b.getValue(),b.getKey().equals("GOLD_ROD")?1:0),utility(a.getValue(),a.getKey().equals("GOLD_ROD")?1:0)))dominated=true;(dominated?bad:good).add(a.getKey());}row(group.getKey(),String.join(", ",good),String.join(", ",bad));}
    }
    private void crateAndBait(){
        table("Crate sensitivity: conditional weight ratios, not world catch rates","| Setup | Native bait crate factor | Canonical crate factor | Combined crate factor | Fish-only restriction |");
        row("Baseline",1,1,1,false);
        row("Magnetic",2.5,1,2.5,false);
        var kujira=KUJIRA_BONE_FISHING_ROD.rodModifiers();var heart=FishingGearRegistry.bobberModifiers(net.minecraft.util.Identifier.of("tide","heart_bobber")).orElseThrow();
        row("Kujira",1,FishingGearEffects.crateWeightMultiplier(kujira),FishingGearEffects.crateWeightMultiplier(kujira),false);
        row("Heart + Magnetic",2.5,FishingGearEffects.crateWeightMultiplier(heart),2.5*FishingGearEffects.crateWeightMultiplier(heart),false);
        row("Full Leviathan",1,FishingGearEffects.crateWeightMultiplier(kujira),0,LeviathanBaitRules.isFishOnlyCatchPool(TideborneFishingGearModifiers.leviathanBait(true)));
        table("Generic bait axes: no arbitrary scalar utility score","| Bait | FL | TL | Lure | S | T | Target | Crate factor | Fish-only |");
        row("Normal",0,0,2,1,1,"none",1,false);row("Lucky",2,0,0,1,1,"none",1,false);row("Magnetic",0,0,0,1,1,"none",2.5,false);
        row("Incandescent",0,0,1,1,1,"warm 1.4",1,false);row("Abyss",0,0,2,1,1,"deep 1.4",1,false);
        var bait=TideborneFishingGearModifiers.leviathanBait(true);row("Leviathan",bait.fishingLuck(),bait.traitLuck(),0,bait.strengthMultiplier(),bait.tempoMultiplier(),"boss 2; empty shipped roster",1,LeviathanBaitRules.isFishOnlyCatchPool(bait));
    }
}
