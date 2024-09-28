package io.github.lightman314.lightmanscurrency.api.misc;

import io.github.lightman314.lightmanscurrency.common.util.IClientTracker;

import javax.annotation.Nonnull;

/**
 * An extension of {@link IClientTracker} that makes available methods to flag the object as exising on the logical server or client<br>
 * Normally not necessary, as most objects have their own ways of tracking which side they're on, but is useful for data that can be saved to an item stack via the {@link io.github.lightman314.lightmanscurrency.api.capability.money.CapabilityMoneyHandler#MONEY_HANDLER_ITEM} capabilty<br>
 * and any objects that attempt to get a {@link io.github.lightman314.lightmanscurrency.api.capability.money.IMoneyHandler IMoneyHandler} capability from an item should check for this interface
 */
public interface ISidedObject extends IClientTracker {

    @Nonnull
    Object flagAsClient();
    @Nonnull
    Object flagAsClient(boolean isClient);
    @Nonnull
    Object flagAsClient(@Nonnull IClientTracker tracker);

}