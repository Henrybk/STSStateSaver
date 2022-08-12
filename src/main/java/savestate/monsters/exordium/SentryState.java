package savestate.monsters.exordium;

import basemod.ReflectionHacks;
import basemod.patches.com.megacrit.cardcrawl.core.EnergyManager.PostEnergyRechargeHook;
import com.evacipated.cardcrawl.modthespire.lib.SpireInsertPatch;
import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;
import com.evacipated.cardcrawl.modthespire.lib.SpireReturn;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.megacrit.cardcrawl.actions.AbstractGameAction;
import com.megacrit.cardcrawl.actions.common.MakeTempCardInDiscardAction;
import com.megacrit.cardcrawl.actions.common.ReducePowerAction;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.core.EnergyManager;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import com.megacrit.cardcrawl.monsters.exordium.Sentry;
import com.megacrit.cardcrawl.ui.panels.EnergyPanel;
import savestate.fastobjects.AnimationStateFast;
import savestate.monsters.Monster;
import savestate.monsters.MonsterState;

import static savestate.SaveStateMod.shouldGoFast;

public class SentryState extends MonsterState {
    private final boolean firstMove;

    public SentryState(AbstractMonster monster) {
        super(monster);

        this.firstMove = ReflectionHacks.getPrivate(monster, Sentry.class, "firstMove");

        monsterTypeNumber = Monster.SENTRY.ordinal();
    }

    public SentryState(String jsonString) {
        super(jsonString);

        // TODO don't parse twice
        JsonObject parsed = new JsonParser().parse(jsonString).getAsJsonObject();

        this.firstMove = parsed.get("first_move").getAsBoolean();

        monsterTypeNumber = Monster.SENTRY.ordinal();
    }

    public SentryState(JsonObject monsterJson) {
        super(monsterJson);

        this.firstMove = monsterJson.get("first_move").getAsBoolean();

        monsterTypeNumber = Monster.SENTRY.ordinal();
    }

    @Override
    public AbstractMonster loadMonster() {
        Sentry result = new Sentry(offsetX, offsetY);

        populateSharedFields(result);

        ReflectionHacks.setPrivate(result, Sentry.class, "firstMove", firstMove);

        return result;
    }

    @Override
    public String encode() {
        JsonObject monsterStateJson = new JsonParser().parse(super.encode()).getAsJsonObject();

        monsterStateJson.addProperty("first_move", firstMove);

        return monsterStateJson.toString();
    }

    @Override
    public JsonObject jsonEncode() {
        JsonObject result = super.jsonEncode();

        result.addProperty("first_move", firstMove);

        return result;
    }

    @SpirePatch(
            clz = Sentry.class,
            paramtypez = {float.class, float.class},
            method = SpirePatch.CONSTRUCTOR
    )
    public static class NoAnimationsPatch {
        @SpireInsertPatch(loc = 66)
        public static SpireReturn Sentry(Sentry _instance, float x, float y) {
            if (shouldGoFast) {
                _instance.state = new AnimationStateFast();
                return SpireReturn.Return(null);
            }
            return SpireReturn.Continue();
        }
    }

    @SpirePatch(
            clz = Sentry.class,
            paramtypez = {},
            method = "takeTurn"
    )
    public static class SpyOnTakeTurnPatch {
        static long startTurn = 0;
        static int nextMove = 0;

        public static SpireReturn Prefix(Sentry _instance) {
            if (shouldGoFast) {
                startTurn = System.currentTimeMillis();
                nextMove = _instance.nextMove;
                return SpireReturn.Continue();
            }
            return SpireReturn.Continue();
        }
    }

    @SpirePatch(
            clz = EnergyManager.class,
            paramtypez = {},
            method = "recharge"
    )
    public static class SpyOnRecharrgePatch {
        public static SpireReturn Prefix(EnergyManager _instance) {
            if (shouldGoFast) {
                if (AbstractDungeon.player.hasRelic("Ice Cream")) {
                    if (EnergyPanel.totalCount > 0) {
                        AbstractDungeon.player.getRelic("Ice Cream").flash();
                    }

                    EnergyPanel.addEnergy(_instance.energy);
                } else if (AbstractDungeon.player.hasPower("Conserve")) {
                    if (EnergyPanel.totalCount > 0) {
                        AbstractDungeon.actionManager
                                .addToTop(new ReducePowerAction(AbstractDungeon.player, AbstractDungeon.player, "Conserve", 1));
                    }

                    EnergyPanel.addEnergy(_instance.energy);
                } else {
                    EnergyPanel.setEnergy(_instance.energy);
                }

                PostEnergyRechargeHook.Insert(_instance);
                return SpireReturn.Return(null);
            }
            return SpireReturn.Continue();
        }
    }

    @SpirePatch(
            clz = MakeTempCardInDiscardAction.class,
            paramtypez = {AbstractCard.class, int.class},
            method = SpirePatch.CONSTRUCTOR
    )
    public static class SpyOnMakeDazePatch {
        public static SpireReturn Prefix(MakeTempCardInDiscardAction _instance, AbstractCard card, int amount) {
            if (shouldGoFast) {
                ReflectionHacks
                        .setPrivate(_instance, MakeTempCardInDiscardAction.class, "numCards", amount);
                _instance.actionType = AbstractGameAction.ActionType.CARD_MANIPULATION;
                ReflectionHacks
                        .setPrivate(_instance, AbstractGameAction.class, "duration", .0001F);
                ReflectionHacks
                        .setPrivate(_instance, AbstractGameAction.class, "startDuration", .0001F);
                ReflectionHacks
                        .setPrivate(_instance, MakeTempCardInDiscardAction.class, "c", card);
                ReflectionHacks
                        .setPrivate(_instance, MakeTempCardInDiscardAction.class, "sameUUID", false);


                return SpireReturn.Return(null);
            }
            return SpireReturn.Continue();
        }
    }
}
