package com.redslovesgames.tideborne.fishing.v2;

import java.util.Objects;

/** Normalizes species fight inputs, then applies bounded specimen size, Body Type, and canonical gear effects. */
public final class FightProfileService {
    /** Builds from immutable species/specimen inputs, relieving only their positive surcharge. */
    public FightProfile create(SpeciesProfile species, SpecimenData specimen, FishingGearModifiers gear) {
        return applyGearModifiers(applyTrophyRelief(speciesBaseline(species), create(species, specimen), gear), gear);
    }

    public FightProfile speciesBaseline(SpeciesProfile species) {
        Objects.requireNonNull(species, "species");
        double strength = normalizeStrength(species.strength());
        return new FightProfile(strength, normalizeTempo(species.tempo()), catchZoneArea(strength), species.behavior());
    }

    /** Inputs must precede all gear effects, so leader/bait costs cannot become eligible surcharge. */
    public FightProfile applyTrophyRelief(FightProfile baseline, FightProfile specimenFight, FishingGearModifiers gear) {
        Objects.requireNonNull(baseline, "baseline");
        Objects.requireNonNull(specimenFight, "specimenFight");
        double relief = FishingGearEffects.trophyFightRelief(gear);
        double strength = relieveSurcharge(baseline.strength(), specimenFight.strength(), relief);
        double tempo = relieveSurcharge(baseline.tempo(), specimenFight.tempo(), relief);
        return new FightProfile(strength, tempo, catchZoneArea(strength), specimenFight.behavior());
    }

    /** Canonical runtime entry: all slots meet here before any clamp or fight multiplier. */
    public MinigameProjection projectMinigame(SpeciesProfile species, SpecimenData specimen,
                                              FishingGearModifiers gear, double difficultyMultiplier) {
        FightProfile relieved = applyTrophyRelief(speciesBaseline(species), create(species, specimen), gear);
        return projectMinigame(relieved, gear, difficultyMultiplier);
    }

    private static double relieveSurcharge(double baseline, double specimenValue, double relief) {
        return specimenValue - Math.max(0.0, specimenValue - baseline) * relief;
    }
    public static final double MAX_EXTERNAL_TEMPO=2.2,MAX_CANONICAL_STRENGTH=1.1,MIN_CATCH_ZONE_AREA=.12,MAX_CATCH_ZONE_AREA=.78,MIN_FINAL_TEMPO=.035,MAX_FINAL_TEMPO=.16,MIN_MINIGAME_CATCH_ZONE_AREA=.05,MAX_MINIGAME_CATCH_ZONE_AREA=1.0,MIN_MINIGAME_SPEED=.05;
    public static final double MIN_GEAR_STRENGTH_MULTIPLIER=.70,MIN_GEAR_TEMPO_MULTIPLIER=.80,MIN_GEAR_CATCH_ZONE_MULTIPLIER=.70,MAX_GEAR_CATCH_ZONE_MULTIPLIER=1.40;
    public static final double GIANT_STRENGTH_MULTIPLIER=1.08,GIANT_TEMPO_MULTIPLIER=.95,DWARF_STRENGTH_MULTIPLIER=.92,DWARF_TEMPO_MULTIPLIER=1.08;
    public FightProfile create(SpeciesProfile species,SpecimenData specimen){Objects.requireNonNull(species,"species");Objects.requireNonNull(specimen,"specimen");if(!species.speciesId().equals(specimen.speciesId()))throw new IllegalArgumentException("species profile and specimen IDs must match");double canonicalStrength=normalizeStrength(species.strength()),canonicalTempo=normalizeTempo(species.tempo());double strength=canonicalStrength*strengthMultiplier(specimen.finalPercentile());double tempo=clamp(canonicalTempo*tempoMultiplier(specimen.finalPercentile()),MIN_FINAL_TEMPO,MAX_FINAL_TEMPO);return applyBodyType(new FightProfile(strength,tempo,catchZoneArea(strength),species.behavior()),specimen.bodyType());}
    public FightProfile applyGearModifiers(FightProfile profile,FishingGearModifiers modifiers){Objects.requireNonNull(profile,"profile");Objects.requireNonNull(modifiers,"modifiers");double strength=profile.strength()*FishingGearEffects.strengthMultiplier(modifiers);double tempo=clamp(profile.tempo()*FishingGearEffects.tempoMultiplier(modifiers),MIN_FINAL_TEMPO,MAX_FINAL_TEMPO);return new FightProfile(strength,tempo,catchZoneArea(strength),profile.behavior());}
    public MinigameProjection projectMinigame(FightProfile profile,FishingGearModifiers modifiers,double difficultyMultiplier){Objects.requireNonNull(profile,"profile");Objects.requireNonNull(modifiers,"modifiers");requireFinite("difficultyMultiplier",difficultyMultiplier);if(difficultyMultiplier<0)throw new IllegalArgumentException("difficultyMultiplier must be nonnegative");double strength=profile.strength()*FishingGearEffects.strengthMultiplier(modifiers);double tempo=profile.tempo()*FishingGearEffects.tempoMultiplier(modifiers);return finishMinigameProjection(catchZoneArea(strength),Math.max(MIN_FINAL_TEMPO,tempo*difficultyMultiplier),modifiers);}
    public MinigameProjection projectCompatibilityMinigame(double tideCatchZoneArea,double tideSpeed,FishingGearModifiers modifiers){requireFinite("tideCatchZoneArea",tideCatchZoneArea);requireFinite("tideSpeed",tideSpeed);Objects.requireNonNull(modifiers,"modifiers");return finishMinigameProjection(tideCatchZoneArea,tideSpeed,modifiers);}
    public double normalizeTempo(double externalTempo){requireFinite("externalTempo",externalTempo);double s=clamp(externalTempo,0,MAX_EXTERNAL_TEMPO);return .04+.085*Math.log1p(s)/Math.log(3.2);} public double normalizeStrength(double externalStrength){requireFinite("externalStrength",externalStrength);return clamp(externalStrength,0,MAX_CANONICAL_STRENGTH);} public double catchZoneArea(double strength){requireFinite("strength",strength);return clamp(.78-.58*Math.pow(Math.max(strength,0),1.25),MIN_CATCH_ZONE_AREA,MAX_CATCH_ZONE_AREA);} public double strengthMultiplier(double percentile){return 1+.12*normalizedPercentileOffset(percentile);} public double tempoMultiplier(double percentile){return 1-.06*normalizedPercentileOffset(percentile);}
    private FightProfile applyBodyType(FightProfile p,SpecimenData.BodyType type){double sm,tm;switch(type){case GIANT->{sm=GIANT_STRENGTH_MULTIPLIER;tm=GIANT_TEMPO_MULTIPLIER;}case DWARF->{sm=DWARF_STRENGTH_MULTIPLIER;tm=DWARF_TEMPO_MULTIPLIER;}case NORMAL->{sm=1;tm=1;}default->throw new IllegalStateException("Unhandled Body Type: "+type);}double s=p.strength()*sm,t=clamp(p.tempo()*tm,MIN_FINAL_TEMPO,MAX_FINAL_TEMPO);return new FightProfile(s,t,catchZoneArea(s),p.behavior());}
    private MinigameProjection finishMinigameProjection(double area,double speed,FishingGearModifiers m){double gearArea=clamp(FishingGearEffects.catchZoneAreaMultiplier(m),MIN_GEAR_CATCH_ZONE_MULTIPLIER,MAX_GEAR_CATCH_ZONE_MULTIPLIER);double adjustedArea=area*gearArea,adjustedSpeed=speed*FishingGearEffects.minigameSpeedMultiplier(m);return new MinigameProjection(clamp(adjustedArea,MIN_MINIGAME_CATCH_ZONE_AREA,MAX_MINIGAME_CATCH_ZONE_AREA),Math.max(MIN_MINIGAME_SPEED,adjustedSpeed));}
    private static double normalizedPercentileOffset(double p){if(!Double.isFinite(p)||p<0||p>100)throw new IllegalArgumentException("percentile must be between 0 and 100");return(p-50)/50;}private static double clamp(double v,double min,double max){return Math.max(min,Math.min(max,v));}private static void requireFinite(String n,double v){if(!Double.isFinite(v))throw new IllegalArgumentException(n+" must be finite");}
    public record MinigameProjection(double catchZoneArea,double speed){public MinigameProjection{if(!Double.isFinite(catchZoneArea)||catchZoneArea<MIN_MINIGAME_CATCH_ZONE_AREA||catchZoneArea>MAX_MINIGAME_CATCH_ZONE_AREA)throw new IllegalArgumentException("catchZoneArea must be within the Tide minigame bounds");if(!Double.isFinite(speed)||speed<MIN_MINIGAME_SPEED)throw new IllegalArgumentException("speed must be finite and at least the Tide minigame floor");}}
}
