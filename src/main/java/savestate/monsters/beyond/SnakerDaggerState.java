package savestate.monsters.beyond;

import basemod.ReflectionHacks;
import savestate.fastobjects.AnimationStateDataFast;
import savestate.fastobjects.AnimationStateFast;
import savestate.monsters.Monster;
import savestate.monsters.MonsterState;
import com.evacipated.cardcrawl.modthespire.lib.SpireInsertPatch;
import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;
import com.evacipated.cardcrawl.modthespire.lib.SpireReturn;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.megacrit.cardcrawl.cards.DamageInfo;
import com.megacrit.cardcrawl.core.AbstractCreature;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import com.megacrit.cardcrawl.monsters.beyond.SnakeDagger;

import static savestate.SaveStateMod.shouldGoFast;

public class SnakerDaggerState extends MonsterState {
    private final boolean firstMove;

    public SnakerDaggerState(AbstractMonster monster) {
        super(monster);

        this.firstMove = ReflectionHacks.getPrivate(monster, SnakeDagger.class, "firstMove");

        monsterTypeNumber = Monster.SNAKE_DAGGER.ordinal();
    }

    public SnakerDaggerState(String jsonString) {
        super(jsonString);

        // TODO don't parse twice
        JsonObject parsed = new JsonParser().parse(jsonString).getAsJsonObject();

        this.firstMove = parsed.get("fist_move").getAsBoolean();

        monsterTypeNumber = Monster.SNAKE_DAGGER.ordinal();
    }

    public SnakerDaggerState(JsonObject monsterJson) {
        super(monsterJson);

        this.firstMove = monsterJson.get("fist_move").getAsBoolean();

        monsterTypeNumber = Monster.SNAKE_DAGGER.ordinal();
    }

    @Override
    public AbstractMonster loadMonster() {
        SnakeDagger monster = new SnakeDagger(offsetX, offsetY);

        populateSharedFields(monster);

        ReflectionHacks.setPrivate(monster, SnakeDagger.class, "firstMove", firstMove);

        return monster;
    }

    @Override
    public String encode() {
        JsonObject monsterStateJson = new JsonParser().parse(super.encode()).getAsJsonObject();

        monsterStateJson.addProperty("fist_move", firstMove);

        return monsterStateJson.toString();
    }

    @Override
    public JsonObject jsonEncode() {
        JsonObject result = super.jsonEncode();

        result.addProperty("fist_move", firstMove);

        return result;
    }

    @SpirePatch(
            clz = SnakeDagger.class,
            paramtypez = {float.class, float.class},
            method = SpirePatch.CONSTRUCTOR
    )
    public static class YetNoAnimationsPatch {

        @SpireInsertPatch(loc = 33)
        public static SpireReturn Insert(SnakeDagger _instance, float x, float y) {
            if (shouldGoFast) {
                _instance.damage.add(new DamageInfo(_instance, 9));
                _instance.damage.add(new DamageInfo(_instance, 25));
                _instance.damage.add(new DamageInfo(_instance, 25, DamageInfo.DamageType.HP_LOSS));

                _instance.state = new AnimationStateFast();
                ReflectionHacks
                        .setPrivate(_instance, AbstractCreature.class, "stateData", new AnimationStateDataFast());
                return SpireReturn.Return(null);
            }
            return SpireReturn.Continue();
        }
    }
}
