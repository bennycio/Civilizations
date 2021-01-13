/*
 * Copyright (c) 2021-2021 Tolmikarc All Rights Reserved
 */

package net.tolmikarc.civilizations.command.admin

import net.tolmikarc.civilizations.manager.CivManager
import net.tolmikarc.civilizations.manager.PlayerManager
import net.tolmikarc.civilizations.settings.Settings
import org.mineacademy.fo.command.SimpleCommandGroup
import org.mineacademy.fo.command.SimpleSubCommand

class AAllyCommand(parent: SimpleCommandGroup?) : SimpleSubCommand(parent, "ally") {
    override fun onCommand() {
        PlayerManager.fromBukkitPlayer(player).let {
            checkConsole()
            val civ = CivManager.getByName(args[0])
            checkNotNull(civ, "Please specify a valid Civilization.")
            val allyCiv = CivManager.getByName(args[1])
            checkNotNull(allyCiv, "Please specify a valid enemy Civilization")
            checkBoolean(allyCiv != civ, "You cannot make this civ ally itself")
            civ?.apply {
                when {
                    args[0].equals("add", ignoreCase = true) -> {
                        checkBoolean(
                            !relationships.allies.contains(allyCiv),
                            "$name is already allies with this civilization"
                        )
                        checkBoolean(
                            !relationships.enemies.contains(allyCiv),
                            "$name cannot ally an enemy Civilization."
                        )
                        relationships.addAlly(allyCiv!!)
                        tell("${Settings.PRIMARY_COLOR}$name is now allies with ${Settings.SECONDARY_COLOR}" + allyCiv.name)
                    }
                    args[0].equals("remove", ignoreCase = true) -> {
                        checkBoolean(
                            relationships.allies.contains(allyCiv),
                            "This Civilization is not an ally."
                        )
                        relationships.removeAlly(allyCiv!!)
                        tell("${Settings.PRIMARY_COLOR}$name is no longer allies with ${Settings.SECONDARY_COLOR}" + allyCiv.name)
                    }
                    else -> {
                        returnInvalidArgs()
                    }
                }
            }
        }

    }

    override fun tabComplete(): List<String>? {
        return if (args.size == 2) listOf("add", "remove") else null
    }

    init {
        setDescription("Ally a Civilization")
        minArguments = 2
        usage = "<civ> <add | remove> <other civ>"
    }
}