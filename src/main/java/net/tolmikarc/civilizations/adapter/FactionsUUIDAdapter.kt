/*
 * Copyright (c) 2021-2021 Tolmikarc All Rights Reserved
 */

package net.tolmikarc.civilizations.adapter

import com.massivecraft.factions.FLocation
import com.massivecraft.factions.FPlayer
import com.massivecraft.factions.Faction
import com.massivecraft.factions.perms.PermissibleAction
import com.massivecraft.factions.perms.Relation
import com.massivecraft.factions.perms.Role
import net.tolmikarc.civilizations.model.CivPlayer
import net.tolmikarc.civilizations.model.Civilization
import net.tolmikarc.civilizations.permissions.ClaimPermissions
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.BookMeta
import org.mineacademy.fo.region.Region
import java.util.*
import java.util.stream.Collectors

object FactionsUUIDAdapter {
    private val convertedFactions: MutableMap<Faction, Civilization> = HashMap()

    fun convertFactionToCiv(faction: Faction, deleteAfterConversion: Boolean): Civilization {
        val civ = Civilization(UUID.randomUUID())
        civ.name = faction.tag
        civ.leader = CivPlayer.fromUUID(faction.getFPlayersWhereRole(Role.ADMIN)[0].player.uniqueId)
            ?: CivPlayer.initializeCivPlayer(faction.getFPlayersWhereRole(Role.ADMIN)[0].player.uniqueId)
        civ.claimPermissions = convertPermissions(faction)
        civ.claims = getConvertedRegions(faction, civ)
        civ.officials = faction.getFPlayersWhereRole(Role.COLEADER).stream()
            .map { fPlayer: FPlayer -> CivPlayer.fromUUID(fPlayer.player.uniqueId) }.collect(Collectors.toSet())
        civ.citizens =
            (faction.fPlayers.stream().map { fPlayer: FPlayer -> CivPlayer.fromUUID(fPlayer.player.uniqueId) }
                .collect(Collectors.toSet()))
        civ.idNumber = civ.totalClaimCount + 1
        civ.power = faction.power.toInt()
        civ.home = faction.home
        val book = ItemStack(Material.WRITTEN_BOOK)
        val bookMeta = book.itemMeta as BookMeta?
        bookMeta!!.addPage("Civ: " + civ.name.toString() + " Description: " + faction.description)
        book.itemMeta = bookMeta
        civ.book = book
        convertedFactions[faction] = civ
        if (deleteAfterConversion) faction.remove()
        return civ
    }

    private fun getConvertedRegions(faction: Faction, civ: Civilization): MutableSet<Region> {
        val handledFactionLocations: MutableSet<FLocation> = HashSet()
        val newRegions: MutableSet<Region> = HashSet()
        var id = 0
        for (fLocation in faction.allClaims) {
            if (handledFactionLocations.contains(fLocation)) continue
            var lowestLocation = fLocation
            var highestLocation = fLocation
            while (faction.allClaims.contains(highestLocation.getRelative(1, 0))) {
                val nextFLocation = highestLocation.getRelative(1, 0)
                highestLocation = nextFLocation
                handledFactionLocations.add(nextFLocation)
            }
            while (faction.allClaims.contains(lowestLocation.getRelative(0, 1))) {
                val nextFLocation = lowestLocation.getRelative(0, 1)
                val handledLocationsMovingRight: MutableSet<FLocation> = HashSet()
                while (faction.allClaims.contains(nextFLocation.getRelative(1, 0))) {
                    val nextNextFLocation = nextFLocation.getRelative(1, 0)
                    handledLocationsMovingRight.add(nextNextFLocation)
                    if (nextNextFLocation == highestLocation.getRelative(0, 1)) break
                }
                if (nextFLocation != highestLocation.getRelative(0, 1)) break
                highestLocation = nextFLocation.getRelative(0, 1)
                lowestLocation = lowestLocation.getRelative(0, 1)
                handledFactionLocations.add(nextFLocation)
                handledFactionLocations.addAll(handledLocationsMovingRight)
            }
            lowestLocation = fLocation
            val newRegion = Region(
                civ.uuid.toString() + "CLAIM" + id,
                Location(
                    lowestLocation.world,
                    (lowestLocation.x * 16).toDouble(),
                    0.0,
                    (lowestLocation.z * 16).toDouble()
                ),
                Location(
                    highestLocation.world,
                    (highestLocation.x * 16 + 15).toDouble(),
                    256.0,
                    (highestLocation.z * 16 + 15).toDouble()
                )
            )
            newRegions.add(newRegion)
            id++
        }
        return newRegions
    }

    private fun convertPermissions(faction: Faction): ClaimPermissions {
        val permissions = ClaimPermissions()
        val perms: Array<BooleanArray> = permissions.permissions
        perms[ClaimPermissions.PermGroup.OFFICIAL.id][ClaimPermissions.PermType.BUILD.id] =
            faction.permissions[Role.COLEADER]!![PermissibleAction.BUILD]!!
        perms[ClaimPermissions.PermGroup.OFFICIAL.id][ClaimPermissions.PermType.BREAK.id] =
            faction.permissions[Role.COLEADER]!![PermissibleAction.DESTROY]!!
        perms[ClaimPermissions.PermGroup.OFFICIAL.id][ClaimPermissions.PermType.SWITCH.id] =
            faction.permissions[Role.COLEADER]!![PermissibleAction.CONTAINER]!!
        perms[ClaimPermissions.PermGroup.OFFICIAL.id][ClaimPermissions.PermType.INTERACT.id] =
            faction.permissions[Role.COLEADER]!![PermissibleAction.ITEM]!!
        perms[ClaimPermissions.PermGroup.ALLY.id][ClaimPermissions.PermType.BUILD.id] =
            faction.permissions[Relation.ALLY]!![PermissibleAction.BUILD]!!
        perms[ClaimPermissions.PermGroup.ALLY.id][ClaimPermissions.PermType.BREAK.id] =
            faction.permissions[Relation.ALLY]!![PermissibleAction.DESTROY]!!
        perms[ClaimPermissions.PermGroup.ALLY.id][ClaimPermissions.PermType.SWITCH.id] =
            faction.permissions[Relation.ALLY]!![PermissibleAction.CONTAINER]!!
        perms[ClaimPermissions.PermGroup.ALLY.id][ClaimPermissions.PermType.INTERACT.id] =
            faction.permissions[Relation.ALLY]!![PermissibleAction.ITEM]!!
        perms[ClaimPermissions.PermGroup.MEMBER.id][ClaimPermissions.PermType.BUILD.id] =
            faction.permissions[Role.NORMAL]!![PermissibleAction.BUILD]!!
        perms[ClaimPermissions.PermGroup.MEMBER.id][ClaimPermissions.PermType.BREAK.id] =
            faction.permissions[Role.NORMAL]!![PermissibleAction.DESTROY]!!
        perms[ClaimPermissions.PermGroup.MEMBER.id][ClaimPermissions.PermType.SWITCH.id] =
            faction.permissions[Role.NORMAL]!![PermissibleAction.CONTAINER]!!
        perms[ClaimPermissions.PermGroup.MEMBER.id][ClaimPermissions.PermType.INTERACT.id] =
            faction.permissions[Role.NORMAL]!![PermissibleAction.ITEM]!!
        perms[ClaimPermissions.PermGroup.OUTSIDER.id][ClaimPermissions.PermType.BUILD.id] =
            faction.permissions[Relation.ENEMY]!![PermissibleAction.BUILD]!!
        perms[ClaimPermissions.PermGroup.OUTSIDER.id][ClaimPermissions.PermType.BREAK.id] =
            faction.permissions[Relation.ENEMY]!![PermissibleAction.DESTROY]!!
        perms[ClaimPermissions.PermGroup.OUTSIDER.id][ClaimPermissions.PermType.SWITCH.id] =
            faction.permissions[Relation.ENEMY]!![PermissibleAction.CONTAINER]!!
        perms[ClaimPermissions.PermGroup.OUTSIDER.id][ClaimPermissions.PermType.INTERACT.id] =
            faction.permissions[Relation.ENEMY]!![PermissibleAction.ITEM]!!
        permissions.permissions = (perms)
        return permissions
    }
}