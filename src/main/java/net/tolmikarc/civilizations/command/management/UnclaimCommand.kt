/*
 * Copyright (c) 2021-2021 Tolmikarc All Rights Reserved
 */

package net.tolmikarc.civilizations.command.management

import net.tolmikarc.civilizations.menu.ConfirmMenu
import net.tolmikarc.civilizations.model.CivPlayer
import net.tolmikarc.civilizations.settings.Settings
import net.tolmikarc.civilizations.util.ClaimUtil.getRegionFromLocation
import org.mineacademy.fo.command.SimpleCommandGroup
import org.mineacademy.fo.command.SimpleSubCommand

class UnclaimCommand(parent: SimpleCommandGroup?) : SimpleSubCommand(parent, "unclaim") {
    override fun onCommand() {
        checkConsole()
        CivPlayer.fromBukkitPlayer(player).let { civPlayer ->
            checkNotNull(civPlayer.civilization, "You do not have a civilization")
            civPlayer.civilization?.apply {
                val regionToRemove = getRegionFromLocation(player.location, this)
                checkNotNull(regionToRemove, "There is no region at your location")
                fun run() {
                    removeClaim(regionToRemove!!)
                    tellSuccess("&cRemoved region successfully")
                }

                ConfirmMenu(
                    "&4Remove Region Here?",
                    "Use \"/civ claim visualize here\" to see this claim before deleting it.",
                    ::run
                ).displayTo(player)
            }
        }
    }

    init {
        setDescription("Remove region at your location from your Civilization's claims")
        if (!Settings.ALL_PERMISSIONS_ENABLED) permission = null
    }
}