/*
 * Copyright (c) 2021-2021 Tolmikarc All Rights Reserved
 */

package net.tolmikarc.civilizations.command

import net.tolmikarc.civilizations.manager.PlayerManager
import net.tolmikarc.civilizations.settings.Settings
import net.tolmikarc.civilizations.task.CooldownTask
import net.tolmikarc.civilizations.task.CooldownTask.Companion.addCooldownTimer
import net.tolmikarc.civilizations.task.CooldownTask.Companion.getCooldownRemaining
import net.tolmikarc.civilizations.task.CooldownTask.Companion.hasCooldown
import org.mineacademy.fo.command.SimpleCommandGroup
import org.mineacademy.fo.command.SimpleSubCommand
import java.util.*

class WarpCommand(parent: SimpleCommandGroup?) : SimpleSubCommand(parent, "warp") {
    override fun onCommand() {
        checkConsole()
        PlayerManager.fromBukkitPlayer(player).run {
            checkNotNull(civilization, "You do not have a Civilization")
            val warp = civilization!!.warps[args[0]]
            checkNotNull(warp, "Your Civilization does not have a home.")
            checkBoolean(
                !hasCooldown(uuid, CooldownTask.CooldownType.TELEPORT),
                "Please wait " + getCooldownRemaining(
                    uuid,
                    CooldownTask.CooldownType.TELEPORT
                ) + " seconds before teleporting again."
            )
            player.teleport(warp!!)
            addCooldownTimer(uuid, CooldownTask.CooldownType.TELEPORT)
        }
    }

    override fun tabComplete(): List<String> {
        val warps: MutableList<String> = ArrayList()
        PlayerManager.fromBukkitPlayer(player).let { civPlayer ->
            if (civPlayer.civilization != null) {
                val civilization = civPlayer.civilization
                warps.addAll(civilization!!.warps.keys)
            }
            return if (args.size == 1) warps else super.tabComplete()
        }
        return super.tabComplete()
    }

    init {
        setDescription("Teleport to a Civilization Warp")
        usage = "<warp>"
        minArguments = 1
        if (!Settings.ALL_PERMISSIONS_ENABLED) permission = null
    }
}