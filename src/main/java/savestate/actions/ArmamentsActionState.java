package savestate.actions;

import basemod.ReflectionHacks;
import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;
import com.megacrit.cardcrawl.actions.AbstractGameAction;
import com.megacrit.cardcrawl.actions.unique.ArmamentsAction;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import savestate.CardState;
import savestate.PlayerState;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.stream.Collectors;

public class ArmamentsActionState implements CurrentActionState {
    private final CardState[] cannotUpgrade;
    private final boolean upgraded;

    public ArmamentsActionState(AbstractGameAction action) {
        this((ArmamentsAction) action);
    }

    public ArmamentsActionState(ArmamentsAction action) {
        ArrayList<AbstractCard> cannotUpgradeSource = ReflectionHacks
                .getPrivate(action, ArmamentsAction.class, "cannotUpgrade");
        cannotUpgrade = PlayerState.toCardStateArray(cannotUpgradeSource);

        upgraded = ReflectionHacks
                .getPrivate(action, ArmamentsAction.class, "upgraded");
    }

    @Override
    public ArmamentsAction loadCurrentAction() {
        ArmamentsAction result = new ArmamentsAction(upgraded);

        ReflectionHacks
                .setPrivate(result, ArmamentsAction.class, "cannotUpgrade",
                        Arrays.stream(cannotUpgrade)
                        .map(CardState::loadCard)
                        .collect(Collectors
                                .toCollection(ArrayList::new)));
        ReflectionHacks
                .setPrivate(result, AbstractGameAction.class, "duration", 0);

        return result;
    }

    @SpirePatch(
            clz = ArmamentsAction.class,
            paramtypez = {},
            method = "update"
    )
    public static class NoDoubleArmamentsPatch {
        public static void Postfix(ArmamentsAction _instance) {
            // Force the action to stay in the the manager until cards are selected
            if (AbstractDungeon.isScreenUp) {
                _instance.isDone = false;
            }
        }
    }
}
