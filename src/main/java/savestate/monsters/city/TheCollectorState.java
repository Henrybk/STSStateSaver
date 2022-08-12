package savestate.monsters.city;

import basemod.ReflectionHacks;
import com.evacipated.cardcrawl.modthespire.lib.SpireInsertPatch;
import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;
import com.evacipated.cardcrawl.modthespire.lib.SpireReturn;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import com.megacrit.cardcrawl.monsters.city.TheCollector;
import savestate.fastobjects.AnimationStateFast;
import savestate.monsters.Monster;
import savestate.monsters.MonsterState;

import static savestate.SaveStateMod.shouldGoFast;

public class TheCollectorState extends MonsterState {
    private final int turnsTaken;
    private final boolean utUsed;
    private final boolean initialSpawn;

    public TheCollectorState(AbstractMonster monster) {
        super(monster);

        this.initialSpawn = ReflectionHacks
                .getPrivate(monster, TheCollector.class, "initialSpawn");
        this.turnsTaken = ReflectionHacks
                .getPrivate(monster, TheCollector.class, "turnsTaken");
        this.utUsed = ReflectionHacks
                .getPrivate(monster, TheCollector.class, "ultUsed");

        this.monsterTypeNumber = Monster.THE_COLLECTOR.ordinal();
    }

    public TheCollectorState(String jsonString) {
        super(jsonString);

        // TODO don't parse twice
        JsonObject parsed = new JsonParser().parse(jsonString).getAsJsonObject();

        this.utUsed = parsed.get("ult_used").getAsBoolean();
        this.turnsTaken = parsed.get("turns_taken").getAsInt();
        this.initialSpawn = parsed.get("initial_spawn").getAsBoolean();

        monsterTypeNumber = Monster.THE_COLLECTOR.ordinal();
    }

    public TheCollectorState(JsonObject monsterJson) {
        super(monsterJson);

        this.utUsed = monsterJson.get("ult_used").getAsBoolean();
        this.turnsTaken = monsterJson.get("turns_taken").getAsInt();
        this.initialSpawn = monsterJson.get("initial_spawn").getAsBoolean();

        monsterTypeNumber = Monster.THE_COLLECTOR.ordinal();
    }

    @Override
    public AbstractMonster loadMonster() {
        TheCollector monster = new TheCollector();
        populateSharedFields(monster);

        ReflectionHacks
                .setPrivate(monster, TheCollector.class, "initialSpawn", initialSpawn);
        ReflectionHacks
                .setPrivate(monster, TheCollector.class, "turnsTaken", turnsTaken);
        ReflectionHacks
                .setPrivate(monster, TheCollector.class, "ultUsed", utUsed);

        return monster;
    }

    @Override
    public String encode() {
        JsonObject monsterStateJson = new JsonParser().parse(super.encode()).getAsJsonObject();

        monsterStateJson.addProperty("initial_spawn", initialSpawn);
        monsterStateJson.addProperty("turns_taken", turnsTaken);
        monsterStateJson.addProperty("ult_used", utUsed);

        return monsterStateJson.toString();
    }

    @Override
    public JsonObject jsonEncode() {
        JsonObject result = super.jsonEncode();

        result.addProperty("initial_spawn", initialSpawn);
        result.addProperty("turns_taken", turnsTaken);
        result.addProperty("ult_used", utUsed);

        return result;
    }

    @SpirePatch(
            clz = TheCollector.class,
            paramtypez = {},
            method = SpirePatch.CONSTRUCTOR
    )
    public static class NoAnimationsPatch {
        @SpireInsertPatch(loc = 98)
        public static SpireReturn TheCollector(TheCollector _instance) {
            if (shouldGoFast) {
                _instance.state = new AnimationStateFast();
                return SpireReturn.Return(null);
            }
            return SpireReturn.Continue();
        }
    }

    @SpirePatch(
            clz = TheCollector.class,
            paramtypez = {},
            method = "update"
    )
    public static class NoUpdateAnimationsPatch {
        @SpireInsertPatch(loc = 236)
        public static SpireReturn TheCollector(TheCollector _instance) {
            if (shouldGoFast) {
                return SpireReturn.Return(null);
            }
            return SpireReturn.Continue();
        }
    }
}
