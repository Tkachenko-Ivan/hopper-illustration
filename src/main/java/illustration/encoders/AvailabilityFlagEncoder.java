package illustration.encoders;

import com.graphhopper.reader.ReaderRelation;
import com.graphhopper.reader.ReaderWay;
import com.graphhopper.routing.util.AbstractFlagEncoder;
import com.graphhopper.routing.util.EncodedDoubleValue;
import com.graphhopper.routing.util.EncodedValue;
import static com.graphhopper.routing.util.FlagEncoder.K_ROUNDABOUT;
import com.graphhopper.routing.util.PriorityCode;
import static com.graphhopper.routing.util.PriorityCode.AVOID_IF_POSSIBLE;
import static com.graphhopper.routing.util.PriorityCode.BEST;
import static com.graphhopper.routing.util.PriorityCode.PREFER;
import static com.graphhopper.routing.util.PriorityCode.UNCHANGED;
import com.graphhopper.routing.weighting.PriorityWeighting;
import com.graphhopper.util.PMap;
import java.util.*;

// Source: https://github.com/graphhopper/graphhopper/blob/0.10/core/src/main/java/com/graphhopper/routing/util/FootFlagEncoder.java
public abstract class AvailabilityFlagEncoder extends AbstractFlagEncoder {

    public int SLOW_SPEED = 2;
    public int MEAN_SPEED = 5;
    public int FERRY_SPEED = 15;

    final Set<String> safeHighwayTags = new HashSet<>();
    final Set<String> allowedHighwayTags = new HashSet<>();

    final Map<String, Integer> hikingNetworkToCode = new HashMap<>();
    protected HashSet<String> sidewalkValues = new HashSet<>(5);
    protected HashSet<String> sidewalksNoValues = new HashSet<>(5);
    private EncodedValue priorityWayEncoder;
    private EncodedValue relationCodeEncoder;

    private List<Long> restricted;

    /**
     * Should be only instantiated via EncodingManager
     */
    public AvailabilityFlagEncoder() {
        this(4, 1);
    }

    public AvailabilityFlagEncoder(List<Long> restricted) {
        this(4, 1);
        this.restricted = restricted;
    }

    public AvailabilityFlagEncoder(PMap properties) {
        this((int) properties.getLong("speedBits", 4),
                properties.getDouble("speedFactor", 1));
        this.properties = properties;
        this.setBlockFords(properties.getBool("block_fords", true));
    }

    public AvailabilityFlagEncoder(String propertiesStr) {
        this(new PMap(propertiesStr));
    }

    public AvailabilityFlagEncoder(int speedBits, double speedFactor) {
        super(speedBits, speedFactor, 0);
        restrictions.addAll(Arrays.asList("foot", "access"));
        restrictedValues.add("private");
        restrictedValues.add("no");
        restrictedValues.add("restricted");
        restrictedValues.add("military");
        restrictedValues.add("emergency");

        intendedValues.add("yes");
        intendedValues.add("designated");
        intendedValues.add("official");
        intendedValues.add("permissive");

        sidewalksNoValues.add("no");
        sidewalksNoValues.add("none");
        sidewalksNoValues.add("separate");

        sidewalkValues.add("yes");
        sidewalkValues.add("both");
        sidewalkValues.add("left");
        sidewalkValues.add("right");

        setBlockByDefault(false);
        potentialBarriers.add("gate");

        safeHighwayTags.add("footway");
        safeHighwayTags.add("path");
        safeHighwayTags.add("steps");
        safeHighwayTags.add("pedestrian");
        safeHighwayTags.add("living_street");
        safeHighwayTags.add("track");
        safeHighwayTags.add("service");

        allowedHighwayTags.addAll(safeHighwayTags);
        allowedHighwayTags.add("cycleway");
        allowedHighwayTags.add("unclassified");
        allowedHighwayTags.add("road");

        hikingNetworkToCode.put("iwn", UNCHANGED.getValue());
        hikingNetworkToCode.put("nwn", UNCHANGED.getValue());
        hikingNetworkToCode.put("rwn", UNCHANGED.getValue());
        hikingNetworkToCode.put("lwn", UNCHANGED.getValue());

        maxPossibleSpeed = FERRY_SPEED;

        init();
    }

    @Override
    public int getVersion() {
        return 4;
    }

    @Override
    public int defineWayBits(int index, int shift) {
        // first two bits are reserved for route handling in superclass
        shift = super.defineWayBits(index, shift);
        // larger value required - ferries are faster than pedestrians
        speedEncoder = new EncodedDoubleValue("Speed", shift, speedBits, speedFactor, MEAN_SPEED, maxPossibleSpeed);
        shift += speedEncoder.getBits();

        priorityWayEncoder = new EncodedValue("PreferWay", shift, 3, 1, 0, 7);
        shift += priorityWayEncoder.getBits();
        return shift;
    }

    @Override
    public int defineRelationBits(int index, int shift) {
        relationCodeEncoder = new EncodedValue("RelationCode", shift, 3, 1, 0, 7);
        return shift + relationCodeEncoder.getBits();
    }

    /**
     * Foot flag encoder does not provide any turn cost / restrictions
     */
    @Override
    public int defineTurnBits(int index, int shift) {
        return shift;
    }

    @Override
    public boolean isTurnRestricted(long flag) {
        return false;
    }

    @Override
    public double getTurnCost(long flag) {
        return 0;
    }

    @Override
    public long getTurnFlags(boolean restricted, double costs) {
        return 0;
    }

    /**
     * Some ways are okay but not separate for pedestrians
     */
    @Override
    public long acceptWay(ReaderWay way) {
        if (restricted.contains(way.getId())) {
            return 0;
        }

        String highwayValue = way.getTag("highway");
        if (highwayValue == null) {
            long acceptPotentially = 0;

            if (way.hasTag("route", ferries)) {
                String footTag = way.getTag("foot");
                if (footTag == null || "yes".equals(footTag)) {
                    acceptPotentially = acceptBit | ferryBit;
                }
            }

            // special case not for all acceptedRailways, only platform
            if (way.hasTag("railway", "platform")) {
                acceptPotentially = acceptBit;
            }

            if (way.hasTag("man_made", "pier")) {
                acceptPotentially = acceptBit;
            }

            if (acceptPotentially != 0) {
                if (way.hasTag(restrictions, restrictedValues) && !getConditionalTagInspector().isRestrictedWayConditionallyPermitted(way)) {
                    return 0;
                }
                return acceptPotentially;
            }

            return 0;
        }

        String sacScale = way.getTag("sac_scale");
        if (sacScale != null) {
            if (!"hiking".equals(sacScale) && !"mountain_hiking".equals(sacScale)
                    && !"demanding_mountain_hiking".equals(sacScale) && !"alpine_hiking".equals(sacScale)) // other scales are too dangerous, see http://wiki.openstreetmap.org/wiki/Key:sac_scale
            {
                return 0;
            }
        }

        // no need to evaluate ferries or fords - already included here
        if (way.hasTag("foot", intendedValues)) {
            return acceptBit;
        }

        // check access restrictions
        if (way.hasTag(restrictions, restrictedValues) && !getConditionalTagInspector().isRestrictedWayConditionallyPermitted(way)) {
            return 0;
        }

        if (way.hasTag("sidewalk", sidewalkValues)) {
            return acceptBit;
        }

        if (!allowedHighwayTags.contains(highwayValue)) {
            return 0;
        }

        if (way.hasTag("motorroad", "yes")) {
            return 0;
        }

        // do not get our feet wet, "yes" is already included above
        if (isBlockFords() && (way.hasTag("highway", "ford") || way.hasTag("ford"))) {
            return 0;
        }

        if (getConditionalTagInspector().isPermittedWayConditionallyRestricted(way)) {
            return 0;
        }

        return acceptBit;
    }

    @Override
    public long handleRelationTags(ReaderRelation relation, long oldRelationFlags) {
        int code = 0;
        if (relation.hasTag("route", "hiking") || relation.hasTag("route", "foot")) {
            Integer val = hikingNetworkToCode.get(relation.getTag("network"));
            if (val != null) {
                code = val;
            } else {
                code = hikingNetworkToCode.get("lwn");
            }
        } else if (relation.hasTag("route", "ferry")) {
            code = PriorityCode.AVOID_IF_POSSIBLE.getValue();
        }

        int oldCode = (int) relationCodeEncoder.getValue(oldRelationFlags);
        if (oldCode < code) {
            return relationCodeEncoder.setValue(0, code);
        }
        return oldRelationFlags;
    }

    @Override
    public long handleWayTags(ReaderWay way, long allowed, long relationFlags) {
        if (!isAccept(allowed)) {
            return 0;
        }

        long flags = 0;
        if (!isFerry(allowed)) {
            String sacScale = way.getTag("sac_scale");
            if (sacScale != null) {
                if ("hiking".equals(sacScale)) {
                    flags = speedEncoder.setDoubleValue(flags, MEAN_SPEED);
                } else {
                    flags = speedEncoder.setDoubleValue(flags, SLOW_SPEED);
                }
            } else {
                flags = speedEncoder.setDoubleValue(flags, MEAN_SPEED);
            }
            flags |= directionBitMask;

            boolean isRoundabout = way.hasTag("junction", "roundabout") || way.hasTag("junction", "circular");
            if (isRoundabout) {
                flags = setBool(flags, K_ROUNDABOUT, true);
            }

        } else {
            double ferrySpeed = getFerrySpeed(way);
            flags = setSpeed(flags, ferrySpeed);
            flags |= directionBitMask;
        }

        int priorityFromRelation = 0;
        if (relationFlags != 0) {
            priorityFromRelation = (int) relationCodeEncoder.getValue(relationFlags);
        }

        flags = priorityWayEncoder.setValue(flags, handlePriority(way, priorityFromRelation));
        return flags;
    }

    @Override
    public double getDouble(long flags, int key) {
        switch (key) {
            case PriorityWeighting.KEY:
                return (double) priorityWayEncoder.getValue(flags) / BEST.getValue();
            default:
                return super.getDouble(flags, key);
        }
    }

    protected int handlePriority(ReaderWay way, int priorityFromRelation) {
        TreeMap<Double, Integer> weightToPrioMap = new TreeMap<Double, Integer>();
        if (priorityFromRelation == 0) {
            weightToPrioMap.put(0d, UNCHANGED.getValue());
        } else {
            weightToPrioMap.put(110d, priorityFromRelation);
        }

        collect(way, weightToPrioMap);

        // pick priority with biggest order value
        return weightToPrioMap.lastEntry().getValue();
    }

    /**
     * @param weightToPrioMap associate a weight with every priority. This
     * sorted map allows subclasses to 'insert' more important priorities as
     * well as overwrite determined priorities.
     */
    void collect(ReaderWay way, TreeMap<Double, Integer> weightToPrioMap) {
        String highway = way.getTag("highway");
        if (way.hasTag("foot", "designated")) {
            weightToPrioMap.put(100d, PREFER.getValue());
        }

        double maxSpeed = getMaxSpeed(way);
        if (safeHighwayTags.contains(highway) || maxSpeed > 0 && maxSpeed <= 20) {
            weightToPrioMap.put(40d, PREFER.getValue());
            if (way.hasTag("tunnel", intendedValues)) {
                if (way.hasTag("sidewalk", sidewalksNoValues)) {
                    weightToPrioMap.put(40d, AVOID_IF_POSSIBLE.getValue());
                } else {
                    weightToPrioMap.put(40d, UNCHANGED.getValue());
                }
            }
        } else if (maxSpeed > 50) {
            if (!way.hasTag("sidewalk", sidewalkValues)) {
                weightToPrioMap.put(45d, AVOID_IF_POSSIBLE.getValue());
            }
        }

        if (way.hasTag("bicycle", "official") || way.hasTag("bicycle", "designated")) {
            weightToPrioMap.put(44d, AVOID_IF_POSSIBLE.getValue());
        }
    }

    @Override
    public boolean supports(Class<?> feature) {
        if (super.supports(feature)) {
            return true;
        }
        return PriorityWeighting.class.isAssignableFrom(feature);
    }

    /*
     * This method is a current hack, to allow ferries to be actually faster than our current storable maxSpeed.
     */
    @Override
    public double getSpeed(long flags) {
        double speed = super.getSpeed(flags);
        if (speed == getMaxSpeed()) {
            // We cannot be sure if it was a long or a short trip
            return SHORT_TRIP_FERRY_SPEED;
        }
        return speed;
    }
}
