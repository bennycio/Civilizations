/*
 * Copyright (c) 2021-2021 Tolmikarc All Rights Reserved
 */

package net.tolmikarc.civilizations.command.admin

import net.tolmikarc.civilizations.command.parents.PlotSubCommand
import net.tolmikarc.civilizations.manager.CivManager
import net.tolmikarc.civilizations.manager.PlayerManager
import net.tolmikarc.civilizations.settings.Settings
import org.mineacademy.fo.command.SimpleCommandGroup

class APlotCommand(parent: SimpleCommandGroup?) : PlotSubCommand(parent) {
    override fun onCommand() {
        checkConsole()
        val civPlayer = PlayerManager.fromBukkitPlayer(player)
        val civ = CivManager.getByName(args[0])
        checkNotNull(civ, "Please specify a valid Civilization.")
        civ?.apply {
            if (args.isNotEmpty())
                when (args[1].toLowerCase()) {
                    "visualize" -> visualize(civPlayer, this)
                    "define" -> definePlot(civPlayer, this)
                    "claim" -> claimPlot(civPlayer, this)
                    "delete" -> TODO()
                    "forsale" -> setPlotForSale(civPlayer, this)
                    "add" -> addMemberToPlot(civPlayer, this)
                }
            PlayerManager.queueForSaving(civPlayer)

        }
    }

    override fun tabComplete(): List<String>? {
        if (args.size == 2) return listOf("define", "claim", "visualize", "forsale", "add")
        return super.tabComplete()
    }

    init {
        minArguments = 2
        usage = "<civ> <define | claim | add | forsale | visualize> [...]"
        if (!Settings.ALL_PERMISSIONS_ENABLED) permission = null
    }
}