/*
 * Copyright (c) 2021-2021 Tolmikarc All Rights Reserved
 */

package net.tolmikarc.civilizations.command.management

import net.tolmikarc.civilizations.model.CivPlayer
import net.tolmikarc.civilizations.settings.Settings
import net.tolmikarc.civilizations.util.MathUtil.doubleToMoney
import net.tolmikarc.civilizations.util.MathUtil.isDouble
import org.mineacademy.fo.command.SimpleCommandGroup
import org.mineacademy.fo.command.SimpleSubCommand
import org.mineacademy.fo.model.HookManager

class DepositCommand(parent: SimpleCommandGroup?) : SimpleSubCommand(parent, "deposit") {
    override fun onCommand() {
        checkConsole()
        CivPlayer.fromBukkitPlayer(player).let {
            checkNotNull(it.civilization, "You must have a civilization to put money into")
            it.civilization?.apply {
                checkBoolean(isDouble(args[0]), "Please type in a valid number")
                val amount = doubleToMoney(args[0].toDouble())
                checkBoolean(HookManager.getBalance(player) - amount > 0, "You do not have enough money to deposit")
                HookManager.withdraw(player, amount)
                addBalance(amount)
                tellSuccess("${Settings.PRIMARY_COLOR}Deposited ${Settings.SECONDARY_COLOR}" + amount + "${Settings.PRIMARY_COLOR} into your Civilization's Bank")
            }
        }
    }

    init {
        setDescription("Deposit Money into your Civilization")
        minArguments = 1
        if (!Settings.ALL_PERMISSIONS_ENABLED) permission = null
    }
}