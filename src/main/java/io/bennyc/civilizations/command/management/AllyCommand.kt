/*
 * Copyright (c) 2021-2021 Tolmikarc All Rights Reserved
 */

package io.bennyc.civilizations.command.management

import io.bennyc.civilizations.PermissionChecker
import io.bennyc.civilizations.manager.CivManager
import io.bennyc.civilizations.manager.PlayerManager
import io.bennyc.civilizations.settings.Localization
import io.bennyc.civilizations.settings.Settings
import org.mineacademy.fo.command.SimpleCommandGroup
import org.mineacademy.fo.command.SimpleSubCommand

class AllyCommand(parent: SimpleCommandGroup?) : SimpleSubCommand(parent, "ally") {
    override fun onCommand() {
        checkConsole()
        io.bennyc.civilizations.manager.PlayerManager.fromBukkitPlayer(player).let {
            checkNotNull(it.civilization, io.bennyc.civilizations.settings.Localization.Warnings.NO_CIV)
            it.civilization?.apply {
                checkBoolean(io.bennyc.civilizations.PermissionChecker.canManageCiv(it, this), io.bennyc.civilizations.settings.Localization.Warnings.CANNOT_MANAGE_CIV)
                val allyCivilization = io.bennyc.civilizations.manager.CivManager.getByName(args[1])
                checkNotNull(
                    allyCivilization,
                    io.bennyc.civilizations.settings.Localization.Warnings.INVALID_SPECIFIC_ARGUMENT.replace("{item}", io.bennyc.civilizations.settings.Localization.CIVILIZATION)
                )
                checkBoolean(allyCivilization != this, io.bennyc.civilizations.settings.Localization.Warnings.CANNOT_SPECIFY_SELF)
                when {
                    args[0].equals("add", ignoreCase = true) -> {
                        checkBoolean(
                            !relationships.allies.contains(allyCivilization),
                            io.bennyc.civilizations.settings.Localization.Warnings.ALREADY_ALLIES
                        )
                        checkBoolean(
                            !relationships.enemies.contains(allyCivilization),
                            io.bennyc.civilizations.settings.Localization.Warnings.ALLY_ENEMY
                        )
                        relationships.addAlly(allyCivilization!!)
                        tellSuccess(io.bennyc.civilizations.settings.Localization.Notifications.ALLIES_TRUE.replace("{civ}", allyCivilization.name!!))
                    }
                    args[0].equals("remove", ignoreCase = true) -> {
                        checkBoolean(
                            relationships.allies.contains(allyCivilization),
                            io.bennyc.civilizations.settings.Localization.Warnings.NOT_ALLY
                        )
                        relationships.removeAlly(allyCivilization!!)
                        tellSuccess(io.bennyc.civilizations.settings.Localization.Notifications.ALLIES_FALSE.replace("{civ}", allyCivilization.name!!))
                    }
                    else -> {
                        returnInvalidArgs()
                    }
                }
            }
        }

    }

    override fun tabComplete(): List<String>? {
        return if (args.size == 1) listOf("add", "remove") else null
    }

    init {
        setDescription("Ally a Civilization")
        minArguments = 2
        usage = "<add | remove> <Civilization>"
        if (!io.bennyc.civilizations.settings.Settings.ALL_PERMISSIONS_ENABLED) permission = null
    }
}