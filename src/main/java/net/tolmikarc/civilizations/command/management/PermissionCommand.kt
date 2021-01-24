/*
 * Copyright (c) 2021-2021 Tolmikarc All Rights Reserved
 */

package net.tolmikarc.civilizations.command.management

import net.tolmikarc.civilizations.PermissionChecker
import net.tolmikarc.civilizations.manager.PlayerManager
import net.tolmikarc.civilizations.model.Civ
import net.tolmikarc.civilizations.permissions.PermissionType
import net.tolmikarc.civilizations.settings.Localization
import net.tolmikarc.civilizations.settings.Settings
import org.mineacademy.fo.Common
import org.mineacademy.fo.command.SimpleCommandGroup
import org.mineacademy.fo.command.SimpleSubCommand

class PermissionCommand(parent: SimpleCommandGroup?) : SimpleSubCommand(parent, "perms|permissions") {
    override fun onCommand() {
        checkConsole()
        PlayerManager.fromBukkitPlayer(player).let { civPlayer ->
            checkNotNull(civPlayer.civilization, Localization.Warnings.NO_CIV)
            civPlayer.civilization?.apply {
                checkBoolean(PermissionChecker.canManageCiv(civPlayer, this), Localization.Warnings.CANNOT_MANAGE_CIV)
                if (args.size == 1) {
                    if (args[0].equals("options", ignoreCase = true)) {
                        tell(
                            "${Settings.PRIMARY_COLOR}Valid Groups: ${Settings.SECONDARY_COLOR}${
                                Common.join(
                                    permissions.ranks.map { it.name },
                                    ", "
                                )
                            }",
                            "${Settings.PRIMARY_COLOR}Valid Permissions: ${Settings.SECONDARY_COLOR}Build, Break, Switch, Interact",
                            "${Settings.PRIMARY_COLOR}Valid values: ${Settings.SECONDARY_COLOR}True, False"
                        )
                    } else returnInvalidArgs()
                }
                if (args.size == 3) {
                    val group = permissions.getGroupByName(args[0])
                    if (group == null) returnInvalidArgs()
                    group?.adjust(PermissionType.valueOf(args[1].toUpperCase()), args[2].toBoolean())
                    tellSuccess(Localization.Notifications.SUCCESS_COMMAND)
                } else
                    returnInvalidArgs()
            }
        }
    }

    override fun tabComplete(): List<String>? {
        var civ: Civ? = null
        if (PlayerManager.fromBukkitPlayer(player).civilization != null)
            civ = PlayerManager.fromBukkitPlayer(player).civilization
        return when (args.size) {
            1 -> civ?.permissions?.ranks?.map { it.name } ?: super.tabComplete()
            2 -> listOf("build", "break", "switch", "interact")
            3 -> listOf("true", "false")
            else -> super.tabComplete()
        }
    }

    init {
        minArguments = 1
        usage = "<permission> <group> <value>"
        setDescription("Allow different groups to perform various actions in your town. Use /civ perms options for a list of valid options.")
        if (!Settings.ALL_PERMISSIONS_ENABLED) permission = null
    }
}