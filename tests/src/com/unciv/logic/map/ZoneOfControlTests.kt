package com.unciv.logic.map

import com.unciv.Constants
import com.unciv.logic.MapSaver
import com.unciv.logic.civilization.CivilizationInfo
import com.unciv.models.ruleset.Nation
import com.unciv.models.ruleset.Ruleset
import com.unciv.models.ruleset.RulesetCache
import com.unciv.testing.GdxTestRunner
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith


@RunWith(GdxTestRunner::class)
class ZoneOfControlTests {
    private var ruleSet = Ruleset()
    private var tileMap = TileMap()
    private var goodGuys = CivilizationInfo()
    private var goodUnit = MapUnit()
    private var fromTile = TileInfo()
    private var evilGuys = CivilizationInfo()
    private var evilUnit = MapUnit()

    @Before
    fun initTheWorld() {

        RulesetCache.loadRulesets()
        ruleSet = RulesetCache.getBaseRuleset()
        goodGuys.initCiv("Good Guys")
        evilGuys.initCiv(Constants.barbarians)

        // Sorry for the 320 char literal - simpler than constructing this world on foot
        @Suppress("SpellCheckingInspection")
        val worldZippedJson = "H4sIAAAAAAAAAJWSsQqDMBRFfygOmu2tHboUWmihQ+nwWjMENMpLpLYh/16DU4vCdRHEw/Hm3sSW+xMLtyYY8RSn17P9GIrCtR08aeWmb7QbfOjapF6dNPVVuKcgg0kq2MYcrA90iw/25mJE2DraC3vfsKuTin3nbbCdozhSUar39EgKgTMLoagS1I0ohh6kqPKpKwzOLIjObYJwiXpRJRxzy+YFPPoIt7Sh+E2DorDOsP6Fj0/D7g/MHIDNPwfACvSVoK9EfIgKirV0t1fjA9zSZKv1AhzSLDg6kkqD8TXWxuxbBO/pC3I2qwsUBgAA"
        tileMap = MapSaver.mapFromSavedString(worldZippedJson)
        tileMap.setTransients(ruleSet, false)

        evilUnit = placeUnit(0, 0, "Archer", evilGuys)
        goodUnit = placeUnit(1, 1, "Warrior", goodGuys)
        fromTile = goodUnit.getTile()
    }

    private fun CivilizationInfo.initCiv(name: String) {
        tech.unitsCanEmbark = true
        civName = name
        nation = Nation().apply {
            this.name = name
            cities = arrayListOf("The Capital")
        }
    }
    private fun placeUnit(x: Int, y: Int, name: String, civInfo: CivilizationInfo): MapUnit {
        val unit = ruleSet.units[name]!!.let {
            it.ruleset = ruleSet
            it.getMapUnit(ruleSet)
        }
        unit.assignOwner(civInfo, false)
        unit.currentMovement = 5f
        val tile = tileMap[x, y]
        tile.militaryUnit = unit
        tile.setUnitTransients(false)
        return unit
    }

    @Test
    fun testZocMovementCost() {
        @Suppress("UNUSED_VARIABLE")
        val otherEvilUnit = placeUnit(0, 2, "Archer", evilGuys)
        var destinationTile = tileMap[2,2]
        val moveAwayCost = goodUnit.movement.getMovementCostBetweenAdjacentTiles(fromTile, destinationTile, goodGuys)
        destinationTile = tileMap[1,0]
        val moveToCommonTileCost = goodUnit.movement.getMovementCostBetweenAdjacentTiles(fromTile, destinationTile, goodGuys)
        destinationTile = tileMap[1,2]
        val moveToOtherZocCost = goodUnit.movement.getMovementCostBetweenAdjacentTiles(fromTile, destinationTile, goodGuys)

        Assert.assertTrue("ZOC rules: moving away from enemy should cost 1 but costs $moveAwayCost", moveAwayCost == 1f)
        Assert.assertTrue("ZOC rules: moving within ZOC should cost all MP but costs $moveToCommonTileCost", moveToCommonTileCost > 99f)
        Assert.assertTrue("ZOC rules: moving to another unit's ZOC should cost 1 but costs $moveToOtherZocCost", moveToOtherZocCost == 1f)
    }

    /*
    @Test
    fun testZocCanReach() {
        val destinationTile = tileMap[0,-1]     // 2 spaces around the enemy, reachable in 4 moves
        goodUnit.currentMovement = 4f
        val canReachIn4 = goodUnit.movement.canReachInCurrentTurn(destinationTile)
        goodUnit.currentMovement = 3f
        val canReachIn3 = goodUnit.movement.canReachInCurrentTurn(destinationTile)
        Assert.assertTrue("ZOC rules: Leaving and reentering ZOC possible in 4 moves", canReachIn4)
        Assert.assertFalse("ZOC rules: Leaving and reentering ZOC not possible in 3 moves", canReachIn3)
    }
    */

    @Test
    fun measureZocMovementCostPerformance() {
        val runtime = Runtime.getRuntime()
        val startFreeMem = runtime.freeMemory()
        val startTime = System.nanoTime()
        /*val sumCosts =*/ measureZocMovementCostPerformanceRunner(iterations = 100_000_000)
        val endTime = System.nanoTime()
        val endFreeMem = runtime.freeMemory()
        println("measureZocMovementCostPerformance took ${(endTime-startTime)/1_000_000_000f}s and ${(startFreeMem-endFreeMem)/1024}kB")
        //Assert.assertTrue("ZOC rules: Performance test should calculate a sum of 0.8*iterations", sumCosts == 40000000.0)
    }

    private fun measureZocMovementCostPerformanceRunner(iterations: Int): Double {
        val destinationTiles = listOf(tileMap[1,0], tileMap[2,1], tileMap[2,2], tileMap[1,2], tileMap[0,1])
        var iteration = 0
        var sumCosts = 0.0
        while (true) {
            for (destinationTile in destinationTiles) {
                if (iteration++ >= iterations) return sumCosts
                sumCosts += goodUnit.movement.getMovementCostBetweenAdjacentTiles(fromTile, destinationTile, goodGuys)
            }
        }
    }
}