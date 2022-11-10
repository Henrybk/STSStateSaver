package savestate;

import basemod.ReflectionHacks;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.megacrit.cardcrawl.map.MapRoomNode;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import com.megacrit.cardcrawl.monsters.MonsterGroup;
import com.megacrit.cardcrawl.monsters.beyond.Reptomancer;
import com.megacrit.cardcrawl.monsters.city.TheCollector;
import com.megacrit.cardcrawl.monsters.city.TorchHead;
import com.megacrit.cardcrawl.rooms.AbstractRoom;
import com.megacrit.cardcrawl.rooms.MonsterRoom;
import com.megacrit.cardcrawl.rooms.MonsterRoomBoss;
import com.megacrit.cardcrawl.rooms.MonsterRoomElite;
import savestate.monsters.MonsterState;
import savestate.monsters.beyond.ReptomancerState;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class MapRoomNodeState {
    private static final String MONSTER_DELIMETER = "###";

    private final boolean taken;
    private final boolean highlighted;
    private final boolean hasEmeraldKey;
    private final boolean isBattleOver;
    private final boolean cannotLose;
    private final boolean eliteTrigger;
    private final boolean mugged;
    private final boolean combatEvent;
    public final boolean rewardAllowed;
    private final boolean rewardTime;
    private final boolean skipMonsterTurn;

    private final float waitTimer;
    public final RoomType roomType;

    public ArrayList<MonsterState> monsterData = null;
    private final AbstractRoom.RoomPhase phase;

    public MapRoomNodeState(MapRoomNode mapRoomNode) {
        this.taken = mapRoomNode.taken;
        this.highlighted = mapRoomNode.highlighted;
        this.hasEmeraldKey = mapRoomNode.hasEmeraldKey;

        AbstractRoom room = mapRoomNode.room;
        this.phase = room.phase;

        // rooms that haven't been entered have null Monster groups

        long startMonsterSave = System.currentTimeMillis();

        if (room.monsters != null) {
            ArrayList<MonsterState> monsters = new ArrayList<>();
            for (AbstractMonster monster : room.monsters.monsters) {
                monsters.add(MonsterState.forMonster(monster));
            }
            this.monsterData = monsters;
        }

        this.isBattleOver = room.isBattleOver;
        this.cannotLose = room.cannotLose;
        this.eliteTrigger = room.eliteTrigger;
        this.mugged = room.mugged;
        this.combatEvent = room.combatEvent;
        this.rewardAllowed = room.rewardAllowed;
        this.rewardTime = room.rewardTime;
        this.skipMonsterTurn = room.skipMonsterTurn;
        this.waitTimer = AbstractRoom.waitTimer;
        this.roomType = getRoomType(mapRoomNode.getRoom());
    }

    private static RoomType getRoomType(AbstractRoom room) {
        if (room instanceof MonsterRoomBoss) {
            return RoomType.BOSS;
        } else if (room instanceof MonsterRoomElite) {
            return RoomType.ELITE;
        } else {
            return RoomType.MONSTER;
        }
    }

    public MapRoomNodeState(String jsonString) {
        JsonObject parsed = new JsonParser().parse(jsonString).getAsJsonObject();

        this.taken = parsed.get("taken").getAsBoolean();
        this.highlighted = parsed.get("highlighted").getAsBoolean();
        this.hasEmeraldKey = parsed.get("has_emerald_key").getAsBoolean();
        this.isBattleOver = parsed.get("is_battle_over").getAsBoolean();
        this.cannotLose = parsed.get("cannot_lose").getAsBoolean();
        this.eliteTrigger = parsed.get("elite_trigger").getAsBoolean();
        this.mugged = parsed.get("mugged").getAsBoolean();
        this.combatEvent = parsed.get("combat_event").getAsBoolean();
        this.rewardAllowed = parsed.get("reward_allowed").getAsBoolean();
        this.rewardTime = parsed.get("reward_time").getAsBoolean();
        this.skipMonsterTurn = parsed.get("skip_monster_turn").getAsBoolean();

        this.waitTimer = parsed.get("wait_timer").getAsFloat();

        this.monsterData = Stream
                .of(parsed.get("monster_data").getAsString().split(MONSTER_DELIMETER))
                .filter(s -> !s.isEmpty()).map(MonsterState::forJsonString)
                .collect(Collectors.toCollection(ArrayList::new));

        this.phase = AbstractRoom.RoomPhase.valueOf(parsed.get("phase_name").getAsString());

        this.roomType = RoomType.valueOf(parsed.get("room_type").getAsString());
    }

    public MapRoomNodeState(JsonObject roomNodeJson) {
        this.taken = roomNodeJson.get("taken").getAsBoolean();
        this.highlighted = roomNodeJson.get("highlighted").getAsBoolean();
        this.hasEmeraldKey = roomNodeJson.get("has_emerald_key").getAsBoolean();
        this.isBattleOver = roomNodeJson.get("is_battle_over").getAsBoolean();
        this.cannotLose = roomNodeJson.get("cannot_lose").getAsBoolean();
        this.eliteTrigger = roomNodeJson.get("elite_trigger").getAsBoolean();
        this.mugged = roomNodeJson.get("mugged").getAsBoolean();
        this.combatEvent = roomNodeJson.get("combat_event").getAsBoolean();
        this.rewardAllowed = roomNodeJson.get("reward_allowed").getAsBoolean();
        this.rewardTime = roomNodeJson.get("reward_time").getAsBoolean();
        this.skipMonsterTurn = roomNodeJson.get("skip_monster_turn").getAsBoolean();
        this.phase = AbstractRoom.RoomPhase.valueOf(roomNodeJson.get("phase_name").getAsString());
        this.roomType = RoomType.valueOf(roomNodeJson.get("room_type").getAsString());
        this.waitTimer = roomNodeJson.get("wait_timer").getAsFloat();

        ArrayList<MonsterState> monsterData = new ArrayList<>();
        roomNodeJson.get("monster_data").getAsJsonArray().forEach(jsonElement -> monsterData
                .add(MonsterState.forJsonObject(jsonElement.getAsJsonObject())));
        this.monsterData = monsterData;

    }

    public MapRoomNode loadMapRoomNode(MapRoomNode mapRoomNode) {
        AbstractRoom room = getRoomForType(roomType);

        mapRoomNode.taken = this.taken;
        mapRoomNode.highlighted = this.highlighted;
        mapRoomNode.hasEmeraldKey = this.hasEmeraldKey;

        room.phase = this.phase;

        room.isBattleOver = this.isBattleOver;
        room.cannotLose = this.cannotLose;
        room.eliteTrigger = this.eliteTrigger;
        room.mugged = this.mugged;
        room.combatEvent = this.combatEvent;
        room.rewardAllowed = this.rewardAllowed;
        room.rewardTime = this.rewardTime;
        room.skipMonsterTurn = this.skipMonsterTurn;
        AbstractRoom.waitTimer = this.waitTimer;

        mapRoomNode.getRoom().dispose();


        if (monsterData != null) {
            List<AbstractMonster> list = new ArrayList<>();
            for (MonsterState monsterDatum : monsterData) {
                AbstractMonster loadMonster = monsterDatum.loadMonster();
                list.add(loadMonster);
            }
            room.monsters = new MonsterGroup(list.toArray(new AbstractMonster[0]));
        } else {
            room.monsters = null;
        }

        for (AbstractMonster monster : room.monsters.monsters) {
            if (monster instanceof TheCollector) {
                int key = 1;
                HashMap<Integer, AbstractMonster> collectorMinions = new HashMap<>();

                ArrayList<AbstractMonster> deadTorchHeads = new ArrayList<>();
                for (AbstractMonster possibleMinion : room.monsters.monsters) {
                    if (possibleMinion instanceof TorchHead) {
                        if (!possibleMinion.isDying) {
                            collectorMinions.put(key++, possibleMinion);
                        } else {
                            deadTorchHeads.add(possibleMinion);
                        }
                    }
                }

                int deadIndex = 0;
                while (key < 3) {
                    // There should be at least two torchheads between being dead and alive
                    TorchHead dummy = new TorchHead(0, 0);
                    dummy.isDead = true;
                    dummy.isDying = true;

                    collectorMinions.put(key++, dummy);
                }

                if (key < 3) {
                    System.err.println("not enough toches");
                }

                ReflectionHacks
                        .setPrivate(monster, TheCollector.class, "enemySlots", collectorMinions);

            } else if (monster instanceof Reptomancer) {
                for (MonsterState monsterState : monsterData) {
                    if (monsterState instanceof ReptomancerState) {
                        ((ReptomancerState) monsterState).postMonstersLoad((Reptomancer) monster);
                    }
                }
            }
        }

        if (room.phase == AbstractRoom.RoomPhase.COMBAT) {
            if (room.monsters != null) {
            }
        } else if (room.phase == AbstractRoom.RoomPhase.EVENT) {
            if (room.event != null) {
                room.update();
            }
        }

        mapRoomNode.room = room;
        return mapRoomNode;
    }

    private static AbstractRoom getRoomForType(RoomType roomType) {
        switch (roomType) {
            case BOSS:
                return new MonsterRoomBoss();
            case ELITE:
                return new MonsterRoomElite();
            default:
                return new MonsterRoom();
        }
    }

    public String encode() {
        JsonObject mapRoomNodeStateJson = new JsonObject();

        mapRoomNodeStateJson.addProperty("taken", taken);
        mapRoomNodeStateJson.addProperty("highlighted", highlighted);
        mapRoomNodeStateJson.addProperty("has_emerald_key", hasEmeraldKey);
        mapRoomNodeStateJson.addProperty("is_battle_over", isBattleOver);
        mapRoomNodeStateJson.addProperty("cannot_lose", cannotLose);
        mapRoomNodeStateJson.addProperty("elite_trigger", eliteTrigger);
        mapRoomNodeStateJson.addProperty("mugged", mugged);
        mapRoomNodeStateJson.addProperty("combat_event", combatEvent);
        mapRoomNodeStateJson.addProperty("reward_allowed", rewardAllowed);
        mapRoomNodeStateJson.addProperty("reward_time", rewardTime);
        mapRoomNodeStateJson.addProperty("skip_monster_turn", skipMonsterTurn);
        mapRoomNodeStateJson.addProperty("wait_timer", waitTimer);
        mapRoomNodeStateJson.addProperty("room_type", roomType.name());

        mapRoomNodeStateJson
                .addProperty("monster_data", monsterData.stream().map(MonsterState::encode)
                                                        .collect(Collectors
                                                                .joining(MONSTER_DELIMETER)));
        mapRoomNodeStateJson.addProperty("phase_name", phase.name());

        return mapRoomNodeStateJson.toString();
    }

    public JsonObject jsonEncode() {
        JsonObject mapRoomNodeStateJson = new JsonObject();

        mapRoomNodeStateJson.addProperty("taken", taken);
        mapRoomNodeStateJson.addProperty("highlighted", highlighted);
        mapRoomNodeStateJson.addProperty("has_emerald_key", hasEmeraldKey);
        mapRoomNodeStateJson.addProperty("is_battle_over", isBattleOver);
        mapRoomNodeStateJson.addProperty("cannot_lose", cannotLose);
        mapRoomNodeStateJson.addProperty("elite_trigger", eliteTrigger);
        mapRoomNodeStateJson.addProperty("mugged", mugged);
        mapRoomNodeStateJson.addProperty("combat_event", combatEvent);
        mapRoomNodeStateJson.addProperty("reward_allowed", rewardAllowed);
        mapRoomNodeStateJson.addProperty("reward_time", rewardTime);
        mapRoomNodeStateJson.addProperty("skip_monster_turn", skipMonsterTurn);
        mapRoomNodeStateJson.addProperty("wait_timer", waitTimer);
        mapRoomNodeStateJson.addProperty("room_type", roomType.name());
        mapRoomNodeStateJson.addProperty("phase_name", phase.name());

        JsonArray monsterDataToSet = new JsonArray();
        monsterData.forEach(monsterState -> monsterDataToSet.add(monsterState.jsonEncode()));
        mapRoomNodeStateJson.add("monster_data", monsterDataToSet);

        return mapRoomNodeStateJson;
    }

    public String diffEncode() {
        JsonObject mapRoomNodeStateJson = new JsonObject();

        mapRoomNodeStateJson
                .addProperty("monster_data", monsterData.stream().map(MonsterState::diffEncode)
                                                        .collect(Collectors
                                                                .joining(MONSTER_DELIMETER)));
        return mapRoomNodeStateJson.toString();
    }

    public static boolean diff(String diffString1, String diffString2) {
        boolean result = true;

        JsonObject one = new JsonParser().parse(diffString1).getAsJsonObject();
        JsonObject two = new JsonParser().parse(diffString2).getAsJsonObject();

        boolean monstersMatch = diffMonsterList(one.get("monster_data").getAsString(), two
                .get("monster_data").getAsString());
        if (!monstersMatch) {
            result = false;
        }

        return result;
    }

    public static boolean diffMonsterList(String diffString1, String diffString2) {
        boolean result = true;

        String[] monsters1 = diffString1.split(MONSTER_DELIMETER);
        String[] monsters2 = diffString2.split(MONSTER_DELIMETER);

        boolean lengthsEqual = monsters1.length == monsters2.length;
        if (!lengthsEqual) {
            result = false;
            System.err.println("different monster counts");
        } else {
            for (int i = 0; i < monsters1.length; i++) {
                result = result && MonsterState.diff(monsters1[i], monsters2[i]);
            }
        }

        return result;
    }


    public enum RoomType {
        MONSTER,
        ELITE,
        BOSS
    }
}
