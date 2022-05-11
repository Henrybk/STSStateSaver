package savestate;

import basemod.helpers.CardModifierManager;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.helpers.CardLibrary;

import java.util.*;
import java.util.function.Function;

import static savestate.SaveStateMod.shouldGoFast;

public class CardState {
    public final String cardId;
    public final boolean upgraded;
    private final int timesUpgraded;
    public final int baseDamage;
    private final int baseBlock;

    public final int misc;
    public final int cost;

    private final int damage;

    private final int costForTurn;
    private final int magicNumber;
    public final int baseMagicNumber;
    private final int block;
    private final boolean freeToPlayOnce;
    private final String name;

    private final boolean inBottleTornado;
    private final boolean inBottleLightning;
    private final boolean inBottleFlame;
    private final boolean isCostModifiedForTurn;
    private final boolean isCostModified;
    private final boolean dontTriggerOnUseCard;
    private final boolean exhaust;
    private final boolean purgeOnUse;
    private final boolean isEthereal;
    private final boolean retain;
    private final boolean selfRetain;
    private final boolean shuffleBackIntoDrawPile;

    private static HashMap<String, HashSet<AbstractCard>> freeCards;

    private final UUID uuid;

    // Everything works without these, there is just s wonky 'draw' animation that can be avoided
    // by setting all the physical properties right away
    private final float current_x;
    private final float current_y;
    private final float target_x;
    private final float target_y;
    private final float angle;
    private final float targetAngle;
    private final float drawScale;
    private final float targetDrawScale;

    private final ArrayList<AbstractCardModifierState> cardModifiers;

    // private final HitboxState hb;
    public CardState(AbstractCard card) {
        long cardConstructorStartTime = System.currentTimeMillis();

        this.cardId = card.cardID;
        this.block = card.block;
        this.upgraded = card.upgraded;
        this.baseDamage = card.baseDamage;
        this.cost = card.cost;
        this.exhaust = card.exhaust;
        this.purgeOnUse = card.purgeOnUse;
        this.retain = card.retain;
        this.damage = card.damage;
        this.costForTurn = card.costForTurn;

        this.inBottleFlame = card.inBottleFlame;
        this.inBottleTornado = card.inBottleTornado;
        this.inBottleLightning = card.inBottleLightning;
        this.freeToPlayOnce = card.freeToPlayOnce;
        this.baseBlock = card.baseBlock;

        this.name = card.name;
        this.uuid = card.uuid;
        this.isCostModifiedForTurn = card.isCostModifiedForTurn;
        this.isCostModified = card.isCostModified;
        this.magicNumber = card.magicNumber;
        this.baseMagicNumber = card.baseMagicNumber;
        this.selfRetain = card.selfRetain;

        this.current_x = card.current_x;
        this.current_y = card.current_y;

        this.target_x = card.target_x;
        this.target_y = card.target_y;

        this.angle = card.angle;
        this.misc = card.misc;
        this.targetAngle = card.targetAngle;

        this.drawScale = card.drawScale;
        this.targetDrawScale = card.targetDrawScale;
        this.timesUpgraded = card.timesUpgraded;
        this.dontTriggerOnUseCard = card.dontTriggerOnUseCard;
        this.isEthereal = card.isEthereal;
        this.shuffleBackIntoDrawPile = card.shuffleBackIntoDrawPile;

        this.cardModifiers = new ArrayList<>();
        CardModifierManager.modifiers(card).forEach(modifier -> cardModifiers
                .add(AbstractCardModifierState.forModifier(modifier)));
    }

    public CardState(String jsonString) {
        JsonObject parsed = new JsonParser().parse(jsonString).getAsJsonObject();

        this.cardId = parsed.get("card_id").getAsString();
        this.upgraded = parsed.get("upgraded").getAsBoolean();
        this.baseDamage = parsed.get("base_damage").getAsInt();
        this.cost = parsed.get("cost").getAsInt();
        this.costForTurn = parsed.get("cost_for_turn").getAsInt();

        this.inBottleLightning = parsed.get("in_bottle_lightning").getAsBoolean();
        this.inBottleTornado = parsed.get("in_bottle_tornado").getAsBoolean();
        this.inBottleFlame = parsed.get("in_bottle_flame").getAsBoolean();

        this.name = parsed.get("name").getAsString();
        this.uuid = UUID.fromString(parsed.get("uuid").getAsString());
        this.freeToPlayOnce = parsed.get("free_to_play_once").getAsBoolean();
        this.isCostModifiedForTurn = parsed.get("is_cost_modified_for_turn").getAsBoolean();
        this.isCostModified = parsed.get("is_cost_modified").getAsBoolean();
        this.magicNumber = parsed.get("magic_number").getAsInt();
        this.block = parsed.get("block").getAsInt();
        this.baseMagicNumber = parsed.get("base_magic_number").getAsInt();
        this.baseBlock = parsed.get("base_block").getAsInt();
        this.timesUpgraded = parsed.get("times_upgraded").getAsInt();
        this.exhaust = parsed.get("exhaust").getAsBoolean();
        this.purgeOnUse = parsed.get("purge_on_use").getAsBoolean();
        this.isEthereal = parsed.get("is_ethereal").getAsBoolean();
        this.misc = parsed.get("misc").getAsInt();
        this.damage = parsed.get("damage").getAsInt();
        this.retain = parsed.get("retain").getAsBoolean();
        this.selfRetain = parsed.get("self_retain").getAsBoolean();
        this.shuffleBackIntoDrawPile = parsed.get("shuffle_back_into_draw_pile").getAsBoolean();

        this.cardModifiers = new ArrayList<>();
        parsed.get("modifiers").getAsJsonArray().forEach(jsonElement -> cardModifiers
                .add(AbstractCardModifierState.forString(jsonElement.getAsString())));

        // TODO
        this.current_x = 0;
        this.current_y = 0;

        this.target_x = 0;
        this.target_y = 0;

        this.angle = 0;
        this.targetAngle = 0;

        this.drawScale = 1.0F;
        this.targetDrawScale = 1.0F;
        this.dontTriggerOnUseCard = false;
    }

    public AbstractCard loadCard() {
        AbstractCard result = getCard(cardId);

        result.upgraded = upgraded;
        result.current_x = current_x;
        result.current_y = current_y;

        result.target_x = target_x;
        result.target_y = target_y;

        result.angle = angle;
        result.targetAngle = targetAngle;

        result.drawScale = drawScale;
        result.targetDrawScale = targetDrawScale;

        result.baseDamage = baseDamage;
        result.cost = cost;
        result.costForTurn = costForTurn;

        result.inBottleLightning = inBottleLightning;
        result.inBottleFlame = inBottleFlame;
        result.inBottleTornado = inBottleTornado;
        result.name = name;

        result.uuid = uuid;
        result.freeToPlayOnce = freeToPlayOnce;
        result.isCostModifiedForTurn = isCostModifiedForTurn;
        result.isCostModified = isCostModified;
        result.magicNumber = magicNumber;
        result.baseMagicNumber = baseMagicNumber;
        result.block = block;
        result.baseBlock = baseBlock;
        result.timesUpgraded = timesUpgraded;
        result.exhaust = exhaust;
        result.purgeOnUse = purgeOnUse;
        result.dontTriggerOnUseCard = dontTriggerOnUseCard;
        result.isEthereal = isEthereal;
        result.misc = misc;
        result.retain = retain;
        result.damage = damage;
        result.selfRetain = selfRetain;
        result.shuffleBackIntoDrawPile = shuffleBackIntoDrawPile;

        for (AbstractCardModifierState modifierState : cardModifiers) {
            CardModifierManager.addModifier(result, modifierState.loadModifier());
        }

        return result;
    }

    public String getName() {
        return cardId;
    }

    public String encode() {
        JsonObject cardStateJson = new JsonObject();

        cardStateJson.addProperty("card_id", cardId);
        cardStateJson.addProperty("upgraded", upgraded);
        cardStateJson.addProperty("base_damage", baseDamage);
        cardStateJson.addProperty("cost", cost);
        cardStateJson.addProperty("cost_for_turn", costForTurn);
        cardStateJson.addProperty("in_bottle_lightning", inBottleLightning);
        cardStateJson.addProperty("in_bottle_flame", inBottleFlame);
        cardStateJson.addProperty("in_bottle_tornado", inBottleTornado);
        cardStateJson.addProperty("name", name);
        cardStateJson.addProperty("free_to_play_once", freeToPlayOnce);
        cardStateJson.addProperty("uuid", uuid.toString());
        cardStateJson.addProperty("is_cost_modified_for_turn", isCostModifiedForTurn);
        cardStateJson.addProperty("is_cost_modified", isCostModified);
        cardStateJson.addProperty("magic_number", magicNumber);
        cardStateJson.addProperty("block", block);
        cardStateJson.addProperty("base_magic_number", baseMagicNumber);
        cardStateJson.addProperty("base_block", baseBlock);
        cardStateJson.addProperty("times_upgraded", timesUpgraded);
        cardStateJson.addProperty("exhaust", exhaust);
        cardStateJson.addProperty("is_ethereal", isEthereal);
        cardStateJson.addProperty("purge_on_use", purgeOnUse);
        cardStateJson.addProperty("misc", misc);
        cardStateJson.addProperty("damage", damage);
        cardStateJson.addProperty("retain", retain);
        cardStateJson.addProperty("self_retain", selfRetain);
        cardStateJson.addProperty("shuffle_back_into_draw_pile", shuffleBackIntoDrawPile);

        JsonArray modifierJsonArray = new JsonArray();
        for (AbstractCardModifierState state : cardModifiers) {
            modifierJsonArray.add(state.encode());
        }
        cardStateJson.add("modifiers", modifierJsonArray);

        return cardStateJson.toString();
    }

    public String diffEncode() {
        JsonObject cardStateJson = new JsonObject();

        cardStateJson.addProperty("card_id", cardId);
        cardStateJson.addProperty("cost", cost);
        cardStateJson.addProperty("cost_for_turn", costForTurn);
        cardStateJson.addProperty("upgraded", upgraded);
        cardStateJson.addProperty("base_magic_number", baseMagicNumber);

        return cardStateJson.toString();
    }

    public static void resetFreeCards() {
        freeCards = new HashMap<>();
    }

    public static void freeCardList(List<AbstractCard> cards) {
        cards.forEach(CardState::freeCard);
        cards.clear();
    }

    public static void freeCard(AbstractCard card) {
        if (card == null) {
            return;
        }

        CardModifierManager.modifiers(card).clear();

        if (freeCards == null) {
            freeCards = new HashMap<>();
        }

        String key = card.cardID;

        if (!freeCards.containsKey(key)) {
            freeCards.put(key, new HashSet<>());
        }

        if (freeCards.get(key).size() > 1000) {
            return;
        }

        freeCards.get(key).add(card);
    }

    public static AbstractCard getCard(String key) {
        Optional<AbstractCard> resultOptional = getCachedCard(key);

        AbstractCard result;
        if (resultOptional.isPresent() && shouldGoFast) {
            result = resultOptional.get();
        } else {
            result = getFreshCard(key);
        }

        return result;
    }

    private static Optional<AbstractCard> getCachedCard(String key) {
        if (freeCards == null || !freeCards.containsKey(key) || freeCards.get(key).isEmpty()) {
            return Optional.empty();
        }

        Iterator<AbstractCard> iterator = freeCards.get(key).iterator();
        AbstractCard result = iterator.next();
        iterator.remove();

        return Optional.of(result);
    }

    private static AbstractCard getFreshCard(String key) {
        AbstractCard card = CardLibrary.getCard(key);

        if (card == null) {
            System.err.println("can't find " + key);
        }

        return card.makeCopy();
    }

    public static int indexForCard(AbstractCard card) {
        int testIndex = 0;

        for (AbstractCard candidate : AbstractDungeon.player.hand.group) {
            if (card == candidate) {
                return testIndex;
            }
            testIndex++;
        }

        for (AbstractCard candidate : AbstractDungeon.player.discardPile.group) {
            if (card == candidate) {
                return testIndex;
            }
            testIndex++;
        }

        for (AbstractCard candidate : AbstractDungeon.player.drawPile.group) {
            if (card == candidate) {
                return testIndex;
            }
            testIndex++;
        }

        if (card == AbstractDungeon.player.cardInUse) {
            return testIndex;
        }
        testIndex++;


        for (AbstractCard candidate : AbstractDungeon.player.exhaustPile.group) {
            if (card == candidate) {
                return testIndex;
            }
            testIndex++;
        }

        return -1;
    }

    public static AbstractCard cardForIndex(int index) {
        if (index == -1) {
            throw new IllegalStateException("No card found");
        }

        int testIndex = 0;

        for (AbstractCard candidate : AbstractDungeon.player.hand.group) {
            if (index == testIndex) {
                return candidate;
            }
            testIndex++;
        }

        for (AbstractCard candidate : AbstractDungeon.player.discardPile.group) {
            if (index == testIndex) {
                return candidate;
            }
            testIndex++;
        }

        for (AbstractCard candidate : AbstractDungeon.player.drawPile.group) {
            if (index == testIndex) {
                return candidate;
            }
            testIndex++;
        }

        if (index == testIndex) {
            return AbstractDungeon.player.cardInUse;
        } else {
            testIndex++;
        }

        for (AbstractCard candidate : AbstractDungeon.player.exhaustPile.group) {
            if (index == testIndex) {
                return candidate;
            }
            testIndex++;
        }

        return null;
    }

    public static class CardFactories {
        public final Function<AbstractCard, Optional<CardState>> factory;
        public final Function<String, Optional<CardState>> jsonFactory;

        public CardFactories(Function<AbstractCard, Optional<CardState>> factory, Function<String, Optional<CardState>> jsonFactory) {
            this.factory = factory;
            this.jsonFactory = jsonFactory;
        }

        public CardFactories(Function<AbstractCard, Optional<CardState>> factory) {
            this.factory = factory;
            this.jsonFactory = json -> Optional.of(new CardState(json));
        }
    }

    public static CardState forCard(AbstractCard card) {
        for (CardFactories factories : StateFactories.cardFactories) {
            Optional<CardState> cardOptional = factories.factory.apply(card);
            if (cardOptional.isPresent()) {
                return cardOptional.get();
            }
        }

        return new CardState(card);
    }

    public static CardState forString(String jsonString) {
        for (CardFactories factories : StateFactories.cardFactories) {
            Optional<CardState> cardOptional = factories.jsonFactory.apply(jsonString);
            if (cardOptional.isPresent()) {
                return cardOptional.get();
            }
        }

        return new CardState(jsonString);
    }
}
