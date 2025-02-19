package com.partyhits.util;


import lombok.EqualsAndHashCode;
import lombok.Value;
import net.runelite.api.Player;
import net.runelite.client.party.messages.PartyMemberMessage;


@Value
@EqualsAndHashCode(callSuper = true)
public class Hit extends PartyMemberMessage
{
    int damage;
    AttackStyle attackStyle;
    String player;
}
