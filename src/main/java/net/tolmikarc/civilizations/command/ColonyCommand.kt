package net.tolmikarc.civilizations.command

import net.tolmikarc.civilizations.model.CivPlayer
import net.tolmikarc.civilizations.model.Colony
import net.tolmikarc.civilizations.settings.Settings
import net.tolmikarc.civilizations.task.CooldownTask
import net.tolmikarc.civilizations.task.CooldownTask.Companion.addCooldownTimer
import net.tolmikarc.civilizations.task.CooldownTask.Companion.getCooldownRemaining
import net.tolmikarc.civilizations.task.CooldownTask.Companion.hasCooldown
import org.bukkit.Location
import org.mineacademy.fo.Common
import org.mineacademy.fo.command.SimpleCommandGroup
import org.mineacademy.fo.command.SimpleSubCommand
import java.util.*
import java.util.function.Consumer

class ColonyCommand(parent: SimpleCommandGroup?) : SimpleSubCommand(parent, "colony") {
    override fun onCommand() {
        checkConsole()
        CivPlayer.fromBukkitPlayer(player).let { civPlayer ->
            checkNotNull(civPlayer.civilization, "You do not have a Civilization")
            civPlayer.civilization?.let { civ ->
                if (args[0].equals("?", ignoreCase = true)) {
                    val colonyIds: MutableList<String> = ArrayList()
                    for (colony in civ.colonies) {
                        colonyIds.add(colony.id.toString())
                    }
                    tell("Colonies: " + Common.join(colonyIds, ", "))
                    return
                }
                val id = findNumber(0, "Please specify a valid number")
                var location: Location? = null
                for (colony in civ.colonies) {
                    if (colony.id == id) location = colony.warp
                }
                checkNotNull(location, "There is no Colony with the specified ID $id")
                checkBoolean(
                    !hasCooldown(civPlayer.playerUUID, CooldownTask.CooldownType.TELEPORT),
                    "Please wait " + getCooldownRemaining(
                        civPlayer.playerUUID,
                        CooldownTask.CooldownType.TELEPORT
                    ) + " seconds before teleporting again."
                )
                player.teleport(location!!)
                addCooldownTimer(civPlayer.playerUUID, CooldownTask.CooldownType.TELEPORT)
            }
        }
    }

    override fun tabComplete(): List<String>? {
        val colonies: MutableList<String> = ArrayList()
        val civPlayer = CivPlayer.fromBukkitPlayer(player)
        if (civPlayer.civilization != null) {
            val civilization = civPlayer.civilization!!
            civilization.colonies.forEach(Consumer { colony: Colony -> colonies.add(colony.id.toString()) })
        }
        return if (args.size == 1) colonies else null
    }

    init {
        setDescription("Teleport to a Civilization Colony")
        usage = "<id # | ?>"
        minArguments = 1
        if (!Settings.ALL_PERMISSIONS_ENABLED) permission = null
    }
}